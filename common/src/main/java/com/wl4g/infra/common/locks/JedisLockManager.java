/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.infra.common.locks;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import com.google.common.annotations.Beta;
import com.wl4g.infra.common.jedis.BasicJedisClient;

import lombok.CustomLog;
import lombok.ToString;

/**
 * JEDIS locks manager.
 *
 * @author wangl.sir
 * @version v1.0 2019年3月19日
 * @since
 */
@CustomLog
@ToString
public class JedisLockManager {
    protected static final String NAMESPACE = "reentrantUnfairLock.";
    protected static final String NXXX = "NX";
    protected static final String EXPX = "PX";
    protected static final long FRAME_INTERVAL_MS = 30L;
    protected static final String UNLOCK_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    protected final BasicJedisClient jedisClient;

    public JedisLockManager(BasicJedisClient jedisClient) {
        this.jedisClient = notNullOf(jedisClient, "jedisClient");
    }

    /**
     * Get and create {@link FastReentrantUnfairDistributedRedLock} with name.
     * 
     * @param lockName
     * @return
     */
    public Lock getLock(@NotBlank String lockName) {
        return getLock(lockName, 10, TimeUnit.SECONDS);
    }

    /**
     * Get and create {@link FastReentrantUnfairDistributedRedLock} with name.
     * 
     * @param lockName
     * @param timeoutMs
     * @param unit
     * @return
     */
    public Lock getLock(@NotBlank String lockName, @Min(0) long timeoutMs, TimeUnit unit) {
        hasTextOf(lockName, "lockName");
        isTrueOf(timeoutMs > 0, "timeoutMs > 0");
        notNullOf(unit, "unit");
        return new FastReentrantUnfairDistributedRedLock(jedisClient, lockName, unit.toMillis(timeoutMs));
    }

    /**
     * Fast unsafe reentrant unfair redlock implemented by REDIS cluster.</br>
     * </br>
     * <font color=red style="text-decoration:underline;">Note: This
     * implementation is not strictly strong consistency. It is recommended to
     * use this distributed lock for scenarios with high performance
     * requirements and low consistency requirements. On the contrary, for
     * scenarios with high consistency requirements (such as orders, payments,
     * etc.), please do not use this lock. You can use the lock of Raft/Paxos
     * strong consistency algorithm such as zookeeper distributed lock. Because
     * redis cluster distributed locks, for example, when the master dies before
     * copying to the slave or when the master and slave die together, there
     * will be serious consequences of locking.</font>
     * 
     * @author James Wong <jameswong1376@gmail.com>
     * @version v1.0 2019年3月21日
     * @since
     * @see <a href=
     *      'https://blog.csdn.net/matt8/article/details/64442064'>Discuss
     *      Antirez failover analysis</a>
     * @see <a href=
     *      'https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html'>Martin
     *      Kleppmann analysis redlock</a>
     * @see <a href='https://redis.io/topics/distlock'>Antirez failover
     *      analysis</a>
     * @see <a href='http://antirez.com/news/101'>VS Martin Kleppmann for
     *      Redlock failover analysis</a>
     */
    @Beta
    @ToString(callSuper = true, exclude = { "jedisClient" })
    public static class FastReentrantUnfairDistributedRedLock extends AbstractDistributedLock {
        private static final long serialVersionUID = -1909894475263151824L;

        final BasicJedisClient jedisClient;

        /**
         * Current locker reentrant counter.</br>
         * <font color=red>Special Note: assuming that the situation of retry to
         * obtain lock occurs, it must be in the same JVM process.</font>
         */
        final AtomicLong counter;

        public FastReentrantUnfairDistributedRedLock(final BasicJedisClient jedisClient, String lockName, long expiredMs) {
            this(jedisClient, lockName, expiredMs, 0L);
        }

        public FastReentrantUnfairDistributedRedLock(final BasicJedisClient jedisClient, String lockName, long expiredMs,
                long counterValue) {
            this(jedisClient, lockName, expiredMs, new AtomicLong(counterValue));
        }

        public FastReentrantUnfairDistributedRedLock(final BasicJedisClient jedisClient, final String lockName,
                final long expiredMs, final AtomicLong counter) {
            super((NAMESPACE.concat(lockName)), getThreadCurrentProcessId(), expiredMs);
            this.jedisClient = notNullOf(jedisClient, "jedisClient");
            isTrueOf(counter.get() >= 0, "counter >= 0");
            this.counter = counter;
        }

        @Override
        public void lock() {
            try {
                lockInterruptibly();
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            if (interrupted())
                throw new InterruptedException();
            while (true) {
                if (doTryAcquire())
                    break;
                sleep(FRAME_INTERVAL_MS);
            }
        }

        @Override
        public boolean tryLock() {
            return doTryAcquire();
        }

        @Override
        public boolean tryLock(long tryTimeout, TimeUnit unit) throws InterruptedException {
            notNullOf(unit, "unit");
            isTrueOf((tryTimeout > 0 && tryTimeout <= expiredMs), "TryTimeout must be > 0 && <= " + expiredMs);
            long t = unit.toMillis(tryTimeout) / FRAME_INTERVAL_MS, c = 0;
            while (t > ++c) {
                if (doTryAcquire())
                    return true;
                sleep(FRAME_INTERVAL_MS);
            }
            return false;
        }

        @Override
        public void unlock() {
            // Obtain locked processId.
            String acquiredProcessId = jedisClient.get(getLockName());
            // Current thread is holder?
            if (!requestId.equals(acquiredProcessId)) {
                log.debug("No need to unlock of requestId: {}, acquiredProcessId: {}, counter: {}", requestId, acquiredProcessId,
                        counter);
                return;
            }

            // Obtain lock record once decrement.
            counter.decrementAndGet();
            log.debug("No need to unlock and reenter the stack lock layer, counter: {}", counter);

            if (counter.longValue() == 0L) { // All thread stack layers exited?
                Object res = jedisClient.eval(UNLOCK_LUA, singletonList(getLockName()), singletonList(requestId));
                if (!assertValidity(res)) {
                    log.debug("Failed to unlock for %{}@{}", requestId, getLockName());
                } else {
                    log.debug("Unlock successful for %{}@{}", requestId, getLockName());
                }
            }
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        /**
         * Execution try acquire locker by reentrant info.</br>
         * 
         * @see JedisLockManager.java
         * @return
         */
        private final boolean doTryAcquire() {
            String acquiredProcessId = jedisClient.get(getLockName()); // Locked-processId.
            if (requestId.equals(acquiredProcessId)) {
                // Obtain lock record once cumulatively.
                counter.incrementAndGet();
                log.debug("Reuse acquire lock for name: {}, acquiredProcessId: {}, counter: {}", getLockName(), acquiredProcessId,
                        counter);
                return true;
            } else {
                // Not currently locked? Lock expired? Local counter reset.
                counter.set(0L);
            }

            // Try to acquire a new lock from the server.
            if (assertValidity(jedisClient.setIfAbsent(getLockName(), requestId, expiredMs))) {
                // Obtain lock record once cumulatively.
                counter.incrementAndGet();
                return true;
            }
            return false;
        }

        /**
         * Assertion validate lock result is acquired/UnAcquired success?
         * 
         * @param res
         * @return
         */
        private final boolean assertValidity(Object res) {
            if (isNull(res)) {
                return false;
            }
            if (res instanceof String) {
                String res0 = res.toString().trim();
                return "1".equals(res0) || "OK".equalsIgnoreCase(res0);
            } else if (res instanceof Boolean) {
                return (boolean) res;
            } else if (res instanceof Number) {
                return ((Number) res).longValue() >= 1L;
            } else {
                throw new IllegalStateException(format("Unknown acquired state for %s", res));
            }
        }

    }

}
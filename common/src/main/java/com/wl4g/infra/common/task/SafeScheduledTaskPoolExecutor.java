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
package com.wl4g.infra.common.task;

import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.makeAccessible;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.nanoTime;
import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;

import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.Getter;
import lombok.ToString;

/**
 * An enhanced security and flexible scheduling executor.</br>
 * As the default {@link java.util.concurrent.ScheduledThreadPoolExecutor} and
 * {@link org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler} of
 * JDK do not limit the maximum task waiting queue, the problem of OOM may
 * occur, which is designed to use a bounded queue to solve this problem.
 * 
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2020年1月18日
 * @since
 * @see {@link org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler}
 * @see https://stackoverflow.com/questions/55429073/why-does-not-scheduledthreadpoolexecutor-provide-finite-queue
 */
public class SafeScheduledTaskPoolExecutor extends ScheduledThreadPoolExecutor {
    protected final Logger log = getLogger(getClass());

    /**
     * Maximum allowed waiting execution queue size.
     */
    private final int acceptQueue;

    /**
     * {@link RejectedExecutionHandler}
     */
    private final RejectedExecutionHandler rejectHandler;

    public SafeScheduledTaskPoolExecutor(int coreMaximumPoolSize, long keepAliveTimeMs, ThreadFactory threadFactory,
            int acceptQueue, RejectedExecutionHandler rejectHandler) {
        super(coreMaximumPoolSize, threadFactory, rejectHandler);
        setRemoveOnCancelPolicy(true);
        isTrue(acceptQueue > 0, "acceptQueue must be greater than 0");
        notNullOf(rejectHandler, "rejectHandler");
        setMaximumPoolSize(coreMaximumPoolSize); // corePoolSize==maximumPoolSize
        setKeepAliveTime(keepAliveTimeMs, MILLISECONDS);
        this.acceptQueue = acceptQueue;
        this.rejectHandler = rejectHandler;
    }

    /**
     * The {@link #invokeAll(Collection, long, TimeUnit)} or
     * {@link #invokeAny(Collection, long, TimeUnit)} related methods also call
     * {@link #execute(Runnable)} in the end. For details, see:
     * 
     * @see {@link java.util.concurrent.AbstractExecutorService#doInvokeAny()#176}
     * @see {@link java.util.concurrent.ExecutorCompletionService#submit()}
     */
    @Override
    public void execute(Runnable command) {
        if (checkRunableLimit(command)) {
            return;
        }
        super.execute(command);
    }

    @Override
    public Future<?> submit(Runnable task) {
        if (checkRunableLimit(task)) {
            return EMPTY_FUTURE;
        }
        return super.submit(task);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        if (checkRunableLimit(task)) {
            return (Future<T>) EMPTY_FUTURE;
        }
        return super.submit(task, result);
    }

    /**
     * Submitted job wait for completed.
     * 
     * @param jobs
     * @param timeoutMs
     * @param await
     * @return
     * @throws IllegalStateException
     */
    public <R> CompleteResult<R> submitForComplete(final @NotNull List<? extends Callable<R>> jobs, long timeoutMs)
            throws IllegalStateException {
        notNullOf(jobs, "jobs");
        isTrueOf(timeoutMs > 0, "timeoutMs > 0");

        final int allJobs = jobs.size();
        final Map<Callable<R>, Future<R>> futures = new LinkedHashMap<>(allJobs);

        // Completed results.
        final Map<Callable<R>, R> completed = new HashMap<>(futures.size());
        // Uncompleted results.
        final Set<Callable<R>> uncompleted = new HashSet<>();

        try {
            final CountDownLatch latch = new CountDownLatch(allJobs);
            jobs.stream().forEach(job -> futures.put(job, submit(new FutureDoneTask<>(latch, job))));

            final Iterator<Entry<Callable<R>, Future<R>>> it = futures.entrySet().iterator();
            if (!latch.await(timeoutMs, MILLISECONDS)) { // Timeout(part-done)
                while (it.hasNext()) {
                    final Entry<Callable<R>, Future<R>> entry = it.next();
                    final Callable<R> job = entry.getKey();
                    final Future<R> future = entry.getValue();
                    // @formatter:off
                    // System.out.printf("debug: job=%s, done=%s, cancelled=%s\n", job, future.isDone(), future.isCancelled());
                    // @formatter:on
                    // Notice: future.isCancelled() a true when the submitted
                    // task is rejected, see: #EMPTY_FUTURE_CANCELLED
                    if (future.isDone()) {
                        // Collect for completed results.
                        completed.putIfAbsent(job, future.get());
                    } else { // Collect for uncompleted results.
                        uncompleted.add(job);
                    }
                }
            } else { // All done
                while (it.hasNext()) {
                    // Collect for completed results.
                    final Entry<Callable<R>, Future<R>> entry = it.next();
                    completed.putIfAbsent(entry.getKey(), entry.getValue().get());
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return new CompleteResult<>(completed.values(), uncompleted);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (checkRunableLimit(command)) {
            return (ScheduledFuture<?>) EMPTY_FUTURE;
        }
        return super.schedule(command, delay, unit);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        // TODO return callable original value.
        if (checkCallableLimit(callable)) {
            return (ScheduledFuture<V>) EMPTY_FUTURE;
        }
        return super.schedule(callable, delay, unit);
    }

    /**
     * Random interval scheduling based on dynamic schedule.
     * 
     * @see {@link java.util.concurrent.ScheduledThreadPoolExecutor#scheduleAtFixedRate(Runnable, long, long, TimeUnit)}
     * 
     * @param runnable
     * @param initialDelay
     * @param minDelay
     * @param maxDelay
     * @param unit
     * @return
     */
    public ScheduledFuture<?> scheduleAtRandomRate(
            Runnable runnable,
            long initialDelay,
            long minDelay,
            long maxDelay,
            TimeUnit unit) {
        return scheduleAtFixedRate(new RandomScheduleRunnable(unit.toMillis(minDelay), unit.toMillis(maxDelay), runnable),
                unit.toMillis(initialDelay), MAX_VALUE, MILLISECONDS);
    }

    /**
     * Random interval scheduling based on fixed schedule.
     * 
     * @see {@link java.util.concurrent.ScheduledThreadPoolExecutor#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)}
     * 
     * @param runnable
     * @param initialDelay
     * @param minDelay
     * @param maxDelay
     * @param unit
     * @return
     */
    public ScheduledFuture<?> scheduleWithRandomDelay(
            Runnable runnable,
            long initialDelay,
            long minDelay,
            long maxDelay,
            TimeUnit unit) {
        return scheduleWithFixedDelay(new RandomScheduleRunnable(unit.toMillis(minDelay), unit.toMillis(maxDelay), runnable),
                unit.toMillis(initialDelay), MAX_VALUE, MILLISECONDS);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (checkRunableLimit(command))
            return null;
        return super.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (checkRunableLimit(command))
            return null;
        return super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    /**
     * @see {@link java.util.concurrent.ScheduledThreadPoolExecutor.ScheduledFutureTask}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        if (runnable instanceof RandomScheduleRunnable) {
            try {
                return new CustomScheduledFutureTask<V>((Callable) callableField.get(task), (long) timeField.get(task), this) {
                    @Override
                    public long getPeriod() {
                        return ((RandomScheduleRunnable) runnable).nextDelay();
                    }
                };
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return task;
    }

    /**
     * @see {@link java.util.concurrent.ScheduledThreadPoolExecutor.ScheduledFutureTask}
     */
    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        return decorateTask(EMPTY_RUNNABLE, task);
    }

    /**
     * Check whether the entry queue is rejected
     * 
     * @param command
     * @return
     */
    private boolean checkCallableLimit(Callable<?> command) {
        if (getQueue().size() > acceptQueue) {
            rejectHandler.rejectedExecution(() -> {
                try {
                    command.call();
                } catch (Exception e) {
                    throw new IllegalStateException();
                }
            }, this);
            return true;
        }
        return false;
    }

    /**
     * Check whether the entry queue is rejected
     * 
     * @param command
     * @return
     */
    private boolean checkRunableLimit(Runnable command) {
        if (getQueue().size() > acceptQueue) {
            rejectHandler.rejectedExecution(command, this);
            // throw new RejectedExecutionException("Rejected execution of " + r
            // + " on " + executor, executor.isShutdown());
            return true;
        }
        return false;
    }

    /**
     * @see {@link ScheduledThreadPoolExecutor.ScheduledFutureTask}
     * @param <V>
     * @author James Wong &lt;jameswong1376@gmail.com&gt;
     * @version 2020年1月20日 v1.0.0
     * @see
     */
    private class CustomScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

        /**
         * {@link ScheduledThreadPoolExecutor} instance object.
         */
        private ScheduledThreadPoolExecutor executor;

        /** Sequence number to break ties FIFO */
        private final long sequenceNumber;

        /** The time the task is enabled to execute in nanoTime units */
        private long time;

        /** The actual task to be re-enqueued by reExecutePeriodic */
        RunnableScheduledFuture<V> outerTask = this;

        /**
         * Index into delay queue, to support faster cancellation.
         */
        int heapIndex;

        /**
         * Creates a one-shot action with given nanoTime-based trigger time.
         */
        CustomScheduledFutureTask(Callable<V> callable, long ns, ScheduledThreadPoolExecutor executor) {
            super(callable);
            this.time = ns;
            this.executor = executor;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(time - nanoTime(), NANOSECONDS);
        }

        public int compareTo(Delayed other) {
            if (other == this) // compare zero if same object
                return 0;
            if (other instanceof CustomScheduledFutureTask) {
                CustomScheduledFutureTask<?> x = (CustomScheduledFutureTask<?>) other;
                long diff = time - x.time;
                if (diff < 0)
                    return -1;
                else if (diff > 0)
                    return 1;
                else if (sequenceNumber < x.sequenceNumber)
                    return -1;
                else
                    return 1;
            }
            long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
            return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
        }

        /**
         * Returns {@code true} if this is a periodic (not a one-shot) action.
         *
         * @return {@code true} if periodic
         */
        public boolean isPeriodic() {
            return getPeriod() != 0;
        }

        /**
         * Period in nanoseconds for repeating tasks. A positive value indicates
         * fixed-rate execution. A negative value indicates fixed-delay
         * execution. A value of 0 indicates a non-repeating task.
         */
        public long getPeriod() {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean cancelled = super.cancel(mayInterruptIfRunning);
            if (cancelled && getRemoveOnCancelPolicy() && heapIndex >= 0)
                remove(this);
            return cancelled;
        }

        /**
         * Overrides FutureTask version so as to reset/requeue if periodic.
         */
        @Override
        public void run() {
            boolean periodic = isPeriodic();
            if (!canRunInCurrentRunState(periodic))
                cancel(false);
            else if (!periodic)
                CustomScheduledFutureTask.super.run();
            else if (CustomScheduledFutureTask.super.runAndReset()) {
                setNextRunTime();

                // reExecutePeriodic(outerTask);
                try {
                    reExecutePeriodicMethod.invoke(executor, outerTask);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        /**
         * Sets the next time to run for a periodic task.
         */
        private void setNextRunTime() {
            long p = getPeriod();
            if (p > 0)
                time += p;
            else
                time = triggerTime(-p);
        }

        /**
         * Returns the trigger time of a delayed action.
         */
        private long triggerTime(long delay) {
            return nanoTime() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
        }

        /**
         * Constrains the values of all delays in the queue to be within
         * Long.MAX_VALUE of each other, to avoid overflow in compareTo. This
         * may occur if a task is eligible to be dequeued, but has not yet been,
         * while some other task is added with a delay of Long.MAX_VALUE.
         */
        private long overflowFree(long delay) {
            Delayed head = (Delayed) getQueue().peek();
            if (head != null) {
                long headDelay = head.getDelay(NANOSECONDS);
                if (headDelay < 0 && (delay - headDelay < 0))
                    delay = Long.MAX_VALUE + headDelay;
            }
            return delay;
        }

        /**
         * Returns true if can run a task given current run state and
         * run-after-shutdown parameters.
         *
         * @param periodic
         *            true if this task periodic, false if delayed
         */
        private boolean canRunInCurrentRunState(boolean periodic) {
            // return super.canRunInCurrentRunState(periodic);
            try {
                return (boolean) canRunInCurrentRunStateMethod.invoke(executor, periodic);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

    }

    /**
     * {@link FutureDoneTask}
     * 
     * @author James Wong &lt;jameswong1376@gmail.com&gt;
     * @version 2019年12月20日 v1.0.0
     * @see
     */
    @CustomLog
    private static class FutureDoneTask<R> implements Callable<R> {
        private final CountDownLatch latch;
        private final Callable<R> job;

        public FutureDoneTask(CountDownLatch latch, Callable<R> job) {
            this.latch = notNullOf(latch, "latch");
            this.job = notNullOf(job, "job");
        }

        @Override
        public R call() {
            try {
                return job.call();
            } catch (Throwable ex) {
                log.error("Failed to execution task.", ex);
                throw new IllegalStateException(ex);
            } finally {
                latch.countDown();
            }
        }
    }

    /**
     * {@link CompleteResult}
     * 
     * @author James Wong
     * @version 2023-02-01
     * @since v3.0.0
     */
    @Getter
    @ToString
    @AllArgsConstructor
    public static class CompleteResult<R> {
        private final Collection<R> completed;
        private final Collection<Callable<R>> uncompleted;
    }

    /**
     * {@link RandomScheduleRunnable}
     * 
     * @author James Wong &lt;jameswong1376@gmail.com&gt;
     * @version 2020年1月20日 v1.0.0
     * @see
     */
    public static class RandomScheduleRunnable implements Runnable {

        /**
         * Random min delay ms.
         */
        final private long minDelayMs;

        /**
         * Random max delay ms.
         */
        final private long maxDelayMs;

        /**
         * Runnable
         */
        final private Runnable runnable;

        public RandomScheduleRunnable(long minDelayMs, long maxDelayMs, Runnable runnable) {
            this.minDelayMs = notNullOf(minDelayMs, "minDelayMs");
            this.maxDelayMs = notNullOf(maxDelayMs, "maxDelayMs");
            this.runnable = notNullOf(runnable, "runnable");
        }

        public long nextDelay() {
            return MILLISECONDS.toNanos(current().nextLong(minDelayMs, maxDelayMs));
        }

        @Override
        public void run() {
            runnable.run();
        }

    }

    /**
     * Empty runnable.
     */
    private static final Runnable EMPTY_RUNNABLE = () -> {
    };

    // Corresponds to the submitForComplete method to check the task state.
    private static final boolean EMPTY_FUTURE_CANCELLED = true;
    private static final ScheduledFuture<Object> EMPTY_FUTURE = new ScheduledFuture<Object>() {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return EMPTY_FUTURE_CANCELLED; // [MARK1]
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed o) {
            return 0;
        }
    };

    /**
     * Sequence number to break scheduling ties, and in turn to guarantee FIFO
     * order among tied entries.
     */
    private static final AtomicLong sequencer = new AtomicLong();

    /**
     * {@link java.util.concurrent.ScheduledThreadPoolExecutor#reExecutePeriodic(RunnableScheduledFuture)}
     */
    private static final Method reExecutePeriodicMethod;

    /**
     * {@link java.util.concurrent.ScheduledThreadPoolExecutor#canRunInCurrentRunState(boolean)}
     */
    private static final Method canRunInCurrentRunStateMethod;

    /**
     * {@link java.util.concurrent.ScheduledThreadPoolExecutor.ScheduledFutureTask#callable}
     */
    private static final Field callableField;

    /**
     * {@link java.util.concurrent.ScheduledThreadPoolExecutor.ScheduledFutureTask#time}
     */
    private static final Field timeField;

    static {
        try {
            Class<?> scheduleFutureTaskClass = Class
                    .forName("java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask");
            callableField = findField(scheduleFutureTaskClass, "callable");
            makeAccessible(callableField);

            timeField = findField(scheduleFutureTaskClass, "time");
            makeAccessible(timeField);

            reExecutePeriodicMethod = ScheduledThreadPoolExecutor.class.getDeclaredMethod("reExecutePeriodic",
                    RunnableScheduledFuture.class);
            makeAccessible(reExecutePeriodicMethod);

            Method _canRunInCurrentRunStateMethod = null;
            try {
                // JDK8
                _canRunInCurrentRunStateMethod = ScheduledThreadPoolExecutor.class.getDeclaredMethod("canRunInCurrentRunState",
                        boolean.class);
            } catch (NoSuchMethodException e) {
                // JDK10.17+
                // see:https://github.com/openjdk/jdk/blob/jdk-10+17/jdk/src/java.base/share/classes/java/util/concurrent/ScheduledThreadPoolExecutor.java#L316
                // see:https://github.com/openjdk/jdk/blob/jdk-10+24/src/java.base/share/classes/java/util/concurrent/ScheduledThreadPoolExecutor.java#L316
                _canRunInCurrentRunStateMethod = ScheduledThreadPoolExecutor.class.getDeclaredMethod("canRunInCurrentRunState",
                        RunnableScheduledFuture.class);
            }
            canRunInCurrentRunStateMethod = _canRunInCurrentRunStateMethod;
            makeAccessible(canRunInCurrentRunStateMethod);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
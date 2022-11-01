/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.common.cache.jedis.cursor;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Charsets;
import com.wl4g.infra.common.cache.jedis.JedisClient;
import com.wl4g.infra.common.collection.CollectionUtils2;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.log.SmartLoggerFactory;
import com.wl4g.infra.common.reflect.ResolvableType;
import com.wl4g.infra.common.serialize.ProtostuffUtils;

import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.util.SafeEncoder;

/**
 * Redis client agnostic {@link CursorSpec} implementation continuously loading
 * additional results from Redis server until reaching its starting point
 * {@code zero}. <br />
 * 
 * <font color=red> Note: redis scan is reverse binary iteration, not sequential
 * pointer iteration. </font> See: <a href=
 * "https://www.jianshu.com/p/2f31881bf847">https://www.jianshu.com/p/2f31881bf847</a>
 * 
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2018年11月9日
 * @since
 * @param <E>
 */
public class HashScanCursor<E> implements Iterator<E> {
    protected final static String REPLICATION = "Replication";
    protected final static String ROLE_MASTER = "role:master";
    protected final static HashScanParams NONE_PARAMS = new HashScanParams();

    protected final SmartLogger log = SmartLoggerFactory.getLogger(getClass());
    protected final HashScanParams params;
    protected final byte[] hasKey;
    protected final Class<?> valueType;
    protected final HashDeserializer deserializer;
    protected final JedisClient jedisClient;

    protected volatile CursorSpec cursor;
    protected volatile CursorState state;
    // Batch scanned values cache.
    protected volatile HashScanIterable<Entry<byte[], byte[]>> iter;
    protected AtomicInteger entriesTotal = new AtomicInteger(0);

    /**
     * Crates new {@link HashScanCursor} with {@code id=0} and
     * {@link ScanParams#NONE}
     */
    public HashScanCursor(JedisClient jedisClient, byte[] hasKey, Class<?> valueType) {
        this(jedisClient, hasKey, valueType, NONE_PARAMS);
    }

    /**
     * Crates new {@link HashScanCursor} with {@code id=0}.
     * 
     * @param params
     */
    public HashScanCursor(JedisClient jedisClient, byte[] hasKey, HashScanParams params) {
        this(jedisClient, new CursorSpec(), hasKey, null, params);
    }

    /**
     * Crates new {@link HashScanCursor} with {@code id=0}.
     * 
     * @param params
     */
    public HashScanCursor(JedisClient jedisClient, byte[] hasKey, Class<?> valueType, HashScanParams params) {
        this(jedisClient, new CursorSpec(), hasKey, valueType, params);
    }

    /**
     * Crates new {@link HashScanCursor} with {@link ScanParams#NONE}
     * 
     * @param cursor
     */
    public HashScanCursor(JedisClient jedisClient, CursorSpec cursor, byte[] hasKey, Class<?> valueType) {
        this(jedisClient, cursor, hasKey, valueType, NONE_PARAMS);
    }

    /**
     * Crates new {@link HashScanCursor}
     * 
     * @param jedisClient
     *            JedisCluster
     * @param cursor
     * @param params
     *            Defaulted to {@link ScanParams#NONE} if nulled.
     */
    public HashScanCursor(JedisClient jedisClient, CursorSpec cursor, byte[] hasKey, Class<?> valueType, HashScanParams params) {
        this(jedisClient, cursor, hasKey, valueType, null, params);
    }

    /**
     * Crates new {@link HashScanCursor}
     * 
     * @param jedisClient
     *            JedisCluster
     * @param cursor
     * @param params
     *            Defaulted to {@link ScanParams#NONE} if nulled.
     */
    public HashScanCursor(JedisClient jedisClient, CursorSpec cursor, byte[] hasKey, Class<?> valueType,
            HashDeserializer deserializer, HashScanParams params) {
        notNullOf(jedisClient, "jedisClient");
        this.hasKey = notNullOf(hasKey, "hasKey");
        this.valueType = nonNull(valueType) ? valueType
                : ResolvableType.forClass(getClass()).getSuperType().getGeneric(0).resolve();
        notNull(valueType, "No scan value java type is specified. Use constructs that can set value java type.");
        this.deserializer = nonNull(deserializer) ? deserializer : new HashDeserializer() {
        };
        this.jedisClient = jedisClient;
        this.params = params != null ? params : NONE_PARAMS;
        this.state = CursorState.READY;
        this.cursor = cursor;
        this.iter = new HashScanIterable<>(cursor, emptyList());
        CursorSpec.validate(cursor);
    }

    /**
     * Initialize the {@link CursorSpec} prior to usage.
     */
    @SuppressWarnings("unchecked")
    public synchronized final <T extends HashScanCursor<E>> T open() {
        if (isOpen()) {
            log.debug("Cursor already " + state + ", no need (re)open it.");
            return (T) this;
        }

        state = CursorState.OPEN;
        nextScan();
        return (T) this;
    }

    public CursorSpec getCursor() {
        return cursor;
    }

    /**
     * Scan keys.
     * 
     * @return
     */
    public List<byte[]> toKeys() {
        return safeList(iter.getEntries()).stream().map(e -> e.getKey()).collect(toList());
    }

    /**
     * Scan keys as string.
     * 
     * @return
     */
    public List<String> toStringkeys() {
        return safeList(iter.getEntries()).stream().map(e -> new String(e.getKey(), UTF_8)).collect(toList());
    }

    /**
     * Mutual exclusion with the {@link HashScanCursor#next()} method (only one
     * can be used)
     * 
     * @throws IOException
     * 
     * @see HashScanCursor#next()
     */
    public synchronized List<E> toValues() throws IOException {
        List<E> list = new ArrayList<>(64);
        while (hasNext()) {
            list.add(next());
        }
        return list;
    }

    /**
     * Fetch the next value from the underlying {@link java.util.Iterable}.
     * mutual exclusion with {@link HashScanCursor#toValues()} method (only one
     * can be used)
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized E next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements available for cursor " + cursor + ".");
        }
        return (E) deserializer.deserialize(iter.iterator().next().getValue(), valueType);
    }

    @Override
    public synchronized boolean hasNext() {
        checkCursorState();

        // If the current 'iter' is fully traversed, you need to check whether
        // the next node has data.
        while (!iter.iterator().hasNext() && !isFinished()) {
            nextScan();
        }

        return (iter.iterator().hasNext() || (!isFinished() && !checkScanCompleted()));
    }

    protected final boolean isReady() {
        return state == CursorState.READY;
    }

    protected final boolean isOpen() {
        return state == CursorState.OPEN;
    }

    /**
     * {@link org.springframework.data.redis.core.Cursor#isClosed()}
     */
    protected boolean isFinished() {
        // state==FINISHED 的两种情况:
        // 1. 所有节点都被扫描完而结束;
        // 2. 基于游标分页限制而结束;
        return state == CursorState.FINISHED;
    }

    protected void finished(boolean resetCursorString) {
        state = CursorState.FINISHED;
        if (resetCursorString) {
            cursor.setCursorString(CursorSpec.STARTEND);
        }
        // cursor.setSelectionPos(nodePools.size() - 1);
    }

    /**
     * Next scan by cursor index.
     */
    protected void nextScan() {
        processScanResult(doScanNode());
    }

    /**
     * Performs the actual scan command using the native client implementation.
     * The given {@literal options} are never {@code null}.
     * 
     * @param jedis
     * @return
     */
    protected HashScanIterable<Entry<byte[], byte[]>> doScanNode() {
        ScanResult<Entry<byte[], byte[]>> res = jedisClient.hscan(hasKey, cursor.getCursorByteArray(), params.toScanParams());

        List<Entry<byte[], byte[]>> entries = Optional.ofNullable(res.getResult()).get();

        // Cumulative total count of scanned entries.
        int total = entriesTotal.addAndGet(entries.size());

        // Latest cursor string of current node.
        String cursorString = res.getCursor();

        // Check whether the total number is exceeded.
        int excess = total - params.getTotal();
        if (excess >= 0) {
            finished(false);
            // After finished scan, the pointer has been reset.
            // cursorString = cursor.getCursorString();

            // Remove the last elements.
            int size = entries.size();
            for (int i = size - 1; i >= size - excess; i--) {
                entries.remove(i);
            }
        }

        return new HashScanIterable<Entry<byte[], byte[]>>(cursor.setCursorString(cursorString), entries);
    }

    /**
     * After process scanned result
     * 
     * @param res
     */
    private void processScanResult(HashScanIterable<Entry<byte[], byte[]>> res) {
        this.iter = res;
        this.cursor = res.cursor;
        if (checkScanCompleted()) { // Scan end?
            finished(true);
        }
    }

    /**
     * Check that currently node finished.
     * 
     * @return
     */
    private boolean checkScanCompleted() {
        return trimToEmpty(cursor.getCursorString()).equalsIgnoreCase(CursorSpec.STARTEND);
    }

    /**
     * Check cursor is open or finished?
     */
    private void checkCursorState() {
        if (!isOpen() && !isFinished()) {
            throw new RuntimeException("Cannot access closed cursor, or did you forget to call open?");
        }
    }

    /**
     * Cursor state
     * 
     * @author James Wong <jameswong1376@gmail.com>
     * @version v1.0 2019年4月1日
     * @since
     */
    private enum CursorState {
        READY, OPEN, FINISHED;
    }

    /**
     * Scan cursor wrapper.
     * 
     * @author Wangl.sir James Wong <jameswong1376@gmail.com>>
     * @version v1.0 2019年11月4日
     * @since
     */
    public static final class CursorSpec implements Serializable {
        private static final long serialVersionUID = 4547949424670284416L;

        /** Cursor end spec. */
        private transient static final String STARTEND = "0";

        /** Scan node cursor value */
        private String cursorString = STARTEND;

        public CursorSpec() {
            super();
        }

        public CursorSpec(String cursor) {
            setCursorString(cursor);
        }

        @JsonIgnore
        public String getCursorString() {
            return cursorString;
        }

        public CursorSpec setCursorString(String cursorString) {
            this.cursorString = hasTextOf(cursorString, "cursorString");
            return this;
        }

        @Override
        public String toString() {
            return getCursorString();
        }

        @JsonIgnore
        public byte[] getCursorByteArray() {
            return cursorString.getBytes(Charsets.UTF_8);
        }

        /**
         * Check has hext records.
         * 
         * @return
         */
        public boolean getHasNext() {
            return !endsWithIgnoreCase(getCursorString(), STARTEND);
        }

        /**
         * As cursor to fully string.
         * 
         * @return
         */
        public String getCursorFullyString() {
            return getCursorString();
        }

        /**
         * Parse cursor string
         * 
         * @param cursorString
         * @return
         */
        public static CursorSpec parse(String cursorString) {
            hasText(cursorString, "Jedis scan cursorString must not be empty.");
            return new CursorSpec(cursorString);
        }

        /**
         * Validation for {@link CursorSpec}
         * 
         * @param cursor
         */
        public static void validate(CursorSpec cursor) {
            notNull(cursor, "Jedis scan cursor must not be null.");
            hasText(cursor.getCursorString(), "Jedis scan cursor value must not be empty.");
        }

    }

    /**
     * Redis cluster multi nodes scan params, {@link ScanParams}
     */
    public static final class HashScanParams implements Serializable {
        private static final long serialVersionUID = -8988706974133080380L;
        private final int total; // Total scan limit for all nodes.
        private final byte[] pattern;

        public HashScanParams() {
            this(10, "");
        }

        public HashScanParams(int total, String pattern) {
            this(total, SafeEncoder.encode(pattern));
        }

        public HashScanParams(int total, byte[] pattern) {
            this.total = total;
            this.pattern = notNullOf(pattern, "pattern");
        }

        public int getTotal() {
            return total;
        }

        public byte[] getPattern() {
            return pattern;
        }

        public ScanParams toScanParams() {
            return new ScanParams().count(getTotal()).match(getPattern());
        }
    }

    /**
     * De-serialization for {@link HashScanCursor#next()} and
     * {@link HashScanCursor#toValues()}, default implemention of
     * {@link ProtostuffUtils}
     */
    public static abstract class HashDeserializer {
        protected Object deserialize(byte[] data, Class<?> clazz) {
            return ProtostuffUtils.deserialize(data, clazz);
        }
    }

    /**
     * {@link HashScanIterable} holds the values contained in Redis
     * {@literal Multibulk reply} on exectuting {@literal SCAN} command.
     * 
     * @author Christoph Strobl
     * @since 1.4
     */
    static final class HashScanIterable<E> implements Iterable<E> {
        private final CursorSpec cursor;
        private final List<E> entries;
        private final Iterator<E> iter;

        /**
         * Scan iterable
         */
        public HashScanIterable() {
            this(new CursorSpec());
        }

        /**
         * Scan iterable
         * 
         * @param cursor
         * @param keys
         */
        public HashScanIterable(CursorSpec cursor) {
            this(cursor, Collections.emptyList());
        }

        /**
         * Scan iterable
         * 
         * @param cursor
         * @param keys
         */
        public HashScanIterable(CursorSpec cursor, List<E> entries) {
            this.cursor = cursor;
            this.entries = (CollectionUtils2.isEmpty(entries) ? emptyList() : new ArrayList<>(entries));
            this.iter = this.entries.iterator();
        }

        /**
         * The cursor id to be used for subsequent requests.
         * 
         * @return
         */
        public CursorSpec getCursor() {
            return cursor;
        }

        /**
         * Get the items returned.
         * 
         * @return
         */
        public List<E> getEntries() {
            return entries;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<E> iterator() {
            return iter;
        }

    }

}
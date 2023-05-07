/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong James Wong <jameswong1376@gmail.com>
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
package com.wl4g.infra.common.store;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.SystemUtils.JAVA_IO_TMPDIR;

import java.io.File;
import java.time.Duration;

import com.wl4g.infra.common.io.DataSize;
import com.wl4g.infra.common.jedis.JedisClientBuilder.JedisConfig;
import com.wl4g.infra.common.rocksdb.RocksDBConfig;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link StoreConfig}
 * 
 * @author James Wong
 * @version 2022-11-02
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class MapStoreConfig {

    private @Default EventRecorderProvider provider = EventRecorderProvider.ROCKSDB;

    private @Default RocksDBStoreConfig rocksdb = new RocksDBStoreConfig();

    private @Default EhCacheStoreConfig ehcache = new EhCacheStoreConfig();

    private @Default RedisStoreConfig redis = new RedisStoreConfig();

    private @Default MemoryStoreConfig memory = new MemoryStoreConfig();

    @Getter
    @Setter
    @ToString
    @SuperBuilder
    @NoArgsConstructor
    public static class RocksDBStoreConfig extends RocksDBConfig {
    }

    @Getter
    @Setter
    @ToString
    @SuperBuilder
    @NoArgsConstructor
    public static class EhCacheStoreConfig {

        /**
         * The cached data elimination algorithm.
         */
        private @Default EliminationAlgorithm eliminationAlg = EliminationAlgorithm.LRU;

        /**
         * The cache persistence data directory.
         */
        private @Default File dataDir = new File(JAVA_IO_TMPDIR, "ehcache-data-" + currentTimeMillis());

        /**
         * The number of entries not persisted to keep in memory.
         */
        private @Default long heapEntries = 1L;

        /**
         * The number of data size not persisted to keep in memory. must be less
         * than {@link #diskSize}
         */
        private @Default DataSize offHeapSize = DataSize.ofMegabytes(1);

        /**
         * The number of total data size not persisted to keep in disk. must be
         * greater than {@link #offHeapSize}
         */
        private @Default DataSize diskSize = DataSize.ofTerabytes(1);

        public static enum EliminationAlgorithm {
            LRU, LFU, FIFO;
        }

    }

    @Getter
    @Setter
    @ToString
    @SuperBuilder
    @NoArgsConstructor
    public static class RedisStoreConfig extends JedisConfig {
        private static final long serialVersionUID = 1L;

        private @Default String cachePrefix = "rengine:eventbus";

        private @Default long expireMs = 60_000L;
    }

    @Getter
    @Setter
    @ToString
    @SuperBuilder
    @NoArgsConstructor
    public static class MemoryStoreConfig {

        /**
         * If you wish the cache should not exceed this number of entries, the
         * cache will evict recently or infrequently used entries when it does,
         * WARNING: the cache may evict entries before this limit is exceeded -
         * usually when the cache size is close to the limit.
         */
        private @Default long maximumSize = 100_000L;

        /**
         * The expiration interval based on write time, all cache entry reads
         * and writes are updated.
         */
        private @Default long expireAfterAccessMs = Duration.ofSeconds(60).toMillis();

        /**
         * The expiration interval based on access time, all cache entries are
         * updated only by write operations.
         */
        private @Default long expireAfterWriteMs = Duration.ofSeconds(600).toMillis();

        /**
         * The number of concurrent cache operations, that is, the number of
         * underlying cache block/segment locks.
         */
        private @Default int concurrencyLevel = 4;
    }

    public static enum EventRecorderProvider {
        ROCKSDB,

        EHCACHE,

        REDIS,

        MEMORY;
    }

}

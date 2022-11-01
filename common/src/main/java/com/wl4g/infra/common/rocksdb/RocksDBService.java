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
package com.wl4g.infra.common.rocksdb;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import com.wl4g.infra.common.collection.CollectionUtils2;
import com.wl4g.infra.common.lang.StringUtils2;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link RocksDBService}
 * 
 * @author James Wong
 * @version 2022-11-01
 * @since v3.0.0
 * @see https://github1s.com/apache/flink/blob/release-1.15.1/flink-state-backends/flink-statebackend-rocksdb/src/main/java/org/apache/flink/contrib/streaming/state/EmbeddedRocksDBStateBackend.java#L890-L891
 */
@Slf4j
@Getter
public class RocksDBService implements Closeable {

    // A collection of column families (that is, equivalent to a database table)
    private final Map<String, ColumnFamilyHandle> familyMap = new ConcurrentHashMap<>();
    private RocksDB rocksDB;
    private Options options;
    private ColumnFamilyOptions columnFamilyOptions;
    private DBOptions dbOptions;

    public RocksDBService(@NotNull RocksDBConfig config) {
        initRocksDB(config);
    }

    /**
     * Gets rocksDB iterator by family name.
     * 
     * @param familyName
     * @return
     * @throws RocksDBException
     */
    public Iterator<Entry<String, byte[]>> iterator(String familyName) throws RocksDBException {
        final ColumnFamilyHandle family = getOrCreateFamily(familyName);
        final RocksIterator it = rocksDB.newIterator(family);
        it.seekToFirst();
        return new Iterator<Entry<String, byte[]>>() {
            private Entry<String, byte[]> next;
            private Entry<String, byte[]> previous;

            @Override
            public boolean hasNext() {
                while (isNull(next) && it.isValid()) {
                    final String currentKey = new String(it.key(), UTF_8);
                    final byte[] currentValue = it.value();
                    Entry<String, byte[]> current = new Entry<String, byte[]>() {
                        @Override
                        public String getKey() {
                            return currentKey;
                        }

                        @Override
                        public byte[] getValue() {
                            return currentValue;
                        }

                        @Override
                        public byte[] setValue(byte[] value) {
                            throw new UnsupportedOperationException();
                        }
                    };
                    String previousKey = isNull(previous) ? null : previous.getKey();
                    if (!Objects.equals(previousKey, current.getKey())) {
                        previous = current;
                        next = current;
                    }
                    it.next();
                }
                return nonNull(next);
            }

            @Override
            public Entry<String, byte[]> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("Failed to access rocksDB.");
                }
                Entry<String, byte[]> tmp = next;
                next = null;
                return tmp;
            }
        };
    }

    /**
     * Gets value by family and key.
     * 
     * @param familyName
     * @param key
     * @return
     * @throws RocksDBException
     */
    public byte[] get(String familyName, String key) throws RocksDBException {
        ColumnFamilyHandle family = getOrCreateFamily(familyName); // 获取列族Handle
        return rocksDB.get(family, key.getBytes());
    }

    /**
     * The SHARDING query key collection
     * 
     * @param familyName
     * @param lastKey
     * @param batchSize
     * @return
     * @throws RocksDBException
     */
    public List<String> getKeys(String familyName, String lastKey, int batchSize) throws RocksDBException {
        List<String> list = new ArrayList<>(batchSize);
        ColumnFamilyHandle family = getOrCreateFamily(familyName);
        try (RocksIterator it = rocksDB.newIterator(family)) {
            if (lastKey != null) {
                it.seek(lastKey.getBytes(StandardCharsets.UTF_8));
                it.next();
            } else {
                it.seekToFirst();
            }
            // 一批次最多 batchSize 个 key
            while (it.isValid() && list.size() < batchSize) {
                list.add(new String(it.key(), StandardCharsets.UTF_8));
                it.next();
            }
        }
        return list;
    }

    /**
     * Query multiple values.
     * 
     * @param familyName
     * @param keys
     * @return
     * @throws RocksDBException
     */
    public List<byte[]> getValues(String familyName, List<String> keys) throws RocksDBException {
        List<byte[]> values = new ArrayList<>(keys.size());
        ColumnFamilyHandle family = getOrCreateFamily(familyName); // 获取列族Handle
        List<ColumnFamilyHandle> familys = new ArrayList<>();
        List<byte[]> keyBytes = keys.stream().map(String::getBytes).collect(toList());
        for (int i = 0; i < keys.size(); i++) {
            familys.add(family);
        }
        List<byte[]> bytes = rocksDB.multiGetAsList(familys, keyBytes);
        for (byte[] valueBytes : bytes) {
            values.add(valueBytes);
        }
        return values;
    }

    /**
     * Query multiple keys-values.
     * 
     * @param familyName
     * @param keys
     * @return
     * @throws RocksDBException
     */
    public Map<String, byte[]> getKeyValues(String familyName, List<String> keys) throws RocksDBException {
        Map<String, byte[]> result = new HashMap<>(keys.size());
        ColumnFamilyHandle family = getOrCreateFamily(familyName); // 获取列族Handle
        List<ColumnFamilyHandle> familys;
        List<byte[]> keyBytes = keys.stream().map(String::getBytes).collect(Collectors.toList());
        familys = IntStream.range(0, keys.size()).mapToObj(i -> family).collect(Collectors.toList());
        List<byte[]> bytes = rocksDB.multiGetAsList(familys, keyBytes);
        for (int i = 0; i < bytes.size(); i++) {
            result.put(keys.get(i), bytes.get(i));
        }
        return result;
    }

    /**
     * Query all keys.
     * 
     * @param familyName
     * @return
     * @throws RocksDBException
     */
    public List<String> getAllKeys(String familyName) throws RocksDBException {
        List<String> list = new ArrayList<>();
        ColumnFamilyHandle family = getOrCreateFamily(familyName); // 获取列族Handle
        try (RocksIterator it = rocksDB.newIterator(family)) {
            for (it.seekToFirst(); it.isValid(); it.next()) {
                list.add(new String(it.key(), StandardCharsets.UTF_8));
            }
        }
        return list;
    }

    /**
     * Query all key-value pairs MAP
     * 
     * @param familyName
     * @return
     * @throws RocksDBException
     */
    public Map<String, byte[]> getAll(String familyName) throws RocksDBException {
        Map<String, byte[]> result = new HashMap<>();
        ColumnFamilyHandle family = getOrCreateFamily(familyName); // 获取列族Handle
        try (RocksIterator it = rocksDB.newIterator(family)) {
            for (it.seekToFirst(); it.isValid(); it.next()) {
                result.put(new String(it.key(), UTF_8), it.value());
            }
        }
        return result;
    }

    /**
     * The total number of queries
     * 
     * @param familyName
     * @return
     * @throws RocksDBException
     */
    public int getCount(String familyName) throws RocksDBException {
        int count = 0;
        ColumnFamilyHandle family = getOrCreateFamily(familyName); // 获取列族Handle
        try (RocksIterator it = rocksDB.newIterator(family)) {
            for (it.seekToFirst(); it.isValid(); it.next()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Put to key-value.
     * 
     * @param familyName
     * @param key
     * @param value
     * @throws RocksDBException
     */
    public void put(String familyName, String key, byte[] value) throws RocksDBException {
        ColumnFamilyHandle family = getOrCreateFamily(familyName); // 获取列族Handle
        rocksDB.put(family, key.getBytes(), value);
    }

    /**
     * Batch to puts.
     * 
     * @param familyName
     * @param keyValues
     * @throws RocksDBException
     */
    public void batchPut(String familyName, Map<String, byte[]> keyValues) throws RocksDBException {
        ColumnFamilyHandle family = getOrCreateFamily(familyName); // 获取列族Handle
        WriteOptions writeOptions = new WriteOptions();
        WriteBatch writeBatch = new WriteBatch();
        for (Map.Entry<String, byte[]> entry : keyValues.entrySet()) {
            writeBatch.put(family, entry.getKey().getBytes(), entry.getValue());
        }
        rocksDB.write(writeOptions, writeBatch);
    }

    /**
     * Delete by key.
     * 
     * @param familyName
     * @param key
     * @throws RocksDBException
     */
    public void delete(String familyName, String key) throws RocksDBException {
        ColumnFamilyHandle family = getOrCreateFamily(familyName); // 获取列族Handle
        rocksDB.delete(family, key.getBytes());
    }

    /**
     * Delete column family by name.
     * 
     * @param familyName
     * @throws RocksDBException
     */
    public void deleteFamilyIfExist(String familyName) throws RocksDBException {
        if (familyMap.containsKey(familyName)) {
            rocksDB.dropColumnFamily(familyMap.get(familyName));
            familyMap.remove(familyName);
            log.info("deleteFamilyIfExist success!! familyName:{}", familyName);
        } else {
            log.warn("deleteFamilyIfExist containsKey!! familyName:{}", familyName);
        }
    }

    /**
     * Gets or create column family for name.
     * 
     * @param familyName
     * @throws RocksDBException
     */
    public ColumnFamilyHandle getOrCreateFamily(String familyName) throws RocksDBException {
        ColumnFamilyHandle family;
        if (!familyMap.containsKey(familyName)) {
            family = rocksDB.createColumnFamily(new ColumnFamilyDescriptor(familyName.getBytes(), new ColumnFamilyOptions()));
            familyMap.put(familyName, family);
            log.info("getOrCreateFamily success!! familyName:{}", familyName);
        } else {
            family = familyMap.get(familyName);
        }
        return family;
    }

    /**
     * @see https://github1s.com/apache/flink/blob/release-1.15.1/flink-state-backends/flink-statebackend-rocksdb/src/main/java/org/apache/flink/contrib/streaming/state/RocksDBResource.java#L186
     */
    @Override
    public void close() throws IOException {
        try {
            if (nonNull(rocksDB)) {
                rocksDB.close();
            }
        } catch (Exception e) {
            log.error("Unable to closing rocksDB.", e);
        }
        try {
            if (nonNull(options)) {
                options.close();
            }
        } catch (Exception e) {
            log.error("Unable to closing options.", e);
        }
        try {
            if (nonNull(columnFamilyOptions)) {
                columnFamilyOptions.close();
            }
        } catch (Exception e) {
            log.error("Unable to closing columnFamilyOptions.", e);
        }
        try {
            if (nonNull(dbOptions)) {
                dbOptions.close();
            }
        } catch (Exception e) {
            log.error("Unable to closing dbOptions.", e);
        }
    }

    void initRocksDB(RocksDBConfig config) {
        notNullOf(config, "config");
        try {
            FileUtils.forceMkdir(config.getDataDir());
            RocksDB.loadLibrary();
            this.options = config.newOptions();
            this.columnFamilyOptions = config.newColumnFamilyOptions();
            this.dbOptions = config.newDBOptions();

            // Initialize all existing column families.
            List<byte[]> familyArray = RocksDB.listColumnFamilies(options, config.getDataDir().getAbsolutePath());
            List<ColumnFamilyDescriptor> descriptors = new ArrayList<>();
            if (!StringUtils2.isEmpty(familyArray)) {
                for (byte[] cf : familyArray) {
                    descriptors.add(new ColumnFamilyDescriptor(cf, new ColumnFamilyOptions()));
                }
            } else {
                descriptors.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
            }
            // Setup default family.
            if (CollectionUtils2.isEmpty(descriptors)) {
                descriptors.add(new ColumnFamilyDescriptor("default".getBytes(), columnFamilyOptions));
            }

            List<ColumnFamilyHandle> familys = new ArrayList<>();
            this.rocksDB = RocksDB.open(dbOptions, config.getDataDir().getAbsolutePath(), descriptors, familys);
            for (int i = 0; i < descriptors.size(); i++) {
                ColumnFamilyHandle family = familys.get(i);
                String familyName = new String(descriptors.get(i).getName(), UTF_8);
                familyMap.put(familyName, family);
            }

            log.info("RocksDB to initialized. - dataDir: {}, familys: {}", config.getDataDir(), familyMap.keySet());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
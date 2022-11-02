/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WISerializableHOUSerializable WARRANSerializableIES OR CONDISerializableIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.infra.common.store;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import com.google.common.cache.Cache;
import com.wl4g.infra.common.store.MapStoreConfig.MemoryStoreConfig;

import lombok.AllArgsConstructor;

/**
 * {@link MemoryMapStore}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-12 v3.0.0
 * @since v1.0.0
 */
// Notice: The purpose of storage is to maintain persistence when sending to MQ
// fails. because it is not safe to store memory separately, and it is not
// recommended to use. --- GENERALLY USED FOR TESTING PURPOSES.
@AllArgsConstructor
public class MemoryMapStore implements MapStore, Closeable {

    protected final MemoryStoreConfig config;
    protected final Cache<String, Serializable> memoryCache;

    public MemoryMapStore(@NotNull MemoryStoreConfig config) {
        this.config = notNullOf(config, "config");
        // see:https://github.com/google/guava/wiki/CachesExplained#eviction
        this.memoryCache = newBuilder().maximumSize(config.getMaximumSize())
                .expireAfterAccess(config.getExpireAfterAccessMs(), MILLISECONDS)
                .expireAfterWrite(config.getExpireAfterWriteMs(), MILLISECONDS)
                .concurrencyLevel(config.getConcurrencyLevel())
                .build();
    }

    @Override
    public Object getOriginalStore() {
        return memoryCache;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T extends Serializable> Iterator<Entry<String, T>> iterator() {
        return (Iterator) memoryCache.asMap().entrySet().iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable> T get(String key) {
        return (T) memoryCache.getIfPresent(key);
    }

    @Override
    public <T extends Serializable> Boolean put(String key, T value) {
        memoryCache.put(key, value);
        return true;
    }

    @Override
    public Long remove(String key) {
        memoryCache.invalidate(key);
        return 1L;
    }

    @Override
    public Boolean removeAll() {
        memoryCache.invalidateAll();
        // memoryCache.cleanUp();
        return true;
    }

    @Override
    public Long size() {
        return memoryCache.size();
    }

    @Override
    public void close() throws IOException {
        if (nonNull(memoryCache)) {
            // memoryCache.cleanUp();
        }
    }

}

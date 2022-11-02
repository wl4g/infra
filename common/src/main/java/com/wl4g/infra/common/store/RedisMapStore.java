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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.infra.common.store;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Objects.nonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.jedis.JedisClientBuilder;
import com.wl4g.infra.common.jedis.JedisService;
import com.wl4g.infra.common.jedis.cursor.HashScanCursor;
import com.wl4g.infra.common.jedis.cursor.HashScanCursor.CursorSpec;
import com.wl4g.infra.common.jedis.cursor.HashScanCursor.HashDeserializer;
import com.wl4g.infra.common.jedis.cursor.HashScanCursor.HashScanParams;
import com.wl4g.infra.common.store.MapStoreConfig.RedisStoreConfig;

import lombok.Getter;

/**
 * {@link RedisRecordCache}
 * 
 * @author James Wong &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-12 v3.0.0
 * @since v1.0.0
 */
@Getter
public class RedisMapStore implements MapStore, Closeable {
    protected final RedisStoreConfig config;
    protected final String hashKey;
    protected final JedisService jedisService;
    protected final Class<?> valueClass;

    public RedisMapStore(@NotNull RedisStoreConfig config, Class<?> valueClass, @NotBlank String hashKey) {
        this(new JedisService(new JedisClientBuilder(config).build()), valueClass, hashKey);
    }

    public RedisMapStore(@NotNull JedisService jedisService, Class<?> valueClass, @NotBlank String hashKey) {
        this.config = null;
        this.jedisService = notNullOf(jedisService, "jedisService");
        this.valueClass = notNullOf(valueClass, "valueClass");
        this.hashKey = notNullOf(hashKey, "hashKey");
    }

    @Override
    public Object getOriginalStore() {
        return jedisService;
    }

    @Override
    public <T extends Serializable> Iterator<Entry<String, T>> iterator() {
        HashScanParams params = new HashScanParams(Integer.MAX_VALUE, "*");
        return new HashScanCursor<T>(jedisService.getJedisClient(), new CursorSpec(), hashKey.getBytes(), valueClass,
                new HashDeserializer() {
                    @Override
                    protected Object deserialize(byte[] data, Class<?> clazz) {
                        return parseJSON(new String(data, UTF_8), clazz);
                    }
                }, params).open();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable> T get(String key) {
        return (T) parseJSON(jedisService.getJedisClient().hget(hashKey, key), valueClass);
    }

    @Override
    public <T extends Serializable> Boolean put(String key, T value) {
        jedisService.getJedisClient().hset(hashKey, key, toJSONString(value));
        return true;
    }

    @Override
    public Long remove(String key) {
        return jedisService.getJedisClient().hdel(hashKey, key);
    }

    @Override
    public Boolean removeAll() {
        final Long result = jedisService.del(hashKey);
        return nonNull(result) && result > 0;
    }

    @Override
    public Long size() {
        return jedisService.getJedisClient().hlen(hashKey);
    }

    @Override
    public void close() throws IOException {
        if (nonNull(jedisService)) {
            jedisService.getJedisClient().close();
        }
    }

}

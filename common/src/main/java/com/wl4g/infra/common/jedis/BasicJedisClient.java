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
package com.wl4g.infra.common.jedis;

import java.util.List;

/**
 * {@link BasicJedisClient}
 * 
 * @author James Wong
 * @version 2023-02-04
 * @since v1.0.0
 */
public interface BasicJedisClient {

    default String get(String key) {
        throw new UnsupportedOperationException();
    }

    default byte[] get(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default String hget(String key, String field) {
        throw new UnsupportedOperationException();
    }

    default String set(byte[] key, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default String setex(byte[] key, long seconds, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default String setex(String key, long seconds, String value) {
        throw new UnsupportedOperationException();
    }

    default String set(String key, String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set {@code key} to hold the string {@code value} and expiration
     * {@code timeout} if {@code key} is absent.
     *
     * @param key
     *            must not be {@literal null}.
     * @param value
     *            must not be {@literal null}.
     * @param timeout
     *            the key expiration timeout.
     * @param unit
     *            must not be {@literal null}.
     * @return {@literal null} when used in pipeline / transaction.
     * @since 2.1
     * @see <a href="https://redis.io/commands/set">Redis Documentation: SET</a>
     */
    default String setIfAbsent(String key, String value, long expireMs) {
        throw new UnsupportedOperationException();
    }

    default Long decrBy(byte[] key, long decrement) {
        throw new UnsupportedOperationException();
    }

    default Long decrBy(String key, long decrement) {
        throw new UnsupportedOperationException();
    }

    default Long decr(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long decr(String key) {
        throw new UnsupportedOperationException();
    }

    default Long del(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long del(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Long del(String key) {
        throw new UnsupportedOperationException();
    }

    default Long del(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Long exists(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Long exists(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Long expireAt(byte[] key, long unixTime) {
        throw new UnsupportedOperationException();
    }

    default Long expireAt(String key, long unixTime) {
        throw new UnsupportedOperationException();
    }

    default Long expire(byte[] key, int seconds) {
        throw new UnsupportedOperationException();
    }

    default Long expire(byte[] key, long seconds) {
        throw new UnsupportedOperationException();
    }

    default Long expire(String key, long seconds) {
        throw new UnsupportedOperationException();
    }

    default Object eval(byte[] script) {
        throw new UnsupportedOperationException();
    }

    default Object eval(byte[] script, byte[] keyCount, byte[]... params) {
        throw new UnsupportedOperationException();
    }

    default Object eval(byte[] script, byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    default Object eval(byte[] script, int keyCount, byte[]... params) {
        throw new UnsupportedOperationException();
    }

    default Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        throw new UnsupportedOperationException();
    }

    default Object eval(String script) {
        throw new UnsupportedOperationException();
    }

    default Object eval(String script, int keyCount, String... params) {
        throw new UnsupportedOperationException();
    }

    default Object eval(String script, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException();
    }

    default Object eval(String script, String sampleKey) {
        throw new UnsupportedOperationException();
    }

}

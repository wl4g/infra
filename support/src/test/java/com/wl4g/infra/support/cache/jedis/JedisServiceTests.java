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
package com.wl4g.infra.support.cache.jedis;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Arrays.asList;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.hash.Funnel;
import com.wl4g.infra.common.bloom.BloomGenerator;
import com.wl4g.infra.support.cache.jedis.JedisClientAutoConfiguration.JedisProperties;

/**
 * {@link JedisServiceTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-04-04 v3.0.0
 * @since v3.0.0
 */
public class JedisServiceTests {

    private static JedisService jedisService;

    @BeforeClass
    public static void init() throws Exception {
        JedisProperties config = new JedisProperties();
        config.setPasswd("zzx!@#$%");
        config.setNodes(asList("127.0.0.1:6379", "127.0.0.1:6380", "127.0.0.1:6381", "127.0.0.1:7379", "127.0.0.1:7380",
                "127.0.0.1:7381"));
        JedisClientFactoryBean factory = new JedisClientFactoryBean(config);
        factory.afterPropertiesSet();
        jedisService = new JedisService(factory.getObject());
    }

    @Test
    public void testBloomfilterValid() throws Exception {
        BloomGenerator<String> bfConfig = new BloomGenerator<>((Funnel<String>) (from, into) -> into.putString(from, UTF_8), 1000,
                0.01);

        // In order to ensure that the test environment is fast, a unique key is
        // generated here.
        String key = "test:bloomfilter:order:".concat(UUID.randomUUID().toString().replaceAll("-", ""));

        // Add test orders ID to bloom.
        for (int i = 0; i < 100; i++) {
            jedisService.bloomAdd(bfConfig, key, "id_" + i);
        }

        // Check if an order id exists in the bloom filter.
        boolean result1 = jedisService.bloomExist(bfConfig, key, "id_31");
        System.out.println(result1);
        assert result1;

        boolean result2 = jedisService.bloomExist(bfConfig, key, "id_123");
        System.out.println(result2);
        assert !result2;
    }

    @Test
    public void testBloomfilterFailOutOfExpectedInsertions() throws Exception {
        BloomGenerator<String> bfConfig = new BloomGenerator<>((Funnel<String>) (from, into) -> into.putString(from, UTF_8), 100, 0.01);

        // In order to ensure that the test environment is fast, a unique key is
        // generated here.
        String key = "test:bloomfilter:order:".concat(UUID.randomUUID().toString().replaceAll("-", ""));

        // Add test orders ID to bloom.
        for (int i = 0; i < 100; i++) {
            jedisService.bloomAdd(bfConfig, key, "id_" + i);
        }

        // Check if an order id exists in the bloom filter.
        boolean result1 = jedisService.bloomExist(bfConfig, key, "id_31");
        System.out.println(result1);
        assert result1;

        boolean result2 = jedisService.bloomExist(bfConfig, key, "id_123");
        System.out.println(result2);
        assert !result2;
    }

}

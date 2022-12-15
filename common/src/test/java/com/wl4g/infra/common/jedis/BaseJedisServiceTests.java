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
package com.wl4g.infra.common.jedis;

import static java.lang.System.out;
import static java.util.Arrays.asList;

import org.junit.Before;

import com.wl4g.infra.common.jedis.JedisClientBuilder.JedisConfig;

/**
 * {@link BaseJedisServiceTests}
 * 
 * @author James Wong
 * @version 2022-11-02
 * @since v3.0.0
 */
public abstract class BaseJedisServiceTests {

    protected JedisConfig jedisConfig;
    protected JedisService jedisService;

    @Before
    public void setup() throws Exception {
        jedisConfig = new JedisConfig();
        jedisConfig.setNodes(asList(new String[] { "127.0.0.1:6379", "127.0.0.1:6380", "127.0.0.1:6381", "127.0.0.1:7379",
                "127.0.0.1:7380", "127.0.0.1:7381" }));
        jedisConfig.setPassword(System.getenv().getOrDefault("REDIS_PASSWORD", "zzx!@#$%"));

        out.println("Instantiating composite operators adapter with cluster ...");
        this.jedisService = new JedisService(new JedisClientBuilder(jedisConfig).build());
    }

}

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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.wl4g.infra.common.cache.jedis.JedisClient;
import com.wl4g.infra.common.cache.jedis.JedisClientBuilder;
import com.wl4g.infra.support.cache.jedis.JedisClientAutoConfiguration.JedisProperties;

import lombok.CustomLog;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * Bean factory of {@link JedisClient}.
 */
@CustomLog
public class JedisClientFactoryBean extends JedisClientBuilder implements FactoryBean<JedisClient>, InitializingBean {

    public JedisClientFactoryBean(@NotNull JedisCluster jedisCluster, @NotNull JedisPool jedisPool) {
        super(jedisCluster, jedisPool);
    }

    public JedisClientFactoryBean(@NotNull JedisProperties config) {
        super(config);
    }

    public JedisClientFactoryBean(@Nullable JedisProperties config, @Nullable JedisCluster jedisCluster,
            @Nullable JedisPool jedisPool) {
        super(null, jedisCluster, jedisPool);
    }

    @Override
    public JedisClient getObject() throws Exception {
        return getJedisClient();
    }

    @Override
    public Class<?> getObjectType() {
        return JedisClient.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        build();
        log.info("Instantiated jedis client: {}", getJedisClient());
    }

}

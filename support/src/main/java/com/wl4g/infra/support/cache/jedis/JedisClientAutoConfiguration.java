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

import static com.wl4g.infra.support.constant.SupportInfraConstant.CONF_PREFIX_INFRA_SUPPORT_JEDIS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.infra.common.cache.jedis.JedisClient;
import com.wl4g.infra.common.cache.jedis.JedisClientBuilder;
import com.wl4g.infra.common.cache.jedis.JedisClientBuilder.JedisConfig;
import com.wl4g.infra.common.cache.jedis.JedisService;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * Jedis auto configuration, Support automatic adaptation to current
 * environment, use jedis singleton, jedis cluster, and then create
 * {@link JedisClientBuilder} and {@link JedisClient}
 * 
 * @author Wangl.sir James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2018年9月16日
 * @since
 */
@ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_JEDIS + ".enabled", matchIfMissing = true)
public class JedisClientAutoConfiguration {

    // Optional
    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_JEDIS)
    @ConditionalOnClass({ JedisCluster.class, JedisPool.class }) // or-relationship
    @ConditionalOnMissingBean({ JedisCluster.class, JedisPool.class }) // or-relationship
    public JedisProperties jedisProperties() {
        return new JedisProperties();
    }

    // Requires
    @Bean
    public JedisClientFactoryBean jedisClientFactoryBean(
            @Autowired(required = false) JedisProperties config,
            @Autowired(required = false) JedisCluster jedisCluster,
            @Autowired(required = false) JedisPool jedisPool) {
        return new JedisClientFactoryBean(config, jedisCluster, jedisPool);
    }

    // Requires
    @Bean(BEAN_NAME_REDIS)
    public JedisService jedisService(JedisClientFactoryBean factory) throws Exception {
        return new JedisService(factory.getObject());
    }

    /**
     * Jedis properties.
     * 
     * @author Wangl.sir James Wong <jameswong1376@gmail.com>>
     * @version v1.0 2018年9月16日
     * @since
     */
    public static class JedisProperties extends JedisConfig {
        private final static long serialVersionUID = 1902261160146495488L;
    }

    /**
     * Resolving spring byName injection conflict.
     */
    public static final String BEAN_NAME_REDIS = "JedisAutoConfiguration.JedisService.Bean";

}
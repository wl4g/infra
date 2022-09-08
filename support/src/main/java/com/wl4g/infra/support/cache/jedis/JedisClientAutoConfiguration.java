/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.infra.support.constant.SupportInfraConstant.CONF_PREFIX_INFRA_SUPPORT_JEDIS;
import static redis.clients.jedis.HostAndPort.parseString;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.infra.common.log.SmartLogger;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Jedis auto configuration, Support automatic adaptation to current
 * environment, use jedis singleton, jedis cluster, and then create
 * {@link JedisClientFactoryBean} and {@link JedisClient}
 * 
 * @author Wangl.sir <James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2018年9月16日
 * @since
 */
@ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_JEDIS + ".enabled", matchIfMissing = true)
public class JedisClientAutoConfiguration {
    protected final SmartLogger log = getLogger(getClass());

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
     * @author Wangl.sir <James Wong <jameswong1376@gmail.com>>
     * @version v1.0 2018年9月16日
     * @since
     */
    public static class JedisProperties implements Serializable {
        private final static long serialVersionUID = 1906168160146495488L;

        protected SmartLogger log = getLogger(getClass());

        private List<String> nodes = new ArrayList<>();
        private String passwd;
        private String clientName;
        private int connTimeout = 10_000;
        private int soTimeout = 10_000;
        private int maxAttempts = 20;
        private int database = 0;

        private JedisPoolConfig poolConfig = new JedisPoolConfig();
        private boolean safeMode = true;

        public JedisProperties() {
            // Default settings.
            /*
             * [Note:] importants, The default value is - 1, that is, there is
             * no time-out for acquiring resources, which will lead to deadlock.
             */
            this.poolConfig.setMaxWait(Duration.ofMillis(10000));
            this.poolConfig.setMinIdle(10);
            this.poolConfig.setMaxIdle(100);
            this.poolConfig.setMaxTotal(60000);
        }

        public List<String> getNodes() {
            return nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public String getPasswd() {
            return passwd;
        }

        public void setPasswd(String passwd) {
            this.passwd = passwd;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public int getConnTimeout() {
            return connTimeout;
        }

        public void setConnTimeout(int connTimeout) {
            this.connTimeout = connTimeout;
        }

        public int getSoTimeout() {
            return soTimeout;
        }

        public void setSoTimeout(int soTimeout) {
            this.soTimeout = soTimeout;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public JedisPoolConfig getPoolConfig() {
            return poolConfig;
        }

        public void setPoolConfig(JedisPoolConfig poolConfig) {
            this.poolConfig = poolConfig;
        }

        public boolean isSafeMode() {
            return safeMode;
        }

        public void setSafeMode(boolean safeMode) {
            this.safeMode = safeMode;
        }

        public final Set<HostAndPort> parseHostAndPort() throws Exception {
            try {
                Set<HostAndPort> haps = new HashSet<HostAndPort>();
                for (String node : getNodes()) {
                    boolean matched = defaultNodePattern.matcher(node).matches();
                    if (!matched) {
                        throw new IllegalArgumentException("illegal ip or port");
                    }
                    haps.add(parseString(node));
                }
                return haps;
            } catch (Exception e) {
                throw new JedisException("Resolve of redis cluster configuration failure.", e);
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
        }

        private final static Pattern defaultNodePattern = Pattern.compile("^.+[:]\\d{1,9}\\s*$");

    }

    /**
     * Resolving spring byName injection conflict.
     */
    public static final String BEAN_NAME_REDIS = "JedisAutoConfiguration.JedisService.Bean";

}
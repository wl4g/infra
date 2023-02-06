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
package com.wl4g.infra.common.jedis;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.notEmpty;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.jedis.cluster.ConfigurableJedisClusterJedisClient;
import com.wl4g.infra.common.jedis.cluster.JedisClusterJedisClient;
import com.wl4g.infra.common.jedis.single.SingleJedisClient;

import lombok.Builder.Default;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Bean factory of {@link JedisClient}.
 */
@CustomLog
public class JedisClientBuilder {

    /**
     * {@link JedisConfig}
     */
    private final JedisConfig config;

    /**
     * {@link JedisCluster}
     */
    private final JedisCluster jedisCluster;

    /**
     * {@link JedisPool}
     */
    private final JedisPool jedisPool;

    /**
     * {@link JedisClient}
     */
    private JedisClient jedisClient;

    public JedisClientBuilder(@NotNull JedisCluster jedisCluster, @NotNull JedisPool jedisPool) {
        this.config = null;
        this.jedisCluster = notNullOf(jedisCluster, "jedisCluster");
        this.jedisPool = notNullOf(jedisPool, "jedisPool");
    }

    public JedisClientBuilder(@NotNull JedisConfig config) {
        this.config = notNullOf(config, "jedisConfig");
        this.jedisCluster = null;
        this.jedisPool = null;
    }

    public JedisClientBuilder(@Nullable JedisConfig config, @Nullable JedisCluster jedisCluster, @Nullable JedisPool jedisPool) {
        this.config = config;
        this.jedisCluster = jedisCluster;
        this.jedisPool = jedisPool;
    }

    public JedisClient getJedisClient() {
        if (isNull(jedisClient)) {
            build();
        }
        return jedisClient;
    }

    /**
     * Build a {@link JedisClient} with existing jedis or via configuration.
     * 
     * @throws Exception
     */
    public JedisClient build() {
        // Initialize of existing JedisCluster or JedisPool.
        if (nonNull(jedisCluster)) {
            jedisClient = new JedisClusterJedisClient(jedisCluster);
            log.info("Instantiated JedisClient: {} via existing JedisCluster: {}", jedisClient, jedisCluster);
        } else if (nonNull(jedisPool)) {
            jedisClient = new SingleJedisClient(jedisPool, false);
            log.info("Instantiated JedisClient: {} via existing JedisPool: {}", jedisClient, jedisPool);
        }
        // Initialize of configuration.
        else {
            notNull(config,
                    "Cannot to automatically instantiate the %s. One of %s, %s and %s, expected at least 1 bean which qualifies as autowire candidate",
                    JedisClient.class.getSimpleName(), JedisPool.class.getSimpleName(), JedisCluster.class.getSimpleName(),
                    JedisConfig.class.getSimpleName());
            init(config);
        }
        return jedisClient;
    }

    /**
     * Initialize {@link JedisClient} via {@link JedisConfig}
     * 
     * @param config
     * @throws Exception
     */
    void init(JedisConfig config) {
        Set<HostAndPort> nodes = null;
        try {
            nodes = config.parseHostAndPort();
        } catch (Exception e) {
            throw new IllegalStateException(format("Unable parse to redis nodes: %s", nodes), e);
        }
        notEmpty(nodes, "Redis nodes configuration is requires, must contain at least 1 node");

        log.info("Connecting to redis nodes of : {}", config.toString());
        try {
            // Nodes size configuration is cluster?
            if (safeList(config.getNodes()).size() > 1) { // Cluster(Multi-nodes).
                jedisClient = new ConfigurableJedisClusterJedisClient(nodes, config.getConnTimeout(), config.getSoTimeout(),
                        config.getMaxAttempts(), config.getPassword(), config.getPoolConfig(), config.isSafeMode());
            } else { // Single
                HostAndPort hap = nodes.iterator().next();
                JedisPool pool = new JedisPool(config.getPoolConfig(), hap.getHost(), hap.getPort(), config.getConnTimeout(),
                        config.getSoTimeout(), config.getPassword(), 0, config.getClientName(), false, null, null, null);
                jedisClient = new SingleJedisClient(pool, config.isSafeMode());
            }
            log.info("Instantiated jedis client via configuration. {}", jedisClient);
        } catch (Exception e) {
            throw new IllegalStateException(format("Cannot connect to redis nodes : %s, reason: %s", nodes, e.getMessage()), e);
        }
    }

    @Getter
    @Setter
    @ToString
    @SuperBuilder
    public static class JedisConfig implements Serializable {
        private final static long serialVersionUID = 1906168160146495488L;
        private final static Pattern defaultNodePattern = Pattern.compile("^.+[:]\\d{1,9}\\s*$");
        private final static int DEFAULT_CONN_TIMEOUT = 10_000;
        private final static int DEFAULT_SO_TIMEOUT = 10_000;
        private final static int DEFAULT_MAX_ATTEMPTS = 20;
        private final static int DEFAULT_DATABASE = 0;
        private final static boolean DEFAULT_SAFE_MOE = true;

        private @Default List<String> nodes = new ArrayList<>();
        private @Nullable String username; // redis6.x
        private @Nullable String password;
        private @Nullable String clientName;
        private @Default int connTimeout = DEFAULT_CONN_TIMEOUT;
        private @Default int soTimeout = DEFAULT_SO_TIMEOUT;
        private @Default int maxAttempts = DEFAULT_MAX_ATTEMPTS;
        private @Default int database = DEFAULT_DATABASE;
        private @Default boolean safeMode = DEFAULT_SAFE_MOE;
        private @Default JedisPoolConfig poolConfig = new JedisPoolConfig();

        public JedisConfig() {
            this.nodes = new ArrayList<>();
            this.username = null;
            this.password = null;
            this.clientName = null;
            this.connTimeout = DEFAULT_CONN_TIMEOUT;
            this.soTimeout = DEFAULT_SO_TIMEOUT;
            this.maxAttempts = DEFAULT_MAX_ATTEMPTS;
            this.database = DEFAULT_DATABASE;
            this.safeMode = DEFAULT_SAFE_MOE;
            /*
             * Notice: importants, The default value is -1, that is, there is no
             * time-out for acquiring resources, which will lead to deadlock.
             */
            this.poolConfig = new JedisPoolConfig();
            this.poolConfig.setMaxWait(Duration.ofSeconds(10));
            this.poolConfig.setMaxTotal(100);
            this.poolConfig.setMaxIdle(10);
            this.poolConfig.setMinIdle(1);
        }

        public final Set<HostAndPort> parseHostAndPort() throws Exception {
            try {
                final Set<HostAndPort> haps = new HashSet<HostAndPort>();
                for (String node : getNodes()) {
                    boolean matched = defaultNodePattern.matcher(node).matches();
                    if (!matched) {
                        throw new IllegalArgumentException("illegal ip or port");
                    }
                    haps.add(HostAndPort.from(node));
                }
                return haps;
            } catch (Exception e) {
                throw new JedisException("Resolve of redis cluster configuration failure.", e);
            }
        }

    }

}

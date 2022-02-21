/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.component.support.cache.jedis.cluster;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.exception.framework.ParameterCanonicalException;
import com.wl4g.component.support.cache.jedis.JedisClient;
import com.wl4g.component.support.cache.jedis.cluster.ConfigurableJedisClusterCommand.ConfigurableJedisClusterConntionHandler;
import com.wl4g.component.support.cache.jedis.util.RedisSpecUtil;

import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.StreamEntry;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.StreamPendingEntry;
import redis.clients.jedis.StreamPendingSummary;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.args.FlushMode;
import redis.clients.jedis.args.ListDirection;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.params.GeoAddParams;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.GeoRadiusStoreParam;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.LPosParams;
import redis.clients.jedis.params.RestoreParams;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.params.XAddParams;
import redis.clients.jedis.params.XClaimParams;
import redis.clients.jedis.params.XPendingParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.params.XTrimParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZIncrByParams;
import redis.clients.jedis.resps.KeyedListElement;
import redis.clients.jedis.resps.KeyedZSetElement;
import redis.clients.jedis.util.JedisClusterHashTagUtil;
import redis.clients.jedis.util.KeyMergeUtil;
import redis.clients.jedis.util.SafeEncoder;

/**
 * {@link ConfigurableJedisClusterJedisClient}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020年3月28日 v1.0.0
 * @see
 */
@SuppressWarnings({ "deprecation", "unchecked" })
public class ConfigurableJedisClusterJedisClient extends JedisCluster implements JedisClient {
    protected final SmartLogger log = getLogger(getClass());

    /** Safety mode, validating storage key. */
    protected final boolean safeMode;

    public ConfigurableJedisClusterJedisClient(HostAndPort node, int connectionTimeout, int soTimeout, int maxAttempts,
            final GenericObjectPoolConfig<Jedis> poolConfig, boolean safeMode) {
        this(singleton(node), connectionTimeout, soTimeout, maxAttempts, null, poolConfig, safeMode);
    }

    public ConfigurableJedisClusterJedisClient(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout,
            int maxAttempts, final GenericObjectPoolConfig<Jedis> poolConfig, boolean safeMode) {
        this(jedisClusterNode, connectionTimeout, soTimeout, maxAttempts, null, poolConfig, safeMode);
    }

    public ConfigurableJedisClusterJedisClient(Set<HostAndPort> jedisClusterNode, int connectionTimeout, int soTimeout,
            int maxAttempts, String password, final GenericObjectPoolConfig<Jedis> poolConfig, boolean safeMode) {
        super(emptySet(), connectionTimeout, soTimeout, maxAttempts, null, null);
        // Overly jedisCluster connection handler
        this.connectionHandler = new ConfigurableJedisClusterConntionHandler(jedisClusterNode, poolConfig, connectionTimeout,
                soTimeout, (isBlank(password) ? null : password));
        this.safeMode = safeMode;
    }

    // ----------------------- JedisCluster ---------------------------

    @Override
    public Boolean copy(String srcKey, String dstKey, boolean replace) {
        checkArguments(srcKey);
        checkArguments(dstKey);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.copy(srcKey, dstKey, replace);
            }
        }.run(2, srcKey, dstKey);
    }

    @Override
    public String set(final String key, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.set(key, value);
            }
        }.run(key);
    }

    @Override
    public String set(final String key, final String value, final SetParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.set(key, value, params);
            }
        }.run(key);
    }

    @Override
    public String get(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.get(key);
            }
        }.run(key);
    }

    @Override
    public String getDel(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.getDel(key);
            }
        }.run(key);
    }

    @Override
    public String getEx(String key, GetExParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.getEx(key, params);
            }
        }.run(key);
    }

    @Override
    public Boolean exists(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.exists(key);
            }
        }.run(key);
    }

    @Override
    public Long exists(final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.exists(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long persist(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.persist(key);
            }
        }.run(key);
    }

    @Override
    public String type(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.type(key);
            }
        }.run(key);
    }

    @Override
    public byte[] dump(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.dump(key);
            }
        }.run(key);
    }

    @Override
    public String restore(final String key, final long ttl, final byte[] serializedValue) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.restore(key, ttl, serializedValue);
            }
        }.run(key);
    }

    @Override
    public String restore(final String key, final long ttl, final byte[] serializedValue, final RestoreParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.restore(key, ttl, serializedValue, params);
            }
        }.run(key);
    }

    @Override
    public Long expire(final String key, final long seconds) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.expire(key, seconds);
            }
        }.run(key);
    }

    @Override
    public Long pexpire(final String key, final long milliseconds) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pexpire(key, milliseconds);
            }
        }.run(key);
    }

    @Override
    public Long expireAt(final String key, final long unixTime) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.expireAt(key, unixTime);
            }
        }.run(key);
    }

    @Override
    public Long pexpireAt(final String key, final long millisecondsTimestamp) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pexpireAt(key, millisecondsTimestamp);
            }
        }.run(key);
    }

    @Override
    public Long ttl(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.ttl(key);
            }
        }.run(key);
    }

    @Override
    public Long pttl(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pttl(key);
            }
        }.run(key);
    }

    @Override
    public Long touch(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.touch(key);
            }
        }.run(key);
    }

    @Override
    public Long touch(final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.touch(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Boolean setbit(final String key, final long offset, final boolean value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.run(key);
    }

    @Override
    public Boolean setbit(final String key, final long offset, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.run(key);
    }

    @Override
    public Boolean getbit(final String key, final long offset) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.getbit(key, offset);
            }
        }.run(key);
    }

    @Override
    public Long setrange(final String key, final long offset, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.setrange(key, offset, value);
            }
        }.run(key);
    }

    @Override
    public String getrange(final String key, final long startOffset, final long endOffset) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.getrange(key, startOffset, endOffset);
            }
        }.run(key);
    }

    @Override
    public String getSet(final String key, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.getSet(key, value);
            }
        }.run(key);
    }

    @Override
    public Long setnx(final String key, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.setnx(key, value);
            }
        }.run(key);
    }

    @Override
    public String setex(final String key, final long seconds, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.setex(key, seconds, value);
            }
        }.run(key);
    }

    @Override
    public String psetex(final String key, final long milliseconds, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.psetex(key, milliseconds, value);
            }
        }.run(key);
    }

    @Override
    public Long decrBy(final String key, final long decrement) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.decrBy(key, decrement);
            }
        }.run(key);
    }

    @Override
    public Long decr(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.decr(key);
            }
        }.run(key);
    }

    @Override
    public Long incrBy(final String key, final long increment) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.incrBy(key, increment);
            }
        }.run(key);
    }

    @Override
    public Double incrByFloat(final String key, final double increment) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.incrByFloat(key, increment);
            }
        }.run(key);
    }

    @Override
    public Long incr(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.incr(key);
            }
        }.run(key);
    }

    @Override
    public Long append(final String key, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.append(key, value);
            }
        }.run(key);
    }

    @Override
    public String substr(final String key, final int start, final int end) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.substr(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Long hset(final String key, final String field, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hset(key, field, value);
            }
        }.run(key);
    }

    @Override
    public Long hset(final String key, final Map<String, String> hash) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hset(key, hash);
            }
        }.run(key);
    }

    @Override
    public String hget(final String key, final String field) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.hget(key, field);
            }
        }.run(key);
    }

    @Override
    public Long hsetnx(final String key, final String field, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hsetnx(key, field, value);
            }
        }.run(key);
    }

    @Override
    public String hmset(final String key, final Map<String, String> hash) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.hmset(key, hash);
            }
        }.run(key);
    }

    @Override
    public List<String> hmget(final String key, final String... fields) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.hmget(key, fields);
            }
        }.run(key);
    }

    @Override
    public Long hincrBy(final String key, final String field, final long value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hincrBy(key, field, value);
            }
        }.run(key);
    }

    @Override
    public Double hincrByFloat(final String key, final String field, final double value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.hincrByFloat(key, field, value);
            }
        }.run(key);
    }

    @Override
    public Boolean hexists(final String key, final String field) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.hexists(key, field);
            }
        }.run(key);
    }

    @Override
    public Long hdel(final String key, final String... field) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hdel(key, field);
            }
        }.run(key);
    }

    @Override
    public Long hlen(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hlen(key);
            }
        }.run(key);
    }

    @Override
    public Set<String> hkeys(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.hkeys(key);
            }
        }.run(key);
    }

    @Override
    public List<String> hvals(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.hvals(key);
            }
        }.run(key);
    }

    @Override
    public Map<String, String> hgetAll(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Map<String, String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Map<String, String> doExecute(Jedis connection) {
                return connection.hgetAll(key);
            }
        }.run(key);
    }

    @Override
    public String hrandfield(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.hrandfield(key);
            }
        }.run(key);
    }

    @Override
    public List<String> hrandfield(final String key, final long count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.hrandfield(key, count);
            }
        }.run(key);
    }

    @Override
    public Map<String, String> hrandfieldWithValues(final String key, final long count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Map<String, String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Map<String, String> doExecute(Jedis connection) {
                return connection.hrandfieldWithValues(key, count);
            }
        }.run(key);
    }

    @Override
    public Long rpush(final String key, final String... string) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.rpush(key, string);
            }
        }.run(key);
    }

    @Override
    public Long lpush(final String key, final String... string) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lpush(key, string);
            }
        }.run(key);
    }

    @Override
    public Long llen(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.llen(key);
            }
        }.run(key);
    }

    @Override
    public List<String> lrange(final String key, final long start, final long stop) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.lrange(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public String ltrim(final String key, final long start, final long stop) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.ltrim(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public String lindex(final String key, final long index) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.lindex(key, index);
            }
        }.run(key);
    }

    @Override
    public String lset(final String key, final long index, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.lset(key, index, value);
            }
        }.run(key);
    }

    @Override
    public Long lrem(final String key, final long count, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lrem(key, count, value);
            }
        }.run(key);
    }

    @Override
    public String lpop(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.lpop(key);
            }
        }.run(key);
    }

    @Override
    public List<String> lpop(final String key, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.lpop(key, count);
            }
        }.run(key);
    }

    @Override
    public Long lpos(final String key, final String element) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lpos(key, element);
            }
        }.run(key);
    }

    @Override
    public Long lpos(final String key, final String element, final LPosParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lpos(key, element, params);
            }
        }.run(key);
    }

    @Override
    public List<Long> lpos(final String key, final String element, final LPosParams params, final long count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<Long>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Long> doExecute(Jedis connection) {
                return connection.lpos(key, element, params, count);
            }
        }.run(key);
    }

    @Override
    public String rpop(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.rpop(key);
            }
        }.run(key);
    }

    @Override
    public List<String> rpop(final String key, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.rpop(key, count);
            }
        }.run(key);
    }

    @Override
    public Long sadd(final String key, final String... member) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sadd(key, member);
            }
        }.run(key);
    }

    @Override
    public Set<String> smembers(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.smembers(key);
            }
        }.run(key);
    }

    @Override
    public Long srem(final String key, final String... member) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.srem(key, member);
            }
        }.run(key);
    }

    @Override
    public String spop(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.spop(key);
            }
        }.run(key);
    }

    @Override
    public Set<String> spop(final String key, final long count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.spop(key, count);
            }
        }.run(key);
    }

    @Override
    public Long scard(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.scard(key);
            }
        }.run(key);
    }

    @Override
    public Boolean sismember(final String key, final String member) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.sismember(key, member);
            }
        }.run(key);
    }

    @Override
    public List<Boolean> smismember(final String key, final String... members) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<Boolean>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Boolean> doExecute(Jedis connection) {
                return connection.smismember(key, members);
            }
        }.run(key);
    }

    @Override
    public String srandmember(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.srandmember(key);
            }
        }.run(key);
    }

    @Override
    public List<String> srandmember(final String key, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.srandmember(key, count);
            }
        }.run(key);
    }

    @Override
    public Long strlen(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.strlen(key);
            }
        }.run(key);
    }

    @Override
    public Long zadd(final String key, final double score, final String member) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zadd(key, score, member);
            }
        }.run(key);
    }

    @Override
    public Long zadd(final String key, final double score, final String member, final ZAddParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zadd(key, score, member, params);
            }
        }.run(key);
    }

    @Override
    public Long zadd(final String key, final Map<String, Double> scoreMembers) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zadd(key, scoreMembers);
            }
        }.run(key);
    }

    @Override
    public Long zadd(final String key, final Map<String, Double> scoreMembers, final ZAddParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zadd(key, scoreMembers, params);
            }
        }.run(key);
    }

    @Override
    public Double zaddIncr(String key, double score, String member, ZAddParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.zaddIncr(key, score, member, params);
            }
        }.run(key);
    }

    @Override
    public Set<String> zdiff(String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zdiff(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Set<Tuple> zdiffWithScores(String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zdiffWithScores(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long zdiffStore(final String dstkey, final String... keys) {
        checkArguments(dstkey);
        String[] wholeKeys = KeyMergeUtil.merge(dstkey, keys);
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zdiffStore(dstkey, keys);
            }
        }.run(wholeKeys.length, wholeKeys);
    }

    @Override
    public Set<String> zrange(final String key, final long start, final long stop) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrange(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public Long zrem(final String key, final String... members) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zrem(key, members);
            }
        }.run(key);
    }

    @Override
    public Double zincrby(final String key, final double increment, final String member) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.zincrby(key, increment, member);
            }
        }.run(key);
    }

    @Override
    public Double zincrby(final String key, final double increment, final String member, final ZIncrByParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.zincrby(key, increment, member, params);
            }
        }.run(key);
    }

    @Override
    public Long zrank(final String key, final String member) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zrank(key, member);
            }
        }.run(key);
    }

    @Override
    public Long zrevrank(final String key, final String member) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zrevrank(key, member);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrange(final String key, final long start, final long stop) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrevrange(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeWithScores(final String key, final long start, final long stop) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeWithScores(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(final String key, final long start, final long stop) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeWithScores(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public String zrandmember(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.zrandmember(key);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrandmember(final String key, final long count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrandmember(key, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrandmemberWithScores(final String key, final long count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrandmemberWithScores(key, count);
            }
        }.run(key);
    }

    @Override
    public Long zcard(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zcard(key);
            }
        }.run(key);
    }

    @Override
    public Double zscore(final String key, final String member) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.zscore(key, member);
            }
        }.run(key);
    }

    @Override
    public List<Double> zmscore(final String key, final String... members) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<Double>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Double> doExecute(Jedis connection) {
                return connection.zmscore(key, members);
            }
        }.run(key);
    }

    @Override
    public Tuple zpopmax(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Tuple>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Tuple doExecute(Jedis connection) {
                return connection.zpopmax(key);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zpopmax(final String key, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zpopmax(key, count);
            }
        }.run(key);
    }

    @Override
    public Tuple zpopmin(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Tuple>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Tuple doExecute(Jedis connection) {
                return connection.zpopmin(key);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zpopmin(final String key, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zpopmin(key, count);
            }
        }.run(key);
    }

    @Override
    public List<String> sort(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.sort(key);
            }
        }.run(key);
    }

    @Override
    public List<String> sort(final String key, final SortingParams sortingParameters) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.sort(key, sortingParameters);
            }
        }.run(key);
    }

    @Override
    public Long zcount(final String key, final double min, final double max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long zcount(final String key, final String min, final String max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max, final int offset, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final String max, final String min) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByScore(final String key, final String min, final String max, final int offset, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final double max, final double min, final int offset, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max, final int offset,
            final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByScore(final String key, final String max, final String min, final int offset, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max, final String min) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max, final int offset,
            final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min, final int offset,
            final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max, final String min, final int offset,
            final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByRank(final String key, final long start, final long stop) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zremrangeByRank(key, start, stop);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByScore(final String key, final double min, final double max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zremrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByScore(final String key, final String min, final String max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zremrangeByScore(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long zlexcount(final String key, final String min, final String max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zlexcount(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrangeByLex(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrangeByLex(final String key, final String min, final String max, final int offset, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrangeByLex(key, min, max, offset, count);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrevrangeByLex(key, max, min);
            }
        }.run(key);
    }

    @Override
    public Set<String> zrevrangeByLex(final String key, final String max, final String min, final int offset, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zrevrangeByLex(key, max, min, offset, count);
            }
        }.run(key);
    }

    @Override
    public Long zremrangeByLex(final String key, final String min, final String max) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zremrangeByLex(key, min, max);
            }
        }.run(key);
    }

    @Override
    public Long linsert(final String key, final ListPosition where, final String pivot, final String value) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.linsert(key, where, pivot, value);
            }
        }.run(key);
    }

    @Override
    public Long lpushx(final String key, final String... string) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lpushx(key, string);
            }
        }.run(key);
    }

    @Override
    public Long rpushx(final String key, final String... string) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.rpushx(key, string);
            }
        }.run(key);
    }

    @Override
    public Long del(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.del(key);
            }
        }.run(key);
    }

    @Override
    public Long unlink(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.unlink(key);
            }
        }.run(key);
    }

    @Override
    public Long unlink(final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.unlink(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public String echo(final String string) {
        // note that it'll be run from arbitrary node
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.echo(string);
            }
        }.run(string);
    }

    @Override
    public Long bitcount(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.bitcount(key);
            }
        }.run(key);
    }

    @Override
    public Long bitcount(final String key, final long start, final long end) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.bitcount(key, start, end);
            }
        }.run(key);
    }

    @Override
    public Set<String> keys(final String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException(
                    this.getClass().getSimpleName() + " only supports KEYS commands with non-empty patterns");
        }
        if (!JedisClusterHashTagUtil.isClusterCompliantMatchPattern(pattern)) {
            throw new IllegalArgumentException(this.getClass().getSimpleName()
                    + " only supports KEYS commands with patterns containing hash-tags" + " ( curly-brackets enclosed strings )");
        }
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.keys(pattern);
            }
        }.run(pattern);
    }

    @Override
    public ScanResult<String> scan(final String cursor, final ScanParams params) {
        String matchPattern = null;
        if (params == null || (matchPattern = doScanMatch(params)) == null || matchPattern.isEmpty()) {
            throw new IllegalArgumentException(
                    JedisCluster.class.getSimpleName() + " only supports SCAN commands with non-empty MATCH patterns");
        }
        if (!JedisClusterHashTagUtil.isClusterCompliantMatchPattern(matchPattern)) {
            throw new IllegalArgumentException(
                    JedisCluster.class.getSimpleName() + " only supports SCAN commands with MATCH patterns containing hash-tags"
                            + " ( curly-brackets enclosed strings )");
        }
        return new ConfigurableJedisClusterCommand<ScanResult<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public ScanResult<String> doExecute(Jedis connection) {
                return connection.scan(cursor, params);
            }
        }.run(matchPattern);
    }

    @Override
    public ScanResult<Entry<String, String>> hscan(final String key, final String cursor) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<ScanResult<Entry<String, String>>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public ScanResult<Entry<String, String>> doExecute(Jedis connection) {
                return connection.hscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public ScanResult<String> sscan(final String key, final String cursor) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<ScanResult<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public ScanResult<String> doExecute(Jedis connection) {
                return connection.sscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public ScanResult<Tuple> zscan(final String key, final String cursor) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<ScanResult<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public ScanResult<Tuple> doExecute(Jedis connection) {
                return connection.zscan(key, cursor);
            }
        }.run(key);
    }

    @Override
    public Long pfadd(final String key, final String... elements) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pfadd(key, elements);
            }
        }.run(key);
    }

    @Override
    public long pfcount(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pfcount(key);
            }
        }.run(key);
    }

    @Override
    public Long del(final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.del(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
        checkArguments(srcKey);
        checkArguments(dstKey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.lmove(srcKey, dstKey, from, to);
            }
        }.run(2, srcKey, dstKey);
    }

    @Override
    public String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
        checkArguments(srcKey);
        checkArguments(dstKey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.blmove(srcKey, dstKey, from, to, timeout);
            }
        }.run(2, srcKey, dstKey);
    }

    @Override
    public List<String> blpop(final int timeout, final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.blpop(timeout, keys);
            }
        }.run(keys.length, keys);

    }

    @Override
    public KeyedListElement blpop(final double timeout, final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<KeyedListElement>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public KeyedListElement doExecute(Jedis connection) {
                return connection.blpop(timeout, keys);
            }
        }.run(keys.length, keys);

    }

    @Override
    public List<String> brpop(final int timeout, final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.brpop(timeout, keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public KeyedListElement brpop(final double timeout, final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<KeyedListElement>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public KeyedListElement doExecute(Jedis connection) {
                return connection.brpop(timeout, keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public KeyedZSetElement bzpopmax(double timeout, String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<KeyedZSetElement>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public KeyedZSetElement doExecute(Jedis connection) {
                return connection.bzpopmax(timeout, keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public KeyedZSetElement bzpopmin(double timeout, String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<KeyedZSetElement>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public KeyedZSetElement doExecute(Jedis connection) {
                return connection.bzpopmin(timeout, keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public List<String> blpop(final int timeout, final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.blpop(timeout, key);
            }
        }.run(key);
    }

    @Override
    public KeyedListElement blpop(double timeout, String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<KeyedListElement>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public KeyedListElement doExecute(Jedis connection) {
                return connection.blpop(timeout, key);
            }
        }.run(key);
    }

    @Override
    public List<String> brpop(final int timeout, final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.brpop(timeout, key);
            }
        }.run(key);
    }

    @Override
    public KeyedListElement brpop(double timeout, String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<KeyedListElement>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public KeyedListElement doExecute(Jedis connection) {
                return connection.brpop(timeout, key);
            }
        }.run(key);
    }

    @Override
    public List<String> mget(final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.mget(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public String mset(final String... keysvalues) {
        String[] keys = new String[keysvalues.length / 2];
        for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
            keys[keyIdx] = keysvalues[keyIdx * 2];
        }
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.mset(keysvalues);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long msetnx(final String... keysvalues) {
        String[] keys = new String[keysvalues.length / 2];

        for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
            keys[keyIdx] = keysvalues[keyIdx * 2];
        }

        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.msetnx(keysvalues);
            }
        }.run(keys.length, keys);
    }

    @Override
    public String rename(final String oldkey, final String newkey) {
        checkArguments(newkey);
        checkArguments(oldkey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.rename(oldkey, newkey);
            }
        }.run(2, oldkey, newkey);
    }

    @Override
    public Long renamenx(final String oldkey, final String newkey) {
        checkArguments(newkey);
        checkArguments(oldkey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.renamenx(oldkey, newkey);
            }
        }.run(2, oldkey, newkey);
    }

    @Override
    public String rpoplpush(final String srckey, final String dstkey) {
        checkArguments(srckey);
        checkArguments(dstkey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.rpoplpush(srckey, dstkey);
            }
        }.run(2, srckey, dstkey);
    }

    @Override
    public Set<String> sdiff(final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.sdiff(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long sdiffstore(final String dstkey, final String... keys) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, keys);

        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sdiffstore(dstkey, keys);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public Set<String> sinter(final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.sinter(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long sinterstore(final String dstkey, final String... keys) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, keys);
        checkArguments(dstkey);
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sinterstore(dstkey, keys);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public Long smove(final String srckey, final String dstkey, final String member) {
        checkArguments(srckey);
        checkArguments(dstkey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.smove(srckey, dstkey, member);
            }
        }.run(2, srckey, dstkey);
    }

    @Override
    public Long sort(final String key, final SortingParams sortingParameters, final String dstkey) {
        checkArguments(key);
        checkArguments(dstkey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sort(key, sortingParameters, dstkey);
            }
        }.run(2, key, dstkey);
    }

    @Override
    public Long sort(final String key, final String dstkey) {
        checkArguments(key);
        checkArguments(dstkey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sort(key, dstkey);
            }
        }.run(2, key, dstkey);
    }

    @Override
    public Set<String> sunion(final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.sunion(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long sunionstore(final String dstkey, final String... keys) {
        String[] wholeKeys = KeyMergeUtil.merge(dstkey, keys);

        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sunionstore(dstkey, keys);
            }
        }.run(wholeKeys.length, wholeKeys);
    }

    @Override
    public Set<String> zinter(final ZParams params, final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zinter(params, keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Set<Tuple> zinterWithScores(final ZParams params, final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zinterWithScores(params, keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long zinterstore(final String dstkey, final String... sets) {
        checkArguments(dstkey);
        String[] wholeKeys = KeyMergeUtil.merge(dstkey, sets);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zinterstore(dstkey, sets);
            }
        }.run(wholeKeys.length, wholeKeys);
    }

    @Override
    public Long zinterstore(final String dstkey, final ZParams params, final String... sets) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, sets);

        checkArguments(dstkey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zinterstore(dstkey, params, sets);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public Set<String> zunion(final ZParams params, final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<String> doExecute(Jedis connection) {
                return connection.zunion(params, keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Set<Tuple> zunionWithScores(final ZParams params, final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zunionWithScores(params, keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Long zunionstore(final String dstkey, final String... sets) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, sets);
        checkArguments(dstkey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zunionstore(dstkey, sets);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public Long zunionstore(final String dstkey, final ZParams params, final String... sets) {
        String[] mergedKeys = KeyMergeUtil.merge(dstkey, sets);
        checkArguments(dstkey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zunionstore(dstkey, params, sets);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public String brpoplpush(final String source, final String destination, final int timeout) {
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.brpoplpush(source, destination, timeout);
            }
        }.run(2, source, destination);
    }

    @Override
    public Long publish(final String channel, final String message) {
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.publish(channel, message);
            }
        }.runWithAnyNode();
    }

    @Override
    public void subscribe(final JedisPubSub jedisPubSub, final String... channels) {
        new ConfigurableJedisClusterCommand<Integer>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Integer doExecute(Jedis connection) {
                connection.subscribe(jedisPubSub, channels);
                return 0;
            }
        }.runWithAnyNode();
    }

    @Override
    public void psubscribe(final JedisPubSub jedisPubSub, final String... patterns) {
        new ConfigurableJedisClusterCommand<Integer>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Integer doExecute(Jedis connection) {
                connection.psubscribe(jedisPubSub, patterns);
                return 0;
            }
        }.runWithAnyNode();
    }

    @Override
    public Long bitop(final BitOP op, final String destKey, final String... srcKeys) {
        checkArguments(srcKeys);
        checkArguments(destKey);
        String[] mergedKeys = KeyMergeUtil.merge(destKey, srcKeys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.bitop(op, destKey, srcKeys);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public String pfmerge(final String destkey, final String... sourcekeys) {
        checkArguments(destkey);
        checkArguments(sourcekeys);
        String[] mergedKeys = KeyMergeUtil.merge(destkey, sourcekeys);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.pfmerge(destkey, sourcekeys);
            }
        }.run(mergedKeys.length, mergedKeys);
    }

    @Override
    public long pfcount(final String... keys) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pfcount(keys);
            }
        }.run(keys.length, keys);
    }

    @Override
    public Object eval(final String script, final int keyCount, final String... params) {
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.eval(script, keyCount, params);
            }
        }.run(keyCount, params);
    }

    @Override
    public Object eval(final String script, final String sampleKey) {
        checkArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.eval(script);
            }
        }.run(sampleKey);
    }

    @Override
    public Object eval(final String script, final List<String> keys, final List<String> args) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.eval(script, keys, args);
            }
        }.run(keys.size(), keys.toArray(new String[keys.size()]));
    }

    @Override
    public Object evalsha(final String sha1, final int keyCount, final String... params) {
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.evalsha(sha1, keyCount, params);
            }
        }.run(keyCount, params);
    }

    @Override
    public Object evalsha(final String sha1, final List<String> keys, final List<String> args) {
        checkArguments(keys);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.evalsha(sha1, keys, args);
            }
        }.run(keys.size(), keys.toArray(new String[keys.size()]));
    }

    @Override
    public Object evalsha(final String sha1, final String sampleKey) {
        checkArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.evalsha(sha1);
            }
        }.run(sampleKey);
    }

    @Override
    public Boolean scriptExists(final String sha1, final String sampleKey) {
        checkArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.scriptExists(sha1);
            }
        }.run(sampleKey);
    }

    @Override
    public List<Boolean> scriptExists(final String sampleKey, final String... sha1) {
        checkArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<List<Boolean>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Boolean> doExecute(Jedis connection) {
                return connection.scriptExists(sha1);
            }
        }.run(sampleKey);
    }

    @Override
    public String scriptLoad(final String script, final String sampleKey) {
        checkArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.scriptLoad(script);
            }
        }.run(sampleKey);
    }

    @Override
    public String scriptFlush(final String sampleKey) {
        checkArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.scriptFlush();
            }
        }.run(sampleKey);
    }

    @Override
    public String scriptKill(final String sampleKey) {
        checkArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.scriptKill();
            }
        }.run(sampleKey);
    }

    @Override
    public Long geoadd(final String key, final double longitude, final double latitude, final String member) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.geoadd(key, longitude, latitude, member);
            }
        }.run(key);
    }

    @Override
    public Long geoadd(final String key, final Map<String, GeoCoordinate> memberCoordinateMap) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.geoadd(key, memberCoordinateMap);
            }
        }.run(key);
    }

    @Override
    public Long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.geoadd(key, params, memberCoordinateMap);
            }
        }.run(key);
    }

    @Override
    public Double geodist(final String key, final String member1, final String member2) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.geodist(key, member1, member2);
            }
        }.run(key);
    }

    @Override
    public Double geodist(final String key, final String member1, final String member2, final GeoUnit unit) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.geodist(key, member1, member2, unit);
            }
        }.run(key);
    }

    @Override
    public List<String> geohash(final String key, final String... members) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<String>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<String> doExecute(Jedis connection) {
                return connection.geohash(key, members);
            }
        }.run(key);
    }

    @Override
    public List<GeoCoordinate> geopos(final String key, final String... members) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoCoordinate>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<GeoCoordinate> doExecute(Jedis connection) {
                return connection.geopos(key, members);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadius(final String key, final double longitude, final double latitude, final double radius,
            final GeoUnit unit) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadius(key, longitude, latitude, radius, unit);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final String key, final double longitude, final double latitude,
            final double radius, final GeoUnit unit) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusReadonly(key, longitude, latitude, radius, unit);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadius(final String key, final double longitude, final double latitude, final double radius,
            final GeoUnit unit, final GeoRadiusParam param) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadius(key, longitude, latitude, radius, unit, param);
            }
        }.run(key);
    }

    @Override
    public Long georadiusStore(final String key, final double longitude, final double latitude, final double radius,
            final GeoUnit unit, final GeoRadiusParam param, final GeoRadiusStoreParam storeParam) {
        String[] keys = storeParam.getStringKeys(key);
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam);
            }
        }.run(keys.length, keys);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final String key, final double longitude, final double latitude,
            final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusReadonly(key, longitude, latitude, radius, unit, param);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final String key, final String member, final double radius,
            final GeoUnit unit) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusByMember(key, member, radius, unit);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final String key, final String member, final double radius,
            final GeoUnit unit) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusByMemberReadonly(key, member, radius, unit);
            }
        }.run(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final String key, final String member, final double radius,
            final GeoUnit unit, final GeoRadiusParam param) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusByMember(key, member, radius, unit, param);
            }
        }.run(key);
    }

    @Override
    public Long georadiusByMemberStore(final String key, final String member, final double radius, final GeoUnit unit,
            final GeoRadiusParam param, final GeoRadiusStoreParam storeParam) {
        String[] keys = storeParam.getStringKeys(key);
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.georadiusByMemberStore(key, member, radius, unit, param, storeParam);
            }
        }.run(keys.length, keys);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final String key, final String member, final double radius,
            final GeoUnit unit, final GeoRadiusParam param) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusByMemberReadonly(key, member, radius, unit, param);
            }
        }.run(key);
    }

    @Override
    public List<Long> bitfield(final String key, final String... arguments) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<Long>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Long> doExecute(Jedis connection) {
                return connection.bitfield(key, arguments);
            }
        }.run(key);
    }

    @Override
    public List<Long> bitfieldReadonly(final String key, final String... arguments) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<Long>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Long> doExecute(Jedis connection) {
                return connection.bitfieldReadonly(key, arguments);
            }
        }.run(key);
    }

    @Override
    public Long hstrlen(final String key, final String field) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hstrlen(key, field);
            }
        }.run(key);
    }

    @Override
    public Long memoryUsage(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.memoryUsage(key);
            }
        }.run(key);
    }

    @Override
    public Long memoryUsage(final String key, final int samples) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.memoryUsage(key, samples);
            }
        }.run(key);
    }

    @Override
    public StreamEntryID xadd(final String key, final StreamEntryID id, final Map<String, String> hash) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<StreamEntryID>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public StreamEntryID doExecute(Jedis connection) {
                return connection.xadd(key, id, hash);
            }
        }.run(key);
    }

    @Override
    public StreamEntryID xadd(final String key, final StreamEntryID id, final Map<String, String> hash, final long maxLen,
            final boolean approximateLength) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<StreamEntryID>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public StreamEntryID doExecute(Jedis connection) {
                return connection.xadd(key, id, hash, maxLen, approximateLength);
            }
        }.run(key);
    }

    @Override
    public StreamEntryID xadd(final String key, final Map<String, String> hash, final XAddParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<StreamEntryID>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public StreamEntryID doExecute(Jedis connection) {
                return connection.xadd(key, hash, params);
            }
        }.run(key);
    }

    @Override
    public Long xlen(final String key) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xlen(key);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntry> xrange(final String key, final StreamEntryID start, final StreamEntryID end) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<StreamEntry>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<StreamEntry> doExecute(Jedis connection) {
                return connection.xrange(key, start, end);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntry> xrange(final String key, final StreamEntryID start, final StreamEntryID end, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<StreamEntry>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<StreamEntry> doExecute(Jedis connection) {
                return connection.xrange(key, start, end, count);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntry> xrevrange(final String key, final StreamEntryID end, final StreamEntryID start) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<StreamEntry>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<StreamEntry> doExecute(Jedis connection) {
                return connection.xrevrange(key, end, start);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntry> xrevrange(final String key, final StreamEntryID end, final StreamEntryID start, final int count) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<StreamEntry>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<StreamEntry> doExecute(Jedis connection) {
                return connection.xrevrange(key, end, start, count);
            }
        }.run(key);
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xread(final int count, final long block,
            final Entry<String, StreamEntryID>... streams) {
        String[] keys = new String[streams.length];
        for (int i = 0; i < streams.length; ++i) {
            keys[i] = streams[i].getKey();
        }
        return new ConfigurableJedisClusterCommand<List<Entry<String, List<StreamEntry>>>>(connectionHandler, maxAttempts,
                this.maxTotalRetriesDuration) {
            @Override
            public List<Entry<String, List<StreamEntry>>> doExecute(Jedis connection) {
                return connection.xread(count, block, streams);
            }
        }.run(keys.length, keys);
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xread(final XReadParams xReadParams, final Map<String, StreamEntryID> streams) {
        return new ConfigurableJedisClusterCommand<List<Entry<String, List<StreamEntry>>>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<Entry<String, List<StreamEntry>>> doExecute(Jedis connection) {
                return connection.xread(xReadParams, streams);
            }
        }.run(streams.size(), getKeys(streams));
    }

    @Override
    public Long xack(final String key, final String group, final StreamEntryID... ids) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xack(key, group, ids);
            }
        }.run(key);
    }

    @Override
    public String xgroupCreate(final String key, final String groupname, final StreamEntryID id, final boolean makeStream) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.xgroupCreate(key, groupname, id, makeStream);
            }
        }.run(key);
    }

    @Override
    public String xgroupSetID(final String key, final String groupname, final StreamEntryID id) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.xgroupSetID(key, groupname, id);
            }
        }.run(key);
    }

    @Override
    public Long xgroupDestroy(final String key, final String groupname) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xgroupDestroy(key, groupname);
            }
        }.run(key);
    }

    @Override
    public Long xgroupDelConsumer(final String key, final String groupname, final String consumername) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xgroupDelConsumer(key, groupname, consumername);
            }
        }.run(key);
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xreadGroup(final String groupname, final String consumer, final int count,
            final long block, final boolean noAck, final Entry<String, StreamEntryID>... streams) {

        String[] keys = new String[streams.length];
        for (int i = 0; i < streams.length; ++i) {
            keys[i] = streams[i].getKey();
        }
        return new ConfigurableJedisClusterCommand<List<Entry<String, List<StreamEntry>>>>(connectionHandler, maxAttempts,
                this.maxTotalRetriesDuration) {
            @Override
            public List<Entry<String, List<StreamEntry>>> doExecute(Jedis connection) {
                return connection.xreadGroup(groupname, consumer, count, block, noAck, streams);
            }
        }.run(keys.length, keys);
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xreadGroup(final String groupname, final String consumer,
            final XReadGroupParams xReadGroupParams, final Map<String, StreamEntryID> streams) {
        return new ConfigurableJedisClusterCommand<List<Entry<String, List<StreamEntry>>>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<Entry<String, List<StreamEntry>>> doExecute(Jedis connection) {
                return connection.xreadGroup(groupname, consumer, xReadGroupParams, streams);
            }
        }.run(streams.size(), getKeys(streams));
    }

    @Override
    public StreamPendingSummary xpending(final String key, final String groupname) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<StreamPendingSummary>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public StreamPendingSummary doExecute(Jedis connection) {
                return connection.xpending(key, groupname);
            }
        }.run(key);
    }

    @Override
    public List<StreamPendingEntry> xpending(final String key, final String groupname, final StreamEntryID start,
            final StreamEntryID end, final int count, final String consumername) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<StreamPendingEntry>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<StreamPendingEntry> doExecute(Jedis connection) {
                return connection.xpending(key, groupname, start, end, count, consumername);
            }
        }.run(key);
    }

    @Override
    public List<StreamPendingEntry> xpending(final String key, final String groupname, final XPendingParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<StreamPendingEntry>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<StreamPendingEntry> doExecute(Jedis connection) {
                return connection.xpending(key, groupname, params);
            }
        }.run(key);
    }

    @Override
    public Long xdel(final String key, final StreamEntryID... ids) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xdel(key, ids);
            }
        }.run(key);
    }

    @Override
    public Long xtrim(final String key, final long maxLen, final boolean approximateLength) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xtrim(key, maxLen, approximateLength);
            }
        }.run(key);
    }

    @Override
    public Long xtrim(final String key, final XTrimParams params) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xtrim(key, params);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntry> xclaim(final String key, final String group, final String consumername, final long minIdleTime,
            final long newIdleTime, final int retries, final boolean force, final StreamEntryID... ids) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<StreamEntry>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<StreamEntry> doExecute(Jedis connection) {
                return connection.xclaim(key, group, consumername, minIdleTime, newIdleTime, retries, force, ids);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, XClaimParams params,
            StreamEntryID... ids) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<StreamEntry>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<StreamEntry> doExecute(Jedis connection) {
                return connection.xclaim(key, group, consumername, minIdleTime, params, ids);
            }
        }.run(key);
    }

    @Override
    public List<StreamEntryID> xclaimJustId(String key, String group, String consumername, long minIdleTime, XClaimParams params,
            StreamEntryID... ids) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<List<StreamEntryID>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<StreamEntryID> doExecute(Jedis connection) {
                return connection.xclaimJustId(key, group, consumername, minIdleTime, params, ids);
            }
        }.run(key);
    }

    public Long waitReplicas(final String key, final int replicas, final long timeout) {
        checkArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.waitReplicas(replicas, timeout);
            }
        }.run(key);
    }

    public Object sendCommand(final String sampleKey, final ProtocolCommand cmd, final String... args) {
        checkArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.sendCommand(cmd, args);
            }
        }.run(sampleKey);
    }

    public Object sendBlockingCommand(final String sampleKey, final ProtocolCommand cmd, final String... args) {
        checkArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.sendBlockingCommand(cmd, args);
            }
        }.run(sampleKey);
    }

    // ----------------------- BinaryJedisCluster ---------------------------

    @Override
    public Boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
        checkBinaryArguments(srcKey);
        checkBinaryArguments(dstKey);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.copy(srcKey, dstKey, replace);
            }
        }.runBinary(2, srcKey, dstKey);
    }

    @Override
    public String set(final byte[] key, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.set(key, value);
            }
        }.runBinary(key);
    }

    @Override
    public String set(final byte[] key, final byte[] value, final SetParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.set(key, value, params);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] get(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.get(key);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] getDel(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.getDel(key);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] getEx(byte[] key, GetExParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.getEx(key, params);
            }
        }.runBinary(key);
    }

    @Override
    public Long exists(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.exists(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Boolean exists(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.exists(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long persist(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.persist(key);
            }
        }.runBinary(key);
    }

    @Override
    public String type(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.type(key);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] dump(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.dump(key);
            }
        }.runBinary(key);
    }

    @Override
    public String restore(final byte[] key, final long ttl, final byte[] serializedValue) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.restore(key, ttl, serializedValue);
            }
        }.runBinary(key);
    }

    @Override
    public String restore(final byte[] key, final long ttl, final byte[] serializedValue, final RestoreParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.restore(key, ttl, serializedValue, params);
            }
        }.runBinary(key);
    }

    @Override
    public Long expire(final byte[] key, final int seconds) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.expire(key, seconds);
            }
        }.runBinary(key);
    }

    @Override
    public Long pexpire(final byte[] key, final long milliseconds) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pexpire(key, milliseconds);
            }
        }.runBinary(key);
    }

    @Override
    public Long expireAt(final byte[] key, final long unixTime) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.expireAt(key, unixTime);
            }
        }.runBinary(key);
    }

    @Override
    public Long pexpireAt(final byte[] key, final long millisecondsTimestamp) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pexpireAt(key, millisecondsTimestamp);
            }
        }.runBinary(key);
    }

    @Override
    public Long ttl(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.ttl(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long pttl(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pttl(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long touch(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.touch(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long touch(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.touch(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Boolean setbit(final byte[] key, final long offset, final boolean value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.runBinary(key);
    }

    @Override
    public Boolean setbit(final byte[] key, final long offset, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.setbit(key, offset, value);
            }
        }.runBinary(key);
    }

    @Override
    public Boolean getbit(final byte[] key, final long offset) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.getbit(key, offset);
            }
        }.runBinary(key);
    }

    @Override
    public Long setrange(final byte[] key, final long offset, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.setrange(key, offset, value);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] getrange(final byte[] key, final long startOffset, final long endOffset) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.getrange(key, startOffset, endOffset);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] getSet(final byte[] key, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.getSet(key, value);
            }
        }.runBinary(key);
    }

    @Override
    public Long setnx(final byte[] key, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.setnx(key, value);
            }
        }.runBinary(key);
    }

    @Override
    public String psetex(final byte[] key, final long milliseconds, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.psetex(key, milliseconds, value);
            }
        }.runBinary(key);
    }

    @Override
    public String setex(final byte[] key, final long seconds, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.setex(key, seconds, value);
            }
        }.runBinary(key);
    }

    @Override
    public Long decrBy(final byte[] key, final long decrement) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.decrBy(key, decrement);
            }
        }.runBinary(key);
    }

    @Override
    public Long decr(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.decr(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long incrBy(final byte[] key, final long increment) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.incrBy(key, increment);
            }
        }.runBinary(key);
    }

    @Override
    public Double incrByFloat(final byte[] key, final double increment) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.incrByFloat(key, increment);
            }
        }.runBinary(key);
    }

    @Override
    public Long incr(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.incr(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long append(final byte[] key, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.append(key, value);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] substr(final byte[] key, final int start, final int end) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.substr(key, start, end);
            }
        }.runBinary(key);
    }

    @Override
    public Long hset(final byte[] key, final byte[] field, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hset(key, field, value);
            }
        }.runBinary(key);
    }

    @Override
    public Long hset(final byte[] key, final Map<byte[], byte[]> hash) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hset(key, hash);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] hget(final byte[] key, final byte[] field) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.hget(key, field);
            }
        }.runBinary(key);
    }

    @Override
    public Long hsetnx(final byte[] key, final byte[] field, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hsetnx(key, field, value);
            }
        }.runBinary(key);
    }

    @Override
    public String hmset(final byte[] key, final Map<byte[], byte[]> hash) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.hmset(key, hash);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> hmget(final byte[] key, final byte[]... fields) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.hmget(key, fields);
            }
        }.runBinary(key);
    }

    @Override
    public Long hincrBy(final byte[] key, final byte[] field, final long value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hincrBy(key, field, value);
            }
        }.runBinary(key);
    }

    @Override
    public Double hincrByFloat(final byte[] key, final byte[] field, final double value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.hincrByFloat(key, field, value);
            }
        }.runBinary(key);
    }

    @Override
    public Boolean hexists(final byte[] key, final byte[] field) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.hexists(key, field);
            }
        }.runBinary(key);
    }

    @Override
    public Long hdel(final byte[] key, final byte[]... field) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hdel(key, field);
            }
        }.runBinary(key);
    }

    @Override
    public Long hlen(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hlen(key);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> hkeys(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.hkeys(key);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> hvals(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.hvals(key);
            }
        }.runBinary(key);
    }

    @Override
    public Map<byte[], byte[]> hgetAll(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Map<byte[], byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Map<byte[], byte[]> doExecute(Jedis connection) {
                return connection.hgetAll(key);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] hrandfield(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.hrandfield(key);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> hrandfield(final byte[] key, final long count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.hrandfield(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public Map<byte[], byte[]> hrandfieldWithValues(final byte[] key, final long count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Map<byte[], byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Map<byte[], byte[]> doExecute(Jedis connection) {
                return connection.hrandfieldWithValues(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public Long rpush(final byte[] key, final byte[]... args) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.rpush(key, args);
            }
        }.runBinary(key);
    }

    @Override
    public Long lpush(final byte[] key, final byte[]... args) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lpush(key, args);
            }
        }.runBinary(key);
    }

    @Override
    public Long llen(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.llen(key);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> lrange(final byte[] key, final long start, final long stop) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.lrange(key, start, stop);
            }
        }.runBinary(key);
    }

    @Override
    public String ltrim(final byte[] key, final long start, final long stop) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.ltrim(key, start, stop);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] lindex(final byte[] key, final long index) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.lindex(key, index);
            }
        }.runBinary(key);
    }

    @Override
    public String lset(final byte[] key, final long index, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.lset(key, index, value);
            }
        }.runBinary(key);
    }

    @Override
    public Long lrem(final byte[] key, final long count, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lrem(key, count, value);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] lpop(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.lpop(key);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> lpop(final byte[] key, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.lpop(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public Long lpos(final byte[] key, final byte[] element) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lpos(key, element);
            }
        }.runBinary(key);
    }

    @Override
    public Long lpos(final byte[] key, final byte[] element, final LPosParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lpos(key, element, params);
            }
        }.runBinary(key);
    }

    @Override
    public List<Long> lpos(final byte[] key, final byte[] element, final LPosParams params, final long count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<Long>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Long> doExecute(Jedis connection) {
                return connection.lpos(key, element, params, count);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] rpop(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.rpop(key);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> rpop(final byte[] key, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.rpop(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public Long sadd(final byte[] key, final byte[]... member) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sadd(key, member);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> smembers(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.smembers(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long srem(final byte[] key, final byte[]... member) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.srem(key, member);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] spop(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.spop(key);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> spop(final byte[] key, final long count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.spop(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public Long scard(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.scard(key);
            }
        }.runBinary(key);
    }

    @Override
    public Boolean sismember(final byte[] key, final byte[] member) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Boolean>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Boolean doExecute(Jedis connection) {
                return connection.sismember(key, member);
            }
        }.runBinary(key);
    }

    @Override
    public List<Boolean> smismember(final byte[] key, final byte[]... members) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<Boolean>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Boolean> doExecute(Jedis connection) {
                return connection.smismember(key, members);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] srandmember(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.srandmember(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long strlen(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.strlen(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long zadd(final byte[] key, final double score, final byte[] member) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zadd(key, score, member);
            }
        }.runBinary(key);
    }

    @Override
    public Long zadd(final byte[] key, final double score, final byte[] member, final ZAddParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zadd(key, score, member, params);
            }
        }.runBinary(key);
    }

    @Override
    public Long zadd(final byte[] key, final Map<byte[], Double> scoreMembers) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zadd(key, scoreMembers);
            }
        }.runBinary(key);
    }

    @Override
    public Long zadd(final byte[] key, final Map<byte[], Double> scoreMembers, final ZAddParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zadd(key, scoreMembers, params);
            }
        }.runBinary(key);
    }

    @Override
    public Double zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.zaddIncr(key, score, member, params);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zdiff(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zdiff(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Set<Tuple> zdiffWithScores(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zdiffWithScores(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Long zdiffStore(final byte[] dstkey, final byte[]... keys) {
        byte[][] wholeKeys = KeyMergeUtil.merge(dstkey, keys);
        checkBinaryArguments(dstkey);
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zdiffStore(dstkey, keys);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public Set<byte[]> zrange(final byte[] key, final long start, final long stop) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrange(key, start, stop);
            }
        }.runBinary(key);
    }

    @Override
    public Long zrem(final byte[] key, final byte[]... members) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zrem(key, members);
            }
        }.runBinary(key);
    }

    @Override
    public Double zincrby(final byte[] key, final double increment, final byte[] member) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.zincrby(key, increment, member);
            }
        }.runBinary(key);
    }

    @Override
    public Double zincrby(final byte[] key, final double increment, final byte[] member, final ZIncrByParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.zincrby(key, increment, member, params);
            }
        }.runBinary(key);
    }

    @Override
    public Long zrank(final byte[] key, final byte[] member) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zrank(key, member);
            }
        }.runBinary(key);
    }

    @Override
    public Long zrevrank(final byte[] key, final byte[] member) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zrevrank(key, member);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrevrange(final byte[] key, final long start, final long stop) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrevrange(key, start, stop);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrangeWithScores(final byte[] key, final long start, final long stop) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeWithScores(key, start, stop);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(final byte[] key, final long start, final long stop) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeWithScores(key, start, stop);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] zrandmember(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.zrandmember(key);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrandmember(final byte[] key, final long count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrandmember(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrandmemberWithScores(final byte[] key, final long count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrandmemberWithScores(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public Long zcard(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zcard(key);
            }
        }.runBinary(key);
    }

    @Override
    public Double zscore(final byte[] key, final byte[] member) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.zscore(key, member);
            }
        }.runBinary(key);
    }

    @Override
    public List<Double> zmscore(final byte[] key, final byte[]... members) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<Double>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Double> doExecute(Jedis connection) {
                return connection.zmscore(key, members);
            }
        }.runBinary(key);
    }

    @Override
    public Tuple zpopmax(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Tuple>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Tuple doExecute(Jedis connection) {
                return connection.zpopmax(key);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zpopmax(final byte[] key, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zpopmax(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public Tuple zpopmin(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Tuple>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Tuple doExecute(Jedis connection) {
                return connection.zpopmin(key);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zpopmin(final byte[] key, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zpopmin(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> sort(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.sort(key);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> sort(final byte[] key, final SortingParams sortingParameters) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.sort(key, sortingParameters);
            }
        }.runBinary(key);
    }

    @Override
    public Long zcount(final byte[] key, final double min, final double max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Long zcount(final byte[] key, final byte[] min, final byte[] max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zcount(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrangeByScore(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final double min, final double max, final int offset, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrangeByScore(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrangeByScore(key, min, max, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final double max, final double min, final int offset, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final double min, final double max, final int offset,
            final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrevrangeByScore(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrevrangeByScore(key, max, min, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final byte[] key, final byte[] min, final byte[] max, final int offset,
            final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final double max, final double min, final int offset,
            final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final byte[] key, final byte[] max, final byte[] min, final int offset,
            final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Long zremrangeByRank(final byte[] key, final long start, final long stop) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zremrangeByRank(key, start, stop);
            }
        }.runBinary(key);
    }

    @Override
    public Long zremrangeByScore(final byte[] key, final double min, final double max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zremrangeByScore(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Long zremrangeByScore(final byte[] key, final byte[] min, final byte[] max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zremrangeByScore(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Long linsert(final byte[] key, final ListPosition where, final byte[] pivot, final byte[] value) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.linsert(key, where, pivot, value);
            }
        }.runBinary(key);
    }

    @Override
    public Long lpushx(final byte[] key, final byte[]... arg) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.lpushx(key, arg);
            }
        }.runBinary(key);
    }

    @Override
    public Long rpushx(final byte[] key, final byte[]... arg) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.rpushx(key, arg);
            }
        }.runBinary(key);
    }

    @Override
    public Long del(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.del(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long unlink(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.unlink(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long unlink(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.unlink(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public byte[] echo(final byte[] arg) {
        // note that it'll be run from arbitary node
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.echo(arg);
            }
        }.runBinary(arg);
    }

    @Override
    public Long bitcount(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.bitcount(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long bitcount(final byte[] key, final long start, final long end) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.bitcount(key, start, end);
            }
        }.runBinary(key);
    }

    @Override
    public Long pfadd(final byte[] key, final byte[]... elements) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pfadd(key, elements);
            }
        }.runBinary(key);
    }

    @Override
    public long pfcount(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pfcount(key);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> srandmember(final byte[] key, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.srandmember(key, count);
            }
        }.runBinary(key);
    }

    @Override
    public Long zlexcount(final byte[] key, final byte[] min, final byte[] max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zlexcount(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrangeByLex(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrangeByLex(final byte[] key, final byte[] min, final byte[] max, final int offset, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrangeByLex(key, min, max, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrevrangeByLex(key, max, min);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> zrevrangeByLex(final byte[] key, final byte[] max, final byte[] min, final int offset, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zrevrangeByLex(key, max, min, offset, count);
            }
        }.runBinary(key);
    }

    @Override
    public Long zremrangeByLex(final byte[] key, final byte[] min, final byte[] max) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zremrangeByLex(key, min, max);
            }
        }.runBinary(key);
    }

    @Override
    public Object eval(final byte[] script, final byte[] keyCount, final byte[]... params) {
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.eval(script, keyCount, params);
            }
        }.runBinary(Integer.parseInt(SafeEncoder.encode(keyCount)), params);
    }

    @Override
    public Object eval(final byte[] script, final int keyCount, final byte[]... params) {
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.eval(script, keyCount, params);
            }
        }.runBinary(keyCount, params);
    }

    @Override
    public Object eval(final byte[] script, final List<byte[]> keys, final List<byte[]> args) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.eval(script, keys, args);
            }
        }.runBinary(keys.size(), keys.toArray(new byte[keys.size()][]));
    }

    @Override
    public Object eval(final byte[] script, final byte[] sampleKey) {
        checkBinaryArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.eval(script);
            }
        }.runBinary(sampleKey);
    }

    @Override
    public Object evalsha(final byte[] sha1, final byte[] sampleKey) {
        checkBinaryArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.evalsha(sha1);
            }
        }.runBinary(sampleKey);
    }

    @Override
    public Object evalsha(final byte[] sha1, final List<byte[]> keys, final List<byte[]> args) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.evalsha(sha1, keys, args);
            }
        }.runBinary(keys.size(), keys.toArray(new byte[keys.size()][]));
    }

    @Override
    public Object evalsha(final byte[] sha1, final int keyCount, final byte[]... params) {
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.evalsha(sha1, keyCount, params);
            }
        }.runBinary(keyCount, params);
    }

    @Override
    public List<Long> scriptExists(final byte[] sampleKey, final byte[]... sha1) {
        checkBinaryArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<List<Long>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Long> doExecute(Jedis connection) {
                return connection.scriptExists(sha1);
            }
        }.runBinary(sampleKey);
    }

    @Override
    public byte[] scriptLoad(final byte[] script, final byte[] sampleKey) {
        checkBinaryArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.scriptLoad(script);
            }
        }.runBinary(sampleKey);
    }

    @Override
    public String scriptFlush(final byte[] sampleKey) {
        checkBinaryArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.scriptFlush();
            }
        }.runBinary(sampleKey);
    }

    @Override
    public String scriptFlush(final byte[] sampleKey, final FlushMode flushMode) {
        checkBinaryArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.scriptFlush(flushMode);
            }
        }.runBinary(sampleKey);
    }

    @Override
    public String scriptKill(final byte[] sampleKey) {
        checkBinaryArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.scriptKill();
            }
        }.runBinary(sampleKey);
    }

    @Override
    public Long del(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.del(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public byte[] lmove(final byte[] srcKey, final byte[] dstKey, final ListDirection from, final ListDirection to) {
        checkBinaryArguments(srcKey);
        checkBinaryArguments(dstKey);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.lmove(srcKey, dstKey, from, to);
            }
        }.runBinary(2, srcKey, dstKey);
    }

    @Override
    public byte[] blmove(final byte[] srcKey, final byte[] dstKey, final ListDirection from, final ListDirection to,
            final double timeout) {
        checkBinaryArguments(srcKey);
        checkBinaryArguments(dstKey);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.blmove(srcKey, dstKey, from, to, timeout);
            }
        }.runBinary(2, srcKey, dstKey);
    }

    @Override
    public List<byte[]> blpop(final int timeout, final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.blpop(timeout, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<byte[]> blpop(final double timeout, final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.blpop(timeout, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<byte[]> brpop(final int timeout, final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.brpop(timeout, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<byte[]> brpop(final double timeout, final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.brpop(timeout, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<byte[]> bzpopmax(double timeout, byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.bzpopmax(timeout, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<byte[]> bzpopmin(double timeout, byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.bzpopmin(timeout, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<byte[]> mget(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.mget(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public String mset(final byte[]... keysvalues) {
        byte[][] keys = new byte[keysvalues.length / 2][];

        for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
            keys[keyIdx] = keysvalues[keyIdx * 2];
        }

        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.mset(keysvalues);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Long msetnx(final byte[]... keysvalues) {
        byte[][] keys = new byte[keysvalues.length / 2][];

        for (int keyIdx = 0; keyIdx < keys.length; keyIdx++) {
            keys[keyIdx] = keysvalues[keyIdx * 2];
        }
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.msetnx(keysvalues);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public String rename(final byte[] oldkey, final byte[] newkey) {
        checkBinaryArguments(oldkey);
        checkBinaryArguments(newkey);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.rename(oldkey, newkey);
            }
        }.runBinary(2, oldkey, newkey);
    }

    @Override
    public Long renamenx(final byte[] oldkey, final byte[] newkey) {
        checkBinaryArguments(oldkey);
        checkBinaryArguments(newkey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.renamenx(oldkey, newkey);
            }
        }.runBinary(2, oldkey, newkey);
    }

    @Override
    public byte[] rpoplpush(final byte[] srckey, final byte[] dstkey) {
        checkBinaryArguments(srckey);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.rpoplpush(srckey, dstkey);
            }
        }.runBinary(2, srckey, dstkey);
    }

    @Override
    public Set<byte[]> sdiff(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.sdiff(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Long sdiffstore(final byte[] dstkey, final byte[]... keys) {
        byte[][] wholeKeys = KeyMergeUtil.merge(dstkey, keys);
        checkBinaryArguments(dstkey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sdiffstore(dstkey, keys);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public Set<byte[]> sinter(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.sinter(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Long sinterstore(final byte[] dstkey, final byte[]... keys) {
        checkBinaryArguments(dstkey);
        checkBinaryArguments(keys);
        byte[][] wholeKeys = KeyMergeUtil.merge(dstkey, keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sinterstore(dstkey, keys);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public Long smove(final byte[] srckey, final byte[] dstkey, final byte[] member) {
        checkBinaryArguments(dstkey);
        checkBinaryArguments(srckey);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.smove(srckey, dstkey, member);
            }
        }.runBinary(2, srckey, dstkey);
    }

    @Override
    public Long sort(final byte[] key, final SortingParams sortingParameters, final byte[] dstkey) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sort(key, sortingParameters, dstkey);
            }
        }.runBinary(2, key, dstkey);
    }

    @Override
    public Long sort(final byte[] key, final byte[] dstkey) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sort(key, dstkey);
            }
        }.runBinary(2, key, dstkey);
    }

    @Override
    public Set<byte[]> sunion(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.sunion(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Long sunionstore(final byte[] dstkey, final byte[]... keys) {
        checkBinaryArguments(dstkey);
        checkBinaryArguments(keys);
        byte[][] wholeKeys = KeyMergeUtil.merge(dstkey, keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.sunionstore(dstkey, keys);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public Set<byte[]> zinter(final ZParams params, final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zinter(params, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Set<Tuple> zinterWithScores(final ZParams params, final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zinterWithScores(params, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Long zinterstore(final byte[] dstkey, final byte[]... sets) {
        checkBinaryArguments(dstkey);
        byte[][] wholeKeys = KeyMergeUtil.merge(dstkey, sets);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zinterstore(dstkey, sets);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public Long zinterstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
        checkBinaryArguments(dstkey);
        byte[][] wholeKeys = KeyMergeUtil.merge(dstkey, sets);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zinterstore(dstkey, params, sets);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public Set<byte[]> zunion(final ZParams params, final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.zunion(params, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Set<Tuple> zunionWithScores(final ZParams params, final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Set<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<Tuple> doExecute(Jedis connection) {
                return connection.zunionWithScores(params, keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Long zunionstore(final byte[] dstkey, final byte[]... sets) {
        checkBinaryArguments(dstkey);
        byte[][] wholeKeys = KeyMergeUtil.merge(dstkey, sets);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zunionstore(dstkey, sets);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public Long zunionstore(final byte[] dstkey, final ZParams params, final byte[]... sets) {
        checkBinaryArguments(dstkey);
        byte[][] wholeKeys = KeyMergeUtil.merge(dstkey, sets);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.zunionstore(dstkey, params, sets);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public byte[] brpoplpush(final byte[] source, final byte[] destination, final int timeout) {
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.brpoplpush(source, destination, timeout);
            }
        }.runBinary(2, source, destination);
    }

    @Override
    public Long publish(final byte[] channel, final byte[] message) {
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.publish(channel, message);
            }
        }.runWithAnyNode();
    }

    @Override
    public void subscribe(final BinaryJedisPubSub jedisPubSub, final byte[]... channels) {
        new ConfigurableJedisClusterCommand<Integer>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Integer doExecute(Jedis connection) {
                connection.subscribe(jedisPubSub, channels);
                return 0;
            }
        }.runWithAnyNode();
    }

    @Override
    public void psubscribe(final BinaryJedisPubSub jedisPubSub, final byte[]... patterns) {
        new ConfigurableJedisClusterCommand<Integer>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Integer doExecute(Jedis connection) {
                connection.psubscribe(jedisPubSub, patterns);
                return 0;
            }
        }.runWithAnyNode();
    }

    @Override
    public Long bitop(final BitOP op, final byte[] destKey, final byte[]... srcKeys) {
        checkBinaryArguments(destKey);
        checkBinaryArguments(srcKeys);
        byte[][] wholeKeys = KeyMergeUtil.merge(destKey, srcKeys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.bitop(op, destKey, srcKeys);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public String pfmerge(final byte[] destkey, final byte[]... sourcekeys) {
        checkBinaryArguments(destkey);
        checkBinaryArguments(sourcekeys);
        byte[][] wholeKeys = KeyMergeUtil.merge(destkey, sourcekeys);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.pfmerge(destkey, sourcekeys);
            }
        }.runBinary(wholeKeys.length, wholeKeys);
    }

    @Override
    public Long pfcount(final byte[]... keys) {
        checkBinaryArguments(keys);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.pfcount(keys);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public Long geoadd(final byte[] key, final double longitude, final double latitude, final byte[] member) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.geoadd(key, longitude, latitude, member);
            }
        }.runBinary(key);
    }

    @Override
    public Long geoadd(final byte[] key, final Map<byte[], GeoCoordinate> memberCoordinateMap) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.geoadd(key, memberCoordinateMap);
            }
        }.runBinary(key);
    }

    @Override
    public Long geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.geoadd(key, params, memberCoordinateMap);
            }
        }.runBinary(key);
    }

    @Override
    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.geodist(key, member1, member2);
            }
        }.runBinary(key);
    }

    @Override
    public Double geodist(final byte[] key, final byte[] member1, final byte[] member2, final GeoUnit unit) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Double>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Double doExecute(Jedis connection) {
                return connection.geodist(key, member1, member2, unit);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> geohash(final byte[] key, final byte[]... members) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.geohash(key, members);
            }
        }.runBinary(key);
    }

    @Override
    public List<GeoCoordinate> geopos(final byte[] key, final byte[]... members) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoCoordinate>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<GeoCoordinate> doExecute(Jedis connection) {
                return connection.geopos(key, members);
            }
        }.runBinary(key);
    }

    @Override
    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude, final double radius,
            final GeoUnit unit) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadius(key, longitude, latitude, radius, unit);
            }
        }.runBinary(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final byte[] key, final double longitude, final double latitude,
            final double radius, final GeoUnit unit) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusReadonly(key, longitude, latitude, radius, unit);
            }
        }.runBinary(key);
    }

    @Override
    public List<GeoRadiusResponse> georadius(final byte[] key, final double longitude, final double latitude, final double radius,
            final GeoUnit unit, final GeoRadiusParam param) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadius(key, longitude, latitude, radius, unit, param);
            }
        }.runBinary(key);
    }

    @Override
    public Long georadiusStore(final byte[] key, final double longitude, final double latitude, final double radius,
            final GeoUnit unit, final GeoRadiusParam param, final GeoRadiusStoreParam storeParam) {
        byte[][] keys = storeParam.getByteKeys(key);
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(final byte[] key, final double longitude, final double latitude,
            final double radius, final GeoUnit unit, final GeoRadiusParam param) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusReadonly(key, longitude, latitude, radius, unit, param);
            }
        }.runBinary(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius,
            final GeoUnit unit) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusByMember(key, member, radius, unit);
            }
        }.runBinary(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final byte[] key, final byte[] member, final double radius,
            final GeoUnit unit) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusByMemberReadonly(key, member, radius, unit);
            }
        }.runBinary(key);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(final byte[] key, final byte[] member, final double radius,
            final GeoUnit unit, final GeoRadiusParam param) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusByMember(key, member, radius, unit, param);
            }
        }.runBinary(key);
    }

    @Override
    public Long georadiusByMemberStore(final byte[] key, final byte[] member, final double radius, final GeoUnit unit,
            final GeoRadiusParam param, final GeoRadiusStoreParam storeParam) {
        byte[][] keys = storeParam.getByteKeys(key);
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.georadiusByMemberStore(key, member, radius, unit, param, storeParam);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(final byte[] key, final byte[] member, final double radius,
            final GeoUnit unit, final GeoRadiusParam param) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<GeoRadiusResponse>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public List<GeoRadiusResponse> doExecute(Jedis connection) {
                return connection.georadiusByMemberReadonly(key, member, radius, unit, param);
            }
        }.runBinary(key);
    }

    @Override
    public Set<byte[]> keys(final byte[] pattern) {
        if (pattern == null || pattern.length == 0) {
            throw new IllegalArgumentException(
                    this.getClass().getSimpleName() + " only supports KEYS commands with non-empty patterns");
        }
        if (!JedisClusterHashTagUtil.isClusterCompliantMatchPattern(pattern)) {
            throw new IllegalArgumentException(this.getClass().getSimpleName()
                    + " only supports KEYS commands with patterns containing hash-tags " + "( curly-brackets enclosed strings )");
        }
        return new ConfigurableJedisClusterCommand<Set<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Set<byte[]> doExecute(Jedis connection) {
                return connection.keys(pattern);
            }
        }.runBinary(pattern);
    }

    @Override
    public ScanResult<byte[]> scan(final byte[] cursor, final ScanParams params) {
        byte[] matchPattern = null;
        if (params == null || (matchPattern = doScanBinaryMatch(params)) == null || matchPattern.length == 0) {
            throw new IllegalArgumentException(
                    BinaryJedisCluster.class.getSimpleName() + " only supports SCAN commands with non-empty MATCH patterns");
        }
        if (!JedisClusterHashTagUtil.isClusterCompliantMatchPattern(matchPattern)) {
            throw new IllegalArgumentException(BinaryJedisCluster.class.getSimpleName()
                    + " only supports SCAN commands with MATCH patterns containing hash-tags"
                    + " ( curly-brackets enclosed strings )");
        }
        return new ConfigurableJedisClusterCommand<ScanResult<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public ScanResult<byte[]> doExecute(Jedis connection) {
                return connection.scan(cursor, params);
            }
        }.runBinary(matchPattern);
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<ScanResult<Map.Entry<byte[], byte[]>>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public ScanResult<Map.Entry<byte[], byte[]>> doExecute(Jedis connection) {
                return connection.hscan(key, cursor);
            }
        }.runBinary(key);
    }

    @Override
    public ScanResult<Map.Entry<byte[], byte[]>> hscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<ScanResult<Map.Entry<byte[], byte[]>>>(connectionHandler, maxAttempts,
                maxTotalRetriesDuration) {
            @Override
            public ScanResult<Map.Entry<byte[], byte[]>> doExecute(Jedis connection) {
                return connection.hscan(key, cursor, params);
            }
        }.runBinary(key);
    }

    @Override
    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<ScanResult<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public ScanResult<byte[]> doExecute(Jedis connection) {
                return connection.sscan(key, cursor);
            }
        }.runBinary(key);
    }

    @Override
    public ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<ScanResult<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public ScanResult<byte[]> doExecute(Jedis connection) {
                return connection.sscan(key, cursor, params);
            }
        }.runBinary(key);
    }

    @Override
    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<ScanResult<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public ScanResult<Tuple> doExecute(Jedis connection) {
                return connection.zscan(key, cursor);
            }
        }.runBinary(key);
    }

    @Override
    public ScanResult<Tuple> zscan(final byte[] key, final byte[] cursor, final ScanParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<ScanResult<Tuple>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public ScanResult<Tuple> doExecute(Jedis connection) {
                return connection.zscan(key, cursor, params);
            }
        }.runBinary(key);
    }

    @Override
    public List<Long> bitfield(final byte[] key, final byte[]... arguments) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<Long>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Long> doExecute(Jedis connection) {
                return connection.bitfield(key, arguments);
            }
        }.runBinary(key);
    }

    @Override
    public List<Long> bitfieldReadonly(final byte[] key, final byte[]... arguments) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<Long>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Long> doExecute(Jedis connection) {
                return connection.bitfieldReadonly(key, arguments);
            }
        }.runBinary(key);
    }

    @Override
    public Long hstrlen(final byte[] key, final byte[] field) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.hstrlen(key, field);
            }
        }.runBinary(key);
    }

    @Override
    public Long memoryUsage(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.memoryUsage(key);
            }
        }.runBinary(key);
    }

    @Override
    public Long memoryUsage(final byte[] key, final int samples) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.memoryUsage(key, samples);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] xadd(final byte[] key, final byte[] id, final Map<byte[], byte[]> hash, final long maxLen,
            final boolean approximateLength) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.xadd(key, id, hash, maxLen, approximateLength);
            }
        }.runBinary(key);
    }

    @Override
    public byte[] xadd(final byte[] key, final Map<byte[], byte[]> hash, final XAddParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<byte[]>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public byte[] doExecute(Jedis connection) {
                return connection.xadd(key, hash, params);
            }
        }.runBinary(key);
    }

    @Override
    public Long xlen(final byte[] key) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xlen(key);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xrange(final byte[] key, final byte[] start, final byte[] end) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xrange(key, start, end);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xrange(final byte[] key, final byte[] start, final byte[] end, final long count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xrange(key, start, end, count);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xrange(final byte[] key, final byte[] start, final byte[] end, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xrange(key, start, end, count);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xrevrange(final byte[] key, final byte[] end, final byte[] start) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xrevrange(key, end, start);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xrevrange(final byte[] key, final byte[] end, final byte[] start, final int count) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xrevrange(key, end, start, count);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xread(final int count, final long block, final Map<byte[], byte[]> streams) {
        byte[][] keys = streams.keySet().toArray(new byte[streams.size()][]);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xread(count, block, streams);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<byte[]> xread(final XReadParams xReadParams, final Entry<byte[], byte[]>... streams) {
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xread(xReadParams, streams);
            }
        }.runBinary(streams.length, getKeys(streams));
    }

    @Override
    public Long xack(final byte[] key, final byte[] group, final byte[]... ids) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xack(key, group, ids);
            }
        }.runBinary(key);
    }

    @Override
    public String xgroupCreate(final byte[] key, final byte[] consumer, final byte[] id, final boolean makeStream) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.xgroupCreate(key, consumer, id, makeStream);
            }
        }.runBinary(key);
    }

    @Override
    public String xgroupSetID(final byte[] key, final byte[] consumer, final byte[] id) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<String>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public String doExecute(Jedis connection) {
                return connection.xgroupSetID(key, consumer, id);
            }
        }.runBinary(key);
    }

    @Override
    public Long xgroupDestroy(final byte[] key, final byte[] consumer) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xgroupDestroy(key, consumer);
            }
        }.runBinary(key);
    }

    @Override
    public Long xgroupDelConsumer(final byte[] key, final byte[] consumer, final byte[] consumerName) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xgroupDelConsumer(key, consumer, consumerName);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xreadGroup(final byte[] groupname, final byte[] consumer, final int count, final long block,
            final boolean noAck, final Map<byte[], byte[]> streams) {
        byte[][] keys = streams.keySet().toArray(new byte[streams.size()][]);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xreadGroup(groupname, consumer, count, block, noAck, streams);
            }
        }.runBinary(keys.length, keys);
    }

    @Override
    public List<byte[]> xreadGroup(final byte[] groupname, final byte[] consumer, final XReadGroupParams xReadGroupParams,
            final Entry<byte[], byte[]>... streams) {
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xreadGroup(groupname, consumer, xReadGroupParams, streams);
            }
        }.runBinary(streams.length, getKeys(streams));
    }

    @Override
    public Long xdel(final byte[] key, final byte[]... ids) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xdel(key, ids);
            }
        }.runBinary(key);
    }

    @Override
    public Long xtrim(final byte[] key, final long maxLen, final boolean approximateLength) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xtrim(key, maxLen, approximateLength);
            }
        }.runBinary(key);
    }

    @Override
    public Long xtrim(final byte[] key, final XTrimParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.xtrim(key, params);
            }
        }.runBinary(key);
    }

    @Override
    public List<Object> xpending(final byte[] key, final byte[] groupname, final byte[] start, final byte[] end, final int count,
            final byte[] consumername) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<Object>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Object> doExecute(Jedis connection) {
                return connection.xpending(key, groupname, start, end, count, consumername);
            }
        }.runBinary(key);
    }

    @Override
    public Object xpending(final byte[] key, final byte[] groupname) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.xpending(key, groupname);
            }
        }.runBinary(key);
    }

    @Override
    public List<Object> xpending(final byte[] key, final byte[] groupname, final XPendingParams params) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<Object>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<Object> doExecute(Jedis connection) {
                return connection.xpending(key, groupname, params);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xclaim(final byte[] key, final byte[] groupname, final byte[] consumername, final long minIdleTime,
            final long newIdleTime, final int retries, final boolean force, final byte[][] ids) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xclaim(key, groupname, consumername, minIdleTime, newIdleTime, retries, force, ids);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xclaim(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params,
            byte[]... ids) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xclaim(key, group, consumername, minIdleTime, params, ids);
            }
        }.runBinary(key);
    }

    @Override
    public List<byte[]> xclaimJustId(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params,
            byte[]... ids) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<List<byte[]>>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public List<byte[]> doExecute(Jedis connection) {
                return connection.xclaimJustId(key, group, consumername, minIdleTime, params, ids);
            }
        }.runBinary(key);
    }

    @Override
    public Long waitReplicas(final byte[] key, final int replicas, final long timeout) {
        checkBinaryArguments(key);
        return new ConfigurableJedisClusterCommand<Long>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Long doExecute(Jedis connection) {
                return connection.waitReplicas(replicas, timeout);
            }
        }.runBinary(key);
    }

    public Object sendCommand(final byte[] sampleKey, final ProtocolCommand cmd, final byte[]... args) {
        checkBinaryArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.sendCommand(cmd, args);
            }
        }.runBinary(sampleKey);
    }

    public Object sendBlockingCommand(final byte[] sampleKey, final ProtocolCommand cmd, final byte[]... args) {
        checkBinaryArguments(sampleKey);
        return new ConfigurableJedisClusterCommand<Object>(connectionHandler, maxAttempts, maxTotalRetriesDuration) {
            @Override
            public Object doExecute(Jedis connection) {
                return connection.sendBlockingCommand(cmd, args);
            }
        }.runBinary(sampleKey);
    }

    private static String[] getKeys(final Map<String, ?> map) {
        return map.keySet().toArray(new String[map.size()]);
    }

    private static byte[][] getKeys(final Entry<byte[], ?>... entries) {
        byte[][] keys = new byte[entries.length][];
        for (int i = 0; i < entries.length; i++) {
            keys[i] = entries[i].getKey();
        }
        return keys;
    }

    // ----------------------- Function's ---------------------------

    /**
     * Check input argument names specification.
     * 
     * @param keys
     * @throws ParameterCanonicalException
     */
    protected void checkBinaryArguments(final byte[]... keys) throws ParameterCanonicalException {
        if (safeMode) {
            RedisSpecUtil.safeCheckKeys(asList(keys));
        }
    }

    /**
     * Check input argument names specification.
     * 
     * @param keys
     * @throws ParameterCanonicalException
     */
    protected void checkBinaryArguments(final List<byte[]> keys) throws ParameterCanonicalException {
        if (safeMode) {
            RedisSpecUtil.safeCheckKeys(asList(keys));
        }
    }

    /**
     * Check input argument names specification.
     * 
     * @param keys
     * @throws ParameterCanonicalException
     */
    protected void checkArguments(final List<String> keys) throws ParameterCanonicalException {
        checkArguments(keys.toArray(new String[0]));
    }

    /**
     * Check input argument names specification.
     * 
     * @param keys
     * @throws ParameterCanonicalException
     */
    protected void checkArguments(final String... keys) throws ParameterCanonicalException {
        if (safeMode) {
            RedisSpecUtil.safeCheckKeys(asList(keys));
        }
    }

    /**
     * Scan cursor matching.
     * 
     * @param params
     * @return
     */
    protected String doScanMatch(ScanParams params) {
        try {
            return (String) PARAMS_MATCH.invoke(params);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Scan cursor matching.
     * 
     * @param params
     * @return
     */
    protected byte[] doScanBinaryMatch(ScanParams params) {
        try {
            return (byte[]) PARAMS_BINARYMATCH.invoke(params);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final Method PARAMS_MATCH;
    private static final Method PARAMS_BINARYMATCH;

    static {
        Method matchMothod = null;
        Method binaryMatchMothod = null;
        for (Method m : ScanParams.class.getDeclaredMethods()) {
            /**
             * {@link redis.clients.jedis.ScanParams#match()}
             */
            if (m.getName().equals("match") && m.getParameterCount() == 0) {
                matchMothod = m;
            }
            /**
             * {@link redis.clients.jedis.ScanParams#match()}
             */
            if (m.getName().equals("binaryMatch") && m.getParameterCount() == 0) {
                binaryMatchMothod = m;
            }
        }
        PARAMS_MATCH = matchMothod;
        PARAMS_BINARYMATCH = binaryMatchMothod;
        notNullOf(PARAMS_MATCH, "redis.clients.jedis.ScanParams#match()");
        notNullOf(PARAMS_BINARYMATCH, "redis.clients.jedis.ScanParams#binaryMatch()");
        PARAMS_MATCH.setAccessible(true);
        PARAMS_BINARYMATCH.setAccessible(true);
    }

}
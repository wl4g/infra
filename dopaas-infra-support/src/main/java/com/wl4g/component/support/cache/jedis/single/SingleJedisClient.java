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
package com.wl4g.component.support.cache.jedis.single;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Collections.singletonMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.wl4g.component.support.cache.jedis.JedisClient;

import redis.clients.jedis.AccessControlLogEntry;
import redis.clients.jedis.AccessControlUser;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.ClusterReset;
import redis.clients.jedis.DebugParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.ListPosition;
import redis.clients.jedis.Module;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.StreamConsumersInfo;
import redis.clients.jedis.StreamEntry;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.StreamGroupInfo;
import redis.clients.jedis.StreamInfo;
import redis.clients.jedis.StreamPendingEntry;
import redis.clients.jedis.StreamPendingSummary;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;
import redis.clients.jedis.args.FlushMode;
import redis.clients.jedis.args.ListDirection;
import redis.clients.jedis.args.UnblockType;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.ClientKillParams;
import redis.clients.jedis.params.GeoAddParams;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.GeoRadiusStoreParam;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.LPosParams;
import redis.clients.jedis.params.MigrateParams;
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
import redis.clients.jedis.util.Slowlog;

/**
 * Single mode Jedis client implemention.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020年7月18日 v1.0.0
 * @see
 */
@SuppressWarnings("unchecked")
public class SingleJedisClient implements JedisClient {

    /** Jedis single pool */
    protected final JedisPool jedisPool;

    /** Safety mode, validating storage key. */
    protected final boolean safeMode;

    public SingleJedisClient(JedisPool jedisPool, boolean safeMode) {
        notNullOf(jedisPool, "jedisPool");
        this.jedisPool = jedisPool;
        this.safeMode = safeMode;
    }

    @Override
    public Map<String, JedisPool> getClusterNodes() {
        return singletonMap("default", jedisPool);
    }

    @Override
    public void close() throws IOException {
        doExecuteWithRedis(jedis -> {
            jedis.close();
            return null;
        });
    }

    @Override
    public Boolean copy(byte[] srcKey, byte[] dstKey, int db, boolean replace) {
        return doExecuteWithRedis(jedis -> jedis.copy(srcKey, dstKey, db, replace));
    }

    @Override
    public Boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
        return doExecuteWithRedis(jedis -> jedis.copy(srcKey, dstKey, replace));
    }

    @Override
    public Long del(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.del(keys));
    }

    @Override
    public Long unlink(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.unlink(keys));
    }

    @Override
    public Long exists(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.exists(keys));
    }

    @Override
    public byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
        return doExecuteWithRedis(jedis -> jedis.lmove(srcKey, dstKey, from, to));
    }

    @Override
    public byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
        return doExecuteWithRedis(jedis -> jedis.blmove(srcKey, dstKey, from, to, timeout));
    }

    @Override
    public List<byte[]> blpop(int timeout, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.blpop(timeout, keys));
    }

    @Override
    public List<byte[]> blpop(double timeout, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.blpop(timeout, keys));
    }

    @Override
    public List<byte[]> brpop(int timeout, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.brpop(timeout, keys));
    }

    @Override
    public List<byte[]> brpop(double timeout, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.brpop(timeout, keys));
    }

    @Override
    public List<byte[]> blpop(byte[]... args) {
        return doExecuteWithRedis(jedis -> jedis.blpop(args));
    }

    @Override
    public List<byte[]> brpop(byte[]... args) {
        return doExecuteWithRedis(jedis -> jedis.brpop(args));
    }

    @Override
    public List<byte[]> bzpopmax(double timeout, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.bzpopmax(timeout, keys));
    }

    @Override
    public List<byte[]> bzpopmin(double timeout, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.bzpopmin(timeout, keys));
    }

    @Override
    public Set<byte[]> keys(byte[] pattern) {
        return doExecuteWithRedis(jedis -> jedis.keys(pattern));
    }

    @Override
    public List<byte[]> mget(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.mget(keys));
    }

    @Override
    public String mset(byte[]... keysvalues) {
        return doExecuteWithRedis(jedis -> jedis.mset(keysvalues));
    }

    @Override
    public Long msetnx(byte[]... keysvalues) {
        return doExecuteWithRedis(jedis -> jedis.msetnx(keysvalues));
    }

    @Override
    public String rename(byte[] oldkey, byte[] newkey) {
        return doExecuteWithRedis(jedis -> jedis.rename(oldkey, newkey));
    }

    @Override
    public Long renamenx(byte[] oldkey, byte[] newkey) {
        return doExecuteWithRedis(jedis -> jedis.renamenx(oldkey, newkey));
    }

    @Override
    public byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
        return doExecuteWithRedis(jedis -> jedis.rpoplpush(srckey, dstkey));
    }

    @Override
    public Set<byte[]> sdiff(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.sdiff(keys));
    }

    @Override
    public Long sdiffstore(byte[] dstkey, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.sdiffstore(dstkey, keys));
    }

    @Override
    public Set<byte[]> sinter(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.sinter(keys));
    }

    @Override
    public Long sinterstore(byte[] dstkey, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.sinterstore(dstkey, keys));
    }

    @Override
    public Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
        return doExecuteWithRedis(jedis -> jedis.smove(srckey, dstkey, member));
    }

    @Override
    public Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
        return doExecuteWithRedis(jedis -> jedis.sort(key, sortingParameters, dstkey));
    }

    @Override
    public Long sort(byte[] key, byte[] dstkey) {
        return doExecuteWithRedis(jedis -> jedis.sort(key, dstkey));
    }

    @Override
    public Set<byte[]> sunion(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.sunion(keys));
    }

    @Override
    public Long sunionstore(byte[] dstkey, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.sunionstore(dstkey, keys));
    }

    @Override
    public String watch(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.watch(keys));
    }

    @Override
    public Set<byte[]> zdiff(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.zdiff(keys));
    }

    @Override
    public Set<Tuple> zdiffWithScores(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.zdiffWithScores(keys));
    }

    @Override
    public Long zdiffStore(byte[] dstkey, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.zdiffStore(dstkey, keys));
    }

    @Override
    public Set<byte[]> zinter(ZParams params, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.zinter(params, keys));
    }

    @Override
    public Set<Tuple> zinterWithScores(ZParams params, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.zinterWithScores(params, keys));
    }

    @Override
    public Long zinterstore(byte[] dstkey, byte[]... sets) {
        return doExecuteWithRedis(jedis -> jedis.zinterstore(dstkey, sets));
    }

    @Override
    public Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return doExecuteWithRedis(jedis -> jedis.zinterstore(dstkey, params, sets));
    }

    @Override
    public Set<byte[]> zunion(ZParams params, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.zunion(params, keys));
    }

    @Override
    public Set<Tuple> zunionWithScores(ZParams params, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.zunionWithScores(params, keys));
    }

    @Override
    public Long zunionstore(byte[] dstkey, byte[]... sets) {
        return doExecuteWithRedis(jedis -> jedis.zunionstore(dstkey, sets));
    }

    @Override
    public Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
        return doExecuteWithRedis(jedis -> jedis.zunionstore(dstkey, params, sets));
    }

    @Override
    public byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
        return doExecuteWithRedis(jedis -> jedis.brpoplpush(source, destination, timeout));
    }

    @Override
    public Long publish(byte[] channel, byte[] message) {
        return doExecuteWithRedis(jedis -> jedis.publish(channel, message));
    }

    @Override
    public void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
        doExecuteWithRedis(jedis -> {
            jedis.subscribe(jedisPubSub, channels);
            return null;
        });
    }

    @Override
    public void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
        doExecuteWithRedis(jedis -> {
            jedis.psubscribe(jedisPubSub, patterns);
            return null;
        });
    }

    @Override
    public byte[] randomBinaryKey() {
        return doExecuteWithRedis(jedis -> jedis.randomBinaryKey());
    }

    @Override
    public Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
        return doExecuteWithRedis(jedis -> jedis.bitop(op, destKey, srcKeys));
    }

    @Override
    public String pfmerge(byte[] destkey, byte[]... sourcekeys) {
        return doExecuteWithRedis(jedis -> jedis.pfmerge(destkey, sourcekeys));
    }

    @Override
    public Long pfcount(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.pfcount(keys));
    }

    @Override
    public Long touch(byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.touch(keys));
    }

    @Override
    public List<byte[]> xread(int count, long block, Map<byte[], byte[]> streams) {
        return doExecuteWithRedis(jedis -> jedis.xread(count, block, streams));
    }

    @Override
    public List<byte[]> xread(XReadParams xReadParams, Entry<byte[], byte[]>... streams) {
        return doExecuteWithRedis(jedis -> jedis.xread(xReadParams, streams));
    }

    @Override
    public List<byte[]> xreadGroup(byte[] groupname, byte[] consumer, int count, long block, boolean noAck,
            Map<byte[], byte[]> streams) {
        return doExecuteWithRedis(jedis -> jedis.xreadGroup(groupname, consumer, count, block, noAck, streams));
    }

    @Override
    public List<byte[]> xreadGroup(byte[] groupname, byte[] consumer, XReadGroupParams xReadGroupParams,
            Entry<byte[], byte[]>... streams) {
        return doExecuteWithRedis(jedis -> jedis.xreadGroup(groupname, consumer, xReadGroupParams, streams));
    }

    @Override
    public Long georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param,
            GeoRadiusStoreParam storeParam) {
        return doExecuteWithRedis(jedis -> jedis.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam));
    }

    @Override
    public Long georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param,
            GeoRadiusStoreParam storeParam) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMemberStore(key, member, radius, unit, param, storeParam));
    }

    @Override
    public Object eval(byte[] script, byte[] keyCount, byte[]... params) {
        return doExecuteWithRedis(jedis -> jedis.eval(script, keyCount, params));
    }

    @Override
    public Object eval(byte[] script, int keyCount, byte[]... params) {
        return doExecuteWithRedis(jedis -> jedis.eval(script, keyCount, params));
    }

    @Override
    public Object eval(byte[] script, List<byte[]> keys, List<byte[]> args) {
        return doExecuteWithRedis(jedis -> jedis.eval(script, keys, args));
    }

    @Override
    public Object eval(byte[] script) {
        return doExecuteWithRedis(jedis -> jedis.eval(script));
    }

    @Override
    public Object evalsha(byte[] sha1) {
        return doExecuteWithRedis(jedis -> jedis.evalsha(sha1));
    }

    @Override
    public Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
        return doExecuteWithRedis(jedis -> jedis.evalsha(sha1, keys, args));
    }

    @Override
    public Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
        return doExecuteWithRedis(jedis -> jedis.evalsha(sha1, keyCount, params));
    }

    @Override
    public List<Long> scriptExists(byte[]... sha1) {
        return doExecuteWithRedis(jedis -> jedis.scriptExists(sha1));
    }

    @Override
    public byte[] scriptLoad(byte[] script) {
        return doExecuteWithRedis(jedis -> jedis.scriptLoad(script));
    }

    @Override
    public String scriptFlush() {
        return doExecuteWithRedis(jedis -> jedis.scriptFlush());
    }

    @Override
    public String scriptFlush(FlushMode flushMode) {
        return doExecuteWithRedis(jedis -> jedis.scriptFlush(flushMode));
    }

    @Override
    public String scriptKill() {
        return doExecuteWithRedis(jedis -> jedis.scriptKill());
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.set(key, value));
    }

    @Override
    public String set(byte[] key, byte[] value, SetParams params) {
        return doExecuteWithRedis(jedis -> jedis.set(key, value, params));
    }

    @Override
    public byte[] get(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.get(key));
    }

    @Override
    public byte[] getDel(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.getDel(key));
    }

    @Override
    public byte[] getEx(byte[] key, GetExParams params) {
        return doExecuteWithRedis(jedis -> jedis.getEx(key, params));
    }

    @Override
    public Boolean exists(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.exists(key));
    }

    @Override
    public Long persist(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.persist(key));
    }

    @Override
    public String type(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.type(key));
    }

    @Override
    public byte[] dump(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.dump(key));
    }

    @Override
    public String restore(byte[] key, long ttl, byte[] serializedValue) {
        return doExecuteWithRedis(jedis -> jedis.restore(key, ttl, serializedValue));
    }

    @Override
    public String restoreReplace(byte[] key, long ttl, byte[] serializedValue) {
        return doExecuteWithRedis(jedis -> jedis.restoreReplace(key, ttl, serializedValue));
    }

    @Override
    public String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
        return doExecuteWithRedis(jedis -> jedis.restore(key, ttl, serializedValue, params));
    }

    @Override
    public Long expire(byte[] key, long seconds) {
        return doExecuteWithRedis(jedis -> jedis.expire(key, seconds));
    }

    @Override
    public Long pexpire(byte[] key, long milliseconds) {
        return doExecuteWithRedis(jedis -> jedis.pexpire(key, milliseconds));
    }

    @Override
    public Long expireAt(byte[] key, long unixTime) {
        return doExecuteWithRedis(jedis -> jedis.expireAt(key, unixTime));
    }

    @Override
    public Long pexpireAt(byte[] key, long millisecondsTimestamp) {
        return doExecuteWithRedis(jedis -> jedis.pexpireAt(key, millisecondsTimestamp));
    }

    @Override
    public Long ttl(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.ttl(key));
    }

    @Override
    public Long pttl(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.pttl(key));
    }

    @Override
    public Long touch(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.touch(key));
    }

    @Override
    public Boolean setbit(byte[] key, long offset, boolean value) {
        return doExecuteWithRedis(jedis -> jedis.setbit(key, offset, value));
    }

    @Override
    public Boolean setbit(byte[] key, long offset, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.setbit(key, offset, value));
    }

    @Override
    public Boolean getbit(byte[] key, long offset) {
        return doExecuteWithRedis(jedis -> jedis.getbit(key, offset));
    }

    @Override
    public Long setrange(byte[] key, long offset, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.setrange(key, offset, value));
    }

    @Override
    public byte[] getrange(byte[] key, long startOffset, long endOffset) {
        return doExecuteWithRedis(jedis -> jedis.getrange(key, startOffset, endOffset));
    }

    @Override
    public byte[] getSet(byte[] key, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.getSet(key, value));
    }

    @Override
    public Long setnx(byte[] key, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.setnx(key, value));
    }

    @Override
    public String setex(byte[] key, long seconds, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.setex(key, seconds, value));
    }

    @Override
    public String psetex(byte[] key, long milliseconds, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.psetex(key, milliseconds, value));
    }

    @Override
    public Long decrBy(byte[] key, long decrement) {
        return doExecuteWithRedis(jedis -> jedis.decrBy(key, decrement));
    }

    @Override
    public Long decr(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.decr(key));
    }

    @Override
    public Long incrBy(byte[] key, long increment) {
        return doExecuteWithRedis(jedis -> jedis.incrBy(key, increment));
    }

    @Override
    public Double incrByFloat(byte[] key, double increment) {
        return doExecuteWithRedis(jedis -> jedis.incrByFloat(key, increment));
    }

    @Override
    public Long incr(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.incr(key));
    }

    @Override
    public Long append(byte[] key, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.append(key, value));
    }

    @Override
    public byte[] substr(byte[] key, int start, int end) {
        return doExecuteWithRedis(jedis -> jedis.substr(key, start, end));
    }

    @Override
    public Long hset(byte[] key, byte[] field, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.hset(key, field, value));
    }

    @Override
    public Long hset(byte[] key, Map<byte[], byte[]> hash) {
        return doExecuteWithRedis(jedis -> jedis.hset(key, hash));
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return doExecuteWithRedis(jedis -> jedis.hget(key, field));
    }

    @Override
    public Long hsetnx(byte[] key, byte[] field, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.hsetnx(key, field, value));
    }

    @Override
    public String hmset(byte[] key, Map<byte[], byte[]> hash) {
        return doExecuteWithRedis(jedis -> jedis.hmset(key, hash));
    }

    @Override
    public List<byte[]> hmget(byte[] key, byte[]... fields) {
        return doExecuteWithRedis(jedis -> jedis.hmget(key, fields));
    }

    @Override
    public Long hincrBy(byte[] key, byte[] field, long value) {
        return doExecuteWithRedis(jedis -> jedis.hincrBy(key, field, value));
    }

    @Override
    public Double hincrByFloat(byte[] key, byte[] field, double value) {
        return doExecuteWithRedis(jedis -> jedis.hincrByFloat(key, field, value));
    }

    @Override
    public Boolean hexists(byte[] key, byte[] field) {
        return doExecuteWithRedis(jedis -> jedis.hexists(key, field));
    }

    @Override
    public Long hdel(byte[] key, byte[]... field) {
        return doExecuteWithRedis(jedis -> jedis.hdel(key, field));
    }

    @Override
    public Long hlen(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.hlen(key));
    }

    @Override
    public Set<byte[]> hkeys(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.hkeys(key));
    }

    @Override
    public List<byte[]> hvals(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.hvals(key));
    }

    @Override
    public Map<byte[], byte[]> hgetAll(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.hgetAll(key));
    }

    @Override
    public byte[] hrandfield(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.hrandfield(key));
    }

    @Override
    public List<byte[]> hrandfield(byte[] key, long count) {
        return doExecuteWithRedis(jedis -> jedis.hrandfield(key, count));
    }

    @Override
    public Map<byte[], byte[]> hrandfieldWithValues(byte[] key, long count) {
        return doExecuteWithRedis(jedis -> jedis.hrandfieldWithValues(key, count));
    }

    @Override
    public Long rpush(byte[] key, byte[]... args) {
        return doExecuteWithRedis(jedis -> jedis.rpush(key, args));
    }

    @Override
    public Long lpush(byte[] key, byte[]... args) {
        return doExecuteWithRedis(jedis -> jedis.lpush(key, args));
    }

    @Override
    public Long llen(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.llen(key));
    }

    @Override
    public List<byte[]> lrange(byte[] key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.lrange(key, start, stop));
    }

    @Override
    public String ltrim(byte[] key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.ltrim(key, start, stop));
    }

    @Override
    public byte[] lindex(byte[] key, long index) {
        return doExecuteWithRedis(jedis -> jedis.lindex(key, index));
    }

    @Override
    public String lset(byte[] key, long index, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.lset(key, index, value));
    }

    @Override
    public Long lrem(byte[] key, long count, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.lrem(key, count, value));
    }

    @Override
    public byte[] lpop(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.lpop(key));
    }

    @Override
    public List<byte[]> lpop(byte[] key, int count) {
        return doExecuteWithRedis(jedis -> jedis.lpop(key, count));
    }

    @Override
    public Long lpos(byte[] key, byte[] element) {
        return doExecuteWithRedis(jedis -> jedis.lpos(key, element));
    }

    @Override
    public Long lpos(byte[] key, byte[] element, LPosParams params) {
        return doExecuteWithRedis(jedis -> jedis.lpos(key, element, params));
    }

    @Override
    public List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
        return doExecuteWithRedis(jedis -> jedis.lpos(key, element, params, count));
    }

    @Override
    public byte[] rpop(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.rpop(key));
    }

    @Override
    public List<byte[]> rpop(byte[] key, int count) {
        return doExecuteWithRedis(jedis -> jedis.rpop(key, count));
    }

    @Override
    public Long sadd(byte[] key, byte[]... member) {
        return doExecuteWithRedis(jedis -> jedis.sadd(key, member));
    }

    @Override
    public Set<byte[]> smembers(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.smembers(key));
    }

    @Override
    public Long srem(byte[] key, byte[]... member) {
        return doExecuteWithRedis(jedis -> jedis.srem(key, member));
    }

    @Override
    public byte[] spop(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.spop(key));
    }

    @Override
    public Set<byte[]> spop(byte[] key, long count) {
        return doExecuteWithRedis(jedis -> jedis.spop(key, count));
    }

    @Override
    public Long scard(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.scard(key));
    }

    @Override
    public Boolean sismember(byte[] key, byte[] member) {
        return doExecuteWithRedis(jedis -> jedis.sismember(key, member));
    }

    @Override
    public List<Boolean> smismember(byte[] key, byte[]... members) {
        return doExecuteWithRedis(jedis -> jedis.smismember(key, members));
    }

    @Override
    public byte[] srandmember(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.srandmember(key));
    }

    @Override
    public List<byte[]> srandmember(byte[] key, int count) {
        return doExecuteWithRedis(jedis -> jedis.srandmember(key, count));
    }

    @Override
    public Long strlen(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.strlen(key));
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        return doExecuteWithRedis(jedis -> jedis.zadd(key, score, member));
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
        return doExecuteWithRedis(jedis -> jedis.zadd(key, score, member, params));
    }

    @Override
    public Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        return doExecuteWithRedis(jedis -> jedis.zadd(key, scoreMembers));
    }

    @Override
    public Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
        return doExecuteWithRedis(jedis -> jedis.zadd(key, scoreMembers, params));
    }

    @Override
    public Double zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
        return doExecuteWithRedis(jedis -> jedis.zaddIncr(key, score, member, params));
    }

    @Override
    public Set<byte[]> zrange(byte[] key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zrange(key, start, stop));
    }

    @Override
    public Long zrem(byte[] key, byte[]... members) {
        return doExecuteWithRedis(jedis -> jedis.zrem(key, members));
    }

    @Override
    public Double zincrby(byte[] key, double increment, byte[] member) {
        return doExecuteWithRedis(jedis -> jedis.zincrby(key, increment, member));
    }

    @Override
    public Double zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
        return doExecuteWithRedis(jedis -> jedis.zincrby(key, increment, member, params));
    }

    @Override
    public Long zrank(byte[] key, byte[] member) {
        return doExecuteWithRedis(jedis -> jedis.zrank(key, member));
    }

    @Override
    public Long zrevrank(byte[] key, byte[] member) {
        return doExecuteWithRedis(jedis -> jedis.zrevrank(key, member));
    }

    @Override
    public Set<byte[]> zrevrange(byte[] key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zrevrange(key, start, stop));
    }

    @Override
    public Set<Tuple> zrangeWithScores(byte[] key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zrangeWithScores(key, start, stop));
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(byte[] key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeWithScores(key, start, stop));
    }

    @Override
    public byte[] zrandmember(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.zrandmember(key));
    }

    @Override
    public Set<byte[]> zrandmember(byte[] key, long count) {
        return doExecuteWithRedis(jedis -> jedis.zrandmember(key, count));
    }

    @Override
    public Set<Tuple> zrandmemberWithScores(byte[] key, long count) {
        return doExecuteWithRedis(jedis -> jedis.zrandmemberWithScores(key, count));
    }

    @Override
    public Long zcard(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.zcard(key));
    }

    @Override
    public Double zscore(byte[] key, byte[] member) {
        return doExecuteWithRedis(jedis -> jedis.zscore(key, member));
    }

    @Override
    public List<Double> zmscore(byte[] key, byte[]... members) {
        return doExecuteWithRedis(jedis -> jedis.zmscore(key, members));
    }

    @Override
    public Tuple zpopmax(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.zpopmax(key));
    }

    @Override
    public Set<Tuple> zpopmax(byte[] key, int count) {
        return doExecuteWithRedis(jedis -> jedis.zpopmax(key, count));
    }

    @Override
    public Tuple zpopmin(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.zpopmin(key));
    }

    @Override
    public Set<Tuple> zpopmin(byte[] key, int count) {
        return doExecuteWithRedis(jedis -> jedis.zpopmin(key, count));
    }

    @Override
    public List<byte[]> sort(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.sort(key));
    }

    @Override
    public List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
        return doExecuteWithRedis(jedis -> jedis.sort(key, sortingParameters));
    }

    @Override
    public Long zcount(byte[] key, double min, double max) {
        return doExecuteWithRedis(jedis -> jedis.zcount(key, min, max));
    }

    @Override
    public Long zcount(byte[] key, byte[] min, byte[] max) {
        return doExecuteWithRedis(jedis -> jedis.zcount(key, min, max));
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScore(key, min, max));
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScore(key, min, max));
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScore(key, max, min));
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScore(key, min, max, offset, count));
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScore(key, max, min));
    }

    @Override
    public Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScore(key, min, max, offset, count));
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScore(key, max, min, offset, count));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScoreWithScores(key, min, max));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScoreWithScores(key, min, max, offset, count));
    }

    @Override
    public Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScore(key, max, min, offset, count));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScoreWithScores(key, min, max));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScoreWithScores(key, min, max, offset, count));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count));
    }

    @Override
    public Long zremrangeByRank(byte[] key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zremrangeByRank(key, start, stop));
    }

    @Override
    public Long zremrangeByScore(byte[] key, double min, double max) {
        return doExecuteWithRedis(jedis -> jedis.zremrangeByScore(key, min, max));
    }

    @Override
    public Long zremrangeByScore(byte[] key, byte[] min, byte[] max) {
        return doExecuteWithRedis(jedis -> jedis.zremrangeByScore(key, min, max));
    }

    @Override
    public Long zlexcount(byte[] key, byte[] min, byte[] max) {
        return doExecuteWithRedis(jedis -> jedis.zlexcount(key, min, max));
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByLex(key, min, max));
    }

    @Override
    public Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByLex(key, min, max, offset, count));
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByLex(key, max, min));
    }

    @Override
    public Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByLex(key, max, min, offset, count));
    }

    @Override
    public Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
        return doExecuteWithRedis(jedis -> jedis.zremrangeByLex(key, min, max));
    }

    @Override
    public Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.linsert(key, where, pivot, value));
    }

    @Override
    public Long lpushx(byte[] key, byte[]... arg) {
        return doExecuteWithRedis(jedis -> jedis.lpushx(key, arg));
    }

    @Override
    public Long rpushx(byte[] key, byte[]... arg) {
        return doExecuteWithRedis(jedis -> jedis.rpushx(key, arg));
    }

    @Override
    public Long del(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.del(key));
    }

    @Override
    public Long unlink(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.unlink(key));
    }

    @Override
    public byte[] echo(byte[] arg) {
        return doExecuteWithRedis(jedis -> jedis.echo(arg));
    }

    @Override
    public Long move(byte[] key, int dbIndex) {
        return doExecuteWithRedis(jedis -> jedis.move(key, dbIndex));
    }

    @Override
    public Long bitcount(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.bitcount(key));
    }

    @Override
    public Long bitcount(byte[] key, long start, long end) {
        return doExecuteWithRedis(jedis -> jedis.bitcount(key, start, end));
    }

    @Override
    public Long pfadd(byte[] key, byte[]... elements) {
        return doExecuteWithRedis(jedis -> jedis.pfadd(key, elements));
    }

    @Override
    public long pfcount(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.pfcount(key));
    }

    @Override
    public Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        return doExecuteWithRedis(jedis -> jedis.geoadd(key, longitude, latitude, member));
    }

    @Override
    public Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return doExecuteWithRedis(jedis -> jedis.geoadd(key, memberCoordinateMap));
    }

    @Override
    public Long geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        return doExecuteWithRedis(jedis -> jedis.geoadd(key, params, memberCoordinateMap));
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2) {
        return doExecuteWithRedis(jedis -> jedis.geodist(key, member1, member2));
    }

    @Override
    public Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.geodist(key, member1, member2, unit));
    }

    @Override
    public List<byte[]> geohash(byte[] key, byte[]... members) {
        return doExecuteWithRedis(jedis -> jedis.geohash(key, members));
    }

    @Override
    public List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
        return doExecuteWithRedis(jedis -> jedis.geopos(key, members));
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.georadius(key, longitude, latitude, radius, unit));
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.georadiusReadonly(key, longitude, latitude, radius, unit));
    }

    @Override
    public List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return doExecuteWithRedis(jedis -> jedis.georadius(key, longitude, latitude, radius, unit, param));
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return doExecuteWithRedis(jedis -> jedis.georadiusReadonly(key, longitude, latitude, radius, unit, param));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMember(key, member, radius, unit));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMemberReadonly(key, member, radius, unit));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMember(key, member, radius, unit, param));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMemberReadonly(key, member, radius, unit, param));
    }

    @Override
    public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
        return doExecuteWithRedis(jedis -> jedis.hscan(key, cursor));
    }

    @Override
    public ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
        return doExecuteWithRedis(jedis -> jedis.hscan(key, cursor, params));
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
        return doExecuteWithRedis(jedis -> jedis.sscan(key, cursor));
    }

    @Override
    public ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
        return doExecuteWithRedis(jedis -> jedis.sscan(key, cursor, params));
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
        return doExecuteWithRedis(jedis -> jedis.zscan(key, cursor));
    }

    @Override
    public ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
        return doExecuteWithRedis(jedis -> jedis.zscan(key, cursor, params));
    }

    @Override
    public List<Long> bitfield(byte[] key, byte[]... arguments) {
        return doExecuteWithRedis(jedis -> jedis.bitfield(key, arguments));
    }

    @Override
    public List<Long> bitfieldReadonly(byte[] key, byte[]... arguments) {
        return doExecuteWithRedis(jedis -> jedis.bitfieldReadonly(key, arguments));
    }

    @Override
    public Long hstrlen(byte[] key, byte[] field) {
        return doExecuteWithRedis(jedis -> jedis.hstrlen(key, field));
    }

    @Override
    public byte[] xadd(byte[] key, byte[] id, Map<byte[], byte[]> hash, long maxLen, boolean approximateLength) {
        return doExecuteWithRedis(jedis -> jedis.xadd(key, id, hash, maxLen, approximateLength));
    }

    @Override
    public byte[] xadd(byte[] key, Map<byte[], byte[]> hash, XAddParams params) {
        return doExecuteWithRedis(jedis -> jedis.xadd(key, hash, params));
    }

    @Override
    public Long xlen(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.xlen(key));
    }

    @Override
    public List<byte[]> xrange(byte[] key, byte[] start, byte[] end) {
        return doExecuteWithRedis(jedis -> jedis.xrange(key, start, end));
    }

    @Override
    public List<byte[]> xrange(byte[] key, byte[] start, byte[] end, int count) {
        return doExecuteWithRedis(jedis -> jedis.xrange(key, start, end, count));
    }

    @Override
    public List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start) {
        return doExecuteWithRedis(jedis -> jedis.xrevrange(key, end, start));
    }

    @Override
    public List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
        return doExecuteWithRedis(jedis -> jedis.xrevrange(key, end, start, count));
    }

    @Override
    public Long xack(byte[] key, byte[] group, byte[]... ids) {
        return doExecuteWithRedis(jedis -> jedis.xack(key, group, ids));
    }

    @Override
    public String xgroupCreate(byte[] key, byte[] consumer, byte[] id, boolean makeStream) {
        return doExecuteWithRedis(jedis -> jedis.xgroupCreate(key, consumer, id, makeStream));
    }

    @Override
    public String xgroupSetID(byte[] key, byte[] consumer, byte[] id) {
        return doExecuteWithRedis(jedis -> jedis.xgroupSetID(key, consumer, id));
    }

    @Override
    public Long xgroupDestroy(byte[] key, byte[] consumer) {
        return doExecuteWithRedis(jedis -> jedis.xgroupDestroy(key, consumer));
    }

    @Override
    public Long xgroupDelConsumer(byte[] key, byte[] consumer, byte[] consumerName) {
        return doExecuteWithRedis(jedis -> jedis.xgroupDelConsumer(key, consumer, consumerName));
    }

    @Override
    public Long xdel(byte[] key, byte[]... ids) {
        return doExecuteWithRedis(jedis -> jedis.xdel(key, ids));
    }

    @Override
    public Long xtrim(byte[] key, long maxLen, boolean approximateLength) {
        return doExecuteWithRedis(jedis -> jedis.xtrim(key, maxLen, approximateLength));
    }

    @Override
    public Long xtrim(byte[] key, XTrimParams params) {
        return doExecuteWithRedis(jedis -> jedis.xtrim(key, params));
    }

    @Override
    public Object xpending(byte[] key, byte[] groupname) {
        return doExecuteWithRedis(jedis -> jedis.xpending(key, groupname));
    }

    @Override
    public List<Object> xpending(byte[] key, byte[] groupname, byte[] start, byte[] end, int count, byte[] consumername) {
        return doExecuteWithRedis(jedis -> jedis.xpending(key, groupname, start, end, count, consumername));
    }

    @Override
    public List<Object> xpending(byte[] key, byte[] groupname, XPendingParams params) {
        return doExecuteWithRedis(jedis -> jedis.xpending(key, groupname, params));
    }

    public List<byte[]> xclaim(byte[] key, byte[] groupname, byte[] consumername, long minIdleTime, long newIdleTime, int retries,
            boolean force, byte[]... ids) {
        return doExecuteWithRedis(
                jedis -> jedis.xclaim(key, groupname, consumername, minIdleTime, newIdleTime, retries, force, ids));
    }

    // For compatibility and adaptation
    @Override
    public List<byte[]> xclaim$JedisCommands(byte[] key, byte[] groupname, byte[] consumername, long minIdleTime,
            long newIdleTime, int retries, boolean force, byte[]... ids) {
        return xclaim(key, groupname, consumername, minIdleTime, newIdleTime, retries, force, ids);
    }

    @Override
    public List<byte[]> xclaim(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params,
            byte[]... ids) {
        return doExecuteWithRedis(jedis -> jedis.xclaim(key, group, consumername, minIdleTime, params, ids));
    }

    @Override
    public List<byte[]> xclaimJustId(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params,
            byte[]... ids) {
        return doExecuteWithRedis(jedis -> jedis.xclaimJustId(key, group, consumername, minIdleTime, params, ids));
    }

    @Override
    public StreamInfo xinfoStream(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.xinfoStream(key));
    }

    @Override
    public Object xinfoStreamBinary(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.xinfoStreamBinary(key));
    }

    @Override
    public List<StreamGroupInfo> xinfoGroup(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.xinfoGroup(key));
    }

    @Override
    public List<Object> xinfoGroupBinary(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.xinfoGroupBinary(key));
    }

    @Override
    public List<StreamConsumersInfo> xinfoConsumers(byte[] key, byte[] group) {
        return doExecuteWithRedis(jedis -> jedis.xinfoConsumers(key, group));
    }

    @Override
    public List<Object> xinfoConsumersBinary(byte[] key, byte[] group) {
        return doExecuteWithRedis(jedis -> jedis.xinfoConsumersBinary(key, group));
    }

    @Override
    public List<byte[]> configGet(byte[] pattern) {
        return doExecuteWithRedis(jedis -> jedis.configGet(pattern));
    }

    @Override
    public byte[] configSet(byte[] parameter, byte[] value) {
        return doExecuteWithRedis(jedis -> jedis.configSet(parameter, value));
    }

    @Override
    public List<Object> slowlogGetBinary() {
        return doExecuteWithRedis(jedis -> jedis.slowlogGetBinary());
    }

    @Override
    public List<Object> slowlogGetBinary(long entries) {
        return doExecuteWithRedis(jedis -> jedis.slowlogGetBinary(entries));
    }

    @Override
    public Long objectRefcount(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.objectRefcount(key));
    }

    @Override
    public byte[] objectEncoding(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.objectEncoding(key));
    }

    @Override
    public Long objectIdletime(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.objectIdletime(key));
    }

    @Override
    public List<byte[]> objectHelpBinary() {
        return doExecuteWithRedis(jedis -> jedis.objectHelpBinary());
    }

    @Override
    public Long objectFreq(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.objectFreq(key));
    }

    @Override
    public String migrate(String host, int port, byte[] key, int destinationDB, int timeout) {
        return doExecuteWithRedis(jedis -> jedis.migrate(host, port, key, destinationDB, timeout));
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.migrate(host, port, destinationDB, timeout, params, keys));
    }

    @Override
    public String clientKill(byte[] ipPort) {
        return doExecuteWithRedis(jedis -> jedis.clientKill(ipPort));
    }

    @Override
    public byte[] clientGetnameBinary() {
        return doExecuteWithRedis(jedis -> jedis.clientGetnameBinary());
    }

    @Override
    public byte[] clientListBinary() {
        return doExecuteWithRedis(jedis -> jedis.clientListBinary());
    }

    @Override
    public byte[] clientListBinary(long... clientIds) {
        return doExecuteWithRedis(jedis -> jedis.clientListBinary(clientIds));
    }

    @Override
    public byte[] clientInfoBinary() {
        return doExecuteWithRedis(jedis -> jedis.clientInfoBinary());
    }

    @Override
    public String clientSetname(byte[] name) {
        return doExecuteWithRedis(jedis -> jedis.clientSetname(name));
    }

    @Override
    public byte[] memoryDoctorBinary() {
        return doExecuteWithRedis(jedis -> jedis.memoryDoctorBinary());
    }

    @Override
    public Long memoryUsage(byte[] key) {
        return doExecuteWithRedis(jedis -> jedis.memoryUsage(key));
    }

    @Override
    public Long memoryUsage(byte[] key, int samples) {
        return doExecuteWithRedis(jedis -> jedis.memoryUsage(key, samples));
    }

    @Override
    public byte[] aclWhoAmIBinary() {
        return doExecuteWithRedis(jedis -> jedis.aclWhoAmIBinary());
    }

    @Override
    public byte[] aclGenPassBinary() {
        return doExecuteWithRedis(jedis -> jedis.aclGenPassBinary());
    }

    @Override
    public List<byte[]> aclListBinary() {
        return doExecuteWithRedis(jedis -> jedis.aclListBinary());
    }

    @Override
    public List<byte[]> aclUsersBinary() {
        return doExecuteWithRedis(jedis -> jedis.aclUsersBinary());
    }

    @Override
    public AccessControlUser aclGetUser(byte[] name) {
        return doExecuteWithRedis(jedis -> jedis.aclGetUser(name));
    }

    @Override
    public String aclSetUser(byte[] name) {
        return doExecuteWithRedis(jedis -> jedis.aclSetUser(name));
    }

    @Override
    public String aclSetUser(byte[] name, byte[]... keys) {
        return doExecuteWithRedis(jedis -> jedis.aclSetUser(name, keys));
    }

    @Override
    public Long aclDelUser(byte[] name) {
        return doExecuteWithRedis(jedis -> jedis.aclDelUser(name));
    }

    @Override
    public List<byte[]> aclCatBinary() {
        return doExecuteWithRedis(jedis -> jedis.aclCatBinary());
    }

    @Override
    public List<byte[]> aclCat(byte[] category) {
        return doExecuteWithRedis(jedis -> jedis.aclCat(category));
    }

    @Override
    public List<byte[]> aclLogBinary() {
        return doExecuteWithRedis(jedis -> jedis.aclLogBinary());
    }

    @Override
    public List<byte[]> aclLogBinary(int limit) {
        return doExecuteWithRedis(jedis -> jedis.aclLogBinary(limit));
    }

    @Override
    public byte[] aclLog(byte[] options) {
        return doExecuteWithRedis(jedis -> jedis.aclLog(options));
    }

    @Override
    public String moduleLoad(String path) {
        return doExecuteWithRedis(jedis -> jedis.moduleLoad(path));
    }

    @Override
    public String moduleUnload(String name) {
        return doExecuteWithRedis(jedis -> jedis.moduleUnload(name));
    }

    @Override
    public List<Module> moduleList() {
        return doExecuteWithRedis(jedis -> jedis.moduleList());
    }

    @Override
    public List<Map<String, String>> sentinelMasters() {
        return doExecuteWithRedis(jedis -> jedis.sentinelMasters());
    }

    @Override
    public List<String> sentinelGetMasterAddrByName(String masterName) {
        return doExecuteWithRedis(jedis -> jedis.sentinelGetMasterAddrByName(masterName));
    }

    @Override
    public Long sentinelReset(String pattern) {
        return doExecuteWithRedis(jedis -> jedis.sentinelReset(pattern));
    }

    @Override
    public List<Map<String, String>> sentinelSlaves(String masterName) {
        return doExecuteWithRedis(jedis -> jedis.sentinelSlaves(masterName));
    }

    @Override
    public String sentinelFailover(String masterName) {
        return doExecuteWithRedis(jedis -> jedis.sentinelFailover(masterName));
    }

    @Override
    public String sentinelMonitor(String masterName, String ip, int port, int quorum) {
        return doExecuteWithRedis(jedis -> jedis.sentinelMonitor(masterName, ip, port, quorum));
    }

    @Override
    public String sentinelRemove(String masterName) {
        return doExecuteWithRedis(jedis -> jedis.sentinelRemove(masterName));
    }

    @Override
    public String sentinelSet(String masterName, Map<String, String> parameterMap) {
        return doExecuteWithRedis(jedis -> jedis.sentinelSet(masterName, parameterMap));
    }

    @Override
    public String clusterNodes() {
        return doExecuteWithRedis(jedis -> jedis.clusterNodes());
    }

    @Override
    public String clusterMeet(String ip, int port) {
        return doExecuteWithRedis(jedis -> jedis.clusterMeet(ip, port));
    }

    @Override
    public String clusterAddSlots(int... slots) {
        return doExecuteWithRedis(jedis -> jedis.clusterAddSlots(slots));
    }

    @Override
    public String clusterDelSlots(int... slots) {
        return doExecuteWithRedis(jedis -> jedis.clusterDelSlots(slots));
    }

    @Override
    public String clusterInfo() {
        return doExecuteWithRedis(jedis -> jedis.clusterInfo());
    }

    @Override
    public List<String> clusterGetKeysInSlot(int slot, int count) {
        return doExecuteWithRedis(jedis -> jedis.clusterGetKeysInSlot(slot, count));
    }

    @Override
    public String clusterSetSlotNode(int slot, String nodeId) {
        return doExecuteWithRedis(jedis -> jedis.clusterSetSlotNode(slot, nodeId));
    }

    @Override
    public String clusterSetSlotMigrating(int slot, String nodeId) {
        return doExecuteWithRedis(jedis -> jedis.clusterSetSlotMigrating(slot, nodeId));
    }

    @Override
    public String clusterSetSlotImporting(int slot, String nodeId) {
        return doExecuteWithRedis(jedis -> jedis.clusterSetSlotImporting(slot, nodeId));
    }

    @Override
    public String clusterSetSlotStable(int slot) {
        return doExecuteWithRedis(jedis -> jedis.clusterSetSlotStable(slot));
    }

    @Override
    public String clusterForget(String nodeId) {
        return doExecuteWithRedis(jedis -> jedis.clusterForget(nodeId));
    }

    @Override
    public String clusterFlushSlots() {
        return doExecuteWithRedis(jedis -> jedis.clusterFlushSlots());
    }

    @Override
    public Long clusterKeySlot(String key) {
        return doExecuteWithRedis(jedis -> jedis.clusterKeySlot(key));
    }

    @Override
    public Long clusterCountKeysInSlot(int slot) {
        return doExecuteWithRedis(jedis -> jedis.clusterCountKeysInSlot(slot));
    }

    @Override
    public String clusterSaveConfig() {
        return doExecuteWithRedis(jedis -> jedis.clusterSaveConfig());
    }

    @Override
    public String clusterReplicate(String nodeId) {
        return doExecuteWithRedis(jedis -> jedis.clusterReplicate(nodeId));
    }

    @Override
    public List<String> clusterSlaves(String nodeId) {
        return doExecuteWithRedis(jedis -> jedis.clusterSlaves(nodeId));
    }

    @Override
    public String clusterFailover() {
        return doExecuteWithRedis(jedis -> jedis.clusterFailover());
    }

    @Override
    public List<Object> clusterSlots() {
        return doExecuteWithRedis(jedis -> jedis.clusterSlots());
    }

    @Override
    public String clusterReset(ClusterReset resetType) {
        return doExecuteWithRedis(jedis -> jedis.clusterReset(resetType));
    }

    @Override
    public String readonly() {
        return doExecuteWithRedis(jedis -> jedis.readonly());
    }

    @Override
    public String ping() {
        return doExecuteWithRedis(jedis -> jedis.ping());
    }

    @Override
    public String quit() {
        return doExecuteWithRedis(jedis -> jedis.quit());
    }

    @Override
    public String flushDB() {
        return doExecuteWithRedis(jedis -> jedis.flushDB());
    }

    @Override
    public String flushDB(FlushMode flushMode) {
        return doExecuteWithRedis(jedis -> jedis.flushDB(flushMode));
    }

    @Override
    public Long dbSize() {
        return doExecuteWithRedis(jedis -> jedis.dbSize());
    }

    @Override
    public String select(int index) {
        return doExecuteWithRedis(jedis -> jedis.select(index));
    }

    @Override
    public String swapDB(int index1, int index2) {
        return doExecuteWithRedis(jedis -> jedis.swapDB(index1, index2));
    }

    @Override
    public String flushAll() {
        return doExecuteWithRedis(jedis -> jedis.flushAll());
    }

    @Override
    public String flushAll(FlushMode flushMode) {
        return doExecuteWithRedis(jedis -> jedis.flushAll(flushMode));
    }

    @Override
    public String auth(String password) {
        return doExecuteWithRedis(jedis -> jedis.auth(password));
    }

    @Override
    public String auth(String user, String password) {
        return doExecuteWithRedis(jedis -> jedis.auth(user, password));
    }

    @Override
    public String save() {
        return doExecuteWithRedis(jedis -> jedis.save());
    }

    @Override
    public String bgsave() {
        return doExecuteWithRedis(jedis -> jedis.bgsave());
    }

    @Override
    public String bgrewriteaof() {
        return doExecuteWithRedis(jedis -> jedis.bgrewriteaof());
    }

    @Override
    public Long lastsave() {
        return doExecuteWithRedis(jedis -> jedis.lastsave());
    }

    @Override
    public String shutdown() {
        return doExecuteWithRedis(jedis -> jedis.shutdown());
    }

    @Override
    public String info() {
        return doExecuteWithRedis(jedis -> jedis.info());
    }

    @Override
    public String info(String section) {
        return doExecuteWithRedis(jedis -> jedis.info(section));
    }

    @Override
    public String slaveof(String host, int port) {
        return doExecuteWithRedis(jedis -> jedis.slaveof(host, port));
    }

    @Override
    public String slaveofNoOne() {
        return doExecuteWithRedis(jedis -> jedis.slaveofNoOne());
    }

    @Override
    public int getDB() {
        return doExecuteWithRedis(jedis -> jedis.getDB());
    }

    @Override
    public String debug(DebugParams params) {
        return doExecuteWithRedis(jedis -> jedis.debug(params));
    }

    @Override
    public String configResetStat() {
        return doExecuteWithRedis(jedis -> jedis.configResetStat());
    }

    @Override
    public String configRewrite() {
        return doExecuteWithRedis(jedis -> jedis.configRewrite());
    }

    @Override
    public Long waitReplicas(int replicas, long timeout) {
        return doExecuteWithRedis(jedis -> jedis.waitReplicas(replicas, timeout));
    }

    @Override
    public Object eval(String script, int keyCount, String... params) {
        return doExecuteWithRedis(jedis -> jedis.eval(script, keyCount, params));
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return doExecuteWithRedis(jedis -> jedis.eval(script, keys, args));
    }

    @Override
    public Object eval(String script) {
        return doExecuteWithRedis(jedis -> jedis.eval(script));
    }

    @Override
    public Object evalsha(String sha1) {
        return doExecuteWithRedis(jedis -> jedis.evalsha(sha1));
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return doExecuteWithRedis(jedis -> jedis.evalsha(sha1, keys, args));
    }

    @Override
    public Object evalsha(String sha1, int keyCount, String... params) {
        return doExecuteWithRedis(jedis -> jedis.evalsha(sha1, keyCount, params));
    }

    @Override
    public Boolean scriptExists(String sha1) {
        return doExecuteWithRedis(jedis -> jedis.scriptExists(sha1));
    }

    @Override
    public List<Boolean> scriptExists(String... sha1) {
        return doExecuteWithRedis(jedis -> jedis.scriptExists(sha1));
    }

    @Override
    public String scriptLoad(String script) {
        return doExecuteWithRedis(jedis -> jedis.scriptLoad(script));
    }

    @Override
    public List<String> configGet(String pattern) {
        return doExecuteWithRedis(jedis -> jedis.configGet(pattern));
    }

    @Override
    public String configSet(String parameter, String value) {
        return doExecuteWithRedis(jedis -> jedis.configSet(parameter, value));
    }

    @Override
    public String slowlogReset() {
        return doExecuteWithRedis(jedis -> jedis.slowlogReset());
    }

    @Override
    public Long slowlogLen() {
        return doExecuteWithRedis(jedis -> jedis.slowlogLen());
    }

    @Override
    public List<Slowlog> slowlogGet() {
        return doExecuteWithRedis(jedis -> jedis.slowlogGet());
    }

    @Override
    public List<Slowlog> slowlogGet(long entries) {
        return doExecuteWithRedis(jedis -> jedis.slowlogGet(entries));
    }

    @Override
    public Long objectRefcount(String key) {
        return doExecuteWithRedis(jedis -> jedis.objectRefcount(key));
    }

    @Override
    public String objectEncoding(String key) {
        return doExecuteWithRedis(jedis -> jedis.objectEncoding(key));
    }

    @Override
    public Long objectIdletime(String key) {
        return doExecuteWithRedis(jedis -> jedis.objectIdletime(key));
    }

    @Override
    public List<String> objectHelp() {
        return doExecuteWithRedis(jedis -> jedis.objectHelp());
    }

    @Override
    public Long objectFreq(String key) {
        return doExecuteWithRedis(jedis -> jedis.objectFreq(key));
    }

    @Override
    public String migrate(String host, int port, String key, int destinationDB, int timeout) {
        return doExecuteWithRedis(jedis -> jedis.migrate(host, port, key, destinationDB, timeout));
    }

    @Override
    public String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.migrate(host, port, destinationDB, timeout, params, keys));
    }

    @Override
    public String clientKill(String ipPort) {
        return doExecuteWithRedis(jedis -> jedis.clientKill(ipPort));
    }

    @Override
    public String clientKill(String ip, int port) {
        return doExecuteWithRedis(jedis -> jedis.clientKill(ip, port));
    }

    @Override
    public Long clientKill(ClientKillParams params) {
        return doExecuteWithRedis(jedis -> jedis.clientKill(params));
    }

    @Override
    public String clientGetname() {
        return doExecuteWithRedis(jedis -> jedis.clientGetname());
    }

    @Override
    public String clientList() {
        return doExecuteWithRedis(jedis -> jedis.clientList());
    }

    @Override
    public String clientList(long... clientIds) {
        return doExecuteWithRedis(jedis -> jedis.clientList(clientIds));
    }

    @Override
    public String clientInfo() {
        return doExecuteWithRedis(jedis -> jedis.clientInfo());
    }

    @Override
    public String clientSetname(String name) {
        return doExecuteWithRedis(jedis -> jedis.clientSetname(name));
    }

    @Override
    public Long clientId() {
        return doExecuteWithRedis(jedis -> jedis.clientId());
    }

    @Override
    public Long clientUnblock(long clientId, UnblockType unblockType) {
        return doExecuteWithRedis(jedis -> jedis.clientUnblock(clientId, unblockType));
    }

    @Override
    public String memoryDoctor() {
        return doExecuteWithRedis(jedis -> jedis.memoryDoctor());
    }

    @Override
    public Long memoryUsage(String key) {
        return doExecuteWithRedis(jedis -> jedis.memoryUsage(key));
    }

    @Override
    public Long memoryUsage(String key, int samples) {
        return doExecuteWithRedis(jedis -> jedis.memoryUsage(key, samples));
    }

    @Override
    public String aclWhoAmI() {
        return doExecuteWithRedis(jedis -> jedis.aclWhoAmI());
    }

    @Override
    public String aclGenPass() {
        return doExecuteWithRedis(jedis -> jedis.aclGenPass());
    }

    @Override
    public List<String> aclList() {
        return doExecuteWithRedis(jedis -> jedis.aclList());
    }

    @Override
    public List<String> aclUsers() {
        return doExecuteWithRedis(jedis -> jedis.aclUsers());
    }

    @Override
    public AccessControlUser aclGetUser(String name) {
        return doExecuteWithRedis(jedis -> jedis.aclGetUser(name));
    }

    @Override
    public String aclSetUser(String name) {
        return doExecuteWithRedis(jedis -> jedis.aclSetUser(name));
    }

    @Override
    public String aclSetUser(String name, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.aclSetUser(name, keys));
    }

    @Override
    public Long aclDelUser(String name) {
        return doExecuteWithRedis(jedis -> jedis.aclDelUser(name));
    }

    @Override
    public List<String> aclCat() {
        return doExecuteWithRedis(jedis -> jedis.aclCat());
    }

    @Override
    public List<String> aclCat(String category) {
        return doExecuteWithRedis(jedis -> jedis.aclCat(category));
    }

    @Override
    public List<AccessControlLogEntry> aclLog() {
        return doExecuteWithRedis(jedis -> jedis.aclLog());
    }

    @Override
    public List<AccessControlLogEntry> aclLog(int limit) {
        return doExecuteWithRedis(jedis -> jedis.aclLog(limit));
    }

    @Override
    public String aclLog(String options) {
        return doExecuteWithRedis(jedis -> jedis.aclLog(options));
    }

    @Override
    public String aclLoad() {
        return doExecuteWithRedis(jedis -> jedis.aclLoad());
    }

    @Override
    public String aclSave() {
        return doExecuteWithRedis(jedis -> jedis.aclSave());
    }

    @Override
    public Boolean copy(String srcKey, String dstKey, int db, boolean replace) {
        return doExecuteWithRedis(jedis -> jedis.copy(srcKey, dstKey, db, replace));
    }

    @Override
    public Boolean copy(String srcKey, String dstKey, boolean replace) {
        return doExecuteWithRedis(jedis -> jedis.copy(srcKey, dstKey, replace));
    }

    @Override
    public Long del(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.del(keys));
    }

    @Override
    public Long unlink(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.unlink(keys));
    }

    @Override
    public Long exists(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.exists(keys));
    }

    @Override
    public String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
        return doExecuteWithRedis(jedis -> jedis.lmove(srcKey, dstKey, from, to));
    }

    @Override
    public String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
        return doExecuteWithRedis(jedis -> jedis.blmove(srcKey, dstKey, from, to, timeout));
    }

    @Override
    public List<String> blpop(int timeout, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.blpop(timeout, keys));
    }

    @Override
    public KeyedListElement blpop(double timeout, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.blpop(timeout, keys));
    }

    @Override
    public List<String> brpop(int timeout, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.brpop(timeout, keys));
    }

    @Override
    public KeyedListElement brpop(double timeout, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.brpop(timeout, keys));
    }

    @Override
    public List<String> blpop(String... args) {
        return doExecuteWithRedis(jedis -> jedis.blpop(args));
    }

    @Override
    public List<String> brpop(String... args) {
        return doExecuteWithRedis(jedis -> jedis.brpop(args));
    }

    @Override
    public KeyedZSetElement bzpopmax(double timeout, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.bzpopmax(timeout, keys));
    }

    @Override
    public KeyedZSetElement bzpopmin(double timeout, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.bzpopmin(timeout, keys));
    }

    @Override
    public Set<String> keys(String pattern) {
        return doExecuteWithRedis(jedis -> jedis.keys(pattern));
    }

    @Override
    public List<String> mget(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.mget(keys));
    }

    @Override
    public String mset(String... keysvalues) {
        return doExecuteWithRedis(jedis -> jedis.mset(keysvalues));
    }

    @Override
    public Long msetnx(String... keysvalues) {
        return doExecuteWithRedis(jedis -> jedis.msetnx(keysvalues));
    }

    @Override
    public String rename(String oldkey, String newkey) {
        return doExecuteWithRedis(jedis -> jedis.rename(oldkey, newkey));
    }

    @Override
    public Long renamenx(String oldkey, String newkey) {
        return doExecuteWithRedis(jedis -> jedis.renamenx(oldkey, newkey));
    }

    @Override
    public String rpoplpush(String srckey, String dstkey) {
        return doExecuteWithRedis(jedis -> jedis.rpoplpush(srckey, dstkey));
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.sdiff(keys));
    }

    @Override
    public Long sdiffstore(String dstkey, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.sdiffstore(dstkey, keys));
    }

    @Override
    public Set<String> sinter(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.sinter(keys));
    }

    @Override
    public Long sinterstore(String dstkey, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.sinterstore(dstkey, keys));
    }

    @Override
    public Long smove(String srckey, String dstkey, String member) {
        return doExecuteWithRedis(jedis -> jedis.smove(srckey, dstkey, member));
    }

    @Override
    public Long sort(String key, SortingParams sortingParameters, String dstkey) {
        return doExecuteWithRedis(jedis -> jedis.sort(key, sortingParameters, dstkey));
    }

    @Override
    public Long sort(String key, String dstkey) {
        return doExecuteWithRedis(jedis -> jedis.sort(key, dstkey));
    }

    @Override
    public Set<String> sunion(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.sunion(keys));
    }

    @Override
    public Long sunionstore(String dstkey, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.sunionstore(dstkey, keys));
    }

    @Override
    public String watch(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.watch(keys));
    }

    @Override
    public String unwatch() {
        return doExecuteWithRedis(jedis -> jedis.unwatch());
    }

    @Override
    public Set<String> zdiff(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.zdiff(keys));
    }

    @Override
    public Set<Tuple> zdiffWithScores(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.zdiffWithScores(keys));
    }

    @Override
    public Long zdiffStore(String dstkey, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.zdiffStore(dstkey, keys));
    }

    @Override
    public Long zinterstore(String dstkey, String... sets) {
        return doExecuteWithRedis(jedis -> jedis.zinterstore(dstkey, sets));
    }

    @Override
    public Long zinterstore(String dstkey, ZParams params, String... sets) {
        return doExecuteWithRedis(jedis -> jedis.zinterstore(dstkey, params, sets));
    }

    @Override
    public Set<String> zinter(ZParams params, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.zinter(params, keys));
    }

    @Override
    public Set<Tuple> zinterWithScores(ZParams params, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.zinterWithScores(params, keys));
    }

    @Override
    public Set<String> zunion(ZParams params, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.zunion(params, keys));
    }

    @Override
    public Set<Tuple> zunionWithScores(ZParams params, String... keys) {
        return doExecuteWithRedis(jedis -> jedis.zunionWithScores(params, keys));
    }

    @Override
    public Long zunionstore(String dstkey, String... sets) {
        return doExecuteWithRedis(jedis -> jedis.zunionstore(dstkey, sets));
    }

    @Override
    public Long zunionstore(String dstkey, ZParams params, String... sets) {
        return doExecuteWithRedis(jedis -> jedis.zunionstore(dstkey, params, sets));
    }

    @Override
    public String brpoplpush(String source, String destination, int timeout) {
        return doExecuteWithRedis(jedis -> jedis.brpoplpush(source, destination, timeout));
    }

    @Override
    public Long publish(String channel, String message) {
        return doExecuteWithRedis(jedis -> jedis.publish(channel, message));
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        doExecuteWithRedis(jedis -> {
            jedis.subscribe(jedisPubSub, channels);
            return null;
        });
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        doExecuteWithRedis(jedis -> {
            jedis.psubscribe(jedisPubSub, patterns);
            return null;
        });
    }

    @Override
    public String randomKey() {
        return doExecuteWithRedis(jedis -> jedis.randomKey());
    }

    @Override
    public Long bitop(BitOP op, String destKey, String... srcKeys) {
        return doExecuteWithRedis(jedis -> jedis.bitop(op, destKey, srcKeys));
    }

    @Override
    public ScanResult<String> scan(String cursor) {
        return doExecuteWithRedis(jedis -> jedis.scan(cursor));
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params) {
        return doExecuteWithRedis(jedis -> jedis.scan(cursor, params));
    }

    @Override
    public String pfmerge(String destkey, String... sourcekeys) {
        return doExecuteWithRedis(jedis -> jedis.pfmerge(destkey, sourcekeys));
    }

    @Override
    public long pfcount(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.pfcount(keys));
    }

    @Override
    public Long touch(String... keys) {
        return doExecuteWithRedis(jedis -> jedis.touch(keys));
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xread(int count, long block, Entry<String, StreamEntryID>... streams) {
        return doExecuteWithRedis(jedis -> jedis.xread(count, block, streams));
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xread(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
        return doExecuteWithRedis(jedis -> jedis.xread(xReadParams, streams));
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, int count, long block,
            boolean noAck, Entry<String, StreamEntryID>... streams) {
        return doExecuteWithRedis(jedis -> jedis.xreadGroup(groupname, consumer, count, block, noAck, streams));
    }

    @Override
    public List<Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, XReadGroupParams xReadGroupParams,
            Map<String, StreamEntryID> streams) {
        return doExecuteWithRedis(jedis -> jedis.xreadGroup(groupname, consumer, xReadGroupParams, streams));
    }

    @Override
    public Long georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param,
            GeoRadiusStoreParam storeParam) {
        return doExecuteWithRedis(jedis -> jedis.georadiusStore(key, longitude, latitude, radius, unit, param, storeParam));
    }

    @Override
    public Long georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param,
            GeoRadiusStoreParam storeParam) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMemberStore(key, member, radius, unit, param, storeParam));
    }

    @Override
    public String set(String key, String value) {
        return doExecuteWithRedis(jedis -> jedis.set(key, value));
    }

    @Override
    public String set(String key, String value, SetParams params) {
        return doExecuteWithRedis(jedis -> jedis.set(key, value, params));
    }

    @Override
    public String get(String key) {
        return doExecuteWithRedis(jedis -> jedis.get(key));
    }

    @Override
    public String getDel(String key) {
        return doExecuteWithRedis(jedis -> jedis.getDel(key));
    }

    @Override
    public String getEx(String key, GetExParams params) {
        return doExecuteWithRedis(jedis -> jedis.getEx(key, params));
    }

    @Override
    public Boolean exists(String key) {
        return doExecuteWithRedis(jedis -> jedis.exists(key));
    }

    @Override
    public Long persist(String key) {
        return doExecuteWithRedis(jedis -> jedis.persist(key));
    }

    @Override
    public String type(String key) {
        return doExecuteWithRedis(jedis -> jedis.type(key));
    }

    @Override
    public byte[] dump(String key) {
        return doExecuteWithRedis(jedis -> jedis.dump(key));
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue) {
        return doExecuteWithRedis(jedis -> jedis.restore(key, ttl, serializedValue));
    }

    @Override
    public String restoreReplace(String key, long ttl, byte[] serializedValue) {
        return doExecuteWithRedis(jedis -> jedis.restoreReplace(key, ttl, serializedValue));
    }

    @Override
    public String restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
        return doExecuteWithRedis(jedis -> jedis.restore(key, ttl, serializedValue, params));
    }

    @Override
    public Long expire(String key, long seconds) {
        return doExecuteWithRedis(jedis -> jedis.expire(key, seconds));
    }

    @Override
    public Long pexpire(String key, long milliseconds) {
        return doExecuteWithRedis(jedis -> jedis.pexpire(key, milliseconds));
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return doExecuteWithRedis(jedis -> jedis.expireAt(key, unixTime));
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return doExecuteWithRedis(jedis -> jedis.pexpireAt(key, millisecondsTimestamp));
    }

    @Override
    public Long ttl(String key) {
        return doExecuteWithRedis(jedis -> jedis.ttl(key));
    }

    @Override
    public Long pttl(String key) {
        return doExecuteWithRedis(jedis -> jedis.pttl(key));
    }

    @Override
    public Long touch(String key) {
        return doExecuteWithRedis(jedis -> jedis.touch(key));
    }

    @Override
    public Boolean setbit(String key, long offset, boolean value) {
        return doExecuteWithRedis(jedis -> jedis.setbit(key, offset, value));
    }

    @Override
    public Boolean setbit(String key, long offset, String value) {
        return doExecuteWithRedis(jedis -> jedis.setbit(key, offset, value));
    }

    @Override
    public Boolean getbit(String key, long offset) {
        return doExecuteWithRedis(jedis -> jedis.getbit(key, offset));
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        return doExecuteWithRedis(jedis -> jedis.setrange(key, offset, value));
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return doExecuteWithRedis(jedis -> jedis.getrange(key, startOffset, endOffset));
    }

    @Override
    public String getSet(String key, String value) {
        return doExecuteWithRedis(jedis -> jedis.getSet(key, value));
    }

    @Override
    public Long setnx(String key, String value) {
        return doExecuteWithRedis(jedis -> jedis.setnx(key, value));
    }

    @Override
    public String setex(String key, long seconds, String value) {
        return doExecuteWithRedis(jedis -> jedis.setex(key, seconds, value));
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return doExecuteWithRedis(jedis -> jedis.psetex(key, milliseconds, value));
    }

    @Override
    public Long decrBy(String key, long decrement) {
        return doExecuteWithRedis(jedis -> jedis.decrBy(key, decrement));
    }

    @Override
    public Long decr(String key) {
        return doExecuteWithRedis(jedis -> jedis.decr(key));
    }

    @Override
    public Long incrBy(String key, long increment) {
        return doExecuteWithRedis(jedis -> jedis.incrBy(key, increment));
    }

    @Override
    public Double incrByFloat(String key, double increment) {
        return doExecuteWithRedis(jedis -> jedis.incrByFloat(key, increment));
    }

    @Override
    public Long incr(String key) {
        return doExecuteWithRedis(jedis -> jedis.incr(key));
    }

    @Override
    public Long append(String key, String value) {
        return doExecuteWithRedis(jedis -> jedis.append(key, value));
    }

    @Override
    public String substr(String key, int start, int end) {
        return doExecuteWithRedis(jedis -> jedis.substr(key, start, end));
    }

    @Override
    public Long hset(String key, String field, String value) {
        return doExecuteWithRedis(jedis -> jedis.hset(key, field, value));
    }

    @Override
    public Long hset(String key, Map<String, String> hash) {
        return doExecuteWithRedis(jedis -> jedis.hset(key, hash));
    }

    @Override
    public String hget(String key, String field) {
        return doExecuteWithRedis(jedis -> jedis.hget(key, field));
    }

    @Override
    public Long hsetnx(String key, String field, String value) {
        return doExecuteWithRedis(jedis -> jedis.hsetnx(key, field, value));
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return doExecuteWithRedis(jedis -> jedis.hmset(key, hash));
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return doExecuteWithRedis(jedis -> jedis.hmget(key, fields));
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return doExecuteWithRedis(jedis -> jedis.hincrBy(key, field, value));
    }

    @Override
    public Double hincrByFloat(String key, String field, double value) {
        return doExecuteWithRedis(jedis -> jedis.hincrByFloat(key, field, value));
    }

    @Override
    public Boolean hexists(String key, String field) {
        return doExecuteWithRedis(jedis -> jedis.hexists(key, field));
    }

    @Override
    public Long hdel(String key, String... field) {
        return doExecuteWithRedis(jedis -> jedis.hdel(key, field));
    }

    @Override
    public Long hlen(String key) {
        return doExecuteWithRedis(jedis -> jedis.hlen(key));
    }

    @Override
    public Set<String> hkeys(String key) {
        return doExecuteWithRedis(jedis -> jedis.hkeys(key));
    }

    @Override
    public List<String> hvals(String key) {
        return doExecuteWithRedis(jedis -> jedis.hvals(key));
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return doExecuteWithRedis(jedis -> jedis.hgetAll(key));
    }

    @Override
    public String hrandfield(String key) {
        return doExecuteWithRedis(jedis -> jedis.hrandfield(key));
    }

    @Override
    public List<String> hrandfield(String key, long count) {
        return doExecuteWithRedis(jedis -> jedis.hrandfield(key, count));
    }

    @Override
    public Map<String, String> hrandfieldWithValues(String key, long count) {
        return doExecuteWithRedis(jedis -> jedis.hrandfieldWithValues(key, count));
    }

    @Override
    public Long rpush(String key, String... string) {
        return doExecuteWithRedis(jedis -> jedis.rpush(key, string));
    }

    @Override
    public Long lpush(String key, String... string) {
        return doExecuteWithRedis(jedis -> jedis.lpush(key, string));
    }

    @Override
    public Long llen(String key) {
        return doExecuteWithRedis(jedis -> jedis.llen(key));
    }

    @Override
    public List<String> lrange(String key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.lrange(key, start, stop));
    }

    @Override
    public String ltrim(String key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.ltrim(key, start, stop));
    }

    @Override
    public String lindex(String key, long index) {
        return doExecuteWithRedis(jedis -> jedis.lindex(key, index));
    }

    @Override
    public String lset(String key, long index, String value) {
        return doExecuteWithRedis(jedis -> jedis.lset(key, index, value));
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return doExecuteWithRedis(jedis -> jedis.lrem(key, count, value));
    }

    @Override
    public String lpop(String key) {
        return doExecuteWithRedis(jedis -> jedis.lpop(key));
    }

    @Override
    public List<String> lpop(String key, int count) {
        return doExecuteWithRedis(jedis -> jedis.lpop(key, count));
    }

    @Override
    public Long lpos(String key, String element) {
        return doExecuteWithRedis(jedis -> jedis.lpos(key, element));
    }

    @Override
    public Long lpos(String key, String element, LPosParams params) {
        return doExecuteWithRedis(jedis -> jedis.lpos(key, element, params));
    }

    @Override
    public List<Long> lpos(String key, String element, LPosParams params, long count) {
        return doExecuteWithRedis(jedis -> jedis.lpos(key, element, params, count));
    }

    @Override
    public String rpop(String key) {
        return doExecuteWithRedis(jedis -> jedis.rpop(key));
    }

    @Override
    public List<String> rpop(String key, int count) {
        return doExecuteWithRedis(jedis -> jedis.rpop(key, count));
    }

    @Override
    public Long sadd(String key, String... member) {
        return doExecuteWithRedis(jedis -> jedis.sadd(key, member));
    }

    @Override
    public Set<String> smembers(String key) {
        return doExecuteWithRedis(jedis -> jedis.smembers(key));
    }

    @Override
    public Long srem(String key, String... member) {
        return doExecuteWithRedis(jedis -> jedis.srem(key, member));
    }

    @Override
    public String spop(String key) {
        return doExecuteWithRedis(jedis -> jedis.spop(key));
    }

    @Override
    public Set<String> spop(String key, long count) {
        return doExecuteWithRedis(jedis -> jedis.spop(key, count));
    }

    @Override
    public Long scard(String key) {
        return doExecuteWithRedis(jedis -> jedis.scard(key));
    }

    @Override
    public Boolean sismember(String key, String member) {
        return doExecuteWithRedis(jedis -> jedis.sismember(key, member));
    }

    @Override
    public List<Boolean> smismember(String key, String... members) {
        return doExecuteWithRedis(jedis -> jedis.smismember(key, members));
    }

    @Override
    public String srandmember(String key) {
        return doExecuteWithRedis(jedis -> jedis.srandmember(key));
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return doExecuteWithRedis(jedis -> jedis.srandmember(key, count));
    }

    @Override
    public Long strlen(String key) {
        return doExecuteWithRedis(jedis -> jedis.strlen(key));
    }

    @Override
    public Long zadd(String key, double score, String member) {
        return doExecuteWithRedis(jedis -> jedis.zadd(key, score, member));
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params) {
        return doExecuteWithRedis(jedis -> jedis.zadd(key, score, member, params));
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers) {
        return doExecuteWithRedis(jedis -> jedis.zadd(key, scoreMembers));
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        return doExecuteWithRedis(jedis -> jedis.zadd(key, scoreMembers, params));
    }

    @Override
    public Double zaddIncr(String key, double score, String member, ZAddParams params) {
        return doExecuteWithRedis(jedis -> jedis.zaddIncr(key, score, member, params));
    }

    @Override
    public Set<String> zrange(String key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zrange(key, start, stop));
    }

    @Override
    public Long zrem(String key, String... members) {
        return doExecuteWithRedis(jedis -> jedis.zrem(key, members));
    }

    @Override
    public Double zincrby(String key, double increment, String member) {
        return doExecuteWithRedis(jedis -> jedis.zincrby(key, increment, member));
    }

    @Override
    public Double zincrby(String key, double increment, String member, ZIncrByParams params) {
        return doExecuteWithRedis(jedis -> jedis.zincrby(key, increment, member, params));
    }

    @Override
    public Long zrank(String key, String member) {
        return doExecuteWithRedis(jedis -> jedis.zrank(key, member));
    }

    @Override
    public Long zrevrank(String key, String member) {
        return doExecuteWithRedis(jedis -> jedis.zrevrank(key, member));
    }

    @Override
    public Set<String> zrevrange(String key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zrevrange(key, start, stop));
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zrangeWithScores(key, start, stop));
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeWithScores(key, start, stop));
    }

    @Override
    public String zrandmember(String key) {
        return doExecuteWithRedis(jedis -> jedis.zrandmember(key));
    }

    @Override
    public Set<String> zrandmember(String key, long count) {
        return doExecuteWithRedis(jedis -> jedis.zrandmember(key, count));
    }

    @Override
    public Set<Tuple> zrandmemberWithScores(String key, long count) {
        return doExecuteWithRedis(jedis -> jedis.zrandmemberWithScores(key, count));
    }

    @Override
    public Long zcard(String key) {
        return doExecuteWithRedis(jedis -> jedis.zcard(key));
    }

    @Override
    public Double zscore(String key, String member) {
        return doExecuteWithRedis(jedis -> jedis.zscore(key, member));
    }

    @Override
    public List<Double> zmscore(String key, String... members) {
        return doExecuteWithRedis(jedis -> jedis.zmscore(key, members));
    }

    @Override
    public Tuple zpopmax(String key) {
        return doExecuteWithRedis(jedis -> jedis.zpopmax(key));
    }

    @Override
    public Set<Tuple> zpopmax(String key, int count) {
        return doExecuteWithRedis(jedis -> jedis.zpopmax(key, count));
    }

    @Override
    public Tuple zpopmin(String key) {
        return doExecuteWithRedis(jedis -> jedis.zpopmin(key));
    }

    @Override
    public Set<Tuple> zpopmin(String key, int count) {
        return doExecuteWithRedis(jedis -> jedis.zpopmin(key, count));
    }

    @Override
    public List<String> sort(String key) {
        return doExecuteWithRedis(jedis -> jedis.sort(key));
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        return doExecuteWithRedis(jedis -> jedis.sort(key, sortingParameters));
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return doExecuteWithRedis(jedis -> jedis.zcount(key, min, max));
    }

    @Override
    public Long zcount(String key, String min, String max) {
        return doExecuteWithRedis(jedis -> jedis.zcount(key, min, max));
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScore(key, min, max));
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScore(key, min, max));
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScore(key, max, min));
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScore(key, min, max, offset, count));
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScore(key, max, min));
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScore(key, min, max, offset, count));
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScore(key, max, min, offset, count));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScoreWithScores(key, min, max));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScoreWithScores(key, min, max, offset, count));
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScore(key, max, min, offset, count));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScoreWithScores(key, min, max));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min));
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByScoreWithScores(key, min, max, offset, count));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count));
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByScoreWithScores(key, max, min, offset, count));
    }

    @Override
    public Long zremrangeByRank(String key, long start, long stop) {
        return doExecuteWithRedis(jedis -> jedis.zremrangeByRank(key, start, stop));
    }

    @Override
    public Long zremrangeByScore(String key, double min, double max) {
        return doExecuteWithRedis(jedis -> jedis.zremrangeByScore(key, min, max));
    }

    @Override
    public Long zremrangeByScore(String key, String min, String max) {
        return doExecuteWithRedis(jedis -> jedis.zremrangeByScore(key, min, max));
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        return doExecuteWithRedis(jedis -> jedis.zlexcount(key, min, max));
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByLex(key, min, max));
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrangeByLex(key, min, max, offset, count));
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByLex(key, max, min));
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return doExecuteWithRedis(jedis -> jedis.zrevrangeByLex(key, max, min, offset, count));
    }

    @Override
    public Long zremrangeByLex(String key, String min, String max) {
        return doExecuteWithRedis(jedis -> jedis.zremrangeByLex(key, min, max));
    }

    @Override
    public Long linsert(String key, ListPosition where, String pivot, String value) {
        return doExecuteWithRedis(jedis -> jedis.linsert(key, where, pivot, value));
    }

    @Override
    public Long lpushx(String key, String... string) {
        return doExecuteWithRedis(jedis -> jedis.lpushx(key, string));
    }

    @Override
    public Long rpushx(String key, String... string) {
        return doExecuteWithRedis(jedis -> jedis.rpushx(key, string));
    }

    @Override
    public List<String> blpop(int timeout, String key) {
        return doExecuteWithRedis(jedis -> jedis.blpop(timeout, key));
    }

    @Override
    public KeyedListElement blpop(double timeout, String key) {
        return doExecuteWithRedis(jedis -> jedis.blpop(timeout, key));
    }

    @Override
    public List<String> brpop(int timeout, String key) {
        return doExecuteWithRedis(jedis -> jedis.brpop(timeout, key));
    }

    @Override
    public KeyedListElement brpop(double timeout, String key) {
        return doExecuteWithRedis(jedis -> jedis.brpop(timeout, key));
    }

    @Override
    public Long del(String key) {
        return doExecuteWithRedis(jedis -> jedis.del(key));
    }

    @Override
    public Long unlink(String key) {
        return doExecuteWithRedis(jedis -> jedis.unlink(key));
    }

    @Override
    public String echo(String string) {
        return doExecuteWithRedis(jedis -> jedis.echo(string));
    }

    @Override
    public Long move(String key, int dbIndex) {
        return doExecuteWithRedis(jedis -> jedis.move(key, dbIndex));
    }

    @Override
    public Long bitcount(String key) {
        return doExecuteWithRedis(jedis -> jedis.bitcount(key));
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        return doExecuteWithRedis(jedis -> jedis.bitcount(key, start, end));
    }

    @Override
    public Long bitpos(String key, boolean value) {
        return doExecuteWithRedis(jedis -> jedis.bitpos(key, value));
    }

    @Override
    public Long bitpos(String key, boolean value, BitPosParams params) {
        return doExecuteWithRedis(jedis -> jedis.bitpos(key, value, params));
    }

    @Override
    public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
        return doExecuteWithRedis(jedis -> jedis.hscan(key, cursor));
    }

    @Override
    public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return doExecuteWithRedis(jedis -> jedis.hscan(key, cursor, params));
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor) {
        return doExecuteWithRedis(jedis -> jedis.sscan(key, cursor));
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor) {
        return doExecuteWithRedis(jedis -> jedis.zscan(key, cursor));
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return doExecuteWithRedis(jedis -> jedis.zscan(key, cursor, params));
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        return doExecuteWithRedis(jedis -> jedis.sscan(key, cursor, params));
    }

    @Override
    public Long pfadd(String key, String... elements) {
        return doExecuteWithRedis(jedis -> jedis.pfadd(key, elements));
    }

    @Override
    public long pfcount(String key) {
        return doExecuteWithRedis(jedis -> jedis.pfcount(key));
    }

    @Override
    public Long geoadd(String key, double longitude, double latitude, String member) {
        return doExecuteWithRedis(jedis -> jedis.geoadd(key, longitude, latitude, member));
    }

    @Override
    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        return doExecuteWithRedis(jedis -> jedis.geoadd(key, memberCoordinateMap));
    }

    @Override
    public Long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
        return doExecuteWithRedis(jedis -> jedis.geoadd(key, params, memberCoordinateMap));
    }

    @Override
    public Double geodist(String key, String member1, String member2) {
        return doExecuteWithRedis(jedis -> jedis.geodist(key, member1, member2));
    }

    @Override
    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.geodist(key, member1, member2, unit));
    }

    @Override
    public List<String> geohash(String key, String... members) {
        return doExecuteWithRedis(jedis -> jedis.geohash(key, members));
    }

    @Override
    public List<GeoCoordinate> geopos(String key, String... members) {
        return doExecuteWithRedis(jedis -> jedis.geopos(key, members));
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.georadius(key, longitude, latitude, radius, unit));
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.georadiusReadonly(key, longitude, latitude, radius, unit));
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return doExecuteWithRedis(jedis -> jedis.georadius(key, longitude, latitude, radius, unit, param));
    }

    @Override
    public List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return doExecuteWithRedis(jedis -> jedis.georadiusReadonly(key, longitude, latitude, radius, unit, param));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMember(key, member, radius, unit));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMemberReadonly(key, member, radius, unit));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMember(key, member, radius, unit, param));
    }

    @Override
    public List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        return doExecuteWithRedis(jedis -> jedis.georadiusByMemberReadonly(key, member, radius, unit, param));
    }

    @Override
    public List<Long> bitfield(String key, String... arguments) {
        return doExecuteWithRedis(jedis -> jedis.bitfield(key, arguments));
    }

    @Override
    public List<Long> bitfieldReadonly(String key, String... arguments) {
        return doExecuteWithRedis(jedis -> jedis.bitfieldReadonly(key, arguments));
    }

    @Override
    public Long hstrlen(String key, String field) {
        return doExecuteWithRedis(jedis -> jedis.hstrlen(key, field));
    }

    @Override
    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
        return doExecuteWithRedis(jedis -> jedis.xadd(key, id, hash));
    }

    @Override
    public StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash, long maxLen, boolean approximateLength) {
        return doExecuteWithRedis(jedis -> jedis.xadd(key, id, hash, maxLen, approximateLength));
    }

    @Override
    public StreamEntryID xadd(String key, Map<String, String> hash, XAddParams params) {
        return doExecuteWithRedis(jedis -> jedis.xadd(key, hash, params));
    }

    @Override
    public Long xlen(String key) {
        return doExecuteWithRedis(jedis -> jedis.xlen(key));
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end) {
        return doExecuteWithRedis(jedis -> jedis.xrange(key, start, end));
    }

    @Override
    public List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
        return doExecuteWithRedis(jedis -> jedis.xrange(key, start, end, count));
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
        return doExecuteWithRedis(jedis -> jedis.xrevrange(key, end, start));
    }

    @Override
    public List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
        return doExecuteWithRedis(jedis -> jedis.xrevrange(key, end, start, count));
    }

    // For compatibility and adaptation
    public long xack(String key, String group, StreamEntryID... ids) {
        return doExecuteWithRedis(jedis -> jedis.xack(key, group, ids));
    }

    // For compatibility and adaptation
    @Override
    public long xack$JedisCommands(String key, String group, StreamEntryID... ids) {
        return xack(key, group, ids);
    }

    @Override
    public String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream) {
        return doExecuteWithRedis(jedis -> jedis.xgroupCreate(key, groupname, id, makeStream));
    }

    @Override
    public String xgroupSetID(String key, String groupname, StreamEntryID id) {
        return doExecuteWithRedis(jedis -> jedis.xgroupSetID(key, groupname, id));
    }

    // For compatibility and adaptation
    public long xgroupDestroy(String key, String groupname) {
        return doExecuteWithRedis(jedis -> jedis.xgroupDestroy(key, groupname));
    }

    // For compatibility and adaptation
    public long xgroupDestroy$JedisCommands(String key, String groupname) {
        return xgroupDestroy(key, groupname);
    }

    @Override
    public Long xgroupDelConsumer(String key, String groupname, String consumername) {
        return doExecuteWithRedis(jedis -> jedis.xgroupDelConsumer(key, groupname, consumername));
    }

    @Override
    public StreamPendingSummary xpending(String key, String groupname) {
        return doExecuteWithRedis(jedis -> jedis.xpending(key, groupname));
    }

    @Override
    public List<StreamPendingEntry> xpending(String key, String groupname, StreamEntryID start, StreamEntryID end, int count,
            String consumername) {
        return doExecuteWithRedis(jedis -> jedis.xpending(key, groupname, start, end, count, consumername));
    }

    @Override
    public List<StreamPendingEntry> xpending(String key, String groupname, XPendingParams params) {
        return doExecuteWithRedis(jedis -> jedis.xpending(key, groupname, params));
    }

    // For compatibility and adaptation
    public long xdel(String key, StreamEntryID... ids) {
        return doExecuteWithRedis(jedis -> jedis.xdel(key, ids));
    }

    // For compatibility and adaptation
    @Override
    public long xdel$JedisCommands(String key, StreamEntryID... ids) {
        return xdel(key, ids);
    }

    // For compatibility and adaptation
    public long xtrim(String key, long maxLen, boolean approximate) {
        return doExecuteWithRedis(jedis -> jedis.xtrim(key, maxLen, approximate));
    }

    // For compatibility and adaptation
    @Override
    public long xtrim$JedisCommands(String key, long maxLen, boolean approximate) {
        return xtrim(key, maxLen, approximate);
    }

    // For compatibility and adaptation
    public long xtrim(String key, XTrimParams params) {
        return doExecuteWithRedis(jedis -> jedis.xtrim(key, params));
    }

    // For compatibility and adaptation
    @Override
    public long xtrim$JedisCommands(String key, XTrimParams params) {
        return xtrim(key, params);
    }

    @Override
    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, long newIdleTime,
            int retries, boolean force, StreamEntryID... ids) {
        return doExecuteWithRedis(jedis -> jedis.xclaim(key, group, consumername, minIdleTime, newIdleTime, retries, force, ids));
    }

    @Override
    public List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, XClaimParams params,
            StreamEntryID... ids) {
        return doExecuteWithRedis(jedis -> jedis.xclaim(key, group, consumername, minIdleTime, params, ids));
    }

    @Override
    public List<StreamEntryID> xclaimJustId(String key, String group, String consumername, long minIdleTime, XClaimParams params,
            StreamEntryID... ids) {
        return doExecuteWithRedis(jedis -> jedis.xclaimJustId(key, group, consumername, minIdleTime, params, ids));
    }

    @Override
    public StreamInfo xinfoStream(String key) {
        return doExecuteWithRedis(jedis -> jedis.xinfoStream(key));
    }

    @Override
    public List<StreamGroupInfo> xinfoGroup(String key) {
        return doExecuteWithRedis(jedis -> jedis.xinfoGroup(key));
    }

    @Override
    public List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
        return doExecuteWithRedis(jedis -> jedis.xinfoConsumers(key, group));
    }

    /**
     * Do execute with redis operations.
     * 
     * @param invoker
     * @return
     */
    protected <T> T doExecuteWithRedis(Function<Jedis, T> invoker) {
        try (Jedis jedis = jedisPool.getResource();) {
            return invoker.apply(jedis);
        } catch (Throwable t) {
            throw new JedisException("Errors jedis processing.", t);
        }
    }

}
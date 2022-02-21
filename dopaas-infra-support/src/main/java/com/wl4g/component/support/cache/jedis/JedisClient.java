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
package com.wl4g.component.support.cache.jedis;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import redis.clients.jedis.JedisCluster;
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
 * Composite {@link Jedis} and {@link JedisCluster} facade adapter. </br>
 * 
 * <p>
 * Synchronization of jedis code process description: </br>
 * Use the following shell command to remove the completely duplicate methods
 * (method name and parameter list) of {@link Jedis} and {@link JedisCluster} :
 * 
 * <pre>
 * cat JedisClient.java|grep default|sort -u
 * </pre>
 * </p>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020年7月18日 v1.0.0
 * @see jedis-3.6.1
 */
@SuppressWarnings("unchecked")
public interface JedisClient extends Closeable {

    // JedisCommands

    default Map<String, JedisPool> getClusterNodes() {
        throw new UnsupportedOperationException();
    }

    default AccessControlUser aclGetUser(byte[] name) {
        throw new UnsupportedOperationException();
    }

    default AccessControlUser aclGetUser(String name) {
        throw new UnsupportedOperationException();
    }

    default Boolean copy(byte[] srcKey, byte[] dstKey, boolean replace) {
        throw new UnsupportedOperationException();
    }

    default Boolean copy(byte[] srcKey, byte[] dstKey, int db, boolean replace) {
        throw new UnsupportedOperationException();
    }

    default Boolean copy(String srcKey, String dstKey, boolean replace) {
        throw new UnsupportedOperationException();
    }

    default Boolean copy(String srcKey, String dstKey, int db, boolean replace) {
        throw new UnsupportedOperationException();
    }

    default Boolean exists(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Boolean exists(String key) {
        throw new UnsupportedOperationException();
    }

    default Boolean getbit(byte[] key, long offset) {
        throw new UnsupportedOperationException();
    }

    default Boolean getbit(String key, long offset) {
        throw new UnsupportedOperationException();
    }

    default Boolean hexists(byte[] key, byte[] field) {
        throw new UnsupportedOperationException();
    }

    default Boolean hexists(String key, String field) {
        throw new UnsupportedOperationException();
    }

    default Boolean scriptExists(String sha1) {
        throw new UnsupportedOperationException();
    }

    default Boolean scriptExists(String sha1, String sampleKey) {
        throw new UnsupportedOperationException();
    }

    default Boolean setbit(byte[] key, long offset, boolean value) {
        throw new UnsupportedOperationException();
    }

    default Boolean setbit(byte[] key, long offset, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default Boolean setbit(String key, long offset, boolean value) {
        throw new UnsupportedOperationException();
    }

    default Boolean setbit(String key, long offset, String value) {
        throw new UnsupportedOperationException();
    }

    default Boolean sismember(byte[] key, byte[] member) {
        throw new UnsupportedOperationException();
    }

    default Boolean sismember(String key, String member) {
        throw new UnsupportedOperationException();
    }

    default byte[] aclGenPassBinary() {
        throw new UnsupportedOperationException();
    }

    default byte[] aclLog(byte[] options) {
        throw new UnsupportedOperationException();
    }

    default byte[] aclWhoAmIBinary() {
        throw new UnsupportedOperationException();
    }

    default byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout) {
        throw new UnsupportedOperationException();
    }

    default byte[] brpoplpush(byte[] source, byte[] destination, int timeout) {
        throw new UnsupportedOperationException();
    }

    default byte[] clientGetnameBinary() {
        throw new UnsupportedOperationException();
    }

    default byte[] clientInfoBinary() {
        throw new UnsupportedOperationException();
    }

    default byte[] clientListBinary() {
        throw new UnsupportedOperationException();
    }

    default byte[] clientListBinary(long... clientIds) {
        throw new UnsupportedOperationException();
    }

    default byte[] configSet(byte[] parameter, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default byte[] dump(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default byte[] dump(String key) {
        throw new UnsupportedOperationException();
    }

    default byte[] echo(byte[] arg) {
        throw new UnsupportedOperationException();
    }

    default byte[] get(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default byte[] getDel(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default byte[] getEx(byte[] key, GetExParams params) {
        throw new UnsupportedOperationException();
    }

    default byte[] getrange(byte[] key, long startOffset, long endOffset) {
        throw new UnsupportedOperationException();
    }

    default byte[] getSet(byte[] key, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default byte[] hget(byte[] key, byte[] field) {
        throw new UnsupportedOperationException();
    }

    default byte[] hrandfield(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default byte[] lindex(byte[] key, long index) {
        throw new UnsupportedOperationException();
    }

    default byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to) {
        throw new UnsupportedOperationException();
    }

    default byte[] lpop(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default byte[] memoryDoctorBinary() {
        throw new UnsupportedOperationException();
    }

    default byte[] objectEncoding(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default byte[] randomBinaryKey() {
        throw new UnsupportedOperationException();
    }

    default byte[] rpop(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default byte[] rpoplpush(byte[] srckey, byte[] dstkey) {
        throw new UnsupportedOperationException();
    }

    default byte[] scriptLoad(byte[] script) {
        throw new UnsupportedOperationException();
    }

    default byte[] scriptLoad(byte[] script, byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    default byte[] spop(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default byte[] srandmember(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default byte[] substr(byte[] key, int start, int end) {
        throw new UnsupportedOperationException();
    }

    default byte[] xadd(byte[] key, byte[] id, Map<byte[], byte[]> hash, long maxLen, boolean approximateLength) {
        throw new UnsupportedOperationException();
    }

    default byte[] xadd(byte[] key, Map<byte[], byte[]> hash, XAddParams params) {
        throw new UnsupportedOperationException();
    }

    default byte[] zrandmember(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Double geodist(byte[] key, byte[] member1, byte[] member2) {
        throw new UnsupportedOperationException();
    }

    default Double geodist(byte[] key, byte[] member1, byte[] member2, GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default Double geodist(String key, String member1, String member2) {
        throw new UnsupportedOperationException();
    }

    default Double geodist(String key, String member1, String member2, GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default Double hincrByFloat(byte[] key, byte[] field, double value) {
        throw new UnsupportedOperationException();
    }

    default Double hincrByFloat(String key, String field, double value) {
        throw new UnsupportedOperationException();
    }

    default Double incrByFloat(byte[] key, double increment) {
        throw new UnsupportedOperationException();
    }

    default Double incrByFloat(String key, double increment) {
        throw new UnsupportedOperationException();
    }

    default Double zaddIncr(byte[] key, double score, byte[] member, ZAddParams params) {
        throw new UnsupportedOperationException();
    }

    default Double zaddIncr(String key, double score, String member, ZAddParams params) {
        throw new UnsupportedOperationException();
    }

    default Double zincrby(byte[] key, double increment, byte[] member) {
        throw new UnsupportedOperationException();
    }

    default Double zincrby(byte[] key, double increment, byte[] member, ZIncrByParams params) {
        throw new UnsupportedOperationException();
    }

    default Double zincrby(String key, double increment, String member) {
        throw new UnsupportedOperationException();
    }

    default Double zincrby(String key, double increment, String member, ZIncrByParams params) {
        throw new UnsupportedOperationException();
    }

    default Double zscore(byte[] key, byte[] member) {
        throw new UnsupportedOperationException();
    }

    default Double zscore(String key, String member) {
        throw new UnsupportedOperationException();
    }

    default int getDB() {
        throw new UnsupportedOperationException();
    }

    default KeyedListElement blpop(double timeout, String key) {
        throw new UnsupportedOperationException();
    }

    default KeyedListElement blpop(double timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    default KeyedListElement brpop(double timeout, String key) {
        throw new UnsupportedOperationException();
    }

    default KeyedListElement brpop(double timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    default KeyedZSetElement bzpopmax(double timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    default KeyedZSetElement bzpopmin(double timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    default List<AccessControlLogEntry> aclLog() {
        throw new UnsupportedOperationException();
    }

    default List<AccessControlLogEntry> aclLog(int limit) {
        throw new UnsupportedOperationException();
    }

    default List<Boolean> scriptExists(String sampleKey, String... sha1) {
        throw new UnsupportedOperationException();
    }

    default List<Boolean> scriptExists(String... sha1) {
        throw new UnsupportedOperationException();
    }

    default List<Boolean> smismember(byte[] key, byte[]... members) {
        throw new UnsupportedOperationException();
    }

    default List<Boolean> smismember(String key, String... members) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> aclCatBinary() {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> aclCat(byte[] category) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> aclListBinary() {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> aclLogBinary() {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> aclLogBinary(int limit) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> aclUsersBinary() {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> blpop(byte[]... args) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> blpop(double timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> blpop(int timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> brpop(byte[]... args) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> brpop(double timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> brpop(int timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> bzpopmax(double timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> bzpopmin(double timeout, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> configGet(byte[] pattern) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> geohash(byte[] key, byte[]... members) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> hmget(byte[] key, byte[]... fields) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> hrandfield(byte[] key, long count) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> hvals(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> lpop(byte[] key, int count) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> lrange(byte[] key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> mget(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> objectHelpBinary() {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> rpop(byte[] key, int count) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> sort(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> sort(byte[] key, SortingParams sortingParameters) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> srandmember(byte[] key, int count) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xclaim(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params,
            byte[]... ids) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xclaim$JedisCommands(byte[] key, byte[] groupname, byte[] consumername, long minIdleTime,
            long newIdleTime, int retries, boolean force, byte[]... ids) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xclaim$JedisClusterCommands(byte[] key, byte[] groupname, byte[] consumername, long minIdleTime,
            long newIdleTime, int retries, boolean force, byte[][] ids) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xclaimJustId(byte[] key, byte[] group, byte[] consumername, long minIdleTime, XClaimParams params,
            byte[]... ids) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xrange(byte[] key, byte[] start, byte[] end) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xrange(byte[] key, byte[] start, byte[] end, int count) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xrange(byte[] key, byte[] start, byte[] end, long count) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xreadGroup(byte[] groupname, byte[] consumer, int count, long block, boolean noAck,
            Map<byte[], byte[]> streams) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xreadGroup(byte[] groupname, byte[] consumer, XReadGroupParams xReadGroupParams,
            Entry<byte[], byte[]>... streams) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xread(int count, long block, Map<byte[], byte[]> streams) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xread(XReadParams xReadParams, Entry<byte[], byte[]>... streams) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start) {
        throw new UnsupportedOperationException();
    }

    default List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start, int count) {
        throw new UnsupportedOperationException();
    }

    default List<Double> zmscore(byte[] key, byte[]... members) {
        throw new UnsupportedOperationException();
    }

    default List<Double> zmscore(String key, String... members) {
        throw new UnsupportedOperationException();
    }

    default List<Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer, int count, long block,
            boolean noAck, Entry<String, StreamEntryID>... streams) {
        throw new UnsupportedOperationException();
    }

    default List<Entry<String, List<StreamEntry>>> xreadGroup(String groupname, String consumer,
            XReadGroupParams xReadGroupParams, Map<String, StreamEntryID> streams) {
        throw new UnsupportedOperationException();
    }

    default List<Entry<String, List<StreamEntry>>> xread(int count, long block, Entry<String, StreamEntryID>... streams) {
        throw new UnsupportedOperationException();
    }

    default List<Entry<String, List<StreamEntry>>> xread(XReadParams xReadParams, Map<String, StreamEntryID> streams) {
        throw new UnsupportedOperationException();
    }

    default List<GeoCoordinate> geopos(byte[] key, byte[]... members) {
        throw new UnsupportedOperationException();
    }

    default List<GeoCoordinate> geopos(String key, String... members) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusByMember(byte[] key, byte[] member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusByMemberReadonly(byte[] key, byte[] member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusByMemberReadonly(String key, String member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadius(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius,
            GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusReadonly(byte[] key, double longitude, double latitude, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius,
            GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadiusReadonly(String key, double longitude, double latitude, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit) {
        throw new UnsupportedOperationException();
    }

    default List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
            GeoRadiusParam param) {
        throw new UnsupportedOperationException();
    }

    default List<Long> bitfield(byte[] key, byte[]... arguments) {
        throw new UnsupportedOperationException();
    }

    default List<Long> bitfieldReadonly(byte[] key, byte[]... arguments) {
        throw new UnsupportedOperationException();
    }

    default List<Long> bitfieldReadonly(String key, String... arguments) {
        throw new UnsupportedOperationException();
    }

    default List<Long> bitfield(String key, String... arguments) {
        throw new UnsupportedOperationException();
    }

    default List<Long> lpos(byte[] key, byte[] element, LPosParams params, long count) {
        throw new UnsupportedOperationException();
    }

    default List<Long> lpos(String key, String element, LPosParams params, long count) {
        throw new UnsupportedOperationException();
    }

    default List<Long> scriptExists(byte[] sampleKey, byte[]... sha1) {
        throw new UnsupportedOperationException();
    }

    default List<Long> scriptExists(byte[]... sha1) {
        throw new UnsupportedOperationException();
    }

    default List<Map<String, String>> sentinelMasters() {
        throw new UnsupportedOperationException();
    }

    default List<Map<String, String>> sentinelSlaves(String masterName) {
        throw new UnsupportedOperationException();
    }

    default List<Module> moduleList() {
        throw new UnsupportedOperationException();
    }

    default List<Object> clusterSlots() {
        throw new UnsupportedOperationException();
    }

    default List<Object> slowlogGetBinary() {
        throw new UnsupportedOperationException();
    }

    default List<Object> slowlogGetBinary(long entries) {
        throw new UnsupportedOperationException();
    }

    default List<Object> xinfoConsumersBinary(byte[] key, byte[] group) {
        throw new UnsupportedOperationException();
    }

    default List<Object> xinfoGroupBinary(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default List<Object> xpending(byte[] key, byte[] groupname, byte[] start, byte[] end, int count, byte[] consumername) {
        throw new UnsupportedOperationException();
    }

    default List<Object> xpending(byte[] key, byte[] groupname, XPendingParams params) {
        throw new UnsupportedOperationException();
    }

    default List<Slowlog> slowlogGet() {
        throw new UnsupportedOperationException();
    }

    default List<Slowlog> slowlogGet(long entries) {
        throw new UnsupportedOperationException();
    }

    default List<StreamConsumersInfo> xinfoConsumers(byte[] key, byte[] group) {
        throw new UnsupportedOperationException();
    }

    default List<StreamConsumersInfo> xinfoConsumers(String key, String group) {
        throw new UnsupportedOperationException();
    }

    default List<StreamEntryID> xclaimJustId(String key, String group, String consumername, long minIdleTime, XClaimParams params,
            StreamEntryID... ids) {
        throw new UnsupportedOperationException();
    }

    default List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, long newIdleTime,
            int retries, boolean force, StreamEntryID... ids) {
        throw new UnsupportedOperationException();
    }

    default List<StreamEntry> xclaim(String key, String group, String consumername, long minIdleTime, XClaimParams params,
            StreamEntryID... ids) {
        throw new UnsupportedOperationException();
    }

    default List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end) {
        throw new UnsupportedOperationException();
    }

    default List<StreamEntry> xrange(String key, StreamEntryID start, StreamEntryID end, int count) {
        throw new UnsupportedOperationException();
    }

    default List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start) {
        throw new UnsupportedOperationException();
    }

    default List<StreamEntry> xrevrange(String key, StreamEntryID end, StreamEntryID start, int count) {
        throw new UnsupportedOperationException();
    }

    default List<StreamGroupInfo> xinfoGroup(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default List<StreamGroupInfo> xinfoGroup(String key) {
        throw new UnsupportedOperationException();
    }

    default List<StreamPendingEntry> xpending(String key, String groupname, StreamEntryID start, StreamEntryID end, int count,
            String consumername) {
        throw new UnsupportedOperationException();
    }

    default List<StreamPendingEntry> xpending(String key, String groupname, XPendingParams params) {
        throw new UnsupportedOperationException();
    }

    default List<String> aclCat() {
        throw new UnsupportedOperationException();
    }

    default List<String> aclCat(String category) {
        throw new UnsupportedOperationException();
    }

    default List<String> aclList() {
        throw new UnsupportedOperationException();
    }

    default List<String> aclUsers() {
        throw new UnsupportedOperationException();
    }

    default List<String> blpop(int timeout, String key) {
        throw new UnsupportedOperationException();
    }

    default List<String> blpop(int timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    default List<String> blpop(String... args) {
        throw new UnsupportedOperationException();
    }

    default List<String> brpop(int timeout, String key) {
        throw new UnsupportedOperationException();
    }

    default List<String> brpop(int timeout, String... keys) {
        throw new UnsupportedOperationException();
    }

    default List<String> brpop(String... args) {
        throw new UnsupportedOperationException();
    }

    default List<String> clusterGetKeysInSlot(int slot, int count) {
        throw new UnsupportedOperationException();
    }

    default List<String> clusterSlaves(String nodeId) {
        throw new UnsupportedOperationException();
    }

    default List<String> configGet(String pattern) {
        throw new UnsupportedOperationException();
    }

    default List<String> geohash(String key, String... members) {
        throw new UnsupportedOperationException();
    }

    default List<String> hmget(String key, String... fields) {
        throw new UnsupportedOperationException();
    }

    default List<String> hrandfield(String key, long count) {
        throw new UnsupportedOperationException();
    }

    default List<String> hvals(String key) {
        throw new UnsupportedOperationException();
    }

    default List<String> lpop(String key, int count) {
        throw new UnsupportedOperationException();
    }

    default List<String> lrange(String key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default List<String> mget(String... keys) {
        throw new UnsupportedOperationException();
    }

    default List<String> objectHelp() {
        throw new UnsupportedOperationException();
    }

    default List<String> rpop(String key, int count) {
        throw new UnsupportedOperationException();
    }

    default List<String> sentinelGetMasterAddrByName(String masterName) {
        throw new UnsupportedOperationException();
    }

    default List<String> sort(String key) {
        throw new UnsupportedOperationException();
    }

    default List<String> sort(String key, SortingParams sortingParameters) {
        throw new UnsupportedOperationException();
    }

    default List<String> srandmember(String key, int count) {
        throw new UnsupportedOperationException();
    }

    default Long aclDelUser(byte[] name) {
        throw new UnsupportedOperationException();
    }

    default Long aclDelUser(String name) {
        throw new UnsupportedOperationException();
    }

    default Long append(byte[] key, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default Long append(String key, String value) {
        throw new UnsupportedOperationException();
    }

    default Long bitcount(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long bitcount(byte[] key, long start, long end) {
        throw new UnsupportedOperationException();
    }

    default Long bitcount(String key) {
        throw new UnsupportedOperationException();
    }

    default Long bitcount(String key, long start, long end) {
        throw new UnsupportedOperationException();
    }

    default Long bitop(BitOP op, byte[] destKey, byte[]... srcKeys) {
        throw new UnsupportedOperationException();
    }

    default Long bitop(BitOP op, String destKey, String... srcKeys) {
        throw new UnsupportedOperationException();
    }

    default Long bitpos(String key, boolean value) {
        throw new UnsupportedOperationException();
    }

    default Long bitpos(String key, boolean value, BitPosParams params) {
        throw new UnsupportedOperationException();
    }

    default Long clientId() {
        throw new UnsupportedOperationException();
    }

    default Long clientKill(ClientKillParams params) {
        throw new UnsupportedOperationException();
    }

    default Long clientUnblock(long clientId, UnblockType unblockType) {
        throw new UnsupportedOperationException();
    }

    default Long clusterCountKeysInSlot(int slot) {
        throw new UnsupportedOperationException();
    }

    default Long clusterKeySlot(String key) {
        throw new UnsupportedOperationException();
    }

    default Long dbSize() {
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

    default Long geoadd(byte[] key, double longitude, double latitude, byte[] member) {
        throw new UnsupportedOperationException();
    }

    default Long geoadd(byte[] key, GeoAddParams params, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        throw new UnsupportedOperationException();
    }

    default Long geoadd(byte[] key, Map<byte[], GeoCoordinate> memberCoordinateMap) {
        throw new UnsupportedOperationException();
    }

    default Long geoadd(String key, double longitude, double latitude, String member) {
        throw new UnsupportedOperationException();
    }

    default Long geoadd(String key, GeoAddParams params, Map<String, GeoCoordinate> memberCoordinateMap) {
        throw new UnsupportedOperationException();
    }

    default Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
        throw new UnsupportedOperationException();
    }

    default Long georadiusByMemberStore(byte[] key, byte[] member, double radius, GeoUnit unit, GeoRadiusParam param,
            GeoRadiusStoreParam storeParam) {
        throw new UnsupportedOperationException();
    }

    default Long georadiusByMemberStore(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param,
            GeoRadiusStoreParam storeParam) {
        throw new UnsupportedOperationException();
    }

    default Long georadiusStore(byte[] key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param,
            GeoRadiusStoreParam storeParam) {
        throw new UnsupportedOperationException();
    }

    default Long georadiusStore(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param,
            GeoRadiusStoreParam storeParam) {
        throw new UnsupportedOperationException();
    }

    default Long hdel(byte[] key, byte[]... field) {
        throw new UnsupportedOperationException();
    }

    default Long hdel(String key, String... field) {
        throw new UnsupportedOperationException();
    }

    default Long hincrBy(byte[] key, byte[] field, long value) {
        throw new UnsupportedOperationException();
    }

    default Long hincrBy(String key, String field, long value) {
        throw new UnsupportedOperationException();
    }

    default Long hlen(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long hlen(String key) {
        throw new UnsupportedOperationException();
    }

    default Long hset(byte[] key, byte[] field, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default Long hset(byte[] key, Map<byte[], byte[]> hash) {
        throw new UnsupportedOperationException();
    }

    default Long hsetnx(byte[] key, byte[] field, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default Long hsetnx(String key, String field, String value) {
        throw new UnsupportedOperationException();
    }

    default Long hset(String key, Map<String, String> hash) {
        throw new UnsupportedOperationException();
    }

    default Long hset(String key, String field, String value) {
        throw new UnsupportedOperationException();
    }

    default Long hstrlen(byte[] key, byte[] field) {
        throw new UnsupportedOperationException();
    }

    default Long hstrlen(String key, String field) {
        throw new UnsupportedOperationException();
    }

    default Long incrBy(byte[] key, long increment) {
        throw new UnsupportedOperationException();
    }

    default Long incrBy(String key, long increment) {
        throw new UnsupportedOperationException();
    }

    default Long incr(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long incr(String key) {
        throw new UnsupportedOperationException();
    }

    default Long lastsave() {
        throw new UnsupportedOperationException();
    }

    default Long linsert(byte[] key, ListPosition where, byte[] pivot, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default Long linsert(String key, ListPosition where, String pivot, String value) {
        throw new UnsupportedOperationException();
    }

    default Long llen(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long llen(String key) {
        throw new UnsupportedOperationException();
    }

    default Long lpos(byte[] key, byte[] element) {
        throw new UnsupportedOperationException();
    }

    default Long lpos(byte[] key, byte[] element, LPosParams params) {
        throw new UnsupportedOperationException();
    }

    default Long lpos(String key, String element) {
        throw new UnsupportedOperationException();
    }

    default Long lpos(String key, String element, LPosParams params) {
        throw new UnsupportedOperationException();
    }

    default Long lpush(byte[] key, byte[]... args) {
        throw new UnsupportedOperationException();
    }

    default Long lpush(String key, String... string) {
        throw new UnsupportedOperationException();
    }

    default Long lpushx(byte[] key, byte[]... arg) {
        throw new UnsupportedOperationException();
    }

    default Long lpushx(String key, String... string) {
        throw new UnsupportedOperationException();
    }

    default Long lrem(byte[] key, long count, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default Long lrem(String key, long count, String value) {
        throw new UnsupportedOperationException();
    }

    default Long memoryUsage(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long memoryUsage(byte[] key, int samples) {
        throw new UnsupportedOperationException();
    }

    default Long memoryUsage(String key) {
        throw new UnsupportedOperationException();
    }

    default Long memoryUsage(String key, int samples) {
        throw new UnsupportedOperationException();
    }

    default Long move(byte[] key, int dbIndex) {
        throw new UnsupportedOperationException();
    }

    default Long move(String key, int dbIndex) {
        throw new UnsupportedOperationException();
    }

    default Long msetnx(byte[]... keysvalues) {
        throw new UnsupportedOperationException();
    }

    default Long msetnx(String... keysvalues) {
        throw new UnsupportedOperationException();
    }

    default Long objectFreq(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long objectFreq(String key) {
        throw new UnsupportedOperationException();
    }

    default Long objectIdletime(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long objectIdletime(String key) {
        throw new UnsupportedOperationException();
    }

    default Long objectRefcount(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long objectRefcount(String key) {
        throw new UnsupportedOperationException();
    }

    default Long persist(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long persist(String key) {
        throw new UnsupportedOperationException();
    }

    default Long pexpireAt(byte[] key, long millisecondsTimestamp) {
        throw new UnsupportedOperationException();
    }

    default Long pexpireAt(String key, long millisecondsTimestamp) {
        throw new UnsupportedOperationException();
    }

    default Long pexpire(byte[] key, long milliseconds) {
        throw new UnsupportedOperationException();
    }

    default Long pexpire(String key, long milliseconds) {
        throw new UnsupportedOperationException();
    }

    default Long pfadd(byte[] key, byte[]... elements) {
        throw new UnsupportedOperationException();
    }

    default Long pfadd(String key, String... elements) {
        throw new UnsupportedOperationException();
    }

    default long pfcount(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long pfcount(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default long pfcount(String key) {
        throw new UnsupportedOperationException();
    }

    default long pfcount(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Long pttl(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long pttl(String key) {
        throw new UnsupportedOperationException();
    }

    default Long publish(byte[] channel, byte[] message) {
        throw new UnsupportedOperationException();
    }

    default Long publish(String channel, String message) {
        throw new UnsupportedOperationException();
    }

    default Long renamenx(byte[] oldkey, byte[] newkey) {
        throw new UnsupportedOperationException();
    }

    default Long renamenx(String oldkey, String newkey) {
        throw new UnsupportedOperationException();
    }

    default Long rpush(byte[] key, byte[]... args) {
        throw new UnsupportedOperationException();
    }

    default Long rpush(String key, String... string) {
        throw new UnsupportedOperationException();
    }

    default Long rpushx(byte[] key, byte[]... arg) {
        throw new UnsupportedOperationException();
    }

    default Long rpushx(String key, String... string) {
        throw new UnsupportedOperationException();
    }

    default Long sadd(byte[] key, byte[]... member) {
        throw new UnsupportedOperationException();
    }

    default Long sadd(String key, String... member) {
        throw new UnsupportedOperationException();
    }

    default Long scard(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long scard(String key) {
        throw new UnsupportedOperationException();
    }

    default Long sdiffstore(byte[] dstkey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Long sdiffstore(String dstkey, String... keys) {
        throw new UnsupportedOperationException();
    }

    default Long sentinelReset(String pattern) {
        throw new UnsupportedOperationException();
    }

    default Long setnx(byte[] key, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default Long setnx(String key, String value) {
        throw new UnsupportedOperationException();
    }

    default Long setrange(byte[] key, long offset, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default Long setrange(String key, long offset, String value) {
        throw new UnsupportedOperationException();
    }

    default Long sinterstore(byte[] dstkey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Long sinterstore(String dstkey, String... keys) {
        throw new UnsupportedOperationException();
    }

    default Long slowlogLen() {
        throw new UnsupportedOperationException();
    }

    default Long smove(byte[] srckey, byte[] dstkey, byte[] member) {
        throw new UnsupportedOperationException();
    }

    default Long smove(String srckey, String dstkey, String member) {
        throw new UnsupportedOperationException();
    }

    default Long sort(byte[] key, byte[] dstkey) {
        throw new UnsupportedOperationException();
    }

    default Long sort(byte[] key, SortingParams sortingParameters, byte[] dstkey) {
        throw new UnsupportedOperationException();
    }

    default Long sort(String key, SortingParams sortingParameters, String dstkey) {
        throw new UnsupportedOperationException();
    }

    default Long sort(String key, String dstkey) {
        throw new UnsupportedOperationException();
    }

    default Long srem(byte[] key, byte[]... member) {
        throw new UnsupportedOperationException();
    }

    default Long srem(String key, String... member) {
        throw new UnsupportedOperationException();
    }

    default Long strlen(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long strlen(String key) {
        throw new UnsupportedOperationException();
    }

    default Long sunionstore(byte[] dstkey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Long sunionstore(String dstkey, String... keys) {
        throw new UnsupportedOperationException();
    }

    default Long touch(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long touch(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Long touch(String key) {
        throw new UnsupportedOperationException();
    }

    default Long touch(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Long ttl(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long ttl(String key) {
        throw new UnsupportedOperationException();
    }

    default Long unlink(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long unlink(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Long unlink(String key) {
        throw new UnsupportedOperationException();
    }

    default Long unlink(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Long waitReplicas(byte[] key, int replicas, long timeout) {
        throw new UnsupportedOperationException();
    }

    default Long waitReplicas(int replicas, long timeout) {
        throw new UnsupportedOperationException();
    }

    default Long waitReplicas(String key, int replicas, long timeout) {
        throw new UnsupportedOperationException();
    }

    default Long xack(byte[] key, byte[] group, byte[]... ids) {
        throw new UnsupportedOperationException();
    }

    default long xack$JedisCommands(String key, String group, StreamEntryID... ids) {
        throw new UnsupportedOperationException();
    }

    default Long xack$JedisClusterCommands(String key, String group, StreamEntryID... ids) {
        throw new UnsupportedOperationException();
    }

    default Long xdel(byte[] key, byte[]... ids) {
        throw new UnsupportedOperationException();
    }

    default long xdel$JedisCommands(String key, StreamEntryID... ids) {
        throw new UnsupportedOperationException();
    }

    default Long xdel$JedisClusterCommands(String key, StreamEntryID... ids) {
        throw new UnsupportedOperationException();
    }

    default Long xgroupDelConsumer(byte[] key, byte[] consumer, byte[] consumerName) {
        throw new UnsupportedOperationException();
    }

    default Long xgroupDelConsumer(String key, String groupname, String consumername) {
        throw new UnsupportedOperationException();
    }

    default Long xgroupDestroy(byte[] key, byte[] consumer) {
        throw new UnsupportedOperationException();
    }

    default long xgroupDestroy$JedisCommands(String key, String groupname) {
        throw new UnsupportedOperationException();
    }

    default Long xgroupDestroy$JedisClusterCommands(String key, String groupname) {
        throw new UnsupportedOperationException();
    }

    default Long xlen(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long xlen(String key) {
        throw new UnsupportedOperationException();
    }

    default Long xtrim(byte[] key, long maxLen, boolean approximateLength) {
        throw new UnsupportedOperationException();
    }

    default Long xtrim(byte[] key, XTrimParams params) {
        throw new UnsupportedOperationException();
    }

    default long xtrim$JedisCommands(String key, long maxLen, boolean approximate) {
        throw new UnsupportedOperationException();
    }

    default Long xtrim$JedisClusterCommands(String key, long maxLen, boolean approximateLength) {
        throw new UnsupportedOperationException();
    }

    default long xtrim$JedisCommands(String key, XTrimParams params) {
        throw new UnsupportedOperationException();
    }

    default Long xtrim$JedisClusterCommands(String key, XTrimParams params) {
        throw new UnsupportedOperationException();
    }

    default Long zadd(byte[] key, double score, byte[] member) {
        throw new UnsupportedOperationException();
    }

    default Long zadd(byte[] key, double score, byte[] member, ZAddParams params) {
        throw new UnsupportedOperationException();
    }

    default Long zadd(byte[] key, Map<byte[], Double> scoreMembers) {
        throw new UnsupportedOperationException();
    }

    default Long zadd(byte[] key, Map<byte[], Double> scoreMembers, ZAddParams params) {
        throw new UnsupportedOperationException();
    }

    default Long zadd(String key, double score, String member) {
        throw new UnsupportedOperationException();
    }

    default Long zadd(String key, double score, String member, ZAddParams params) {
        throw new UnsupportedOperationException();
    }

    default Long zadd(String key, Map<String, Double> scoreMembers) {
        throw new UnsupportedOperationException();
    }

    default Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
        throw new UnsupportedOperationException();
    }

    default Long zcard(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Long zcard(String key) {
        throw new UnsupportedOperationException();
    }

    default Long zcount(byte[] key, byte[] min, byte[] max) {
        throw new UnsupportedOperationException();
    }

    default Long zcount(byte[] key, double min, double max) {
        throw new UnsupportedOperationException();
    }

    default Long zcount(String key, double min, double max) {
        throw new UnsupportedOperationException();
    }

    default Long zcount(String key, String min, String max) {
        throw new UnsupportedOperationException();
    }

    default Long zdiffStore(byte[] dstkey, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Long zdiffStore(String dstkey, String... keys) {
        throw new UnsupportedOperationException();
    }

    default Long zinterstore(byte[] dstkey, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    default Long zinterstore(byte[] dstkey, ZParams params, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    default Long zinterstore(String dstkey, String... sets) {
        throw new UnsupportedOperationException();
    }

    default Long zinterstore(String dstkey, ZParams params, String... sets) {
        throw new UnsupportedOperationException();
    }

    default Long zlexcount(byte[] key, byte[] min, byte[] max) {
        throw new UnsupportedOperationException();
    }

    default Long zlexcount(String key, String min, String max) {
        throw new UnsupportedOperationException();
    }

    default Long zrank(byte[] key, byte[] member) {
        throw new UnsupportedOperationException();
    }

    default Long zrank(String key, String member) {
        throw new UnsupportedOperationException();
    }

    default Long zrem(byte[] key, byte[]... members) {
        throw new UnsupportedOperationException();
    }

    default Long zremrangeByLex(byte[] key, byte[] min, byte[] max) {
        throw new UnsupportedOperationException();
    }

    default Long zremrangeByLex(String key, String min, String max) {
        throw new UnsupportedOperationException();
    }

    default Long zremrangeByRank(byte[] key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Long zremrangeByRank(String key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Long zremrangeByScore(byte[] key, byte[] min, byte[] max) {
        throw new UnsupportedOperationException();
    }

    default Long zremrangeByScore(byte[] key, double min, double max) {
        throw new UnsupportedOperationException();
    }

    default Long zremrangeByScore(String key, double min, double max) {
        throw new UnsupportedOperationException();
    }

    default Long zremrangeByScore(String key, String min, String max) {
        throw new UnsupportedOperationException();
    }

    default Long zrem(String key, String... members) {
        throw new UnsupportedOperationException();
    }

    default Long zrevrank(byte[] key, byte[] member) {
        throw new UnsupportedOperationException();
    }

    default Long zrevrank(String key, String member) {
        throw new UnsupportedOperationException();
    }

    default Long zunionstore(byte[] dstkey, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    default Long zunionstore(byte[] dstkey, ZParams params, byte[]... sets) {
        throw new UnsupportedOperationException();
    }

    default Long zunionstore(String dstkey, String... sets) {
        throw new UnsupportedOperationException();
    }

    default Long zunionstore(String dstkey, ZParams params, String... sets) {
        throw new UnsupportedOperationException();
    }

    default Map<byte[], byte[]> hgetAll(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Map<byte[], byte[]> hrandfieldWithValues(byte[] key, long count) {
        throw new UnsupportedOperationException();
    }

    default Map<String, String> hgetAll(String key) {
        throw new UnsupportedOperationException();
    }

    default Map<String, String> hrandfieldWithValues(String key, long count) {
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

    default Object evalsha(byte[] sha1) {
        throw new UnsupportedOperationException();
    }

    default Object evalsha(byte[] sha1, byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    default Object evalsha(byte[] sha1, int keyCount, byte[]... params) {
        throw new UnsupportedOperationException();
    }

    default Object evalsha(byte[] sha1, List<byte[]> keys, List<byte[]> args) {
        throw new UnsupportedOperationException();
    }

    default Object evalsha(String sha1) {
        throw new UnsupportedOperationException();
    }

    default Object evalsha(String sha1, int keyCount, String... params) {
        throw new UnsupportedOperationException();
    }

    default Object evalsha(String sha1, List<String> keys, List<String> args) {
        throw new UnsupportedOperationException();
    }

    default Object evalsha(String sha1, String sampleKey) {
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

    default Object xinfoStreamBinary(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Object xpending(byte[] key, byte[] groupname) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<byte[]> scan(byte[] cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<byte[]> sscan(byte[] key, byte[] cursor) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<byte[]> sscan(byte[] key, byte[] cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<Entry<byte[], byte[]>> hscan(byte[] key, byte[] cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<Entry<String, String>> hscan(String key, String cursor) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<String> scan(String cursor) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<String> scan(String cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<String> sscan(String key, String cursor) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<Tuple> zscan(byte[] key, byte[] cursor) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<Tuple> zscan(byte[] key, byte[] cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<Tuple> zscan(String key, String cursor) {
        throw new UnsupportedOperationException();
    }

    default ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> hkeys(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> keys(byte[] pattern) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> sdiff(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> sinter(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> smembers(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> spop(byte[] key, long count) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> sunion(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zdiff(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zinter(ZParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrandmember(byte[] key, long count) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrangeByLex(byte[] key, byte[] min, byte[] max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrangeByScore(byte[] key, byte[] min, byte[] max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrangeByScore(byte[] key, double min, double max) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrange(byte[] key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrevrangeByLex(byte[] key, byte[] max, byte[] min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrevrangeByScore(byte[] key, byte[] max, byte[] min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zrevrange(byte[] key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Set<byte[]> zunion(ZParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<String> hkeys(String key) {
        throw new UnsupportedOperationException();
    }

    default Set<String> keys(String pattern) {
        throw new UnsupportedOperationException();
    }

    default Set<String> sdiff(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<String> sinter(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<String> smembers(String key) {
        throw new UnsupportedOperationException();
    }

    default Set<String> spop(String key, long count) {
        throw new UnsupportedOperationException();
    }

    default Set<String> sunion(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zdiff(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zinter(ZParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrandmember(String key, long count) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrangeByLex(String key, String min, String max) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrangeByScore(String key, double min, double max) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrangeByScore(String key, String min, String max) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrange(String key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrevrangeByLex(String key, String max, String min) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrevrangeByScore(String key, double max, double min) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrevrangeByScore(String key, String max, String min) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zrevrange(String key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Set<String> zunion(ZParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zdiffWithScores(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zdiffWithScores(String... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zinterWithScores(ZParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zinterWithScores(ZParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zpopmax(byte[] key, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zpopmax(String key, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zpopmin(byte[] key, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zpopmin(String key, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrandmemberWithScores(byte[] key, long count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrandmemberWithScores(String key, long count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeByScoreWithScores(byte[] key, byte[] min, byte[] max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeWithScores(byte[] key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrangeWithScores(String key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeByScoreWithScores(byte[] key, byte[] max, byte[] min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeWithScores(byte[] key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zunionWithScores(ZParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default Set<Tuple> zunionWithScores(ZParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    default StreamEntryID xadd(String key, Map<String, String> hash, XAddParams params) {
        throw new UnsupportedOperationException();
    }

    default StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash) {
        throw new UnsupportedOperationException();
    }

    default StreamEntryID xadd(String key, StreamEntryID id, Map<String, String> hash, long maxLen, boolean approximateLength) {
        throw new UnsupportedOperationException();
    }

    default StreamInfo xinfoStream(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default StreamInfo xinfoStream(String key) {
        throw new UnsupportedOperationException();
    }

    default StreamPendingSummary xpending(String key, String groupname) {
        throw new UnsupportedOperationException();
    }

    default String aclGenPass() {
        throw new UnsupportedOperationException();
    }

    default String aclLoad() {
        throw new UnsupportedOperationException();
    }

    default String aclLog(String options) {
        throw new UnsupportedOperationException();
    }

    default String aclSave() {
        throw new UnsupportedOperationException();
    }

    default String aclSetUser(byte[] name) {
        throw new UnsupportedOperationException();
    }

    default String aclSetUser(byte[] name, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default String aclSetUser(String name) {
        throw new UnsupportedOperationException();
    }

    default String aclSetUser(String name, String... keys) {
        throw new UnsupportedOperationException();
    }

    default String aclWhoAmI() {
        throw new UnsupportedOperationException();
    }

    default String auth(String password) {
        throw new UnsupportedOperationException();
    }

    default String auth(String user, String password) {
        throw new UnsupportedOperationException();
    }

    default String bgrewriteaof() {
        throw new UnsupportedOperationException();
    }

    default String bgsave() {
        throw new UnsupportedOperationException();
    }

    default String blmove(String srcKey, String dstKey, ListDirection from, ListDirection to, double timeout) {
        throw new UnsupportedOperationException();
    }

    default String brpoplpush(String source, String destination, int timeout) {
        throw new UnsupportedOperationException();
    }

    default String clientGetname() {
        throw new UnsupportedOperationException();
    }

    default String clientInfo() {
        throw new UnsupportedOperationException();
    }

    default String clientKill(byte[] ipPort) {
        throw new UnsupportedOperationException();
    }

    default String clientKill(String ip, int port) {
        throw new UnsupportedOperationException();
    }

    default String clientKill(String ipPort) {
        throw new UnsupportedOperationException();
    }

    default String clientList() {
        throw new UnsupportedOperationException();
    }

    default String clientList(long... clientIds) {
        throw new UnsupportedOperationException();
    }

    default String clientSetname(byte[] name) {
        throw new UnsupportedOperationException();
    }

    default String clientSetname(String name) {
        throw new UnsupportedOperationException();
    }

    default String clusterAddSlots(int... slots) {
        throw new UnsupportedOperationException();
    }

    default String clusterDelSlots(int... slots) {
        throw new UnsupportedOperationException();
    }

    default String clusterFailover() {
        throw new UnsupportedOperationException();
    }

    default String clusterFlushSlots() {
        throw new UnsupportedOperationException();
    }

    default String clusterForget(String nodeId) {
        throw new UnsupportedOperationException();
    }

    default String clusterInfo() {
        throw new UnsupportedOperationException();
    }

    default String clusterMeet(String ip, int port) {
        throw new UnsupportedOperationException();
    }

    default String clusterNodes() {
        throw new UnsupportedOperationException();
    }

    default String clusterReplicate(String nodeId) {
        throw new UnsupportedOperationException();
    }

    default String clusterReset(ClusterReset resetType) {
        throw new UnsupportedOperationException();
    }

    default String clusterSaveConfig() {
        throw new UnsupportedOperationException();
    }

    default String clusterSetSlotImporting(int slot, String nodeId) {
        throw new UnsupportedOperationException();
    }

    default String clusterSetSlotMigrating(int slot, String nodeId) {
        throw new UnsupportedOperationException();
    }

    default String clusterSetSlotNode(int slot, String nodeId) {
        throw new UnsupportedOperationException();
    }

    default String clusterSetSlotStable(int slot) {
        throw new UnsupportedOperationException();
    }

    default String configResetStat() {
        throw new UnsupportedOperationException();
    }

    default String configRewrite() {
        throw new UnsupportedOperationException();
    }

    default String configSet(String parameter, String value) {
        throw new UnsupportedOperationException();
    }

    default String debug(DebugParams params) {
        throw new UnsupportedOperationException();
    }

    default String echo(String string) {
        throw new UnsupportedOperationException();
    }

    default String flushAll() {
        throw new UnsupportedOperationException();
    }

    default String flushAll(FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    default String flushDB() {
        throw new UnsupportedOperationException();
    }

    default String flushDB(FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    default String getDel(String key) {
        throw new UnsupportedOperationException();
    }

    default String getEx(String key, GetExParams params) {
        throw new UnsupportedOperationException();
    }

    default String getrange(String key, long startOffset, long endOffset) {
        throw new UnsupportedOperationException();
    }

    default String getSet(String key, String value) {
        throw new UnsupportedOperationException();
    }

    default String get(String key) {
        throw new UnsupportedOperationException();
    }

    default String hget(String key, String field) {
        throw new UnsupportedOperationException();
    }

    default String hmset(byte[] key, Map<byte[], byte[]> hash) {
        throw new UnsupportedOperationException();
    }

    default String hmset(String key, Map<String, String> hash) {
        throw new UnsupportedOperationException();
    }

    default String hrandfield(String key) {
        throw new UnsupportedOperationException();
    }

    default String info() {
        throw new UnsupportedOperationException();
    }

    default String info(String section) {
        throw new UnsupportedOperationException();
    }

    default String lindex(String key, long index) {
        throw new UnsupportedOperationException();
    }

    default String lmove(String srcKey, String dstKey, ListDirection from, ListDirection to) {
        throw new UnsupportedOperationException();
    }

    default String lpop(String key) {
        throw new UnsupportedOperationException();
    }

    default String lset(byte[] key, long index, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default String lset(String key, long index, String value) {
        throw new UnsupportedOperationException();
    }

    default String ltrim(byte[] key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default String ltrim(String key, long start, long stop) {
        throw new UnsupportedOperationException();
    }

    default String memoryDoctor() {
        throw new UnsupportedOperationException();
    }

    default String migrate(String host, int port, byte[] key, int destinationDB, int timeout) {
        throw new UnsupportedOperationException();
    }

    default String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default String migrate(String host, int port, int destinationDB, int timeout, MigrateParams params, String... keys) {
        throw new UnsupportedOperationException();
    }

    default String migrate(String host, int port, String key, int destinationDB, int timeout) {
        throw new UnsupportedOperationException();
    }

    default String moduleLoad(String path) {
        throw new UnsupportedOperationException();
    }

    default String moduleUnload(String name) {
        throw new UnsupportedOperationException();
    }

    default String mset(byte[]... keysvalues) {
        throw new UnsupportedOperationException();
    }

    default String mset(String... keysvalues) {
        throw new UnsupportedOperationException();
    }

    default String objectEncoding(String key) {
        throw new UnsupportedOperationException();
    }

    default String pfmerge(byte[] destkey, byte[]... sourcekeys) {
        throw new UnsupportedOperationException();
    }

    default String pfmerge(String destkey, String... sourcekeys) {
        throw new UnsupportedOperationException();
    }

    default String ping() {
        throw new UnsupportedOperationException();
    }

    default String psetex(byte[] key, long milliseconds, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default String psetex(String key, long milliseconds, String value) {
        throw new UnsupportedOperationException();
    }

    default String quit() {
        throw new UnsupportedOperationException();
    }

    default String randomKey() {
        throw new UnsupportedOperationException();
    }

    default String readonly() {
        throw new UnsupportedOperationException();
    }

    default String rename(byte[] oldkey, byte[] newkey) {
        throw new UnsupportedOperationException();
    }

    default String rename(String oldkey, String newkey) {
        throw new UnsupportedOperationException();
    }

    default String restore(byte[] key, long ttl, byte[] serializedValue) {
        throw new UnsupportedOperationException();
    }

    default String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params) {
        throw new UnsupportedOperationException();
    }

    default String restoreReplace(byte[] key, long ttl, byte[] serializedValue) {
        throw new UnsupportedOperationException();
    }

    default String restoreReplace(String key, long ttl, byte[] serializedValue) {
        throw new UnsupportedOperationException();
    }

    default String restore(String key, long ttl, byte[] serializedValue) {
        throw new UnsupportedOperationException();
    }

    default String restore(String key, long ttl, byte[] serializedValue, RestoreParams params) {
        throw new UnsupportedOperationException();
    }

    default String rpoplpush(String srckey, String dstkey) {
        throw new UnsupportedOperationException();
    }

    default String rpop(String key) {
        throw new UnsupportedOperationException();
    }

    default String save() {
        throw new UnsupportedOperationException();
    }

    default String scriptFlush() {
        throw new UnsupportedOperationException();
    }

    default String scriptFlush(byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    default String scriptFlush(byte[] sampleKey, FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    default String scriptFlush(FlushMode flushMode) {
        throw new UnsupportedOperationException();
    }

    default String scriptFlush(String sampleKey) {
        throw new UnsupportedOperationException();
    }

    default String scriptKill() {
        throw new UnsupportedOperationException();
    }

    default String scriptKill(byte[] sampleKey) {
        throw new UnsupportedOperationException();
    }

    default String scriptKill(String sampleKey) {
        throw new UnsupportedOperationException();
    }

    default String scriptLoad(String script) {
        throw new UnsupportedOperationException();
    }

    default String scriptLoad(String script, String sampleKey) {
        throw new UnsupportedOperationException();
    }

    default String select(int index) {
        throw new UnsupportedOperationException();
    }

    default String sentinelFailover(String masterName) {
        throw new UnsupportedOperationException();
    }

    default String sentinelMonitor(String masterName, String ip, int port, int quorum) {
        throw new UnsupportedOperationException();
    }

    default String sentinelRemove(String masterName) {
        throw new UnsupportedOperationException();
    }

    default String sentinelSet(String masterName, Map<String, String> parameterMap) {
        throw new UnsupportedOperationException();
    }

    default String set(byte[] key, byte[] value) {
        throw new UnsupportedOperationException();
    }

    default String set(byte[] key, byte[] value, SetParams params) {
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

    default String set(String key, String value, SetParams params) {
        throw new UnsupportedOperationException();
    }

    default String shutdown() {
        throw new UnsupportedOperationException();
    }

    default String slaveofNoOne() {
        throw new UnsupportedOperationException();
    }

    default String slaveof(String host, int port) {
        throw new UnsupportedOperationException();
    }

    default String slowlogReset() {
        throw new UnsupportedOperationException();
    }

    default String spop(String key) {
        throw new UnsupportedOperationException();
    }

    default String srandmember(String key) {
        throw new UnsupportedOperationException();
    }

    default String substr(String key, int start, int end) {
        throw new UnsupportedOperationException();
    }

    default String swapDB(int index1, int index2) {
        throw new UnsupportedOperationException();
    }

    default String type(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default String type(String key) {
        throw new UnsupportedOperationException();
    }

    default String unwatch() {
        throw new UnsupportedOperationException();
    }

    default String watch(byte[]... keys) {
        throw new UnsupportedOperationException();
    }

    default String watch(String... keys) {
        throw new UnsupportedOperationException();
    }

    default String xgroupCreate(byte[] key, byte[] consumer, byte[] id, boolean makeStream) {
        throw new UnsupportedOperationException();
    }

    default String xgroupCreate(String key, String groupname, StreamEntryID id, boolean makeStream) {
        throw new UnsupportedOperationException();
    }

    default String xgroupSetID(byte[] key, byte[] consumer, byte[] id) {
        throw new UnsupportedOperationException();
    }

    default String xgroupSetID(String key, String groupname, StreamEntryID id) {
        throw new UnsupportedOperationException();
    }

    default String zrandmember(String key) {
        throw new UnsupportedOperationException();
    }

    default Tuple zpopmax(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Tuple zpopmax(String key) {
        throw new UnsupportedOperationException();
    }

    default Tuple zpopmin(byte[] key) {
        throw new UnsupportedOperationException();
    }

    default Tuple zpopmin(String key) {
        throw new UnsupportedOperationException();
    }

    default void psubscribe(BinaryJedisPubSub jedisPubSub, byte[]... patterns) {
        throw new UnsupportedOperationException();
    }

    default void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        throw new UnsupportedOperationException();
    }

    default void subscribe(BinaryJedisPubSub jedisPubSub, byte[]... channels) {
        throw new UnsupportedOperationException();
    }

    default void subscribe(JedisPubSub jedisPubSub, String... channels) {
        throw new UnsupportedOperationException();
    }

}
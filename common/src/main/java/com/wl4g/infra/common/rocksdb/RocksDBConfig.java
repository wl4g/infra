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
package com.wl4g.infra.common.rocksdb;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.rocksdb.AbstractCompactionFilter;
import org.rocksdb.AbstractCompactionFilterFactory;
import org.rocksdb.AbstractComparator;
import org.rocksdb.AbstractEventListener;
import org.rocksdb.AbstractWalFilter;
import org.rocksdb.BuiltinComparator;
import org.rocksdb.Cache;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.CompactionOptionsFIFO;
import org.rocksdb.CompactionOptionsUniversal;
import org.rocksdb.CompactionPriority;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionOptions;
import org.rocksdb.CompressionType;
import org.rocksdb.ConcurrentTaskLimiter;
import org.rocksdb.DBOptions;
import org.rocksdb.DbPath;
import org.rocksdb.InfoLogLevel;
import org.rocksdb.Logger;
import org.rocksdb.MemTableConfig;
import org.rocksdb.MergeOperator;
import org.rocksdb.Options;
import org.rocksdb.RateLimiter;
import org.rocksdb.SstFileManager;
import org.rocksdb.SstPartitionerFactory;
import org.rocksdb.TableFormatConfig;
import org.rocksdb.WALRecoveryMode;
import org.rocksdb.WriteBufferManager;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link RocksDBConfig}
 * 
 * @author James Wong
 * @version 2022-11-01
 * @since v3.0.0
 * @see https://github1s.com/facebook/rocksdb/blob/v6.20.3/java/src/test/java/org/rocksdb/RocksIteratorTest.java#L25-L26
 */
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
public class RocksDBConfig {

    private @Default File dataDir = new File("/tmp/rocksdb/data-" + DEFAULT_SUFFIX);

    private @Default Boolean createDBIfMissing = true;

    private Cache rawCache;

    // see:https://github1s.com/apache/flink/blob/release-1.15.1/flink-state-backends/flink-statebackend-rocksdb/src/main/java/org/apache/flink/contrib/streaming/state/RocksDBResource.java#L186-L113
    private @Default Long blockCacheSizeMb = 40960L;

    private Long memtableMemoryBudget;

    private BuiltinComparator builtinComparator;

    private AbstractComparator comparator;

    private String MergeOperatorName;

    private MergeOperator mergeOperator;

    private AbstractCompactionFilterFactory<? extends AbstractCompactionFilter<?>> compactionFilterFactory;

    private Long writeBufferSize;

    private Integer maxWriteBufferNumber;

    private Boolean errorIfExists;

    private Boolean paranoidChecks;

    private Integer maxFileOpeningThreads;

    private Long maxTotalWalSize;

    private @Default Integer maxOpenFiles = -1;

    private @Default Boolean useFsync = true;

    private Collection<DbPath> dbPaths;

    private @Default String dbLogDir = "/tmp/rocksdb/log-" + DEFAULT_SUFFIX;

    private @Default String walDir = "/tmp/rocksdb/wal-" + DEFAULT_SUFFIX;

    private Long deleteObsoleteFilesPeriodMicros;

    private Integer maxSubcompactions;

    private @Default Integer maxBackgroundJobs = 4;

    private Long maxLogFileSize;

    private Long logFileTimeToRoll;

    private Long keepLogFileNum;

    private Long recycleLogFileNum;

    private Long maxManifestFileSize;

    private Long maxTableFilesSizeFIFO;

    private Integer tableCacheNumshardbits;

    private Long walTtlSeconds;

    private Long maxWriteBatchGroupSizeBytes;

    private Long walSizeLimitMB;

    private Long manifestPreallocationSize;

    private Boolean useDirectReads;

    private Boolean useDirectIoForFlushAndCompaction;

    private Boolean allowFAllocate;

    private Boolean allowMmapReads;

    private Boolean allowMmapWrites;

    private Boolean isFdCloseOnExec;

    private @Default Integer statsDumpPeriodSec = 0;

    private Integer statsPersistPeriodSec;

    private Long statsHistoryBufferSize;

    private Boolean adviseRandomOnOpen;

    private Long dbWriteBufferSize;

    private WriteBufferManager writeBufferManager;

    private Boolean newTableReaderForCompactionInputs;

    private Long compactionReadaheadSize;

    private Long randomAccessMaxBufferSize;

    private Long writableFileMaxBufferSize;

    private Boolean useAdaptiveMutex;

    private Long bytesPerSync;

    private Long walBytesPerSync;

    private Boolean strictBytesPerSync;

    private List<AbstractEventListener> listeners;

    private Boolean enableThreadTracking;

    private Long delayedWriteRate;

    private Boolean enablePipelinedWrite;

    private Boolean unorderedWrite;

    private Boolean allowConcurrentMemtableWrite;

    private Boolean enableWriteThreadAdaptiveYield;

    private Long writeThreadMaxYieldUsec;

    private Long writeThreadSlowYieldUsec;

    private Boolean skipStatsUpdateOnDbOpen;

    private Boolean skipCheckingSstFileSizesOnDbOpen;

    private WALRecoveryMode walRecoveryMode;

    private Boolean allow2pc;

    private Cache rowCache;

    private AbstractWalFilter walFilter;

    private Boolean failIfOptionsFileError;

    private Boolean dumpMallocStats;

    private Boolean avoidFlushDuringRecovery;

    private Boolean avoidFlushDuringShutdown;

    private Boolean allowIngestBehind;

    private Boolean preserveDeletes;

    private Boolean twoWriteQueues;

    private Boolean manualWalFlush;

    private MemTableConfig memTableConfig;

    private RateLimiter rateLimiter;

    private SstFileManager sstFileManager;

    private Logger logger;

    private @Default InfoLogLevel infoLogLevel = InfoLogLevel.HEADER_LEVEL;

    private TableFormatConfig tableFormatConfig;

    private Collection<DbPath> cfPaths;

    private Integer useFixedLengthPrefixExtractor;

    private Integer useCappedPrefixExtractor;

    private List<CompressionType> compressionPerLevel;

    private CompressionType compressionType;

    private CompressionType bottommostCompressionType;

    private CompressionOptions bottommostCompressionOptions;

    private CompressionOptions compressionOptions;

    private CompactionStyle compactionStyle;

    private Integer numLevels;

    private Integer levelZeroFileNumCompactionTrigger;

    private Integer levelZeroSlowdownWritesTrigger;

    private Long targetFileSizeBase;

    private Long maxBytesForLevelBase;

    private Boolean enableLevelCompactionDynamicLevelBytes;

    private Double maxBytesForLevelMultiplier;

    private Long maxCompactionBytes;

    private Long arenaBlockSize;

    private Boolean disableAutoCompactions;

    private Long maxSequentialSkipInIterations;

    private Boolean inplaceUpdateSupport;

    private Long inplaceUpdateNumLocks;

    private Double memtablePrefixBloomSizeRatio;

    private Integer bloomLocality;

    private Long maxSuccessiveMerges;

    private Integer minWriteBufferNumberToMerge;

    private Boolean optimizeFiltersForHits;

    private Long memtableHugePageSize;

    private Long softPendingCompactionBytesLimit;

    private Long hardPendingCompactionBytesLimit;

    private Integer level0FileNumCompactionTrigger;

    private Integer level0SlowdownWritesTrigger;

    private Integer level0StopWritesTrigger;

    private int[] maxBytesForLevelMultiplierAdditional;

    private Boolean paranoidFileChecks;

    private Integer maxWriteBufferNumberToMaintain;

    private CompactionPriority compactionPriority;

    private Boolean reportBgIoStats;

    private Long ttl;

    private CompactionOptionsUniversal compactionOptionsUniversal;

    private CompactionOptionsFIFO compactionOptionsFIFO;

    private Boolean forceConsistencyChecks;

    private Boolean atomicFlush;

    private Boolean avoidUnnecessaryBlockingIO;

    private Boolean persistStatsToDisk;

    private Boolean writeDbidToManifest;

    private Long logReadaheadSize;

    private Boolean bestEffortsRecovery;

    private Integer maxBgErrorResumeCount;

    private Long bgerrorResumeRetryInterval;

    private SstPartitionerFactory sstPartitionerFactory;

    private ConcurrentTaskLimiter compactionThreadLimiter;

    public Options newOptions() {
        Options options = new Options();
        options.setCreateIfMissing(getCreateDBIfMissing());
        if (nonNull(getRawCache())) {
            options.setRowCache(getRawCache());
        }

        if (nonNull(getBlockCacheSizeMb())) {
            options.optimizeForPointLookup(getBlockCacheSizeMb());
        }

        if (nonNull(getMemtableMemoryBudget())) {
            options.optimizeUniversalStyleCompaction(getMemtableMemoryBudget());
        }

        if (nonNull(getBuiltinComparator())) {
            options.setComparator(getBuiltinComparator());
        }

        if (nonNull(getComparator())) {
            options.setComparator(getComparator());
        }

        if (nonNull(getMergeOperatorName())) {
            options.setMergeOperatorName(getMergeOperatorName());
        }

        if (nonNull(getMergeOperator())) {
            options.setMergeOperator(getMergeOperator());
        }

        if (nonNull(getCompactionFilterFactory())) {
            options.setCompactionFilterFactory(getCompactionFilterFactory());
        }

        if (nonNull(getWriteBufferSize())) {
            options.setWriteBufferSize(getWriteBufferSize());
        }

        if (nonNull(getMaxWriteBufferNumber())) {
            options.setMaxWriteBufferNumber(getMaxWriteBufferNumber());
        }

        if (nonNull(getErrorIfExists())) {
            options.setErrorIfExists(getErrorIfExists());
        }

        if (nonNull(getParanoidChecks())) {
            options.setParanoidChecks(getParanoidChecks());
        }

        if (nonNull(getMaxFileOpeningThreads())) {
            options.setMaxFileOpeningThreads(getMaxFileOpeningThreads());
        }

        if (nonNull(getMaxTotalWalSize())) {
            options.setMaxTotalWalSize(getMaxTotalWalSize());
        }

        if (nonNull(getMaxOpenFiles())) {
            options.setMaxOpenFiles(getMaxOpenFiles());
        }

        if (nonNull(getUseFsync())) {
            options.setUseFsync(getUseFsync());
        }

        if (nonNull(getDbPaths())) {
            options.setDbPaths(getDbPaths());
        }

        if (nonNull(getDbLogDir())) {
            options.setDbLogDir(getDbLogDir());
        }

        if (nonNull(getWalDir())) {
            options.setWalDir(getWalDir());
        }

        if (nonNull(getDeleteObsoleteFilesPeriodMicros())) {
            options.setDeleteObsoleteFilesPeriodMicros(getDeleteObsoleteFilesPeriodMicros());
        }

        if (nonNull(getMaxSubcompactions())) {
            options.setMaxSubcompactions(getMaxSubcompactions());
        }

        if (nonNull(getMaxBackgroundJobs())) {
            options.setMaxBackgroundJobs(getMaxBackgroundJobs());
        }

        if (nonNull(getMaxLogFileSize())) {
            options.setMaxLogFileSize(getMaxLogFileSize());
        }

        if (nonNull(getLogFileTimeToRoll())) {
            options.setLogFileTimeToRoll(getLogFileTimeToRoll());
        }

        if (nonNull(getKeepLogFileNum())) {
            options.setKeepLogFileNum(getKeepLogFileNum());
        }

        if (nonNull(getRecycleLogFileNum())) {
            options.setRecycleLogFileNum(getRecycleLogFileNum());
        }

        if (nonNull(getMaxManifestFileSize())) {
            options.setMaxManifestFileSize(getMaxManifestFileSize());
        }

        if (nonNull(getMaxTableFilesSizeFIFO())) {
            options.setMaxTableFilesSizeFIFO(getMaxTableFilesSizeFIFO());
        }

        if (nonNull(getTableCacheNumshardbits())) {
            options.setTableCacheNumshardbits(getTableCacheNumshardbits());
        }

        if (nonNull(getWalTtlSeconds())) {
            options.setWalTtlSeconds(getWalTtlSeconds());
        }

        if (nonNull(getMaxWriteBatchGroupSizeBytes())) {
            options.setMaxWriteBatchGroupSizeBytes(getMaxWriteBatchGroupSizeBytes());
        }

        if (nonNull(getWalSizeLimitMB())) {
            options.setWalSizeLimitMB(getWalSizeLimitMB());
        }

        if (nonNull(getManifestPreallocationSize())) {
            options.setManifestPreallocationSize(getManifestPreallocationSize());
        }

        if (nonNull(getUseDirectReads())) {
            options.setUseDirectReads(getUseDirectReads());
        }

        if (nonNull(getUseDirectIoForFlushAndCompaction())) {
            options.setUseDirectIoForFlushAndCompaction(getUseDirectIoForFlushAndCompaction());
        }

        if (nonNull(getAllowFAllocate())) {
            options.setAllowFAllocate(getAllowFAllocate());
        }

        if (nonNull(getAllowMmapReads())) {
            options.setAllowMmapReads(getAllowMmapReads());
        }

        if (nonNull(getAllowMmapWrites())) {
            options.setAllowMmapWrites(getAllowMmapWrites());
        }

        if (nonNull(getIsFdCloseOnExec())) {
            options.setIsFdCloseOnExec(getIsFdCloseOnExec());
        }

        if (nonNull(getStatsDumpPeriodSec())) {
            options.setStatsDumpPeriodSec(getStatsDumpPeriodSec());
        }

        if (nonNull(getStatsPersistPeriodSec())) {
            options.setStatsPersistPeriodSec(getStatsPersistPeriodSec());
        }

        if (nonNull(getStatsHistoryBufferSize())) {
            options.setStatsHistoryBufferSize(getStatsHistoryBufferSize());
        }

        if (nonNull(getAdviseRandomOnOpen())) {
            options.setAdviseRandomOnOpen(getAdviseRandomOnOpen());
        }

        if (nonNull(getDbWriteBufferSize())) {
            options.setDbWriteBufferSize(getDbWriteBufferSize());
        }

        if (nonNull(getWriteBufferManager())) {
            options.setWriteBufferManager(getWriteBufferManager());
        }

        if (nonNull(getNewTableReaderForCompactionInputs())) {
            options.setNewTableReaderForCompactionInputs(getNewTableReaderForCompactionInputs());
        }

        if (nonNull(getCompactionReadaheadSize())) {
            options.setCompactionReadaheadSize(getCompactionReadaheadSize());
        }

        if (nonNull(getRandomAccessMaxBufferSize())) {
            options.setRandomAccessMaxBufferSize(getRandomAccessMaxBufferSize());
        }

        if (nonNull(getWritableFileMaxBufferSize())) {
            options.setWritableFileMaxBufferSize(getWritableFileMaxBufferSize());
        }

        if (nonNull(getUseAdaptiveMutex())) {
            options.setUseAdaptiveMutex(getUseAdaptiveMutex());
        }

        if (nonNull(getBytesPerSync())) {
            options.setBytesPerSync(getBytesPerSync());
        }

        if (nonNull(getWalBytesPerSync())) {
            options.setWalBytesPerSync(getWalBytesPerSync());
        }

        if (nonNull(getStrictBytesPerSync())) {
            options.setStrictBytesPerSync(getStrictBytesPerSync());
        }

        if (nonNull(getListeners())) {
            options.setListeners(getListeners());
        }

        if (nonNull(getEnableThreadTracking())) {
            options.setEnableThreadTracking(getEnableThreadTracking());
        }

        if (nonNull(getDelayedWriteRate())) {
            options.setDelayedWriteRate(getDelayedWriteRate());
        }

        if (nonNull(getEnablePipelinedWrite())) {
            options.setEnablePipelinedWrite(getEnablePipelinedWrite());
        }

        if (nonNull(getUnorderedWrite())) {
            options.setUnorderedWrite(getUnorderedWrite());
        }

        if (nonNull(getAllowConcurrentMemtableWrite())) {
            options.setAllowConcurrentMemtableWrite(getAllowConcurrentMemtableWrite());
        }

        if (nonNull(getEnableWriteThreadAdaptiveYield())) {
            options.setEnableWriteThreadAdaptiveYield(getEnableWriteThreadAdaptiveYield());
        }

        if (nonNull(getWriteThreadMaxYieldUsec())) {
            options.setWriteThreadMaxYieldUsec(getWriteThreadMaxYieldUsec());
        }

        if (nonNull(getWriteThreadSlowYieldUsec())) {
            options.setWriteThreadSlowYieldUsec(getWriteThreadSlowYieldUsec());
        }

        if (nonNull(getSkipStatsUpdateOnDbOpen())) {
            options.setSkipStatsUpdateOnDbOpen(getSkipStatsUpdateOnDbOpen());
        }

        if (nonNull(getSkipCheckingSstFileSizesOnDbOpen())) {
            options.setSkipCheckingSstFileSizesOnDbOpen(getSkipCheckingSstFileSizesOnDbOpen());
        }

        if (nonNull(getWalRecoveryMode())) {
            options.setWalRecoveryMode(getWalRecoveryMode());
        }

        if (nonNull(getAllow2pc())) {
            options.setAllow2pc(getAllow2pc());
        }

        if (nonNull(getRowCache())) {
            options.setRowCache(getRowCache());
        }

        if (nonNull(getWalFilter())) {
            options.setWalFilter(getWalFilter());
        }

        if (nonNull(getFailIfOptionsFileError())) {
            options.setFailIfOptionsFileError(getFailIfOptionsFileError());
        }

        if (nonNull(getDumpMallocStats())) {
            options.setDumpMallocStats(getDumpMallocStats());
        }

        if (nonNull(getAvoidFlushDuringRecovery())) {
            options.setAvoidFlushDuringRecovery(getAvoidFlushDuringRecovery());
        }

        if (nonNull(getAvoidFlushDuringShutdown())) {
            options.setAvoidFlushDuringShutdown(getAvoidFlushDuringShutdown());
        }

        if (nonNull(getAllowIngestBehind())) {
            options.setAllowIngestBehind(getAllowIngestBehind());
        }

        if (nonNull(getPreserveDeletes())) {
            options.setPreserveDeletes(getPreserveDeletes());
        }

        if (nonNull(getTwoWriteQueues())) {
            options.setTwoWriteQueues(getTwoWriteQueues());
        }

        if (nonNull(getManualWalFlush())) {
            options.setManualWalFlush(getManualWalFlush());
        }

        if (nonNull(getMemTableConfig())) {
            options.setMemTableConfig(getMemTableConfig());
        }

        if (nonNull(getRateLimiter())) {
            options.setRateLimiter(getRateLimiter());
        }

        if (nonNull(getSstFileManager())) {
            options.setSstFileManager(getSstFileManager());
        }

        if (nonNull(getLogger())) {
            options.setLogger(getLogger());
        }

        if (nonNull(getInfoLogLevel())) {
            options.setInfoLogLevel(getInfoLogLevel());
        }

        if (nonNull(getTableFormatConfig())) {
            options.setTableFormatConfig(getTableFormatConfig());
        }

        if (nonNull(getCfPaths())) {
            options.setCfPaths(getCfPaths());
        }

        if (nonNull(getUseFixedLengthPrefixExtractor())) {
            options.useFixedLengthPrefixExtractor(getUseFixedLengthPrefixExtractor());
        }

        if (nonNull(getUseCappedPrefixExtractor())) {
            options.useCappedPrefixExtractor(getUseCappedPrefixExtractor());
        }

        if (nonNull(getCompressionPerLevel())) {
            options.setCompressionPerLevel(getCompressionPerLevel());
        }

        if (nonNull(getCompressionType())) {
            options.setCompressionType(getCompressionType());
        }

        if (nonNull(getBottommostCompressionType())) {
            options.setBottommostCompressionType(getBottommostCompressionType());
        }

        if (nonNull(getBottommostCompressionOptions())) {
            options.setBottommostCompressionOptions(getBottommostCompressionOptions());
        }

        if (nonNull(getCompressionOptions())) {
            options.setCompressionOptions(getCompressionOptions());
        }

        if (nonNull(getCompactionStyle())) {
            options.setCompactionStyle(getCompactionStyle());
        }

        if (nonNull(getNumLevels())) {
            options.setNumLevels(getNumLevels());
        }

        if (nonNull(getLevelZeroFileNumCompactionTrigger())) {
            options.setLevelZeroFileNumCompactionTrigger(getLevelZeroFileNumCompactionTrigger());
        }

        if (nonNull(getLevel0SlowdownWritesTrigger())) {
            options.setLevel0SlowdownWritesTrigger(getLevel0SlowdownWritesTrigger());
        }

        if (nonNull(getTargetFileSizeBase())) {
            options.setTargetFileSizeBase(getTargetFileSizeBase());
        }

        if (nonNull(getMaxBytesForLevelBase())) {
            options.setMaxBytesForLevelBase(getMaxBytesForLevelBase());
        }

        if (nonNull(getEnableLevelCompactionDynamicLevelBytes())) {
            options.setLevelCompactionDynamicLevelBytes(getEnableLevelCompactionDynamicLevelBytes());
        }

        if (nonNull(getMaxBytesForLevelMultiplier())) {
            options.setMaxBytesForLevelMultiplier(getMaxBytesForLevelMultiplier());
        }

        if (nonNull(getMaxCompactionBytes())) {
            options.setMaxCompactionBytes(getMaxCompactionBytes());
        }

        if (nonNull(getArenaBlockSize())) {
            options.setArenaBlockSize(getArenaBlockSize());
        }

        if (nonNull(getDisableAutoCompactions())) {
            options.setDisableAutoCompactions(getDisableAutoCompactions());
        }

        if (nonNull(getMaxSequentialSkipInIterations())) {
            options.setMaxSequentialSkipInIterations(getMaxSequentialSkipInIterations());
        }

        if (nonNull(getInplaceUpdateSupport())) {
            options.setInplaceUpdateSupport(getInplaceUpdateSupport());
        }

        if (nonNull(getInplaceUpdateNumLocks())) {
            options.setInplaceUpdateNumLocks(getInplaceUpdateNumLocks());
        }

        if (nonNull(getMemtablePrefixBloomSizeRatio())) {
            options.setMemtablePrefixBloomSizeRatio(getMemtablePrefixBloomSizeRatio());
        }

        if (nonNull(getBloomLocality())) {
            options.setBloomLocality(getBloomLocality());
        }

        if (nonNull(getMaxSuccessiveMerges())) {
            options.setMaxSuccessiveMerges(getMaxSuccessiveMerges());
        }

        if (nonNull(getMinWriteBufferNumberToMerge())) {
            options.setMinWriteBufferNumberToMerge(getMinWriteBufferNumberToMerge());
        }

        if (nonNull(getOptimizeFiltersForHits())) {
            options.setOptimizeFiltersForHits(getOptimizeFiltersForHits());
        }

        if (nonNull(getMemtableHugePageSize())) {
            options.setMemtableHugePageSize(getMemtableHugePageSize());
        }

        if (nonNull(getSoftPendingCompactionBytesLimit())) {
            options.setSoftPendingCompactionBytesLimit(getSoftPendingCompactionBytesLimit());
        }

        if (nonNull(getHardPendingCompactionBytesLimit())) {
            options.setHardPendingCompactionBytesLimit(getHardPendingCompactionBytesLimit());
        }

        if (nonNull(getLevel0FileNumCompactionTrigger())) {
            options.setLevel0FileNumCompactionTrigger(getLevel0FileNumCompactionTrigger());
        }

        if (nonNull(getLevel0SlowdownWritesTrigger())) {
            options.setLevel0SlowdownWritesTrigger(getLevel0SlowdownWritesTrigger());
        }

        if (nonNull(getLevel0StopWritesTrigger())) {
            options.setLevel0StopWritesTrigger(getLevel0StopWritesTrigger());
        }

        if (nonNull(getMaxBytesForLevelMultiplierAdditional())) {
            options.setMaxBytesForLevelMultiplierAdditional(getMaxBytesForLevelMultiplierAdditional());
        }

        if (nonNull(getParanoidFileChecks())) {
            options.setParanoidFileChecks(getParanoidFileChecks());
        }

        if (nonNull(getMaxWriteBufferNumberToMaintain())) {
            options.setMaxWriteBufferNumberToMaintain(getMaxWriteBufferNumberToMaintain());
        }

        if (nonNull(getCompactionPriority())) {
            options.setCompactionPriority(getCompactionPriority());
        }

        if (nonNull(getReportBgIoStats())) {
            options.setReportBgIoStats(getReportBgIoStats());
        }

        if (nonNull(getTtl())) {
            options.setTtl(getTtl());
        }

        if (nonNull(getCompactionOptionsUniversal())) {
            options.setCompactionOptionsUniversal(getCompactionOptionsUniversal());
        }

        if (nonNull(getCompactionOptionsFIFO())) {
            options.setCompactionOptionsFIFO(getCompactionOptionsFIFO());
        }

        if (nonNull(getForceConsistencyChecks())) {
            options.setForceConsistencyChecks(getForceConsistencyChecks());
        }

        if (nonNull(getAtomicFlush())) {
            options.setAtomicFlush(getAtomicFlush());
        }

        if (nonNull(getAvoidUnnecessaryBlockingIO())) {
            options.setAvoidUnnecessaryBlockingIO(getAvoidUnnecessaryBlockingIO());
        }

        if (nonNull(getPersistStatsToDisk())) {
            options.setPersistStatsToDisk(getPersistStatsToDisk());
        }

        if (nonNull(getWriteDbidToManifest())) {
            options.setWriteDbidToManifest(getWriteDbidToManifest());
        }

        if (nonNull(getLogReadaheadSize())) {
            options.setLogReadaheadSize(getLogReadaheadSize());
        }

        if (nonNull(getBestEffortsRecovery())) {
            options.setBestEffortsRecovery(getBestEffortsRecovery());
        }

        if (nonNull(getMaxBgErrorResumeCount())) {
            options.setMaxBgErrorResumeCount(getMaxBgErrorResumeCount());
        }

        if (nonNull(getBgerrorResumeRetryInterval())) {
            options.setBgerrorResumeRetryInterval(getBgerrorResumeRetryInterval());
        }

        if (nonNull(getSstPartitionerFactory())) {
            options.setSstPartitionerFactory(getSstPartitionerFactory());
        }

        if (nonNull(getCompactionThreadLimiter())) {
            options.setCompactionThreadLimiter(getCompactionThreadLimiter());
        }

        return options;
    }

    public ColumnFamilyOptions newColumnFamilyOptions() {
        ColumnFamilyOptions options = new ColumnFamilyOptions();

        if (nonNull(getBlockCacheSizeMb())) {
            options.optimizeForPointLookup(getBlockCacheSizeMb());
        }

        if (nonNull(getMemtableMemoryBudget())) {
            options.optimizeUniversalStyleCompaction(getMemtableMemoryBudget());
        }

        if (nonNull(getBuiltinComparator())) {
            options.setComparator(getBuiltinComparator());
        }

        if (nonNull(getComparator())) {
            options.setComparator(getComparator());
        }

        if (nonNull(getMergeOperatorName())) {
            options.setMergeOperatorName(getMergeOperatorName());
        }

        if (nonNull(getMergeOperator())) {
            options.setMergeOperator(getMergeOperator());
        }

        if (nonNull(getCompactionFilterFactory())) {
            options.setCompactionFilterFactory(getCompactionFilterFactory());
        }

        if (nonNull(getWriteBufferSize())) {
            options.setWriteBufferSize(getWriteBufferSize());
        }

        if (nonNull(getMaxWriteBufferNumber())) {
            options.setMaxWriteBufferNumber(getMaxWriteBufferNumber());
        }

        if (nonNull(getMaxTableFilesSizeFIFO())) {
            options.setMaxTableFilesSizeFIFO(getMaxTableFilesSizeFIFO());
        }

        if (nonNull(getTableFormatConfig())) {
            options.setTableFormatConfig(getTableFormatConfig());
        }

        if (nonNull(getCfPaths())) {
            options.setCfPaths(getCfPaths());
        }

        if (nonNull(getUseFixedLengthPrefixExtractor())) {
            options.useFixedLengthPrefixExtractor(getUseFixedLengthPrefixExtractor());
        }

        if (nonNull(getUseCappedPrefixExtractor())) {
            options.useCappedPrefixExtractor(getUseCappedPrefixExtractor());
        }

        if (nonNull(getCompressionPerLevel())) {
            options.setCompressionPerLevel(getCompressionPerLevel());
        }

        if (nonNull(getCompressionType())) {
            options.setCompressionType(getCompressionType());
        }

        if (nonNull(getBottommostCompressionType())) {
            options.setBottommostCompressionType(getBottommostCompressionType());
        }

        if (nonNull(getBottommostCompressionOptions())) {
            options.setBottommostCompressionOptions(getBottommostCompressionOptions());
        }

        if (nonNull(getCompressionOptions())) {
            options.setCompressionOptions(getCompressionOptions());
        }

        if (nonNull(getCompactionStyle())) {
            options.setCompactionStyle(getCompactionStyle());
        }

        if (nonNull(getNumLevels())) {
            options.setNumLevels(getNumLevels());
        }

        if (nonNull(getLevelZeroFileNumCompactionTrigger())) {
            options.setLevelZeroFileNumCompactionTrigger(getLevelZeroFileNumCompactionTrigger());
        }

        if (nonNull(getLevel0SlowdownWritesTrigger())) {
            options.setLevel0SlowdownWritesTrigger(getLevel0SlowdownWritesTrigger());
        }

        if (nonNull(getTargetFileSizeBase())) {
            options.setTargetFileSizeBase(getTargetFileSizeBase());
        }

        if (nonNull(getMaxBytesForLevelBase())) {
            options.setMaxBytesForLevelBase(getMaxBytesForLevelBase());
        }

        if (nonNull(getEnableLevelCompactionDynamicLevelBytes())) {
            options.setLevelCompactionDynamicLevelBytes(getEnableLevelCompactionDynamicLevelBytes());
        }

        if (nonNull(getMaxBytesForLevelMultiplier())) {
            options.setMaxBytesForLevelMultiplier(getMaxBytesForLevelMultiplier());
        }

        if (nonNull(getMaxCompactionBytes())) {
            options.setMaxCompactionBytes(getMaxCompactionBytes());
        }

        if (nonNull(getArenaBlockSize())) {
            options.setArenaBlockSize(getArenaBlockSize());
        }

        if (nonNull(getDisableAutoCompactions())) {
            options.setDisableAutoCompactions(getDisableAutoCompactions());
        }

        if (nonNull(getMaxSequentialSkipInIterations())) {
            options.setMaxSequentialSkipInIterations(getMaxSequentialSkipInIterations());
        }

        if (nonNull(getInplaceUpdateSupport())) {
            options.setInplaceUpdateSupport(getInplaceUpdateSupport());
        }

        if (nonNull(getInplaceUpdateNumLocks())) {
            options.setInplaceUpdateNumLocks(getInplaceUpdateNumLocks());
        }

        if (nonNull(getMemtablePrefixBloomSizeRatio())) {
            options.setMemtablePrefixBloomSizeRatio(getMemtablePrefixBloomSizeRatio());
        }

        if (nonNull(getBloomLocality())) {
            options.setBloomLocality(getBloomLocality());
        }

        if (nonNull(getMaxSuccessiveMerges())) {
            options.setMaxSuccessiveMerges(getMaxSuccessiveMerges());
        }

        if (nonNull(getMinWriteBufferNumberToMerge())) {
            options.setMinWriteBufferNumberToMerge(getMinWriteBufferNumberToMerge());
        }

        if (nonNull(getOptimizeFiltersForHits())) {
            options.setOptimizeFiltersForHits(getOptimizeFiltersForHits());
        }

        if (nonNull(getMemtableHugePageSize())) {
            options.setMemtableHugePageSize(getMemtableHugePageSize());
        }

        if (nonNull(getSoftPendingCompactionBytesLimit())) {
            options.setSoftPendingCompactionBytesLimit(getSoftPendingCompactionBytesLimit());
        }

        if (nonNull(getHardPendingCompactionBytesLimit())) {
            options.setHardPendingCompactionBytesLimit(getHardPendingCompactionBytesLimit());
        }

        if (nonNull(getLevel0FileNumCompactionTrigger())) {
            options.setLevel0FileNumCompactionTrigger(getLevel0FileNumCompactionTrigger());
        }

        if (nonNull(getLevel0SlowdownWritesTrigger())) {
            options.setLevel0SlowdownWritesTrigger(getLevel0SlowdownWritesTrigger());
        }

        if (nonNull(getLevel0StopWritesTrigger())) {
            options.setLevel0StopWritesTrigger(getLevel0StopWritesTrigger());
        }

        if (nonNull(getMaxBytesForLevelMultiplierAdditional())) {
            options.setMaxBytesForLevelMultiplierAdditional(getMaxBytesForLevelMultiplierAdditional());
        }

        if (nonNull(getParanoidFileChecks())) {
            options.setParanoidFileChecks(getParanoidFileChecks());
        }

        if (nonNull(getMaxWriteBufferNumberToMaintain())) {
            options.setMaxWriteBufferNumberToMaintain(getMaxWriteBufferNumberToMaintain());
        }

        if (nonNull(getCompactionPriority())) {
            options.setCompactionPriority(getCompactionPriority());
        }

        if (nonNull(getReportBgIoStats())) {
            options.setReportBgIoStats(getReportBgIoStats());
        }

        if (nonNull(getTtl())) {
            options.setTtl(getTtl());
        }

        if (nonNull(getCompactionOptionsUniversal())) {
            options.setCompactionOptionsUniversal(getCompactionOptionsUniversal());
        }

        if (nonNull(getCompactionOptionsFIFO())) {
            options.setCompactionOptionsFIFO(getCompactionOptionsFIFO());
        }

        if (nonNull(getForceConsistencyChecks())) {
            options.setForceConsistencyChecks(getForceConsistencyChecks());
        }

        if (nonNull(getSstPartitionerFactory())) {
            options.setSstPartitionerFactory(getSstPartitionerFactory());
        }

        if (nonNull(getCompactionThreadLimiter())) {
            options.setCompactionThreadLimiter(getCompactionThreadLimiter());
        }

        return options;
    }

    public DBOptions newDBOptions() {
        DBOptions options = new DBOptions();
        options.setCreateIfMissing(getCreateDBIfMissing());
        if (nonNull(getRawCache())) {
            options.setRowCache(getRawCache());
        }

        if (nonNull(getErrorIfExists())) {
            options.setErrorIfExists(getErrorIfExists());
        }

        if (nonNull(getParanoidChecks())) {
            options.setParanoidChecks(getParanoidChecks());
        }

        if (nonNull(getMaxFileOpeningThreads())) {
            options.setMaxFileOpeningThreads(getMaxFileOpeningThreads());
        }

        if (nonNull(getMaxTotalWalSize())) {
            options.setMaxTotalWalSize(getMaxTotalWalSize());
        }

        if (nonNull(getMaxOpenFiles())) {
            options.setMaxOpenFiles(getMaxOpenFiles());
        }

        if (nonNull(getUseFsync())) {
            options.setUseFsync(getUseFsync());
        }

        if (nonNull(getDbPaths())) {
            options.setDbPaths(getDbPaths());
        }

        if (nonNull(getDbLogDir())) {
            options.setDbLogDir(getDbLogDir());
        }

        if (nonNull(getWalDir())) {
            options.setWalDir(getWalDir());
        }

        if (nonNull(getDeleteObsoleteFilesPeriodMicros())) {
            options.setDeleteObsoleteFilesPeriodMicros(getDeleteObsoleteFilesPeriodMicros());
        }

        if (nonNull(getMaxSubcompactions())) {
            options.setMaxSubcompactions(getMaxSubcompactions());
        }

        if (nonNull(getMaxBackgroundJobs())) {
            options.setMaxBackgroundJobs(getMaxBackgroundJobs());
        }

        if (nonNull(getMaxLogFileSize())) {
            options.setMaxLogFileSize(getMaxLogFileSize());
        }

        if (nonNull(getLogFileTimeToRoll())) {
            options.setLogFileTimeToRoll(getLogFileTimeToRoll());
        }

        if (nonNull(getKeepLogFileNum())) {
            options.setKeepLogFileNum(getKeepLogFileNum());
        }

        if (nonNull(getRecycleLogFileNum())) {
            options.setRecycleLogFileNum(getRecycleLogFileNum());
        }

        if (nonNull(getMaxManifestFileSize())) {
            options.setMaxManifestFileSize(getMaxManifestFileSize());
        }

        if (nonNull(getTableCacheNumshardbits())) {
            options.setTableCacheNumshardbits(getTableCacheNumshardbits());
        }

        if (nonNull(getWalTtlSeconds())) {
            options.setWalTtlSeconds(getWalTtlSeconds());
        }

        if (nonNull(getMaxWriteBatchGroupSizeBytes())) {
            options.setMaxWriteBatchGroupSizeBytes(getMaxWriteBatchGroupSizeBytes());
        }

        if (nonNull(getWalSizeLimitMB())) {
            options.setWalSizeLimitMB(getWalSizeLimitMB());
        }

        if (nonNull(getManifestPreallocationSize())) {
            options.setManifestPreallocationSize(getManifestPreallocationSize());
        }

        if (nonNull(getUseDirectReads())) {
            options.setUseDirectReads(getUseDirectReads());
        }

        if (nonNull(getUseDirectIoForFlushAndCompaction())) {
            options.setUseDirectIoForFlushAndCompaction(getUseDirectIoForFlushAndCompaction());
        }

        if (nonNull(getAllowFAllocate())) {
            options.setAllowFAllocate(getAllowFAllocate());
        }

        if (nonNull(getAllowMmapReads())) {
            options.setAllowMmapReads(getAllowMmapReads());
        }

        if (nonNull(getAllowMmapWrites())) {
            options.setAllowMmapWrites(getAllowMmapWrites());
        }

        if (nonNull(getIsFdCloseOnExec())) {
            options.setIsFdCloseOnExec(getIsFdCloseOnExec());
        }

        if (nonNull(getStatsDumpPeriodSec())) {
            options.setStatsDumpPeriodSec(getStatsDumpPeriodSec());
        }

        if (nonNull(getStatsPersistPeriodSec())) {
            options.setStatsPersistPeriodSec(getStatsPersistPeriodSec());
        }

        if (nonNull(getStatsHistoryBufferSize())) {
            options.setStatsHistoryBufferSize(getStatsHistoryBufferSize());
        }

        if (nonNull(getAdviseRandomOnOpen())) {
            options.setAdviseRandomOnOpen(getAdviseRandomOnOpen());
        }

        if (nonNull(getDbWriteBufferSize())) {
            options.setDbWriteBufferSize(getDbWriteBufferSize());
        }

        if (nonNull(getWriteBufferManager())) {
            options.setWriteBufferManager(getWriteBufferManager());
        }

        if (nonNull(getNewTableReaderForCompactionInputs())) {
            options.setNewTableReaderForCompactionInputs(getNewTableReaderForCompactionInputs());
        }

        if (nonNull(getCompactionReadaheadSize())) {
            options.setCompactionReadaheadSize(getCompactionReadaheadSize());
        }

        if (nonNull(getRandomAccessMaxBufferSize())) {
            options.setRandomAccessMaxBufferSize(getRandomAccessMaxBufferSize());
        }

        if (nonNull(getWritableFileMaxBufferSize())) {
            options.setWritableFileMaxBufferSize(getWritableFileMaxBufferSize());
        }

        if (nonNull(getUseAdaptiveMutex())) {
            options.setUseAdaptiveMutex(getUseAdaptiveMutex());
        }

        if (nonNull(getBytesPerSync())) {
            options.setBytesPerSync(getBytesPerSync());
        }

        if (nonNull(getWalBytesPerSync())) {
            options.setWalBytesPerSync(getWalBytesPerSync());
        }

        if (nonNull(getStrictBytesPerSync())) {
            options.setStrictBytesPerSync(getStrictBytesPerSync());
        }

        if (nonNull(getListeners())) {
            options.setListeners(getListeners());
        }

        if (nonNull(getEnableThreadTracking())) {
            options.setEnableThreadTracking(getEnableThreadTracking());
        }

        if (nonNull(getDelayedWriteRate())) {
            options.setDelayedWriteRate(getDelayedWriteRate());
        }

        if (nonNull(getEnablePipelinedWrite())) {
            options.setEnablePipelinedWrite(getEnablePipelinedWrite());
        }

        if (nonNull(getUnorderedWrite())) {
            options.setUnorderedWrite(getUnorderedWrite());
        }

        if (nonNull(getAllowConcurrentMemtableWrite())) {
            options.setAllowConcurrentMemtableWrite(getAllowConcurrentMemtableWrite());
        }

        if (nonNull(getEnableWriteThreadAdaptiveYield())) {
            options.setEnableWriteThreadAdaptiveYield(getEnableWriteThreadAdaptiveYield());
        }

        if (nonNull(getWriteThreadMaxYieldUsec())) {
            options.setWriteThreadMaxYieldUsec(getWriteThreadMaxYieldUsec());
        }

        if (nonNull(getWriteThreadSlowYieldUsec())) {
            options.setWriteThreadSlowYieldUsec(getWriteThreadSlowYieldUsec());
        }

        if (nonNull(getSkipStatsUpdateOnDbOpen())) {
            options.setSkipStatsUpdateOnDbOpen(getSkipStatsUpdateOnDbOpen());
        }

        if (nonNull(getSkipCheckingSstFileSizesOnDbOpen())) {
            options.setSkipCheckingSstFileSizesOnDbOpen(getSkipCheckingSstFileSizesOnDbOpen());
        }

        if (nonNull(getWalRecoveryMode())) {
            options.setWalRecoveryMode(getWalRecoveryMode());
        }

        if (nonNull(getAllow2pc())) {
            options.setAllow2pc(getAllow2pc());
        }

        if (nonNull(getRowCache())) {
            options.setRowCache(getRowCache());
        }

        if (nonNull(getWalFilter())) {
            options.setWalFilter(getWalFilter());
        }

        if (nonNull(getFailIfOptionsFileError())) {
            options.setFailIfOptionsFileError(getFailIfOptionsFileError());
        }

        if (nonNull(getDumpMallocStats())) {
            options.setDumpMallocStats(getDumpMallocStats());
        }

        if (nonNull(getAvoidFlushDuringRecovery())) {
            options.setAvoidFlushDuringRecovery(getAvoidFlushDuringRecovery());
        }

        if (nonNull(getAvoidFlushDuringShutdown())) {
            options.setAvoidFlushDuringShutdown(getAvoidFlushDuringShutdown());
        }

        if (nonNull(getAllowIngestBehind())) {
            options.setAllowIngestBehind(getAllowIngestBehind());
        }

        if (nonNull(getPreserveDeletes())) {
            options.setPreserveDeletes(getPreserveDeletes());
        }

        if (nonNull(getTwoWriteQueues())) {
            options.setTwoWriteQueues(getTwoWriteQueues());
        }

        if (nonNull(getManualWalFlush())) {
            options.setManualWalFlush(getManualWalFlush());
        }

        if (nonNull(getRateLimiter())) {
            options.setRateLimiter(getRateLimiter());
        }

        if (nonNull(getSstFileManager())) {
            options.setSstFileManager(getSstFileManager());
        }

        if (nonNull(getLogger())) {
            options.setLogger(getLogger());
        }

        if (nonNull(getInfoLogLevel())) {
            options.setInfoLogLevel(getInfoLogLevel());
        }

        if (nonNull(getAtomicFlush())) {
            options.setAtomicFlush(getAtomicFlush());
        }

        if (nonNull(getAvoidUnnecessaryBlockingIO())) {
            options.setAvoidUnnecessaryBlockingIO(getAvoidUnnecessaryBlockingIO());
        }

        if (nonNull(getPersistStatsToDisk())) {
            options.setPersistStatsToDisk(getPersistStatsToDisk());
        }

        if (nonNull(getWriteDbidToManifest())) {
            options.setWriteDbidToManifest(getWriteDbidToManifest());
        }

        if (nonNull(getLogReadaheadSize())) {
            options.setLogReadaheadSize(getLogReadaheadSize());
        }

        if (nonNull(getBestEffortsRecovery())) {
            options.setBestEffortsRecovery(getBestEffortsRecovery());
        }

        if (nonNull(getMaxBgErrorResumeCount())) {
            options.setMaxBgErrorResumeCount(getMaxBgErrorResumeCount());
        }

        if (nonNull(getBgerrorResumeRetryInterval())) {
            options.setBgerrorResumeRetryInterval(getBgerrorResumeRetryInterval());
        }

        return options;
    }

    public static final String DEFAULT_SUFFIX = "" + currentTimeMillis();

}

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
package com.wl4g.component.integration.sharding.failover;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.notEmpty;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.Assert2.state;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.shardingsphere.governance.context.metadata.GovernanceMetaDataContexts;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import com.wl4g.component.common.lang.StringUtils2;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.task.GenericTaskRunner;
import com.wl4g.component.common.task.RunnerProperties;
import com.wl4g.component.common.task.RunnerProperties.StartupMode;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats.NodeInfo;
import com.wl4g.component.integration.sharding.failover.config.FailoverConfiguration;
import com.wl4g.component.integration.sharding.failover.exception.FailoverException;
import com.wl4g.component.integration.sharding.failover.exception.UnreachablePrimaryNodeFailoverException;
import com.wl4g.component.integration.sharding.failover.initializer.FailoverAbstractBootstrapInitializer;
import com.wl4g.component.integration.sharding.failover.jdbc.JdbcOperator;
import com.wl4g.component.integration.sharding.util.HostUtil;
import com.wl4g.component.integration.sharding.util.JdbcUtil;
import com.wl4g.component.integration.sharding.util.JdbcUtil.JdbcInformation;
import com.zaxxer.hikari.HikariDataSource;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link AbstractProxyFailover}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public abstract class AbstractProxyFailover<S extends NodeStats> extends GenericTaskRunner<RunnerProperties>
        implements ProxyFailover<S> {

    private final FailoverAbstractBootstrapInitializer initializer;
    private final ShardingSphereMetaData metadata;
    private final Map<String, HikariDataSource> cachingAdminDataSources = new ConcurrentHashMap<>(4);
    private FailoverSchemaConfiguration cachingFailoverSchemaConfig;

    public AbstractProxyFailover(FailoverAbstractBootstrapInitializer initializer, ShardingSphereMetaData metadata) {
        super(new RunnerProperties(StartupMode.NOSTARTUP, 1));
        this.initializer = notNullOf(initializer, "initializer");
        this.metadata = notNullOf(metadata, "metadata");
        FailoverShutdownManager.getInstance().addShutdownDestroyHandler(this);
    }

    @Override
    protected void postStartupProperties() throws Exception {
        FailoverConfiguration failoverConfig = ProxyContext.getInstance().getFailoverConfig();
        getWorker().scheduleWithRandomDelay(this, failoverConfig.getInspectInitialDelayMs(),
                failoverConfig.getInspectMinDelayMs(), failoverConfig.getInspectMaxDelayMs(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        final String lockName = getSchemaName().concat(".failover");
        Optional<ShardingSphereLock> op = ProxyContext.getInstance().getLock();
        try {
            if (ProxyContext.getInstance().getMetaDataContexts() instanceof GovernanceMetaDataContexts) { // In-GovernanceMetaContexts-mode.
                assert op.isPresent() : new FailoverException(
                        format("Failover running in governance mode, the lock must be enabled. Please check config '%s'",
                                ConfigurationPropertyKey.LOCK_ENABLED.getKey()),
                        null);

                if (op.get().tryLock(lockName, 10_000L)) {
                    log.info("Obtained failover execution lock, prepare for processing ...");
                    processFailover();
                } else {
                    log.warn("No obtained failover execution lock, skip for processing.");
                }
            } else { // In StandardMetaContexts mode.
                log.info("In standard context running, direct for processing ...");
                processFailover();
            }
        } catch (Exception e) {
            log.error("Failed to process backend nodes primary-slave failover.", e);
        } finally {
            if (op.isPresent()) {
                op.get().releaseLock(lockName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processFailover() throws Exception {
        FailoverSchemaConfiguration oldFailoverConfig = loadFailoverSchemaConfiguration(true);

        // Other rule configurations do not need to be update.
        List<RuleConfiguration> newRuleConfigs = new ArrayList<>(oldFailoverConfig.getOtherRuleConfigs());

        boolean anyChanaged = false;
        for (ReadwriteSplittingRuleConfiguration oldRwRuleConfig : safeList(oldFailoverConfig.getReadwriteRuleConfigs())) {
            // New read-write-splitting dataSources.
            List<ReadwriteSplittingDataSourceRuleConfiguration> newRwDataSources = new ArrayList<>(4);

            for (ReadwriteSplittingDataSourceRuleConfiguration oldRwDataSource : safeList(oldRwRuleConfig.getDataSources())) {
                // Obtain backend node admin dataSource.
                DataSource adminDataSource = obtainSelectedBackendNodeAdminDataSource(oldRwDataSource.getName(),
                        oldRwDataSource.getReadDataSourceNames());

                log.debug("Inspecting ... oldRwDataSourceConfig: {}, actualAdminDataSource: {}",
                        () -> toJSONString(oldRwDataSource), () -> adminDataSource);

                try (JdbcOperator operator = new JdbcOperator(adminDataSource);) {
                    // Inspect primary/standby latest information.
                    S result = inspecting(operator);
                    log.info("Inspected rwDataSourceName: {}, nodeInfo: {}", () -> oldRwDataSource.getName(),
                            () -> toJSONString(result));

                    // Selection new primary node.
                    notEmpty(result.getPrimaryNodes(), UnreachablePrimaryNodeFailoverException.class,
                            "No primary node information is currently queried.");
                    NodeInfo newPrimaryNode = chooseNewPrimaryNode(result.getPrimaryNodes());

                    // TODO
                    // Transform db host/port to external(loadBalancer)
                    // host/port.
                    String newPrimaryDataSourceName = findMatchingNewPrimaryDataSourceName(
                            oldFailoverConfig.getAllDataSourceConfigs(), oldRwDataSource, newPrimaryNode);
                    String oldPrimaryDataSourceName = oldRwDataSource.getWriteDataSourceName();

                    // Check dataSource primary changed?
                    if (isChangedPrimaryNode(newPrimaryNode, newPrimaryDataSourceName, oldPrimaryDataSourceName)) {
                        // Gets changed new read dataSourceNames
                        List<String> newReadDataSourceNames = getChangedNewReadDataSourceNames(
                                oldFailoverConfig.getAllDataSourceConfigs(), oldRwDataSource, result.getStandbyNodes());

                        // New read-write-splitting dataSource.
                        ReadwriteSplittingDataSourceRuleConfiguration newRwDataSource = new ReadwriteSplittingDataSourceRuleConfiguration(
                                oldRwDataSource.getName(), oldRwDataSource.getAutoAwareDataSourceName(), newPrimaryDataSourceName,
                                newReadDataSourceNames, oldRwDataSource.getLoadBalancerName());
                        newRwDataSources.add(newRwDataSource);
                        anyChanaged = true;
                    } else { // Not changed
                        newRwDataSources.add(oldRwDataSource);
                        log.debug(
                                "Skiping change read-write-splitting, becuase primary dataSourceName it's up to date. {}, actualSchemaName: {}, schemaName: {}",
                                oldPrimaryDataSourceName, oldRwDataSource.getName(), getSchemaName());
                    }
                }
            }
            if (!newRwDataSources.isEmpty()) {
                newRuleConfigs.add(new ReadwriteSplittingRuleConfiguration(newRwDataSources, oldRwRuleConfig.getLoadBalancers()));
            }
        }

        if (anyChanaged && !newRuleConfigs.isEmpty()) {
            doChangeReadwriteSplittingRuleConfiguration(newRuleConfigs);
        }
    }

    /**
     * Obtain selection back-end node administrator dataSource.
     * 
     * @param haReadwriteDataSourceName
     * @param readDataSourceNames
     * @return
     * @throws SQLException
     */
    private synchronized DataSource obtainSelectedBackendNodeAdminDataSource(String haReadwriteDataSourceName,
            List<String> readDataSourceNames) throws SQLException {
        HikariDataSource adminDataSource = cachingAdminDataSources.get(haReadwriteDataSourceName);
        if (nonNull(adminDataSource)) {
            // Detecting & checking dataSource active?
            try {
                adminDataSource.getConnection().close();
                return adminDataSource;
            } catch (SQLException e) {
                adminDataSource.close();
                cachingAdminDataSources.remove(haReadwriteDataSourceName); // reset
                log.warn("Deaded caching dataSource: {}({}), reason: {}", adminDataSource, adminDataSource.getJdbcUrl(),
                        e.getMessage());
            }
        }
        log.info("Trying obtain next node admin dataSource ... - {}", haReadwriteDataSourceName);

        for (Entry<String, DataSource> ent : metadata.getResource().getDataSources().entrySet()) {
            String dsName = ent.getKey();
            // Skip data source names that do not belong to the data source
            // collection of the current read-write separation configuration.
            // (YAML tag: !READWRITE_SPLITTING)
            if (!safeList(readDataSourceNames).contains(dsName)) {
                continue;
            }

            DataSource ds = ent.getValue();
            try {
                // Find backend node business dataSource jdbcUrl.
                String jdbcUrl = null;
                if (ds instanceof HikariDataSource) {
                    jdbcUrl = ((HikariDataSource) ds).getJdbcUrl();
                } else {
                    throw new UnsupportedOperationException(format("No supported dataSource type. %s", ds.getClass()));
                }
                state(!isBlank(jdbcUrl), "Unable get backend node admin dataSource jdbcUrl.");

                // Build backend node admin connection.
                adminDataSource = new HikariDataSource();
                adminDataSource.setConnectionTimeout(6_000L);
                adminDataSource.setMaximumPoolSize(1);
                adminDataSource.setMinimumIdle(1);
                adminDataSource.setIdleTimeout(0L);
                adminDataSource.setMaxLifetime(180_000L);

                JdbcInformation info = JdbcUtil.resolve(jdbcUrl);
                decorateAdminBackendDataSource(dsName, info.getHost(), info.getPort(), adminDataSource);

                cachingAdminDataSources.put(haReadwriteDataSourceName, adminDataSource);
                return adminDataSource;
            } catch (Exception e) {
                log.warn("Cannot build backend connection of dataSourceName: {}", dsName);
            }
        }

        throw new SQLException(
                format("Failed to build backend connection. metadata dataSources: %s", metadata.getResource().getDataSources()));
    }

    /**
     * Decoration creating administrator dataSource properties.
     * 
     * @param ruleDataSourceName
     * @param ruleDataSourceJdbcHost
     * @param ruldDataSourceJdbcPort
     * @param adminDataSource
     */
    protected abstract void decorateAdminBackendDataSource(String ruleDataSourceName, String ruleDataSourceJdbcHost,
            int ruldDataSourceJdbcPort, HikariDataSource adminDataSource);

    /**
     * Choosing new primary node {@link NodeInfo}
     * 
     * @param newPrimaryNodes
     * @return
     */
    protected NodeInfo chooseNewPrimaryNode(List<? extends NodeInfo> newPrimaryNodes) {
        return newPrimaryNodes.get(0); // By default
    }

    /**
     * Check whether the master node has been changed.
     * 
     * @param newPrimaryNode
     * @param newPrimaryDataSourceName
     * @param oldPrimaryDataSourceName
     * @see https://shardingsphere.apache.org/document/current/cn/features/governance/management/registry-center/#metadataschemenamedatasources
     * @return
     */
    private boolean isChangedPrimaryNode(NodeInfo newPrimaryNode, String newPrimaryDataSourceName,
            String oldPrimaryDataSourceName) {
        return !StringUtils2.equals(oldPrimaryDataSourceName, newPrimaryDataSourceName);
    }

    /**
     * Gets changed new read dataSource names.
     * 
     * @param allDataSourceConfigs
     * @param oldRwDataSource
     * @param newStandbyNodes
     * @return
     */
    private List<String> getChangedNewReadDataSourceNames(Map<String, DataSourceConfiguration> allDataSourceConfigs,
            ReadwriteSplittingDataSourceRuleConfiguration oldRwDataSource, List<? extends NodeInfo> newStandbyNodes) {

        List<String> newReadDataSourceNames = safeList(newStandbyNodes).stream()
                .map(n -> findMatchingNewPrimaryDataSourceName(allDataSourceConfigs, oldRwDataSource, n)).collect(toList());

        return newReadDataSourceNames;
    }

    /**
     * Gets the schema name of the target to be processed by the current
     * failover.
     * 
     * Notes: a failover instance is responsible for monitoring a schema.
     * 
     * @return
     */
    protected String getSchemaName() {
        return metadata.getName();
    }

    /**
     * Load failover schema configuration.
     * 
     * @param useCache
     * @return
     */
    private synchronized FailoverSchemaConfiguration loadFailoverSchemaConfiguration(boolean useCache) {
        if (useCache && nonNull(cachingFailoverSchemaConfig)) {
            return cachingFailoverSchemaConfig;
        }

        Map<String, DataSourceConfiguration> allDataSourceConfigs = initializer.loadDataSourceConfigs(getSchemaName());
        Collection<RuleConfiguration> allRuleConfigs = initializer.loadRuleConfigs(getSchemaName());

        List<ReadwriteSplittingRuleConfiguration> readwriteRuleConfigs = new ArrayList<>(2);
        List<RuleConfiguration> otherRuleConfigs = new ArrayList<>(2);
        for (RuleConfiguration ruleConfig : safeList(allRuleConfigs)) {
            if (ruleConfig instanceof ReadwriteSplittingRuleConfiguration) {
                readwriteRuleConfigs.add((ReadwriteSplittingRuleConfiguration) ruleConfig);
            } else {
                otherRuleConfigs.add(ruleConfig);
            }
        }

        return new FailoverSchemaConfiguration(allDataSourceConfigs, allRuleConfigs, readwriteRuleConfigs, otherRuleConfigs);
    }

    /**
     * Transform matching configuration dataSource name by host and port.
     * 
     * @param allDataSourceConfigs
     * @param oldRwDataSource
     * @param node
     * @return
     */
    private String findMatchingNewPrimaryDataSourceName(Map<String, DataSourceConfiguration> allDataSourceConfigs,
            ReadwriteSplittingDataSourceRuleConfiguration oldRwDataSource, NodeInfo node) {

        List<String> oldAllRwDataSourceNames = new ArrayList<>(oldRwDataSource.getReadDataSourceNames());
        oldAllRwDataSourceNames.add(oldRwDataSource.getWriteDataSourceName());

        for (Entry<String, DataSourceConfiguration> ent : safeMap(allDataSourceConfigs).entrySet()) {
            String defineDataSourceName = ent.getKey();
            String jdbcUrl = valueOf(ent.getValue().getProps().get("jdbcUrl"));

            if (oldAllRwDataSourceNames.contains(defineDataSourceName)) {
                JdbcInformation info = JdbcUtil.resolve(jdbcUrl);
                if (info.getPort() == node.getPort() && HostUtil.isSameHost(info.getHost(), node.getHost())) {
                    return ent.getKey(); // Define dataSource name.
                }
            }
        }

        throw new IllegalStateException(format("No found dataSource name by host: %s, port: %s", node.getHost(), node.getPort()));
    }

    /**
     * Do changing readWriteSplitting rule configuration.
     * 
     * @param newReadWriteSplittingRuleConfigs
     */
    private void doChangeReadwriteSplittingRuleConfiguration(List<RuleConfiguration> newReadWriteSplittingRuleConfigs) {
        log.info("Changed new read-write-splitting rule configuration ... - {}", newReadWriteSplittingRuleConfigs);
        initializer.updateSchemaRuleConfiguration(getSchemaName(), newReadWriteSplittingRuleConfigs);
    }

    @Getter
    @AllArgsConstructor
    static class FailoverSchemaConfiguration {
        private Map<String, DataSourceConfiguration> allDataSourceConfigs;
        private Collection<RuleConfiguration> allRuleConfigs;
        private List<ReadwriteSplittingRuleConfiguration> readwriteRuleConfigs;
        private List<RuleConfiguration> otherRuleConfigs;
    }

    static class FailoverShutdownManager {
        private static final SmartLogger log = getLogger(FailoverShutdownManager.class);
        private static final FailoverShutdownManager INSTANCE = new FailoverShutdownManager();
        private static final List<AbstractProxyFailover<? extends NodeStats>> shutdownFailovers = new Vector<>(4);
        private Thread shutdownThread;

        public static FailoverShutdownManager getInstance() {
            return INSTANCE;
        }

        public void addShutdownDestroyHandler(AbstractProxyFailover<? extends NodeStats> failover) {
            shutdownFailovers.add(notNullOf(failover, "failover"));
            initIfNecessary();
        }

        /**
         * Initialization failovers to JVM shutdown handlers.
         */
        private synchronized void initIfNecessary() {
            if (isNull(shutdownThread)) {
                shutdownThread = new Thread(() -> {
                    safeList(shutdownFailovers).forEach(failover -> {
                        safeMap(failover.cachingAdminDataSources).forEach((dataSourceName, adminDataSource) -> {
                            try {
                                log.info("Closing adminDataSource: {}, of '{}'", adminDataSource, dataSourceName);
                                adminDataSource.close();
                            } catch (Exception e) {
                                log.info("Unable close adminDataSource: {}, of '{}'", adminDataSource, dataSourceName);
                            }
                        });
                    });
                });
            }
        }

    }

}

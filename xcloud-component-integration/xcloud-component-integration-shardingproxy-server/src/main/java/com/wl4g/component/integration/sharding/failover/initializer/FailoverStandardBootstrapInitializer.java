/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.component.integration.sharding.failover.initializer;

import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.transaction.context.TransactionContexts;

import lombok.extern.slf4j.Slf4j;

/**
 * Standard bootstrap initializer.
 */
@Slf4j
public final class FailoverStandardBootstrapInitializer extends FailoverAbstractBootstrapInitializer {

    private volatile YamlProxyConfiguration currentYamlProxyConfig;

    @Override
    protected ProxyConfiguration getProxyConfiguration(final YamlProxyConfiguration yamlConfig) {
        return new YamlProxyConfigurationSwapper().swap(yamlConfig);
    }

    @Override
    protected void initContext(YamlProxyConfiguration yamlConfig, boolean registerSubscriber) throws SQLException {
        this.currentYamlProxyConfig = notNullOf(yamlConfig, "yamlConfig");
        super.initContext(yamlConfig, registerSubscriber);
    }

    @Override
    protected MetaDataContexts decorateMetaDataContexts(final MetaDataContexts metaDataContexts) {
        return metaDataContexts;
    }

    @Override
    protected TransactionContexts decorateTransactionContexts(final TransactionContexts transactionContexts,
            final String xaTransactionMangerType) {
        return transactionContexts;
    }

    @Override
    protected void initScalingWorker(final YamlProxyConfiguration yamlConfig) {
        getScalingConfiguration(yamlConfig).ifPresent(optional -> ScalingContext.getInstance().init(optional));
    }

    //
    // ADD for failover
    //

    @Override
    public Map<String, DataSourceConfiguration> loadDataSourceConfigs(String schemaName) {
        return safeMap(ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources()).entrySet().stream()
                .collect(toMap(e -> e.getKey(), e -> DataSourceConfiguration.getDataSourceConfiguration(e.getValue())));
    }

    @Override
    public Collection<RuleConfiguration> loadRuleConfigs(String schemaName) {
        return ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations();
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void updateSchemaRuleConfiguration(String schemaName,
            Collection<? extends RuleConfiguration> schemaRuleConfigs) {
        try {
            YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
            Collection<YamlRuleConfiguration> yamlSchemaRuleConfigs = swapperEngine
                    .swapToYamlRuleConfigurations((Collection<RuleConfiguration>) schemaRuleConfigs);
            safeMap(currentYamlProxyConfig.getRuleConfigurations()).entrySet().stream()
                    .filter(ent -> ent.getKey().equals(schemaName)).forEach(ent -> {
                        YamlProxyRuleConfiguration yamlProxyRuleConfig = ent.getValue();
                        yamlProxyRuleConfig.getRules().addAll(yamlSchemaRuleConfigs);
                    });
            initContext(currentYamlProxyConfig, false);
        } catch (SQLException e) {
            log.error(format("Failed to update schema rule configuration. - schemaName: %s, schemaRuleConfigs: %s", schemaName,
                    toJSONString(schemaRuleConfigs)), e);
        }
    }

}

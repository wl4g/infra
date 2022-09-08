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
package com.wl4g.infra.metrics;

import static com.wl4g.infra.metrics.constants.MetricsInfraConstants.CONF_PREFIX_INFRA_UTIL;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.wl4g.infra.common.net.InetUtils;
import com.wl4g.infra.common.net.InetUtils.InetUtilsProperties;

import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * {@link MetricsInfraAutoConfiguration}
 * 
 * @author Wangl.sir &lt;James Wong <jameswong1376@gmail.com>&gt;
 * @version 2021-11-16 v1.0.0
 * @since v1.0.0
 */
@Configuration
public class MetricsInfraAutoConfiguration {

    @Bean(BEAN_INETUTILS_PROPERTIES)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_UTIL)
    public InetUtilsProperties inetUtilsProperties() {
        return new InetUtilsProperties();
    }

    @Bean(BEAN_INETUTILS)
    public InetUtils inetUtils(InetUtilsProperties inetProps) {
        return new InetUtils(inetProps);
    }

    @Bean
    public MetricsFacade metricsFacade(
            Environment environment,
            PrometheusMeterRegistry registry,
            @Qualifier(BEAN_INETUTILS) InetUtils inet) {
        return new MetricsFacade(environment, registry, inet);
    }

    public static final String BEAN_INETUTILS = "metricsInfraInetUtils";
    public static final String BEAN_INETUTILS_PROPERTIES = BEAN_INETUTILS + "Properties";

}

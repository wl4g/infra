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
package com.wl4g.infra.integration.feign.core.annotation;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.integration.feign.core.constant.FeignConsumerConstant.CONF_PREFIX_INFRA_FEIGN_ENABLED;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import com.google.common.base.Splitter;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.integration.feign.core.config.CoreFeignAutoConfiguration;
import com.wl4g.infra.integration.feign.core.config.FeignSpringBootAutoConfiguration;
import com.wl4g.infra.integration.feign.core.constant.FeignConsumerConstant;
import com.wl4g.infra.integration.feign.core.context.DefaultFeignContextAutoConfiguration;
import com.wl4g.infra.integration.feign.core.context.internal.FeignContextAutoConfiguration;
//import com.wl4g.infra.integration.feign.core.config.CoreFeignAutoConfiguration;
//import com.wl4g.infra.integration.feign.core.context.internal.FeignContextAutoConfiguration;
import com.wl4g.infra.integration.feign.core.plugin.InsertBeanBindingPluginCoprocessor;
import com.wl4g.infra.integration.feign.core.plugin.PageBindingPluginCoprocessor;
import com.wl4g.infra.integration.feign.core.plugin.SimpleStacktracePluginCoprocessor;

/**
 * {@link AutoConfigurationRegistrar}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2020-01-12
 * @sine v1.0
 * @see
 */
class AutoConfigurationRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
    protected final SmartLogger log = getLogger(getClass());

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Automatic adaptation register configuration classes bean. </br>
     * </br>
     * </br>
     * The following SpringCloud auto configuration should not be affected.
     * 
     * {@link org.springframework.cloud.openfeign.ribbon.DefaultFeignLoadBalancedConfiguration}
     * {@link org.springframework.cloud.openfeign.ribbon.HttpClientFeignLoadBalancedConfiguration}
     * {@link org.springframework.cloud.openfeign.ribbon.OkHttpFeignLoadBalancedConfiguration}
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // Check enabled configuration
        if (!isEnableConfiguration(environment)) {
            log.warn("No enabled Feign+SpringBoot / Feign+SpringBoot+Istio / Feign+SpringCloud / Feign+SpringBoot+Dubbo"
                    + " auto configurer !");
            return;
        }

        // Register core requires configuration.
        //
        registerBeanDefinition0(registry, CoreFeignAutoConfiguration.class);

        // Register SpringCloud-Feign environment configuration.
        if (FrameworkType.FEIGN_SPRINGCLOUD.isActive()) {
            // Nothing to-do, some codes ...
        }
        // Register Apache-Dubbo-Feign environment configuration.
        else if (FrameworkType.FEIGN_SPRINGBOOT_APACHE_DUBBO.isActive()) {
            // Nothing to-do, some codes ...
        }
        // Register (Default)SpringBoot-Feign or SpringBoot-Istio-Feign
        // environment configuration.
        else {
            if (FrameworkType.FEIGN_SPRINGBOOT.isActive() || FrameworkType.FEIGN_SPRINGBOOT_ISTIO.isActive()) {
                registerBeanDefinition0(registry, FeignSpringBootAutoConfiguration.class);
            }
            if (FrameworkType.FEIGN_SPRINGBOOT.isActive()) {
                registerBeanDefinition0(registry, DefaultFeignContextAutoConfiguration.class);
            }
        }

        // Register build-in feign-context configuration.
        //
        registerBeanDefinition0(registry, FeignContextAutoConfiguration.class);

        // Register plug-in's configuration.
        //
        // registerBeanDefinition(registry,FeignPluginsAutoConfiguration.class);
        registerPlugins(registry);
    }

    /**
     * Register the feign context coprocessing plug-in's.
     * 
     * @param registry
     */
    private void registerPlugins(BeanDefinitionRegistry registry) {
        List<String> plugins = DEFAULT_PLUGINS;

        // Gets from configuration.
        String pluginValue = environment.getProperty(FeignConsumerConstant.CONF_PREFIX_INFRA_FEIGN_PLUGIN_CLASSES);
        if (!isBlank(pluginValue)) {
            List<String> plugins0 = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(pluginValue);
            if (!isEmpty(plugins0)) {
                plugins = plugins0;
            }
        }

        // Register plug-in's bean.
        for (String cls : plugins) {
            try {
                registerBeanDefinition0(registry, ClassUtils.forName(cls, Thread.currentThread().getContextClassLoader()));
            } catch (ClassNotFoundException | LinkageError e) {
                throw new IllegalStateException(format("Failed to register coprocessing for '%s'", cls), e);
            }
        }
    }

    private void registerBeanDefinition0(BeanDefinitionRegistry registry, Class<?> configurationClass) {
        AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(configurationClass).getBeanDefinition();
        registry.registerBeanDefinition(AnnotationBeanNameGenerator.INSTANCE.generateBeanName(definition, registry), definition);
    }

    /**
     * Check enabled configuration.
     * 
     * @return
     */
    public static boolean isEnableConfiguration(Environment environment) {
        // Default by true, If want to run in Stand-alone mode, should set false
        return environment.getProperty(CONF_PREFIX_INFRA_FEIGN_ENABLED, boolean.class, true);
    }

    private static List<String> DEFAULT_PLUGINS = unmodifiableList(
            Arrays.asList(InsertBeanBindingPluginCoprocessor.class.getName(), PageBindingPluginCoprocessor.class.getName(),
                    SimpleStacktracePluginCoprocessor.class.getName()));

}

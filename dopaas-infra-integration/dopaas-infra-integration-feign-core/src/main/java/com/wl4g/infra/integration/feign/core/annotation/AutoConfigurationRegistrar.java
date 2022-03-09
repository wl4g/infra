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
package com.wl4g.infra.integration.feign.core.annotation;

import static com.wl4g.infra.common.lang.ClassUtils2.isPresent;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.integration.feign.core.constant.FeignConsumerConstant.KEY_CONFIG_ENABLE;
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
import com.wl4g.infra.integration.feign.core.config.SpringBootFeignAutoConfiguration;
import com.wl4g.infra.integration.feign.core.constant.FeignConsumerConstant;
import com.wl4g.infra.integration.feign.core.context.DefaultFeignContextAutoConfiguration;
import com.wl4g.infra.integration.feign.core.context.internal.FeignContextAutoConfiguration;
import com.wl4g.infra.integration.feign.core.plugin.InsertBeanBindingPluginCoprocessor;
import com.wl4g.infra.integration.feign.core.plugin.PageBindingPluginCoprocessor;
import com.wl4g.infra.integration.feign.core.plugin.SimpleStacktracePluginCoprocessor;

/**
 * {@link AutoConfigurationRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
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
     * Spring cloud auto configuration should not be affected.
     * 
     * {@link org.springframework.cloud.openfeign.ribbon.DefaultFeignLoadBalancedConfiguration}
     * {@link org.springframework.cloud.openfeign.ribbon.HttpClientFeignLoadBalancedConfiguration}
     * {@link org.springframework.cloud.openfeign.ribbon.OkHttpFeignLoadBalancedConfiguration}
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // Check enabled configuration
        if (!isEnableConfiguration(environment)) {
            log.warn("No enabled SpringBoot-Feign/SpringBoot-Istio-Feign/SpringCloud-Feign/Dubbo-Feign auto configurer!");
            return;
        }

        // Register core(requires) configuration.
        //
        registerBeanDefinition(registry, CoreFeignAutoConfiguration.class);
        registerBeanDefinition(registry, FeignContextAutoConfiguration.class);

        // Register plug-in's configuration.
        //
        // registerBeanDefinition(registry,FeignPluginsAutoConfiguration.class);
        registerPlugins(registry);

        // Register SpringCloud-Feign environment configuration.
        if (isSpringCloudFeignEnvironment()) {
            // Ignore, some codes ...
        }
        // Register Apache-Dubbo-Feign environment configuration.
        else if (isApacheDubboFeignEnvironment()) {
            // Ignore, some codes ...
        }
        // Register (Default)SpringBoot-Feign or SpringBoot-Istio-Feign
        // environment configuration.
        else {
            if (isSpringBootFeignEnvironment() || isSpringBootIstioFeignEnvironment()) {
                registerBeanDefinition(registry, SpringBootFeignAutoConfiguration.class);
            }
            if (isSpringBootFeignEnvironment()) {
                registerBeanDefinition(registry, DefaultFeignContextAutoConfiguration.class);
            }
        }

    }

    private void registerPlugins(BeanDefinitionRegistry registry) {
        List<String> plugins = DEFAULT_PLUGINS;

        // Gets from configuration.
        String pluginValue = environment.getProperty(FeignConsumerConstant.KEY_PLUGIN_CLASSES);
        if (!isBlank(pluginValue)) {
            List<String> plugins0 = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(pluginValue);
            if (!isEmpty(plugins0)) {
                plugins = plugins0;
            }
        }

        // Register plug-in's configuration.
        for (String pluginClass : plugins) {
            try {
                registerBeanDefinition(registry, ClassUtils.forName(pluginClass, Thread.currentThread().getContextClassLoader()));
            } catch (ClassNotFoundException | LinkageError e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> configurationClass) {
        AbstractBeanDefinition definition = BeanDefinitionBuilder.genericBeanDefinition(configurationClass).getBeanDefinition();
        registry.registerBeanDefinition(AnnotationBeanNameGenerator.INSTANCE.generateBeanName(definition, registry), definition);
    }

    /**
     * Check enabled SpringBoot-Feign or SpringCloud-Feign configuration.
     * 
     * @return
     */
    public static boolean isEnableConfiguration(Environment environment) {
        // Default by true, If want to run in Stand-alone mode, should set false
        return environment.getProperty(KEY_CONFIG_ENABLE, boolean.class, true);
    }

    /**
     * Check if the current SpringBoot-Feign environment.
     * 
     * @return
     */
    public static boolean isSpringBootFeignEnvironment() {
        if (isAlibabaDubboFeignEnvironment()) {
            throw new IllegalStateException(format("Check the existing com.alibaba.dubbo.xx "
                    + "classes in the current classpath, the new version has been migrated to org.apache.dubbo.xx,"
                    + "please remove old-version dependency and use the new version!"));
        }
        return !isSpringBootIstioFeignEnvironment() && !isSpringCloudFeignEnvironment() && !isApacheDubboFeignEnvironment();
    }

    /**
     * Check if the current SpringBoot-Istio-Feign environment.
     * 
     * @return
     */
    public static boolean isSpringBootIstioFeignEnvironment() {
        return isPresent("com.wl4g.infra.integration.feign.istio.constant.IstioFeignConstant");
    }

    /**
     * Check if the current SpringCloud-Feign environment.
     * 
     * @return
     */
    public static boolean isSpringCloudFeignEnvironment() {
        // isPresent("org.springframework.cloud.openfeign.FeignClientsRegistrar")
        return isPresent("com.wl4g.infra.integration.feign.springcloud.constant.SpringCloudFeignConstant");
    }

    /**
     * Check if the current ApacheDubbo-Feign environment.
     * 
     * @return
     */
    public static boolean isApacheDubboFeignEnvironment() {
        // isPresent("org.apache.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor")
        return isPresent("com.wl4g.infra.integration.feign.dubbo.constant.DubboFeignConstant");
    }

    /**
     * Check if the current ApacheDubbo-Feign environment. </br>
     * </br>
     * 
     * Notice: The new version of DUBBO package name is: org.apache.dubbo.xxx
     * 
     * @return
     */
    @Deprecated
    public static boolean isAlibabaDubboFeignEnvironment() {
        return isPresent("com.alibaba.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor");
    }

    private static List<String> DEFAULT_PLUGINS = unmodifiableList(
            Arrays.asList(InsertBeanBindingPluginCoprocessor.class.getName(), PageBindingPluginCoprocessor.class.getName(),
                    SimpleStacktracePluginCoprocessor.class.getName()));

}

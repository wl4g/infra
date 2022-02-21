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
package com.wl4g.component.integration.feign.core.annotation;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.feign.core.config.FeignConsumerAutoConfiguration;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link FeignConsumerConfigurerRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-01-12
 * @sine v1.0
 * @see
 */
class FeignConsumerConfigurerRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {
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
		if (!FeignConsumersRegistrar.isEnableConfiguration(environment)) {
			log.warn("No enabled SpringBoot and SpringCloud feign auto configurer!");
			return;
		}

		// Not SpringCloud + feign
		if (!FeignConsumersRegistrar.hasSpringCloudFeignClass()) {
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FeignConsumerAutoConfiguration.class);
			AbstractBeanDefinition definition = builder.getBeanDefinition();
			String beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(definition, registry);
			registry.registerBeanDefinition(beanName, definition);
		}
	}

}

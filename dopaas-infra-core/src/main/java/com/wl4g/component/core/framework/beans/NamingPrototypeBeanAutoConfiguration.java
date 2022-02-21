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
package com.wl4g.component.core.framework.beans;

import com.wl4g.component.common.log.SmartLogger;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.core.constant.CoreConfigConstant.KEY_NAMING_PROTOYPE_FACTORY;
import static java.util.Objects.nonNull;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.ClassUtils.forName;
import static org.springframework.util.ClassUtils.getDefaultClassLoader;
import static org.springframework.util.CollectionUtils.isEmpty;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

/**
 * Delegate alias prototype bean auto configuration.
 * 
 * @author Wangl.sir <Wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0.0 2019-10-09
 * @since
 */
@ConditionalOnProperty(name = KEY_NAMING_PROTOYPE_FACTORY + ".enabled", matchIfMissing = true)
public class NamingPrototypeBeanAutoConfiguration {

	@Bean
	public NamingPrototypeBeanFactory namingPrototypeBeanFactory(BeanFactory beanFactory) {
		return new NamingPrototypeBeanFactory(beanFactory);
	}

	@Bean
	public NamingPrototypeBeanRegistrar namingPrototypeBeanRegistrar(NamingPrototypeBeanFactory factory) {
		return new NamingPrototypeBeanRegistrar(factory);
	}

	/**
	 * Delegate alias prototype bean importing auto registrar.
	 * 
	 * @author Wangl.sir <Wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0.0 2019-10-09
	 * @since
	 * @see {@link MapperScannerRegistrar} struct implements.
	 */
	static class NamingPrototypeBeanRegistrar implements BeanDefinitionRegistryPostProcessor {
		private final SmartLogger log = getLogger(getClass());

		private final NamingPrototypeBeanFactory factory;

		public NamingPrototypeBeanRegistrar(NamingPrototypeBeanFactory factory) {
			this.factory = notNullOf(factory, "factory");
		}

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		}

		@Override
		public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
			for (String beanName : registry.getBeanDefinitionNames()) {
				BeanDefinition bd = registry.getBeanDefinition(beanName);
				if (nonNull(beanName) && bd.isPrototype()) {
					if (bd instanceof AnnotatedBeanDefinition) {
						log.debug("Register prototype bean with AnnotatedBeanDefinition... - {}", bd);

						AnnotatedBeanDefinition abd = (AnnotatedBeanDefinition) bd;
						String prototypeBeanClassName = null;
						// Bean alias, Used to get prototype bean instance.
						String[] beanAliass = null;
						if (abd instanceof ScannedGenericBeanDefinition) {
							// Using with @Service/@Component...
							AnnotationMetadata metadata = abd.getMetadata();
							if (nonNull(metadata)) {
								prototypeBeanClassName = metadata.getClassName();
								beanAliass = getAnnotationNamingAliasValue(metadata);
							}
						} else {
							/**
							 * Using with {@link Configuration} </br>
							 * See: {@link ConfigurationClassBeanDefinition}
							 */
							MethodMetadata metadata = abd.getFactoryMethodMetadata();
							if (nonNull(metadata)) {
								prototypeBeanClassName = metadata.getReturnTypeName();
								beanAliass = getAnnotationNamingAliasValue(metadata);
							}
						}
						if (!isBlank(prototypeBeanClassName) && nonNull(beanAliass)) {
							try {
								Class<?> beanClass = forName(prototypeBeanClassName, getDefaultClassLoader());
								registerPrototypeBean(beanName, (Class<?>) beanClass, ArrayUtils.add(beanAliass, beanName));
							} catch (LinkageError | ClassNotFoundException e) {
								throw new IllegalStateException(e);
							}
						}
					}
				}
			}

		}

		/**
		 * Gets annotation naming alias value.
		 * 
		 * @param metadata
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		private String[] getAnnotationNamingAliasValue(AnnotatedTypeMetadata metadata) {
			MultiValueMap<String, Object> annotationPropertyValues = metadata
					.getAllAnnotationAttributes(NamingPrototype.class.getName());
			if (!CollectionUtils.isEmpty(annotationPropertyValues)) {
				/**
				 * See:{@link DelegateAlias}
				 */
				Object values = annotationPropertyValues.get("value");
				if (nonNull(values) && values instanceof List) {
					List _values = ((List) values);
					if (!isEmpty(_values)) {
						return (String[]) _values.get(0);
					}
				}
			}
			return null;
		}

		/**
		 * Register prototype bean name alias to
		 * {@link NamingPrototypeBeanFactory}.
		 * 
		 * @param beanName
		 * @param beanClass
		 * @param namingBeanAliass
		 */
		private final void registerPrototypeBean(String beanName, Class<?> beanClass, String... namingBeanAliass) {
			if (nonNull(namingBeanAliass)) {
				for (String alias : namingBeanAliass) {
					if (factory.registerBeanAlias(alias, beanName)) {
						log.debug("Registered prototype bean for alias: {}, class: {}", alias, beanClass);
					}
				}
			}
		}

	}

}

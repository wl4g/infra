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
package com.wl4g.component.integration.springboot.feign.annotation;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.component.common.log.SmartLogger;
import static com.wl4g.component.integration.springboot.feign.constant.SpringBootFeignConstant.KEY_CONFIG_ENABLE;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.endsWithAny;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.StringUtils.hasText;
import static com.wl4g.component.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.component.common.lang.ClassUtils2.isPresent;
import static com.wl4g.component.common.lang.TypeConverts.parseLongOrNull;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.integration.springboot.feign.annotation.EnableSpringBootFeignClients.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * {@link SpringBootFeignClientsRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
class SpringBootFeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
	private static final SmartLogger log = getLogger(SpringBootFeignClientsRegistrar.class);

	@SuppressWarnings("unused")
	private ResourceLoader resourceLoader;
	private Environment environment;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		// Check enabled configuration
		if (!isEnableSpringFeignConfiguration(environment)) {
			log.warn("No enabled Spring Boot/Cloud Feign configuration!");
			return;
		}

		AnnotationAttributes attrs = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(EnableSpringBootFeignClients.class.getName()));
		if (nonNull(attrs)) {
			final Class<?>[] clients = isNull(attrs) ? null : attrs.getClassArray("clients");
			if (isEmptyArray(clients)) {
				/**
				 * Notes：这里需手动合并，因为当@EnableSpringBootFeignClients引用了@EnableFeignClients时，
				 * 且没有依赖spring-cloud-openfeign包时，就不能合并属性值？
				 * 如，配了value就只能获取value的值，而无法获取被@AliasFor的basePackages的值，稳妥起见手动合并。
				 */
				Set<String> scanBasePackages = getScanBasePackages(metadata, attrs).stream().filter(pkg -> !isBlank(pkg))
						.collect(toSet());
				ExcludeExportFeignServicesFilter.setScanBasePackages(scanBasePackages.toArray(new String[0]));

				// Spring Cloud + feign
				if (hasSpringCloudFeignClass()) {
					log.info("The current classpath contains springcloud feign, "
							+ "which automatically enables the SpringCloud + Feign architecture. "
							+ "SpringBoot + Feign has been ignored");
				}
				// Spring Boot + feign
				else {
					registerSpringBootFeignClients(metadata, registry, attrs, scanBasePackages);
				}
			} else {
				for (Class<?> clazz : clients) {
					AnnotatedGenericBeanDefinition definition = new AnnotatedGenericBeanDefinition(clazz);
					configurerFeignClientPropertyValues(definition, attrs.getClassArray(DEFAULT_CONFIGURATION));
					registry.registerBeanDefinition(defaultBeanGenerator.generateBeanName(definition, registry), definition);
				}
			}
		}
	}

	private void registerSpringBootFeignClients(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
			AnnotationAttributes attrs, Set<String> scanBasePackages) {
		new SpringBootFeignClientScanner(registry, attrs.getClassArray(DEFAULT_CONFIGURATION))
				.doScan(StringUtils.toStringArray(scanBasePackages));
	}

	private Set<String> getScanBasePackages(AnnotationMetadata metadata, AnnotationAttributes attrs) {
		Set<String> scanBasePackages = new HashSet<>();
		for (String pkg : (String[]) attrs.get("value")) {
			if (hasText(pkg)) {
				scanBasePackages.add(pkg);
			}
		}
		for (String pkg : (String[]) attrs.get(BASE_PACKAGES)) {
			if (hasText(pkg)) {
				scanBasePackages.add(pkg);
			}
		}
		for (Class<?> clazz : (Class[]) attrs.get(BASE_PACKAGE_CLASSES)) {
			scanBasePackages.add(ClassUtils.getPackageName(clazz));
		}
		if (scanBasePackages.isEmpty()) {
			scanBasePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
		}
		return scanBasePackages;
	}

	private void configurerFeignClientPropertyValues(BeanDefinition definition, @Nullable Class<?>[] defaultConfiguration) {
		// First, find springboot feign client definition.
		MergedAnnotation<?> feignClient = ((AnnotatedBeanDefinition) definition).getMetadata().getAnnotations()
				.get(SpringBootFeignClient.class);
		if (!feignClient.isPresent()) {
			// Fallback, find springcloud feign
			feignClient = ((ScannedGenericBeanDefinition) definition).getMetadata().getAnnotations().get(FeignClient.class);
			if (!feignClient.isPresent()) {
				return;
			}
		}

		String beanClassName = definition.getBeanClassName();
		((GenericBeanDefinition) definition).setBeanClass(SpringBootFeignFactoryBean.class);
		definition.setPrimary(feignClient.getBoolean("primary"));
		MutablePropertyValues propertyValues = definition.getPropertyValues();
		propertyValues.add("targetClass", beanClassName);
		propertyValues.add("url", environment.resolveRequiredPlaceholders(feignClient.getString("url")));// baseUrl
		propertyValues.add("path", getRequestPath(definition, feignClient));
		propertyValues.add("decode404", feignClient.getBoolean("decode404"));
		propertyValues.add("configuration", feignClient.getClassArray("configuration"));
		propertyValues.add("logLevel", feignClient.getValue("logLevel").orElse(null));
		propertyValues.add("connectTimeout", resolveNullableLong(feignClient, "connectTimeout"));
		propertyValues.add("readTimeout", resolveNullableLong(feignClient, "readTimeout"));
		propertyValues.add("writeTimeout", resolveNullableLong(feignClient, "writeTimeout"));
		propertyValues.add("followRedirects", resolveNullableBoolean(feignClient, "followRedirects"));
		propertyValues.add("defaultConfiguration", defaultConfiguration);
	}

	private Boolean resolveNullableBoolean(MergedAnnotation<?> feignClient, String attributeName) {
		try {
			return Boolean.parseBoolean(environment.resolveRequiredPlaceholders(feignClient.getString(attributeName)));
		} catch (NoSuchElementException e) {
			log.debug(format("Cannot resolve %s, using default value", attributeName), e.getMessage());
		}
		return null;
	}

	private Long resolveNullableLong(MergedAnnotation<?> feignClient, String attributeName) {
		try {
			return parseLongOrNull(environment.resolveRequiredPlaceholders(feignClient.getString(attributeName)));
		} catch (NoSuchElementException e) {
			log.debug(format("Cannot resolve %s, using default value", attributeName), e.getMessage());
		}
		return null;
	}

	private String getRequestPath(BeanDefinition definition, MergedAnnotation<?> feignClient) {
		String path = "";

		// Notes: SpringMvcContract It will automatically splice to the URL.
		MergedAnnotation<?> requestMapping = ((AnnotatedBeanDefinition) definition).getMetadata().getAnnotations()
				.get(RequestMapping.class);
		if (!requestMapping.isPresent()) {
			// Fallback, find by @FeignClient/@SpringBootFeignClient
			path = feignClient.getString("path");
		}

		return environment.resolveRequiredPlaceholders(path);

	}

	public static boolean hasSpringCloudFeignClass() {
		return isPresent("org.springframework.cloud.openfeign.FeignClientsRegistrar");
	}

	/**
	 * Check enabled Spring Boot/Cloud Feign configuration.
	 * 
	 * @return
	 */
	public static boolean isEnableSpringFeignConfiguration(Environment environment) {
		return environment.getProperty(KEY_CONFIG_ENABLE, boolean.class, true);
	}

	/**
	 * {@link SpringBootFeignClientScanner}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2020-12-23
	 * @sine v1.0
	 * @see
	 */
	class SpringBootFeignClientScanner extends ClassPathBeanDefinitionScanner {

		@Nullable
		private final Class<?>[] defaultConfiguration;

		public SpringBootFeignClientScanner(BeanDefinitionRegistry registry, Class<?>[] defaultConfiguration) {
			super(registry, true);
			this.defaultConfiguration = defaultConfiguration;
			registerFilters();
			setBeanNameGenerator(defaultBeanGenerator);
		}

		@Override
		public Set<BeanDefinitionHolder> doScan(String... basePackages) {
			Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
			if (beanDefinitions.isEmpty()) {
				log.warn("No spring boot feign client is found in package '" + Arrays.toString(basePackages) + "'.");
			}

			for (BeanDefinitionHolder holder : beanDefinitions) {
				ScannedGenericBeanDefinition definition = (ScannedGenericBeanDefinition) holder.getBeanDefinition();
				configurerFeignClientPropertyValues(definition, defaultConfiguration);
			}

			return beanDefinitions;
		}

		@Override
		protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			return beanDefinition.getMetadata().isInterface();
		}

		private void registerFilters() {
			// Include service interfaces.
			addIncludeFilter(new AnnotationTypeFilter(SpringBootFeignClient.class, true, true));
			addIncludeFilter(new AnnotationTypeFilter(FeignClient.class, true, true)); // For-compatibility

			// Exclude service interfaces.
			addExcludeFilter(new TypeFilter() {
				@Override
				public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
						throws IOException {
					String className = metadataReader.getClassMetadata().getClassName();
					return endsWithAny(className, "package-info", "module-info.java");
				}
			});
		}
	}

	public static final BeanNameGenerator defaultBeanGenerator = (definition,
			registry) -> AnnotationBeanNameGenerator.INSTANCE.generateBeanName(definition, registry) + ".SpringBootFeignClient";

}

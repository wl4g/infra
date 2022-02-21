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
package com.wl4g.component.integration.feign.springcloud.proxy.annotation;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.wl4g.component.integration.feign.springcloud.proxy.FeignProviderProxiesConfigurer;

import static org.springframework.util.StringUtils.*;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

import java.io.IOException;
import java.util.*;

/**
 * Patterned after Spring Integration IntegrationComponentScanRegistrar and
 * RibbonClientsConfigurationRegistgrar.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author Spencer Gibb
 * @author Jakub Narloch
 * @author Venil Noronha
 * @author Gang Li
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see Implementation simulated of refer:
 *      {@link org.springframework.cloud.openfeign.FeignClientsRegistrar}
 */
public class FeignProviderProxiesRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

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
		AnnotationAttributes attrs = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(EnableFeignProviderProxies.class.getName()));
		if (!isNull(attrs)) {
			registerFeignConfigurer(metadata, attrs, registry, generateBaseBeanName(metadata, 0));
		}
	}

	protected void registerFeignConfigurer(AnnotationMetadata metadata, AnnotationAttributes attrs,
			BeanDefinitionRegistry registry, String beanName) {

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FeignProviderProxiesConfigurer.class);

		Class<?> superClass = attrs == null ? null : (Class<?>) attrs.get("superClass");
		superClass = superClass.equals(Void.class) ? null : superClass;
		builder.addPropertyValue("superClass", superClass);

		Set<String> basePackages;
		AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(FeignClient.class);
		final Class<?>[] clients = attrs == null ? null : (Class<?>[]) attrs.get("clients");

		if (clients == null || clients.length == 0) {
			builder.addPropertyValue("includeFilter", annotationTypeFilter);
			basePackages = getBasePackages(metadata);
		} else {
			basePackages = new HashSet<>();
			final Set<String> clientClasses = new HashSet<>();
			for (Class<?> clazz : clients) {
				basePackages.add(ClassUtils.getPackageName(clazz));
				clientClasses.add(clazz.getCanonicalName());
			}
			AbstractClassTestingTypeFilter filter = new AbstractClassTestingTypeFilter() {
				@Override
				protected boolean match(ClassMetadata metadata) {
					String cleaned = metadata.getClassName().replaceAll("\\$", ".");
					return clientClasses.contains(cleaned);
				}
			};
			builder.addPropertyValue("includeFilter", new AllTypeFilter(asList(filter, annotationTypeFilter)));
		}

		builder.addPropertyValue("basePackages", basePackages);
		builder.addPropertyValue("resourceLoader", resourceLoader);
		builder.addPropertyValue("environment", environment);

		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
	}

	protected Set<String> getBasePackages(AnnotationMetadata metadata) {
		Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableFeignProviderProxies.class.getCanonicalName());

		Set<String> basePackages = new HashSet<>();
		for (String pkg : (String[]) attributes.get("value")) {
			if (hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (String pkg : (String[]) attributes.get("basePackages")) {
			if (hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
			basePackages.add(ClassUtils.getPackageName(clazz));
		}

		if (basePackages.isEmpty()) {
			basePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
		}
		return basePackages;
	}

	private static String generateBaseBeanName(AnnotationMetadata metadata, int index) {
		return metadata.getClassName() + "#" + FeignProviderProxiesRegistrar.class.getSimpleName() + "#" + index;
	}

	/**
	 * Helper class to create a {@link TypeFilter} that matches if all the
	 * delegates match.
	 *
	 * @author Oliver Gierke
	 */
	public static class AllTypeFilter implements TypeFilter {

		private final List<TypeFilter> delegates;

		/**
		 * Creates a new {@link AllTypeFilter} to match if all the given
		 * delegates match.
		 *
		 * @param delegates
		 *            must not be {@literal null}.
		 */
		public AllTypeFilter(List<TypeFilter> delegates) {
			Assert.notNull(delegates, "This argument is required, it must not be null");
			this.delegates = delegates;
		}

		@Override
		public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
			for (TypeFilter filter : this.delegates) {
				if (!filter.match(metadataReader, metadataReaderFactory)) {
					return false;
				}
			}
			return true;
		}

	}

}
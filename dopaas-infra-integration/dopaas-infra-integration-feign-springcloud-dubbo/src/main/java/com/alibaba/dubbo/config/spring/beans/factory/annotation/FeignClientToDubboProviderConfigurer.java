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
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.AnnotationPropertyValuesAdapter;
import com.alibaba.dubbo.config.spring.context.annotation.DubboClassPathBeanDefinitionScanner;

import static com.alibaba.dubbo.config.spring.util.ObjectUtils.of;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.ClassUtils.resolveClassName;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static com.wl4g.component.common.lang.Assert2.hasText;
import static com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyUtil.FEIGN_DUBBO_ORDER;
import static com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyUtil.generateFeignProxyBeanName;
import static com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyUtil.isFeignProxyBean;

/**
 * {@code @FeignClient} service to dubbo's provider configurer.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-20
 * @sine v1.0
 * @see {@link com.alibaba.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor}
 * @see {@link com.alibaba.dubbo.config.spring.ServiceBean}
 * @see {@link com.alibaba.dubbo.config.spring.ReferenceBean}
 */
@Order(FEIGN_DUBBO_ORDER)
public class FeignClientToDubboProviderConfigurer
		implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ResourceLoaderAware, BeanClassLoaderAware {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Set<String> packagesToScan;

	private Environment environment;
	private ResourceLoader resourceLoader;
	private ClassLoader classLoader;
	private Service defaultService;

	public FeignClientToDubboProviderConfigurer(String... packagesToScan) {
		this(asList(packagesToScan));
	}

	public FeignClientToDubboProviderConfigurer(Collection<String> packagesToScan) {
		this(new LinkedHashSet<String>(packagesToScan));
	}

	public FeignClientToDubboProviderConfigurer(Set<String> packagesToScan) {
		this.packagesToScan = packagesToScan;
		// Generate {@code @Service} default configuration instance.
		@Service
		final class DefaultServiceClass {
		}
		this.defaultService = DefaultServiceClass.class.getAnnotation(Service.class);
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		Set<String> resolvedPackagesToScan = resolvePackagesToScan(packagesToScan);
		if (!CollectionUtils.isEmpty(resolvedPackagesToScan)) {
			registerServiceBeans(resolvedPackagesToScan, registry);
		} else {
			log.warn("packagesToScan is empty , ServiceBean registry will be ignored!");
		}
	}

	/**
	 * Registers Beans whose classes was annotated {@link FeignClient}
	 *
	 * @param packagesToScan
	 *            The base packages to scan
	 * @param registry
	 *            {@link BeanDefinitionRegistry}
	 */
	private void registerServiceBeans(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
		BeanNameGenerator beanNameGenerator = resolveBeanNameGenerator(registry);

		DubboClassPathBeanDefinitionScanner scanner = registerServiceBeansWithScans(packagesToScan, registry, beanNameGenerator);
		registerServiceBeansWithFeignProxies(registry, scanner, beanNameGenerator);
	}

	/**
	 * Scan the beans that register all service implementations. </br>
	 * 
	 * @param packagesToScan
	 * @param registry
	 * @param beanNameGenerator
	 * @return
	 */
	@Deprecated
	private DubboClassPathBeanDefinitionScanner registerServiceBeansWithScans(Set<String> packagesToScan,
			BeanDefinitionRegistry registry, BeanNameGenerator beanNameGenerator) {

		DubboClassPathBeanDefinitionScanner scanner = new DubboClassPathBeanDefinitionScanner(registry, environment,
				resourceLoader);
		scanner.setBeanNameGenerator(beanNameGenerator);
		scanner.addIncludeFilter(new AnnotationTypeFilter(FeignClient.class, true, true));

		// for (String packageToScan : packagesToScan) {
		// /**
		// * Scan injection will still be performed in
		// * {@link
		// com.alibaba.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor#registerServiceBeans()}
		// * </br>
		// * Moreover, the main purpose here is to use the
		// * {@link org.springframework.stereotype.Service} bean directly. Of
		// * course, there is no need to inject it manually, because spring
		// * will automatically recognize and inject it.
		// */
		// // Registers @FeignClient Bean first
		// scanner.scan(packageToScan);
		//
		// // Finds all BeanDefinitionHolders of @FeignClient whether
		// // @ComponentScan scans or not.
		// Set<BeanDefinition> beanDefinitions =
		// scanner.findCandidateComponents(packageToScan);
		// if (!CollectionUtils.isEmpty(beanDefinitions)) {
		// for (BeanDefinition beanDefinition : beanDefinitions) {
		// registerServiceBean(beanDefinition, registry, scanner,
		// beanNameGenerator);
		// }
		// log.info(beanDefinitions.size() + " annotated @FeignClient Components
		// { " + beanDefinitions
		// + " } were scanned under package[" + packageToScan + "]");
		// } else {
		// log.warn("No Spring Bean annotating @FeignClient was found under
		// package[" + packageToScan + "]");
		// }
		// }

		return scanner;
	}

	/**
	 * Register all beans that are represented by the feign rest proxy. </br>
	 * </br>
	 * Scan injection will still be performed in
	 * {@link com.alibaba.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor#registerServiceBeans()}
	 * 
	 * @param registry
	 * @param scanner
	 * @param beanNameGenerator
	 * @see {@link com.wl4g.component.integration.springcloud.feign.FeignProxyController}
	 * @see {@link com.wl4g.component.integration.springcloud.feign.FeignProviderProxiesConfigurer#registerFeignProxyBean()}
	 */
	private void registerServiceBeansWithFeignProxies(BeanDefinitionRegistry registry,
			DubboClassPathBeanDefinitionScanner scanner, BeanNameGenerator beanNameGenerator) {

		for (String beanName : registry.getBeanDefinitionNames()) {
			BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
			if (isFeignProxyBean(beanDefinition)) {
				registerServiceBean(beanDefinition, registry, scanner, beanNameGenerator);
			}
		}
	}

	private BeanNameGenerator resolveBeanNameGenerator(BeanDefinitionRegistry registry) {

		// BeanNameGenerator beanNameGenerator = null;
		// if (registry instanceof SingletonBeanRegistry) {
		// SingletonBeanRegistry singletonBeanRegistry =
		// SingletonBeanRegistry.class.cast(registry);
		// beanNameGenerator = (BeanNameGenerator)
		// singletonBeanRegistry.getSingleton(CONFIGURATION_BEAN_NAME_GENERATOR);
		// }
		// if (beanNameGenerator == null) {
		// log.warn(
		// "BeanNameGenerator bean can't be found in BeanFactory with name [" +
		// CONFIGURATION_BEAN_NAME_GENERATOR + "]");
		// log.warn("BeanNameGenerator will be a instance of " +
		// AnnotationBeanNameGenerator.class.getName()
		// + " , it maybe a potential problem on bean name generation.");
		// beanNameGenerator = new AnnotationBeanNameGenerator();
		// }
		// return beanNameGenerator;

		/**
		 * In order to enhance the integration of feign and Dubbo, the ability
		 * to expose rest services only by using spring
		 * {@link org.springframework.stereotype.Service} instead of
		 * {@link RestController} is realized {@link ServiceBean#ref} The bean
		 * of the proxy should be set, see:
		 * {@link com.wl4g.component.integration.springcloud.feign.FeignProviderProxiesRegistrar#registerFeignClients()}
		 */
		return (definition, registry0) -> {
			String beanClassName = hasText(definition.getBeanClassName(), "No bean class name set");
			return generateFeignProxyBeanName(beanClassName);
		};

	}

	/**
	 * Register dubbo service bean.
	 * 
	 * @param beanDefinition
	 * @param registry
	 * @param scanner
	 * @param beanNameGenerator
	 */
	private void registerServiceBean(BeanDefinition beanDefinition, BeanDefinitionRegistry registry,
			DubboClassPathBeanDefinitionScanner scanner, BeanNameGenerator beanNameGenerator) {

		String annotatedServiceBeanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
		BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, annotatedServiceBeanName);

		Class<?> beanClass = resolveClass(beanDefinitionHolder);
		Service service = findAnnotation(beanClass, Service.class);
		if (null == service) {
			service = this.defaultService;
		}

		Class<?> interfaceClass = resolveServiceInterfaceClass(beanClass, service);
		AbstractBeanDefinition serviceBeanDefinition = buildServiceBeanDefinition(service, interfaceClass,
				annotatedServiceBeanName);

		// ServiceBean Bean name
		String serviceBeanName = generateServiceBeanName(service, interfaceClass, annotatedServiceBeanName);

		// check duplicated candidate bean
		if (scanner.checkCandidate(serviceBeanName, serviceBeanDefinition)) {
			registry.registerBeanDefinition(serviceBeanName, serviceBeanDefinition);
			log.info("The BeanDefinition[" + serviceBeanDefinition + "] of ServiceBean has been registered with name : "
					+ serviceBeanName);
		} else {
			log.warn("The Duplicated BeanDefinition[" + serviceBeanDefinition + "] of ServiceBean[ bean name : " + serviceBeanName
					+ "] was be found , Did @DubboComponentScan scan to same package in many times?");
		}
	}

	/**
	 * Generates the bean name of {@link ServiceBean}
	 *
	 * @param service
	 * @param interfaceClass
	 *            the class of interface annotated {@link Service}
	 * @param annotatedServiceBeanName
	 *            the bean name of annotated {@link Service}
	 * @return ServiceBean@interfaceClassName#annotatedServiceBeanName
	 * @since 2.5.9
	 */
	private String generateServiceBeanName(Service service, Class<?> interfaceClass, String annotatedServiceBeanName) {
		StringBuilder beanNameBuilder = new StringBuilder(ServiceBean.class.getSimpleName());
		beanNameBuilder.append(SEPARATOR).append(annotatedServiceBeanName);

		String interfaceClassName = interfaceClass.getName();
		beanNameBuilder.append(SEPARATOR).append(interfaceClassName);

		String version = service.version();
		if (StringUtils.hasText(version)) {
			beanNameBuilder.append(SEPARATOR).append(version);
		}

		String group = service.group();
		if (StringUtils.hasText(group)) {
			beanNameBuilder.append(SEPARATOR).append(group);
		}

		return beanNameBuilder.toString();
	}

	private Class<?> resolveServiceInterfaceClass(Class<?> annotatedServiceBeanClass, Service service) {
		Class<?> interfaceClass = service.interfaceClass();
		if (void.class.equals(interfaceClass)) {
			interfaceClass = null;
			String interfaceClassName = service.interfaceName();
			if (StringUtils.hasText(interfaceClassName)) {
				if (ClassUtils.isPresent(interfaceClassName, classLoader)) {
					interfaceClass = resolveClassName(interfaceClassName, classLoader);
				}
			}
		}

		if (interfaceClass == null) {
			Class<?>[] allInterfaces = annotatedServiceBeanClass.getInterfaces();
			if (allInterfaces.length > 0) {
				interfaceClass = allInterfaces[0];
			}
		}

		Assert.notNull(interfaceClass, "@Service interfaceClass() or interfaceName() or interface class must be present!");
		Assert.isTrue(interfaceClass.isInterface(), "The type that was annotated @Service is not an interface!");
		return interfaceClass;
	}

	private Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder) {
		BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
		return resolveClassName(beanDefinition.getBeanClassName(), classLoader);
	}

	private Set<String> resolvePackagesToScan(Set<String> packagesToScan) {
		Set<String> resolvedPackagesToScan = new LinkedHashSet<String>(packagesToScan.size());
		for (String packageToScan : packagesToScan) {
			if (StringUtils.hasText(packageToScan)) {
				String resolvedPackageToScan = environment.resolvePlaceholders(packageToScan.trim());
				resolvedPackagesToScan.add(resolvedPackageToScan);
			}
		}
		return resolvedPackagesToScan;
	}

	private AbstractBeanDefinition buildServiceBeanDefinition(Service service, Class<?> interfaceClass,
			String annotatedServiceBeanName) {
		BeanDefinitionBuilder builder = rootBeanDefinition(ServiceBean.class);
		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

		MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
		String[] ignoreAttributeNames = of("provider", "monitor", "application", "module", "registry", "protocol", "interface");
		propertyValues.addPropertyValues(new AnnotationPropertyValuesAdapter(service, environment, ignoreAttributeNames));

		// References "ref" property to annotated-@Service Bean
		addPropertyReference(builder, "ref", annotatedServiceBeanName);
		// Set interface
		builder.addPropertyValue("interface", interfaceClass.getName());

		/**
		 * Add {@link com.alibaba.dubbo.config.ProviderConfig} Bean reference
		 */
		String providerConfigBeanName = service.provider();
		if (StringUtils.hasText(providerConfigBeanName)) {
			addPropertyReference(builder, "provider", providerConfigBeanName);
		}

		/**
		 * Add {@link com.alibaba.dubbo.config.MonitorConfig} Bean reference
		 */
		String monitorConfigBeanName = service.monitor();
		if (StringUtils.hasText(monitorConfigBeanName)) {
			addPropertyReference(builder, "monitor", monitorConfigBeanName);
		}

		/**
		 * Add {@link com.alibaba.dubbo.config.ApplicationConfig} Bean reference
		 */
		String applicationConfigBeanName = service.application();
		if (StringUtils.hasText(applicationConfigBeanName)) {
			addPropertyReference(builder, "application", applicationConfigBeanName);
		}

		/**
		 * Add {@link com.alibaba.dubbo.config.ModuleConfig} Bean reference
		 */
		String moduleConfigBeanName = service.module();
		if (StringUtils.hasText(moduleConfigBeanName)) {
			addPropertyReference(builder, "module", moduleConfigBeanName);
		}

		/**
		 * Add {@link com.alibaba.dubbo.config.RegistryConfig} Bean reference
		 */
		String[] registryConfigBeanNames = service.registry();
		List<RuntimeBeanReference> registryRuntimeBeanReferences = toRuntimeBeanReferences(registryConfigBeanNames);
		if (!registryRuntimeBeanReferences.isEmpty()) {
			builder.addPropertyValue("registries", registryRuntimeBeanReferences);
		}

		/**
		 * Add {@link com.alibaba.dubbo.config.ProtocolConfig} Bean reference
		 */
		String[] protocolConfigBeanNames = service.protocol();
		List<RuntimeBeanReference> protocolRuntimeBeanReferences = toRuntimeBeanReferences(protocolConfigBeanNames);

		if (!protocolRuntimeBeanReferences.isEmpty()) {
			builder.addPropertyValue("protocols", protocolRuntimeBeanReferences);
		}

		return builder.getBeanDefinition();
	}

	private ManagedList<RuntimeBeanReference> toRuntimeBeanReferences(String... beanNames) {
		ManagedList<RuntimeBeanReference> runtimeBeanReferences = new ManagedList<RuntimeBeanReference>();
		if (!ObjectUtils.isEmpty(beanNames)) {
			for (String beanName : beanNames) {
				String resolvedBeanName = environment.resolvePlaceholders(beanName);
				runtimeBeanReferences.add(new RuntimeBeanReference(resolvedBeanName));
			}
		}
		return runtimeBeanReferences;
	}

	private void addPropertyReference(BeanDefinitionBuilder builder, String propertyName, String beanName) {
		String resolvedBeanName = environment.resolvePlaceholders(beanName);
		builder.addPropertyReference(propertyName, resolvedBeanName);
	}

	private static final String SEPARATOR = ":";

}
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
package com.wl4g.component.integration.feign.springcloud.proxy;

import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.TypeFilter;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.ClassUtils.CGLIB_CLASS_SEPARATOR;

import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyUtil.FEIGNPROXY_INTERFACE_CLASS_ATTRIBUTE;
import static com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyUtil.FEIGN_PROXY_ORDER;
import static com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyUtil.generateFeignProxyBeanName;

/**
 * The scanning injection is realized with reference to
 * {@link org.mybatis.spring.mapper.MapperScannerConfigurer},
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-23
 * @sine v1.0
 * @see
 */
@Order(FEIGN_PROXY_ORDER)
public class FeignProviderProxiesConfigurer
		implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

	protected final Logger log = getLogger(getClass());

	private ApplicationContext applicationContext;
	@SuppressWarnings("unused")
	private String beanName;

	private TypeFilter includeFilter;
	private Class<?> superClass;
	private Set<String> basePackages;
	@SuppressWarnings("unused")
	@Deprecated
	private ResourceLoader resourceLoader;
	private Environment environment;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setIncludeFilter(TypeFilter includeFilter) {
		this.includeFilter = includeFilter;
	}

	public void setSuperClass(Class<?> superClass) {
		this.superClass = superClass;
	}

	public void setBasePackages(Set<String> basePackages) {
		this.basePackages = basePackages;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	// --- Feign provider proxies process. ---

	@Override
	public void afterPropertiesSet() throws Exception {
		notNull(this.basePackages, "Property 'basePackage' is required");
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// left intentionally blank
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		ClassPathScanningCandidateComponentProvider scanner = createScanner();
		// scanner.setResourceLoader(resourceLoader);
		scanner.setResourceLoader(applicationContext);
		scanner.addIncludeFilter(includeFilter);

		for (String basePackage : basePackages) {
			Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
			for (BeanDefinition candidateComponent : candidateComponents) {
				if (candidateComponent instanceof AnnotatedBeanDefinition) {
					// verify annotated class is an interface
					AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
					AnnotationMetadata annoMetadata = beanDefinition.getMetadata();
					isTrue(annoMetadata.isInterface(), "@FeignClient can only be specified on an interface");

					Class<?> interfaceClass = null;
					try {
						interfaceClass = Class.forName(beanDefinition.getBeanClassName());
					} catch (ClassNotFoundException e) {
						log.warn("Not found class : '{}'", beanDefinition.getBeanClassName());
					}
					// Proxy feign client
					if (interfaceClass != null) {
						registerFeignProxyBean(registry, interfaceClass);
					}
				}
			}
		}

	}

	protected ClassPathScanningCandidateComponentProvider createScanner() {
		return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				boolean isCandidate = false;
				if (beanDefinition.getMetadata().isIndependent()) {
					if (!beanDefinition.getMetadata().isAnnotation()) {
						isCandidate = true;
					}
				}
				return isCandidate;
			}
		};
	}

	/**
	 * Register feign client rest proxy bean with {@link FeignProxyController}
	 * </br>
	 * [Note]: Bean definition className must be a proxy class, refer to:
	 * {@link com.alibaba.dubbo.config.spring.beans.factory.annotation.FeignClientToDubboProviderConfigurer#resolveServiceInterfaceClass()}
	 * {@link com.wl4g.component.integration.springcloud.util.FeignDubboUtils.isFeignProxyBean()}
	 * 
	 * @param registry
	 * @param interfaceClass
	 */
	protected void registerFeignProxyBean(BeanDefinitionRegistry registry, final Class<?> interfaceClass) {
		Enhancer enhancer = new Enhancer() {
			@Override
			protected void setNamePrefix(String namePrefix) {
				super.setNamePrefix(interfaceClass.getName().concat(CGLIB_CLASS_SEPARATOR)
						.concat(FeignProxyController.class.getSimpleName()));
			}
		};
		if (superClass != null) {
			enhancer.setSuperclass(superClass);
		}
		enhancer.setCallback(new FeignProxyInvocationHandler(applicationContext, interfaceClass));
		enhancer.setInterfaces(new Class[] { interfaceClass, FeignProxyController.class });
		Object proxy = enhancer.create();

		RootBeanDefinition proxyBeanDefinition = new RootBeanDefinition();
		proxyBeanDefinition.setInstanceSupplier(() -> proxy);
		proxyBeanDefinition.setAttribute(FEIGNPROXY_INTERFACE_CLASS_ATTRIBUTE, interfaceClass);
		// Must be a proxy class. refer:
		// FeignClientDubboProviderConfigurer#resolveServiceInterfaceClass()
		proxyBeanDefinition.setBeanClassName(proxy.getClass().getName());
		/**
		 * Solve the unique problem of multiple candidate bean injection.</br>
		 * for example errors:
		 * 
		 * <pre>
		 * Field organizationService in com.wl4g.iam.service.impl.RoleServiceImpl required a single bean, but 3 were found:
		 * - organizationServiceImpl: defined in file [.../com/wl4g/iam/service/impl/OrganizationServiceImpl.class]
		 * - organizationService$FeignProxyController: defined in null
		 * - organizationServiceImpl$FeignProxyController: defined in file [.../com/wl4g/iam/service/impl/OrganizationServiceImpl.class]
		 * </pre>
		 */
		proxyBeanDefinition.setPrimary(true);

		String proxyBeanName = generateFeignProxyBeanName(interfaceClass.getName());
		registry.registerBeanDefinition(proxyBeanName, proxyBeanDefinition);
		log.info("Register feign client rest proxy: '{}' by interfaceClass: '{}'", proxy, interfaceClass);
	}

}

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
package com.wl4g.component.core.framework.proxy;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.lang.ClassUtils2.anyTypeOf;
import static com.wl4g.component.common.lang.ClassUtils2.getPackageName;
import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findFieldNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.getField;
import static com.wl4g.component.core.constant.CoreConfigConstant.KEY_SMART_PROXY;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isFinal;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.startsWithAny;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.ClassUtils.CGLIB_CLASS_SEPARATOR;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

import com.wl4g.component.common.log.SmartLogger;

/**
 * Intelligent AOP enhanced proxy configurator supports proxy creation of
 * various types of beans, such as: Simple Object, {@link FactoryBean} and
 * {@link org.mybatis.spring.mapper.MapperFactoryBean} etc.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-23
 * @sine v1.0
 * @see
 */
@ConditionalOnProperty(name = KEY_SMART_PROXY + ".enabled", matchIfMissing = true)
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class SmartProxyAutoConfiguration implements InitializingBean, BeanPostProcessor {
	protected final SmartLogger log = getLogger(getClass());

	private final Map<Class<?>, List<SmartProxyFilter>> knownProxiedMapping = new ConcurrentHashMap<>(4);

	@Value("${" + KEY_SMART_PROXY + ".base-packages:}")
	private String[] basePackages;
	@Autowired
	private DefaultListableBeanFactory beanFactory;
	@Autowired(required = false)
	private List<SmartProxyFilter> processors;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.processors = safeList(processors);
		AnnotationAwareOrderComparator.sort(processors);
	}

	/**
	 * {@link AnnotationAwareAspectJAutoProxyCreator#postProcessAfterInitialization(Object, String)}
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (skipNoShouldProxy(bean, beanName)) {
			return bean;
		}

		// Gets actual original target class.
		Class<?> targetClass = getOriginalActualTargetClass(bean, beanName);
		if (isNull(targetClass)) {// Unable proxy
			log.warn("Skip bean that cannot be proxied, because cannot get actual original target class. - '{}'", bean);
			return bean;
		}

		// Skip exclude beans
		if (skipExcludeBean(bean, beanName, targetClass)) {
			return bean;
		}

		// Add known proxied mappings.
		boolean hasSupportProxied = false;
		for (SmartProxyFilter p : processors) {
			if (p.supportTypeProxy(bean, targetClass)) {
				hasSupportProxied = true;
				// Add proxies handler mapping.
				addKnownProxiedMapping(targetClass, p);
			}
		}

		if (hasSupportProxied) {
			// Wrap to proxy bean.
			return wrapEnhanceProxyBean(bean, beanName, targetClass);
		}

		return bean;
	}

	/**
	 * Wrap object bean to enhanced proxy bean.
	 * 
	 * @param bean
	 * @param beanName
	 * @param targetClass
	 * @return
	 */
	private Object wrapEnhanceProxyBean(Object bean, String beanName, final Class<?> targetClass) {
		Enhancer enhancer = new Enhancer() {
			@Override
			protected void setNamePrefix(String namePrefix) {
				Class<?> proxyClass = (bean instanceof FactoryBean) ? SmartFactoryBeanProxy.class : SmartProxy.class;
				super.setNamePrefix(targetClass.getName().concat(CGLIB_CLASS_SEPARATOR).concat(proxyClass.getSimpleName()));
			}
		};
		// Sets enhanced interfaces or superClass.
		List<Class<?>> enhancedInterfaces = new ArrayList<>(4);
		if (targetClass.isInterface()) { // Is interface
			enhancedInterfaces.add(targetClass);
		} else { // Is class
			// [START] extension logic.(In order to enhance the class modified
			// by final)
			SmartProxyFor proxyFor = findAnnotation(targetClass, SmartProxyFor.class);
			if (nonNull(proxyFor)) {
				// Priority is given to display defined enhanced interfaces.
				Class<?>[] interfaces = proxyFor.interfaces();
				if (isEmptyArray(interfaces)) {
					// Fallback, using all interfaces of the targetClass.
					interfaces = targetClass.getInterfaces();
				}
				enhancedInterfaces.addAll(asList(interfaces));
				// [END] extension logic.
			} else { // No interfaces
				isTrue(!isFinal(targetClass.getModifiers()),
						() -> format("Enhance proxy target class must a interface or not final type. - %s", targetClass));
				enhancer.setSuperclass(targetClass);
			}
		}
		if (bean instanceof FactoryBean) {
			enhancedInterfaces.add(SmartFactoryBeanProxy.class);
			enhancer.setCallback(new DispatcherFactoryBeanSmartProxyInvocation(beanFactory, this, bean, beanName, targetClass));
		} else {
			enhancedInterfaces.add(SmartProxy.class);
			enhancer.setCallback(new DispatcherSmartProxyInvocation(beanFactory, this, beanName, targetClass, () -> bean));
		}
		enhancer.setInterfaces(enhancedInterfaces.toArray(new Class[0]));

		final Object proxy = enhancer.create();
		log.info("Created smart proxy: '{}' of actual original target class: '{}'", proxy, targetClass);
		return proxy;
	}

	/**
	 * Addidition known proxied and {@link SmartProxyFilter} mappings.
	 * 
	 * @param targetClass
	 * @param processor
	 */
	private void addKnownProxiedMapping(Class<?> targetClass, SmartProxyFilter processor) {
		List<SmartProxyFilter> processors = knownProxiedMapping.get(targetClass);
		if (isNull(processors)) {
			processors = new ArrayList<>(4);
		}
		if (!processors.contains(processor)) {
			processors.add(processor);
		}
		knownProxiedMapping.put(targetClass, processors);
	}

	/**
	 * Gets known proxied and {@link SmartProxyFilter} processor.
	 * 
	 * @param targetClass
	 * @return
	 */
	final List<SmartProxyFilter> getProcessors(Class<?> targetClass) {
		return safeList(knownProxiedMapping.get(targetClass));
	}

	/**
	 * Whether kip not should proxy bean.
	 * 
	 * @param bean
	 * @param beanName
	 * @return
	 */
	protected boolean skipNoShouldProxy(Object bean, String beanName) {
		// Ignore spring internal bean?
		if (startsWithAny(bean.getClass().getName(), EXCLUDE_BASE_PACKAGES)) {
			return true;
		}
		// Proxied bean? (Only SmartProxy proxy is allowed once)
		else if (anyTypeOf(bean, SmartFactoryBeanProxy.class, SmartProxy.class)) {
			return true;
		}
		return false;
	}

	/**
	 * Skip exclude beans by base packages.
	 * 
	 * @param bean
	 * @param beanName
	 * @param targetClass
	 * @return
	 */
	protected boolean skipExcludeBean(Object bean, String beanName, Class<?> targetClass) {
		return !(isEmptyArray(basePackages) || startsWithAny(getPackageName(targetClass), basePackages));
	}

	/**
	 * Get the actual type of the bean according to the default policy. If the
	 * bean has been proxied, it represents the interface or class of the actual
	 * proxy.
	 * 
	 * @param bean
	 * @param beanName
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected Class<?> getOriginalActualTargetClass(Object bean, String beanName) {
		if (bean.getClass().getName().contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {// CGLIB-proxy-bean
			Class<?> targetClass = bean.getClass().getSuperclass();
			if (nonNull(targetClass)) {
				return targetClass;
			}
			Class<?>[] interfaces = bean.getClass().getInterfaces();
			return (nonNull(interfaces) && interfaces.length > 0) ? interfaces[0] : null;
		} else if (Proxy.class.isInstance(bean)) {// JDK proxy bean
			return getField(proxyInterfaceField, bean, true).getClass();
		} else if (bean instanceof FactoryBean) {// FactoryBean
			// e.g: org.mybatis.spring.mapper.MapperFactoryBean#getObjectType()
			if (nonNull(mapperFactoryBeanClass) && mapperFactoryBeanClass.isInstance(bean)) {
				Object proxyInvocation = null;
				try {
					proxyInvocation = ((FactoryBean) bean).getObject();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
				if (nonNull(proxyInvocation) && Proxy.class.isInstance(proxyInvocation)) {
					Object mapperProxy = getField(proxyInterfaceField, proxyInvocation, true);
					// e.g: org.apache.ibatis.binding.MapperProxy@6cb2ee5c
					// ==> com.sun.proxy.$Proxy96
					if (nonNull(mapperProxyClass) && mapperProxyClass.isInstance(mapperProxy)) {
						return getField(mapperProxyInterfaceField, mapperProxy, true);
					}
					return mapperProxy.getClass();
				}
			}
			return ((FactoryBean) bean).getObjectType();
		}
		return bean.getClass();
	}

	/**
	 * The type of is {@link FactoryBean} smart enhanced proxy mark interface.
	 */
	@SuppressWarnings("rawtypes")
	public static interface SmartFactoryBeanProxy extends SmartProxy, FactoryBean {
	}

	/**
	 * Generic smart enhanced proxy mark interface.
	 */
	public static interface SmartProxy {
	}

	/**
	 * Excludes bean class base packages.
	 */
	public static final String[] EXCLUDE_BASE_PACKAGES = { "org.springframework", "java.", "javax." };

	// JDK proxy interface field.
	public static final Field proxyInterfaceField = findFieldNullable(Proxy.class, "h", InvocationHandler.class);
	// Mybatis mapper proxy classes and methods and fields.
	public static final Class<?> mapperFactoryBeanClass = resolveClassNameNullable("org.mybatis.spring.mapper.MapperFactoryBean");
	public static final Field mapperFactoryBeanInterfaceField = findFieldNullable(mapperFactoryBeanClass, "mapperInterface",
			Class.class);
	public static final Class<?> mapperProxyClass = resolveClassNameNullable("org.apache.ibatis.binding.MapperProxy");
	public static final Field mapperProxyInterfaceField = findFieldNullable(mapperProxyClass, "mapperInterface", Class.class);

}

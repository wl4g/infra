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
package com.wl4g.component.core.utils.context;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.isNull;

import java.util.Map;

import javax.annotation.Nullable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.reactive.context.ConfigurableReactiveWebEnvironment;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.WebApplicationContext;

import com.wl4g.component.common.lang.ClassUtils2;
import com.wl4g.component.common.log.SmartLogger;

/**
 * Spring container context holder.</br>
 * 
 * Deprecated use classes, because there may be injection disorder leading to
 * exceptions when using @{@link Configuration} and @{@link Component}
 * or @{@link Service} etc.
 * 
 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2019-07-12
 * @since
 */
@Component("xcloudComponentSpringContextHolder")
@Lazy(false)
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {

	private static final SmartLogger log = getLogger(SpringContextHolder.class);

	private static ApplicationContext _actx;

	/**
	 * Implement the ApplicationContextAware interface, injecting the Context
	 * into a static variable.
	 */
	@Override
	public void setApplicationContext(ApplicationContext actx) {
		log.debug("Inject the ApplicationContext into the SpringContextHolder:" + actx);
		if (_actx != null) {
			log.warn("The ApplicationContext in the SpringContextHolder is overridden. The original ApplicationContext is:"
					+ _actx);
		}
		_actx = actx; // NOSONAR

	}

	/**
	 * Implement the DisposableBean interface to clean up static variables when
	 * the Context is closed.
	 */
	public void destroy() throws Exception {
		clear();
	}

	/**
	 * Gets the ApplicationContext stored in a static variable.
	 */
	public static ApplicationContext getApplicationContext() {
		assertContextInjected();
		return _actx;
	}

	/**
	 * Gets the bean from the static variable applicationContext, automatically
	 * transform to the type of the assigned object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) {
		assertContextInjected();
		return (T) _actx.getBean(name);
	}

	/**
	 * Gets the bean from the static variable applicationContext, automatically
	 * transform to the type of the assigned object.
	 */
	public static <T> T getBean(Class<T> requiredType) {
		assertContextInjected();
		return (T) _actx.getBean(requiredType);
	}

	/**
	 * Gets the beans from the static variable applicationContext, automatically
	 * transform to the type of the assigned object.
	 */
	public static <T> Map<String, T> getBeans(Class<T> requiredType) {
		assertContextInjected();
		return _actx.getBeansOfType(requiredType);
	}

	/**
	 * Clear the ApplicationContext in the SpringContextHolder to Null.
	 */
	public static void clear() {
		log.debug("Clear the ApplicationContext in the SpringContextHolder:" + _actx);
		_actx = null;
	}

	/**
	 * Check that the ApplicationContext is not empty.
	 */
	private static void assertContextInjected() {
		if (_actx == null) {
			throw new IllegalStateException("applicaitonContext未注入,请在springmvc-servlet.xml中定义SpringContextHolder");
		}
	}

	/**
	 * {@link org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition#isServletWebApplication}
	 * 
	 * @return
	 */
	public static boolean isServletWebApplication(@Nullable ClassLoader classLoader, @Nullable BeanFactory beanFactory,
			@Nullable Environment environment, @Nullable ResourceLoader resourceLoader) {

		if (!ClassUtils2.isPresent(SERVLET_WEB_APPLICATION_CLASS, classLoader)) {
			return false;
		}
		if (!isNull(beanFactory) && beanFactory instanceof ConfigurableBeanFactory) {
			String[] scopes = ((ConfigurableBeanFactory) beanFactory).getRegisteredScopeNames();
			if (ObjectUtils.containsElement(scopes, "session")) {
				return true;
			}
		}
		if (!isNull(environment) && (environment instanceof ConfigurableWebEnvironment)) {
			return true;
		}
		if (!isNull(resourceLoader) && (resourceLoader instanceof WebApplicationContext)) {
			return true;
		}
		return false;
	}

	/**
	 * {@link org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition#isReactiveWebApplication}
	 * 
	 * @return
	 */
	public static boolean isReactiveWebApplication(@Nullable ClassLoader classLoader, @Nullable Environment environment,
			@Nullable ResourceLoader resourceLoader) {
		if (!ClassUtils2.isPresent(REACTIVE_WEB_APPLICATION_CLASS, classLoader)) {
			return false;
		}
		if (!isNull(environment) && (environment instanceof ConfigurableReactiveWebEnvironment)) {
			return true;
		}
		if (!isNull(resourceLoader) && (resourceLoader instanceof ReactiveWebApplicationContext)) {
			return true;
		}
		return false;
	}

	private static final String SERVLET_WEB_APPLICATION_CLASS = "org.springframework.web.context.support.GenericWebApplicationContext";
	private static final String REACTIVE_WEB_APPLICATION_CLASS = "org.springframework.web.reactive.HandlerResult";

}
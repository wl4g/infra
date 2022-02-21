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

import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import com.wl4g.component.common.log.SmartLogger;

/**
 * {@link AbstractDispatcherProxyInvocation}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author liqiu
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see
 */
public abstract class AbstractDispatcherProxyInvocation implements ProxyInvocation {
	protected final SmartLogger log = getLogger(getClass());

	protected final DefaultListableBeanFactory beanFactory;
	protected final SmartProxyAutoConfiguration configurer;
	protected final boolean isSingleton;
	protected final String targetBeanName;
	protected final Class<?> targetClass; // actual original target class

	protected AbstractDispatcherProxyInvocation(DefaultListableBeanFactory beanFactory, SmartProxyAutoConfiguration configurer,
			String targetBeanName, Class<?> targetClass) {
		this.beanFactory = notNullOf(beanFactory, "beanFactory");
		this.configurer = notNullOf(configurer, "configurer");
		this.targetBeanName = hasTextOf(targetBeanName, "targetBeanName");
		this.targetClass = notNullOf(targetClass, "targetClass");
		this.isSingleton = beanFactory.getBeanDefinition(targetBeanName).isSingleton();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (isExcludeMethod(proxy, getTarget(), method, args)) {
			return method.invoke(getTarget(), args);
		}
		return doInvoke(getTarget(), method, args);
	}

	protected boolean isExcludeMethod(Object proxy, Object target, Method method, Object[] args) {
		return excludeMethods.contains(method.getName());
	}

	protected abstract Object doInvoke(final Object target, final Method method, final Object[] args) throws Throwable;

	public static final List<String> excludeMethods = unmodifiableList(new ArrayList<String>(4) {
		private static final long serialVersionUID = 3369346948736795743L;
		{
			addAll(asList(Object.class.getDeclaredMethods()).stream().map(m -> m.getName()).collect(toList()));
			add("wait"); // Object#wait()
			add("notifyAll"); // Object#notifyAll()
			add("notify"); // Object#notify()
			add("finalize"); // Object#finalize()
			add("clone"); // Object#clone()
			add("hashCode"); // Object#hashCode()
			add("toString"); // Object#toString()
			add("equals"); // Object#equals()
			add("getClass"); // Object#getClass()
			add("isSingleton"); // org.springframework.beans.factory.FactoryBean#isSingleton()
			add("getObjectType"); // org.springframework.beans.factory.FactoryBean#getObjectType()
			// add("getObject");//org.springframework.beans.factory.FactoryBean#getObject()
			add("afterPropertiesSet"); // InitializingBean#afterPropertiesSet()
			add("destroy"); // DisposableBean#destroy()
			add("close"); // Closeable#close()
		}
	});
	public static final String FACTORYBEAN_GETOBJECT = "getObject";

}
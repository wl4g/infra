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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.annotation.Configuration;

import com.wl4g.component.core.framework.proxy.SmartProxyAutoConfiguration.SmartProxy;

import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isFinal;
import static org.springframework.util.ClassUtils.CGLIB_CLASS_SEPARATOR;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link DispatcherFactoryBeanSmartProxyInvocation}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author liqiu
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see
 */
public class DispatcherFactoryBeanSmartProxyInvocation extends AbstractDispatcherProxyInvocation {

	protected final Object targetFactoryBean; // typeof FactoryBean

	/**
	 * For example, when running in mybatis environment, the mapper instance is
	 * dynamically generated and annotated by {@link Configuration}
	 * {@link org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration}
	 * Executed later than {@link SmartProxyAutoConfiguration}, Therefore, it
	 * cannot be called directly when registering this class dynamically
	 * {@link BeanFactory#getBean()} to obtain the target instance, which can
	 * only be obtained indirectly when called.</br>
	 * 
	 * For specific reason analysis, refer to spring source code:
	 * {@link org.springframework.context.support.AbstractApplicationContext#refresh()}
	 * 
	 * @param beanFactory
	 * @param configurer
	 * @param targetFactoryBean
	 * @param targetBeanName
	 * @param targetClass
	 */
	public DispatcherFactoryBeanSmartProxyInvocation(DefaultListableBeanFactory beanFactory,
			SmartProxyAutoConfiguration configurer, Object targetFactoryBean, String targetBeanName, Class<?> targetClass) {
		super(beanFactory, configurer, targetBeanName, targetClass);
		this.targetFactoryBean = notNullOf(targetFactoryBean, "targetFactoryBean");
	}

	@Override
	public Object getTarget() {
		return targetFactoryBean;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object doInvoke(Object targetFactoryBean, Method method, Object[] args) throws Throwable {
		if (method.getName().equals(FACTORYBEAN_GETOBJECT)) {
			// TODO use cache
			Enhancer enhancer = new Enhancer() {
				@Override
				protected void setNamePrefix(String namePrefix) {
					super.setNamePrefix(
							targetClass.getName().concat(CGLIB_CLASS_SEPARATOR).concat(SmartProxy.class.getSimpleName()));
				}
			};
			enhancer.setCallback(new DispatcherSmartProxyInvocation(beanFactory, configurer, targetBeanName, targetClass, () -> {
				try {
					return ((FactoryBean) targetFactoryBean).getObject();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}));
			// Sets interfaces.
			List<Class<?>> interfaces = new ArrayList<>(4);
			interfaces.add(SmartProxy.class);
			if (targetClass.isInterface()) {
				interfaces.add(targetClass);
			} else {
				isTrue(!isFinal(targetClass.getModifiers()),
						() -> format("Unsupported enhance proxy, target class must a interface or not final type. - %s",
								targetClass));
				enhancer.setSuperclass(targetClass);
			}
			enhancer.setInterfaces(interfaces.toArray(new Class[0]));
			return enhancer.create();
		}

		// Other methods.
		return method.invoke(targetFactoryBean, args);
	}

}
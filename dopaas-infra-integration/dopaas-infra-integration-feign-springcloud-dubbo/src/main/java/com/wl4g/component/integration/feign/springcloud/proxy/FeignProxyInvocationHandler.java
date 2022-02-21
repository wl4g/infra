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

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import static org.springframework.aop.support.AopUtils.getTargetClass;

import com.wl4g.component.common.collection.CollectionUtils2;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.framework.proxy.ProxyInvocation;
import com.wl4g.component.integration.feign.springcloud.proxy.FeignProviderProxiesConfigurer;
import com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyController;
import com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyInvocationHandler;
import com.wl4g.component.integration.feign.springcloud.proxy.NoSuchFeignProxyBeanException;

import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyUtil.typeofDubboServiceBean;
import static com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyUtil.typeofMapperProxy;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

/**
 * {@link FeignProxyInvocationHandler}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author liqiu
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see
 */
public class FeignProxyInvocationHandler implements ProxyInvocation {
	private final SmartLogger log = getLogger(getClass());

	private final ApplicationContext applicationContext;
	private final Class<?> targetInterfaceType;

	private final AtomicBoolean init = new AtomicBoolean(false);
	private volatile Object target;

	/**
	 * For example, when running in mybatis environment, the mapper instance is
	 * dynamically generated and annotated by {@link Configuration}
	 * {@link org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration}
	 * Executed later than {@link FeignProviderProxiesConfigurer}, Therefore, it
	 * cannot be called directly when registering this class dynamically
	 * {@link BeanFactory#getBean()} to obtain the target instance, which can
	 * only be obtained indirectly when called.</br>
	 * 
	 * For specific reason analysis, refer to spring source code:
	 * {@link org.springframework.context.support.AbstractApplicationContext#refresh()}
	 * 
	 * @param applicationContext
	 * @param targetInterfaceType
	 */
	public FeignProxyInvocationHandler(ApplicationContext applicationContext, Class<?> targetInterfaceType) {
		this.applicationContext = notNullOf(applicationContext, "applicationContext");
		this.targetInterfaceType = notNullOf(targetInterfaceType, "targetInterfaceType");
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("toString")) {
			return getTargetClass(proxy).toString();
		}
		if (method.getName().equals("equals")) {
			return method.invoke(getTargetClass(proxy), args);
		}
		if (method.getName().equals("hashCode")) {
			return getTargetClass(proxy).hashCode();
		}

		// Invoke target method.
		final Object result = method.invoke(getTarget(), args);
		log.debug("Invoked feign proxy orig method: {}, args:{}, return: {}", method, args, result);
		return result;
	}

	/**
	 * Gets invocation origin target object.
	 * 
	 * @return
	 */
	@Override
	public Object getTarget() {
		if (isNull(target) && init.compareAndSet(false, true)) {
			synchronized (this) {
				if (isNull(target)) {
					// find the most original and the best from
					// multi beans.
					this.target = obtainBestCandidateTargetBean();
				}
			}
		}
		return target;
	}

	/**
	 * Obtain best candidates original target bean. </br>
	 * </br>
	 * <p>
	 * Filter excluded objects:</br>
	 * 1. Servicebean proxy instance of provider in Dubbo environment;</br>
	 * 2. The rest controller proxy instance of feign client in springcloud +
	 * Dubbo environment;</br>
	 * </br>
	 * Filter included objects:</br>
	 * 1. Mapper proxy instance in mybatis environment;</br>
	 * </p>
	 * 
	 * @return
	 */
	private final Object obtainBestCandidateTargetBean() {
		Collection<?> candidateBeans = safeMap(applicationContext.getBeansOfType(targetInterfaceType)).values();
		if (CollectionUtils2.isEmpty(candidateBeans)) {
			throw new NoSuchFeignProxyBeanException(targetInterfaceType);
		}

		// Filtering candidate beans
		List<Object> bestCandidates = candidateBeans.stream().filter(bestBeanFilter()).collect(toList());

		if (bestCandidates.size() == 1) {
			Object best = bestCandidates.get(0);
			log.info("Obtain best feign proxy target bean: {} of interface type: {}, candidates: {}", best, targetInterfaceType,
					candidateBeans);
			return best;
		} else {
			if (bestCandidates.size() > 1) {
				throw new NoSuchFeignProxyBeanException(
						format("Ambiguous feign proxy target multiple beans of interface type '%s', all candidate beans: {}",
								targetInterfaceType, candidateBeans),
						targetInterfaceType);
			}
			throw new NoSuchFeignProxyBeanException(
					format("No best target bean of interface type '%s' was obtain for the feign proxy. candidate beans: {}",
							targetInterfaceType, candidateBeans),
					targetInterfaceType);
		}

	}

	/**
	 * Best target bean filtering predicate.
	 * 
	 * @return
	 */
	private final Predicate<? super Object> bestBeanFilter() {
		return obj -> (nonNull(obj) && !typeofDubboServiceBean(obj) && !(obj instanceof FeignProxyController)
				|| typeofMapperProxy(obj));
	}

}
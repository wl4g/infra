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

import static com.wl4g.component.common.lang.ClassUtils2.getShortName;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.uncapitalize;

import org.springframework.beans.factory.config.BeanDefinition;

import com.wl4g.component.common.lang.ClassUtils2;
import com.wl4g.component.core.utils.AopUtils2;
import com.wl4g.component.integration.feign.springcloud.proxy.FeignProviderProxiesConfigurer;
import com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyController;
import com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyUtil;

/**
 * {@link FeignProxyUtil}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-23
 * @sine v1.0
 * @see
 */
public final class FeignProxyUtil {

	/**
	 * Check whether the feign provider proxed bean name.
	 * 
	 * @param beanDefinition
	 * @return
	 */
	public static boolean isFeignProxyBean(BeanDefinition beanDefinition) {
		return beanDefinition.getAttribute(FEIGNPROXY_INTERFACE_CLASS_ATTRIBUTE) != null;
	}

	/**
	 * Check whether the feign provider proxed bean name.
	 * 
	 * @param beanName
	 * @return
	 */
	public static boolean isFeignProxyBean(String beanName) {
		return valueOf(beanName).endsWith(FEIGNPROXY_BEAN_SUFFIX);
	}

	/**
	 * Generate feign client proxy bean name.
	 * 
	 * @param interfaceClassName
	 * @return
	 */
	public static String generateFeignProxyBeanName(String interfaceClassName) {
		return uncapitalize(getShortName(interfaceClassName)).concat(FEIGNPROXY_BEAN_SUFFIX);
	}

	/**
	 * Check whether the object type belongs to mybatis's mapper proxy.
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean typeofMapperProxy(Object obj) {
		return nonNull(mapperProxyClass) && mapperProxyClass.isInstance(AopUtils2.getTarget(obj));
	}

	/**
	 * Check whether the object type belongs to dubbo service bean proxy.
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean typeofDubboServiceBean(Object obj) {
		return nonNull(dubboServiceBeanClass) && dubboServiceBeanClass.isInstance(AopUtils2.getTarget(obj));
	}

	/**
	 * Mybatis mapper proxy class.
	 */
	public static final Class<?> mapperProxyClass = ClassUtils2.resolveClassNameNullable("org.apache.ibatis.binding.MapperProxy");

	/**
	 * Dubbo {@link com.alibaba.dubbo.config.spring.ServiceBean} class.
	 */
	public static final Class<?> dubboServiceBeanClass = ClassUtils2
			.resolveClassNameNullable("com.alibaba.dubbo.config.spring.ServiceBean");

	/**
	 * Feign proxy controller bean name suffix.
	 */
	public static final String FEIGNPROXY_BEAN_SUFFIX = "$".concat(FeignProxyController.class.getSimpleName());

	/**
	 * Feign proxy controller bean of origin interface class.
	 */
	public static final String FEIGNPROXY_INTERFACE_CLASS_ATTRIBUTE = "feignProxyOfInterfaceClass";

	/**
	 * {@link FeignProviderProxiesConfigurer} must be executed after
	 * {@link FeignClientToDubboProviderConfigurer}, because the
	 * {@link RestController} proxy class needs to be created first, On the
	 * contrary, because the latter creates a Dubbo {@link ServiceBean} instance
	 * of the service interface, it is not possible to confirm that the correct
	 * original object is obtained in the former.
	 */
	public static final int FEIGN_PROXY_ORDER = 9234852;
	public static final int FEIGN_DUBBO_ORDER = FEIGN_PROXY_ORDER + 1;

}

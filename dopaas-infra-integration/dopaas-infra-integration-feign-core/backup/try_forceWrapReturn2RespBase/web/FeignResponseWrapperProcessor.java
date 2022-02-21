/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.component.integration.springboot.feign.web;

import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.wl4g.component.common.web.rest.RespBase;
import com.wl4g.component.core.framework.proxy.SmartProxyProcessor;
import com.wl4g.component.integration.springboot.feign.annotation.SpringBootFeignClient;

/**
 * {@link FeignResponseWrapperProcessor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-28
 * @sine v1.0
 * @see
 */
public class FeignResponseWrapperProcessor implements SmartProxyProcessor {

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 200;
	}

	@Override
	public boolean supportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
		return hasAnnotation(actualOriginalTargetClass, SpringBootFeignClient.class);
	}

	/**
	 * Force standardizing and unifying the message structure, responsed by RPC
	 * api can solve some problems and have advantages, such as that some
	 * serialization frameworks can't directly deserialize collection types, and
	 * can uniformly use {@link RespBase} to return error codes when exceptions
	 * occur.
	 */
	@Override
	public Object postHandle(@NotNull Object bean, @NotNull Method method, Object[] parameters, Object result, Throwable ex) {
		return nonNull(result) ? new RespBase<>().withData(result) : null;
	}

//	@Configuration
	static class FeignResponseWrapperAutoConfiguration {
		@Bean
		public FeignResponseWrapperProcessor feignResponseWrapperProcessor() {
			return new FeignResponseWrapperProcessor();
		}
	}

}

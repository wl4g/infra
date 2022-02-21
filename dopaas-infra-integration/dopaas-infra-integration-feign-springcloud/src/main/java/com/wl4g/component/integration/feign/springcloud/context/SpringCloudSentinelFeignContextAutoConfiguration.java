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
package com.wl4g.component.integration.feign.springcloud.context;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.component.integration.feign.core.context.DefaultFeignContextAutoConfiguration;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/***
 * SpringCloud sentinel feign context holder auto configuration.
 *
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see
 */
@ConditionalOnClass({ SentinelAutoConfiguration.class, FeignConsumer.class, FeignClient.class })
@ConditionalOnWebApplication(type = Type.SERVLET)
@AutoConfigureBefore(DefaultFeignContextAutoConfiguration.class)
public class SpringCloudSentinelFeignContextAutoConfiguration implements WebMvcConfigurer {
	protected final SmartLogger log = getLogger(getClass());

	@Bean
	public SentinelFeignContextServletInterceptor sentinelFeignContextServletInterceptor() {
		return new SentinelFeignContextServletInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(sentinelFeignContextServletInterceptor()).addPathPatterns("/**");
	}

	/**
	 * Sentinel servlet request context handler interceptor.
	 */
	static class SentinelFeignContextServletInterceptor implements RequestInterceptor, HandlerInterceptor {

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
				throws Exception {
			// TODO Auto-generated method stub
		}

		@Override
		public void apply(RequestTemplate template) {
			// TODO Auto-generated method stub

		}

	}

}

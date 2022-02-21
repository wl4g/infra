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

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.cloud.netflix.hystrix.HystrixAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;
import com.wl4g.component.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.component.integration.feign.core.context.DefaultFeignContextAutoConfiguration;
import com.wl4g.component.integration.feign.core.context.RpcContextHolder;

/**
 * SpringCloud hystrix feign context holder auto configuration.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-17
 * @sine v1.0
 * @see https://blog.csdn.net/zhouyuhhu/article/details/103719825/
 */
@ConditionalOnClass({ HystrixAutoConfiguration.class, FeignConsumer.class, FeignClient.class })
@ConditionalOnWebApplication(type = Type.SERVLET)
@AutoConfigureBefore(DefaultFeignContextAutoConfiguration.class)
public class SpringCloudHystrixFeignContextAutoConfiguration {

	@Bean
	public RpcContextHolder springCloudHystrixFeignContextHolder() {
		return new SpringCloudHystrixFeignRpcContextHolder();
	}

	@Bean
	public HytrixFeignContextServletInterceptor hytrixFeignContextServletInterceptor() {
		return new HytrixFeignContextServletInterceptor();
	}

	@Bean
	public HytrixFeignContextWebMvcConfigurer hytrixFeignContextWebMvcConfigurer(
			HytrixFeignContextServletInterceptor interceptor) {
		return new HytrixFeignContextWebMvcConfigurer(interceptor);
	}

	static class HytrixFeignContextWebMvcConfigurer implements WebMvcConfigurer {
		private final HytrixFeignContextServletInterceptor interceptor;

		public HytrixFeignContextWebMvcConfigurer(HytrixFeignContextServletInterceptor interceptor) {
			this.interceptor = notNullOf(interceptor, "interceptor");
		}

		@Override
		public void addInterceptors(InterceptorRegistry registry) {
			registry.addInterceptor(interceptor).addPathPatterns("/**");
		}
	}

	/**
	 * Hytrix Servlet request context parameters interceptor. </br>
	 * Refer to <a href=
	 * "https://blog.csdn.net/luliuliu1234/article/details/96472893">HystrixInterceptor</a>
	 */
	static class HytrixFeignContextServletInterceptor implements HandlerInterceptor {
		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			// RpcContextHolder holder = RpcContextHolder.get();
			// isInstanceOf(HytrixFeignRpcContextHolder.class, holder);
			return true;
		}

		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
				throws Exception {
			RpcContextHolder.getContext().clearAttachments();
			RpcContextHolder.getServerContext().clearAttachments();
			((SpringCloudHystrixFeignRpcContextHolder) RpcContextHolder.getContext()).close();
			((SpringCloudHystrixFeignRpcContextHolder) RpcContextHolder.getServerContext()).close();
		}
	}

	static class SpringCloudHystrixFeignRpcContextHolder extends RpcContextHolder implements Closeable {
		private static final HystrixRequestVariableDefault<SpringCloudHystrixFeignRpcContextHolder> LOCAL = new HystrixRequestVariableDefault<>();
		private static final HystrixRequestVariableDefault<SpringCloudHystrixFeignRpcContextHolder> SERVER_LOCAL = new HystrixRequestVariableDefault<>();

		// Notes: Since feignclient ignores case when setting header, it should
		// be unified here.
		/** Feign request context attachments store. */
		private final Map<String, String> attachments = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		@Override
		public String getAttachment(String key) {
			return attachments.get(key);
		}

		@Override
		public Map<String, String> getAttachments() {
			return attachments;
		}

		@Override
		public void setAttachment(String key, String value) {
			attachments.put(key, value);
		}

		@Override
		public void removeAttachment(String key) {
			attachments.remove(key);
		}

		@Override
		public void clearAttachments() {
			attachments.clear();
		}

		@Override
		public void close() throws IOException {
			if (HystrixRequestContext.isCurrentThreadInitialized()) {
				// Destroy current thread
				HystrixRequestContext.getContextForCurrentThread().shutdown();
			}
		}

		@Override
		protected RpcContextHolder getContext0() {
			if (!HystrixRequestContext.isCurrentThreadInitialized()) {
				HystrixRequestContext.initializeContext();
			}
			SpringCloudHystrixFeignRpcContextHolder current = LOCAL.get();
			// Initializes hystrix request context in the current thread.
			if (isNull(current)) {
				LOCAL.set(current = new SpringCloudHystrixFeignRpcContextHolder());
			}
			return current;
		}

		@Override
		protected RpcContextHolder getServerContext0() {
			if (!HystrixRequestContext.isCurrentThreadInitialized()) {
				HystrixRequestContext.initializeContext();
			}
			SpringCloudHystrixFeignRpcContextHolder current = SERVER_LOCAL.get();
			// Initializes hystrix request context in the current thread.
			if (isNull(current)) {
				SERVER_LOCAL.set(current = new SpringCloudHystrixFeignRpcContextHolder());
			}
			return current;
		}

		@Override
		protected void removeContext0() {
			LOCAL.remove();
		}

		@Override
		protected void removeServerContext0() {
			SERVER_LOCAL.remove();
		}
	}

}

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
package com.wl4g.component.integration.feign.core.context.internal;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static java.util.Objects.isNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.wl4g.component.core.utils.context.SpringContextHolder;

import feign.RequestTemplate;
import feign.Response;

/**
 * {@link FeignContextCoprocessor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-04-27
 * @sine v1.0
 * @see
 */
public interface FeignContextCoprocessor extends Ordered {

	@Override
	default int getOrder() {
		return 0;
	}

	default void prepareConsumerExecution(@NotNull RequestTemplate template, @Nullable HttpServletRequest request) {
	}

	default void beforeConsumerExecution(@NotNull Object proxy, @NotNull Method method, @Nullable Object[] args) {
	}

	default void afterConsumerExecution(@NotNull Response response, Type type) {
	}

	default void beforeProviderExecution(@Nullable HttpServletRequest request, @NotNull Object target, @NotNull Method method,
			Object[] parameters) {
	}

	default void afterProviderExecution(@NotNull Object target, @NotNull Method method, Object[] args) {
	};

	static final class Invokers {
		private static final FeignContextCoprocessor[] DEFAULT = new FeignContextCoprocessor[0];
		private static volatile FeignContextCoprocessor[] coprocessors;

		public static void prepareConsumerExecution(@NotNull RequestTemplate template, @Nullable HttpServletRequest request) {
			for (FeignContextCoprocessor c : obtainCoprocessors()) {
				c.prepareConsumerExecution(template, request);
			}
		}

		public static void beforeConsumerExecution(@NotNull Object proxy, @NotNull Method method, @Nullable Object[] args) {
			for (FeignContextCoprocessor c : obtainCoprocessors()) {
				c.beforeConsumerExecution(proxy, method, args);
			}
		}

		public static void afterConsumerExecution(@NotNull Response response, Type type) {
			for (FeignContextCoprocessor c : obtainCoprocessors()) {
				c.afterConsumerExecution(response, type);
			}
		}

		public static void beforeProviderExecution(@Nullable HttpServletRequest request, @NotNull Object target,
				@NotNull Method method, Object[] parameters) {
			for (FeignContextCoprocessor c : obtainCoprocessors()) {
				c.beforeProviderExecution(request, target, method, parameters);
			}
		}

		public static void afterProviderExecution(@NotNull Object target, @NotNull Method method, Object[] args) {
			for (FeignContextCoprocessor c : obtainCoprocessors()) {
				c.afterProviderExecution(target, method, args);
			}
		}

		private static final FeignContextCoprocessor[] obtainCoprocessors() {
			if (isNull(coprocessors)) {
				synchronized (Invokers.class) {
					if (isNull(coprocessors)) {
						Map<String, FeignContextCoprocessor> beans = SpringContextHolder.getBeans(FeignContextCoprocessor.class);
						coprocessors = safeMap(beans).values().stream().toArray(FeignContextCoprocessor[]::new);
						AnnotationAwareOrderComparator.sort(coprocessors);
					}
				}
			}
			return (isEmptyArray(coprocessors)) ? DEFAULT : coprocessors;
		}

	}

}

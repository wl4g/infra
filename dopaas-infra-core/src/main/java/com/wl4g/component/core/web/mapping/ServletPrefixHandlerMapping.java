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
package com.wl4g.component.core.web.mapping;

import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * {@link ServletPrefixHandlerMapping}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-12
 * @sine v1.0.0
 * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ReactiveRestApiConfiguration}
 * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ServletRestApiConfirguation}
 * @see {@link org.springframework.web.servlet.DispatcherServlet#getHandler()}
 */
public class ServletPrefixHandlerMapping extends RequestMappingHandlerMapping {

	private final String mappingPrefix;
	private final Object handlers[];

	public ServletPrefixHandlerMapping(@Nullable String mappingPrefix, @NotNull Object... handlers) {
		// Default by empty
		this.mappingPrefix = isBlank(mappingPrefix) ? "" : mappingPrefix;
		this.handlers = handlers.clone();
		setOrder(-50);
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		for (Object handler : handlers) {
			detectHandlerMethods(handler);
		}
	}

	@Override
	protected void initApplicationContext() throws BeansException {
		setInterceptors(safeMap(getApplicationContext().getBeansOfType(HandlerInterceptor.class)).values().toArray());
		super.initApplicationContext();
	}

	@Override
	protected boolean isHandler(Class<?> beanType) {
		return false;
	}

	@Override
	protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
		if (mapping == null) {
			return;
		}
		super.registerHandlerMethod(handler, method, withPrefix(mapping));
	}

	private RequestMappingInfo withPrefix(RequestMappingInfo mapping) {
		List<String> newPatterns = withNewPatterns(mapping);

		PatternsRequestCondition patterns = new PatternsRequestCondition(newPatterns.toArray(new String[newPatterns.size()]));
		return new RequestMappingInfo(patterns, mapping.getMethodsCondition(), mapping.getParamsCondition(),
				mapping.getHeadersCondition(), mapping.getConsumesCondition(), mapping.getProducesCondition(),
				mapping.getCustomCondition());
	}

	private List<String> withNewPatterns(RequestMappingInfo mapping) {
		return mapping.getPatternsCondition().getPatterns().stream().map(pattern -> mappingPrefix.concat(pattern))
				.collect(toList());
	}

}

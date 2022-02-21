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

import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import com.wl4g.component.common.lang.ClassUtils2;

/**
 * Abstract prefix request handler mapping auto configuration support.
 * 
 * @author wangl.sir
 * @version v1.0 2019年1月10日
 * @since
 * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ReactiveRestApiConfiguration}
 * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ServletRestApiConfirguation}
 */
public abstract class PrefixHandlerMappingSupport implements ApplicationContextAware {

	/**
	 * {@link ApplicationContext}
	 */
	protected ApplicationContext actx;

	@Override
	public void setApplicationContext(ApplicationContext actx) throws BeansException {
		this.actx = notNullOf(actx, "applicationContext");
	}

	/**
	 * New create prefix handler mapping.
	 * 
	 * @param mappingPrefix
	 * @param annotationClass
	 * @return
	 */
	protected Object newPrefixHandlerMapping(@NotBlank String mappingPrefix,
			@NotNull Class<? extends Annotation> annotationClass) {
		hasTextOf(mappingPrefix, "mappingPrefix");
		notNullOf(annotationClass, "annotationClass");

		Map<String, Object> handlers = actx.getBeansWithAnnotation(annotationClass);
		return newPrefixHandlerMapping(mappingPrefix, handlers.values().toArray(new Object[handlers.size()]));
	}

	/**
	 * New create prefix handler mapping.
	 * 
	 * @param mappingPrefix
	 * @param handlers
	 * @return
	 */
	protected Object newPrefixHandlerMapping(@NotBlank String mappingPrefix, @NotNull Object... handlers) {
		hasTextOf(mappingPrefix, "mappingPrefix");
		notNullOf(handlers, "handlers");
		if (isReactiveWebApplication()) { // Reactive priority
			return new ReactivePrefixHandlerMapping(mappingPrefix, handlers);
		} else {
			return new ServletPrefixHandlerMapping(mappingPrefix, handlers);
		}
	}

	/**
	 * Check whether the current web application environment is reactive
	 * 
	 * @return
	 */
	public static boolean isReactiveWebApplication() {
		return ClassUtils2.isPresent(REACTIVE_WEB_APPLICATION_CLASS, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * {@link PathUtils}
	 * 
	 * @see
	 */
	public final static class PathUtils {

		private PathUtils() {
		}

		public static String normalizePath(String path) {
			if (!StringUtils.hasText(path)) {
				return path;
			}
			String normalizedPath = path;
			if (!normalizedPath.startsWith("/")) {
				normalizedPath = "/" + normalizedPath;
			}
			if (normalizedPath.endsWith("/")) {
				normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
			}
			return normalizedPath;
		}

	}

	/**
	 * Marked webflux reactive web application primary class.
	 * 
	 * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ReactiveRestApiConfiguration}
	 * @see {@link org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition}
	 */
	private static final String REACTIVE_WEB_APPLICATION_CLASS = "org.springframework.web.reactive.HandlerResult";

}
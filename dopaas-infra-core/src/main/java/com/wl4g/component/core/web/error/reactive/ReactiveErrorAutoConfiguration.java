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
package com.wl4g.component.core.web.error.reactive;

import static com.wl4g.component.core.constant.CoreConfigConstant.KEY_WEB_GLOBAL_ERROR;
import static java.util.stream.Collectors.toList;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import com.wl4g.component.core.web.error.AbstractErrorAutoConfiguration;

/**
 * Global error controller handler auto configuration.
 * 
 * @author wangl.sir
 * @version v1.0 2019年1月10日
 * @since
 */
@ConditionalOnProperty(value = KEY_WEB_GLOBAL_ERROR + ".enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(ViewResolver.class)
public class ReactiveErrorAutoConfiguration extends AbstractErrorAutoConfiguration {

	/**
	 * @see {@link org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration#errorWebExceptionHandler}
	 */
	@Bean
	@Order(-2) // Takes precedence over the default handler
	public ReactiveSmartErrorHandler reactiveSmartErrorHandler(
			org.springframework.boot.web.reactive.error.ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
			ObjectProvider<ViewResolver> viewResolvers, ServerCodecConfigurer codecConfigurer, ApplicationContext actx) {
		ReactiveSmartErrorHandler errorHandler = new ReactiveSmartErrorHandler(errorAttributes, resourceProperties, actx);
		errorHandler.setViewResolvers(viewResolvers.orderedStream().collect(toList()));
		errorHandler.setMessageWriters(codecConfigurer.getWriters());
		errorHandler.setMessageReaders(codecConfigurer.getReaders());
		return errorHandler;
	}

}
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
package com.wl4g.component.core.web.error.servlet;

import static com.wl4g.component.core.constant.CoreConfigConstant.KEY_WEB_GLOBAL_ERROR;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;

import com.wl4g.component.core.web.error.AbstractErrorAutoConfiguration;
import com.wl4g.component.core.web.error.CompositeErrorConfigurer;

/**
 * Global error controller handler auto configuration.
 * 
 * @author wangl.sir
 * @version v1.0 2019年1月10日
 * @since
 */
@ConditionalOnProperty(value = KEY_WEB_GLOBAL_ERROR + ".enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServletErrorAutoConfiguration extends AbstractErrorAutoConfiguration {

	/**
	 * {@link ServletErrorHandlerAutoConfirguation}
	 * 
	 * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ServletRestApiConfirguation}
	 */
	@Bean
	public ServletSmartErrorHandler servletSmartErrorHandler(ErrorHandlerProperties config, ErrorAttributes errorAttrs,
			CompositeErrorConfigurer adapter) {
		return new ServletSmartErrorHandler(config, errorAttrs, adapter);
	}

}
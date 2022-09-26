/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.core.web.error.servlet;

import static com.wl4g.infra.core.constant.CoreInfraConstants.CONF_PREFIX_INFRA_CORE_WEB_GLOBAL_ERROR;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;

import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration;
import com.wl4g.infra.core.web.error.handler.CompositeSmartErrorHandler;

/**
 * Global error controller handler auto configuration.
 * 
 * @author wangl.sir
 * @version v1.0 2019年1月10日
 * @since
 */
@ConditionalOnProperty(value = CONF_PREFIX_INFRA_CORE_WEB_GLOBAL_ERROR + ".enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServletErrorAutoConfiguration extends AbstractErrorAutoConfiguration {

    /**
     * {@link ServletErrorHandlerAutoConfirguation}
     * 
     * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ServletRestApiConfirguation}
     */
    @Bean
    public ServletSmartErrorController servletSmartErrorController(
            ErrorHandlerProperties config,
            ErrorAttributes errorAttributes,
            CompositeSmartErrorHandler errorHandler) {
        return new ServletSmartErrorController(config, errorAttributes, errorHandler);
    }

}
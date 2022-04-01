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
package com.wl4g.infra.core.web.error.servlet;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.web.WebUtils2.write;
import static com.wl4g.infra.common.web.WebUtils2.writeJson;
import static com.wl4g.infra.core.constant.CoreInfraConstants.CONF_PREFIX_INFRA_CORE_WEB_GLOBAL_ERROR;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration;
import com.wl4g.infra.core.web.error.handler.AbstractSmartErrorHandler;
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

    @Bean
    @ConditionalOnMissingBean
    public AbstractSmartErrorHandler.ErrorRender defaultServletSmartErrorRender(ErrorHandlerProperties config) {
        return new AbstractSmartErrorHandler.ErrorRender() {
            @Override
            public void renderingJson(Map<String, Object> model, RespBase<Object> resp) throws Exception {
                writeJson((HttpServletResponse) getHttpResponse(), resp.asJson());
            }

            @Override
            public void renderingTemplate(Map<String, Object> model, int status, String templateString) throws Exception {
                write((HttpServletResponse) getHttpResponse(), status, TEXT_HTML_VALUE, templateString.getBytes(UTF_8));
            }

            @Override
            public void redirectLocation(Map<String, Object> model, String errorRedirectUri) throws Exception {
                ((HttpServletResponse) getHttpResponse()).sendRedirect(errorRedirectUri);
            }
        };
    }

    /**
     * {@link ServletErrorHandlerAutoConfirguation}
     * 
     * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ServletRestApiConfirguation}
     */
    @Bean
    public ServletSmartErrorController servletSmartErrorController(
            ErrorHandlerProperties config,
            ErrorAttributes errorAttributes,
            CompositeSmartErrorHandler errorHandler,
            AbstractSmartErrorHandler.ErrorRender errorRender) {
        return new ServletSmartErrorController(config, errorAttributes, errorHandler, errorRender);
    }

}
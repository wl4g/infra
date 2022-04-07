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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.web.WebUtils2.isStacktraceRequest;
import static com.wl4g.infra.core.web.error.handler.AbstractSmartErrorHandler.obtainErrorAttributeOptions;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration.ErrorController;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration.ErrorHandlerProperties;
import com.wl4g.infra.core.web.error.handler.AbstractSmartErrorHandler;
import com.wl4g.infra.core.web.error.handler.AbstractSmartErrorHandler.ErrorRender;
import com.wl4g.infra.core.web.error.handler.CompositeSmartErrorHandler;

/**
 * Servlet smart global error controller.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年1月10日
 * @since
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ErrorController
@ControllerAdvice
@ConditionalOnBean(ServletErrorAutoConfiguration.class)
public class ServletSmartErrorController extends AbstractErrorController {

    protected final SmartLogger log = getLogger(getClass());
    protected final ErrorHandlerProperties config;
    protected final CompositeSmartErrorHandler errorHandler;
    protected final AbstractSmartErrorHandler.ErrorRender errorRender;

    public ServletSmartErrorController(ErrorHandlerProperties config, ErrorAttributes errorAttributes,
            CompositeSmartErrorHandler errorHandler, AbstractSmartErrorHandler.ErrorRender errorRender) {
        super(errorAttributes);
        this.config = notNullOf(config, "config");
        this.errorHandler = notNullOf(errorHandler, "errorHandler");
        this.errorRender = notNullOf(errorRender, "errorRender");
    }

    /**
     * Returns the path of the error page.
     *
     * @return the error path
     */
    @Override
    public String getErrorPath() {
        return DEFAULT_ERROR_PATH;
    }

    /**
     * DO any servlet request handler errors.
     * 
     * @param request
     * @param response
     * @param th
     * @return
     */
    @RequestMapping(DEFAULT_ERROR_PATH)
    @ExceptionHandler({ Throwable.class })
    public void doAnyHandleError(final HttpServletRequest request, final HttpServletResponse response, final Throwable th) {
        // Obtain errors attributes.
        Map<String, Object> model = getErrorAttributes(request, th);

        // handle errors
        errorHandler.rendering(new WebRequestExtractor() {
            @Override
            public String getQueryParam(String name) {
                return request.getParameter(name);
            }

            @Override
            public String getHeader(String name) {
                return request.getHeader(name);
            }
        }, model, th, new ErrorRender() {
            @Override
            public Object renderingJson(Map<String, Object> model, RespBase<Object> resp) throws Exception {
                return errorRender.renderingJson(model, resp);
            }

            @Override
            public Object renderingTemplate(Map<String, Object> model, int status, String templateString) throws Exception {
                return errorRender.renderingTemplate(model, status, templateString);
            }

            @Override
            public Object redirectLocation(Map<String, Object> model, String errorRedirectUri) throws Exception {
                return errorRender.redirectLocation(model, errorRedirectUri);
            }

            @Override
            public Object getHttpResponse() {
                return response;
            }
        });

    }

    /**
     * Extract error details model
     * 
     * @param request
     * @param th
     * @return
     */
    private Map<String, Object> getErrorAttributes(HttpServletRequest request, Throwable th) {
        boolean _stacktrace = isStackTrace(request);
        Map<String, Object> model = super.getErrorAttributes(request, obtainErrorAttributeOptions(_stacktrace));
        if (_stacktrace) {
            log.error("Origin Errors - {}", model);
        }

        // Correct replacement using meaningful status codes.
        model.put("status", errorHandler.getStatus(model, th));
        // Correct replacement with meaningful status messages.
        model.put("message", errorHandler.getRootCause(model, th));
        return model;
    }

    /**
     * Whether error stack information is enabled
     * 
     * @param request
     * @return
     */
    private boolean isStackTrace(ServletRequest request) {
        if (log.isDebugEnabled()) {
            return true;
        }
        return isStacktraceRequest(request);
    }

    private static final String DEFAULT_ERROR_PATH = "/error";

}
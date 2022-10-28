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
package com.wl4g.infra.core.web.error.reactive;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.ClassUtils2.resolveClassName;
import static com.wl4g.infra.common.lang.StringUtils2.isTrue;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static com.wl4g.infra.common.runtime.JvmRuntimeTool.isJvmInDebugging;
import static com.wl4g.infra.common.web.WebUtils.PARAM_STACKTRACE;
import static com.wl4g.infra.core.constant.CoreInfraConstants.TRACE_REQUEST_ID_HEADER;
import static com.wl4g.infra.core.web.error.handler.AbstractSmartErrorHandler.obtainErrorAttributeOptions;
import static java.util.Locale.US;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Field;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpCookie;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest.Headers;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration.ErrorController;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration.ErrorHandlerProperties;
import com.wl4g.infra.core.web.error.handler.AbstractSmartErrorHandler;
import com.wl4g.infra.core.web.error.handler.CompositeSmartErrorHandler;

import reactor.core.publisher.Mono;

/**
 * Reactive smart global web error handler.
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020-09-12
 * @sine v1.0.0
 * @see https://blog.csdn.net/keets1992/article/details/85077874
 */
@ErrorController
@ControllerAdvice
@ConditionalOnBean(ReactiveErrorAutoConfiguration.class)
public class ReactiveSmartErrorController extends AbstractErrorWebExceptionHandler implements InitializingBean {

    protected final SmartLogger log = getLogger(getClass());
    protected final ErrorHandlerProperties config;
    protected final CompositeSmartErrorHandler errorHandler;
    protected final AbstractSmartErrorHandler.ErrorRender errorRender;

    public ReactiveSmartErrorController(ErrorAttributes errorAttributes, Resources resources, ApplicationContext actx,
            ErrorHandlerProperties config, CompositeSmartErrorHandler errorHandler,
            AbstractSmartErrorHandler.ErrorRender errorRender) {
        super(errorAttributes, resources, actx);
        this.config = notNullOf(config, "config");
        this.errorHandler = notNullOf(errorHandler, "errorHandler");
        this.errorRender = notNullOf(errorRender, "errorRender");
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::handleRendering);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        boolean _stacktrace = isStackTrace(request);
        Map<String, Object> model = super.getErrorAttributes(request, obtainErrorAttributeOptions(_stacktrace));
        if (_stacktrace) {
            log.error("Origin Errors - {}", model);
        }

        // Correct replacement using meaningful status codes.
        model.put("status", errorHandler.getStatus(model, getError(request)));
        // Correct replacement with meaningful status messages.
        model.put("message", errorHandler.getRootCause(model, getError(request)));
        return model;
    }

    /**
     * Rendering error response handle.
     * 
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    private Mono<ServerResponse> handleRendering(final ServerRequest request) {
        Map<String, Object> model = getErrorAttributes(request, false);

        return (Mono<ServerResponse>) errorHandler.rendering(new WebRequestExtractor() {
            @Override
            public String getRequestId() {
                if (request instanceof org.springframework.http.server.ServerHttpRequest) {
                    return ((org.springframework.http.server.ServerHttpRequest) (request)).getHeaders()
                            .getFirst(TRACE_REQUEST_ID_HEADER);
                } else if (request instanceof org.springframework.http.server.reactive.ServerHttpRequest) {
                    return ((org.springframework.http.server.reactive.ServerHttpRequest) (request)).getHeaders()
                            .getFirst(TRACE_REQUEST_ID_HEADER);
                } else if (request instanceof org.springframework.web.reactive.function.server.ServerRequest) {
                    org.springframework.web.reactive.function.server.ServerRequest.Headers headers = getField(
                            REACTIVE_SERVER_REQUEST_HEADER_FIELD, request, true);
                    return headers.firstHeader(TRACE_REQUEST_ID_HEADER);
                }
                return null;
            }

            @Override
            public String getQueryValue(String name) {
                return request.queryParam(name).orElse(null);
            }

            @Override
            public String getHeaderValue(String name) {
                return request.headers().asHttpHeaders().getFirst(name);
            }
        }, model, getError(request), errorRender);
    }

    /**
     * Whether error stack information is enabled
     * 
     * @param request
     * @return
     */
    private boolean isStackTrace(ServerRequest request) {
        if (log.isDebugEnabled() || isJvmInDebugging) {
            return true;
        }
        String stacktrace = request.queryParam(PARAM_STACKTRACE).orElse(null);
        if (isBlank(stacktrace)) {
            Headers headers = request.headers();
            if (nonNull(headers)) {
                stacktrace = headers.firstHeader(PARAM_STACKTRACE);
            }
        }
        if (isBlank(stacktrace)) {
            MultiValueMap<String, HttpCookie> cookies = request.cookies();
            if (nonNull(cookies)) {
                HttpCookie c = cookies.getFirst(PARAM_STACKTRACE);
                if (nonNull(c)) {
                    stacktrace = c.getValue();
                }
            }
        }
        if (isBlank(stacktrace)) {
            return false;
        }
        return isTrue(stacktrace.toLowerCase(US), false);
    }

    public static final Class<?> REACTIVE_DEFAULT_SERVER_REQUEST_CLASS = resolveClassName(
            "org.springframework.web.reactive.function.server.DefaultServerRequest", null);
    public static final Field REACTIVE_SERVER_REQUEST_HEADER_FIELD = findField(REACTIVE_DEFAULT_SERVER_REQUEST_CLASS, "headers",
            org.springframework.web.reactive.function.server.ServerRequest.Headers.class);
}
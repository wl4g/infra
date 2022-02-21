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

import static com.wl4g.component.common.lang.StringUtils2.isTrue;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.web.WebUtils2.PARAM_STACKTRACE;
import static com.wl4g.component.core.web.error.ErrorConfigurer.obtainErrorAttributeOptions;
import static java.util.Locale.US;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.TEMPORARY_REDIRECT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.wl4g.component.common.jvm.JvmRuntimeKit;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.web.WebUtils2.RequestExtractor;
import com.wl4g.component.common.web.rest.RespBase;
import com.wl4g.component.core.web.error.AbstractErrorAutoConfiguration.ErrorController;
import com.wl4g.component.core.web.error.AbstractErrorAutoConfiguration.ErrorHandlerProperties;
import com.wl4g.component.core.web.error.CompositeErrorConfigurer;
import com.wl4g.component.core.web.error.ErrorConfigurer;
import com.wl4g.component.core.web.error.ErrorConfigurer.RenderingErrorHandler;

import reactor.core.publisher.Mono;

/**
 * Reactive smart global web error handler.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-12
 * @sine v1.0.0
 * @see https://blog.csdn.net/keets1992/article/details/85077874
 */
@ErrorController
@ControllerAdvice
@ConditionalOnBean(ReactiveErrorAutoConfiguration.class)
public class ReactiveSmartErrorHandler extends AbstractErrorWebExceptionHandler implements InitializingBean {

	protected final SmartLogger log = getLogger(getClass());

	/** {@link ErrorHandlerProperties} */
	@Autowired
	protected ErrorHandlerProperties config;

	/** {@link ErrorConfigurer} */
	@Autowired
	protected CompositeErrorConfigurer configurer;

	public ReactiveSmartErrorHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
			ApplicationContext actx) {
		super(errorAttributes, resourceProperties, actx);
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
	}

	@Override
	protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
		boolean _stacktrace = isStackTrace(request);
		Map<String, Object> model = super.getErrorAttributes(request, obtainErrorAttributeOptions(_stacktrace));
		if (_stacktrace) {
			log.error("Origin Errors - {}", model);
		}

		// Correct replacement using meaningful status codes.
		model.put("status", configurer.getStatus(model, getError(request)));
		// Correct replacement with meaningful status messages.
		model.put("message", configurer.getRootCause(model, getError(request)));
		return model;
	}

	/**
	 * Rendering error response.
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Mono<ServerResponse> renderErrorResponse(final ServerRequest request) {
		Map<String, Object> model = getErrorAttributes(request, false);

		return (Mono<ServerResponse>) configurer.handleErrorRendering(new RequestExtractor() {
			@Override
			public String getQueryParam(String name) {
				return request.queryParam(name).orElse(null);
			}

			@Override
			public String getHeader(String name) {
				return request.headers().asHttpHeaders().getFirst(name);
			}
		}, model, getError(request), new RenderingErrorHandler() {

			@Override
			public Object renderingWithJson(Map<String, Object> model, RespBase<Object> resp) throws Exception {
				return ServerResponse.ok().contentType(APPLICATION_JSON).body(fromValue(resp));
			}

			@Override
			public Object renderingWithView(Map<String, Object> model, int status, String renderString) throws Exception {
				return ServerResponse.status(status).contentType(TEXT_HTML).body(fromValue(renderString));
			}

			@Override
			public Object redirectError(Map<String, Object> model, String errorRedirectURI) throws Exception {
				return ServerResponse.status(TEMPORARY_REDIRECT.value()).contentType(TEXT_HTML)
						.location(URI.create(errorRedirectURI)).build();
			}
		});

	}

	/**
	 * Whether error stack information is enabled
	 * 
	 * @param request
	 * @return
	 */
	private boolean isStackTrace(ServerRequest request) {
		if (log.isDebugEnabled() || JvmRuntimeKit.isJVMDebugging) {
			return true;
		}
		String stacktrace = request.queryParam(PARAM_STACKTRACE).orElse(null);
		if (isBlank(stacktrace)) {
			stacktrace = request.headers().firstHeader(PARAM_STACKTRACE);
		}
		if (isBlank(stacktrace)) {
			stacktrace = request.cookies().getFirst(PARAM_STACKTRACE).getValue();
		}
		if (isBlank(stacktrace)) {
			return false;
		}
		return isTrue(stacktrace.toLowerCase(US), false);
	}

}
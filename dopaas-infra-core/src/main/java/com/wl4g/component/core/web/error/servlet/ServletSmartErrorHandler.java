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

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.web.WebUtils2.isStacktraceRequest;
import static com.wl4g.component.common.web.WebUtils2.write;
import static com.wl4g.component.common.web.WebUtils2.writeJson;
import static com.wl4g.component.core.web.error.ErrorConfigurer.obtainErrorAttributeOptions;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

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

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.web.WebUtils2.RequestExtractor;
import com.wl4g.component.common.web.rest.RespBase;
import com.wl4g.component.core.web.error.AbstractErrorAutoConfiguration.ErrorController;
import com.wl4g.component.core.web.error.AbstractErrorAutoConfiguration.ErrorHandlerProperties;
import com.wl4g.component.core.web.error.CompositeErrorConfigurer;
import com.wl4g.component.core.web.error.ErrorConfigurer;
import com.wl4g.component.core.web.error.ErrorConfigurer.RenderingErrorHandler;

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
public class ServletSmartErrorHandler extends AbstractErrorController {
	protected final SmartLogger log = getLogger(getClass());

	/** {@link ErrorHandlerProperties} */
	protected final ErrorHandlerProperties config;

	/** {@link ErrorConfigurer} */
	protected final CompositeErrorConfigurer configurer;

	public ServletSmartErrorHandler(ErrorHandlerProperties config, ErrorAttributes errorAttributes,
			CompositeErrorConfigurer configurer) {
		super(errorAttributes);
		this.config = notNullOf(config, "config");
		this.configurer = notNullOf(configurer, "configurer");
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
	@ExceptionHandler({ Exception.class })
	public void doAnyHandleError(final HttpServletRequest request, final HttpServletResponse response, final Exception th) {
		// Obtain errors attributes.
		Map<String, Object> model = getErrorAttributes(request, th);

		// handle errors
		configurer.handleErrorRendering(new RequestExtractor() {
			@Override
			public String getQueryParam(String name) {
				return request.getParameter(name);
			}

			@Override
			public String getHeader(String name) {
				return request.getHeader(name);
			}
		}, model, th, new RenderingErrorHandler() {
			@Override
			public Object renderingWithJson(Map<String, Object> model, RespBase<Object> resp) throws Exception {
				writeJson(response, resp.asJson());
				return null;
			}

			@Override
			public Object renderingWithView(Map<String, Object> model, int status, String renderString) throws Exception {
				write(response, status, TEXT_HTML_VALUE, renderString.getBytes(UTF_8));
				return null;
			}

			@Override
			public Object redirectError(Map<String, Object> model, String errorRedirectURI) throws Exception {
				response.sendRedirect(errorRedirectURI);
				return null;
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
		model.put("status", configurer.getStatus(model, th));
		// Correct replacement with meaningful status messages.
		model.put("message", configurer.getRootCause(model, th));
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
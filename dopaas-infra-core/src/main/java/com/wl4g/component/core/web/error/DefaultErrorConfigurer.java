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
package com.wl4g.component.core.web.error;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.web.rest.RespBase.*;
import static com.wl4g.component.common.web.rest.RespBase.RetCode.*;
import static java.util.Objects.isNull;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.web.error.AbstractErrorAutoConfiguration.ErrorHandlerProperties;

/**
 * Default basic error configure.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年11月1日
 * @since
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefaultErrorConfigurer extends ErrorConfigurer {

	public DefaultErrorConfigurer(ErrorHandlerProperties config) {
		super(config);
	}

	final protected SmartLogger log = getLogger(getClass());

	@Override
	public Integer getStatus(Map<String, Object> model, Throwable th) {
		Integer statusCode = (Integer) model.get("status");
		/**
		 * Eliminate meaningless status code: 999
		 * 
		 * @see {@link org.springframework.boot.autoconfigure.web.DefaultErrorAttributes#addStatus()}
		 */
		if (isNull(statusCode) || statusCode == 999) {
			RetCode retCode = getRestfulCode(th);
			if (!isNull(retCode)) {
				statusCode = retCode.getErrcode();
			} else if (th instanceof IllegalArgumentException) {
				return BAD_PARAMS.getErrcode();
			} else if (th instanceof UnsupportedOperationException) {
				return UNSUPPORTED.getErrcode();
			} else { // status=999?
				// statusCode = (Integer)
				// equest.getAttribute("javax.servlet.error.status_code");
			}
		}
		if (!isNull(statusCode)) {
			return statusCode;
		}
		return null;
	}

	@Override
	public String getRootCause(Map<String, Object> model, Throwable th) {
		return extractValidErrorsMessage(model);
	}

}
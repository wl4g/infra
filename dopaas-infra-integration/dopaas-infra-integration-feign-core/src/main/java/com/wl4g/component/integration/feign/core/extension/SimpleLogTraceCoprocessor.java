/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.component.integration.feign.core.extension;

import static com.wl4g.component.common.web.WebUtils2.PARAM_STACKTRACE;
import static com.wl4g.component.common.web.WebUtils2.isStacktraceRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.wl4g.component.integration.feign.core.context.RpcContextHolder;
import com.wl4g.component.integration.feign.core.context.internal.FeignContextCoprocessor;

import feign.RequestTemplate;

/**
 * {@link SimpleLogTraceCoprocessor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-04-27
 * @sine v1.0
 * @see
 */
public class SimpleLogTraceCoprocessor implements FeignContextCoprocessor {

	@Override
	public void prepareConsumerExecution(@NotNull RequestTemplate template, HttpServletRequest request) {
		// Pass 'stacktrace' parameter through to the next service.
		if (isStacktraceRequest(request)) {
			template.header(PARAM_STACKTRACE, RpcContextHolder.getContext().getAttachment(PARAM_STACKTRACE));
		}
	}

}

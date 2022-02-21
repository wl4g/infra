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
package com.wl4g.component.integration.feign.core.context.internal;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmpty;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.StringUtils2.startsWithIgnoreCase;
import static com.wl4g.component.common.web.WebUtils2.getRequestHeaders;
import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.wl4g.component.integration.feign.core.context.RpcContextHolder;

import feign.RequestTemplate;

/**
 * {@link FeignRpcContextBinders}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-04
 * @sine v1.0
 * @see
 */
public final class FeignRpcContextBinders {

	// --- Feign Client(Consumer). ---

	public static void writeAttachmentsToFeignRequest(@NotNull RequestTemplate template) {
		notNullOf(template, "template");
		safeMap(RpcContextHolder.getContext().getAttachments())
				.forEach((name, value) -> template.header(ATTACHMENT_HEADER_PREFIX.concat(name), value));
	}

	public static void bindAttachmentsFromFeignResposne(@NotNull feign.Response response) {
		notNullOf(response, "response");
		// Bind feign response attachments to current rpcContext.
		if (!isEmpty(response.headers())) {
			response.headers().forEach((name, values) -> {
				if (startsWithIgnoreCase(name, ATTACHMENT_HEADER_PREFIX)) {
					if (nonNull(values) && !values.isEmpty()) {
						String firstValue = values.iterator().next();
						RpcContextHolder.getServerContext().getAttachments()
								.put(name.substring(ATTACHMENT_HEADER_PREFIX_LEN, name.length()), firstValue);
					}
				}
			});
		}
	}

	// --- Feign Server(Provider). ---

	public static void bindAttachmentsFromRequest(@NotNull HttpServletRequest request) {
		notNullOf(request, "request");

		// Find request attachments by headers.
		Map<String, String> requestAttachments = getRequestHeaders(request,
				name -> startsWithIgnoreCase(name, ATTACHMENT_HEADER_PREFIX));

		// Clear FIXED header prefix.
		Map<String, String> attachments = new HashMap<>();
		requestAttachments.forEach((name, value) -> {
			if (startsWithIgnoreCase(name, ATTACHMENT_HEADER_PREFIX)) {
				attachments.put(name.substring(ATTACHMENT_HEADER_PREFIX_LEN, name.length()), value);
			} else {
				attachments.put(name, value);
			}
		});

		RpcContextHolder.getContext().setAttachments(attachments);
	}

	public static void writeAttachemntsToResponse(@NotNull HttpServletResponse response) {
		notNullOf(response, "response");
		safeMap(RpcContextHolder.getServerContext().getAttachments())
				.forEach((name, value) -> response.setHeader(ATTACHMENT_HEADER_PREFIX.concat(name), value));
	}

	public static final String ATTACHMENT_HEADER_PREFIX = "X-Rpc-Attach-";
	public static final int ATTACHMENT_HEADER_PREFIX_LEN = ATTACHMENT_HEADER_PREFIX.length();

}

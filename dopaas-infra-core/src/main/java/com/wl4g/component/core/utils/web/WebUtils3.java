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
package com.wl4g.component.core.utils.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Objects.nonNull;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.wl4g.component.common.web.WebUtils2;

/**
 * {@link WebUtils3}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年5月21日
 * @since
 */
public abstract class WebUtils3 extends WebUtils2 {

	/**
	 * Gets request parameter.
	 * 
	 * @param name
	 * @return
	 */
	public static String getRequestParameter(String name) {
		HttpServletRequest request = currentServletRequest();
		return nonNull(request) ? request.getParameter(name) : null;
	}

	public static HttpServletRequest currentServletRequest() {
		ServletRequestAttributes attr = (ServletRequestAttributes) getRequestAttributes();
		return nonNull(attr) ? attr.getRequest() : null;
	}

	public static HttpServletResponse currentServletResponse() {
		ServletRequestAttributes attr = (ServletRequestAttributes) getRequestAttributes();
		return nonNull(attr) ? attr.getResponse() : null;
	}

}
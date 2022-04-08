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
package com.wl4g.infra.core.web.matcher;

import javax.servlet.http.HttpServletRequest;

import com.wl4g.infra.common.web.CookieUtils;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.RequestExtractor;

import lombok.AllArgsConstructor;

/**
 * {@link ServletRequsetExtractor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-07 v3.0.0
 * @since v3.0.0
 */
@AllArgsConstructor
public class ServletRequsetExtractor implements RequestExtractor {

    private final HttpServletRequest request;

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getPath() {
        return request.getRequestURI();
    }

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public String getHost() {
        return request.getServerName();
    }

    @Override
    public Integer getPort() {
        return request.getServerPort();
    }

    @Override
    public String getHeaderValue(String name) {
        return request.getHeader(name);
    }

    @Override
    public String getCookieValue(String name) {
        return CookieUtils.getCookie(request, name);
    }

    @Override
    public String getQueryValue(String name) {
        return request.getParameter(name);
    }

}

/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.context.utils.web;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.infra.context.constant.ContextInfraConstants.TRACE_REQUEST_ID_HEADER;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.security.Principal;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.EnumerationUtils;

import com.wl4g.infra.common.web.CookieUtils;
import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;

import lombok.AllArgsConstructor;

/**
 * {@link ServletRequsetExtractor}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-04-07 v3.0.0
 * @since v3.0.0
 */
@AllArgsConstructor
public class ServletRequsetExtractor implements WebRequestExtractor {

    private final HttpServletRequest request;

    @Override
    public String getRequestId() {
        return request.getHeader(TRACE_REQUEST_ID_HEADER);
    }

    @Override
    public URI getRequestURI() {
        return URI.create(request.getRequestURI());
    }

    @Override
    public Principal getPrincipal() {
        return request.getUserPrincipal();
    }

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
    public Collection<String> getQueryNames() {
        return request.getParameterMap().keySet();
    }

    @Override
    public String getQueryValue(String name) {
        return request.getParameter(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getHeaderNames() {
        return EnumerationUtils.toList(request.getHeaderNames());
    }

    @Override
    public String getHeaderValue(String name) {
        return request.getHeader(name);
    }

    @Override
    public Collection<String> getCookieNames() {
        return safeArrayToList(request.getCookies()).stream().map(c -> c.getName()).collect(toList());
    }

    @Override
    public String getCookieValue(String name) {
        return CookieUtils.getCookie(request, name);
    }

}

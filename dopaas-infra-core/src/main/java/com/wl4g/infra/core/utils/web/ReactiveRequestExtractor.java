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
package com.wl4g.infra.core.utils.web;

import static com.wl4g.infra.common.lang.ClassUtils2.resolveClassName;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.core.constant.CoreInfraConstants.TRACE_REQUEST_ID_HEADER;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Collection;

import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;

import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;

import lombok.AllArgsConstructor;

/**
 * {@link ReactiveRequestExtractor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-07 v3.0.0
 * @since v3.0.0
 */
@AllArgsConstructor
public class ReactiveRequestExtractor implements WebRequestExtractor {

    private final ServerHttpRequest request;

    @Override
    public String getRequestId() {
        return request.getHeaders().getFirst(TRACE_REQUEST_ID_HEADER);
    }

    @Override
    public URI getRequestURI() {
        return request.getURI();
    }

    @Override
    public String getMethod() {
        return request.getMethod().name();
    }

    @Override
    public String getScheme() {
        return request.getURI().getScheme();
    }

    @Override
    public String getHost() {
        return request.getURI().getHost();
    }

    @Override
    public Integer getPort() {
        return request.getURI().getPort();
    }

    @Override
    public String getPath() {
        return request.getURI().getPath();
    }

    @Override
    public Collection<String> getQueryNames() {
        return request.getQueryParams().keySet();
    }

    @Override
    public String getQueryValue(String name) {
        return request.getQueryParams().getFirst(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return request.getHeaders().keySet();
    }

    @Override
    public String getHeaderValue(String name) {
        return request.getHeaders().getFirst(name);
    }

    @Override
    public Collection<String> getCookieNames() {
        return request.getCookies().keySet();
    }

    @Override
    public String getCookieValue(String name) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (isEmpty(cookies)) {
            HttpCookie cookie = cookies.getFirst(name);
            if (nonNull(cookie)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static final Class<?> REACTIVE_DEFAULT_SERVER_REQUEST_CLASS = resolveClassName(
            "org.springframework.web.reactive.function.server.DefaultServerRequest", null);
    public static final Field REACTIVE_SERVER_REQUEST_HEADER_FIELD = findField(REACTIVE_DEFAULT_SERVER_REQUEST_CLASS, "headers",
            org.springframework.web.reactive.function.server.ServerRequest.Headers.class);

}

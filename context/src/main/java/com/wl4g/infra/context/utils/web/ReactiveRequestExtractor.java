/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.ClassUtils2.resolveClassName;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.context.constant.ContextInfraConstants.TRACE_REQUEST_ID_HEADER;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.split;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.lang.reflect.Field;
import java.net.URI;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpRequest;
import org.springframework.util.MultiValueMap;

import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;
import com.wl4g.infra.common.web.WebUtils2;

/**
 * {@link ReactiveRequestExtractor}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-04-07 v3.0.0
 * @since v3.0.0
 */
public class ReactiveRequestExtractor implements WebRequestExtractor {

    private final HttpRequest request;

    public ReactiveRequestExtractor(org.springframework.http.server.ServerHttpRequest request) {
        this.request = notNullOf(request, "request");
    }

    public ReactiveRequestExtractor(org.springframework.http.server.reactive.ServerHttpRequest request) {
        this.request = notNullOf(request, "request");
    }

    public ReactiveRequestExtractor(org.springframework.web.reactive.function.server.ServerRequest request) {
        this.request = notNullOf(request, "request");
    }

    @Override
    public String getRequestId() {
        return request.getHeaders().getFirst(TRACE_REQUEST_ID_HEADER);
    }

    @Override
    public URI getRequestURI() {
        return request.getURI();
    }

    @Override
    public Principal getPrincipal() {
        if (request instanceof org.springframework.http.server.ServerHttpRequest) {
            return ((org.springframework.http.server.ServerHttpRequest) (request)).getPrincipal();
        } else if (request instanceof org.springframework.http.server.reactive.ServerHttpRequest) {
            return null; // not-get-principal
        } else if (request instanceof org.springframework.web.reactive.function.server.ServerRequest) {
            AtomicReference<Principal> principal = new AtomicReference<>(null);
            ((org.springframework.web.reactive.function.server.ServerRequest) request).principal()
                    .doOnSuccess(p -> principal.set(p));
            return principal.get();
        }
        return null;
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
        if (request instanceof org.springframework.http.server.ServerHttpRequest) {
            String urlQuery = ((org.springframework.http.server.ServerHttpRequest) (request)).getURI().getQuery();
            return WebUtils2.toQueryParams(urlQuery).keySet();
        } else if (request instanceof org.springframework.http.server.reactive.ServerHttpRequest) {
            return ((org.springframework.http.server.reactive.ServerHttpRequest) request).getQueryParams().keySet();
        } else if (request instanceof org.springframework.web.reactive.function.server.ServerRequest) {
            return ((org.springframework.web.reactive.function.server.ServerRequest) request).queryParams().keySet();
        }
        return null;
    }

    @Override
    public String getQueryValue(String name) {
        if (request instanceof org.springframework.http.server.ServerHttpRequest) {
            String urlQuery = ((org.springframework.http.server.ServerHttpRequest) (request)).getURI().getQuery();
            return WebUtils2.toQueryParams(urlQuery).get(name);
        } else if (request instanceof org.springframework.http.server.reactive.ServerHttpRequest) {
            return ((org.springframework.http.server.reactive.ServerHttpRequest) request).getQueryParams().getFirst(name);
        } else if (request instanceof org.springframework.web.reactive.function.server.ServerRequest) {
            return ((org.springframework.web.reactive.function.server.ServerRequest) request).queryParams().getFirst(name);
        }
        return null;
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
        if (request instanceof org.springframework.http.server.ServerHttpRequest) {
            List<String> cookies = safeArrayToList(split(getHeaderValue("cookie"), ";"));
            return cookies.stream().map(c -> split(c, "=")[0]).collect(toList());
        } else if (request instanceof org.springframework.http.server.reactive.ServerHttpRequest) {
            return ((org.springframework.http.server.reactive.ServerHttpRequest) request).getCookies().keySet();
        } else if (request instanceof org.springframework.web.reactive.function.server.ServerRequest) {
            return ((org.springframework.web.reactive.function.server.ServerRequest) request).cookies().keySet();
        }
        return null;
    }

    @Override
    public String getCookieValue(String name) {
        if (request instanceof org.springframework.http.server.ServerHttpRequest) {
            for (String cookieKeyValue : safeArrayToList(split(getHeaderValue("cookie"), ";"))) {
                String[] parts = split(cookieKeyValue, "=");
                if (nonNull(parts) && parts.length >= 2 && eqIgnCase(name, parts[0])) {
                    return parts[1];
                }
            }
        } else if (request instanceof org.springframework.http.server.reactive.ServerHttpRequest) {
            MultiValueMap<String, HttpCookie> cookies = ((org.springframework.http.server.reactive.ServerHttpRequest) request)
                    .getCookies();
            if (isEmpty(cookies)) {
                HttpCookie cookie = cookies.getFirst(name);
                if (nonNull(cookie)) {
                    return cookie.getValue();
                }
            }
        } else if (request instanceof org.springframework.web.reactive.function.server.ServerRequest) {
            MultiValueMap<String, HttpCookie> cookies = ((org.springframework.web.reactive.function.server.ServerRequest) request)
                    .cookies();
            if (isEmpty(cookies)) {
                HttpCookie cookie = cookies.getFirst(name);
                if (nonNull(cookie)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static final Class<?> REACTIVE_DEFAULT_SERVER_REQUEST_CLASS = resolveClassName(
            "org.springframework.web.reactive.function.server.DefaultServerRequest", null);
    public static final Field REACTIVE_SERVER_REQUEST_HEADER_FIELD = findField(REACTIVE_DEFAULT_SERVER_REQUEST_CLASS, "headers",
            org.springframework.web.reactive.function.server.ServerRequest.Headers.class);

}

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
package com.wl4g.infra.common.web;

import static com.wl4g.infra.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.web.UserAgentUtils.isBrowser;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Locale.US;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.EnumerationUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import com.wl4g.infra.common.collection.CollectionUtils2;
import com.wl4g.infra.common.collection.multimap.LinkedMultiValueMap;
import com.wl4g.infra.common.collection.multimap.MultiValueMap;
import com.wl4g.infra.common.lang.StringUtils2;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.runtime.JvmRuntimeTool;

/**
 * Generic Web utility.
 * 
 * @author James Wong James Wong <jameswong1376@gmail.com>
 * @version v1.0
 * @date 2018年11月30日
 * @since
 */
@Beta
public abstract class WebUtils2 extends WebUtils {
    protected final static SmartLogger log = getLogger(WebUtils2.class);

    /**
     * Gets HTTP remote IP address </br>
     * Warning: Be careful if you are implementing security, as all of these
     * headers are easy to fake.
     * 
     * @param request
     *            HTTP request
     * @return Real remote client IP
     */
    public static String getHttpRemoteAddr(HttpServletRequest request) {
        for (String header : HEADER_REAL_IP) {
            String ip = request.getHeader(header);
            if (isNotBlank(ip) && !"Unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Output JSON data with default settings
     * 
     * @param response
     * @param json
     * @throws IOException
     */
    public static void writeJson(HttpServletResponse response, String json) throws IOException {
        write(response, HttpServletResponse.SC_OK, MediaType.JSON_UTF_8.toString(), json.getBytes(Charsets.UTF_8));
    }

    /**
     * Output message
     * 
     * @param response
     * @param status
     * @param contentType
     * @param body
     * @throws IOException
     */
    public static void write(
            @NotNull HttpServletResponse response,
            int status,
            @NotBlank String contentType,
            @Nullable byte[] body) throws IOException {
        notNullOf(response, "response");
        hasTextOf(contentType, "contentType");

        OutputStream out = null;
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.setContentType(contentType);
        if (!isNull(body)) {
            out = response.getOutputStream();
            out.write(body);
            response.flushBuffer();
            // out.close(); // [Cannot close !!!]
        }
    }

    /**
     * Check that the requested resource is a base media file?
     * 
     * @param request
     * @return
     */
    public static boolean isMediaRequest(HttpServletRequest request) {
        return isMediaRequest(request.getRequestURI());
    }

    /**
     * Is true </br>
     * 
     * @param request
     * @param value
     * @param defaultValue
     * @return Return TRUE with true/t/y/yes/on/1/enabled
     */
    public static boolean isTrue(ServletRequest request, String keyname, boolean defaultValue) {
        return isTrue(request.getParameter(keyname), defaultValue);
    }

    /**
     * Reject http request methods.
     * 
     * @param allowMode
     * @param request
     * @param response
     * @param methods
     * @throws UnsupportedOperationException
     */
    public static void rejectRequestMethod(
            boolean allowMode,
            @NotNull ServletRequest request,
            @NotNull ServletResponse response,
            String... methods) throws UnsupportedOperationException {
        notNullOf(request, "request");
        notNullOf(response, "response");
        if (!isEmptyArray(methods)) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            boolean rejected1 = true, rejected2 = false;
            for (String method : methods) {
                if (method.equalsIgnoreCase(req.getMethod())) {
                    if (allowMode) {
                        rejected1 = false;
                    } else {
                        rejected2 = true;
                    }
                    break;
                }
            }
            if ((allowMode && rejected1) || (!allowMode && rejected2)) {
                resp.setStatus(405);
                throw new UnsupportedOperationException(format("No support '%s' request method", req.getMethod()));
            }
        }
    }

    /**
     * Parse the given string with matrix variables. An example string would
     * look like this {@code "q1=a;q1=b;q2=a,b,c"}. The resulting map would
     * contain keys {@code "q1"} and {@code "q2"} with values {@code ["a","b"]}
     * and {@code ["a","b","c"]} respectively.
     * 
     * @param matrixVariables
     *            the unparsed matrix variables string
     * @return a map with matrix variable names and values (never {@code null})
     */
    public static MultiValueMap<String, String> parseMatrixVariables(@Nullable String matrixVariables) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (!isBlank(matrixVariables)) {
            return result;
        }
        StringTokenizer pairs = new StringTokenizer(matrixVariables, ";");
        while (pairs.hasMoreTokens()) {
            String pair = pairs.nextToken();
            int index = pair.indexOf('=');
            if (index != -1) {
                String name = pair.substring(0, index);
                String rawValue = pair.substring(index + 1);
                for (String value : StringUtils2.commaDelimitedListToStringArray(rawValue)) {
                    result.add(name, value);
                }
            } else {
                result.add(pair, "");
            }
        }
        return result;
    }

    /**
     * Gets request parameter value by name
     * 
     * @param request
     * @param paramName
     * @param required
     * @return
     */
    public static String getRequestParam(@NotNull ServletRequest request, String paramName, boolean required) {
        notNullOf(request, "request");
        String paramValue = request.getParameter(paramName);
        String cleanedValue = paramValue;
        if (paramValue != null) {
            cleanedValue = paramValue.trim();
            if (cleanedValue.equals(EMPTY)) {
                cleanedValue = null;
            }
        }
        if (required) {
            hasText(cleanedValue, format("Request parameter '%s' is missing", paramName));
        }
        return cleanedValue;
    }

    /**
     * Extract request parameters with first value
     * 
     * @param request
     * @return
     */
    public static Map<String, String> getFirstParameters(@Nullable ServletRequest request) {
        return nonNull(request) ? safeMap(request.getParameterMap()).entrySet()
                .stream()
                .collect(toMap(e -> e.getKey(), e -> isEmptyArray(e.getValue()) ? null : e.getValue()[0])) : emptyMap();
    }

    /**
     * Get full request query URL
     * 
     * @param request
     * @return e.g:https://portal.mydomain.com/myapp/index?cid=xx&tid=xxx =>
     *         https://portal.mydomain.com/myapp/index?cid=xx&tid=xxx
     */
    public static String getFullRequestURL(@NotNull HttpServletRequest request) {
        notNullOf(request, "request");
        return getFullRequestURL(request, true);
    }

    /**
     * Get full request query URL
     * 
     * @param request
     * @param includeQuery
     *            Does it contain query parameters?
     * @return e.g:https://portal.mydomain.com/myapp/index?cid=xx&tid=xxx =>
     *         https://portal.mydomain.com/myapp/index?cid=xx&tid=xxx
     */
    public static String getFullRequestURL(@NotNull HttpServletRequest request, boolean includeQuery) {
        notNullOf(request, "request");
        String queryString = includeQuery ? request.getQueryString() : null;
        return request.getRequestURL().toString() + (StringUtils.isEmpty(queryString) ? "" : ("?" + queryString));
    }

    /**
     * Get full request query URI
     * 
     * @param request
     * @return e.g:https://portal.mydomain.com/myapp/index?cid=xx&tid=xxx =>
     *         /myapp/index?cid=xx&tid=xxx
     */
    public static String getFullRequestURI(@NotNull HttpServletRequest request) {
        notNullOf(request, "request");
        String queryString = request.getQueryString();
        return request.getRequestURI() + (StringUtils.isEmpty(queryString) ? "" : ("?" + queryString));
    }

    /**
     * Has HTTP Request header
     * 
     * @param request
     * @return
     */
    public static boolean hasHeader(@NotNull HttpServletRequest request, String name) {
        notNullOf(request, "request");
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            if (StringUtils.equalsIgnoreCase(names.nextElement(), name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets HTTP request headers.
     * 
     * @param request
     * @return
     */
    public static Map<String, String> getRequestHeaders(@NotNull HttpServletRequest request) {
        return getRequestHeaders(request, null);
    }

    /**
     * Gets request header value by name
     * 
     * @param request
     * @param headerName
     * @param required
     * @return
     */
    public static String getHeader(@NotNull HttpServletRequest request, String headerName, boolean required) {
        notNullOf(request, "request");
        String headerValue = request.getHeader(headerName);
        String cleanedValue = headerValue;
        if (headerValue != null) {
            cleanedValue = headerValue.trim();
            if (cleanedValue.equals(EMPTY)) {
                cleanedValue = null;
            }
        }
        if (required) {
            hasText(cleanedValue, format("Request header '%s' is missing", headerName));
        }
        return cleanedValue;
    }

    /**
     * Gets HTTP request headers.
     * 
     * @param request
     * @param filter
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getRequestHeaders(@NotNull HttpServletRequest request, @Nullable Predicate<String> filter) {
        notNullOf(request, "request");
        filter = isNull(filter) ? defaultStringAnyFilter : filter;
        List<String> headerNames = EnumerationUtils.toList(request.getHeaderNames());
        return headerNames.stream()
                .filter(filter)
                .map(name -> singletonMap(name, request.getHeader((String) name)))
                .flatMap(e -> e.entrySet().stream())
                .collect(toMap(e -> e.getKey(), e -> e.getValue()));
    }

    /**
     * Is XHR Request
     * 
     * @param request
     * @return
     */
    public static boolean isXHRRequest(@NotNull HttpServletRequest request) {
        notNullOf(request, "request");
        return isXHRRequest(new WebRequestExtractor() {
            @Override
            public String getHeaderValue(String name) {
                return request.getHeader("X-Requested-With");
            }
        });
    }

    /**
     * Get HTTP request RFC standard based URI
     * 
     * @param request
     * @param hasCtxPath
     * @return
     */
    public static String getRFCBaseURI(@NotNull HttpServletRequest request, boolean hasCtxPath) {
        notNullOf(request, "request");

        // Context path
        String ctxPath = request.getContextPath();
        notNull(ctxPath, "Http request contextPath must not be null");
        ctxPath = !hasCtxPath ? "" : ctxPath;

        // Scheme
        String scheme = request.getScheme();
        for (String schemeKey : HEADER_REAL_PROTOCOL) {
            String scheme0 = request.getHeader(schemeKey);
            if (!isBlank(scheme0)) {
                scheme = scheme0;
                break;
            }
        }

        // Host & Port
        String serverName = request.getServerName();
        int port = request.getServerPort();
        finished: for (String hostKey : HEADER_REAL_HOST) {
            // Notice: Manually traversing the request header can prevent some
            // wrapped HTTP requests from being case sensitive.
            Enumeration<String> en = request.getHeaderNames();
            while (en.hasMoreElements()) {
                String headerName = en.nextElement();
                // The original servlet specification requires that it be case
                // insensitive.
                if (equalsIgnoreCase(hostKey, headerName)) {
                    // me.domain.com:8080
                    serverName = request.getHeader(headerName);
                    if (headerName.contains(":")) {
                        String[] part = split(headerName, ":");
                        serverName = part[0];
                        if (!isBlank(part[1])) {
                            port = Integer.parseInt(part[1]);
                        }
                    } else if (equalsIgnoreCase(scheme, "HTTP")) {
                        port = 80;
                    } else if (equalsIgnoreCase(scheme, "HTTPS")) {
                        port = 443;
                    }
                    break finished;
                }
            }
        }

        // Obtain baseURI with default port.
        final String baseURI = getBaseURIForDefault(scheme, serverName, port).concat(ctxPath);

        if (JvmRuntimeTool.isJvmInDebugging) {
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> en = request.getHeaderNames();
            while (en.hasMoreElements()) {
                String name = en.nextElement();
                headers.put(name, request.getHeader(name));
            }
            log.info("::: Got the request RFC base URI: {}, by headers: {}", baseURI, headers);
        }

        return baseURI;
    }

    /**
     * Obtain the available request remember URL, for example: used to log in
     * successfully and redirect to the last remembered URL
     * 
     * @param request
     * @return
     */
    public static String getAvaliableRequestRememberUrl(@NotNull HttpServletRequest request) {
        notNullOf(request, "request");
        String rememberUrl = request.getHeader("Referer");
        // #[RFC7231], https://tools.ietf.org/html/rfc7231#section-5.5.2
        rememberUrl = isNotBlank(rememberUrl) ? rememberUrl : request.getHeader("Referrer");
        // Fallback
        if (isBlank(rememberUrl) && request.getMethod().equalsIgnoreCase("GET")) {
            rememberUrl = getFullRequestURL(request, true);
        }
        return rememberUrl;
    }

    /**
     * Check to see if the printing servlet is enabled to request the wrong
     * stack information.
     * 
     * @param request
     * @return
     */
    @Beta
    public static boolean isStacktraceRequest(@Nullable ServletRequest request) {
        if (isNull(request)) {
            return false;
        }
        if (JvmRuntimeTool.isJvmInDebugging) {
            return true;
        }
        String stacktrace = request.getParameter(PARAM_STACKTRACE);
        if (request instanceof HttpServletRequest) {
            if (isBlank(stacktrace)) {
                stacktrace = ((HttpServletRequest) request).getHeader(PARAM_STACKTRACE);
            }
            if (isBlank(stacktrace)) {
                stacktrace = CookieUtils.getCookie((HttpServletRequest) request, PARAM_STACKTRACE);
            }
        }
        if (isBlank(stacktrace)) {
            return false;
        }
        return isTrue(stacktrace.toLowerCase(US), false);
    }

    /**
     * Gets parameter by fallback for request query, headers, cookies, default
     * value.
     * 
     * @param <T>
     * @param request
     * @param bodyArgs
     * @param key
     * @param defaultValue
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T> T getDefaultMergedParam(
            final @NotNull HttpServletRequest request,
            final @Nullable Map<String, Object> bodyArgs,
            final @NotBlank String key,
            final @Nullable T defaultValue) {
        notNullOf(request, "request");
        hasTextOf(key, "key");
        final Map<String, String> queryParams = getFirstParameters(request);
        final Map<String, String> headers = getRequestHeaders(request);

        // 1. Get parameter by query (first priority).
        Object value = queryParams.get(key);
        // 2. Get parameter by headers (second priority).
        if (StringUtils2.isEmpty(value)) {
            // Notice: It may be converted to lowercase by services such as
            // front-end load balancing and gateways.
            // value = headers.get(key);
            value = headers.entrySet()
                    .stream()
                    .filter(e -> equalsAnyIgnoreCase(e.getKey(), key, "x-".concat(key)))
                    .map(e -> e.getValue())
                    .findFirst()
                    .orElse(null);
        }
        // 3. Get parameter by cookie (third priority).
        if (StringUtils2.isEmpty(value)) {
            value = CookieUtils.getCookie(request, key);
        }
        // 4. Get parameter by body (four priority).
        if (StringUtils2.isEmpty(value) && nonNull(bodyArgs)) {
            final Object valueObj = bodyArgs.get(key);
            if (nonNull(valueObj)) {
                if (valueObj instanceof String) {
                    value = valueObj.toString();
                } else if (valueObj instanceof Collection) {
                    value = CollectionUtils2.safeList((Collection) valueObj);
                }
            }
        }
        // 5. Use default if none.
        value = StringUtils2.isEmpty(value) ? defaultValue : value;

        // Transform string value to collection.
        if (defaultValue instanceof Collection && value instanceof String) {
            value = asList(split((String) value, ","));
        }
        return (T) value;
    }

    /**
     * Generic dynamic web message response type processing enumeration.
     * 
     * @author James Wong James Wong <jameswong1376@gmail.com>
     * @version v1.0
     * @date 2019年1月4日
     * @since
     */
    public static enum ResponseType {
        AUTO, WEBURI, JSON;

        /**
         * Default get response type parameter name.
         */
        public static final String DEFAULT_RESPTYPE_NAME = "response_type";

        /**
         * Get the name of the corresponding data type parameter. Note that
         * NGINX defaults to replace the underlined header, such as:
         * 
         * <pre>
         * header(response_type: json) => header(responsetype: json)
         * </pre>
         * 
         * and how to disable this feature of NGINX:
         * 
         * <pre>
         * http {
         * 	underscores_in_headers on;
         * }
         * </pre>
         */
        public static final String[] RESPTYPE_NAMES = { DEFAULT_RESPTYPE_NAME, "responsetype", "Response-Type",
                "X-Response-Type" };

        /**
         * Safe converter string to {@link ResponseType}
         * 
         * @param respType
         * @return
         */
        public static final ResponseType safeOf(String respType) {
            for (ResponseType t : values()) {
                if (String.valueOf(respType).equalsIgnoreCase(t.name())) {
                    return t;
                }
            }
            return null;
        }

        /**
         * Check whether the response is in JSON format
         * 
         * @param respTypeValue
         * @param request
         * @return
         */
        public static boolean isRespJSON(@NotBlank final String respTypeValue, @NotNull final HttpServletRequest request) {
            return determineResponseWithJson(safeOf(respTypeValue), new WebRequestExtractor() {
                @Override
                public String getQueryValue(String name) {
                    return request.getParameter(name);
                }

                @Override
                public String getHeaderValue(String name) {
                    return request.getHeader(name);
                }
            });
        }

        /**
         * Check whether the response is in JSON format
         * 
         * @param request
         * @return
         */
        public static boolean isRespJSON(@NotNull final HttpServletRequest request) {
            return isRespJSON(new WebRequestExtractor() {
                @Override
                public String getQueryValue(String name) {
                    return request.getParameter(name);
                }

                @Override
                public String getHeaderValue(String name) {
                    return request.getHeader(name);
                }
            }, null);
        }

        /**
         * Check whether the response is in JSON format.
         * 
         * @param extractor
         *            request wrapper
         * @param respTypeName
         *            response type paremter name.
         * @return
         */
        public static boolean isRespJSON(@NotNull WebRequestExtractor extractor, @Nullable String respTypeName) {
            notNullOf(extractor, "request");

            List<String> respTypeNames = asList(RESPTYPE_NAMES);
            if (!isBlank(respTypeName)) {
                respTypeNames.add(respTypeName);
            }

            for (String name : respTypeNames) {
                String respTypeValue = extractor.getQueryValue(name);
                respTypeValue = isBlank(respTypeValue) ? extractor.getHeaderValue(name) : respTypeValue;
                if (!isBlank(respTypeValue)) {
                    return determineResponseWithJson(safeOf(respTypeValue), extractor);
                }
            }

            // Using default auto mode
            return determineResponseWithJson(ResponseType.AUTO, extractor);
        }

        /**
         * Determine response JSON message
         * 
         * @param respType
         * @param extractor
         * @return
         */
        private static boolean determineResponseWithJson(ResponseType respType, @NotNull WebRequestExtractor extractor) {
            notNullOf(extractor, "request");

            // Using default strategy
            if (Objects.isNull(respType)) {
                respType = ResponseType.AUTO;
            }

            // Has header(accept:application/json)
            boolean hasAccpetJson = false;
            for (String typePart : String.valueOf(extractor.getHeaderValue("Accept")).split(",")) {
                if (startsWithIgnoreCase(typePart, "application/json")) {
                    hasAccpetJson = true;
                    break;
                }
            }

            // Has header(origin:xx.domain.com)
            boolean hasOrigin = !isBlank(extractor.getHeaderValue("Origin"));

            // Is header[XHR] ?
            boolean isXhr = isXHRRequest(extractor);

            switch (respType) { // Matching
            case JSON:
                return true;
            case WEBURI:
                return false;
            case AUTO:
                /*
                 * When it's a browser request and not an XHR and token request
                 * (no X-Requested-With: XMLHttpRequest and token at the head of
                 * the line), it responds to the rendering page, otherwise it
                 * responds to JSON.
                 */
                return isBrowser(extractor.getHeaderValue("User-Agent")) ? (isXhr || hasAccpetJson || hasOrigin) : true;
            default:
                throw new IllegalStateException(format("Illegal response type %s", respType));
            }
        }
    }

}
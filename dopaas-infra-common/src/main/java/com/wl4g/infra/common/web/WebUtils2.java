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
package com.wl4g.infra.common.web;

import static com.wl4g.infra.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.StringUtils2.isDomain;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.web.UserAgentUtils.isBrowser;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Locale.US;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isAnyBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.replaceIgnoreCase;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.ServerRequest;

import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import com.wl4g.infra.common.collection.CollectionUtils2;
import com.wl4g.infra.common.collection.multimap.LinkedMultiValueMap;
import com.wl4g.infra.common.collection.multimap.MultiValueMap;
import com.wl4g.infra.common.jvm.JvmRuntimeKit;
import com.wl4g.infra.common.lang.Assert2;
import com.wl4g.infra.common.lang.StringUtils2;
import com.wl4g.infra.common.log.SmartLogger;

/**
 * Generic Web utilitys.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月30日
 * @since
 */
@Beta
public abstract class WebUtils2 {
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
    public static void write(@NotNull HttpServletResponse response, int status, @NotBlank String contentType,
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
     * Check that the requested resource is a base media file?
     * 
     * @param path
     * @return
     */
    public static boolean isMediaRequest(String path) {
        String ext = StringUtils2.getFilenameExtension(path);
        for (String media : MEDIA_BASE) {
            if (equalsIgnoreCase(ext, media)) {
                return true;
            }
        }
        return false;
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
     * Is true </br>
     * 
     * @param value
     * @return Return TRUE with true/t/y/yes/on/1/enabled
     */
    public static boolean isTrue(String value) {
        return isTrue(value, false);
    }

    /**
     * Is true </br>
     * 
     * @param value
     * @param defaultValue
     * @return Return TRUE with true/t/y/yes/on/1/enabled
     */
    public static boolean isTrue(String value, boolean defaultValue) {
        return StringUtils2.isTrue(value, defaultValue);
    }

    /**
     * To query URL parameters.
     * 
     * <pre>
     * toQueryParams("application=iam-example&redirect_url=http://my.com/index") == {application->iam-example, redirect_url=>http://my.com/index}
     * toQueryParams("application=iam-example&redirect_url=http://my.com/index/#/me") == {application->iam-example, redirect_url=>http://my.com/index/#/me}
     * </pre>
     * 
     * @param urlQuery
     * @return
     */
    public static Map<String, String> toQueryParams(@Nullable String urlQuery) {
        Map<String, String> parameters = new LinkedHashMap<>(4);
        if (isBlank(urlQuery))
            return parameters;
        try {
            // Remove the character to the left of the '?'
            int separQuestIndex = urlQuery.lastIndexOf("?");
            if (separQuestIndex > 0) {
                urlQuery = urlQuery.substring(separQuestIndex + 1);
            }

            String[] paramPairs = urlQuery.split("&");
            for (int i = 0; i < paramPairs.length; i++) {
                String[] parts = trimToEmpty(paramPairs[i]).split("=");
                if (parts.length >= 2) {
                    parameters.put(parts[0], parts[1]);
                }
            }
            return parameters;
        } catch (Exception e) {
            throw new IllegalArgumentException(format("Illegal parameter format. '%s'", urlQuery), e);
        }
    }

    /**
     * Map to query URL
     * 
     * @param uri
     * @param queryParams
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static String applyQueryURL(@Nullable String uri, @Nullable Map queryParams) {
        if (CollectionUtils2.isEmpty(queryParams) || isBlank(uri)) {
            return uri;
        }

        URI _uri = URI.create(uri);
        // Merge origin-older uri query parameters.
        Map<String, String> mergeParams = new HashMap<>(toQueryParams(_uri.getQuery()));
        mergeParams.putAll(queryParams);
        // Gets base URI.
        StringBuffer url = new StringBuffer(uri); // Relative path?
        if (!isAnyBlank(_uri.getScheme(), _uri.getHost())) {
            url.setLength(0); // Reset
            url.append(getBaseURIForDefault(_uri.getScheme(), _uri.getHost(), _uri.getPort()));
            url.append(_uri.getPath());
        }
        if (url.lastIndexOf("?") == -1) {
            url.append("?");
        }

        // To URI parameters string
        for (Iterator<?> it = mergeParams.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            url.append(key);
            url.append("=");
            // Prevents any occurrence of a value string null
            Object value = mergeParams.get(key);
            if (value != null) {
                url.append(value); // "null"
            }
            if (it.hasNext()) {
                url.append("&");
            }
        }
        return url.toString();
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
    public static void rejectRequestMethod(boolean allowMode, @NotNull ServletRequest request, @NotNull ServletResponse response,
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
     * Gets multi map first value.
     * 
     * @param params
     * @return
     */
    public static String getMultiMapFirstValue(@Nullable Map<String, List<String>> params, String name) {
        if (isNull(params)) {
            return null;
        }
        return params.entrySet()
                .stream()
                .filter(e -> equalsIgnoreCase(e.getKey(), name))
                .map(e -> CollectionUtils2.isEmpty(e.getValue()) ? e.getValue().get(0) : null)
                .filter(e -> !isNull(e))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets request parameter value by name
     * 
     * @param request
     * @param paramName
     * @param required
     * @return
     */
    public static String getRequestParam(ServletRequest request, String paramName, boolean required) {
        String paramValue = request.getParameter(paramName);
        String cleanedValue = paramValue;
        if (paramValue != null) {
            cleanedValue = paramValue.trim();
            if (cleanedValue.equals(EMPTY)) {
                cleanedValue = null;
            }
        }
        if (required) {
            hasTextOf(cleanedValue, paramName);
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
        return nonNull(request) ? safeMap(request.getParameterMap()).entrySet().stream().collect(
                toMap(e -> e.getKey(), e -> isEmptyArray(e.getValue()) ? null : e.getValue()[0])) : emptyMap();
    }

    /**
     * Get full request query URL
     * 
     * @param request
     * @return e.g:https://portal.mydomain.com/myapp/index?cid=xx&tid=xxx =>
     *         https://portal.mydomain.com/myapp/index?cid=xx&tid=xxx
     */
    public static String getFullRequestURL(HttpServletRequest request) {
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
    public static String getFullRequestURL(HttpServletRequest request, boolean includeQuery) {
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
    public static String getFullRequestURI(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return request.getRequestURI() + (StringUtils.isEmpty(queryString) ? "" : ("?" + queryString));
    }

    /**
     * Has HTTP Request header
     * 
     * @param request
     * @return
     */
    public static boolean hasHeader(HttpServletRequest request, String name) {
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
        return isXHRRequest(new RequestExtractor() {
            @Override
            public String getHeader(String name) {
                return request.getHeader("X-Requested-With");
            }
        });
    }

    /**
     * Is XHR Request
     * 
     * @param extractor
     * @return
     */
    public static boolean isXHRRequest(@NotNull RequestExtractor extractor) {
        notNullOf(extractor, "extractor");
        return equalsIgnoreCase(extractor.getHeader("X-Requested-With"), "XMLHttpRequest");
    }

    /**
     * URL encode by UTF-8
     * 
     * @param url
     *            plain URL
     * @return
     */
    public static String safeEncodeURL(String url) {
        try {
            if (!contains(trimToEmpty(url).toLowerCase(US), URL_SEPAR_SLASH)) {
                return URLEncoder.encode(url, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return url;
    }

    /**
     * URL decode by UTF-8
     * 
     * @param url
     *            encode URL
     * @return
     */
    public static String safeDecodeURL(String url) {
        try {
            if (containsAny(trimToEmpty(url).toLowerCase(US), URL_SEPAR_SLASH, URL_SEPAR_QUEST, URL_SEPAR_COLON)) {
                return URLDecoder.decode(url, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return url;
    }

    /**
     * Extract top level domain string. </br>
     * 
     * <pre>
     *extDomainString("my.wl4g.com")                         =>  my.wl4g.com
     *extDomainString("//my.wl4g.com/myapp1")                =>  my.wl4g.com
     *extDomainString("/myapp1/api/v2/list")                 =>  ""
     *extDomainString("http://my.wl4g.com.cn/myapp1")        =>  my.wl4g.com.cn
     *extDomainString("https://my2.my1.wl4g.com:80/myapp1")  =>  my2.my1.wl4g.com
     * </pre>
     * 
     * @param hostOrUri
     * @return
     */
    public static String extDomainString(String hostOrUri) {
        if (isBlank(hostOrUri)) {
            return hostOrUri;
        }
        hostOrUri = safeDecodeURL(hostOrUri);
        String domain = hostOrUri; // Is host?
        if (containsAny(hostOrUri, '/')) { // Is URI?
            domain = URI.create(hostOrUri).getHost();
        }
        // Check domain available?
        // isTrueOf(isDomain(domain), format("hostOrUri: %s", hostOrUri));
        if (!isDomain(domain) || isBlank(domain)) {
            return EMPTY;
        }
        return domain;
    }

    /**
     * Extract top level domain string. </br>
     * 
     * <pre>
     *extTopDomainString("my.wl4g.com")                         =>  wl4g.com
     *extTopDomainString("//my.wl4g.com/myapp1")                =>  wl4g.com
     *extTopDomainString("/myapp1/api/v2/list")                 =>  ""
     *extTopDomainString("http://my.wl4g.com.cn/myapp1")        =>  wl4g.com.cn
     *extTopDomainString("https://my2.my1.wl4g.com:80/myapp1")  =>  wl4g.com
     * </pre>
     * 
     * @param hostOrUri
     * @return
     */
    public static String extTopDomainString(String hostOrUri) {
        String domain = extDomainString(hostOrUri);
        if (isBlank(domain)) { // Available?
            return EMPTY;
        }
        String[] parts = split(domain, ".");
        int endIndex = 2;
        if (domain.endsWith("com.cn")) { // Special parse
            endIndex = 3;
        }
        StringBuffer topDomain = new StringBuffer();
        for (int i = 0; i < parts.length; i++) {
            if (i >= (parts.length - endIndex)) {
                topDomain.append(parts[i]);
                if (i < (parts.length - 1)) {
                    topDomain.append(".");
                }
            }
        }
        return topDomain.toString();
    }

    /**
     * Domain names equals two URIs are equal (including secondary and tertiary
     * domain names, etc. Exact matching)
     * 
     * e.g.</br>
     * 
     * <pre>
     * isEqualWithDomain("http://my.wl4g.com/myapp1","http://my.wl4g.com/myapp2")=true
     * isEqualWithDomain("http://my1.domin.com/myapp1","http://my.wl4g.com/myapp2")=false
     * isEqualWithDomain("http://my.wl4g.com:80/myapp1","http://my.wl4g.com:8080/myapp2")=true
     * isEqualWithDomain("https://my.wl4g.com:80/myapp1","http://my.wl4g.com:8080/myapp2")=true
     * isEqualWithDomain("http://localhost","http://localhost:8080/myapp2")=true
     * isEqualWithDomain("http://127.0.0.1","http://127.0.0.1:8080/myapp2")=true
     * </pre>
     * 
     * @param uria
     * @param urib
     * @return
     */
    public static boolean isEqualWithDomain(String uria, String urib) {
        if (isNull(uria) || isNull(urib)) {
            return false;
        }
        return URI.create(safeDecodeURL(uria)).getHost().equals(URI.create(safeDecodeURL(urib)).getHost());
    }

    /**
     * Check whether the wildcard domain uri belongs to the same origin. </br>
     * 
     * e.g:
     * 
     * <pre>
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com/API/v2", "http://bb.aa.domain.com/API/v2", true) == true
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com/API/v2", "https://bb.aa.domain.com/API/v2", true) == false
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com/api/v2/", "http://bb.aa.domain.com/API/v2", true) == true
     * {@link #isSameWildcardOrigin}("http://bb.*.domain.com", "https://bb.aa.domain.com", false) == true
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com", "https://bb.aa.domain.com", true) == false
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com:8080", "http://bb.aa.domain.com:8080/", true) == true
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com:8080", "http://bb.aa.domain.com:8443/v2/xx", true) == true
     * {@link #isSameWildcardOrigin}("http://*.aa.domain.com:*", "http://bb.aa.domain.com:8443/v2/xx", true) == true
     * </pre>
     * 
     * @param defWildcardUri
     *            Definition wildcard URI
     * @param requestUri
     * @param checkScheme
     * @return
     */
    public static boolean isSameWildcardOrigin(String defWildcardUri, String requestUri, boolean checkScheme) {
        if (isBlank(defWildcardUri) || isBlank(requestUri))
            return false;
        if (defWildcardUri.equals(requestUri)) // URL equaled?
            return true;

        // Scheme matched?
        URI uri1 = URI.create(defWildcardUri);
        URI uri2 = URI.create(requestUri);
        final boolean schemeMatched = uri1.getScheme().equalsIgnoreCase(uri2.getScheme());
        if (checkScheme && !schemeMatched)
            return false;

        // Hostname equaled?
        String hostname1 = extractWildcardEndpoint(defWildcardUri);
        String hostname2 = extractWildcardEndpoint(requestUri);
        if (equalsIgnoreCase(hostname1, hostname2))
            return true;

        // Hostname wildcard matched?
        boolean wildcardHostnameMatched = false;
        String[] parts1 = split(hostname1, ".");
        String[] parts2 = split(hostname2, ".");
        for (int i = 0; i < parts1.length; i++) {
            if (equalsIgnoreCase(parts1[i], "*")) {
                if (i < (hostname1.length() - 1) && i < (hostname2.length() - 1)) {
                    String compare1 = join(parts1, ".", i + 1, parts1.length);
                    String compare2 = join(parts2, ".", i + 1, parts2.length);
                    if (equalsIgnoreCase(compare1, compare2)) {
                        wildcardHostnameMatched = true;
                        break;
                    }
                }
            }
        }
        // Check scheme matched.
        if (checkScheme && wildcardHostnameMatched) {
            return schemeMatched;
        }

        return wildcardHostnameMatched;
    }

    /**
     * Extract domain text from {@link URI}. </br>
     * Uri resolution cannot be used here because it may fail when there are
     * wildcards, e.g,
     * {@link URI#create}("http://*.aa.domain.com/api/v2/).gethost() is
     * null.</br>
     * 
     * <pre>
     * {@link #extractWildcardHostName}("http://*.domain.com/v2/xx") == *.domain.com
     * {@link #extractWildcardHostName}("http://*.aa.domain.com:*") == *.aa.domain.com
     * {@link #extractWildcardHostName}("http://*.bb.domain.com:8080/v2/xx") == *.bb.domain.com
     * </pre>
     * 
     * @param wildcardUri
     * @return
     */
    public static String extractWildcardEndpoint(String wildcardUri) {
        if (isEmpty(wildcardUri))
            return EMPTY;

        wildcardUri = trimToEmpty(safeEncodeURL(wildcardUri)).toLowerCase(US);
        String noPrefix = wildcardUri.substring(wildcardUri.indexOf(URL_SEPAR_PROTO) + URL_SEPAR_PROTO.length());
        int slashIndex = noPrefix.indexOf(URL_SEPAR_SLASH);
        String serverName = noPrefix;
        if (slashIndex > 0) {
            serverName = noPrefix.substring(0, slashIndex);
        }

        // Check domain illegal?
        // e.g, http://*.domain.com:8080[allow]
        // http://*.domain.com:*[allow]
        // http://*.aa.*.domain.com[noallow]
        String hostname = serverName;
        if (serverName.contains(URL_SEPAR_COLON)) {
            hostname = serverName.substring(0, serverName.indexOf(URL_SEPAR_COLON));
        }
        Assert2.isTrue(hostname.indexOf("*") == hostname.lastIndexOf("*"), "Illegal serverName: %s, contains multiple wildcards!",
                serverName);
        return safeDecodeURL(hostname);
    }

    /**
     * Determine whether the requested URL belongs to the domain. e.g:</br>
     * withInDomain("my.domain.com","http://my.domain.com/myapp") = true </br>
     * withInDomain("my.domain.com","https://my.domain.com/myapp") = true </br>
     * withInDomain("my.domain.com","https://my1.domain.com/myapp") = false
     * </br>
     * withInDomain("*.domain.com", "https://other1.domain.com/myapp") = true
     * </br>
     * 
     * @param domain
     * @param url
     * @return
     */
    public static boolean withInDomain(String domain, String url) {
        notNull(domain, "'domain' must not be null");
        notNull(url, "'requestUrl' must not be null");
        try {
            String hostname = new URI(safeDecodeURL(cleanURI(url))).getHost();
            if (!domain.contains("*")) {
                Assert2.isTrue(isDomain(domain), String.format("Illegal domain[%s] name format", domain));
                return equalsIgnoreCase(domain, hostname);
            }
            if (domain.startsWith("*")) {
                return equalsIgnoreCase(domain.substring(1), hostname.substring(hostname.indexOf(".")));
            }
            return false;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Determine whether the requested URL belongs to the base URI. e.g:</br>
     * withInURL("https://domain/myapp/login","https://domain/myapp/login?t=123")
     * == true </br>
     * withInURL("https://domain/myapp","https://domain/myapp/login?t=123") ==
     * true </br>
     * withInURL("https://domain/myapp/login?r=abc","https://domain/myapp/login?t=123")
     * == true </br>
     * withInURL("https://domain/myapp/login?r=abc","http://domain/myapp/login?t=123")
     * == false </br>
     * </br>
     * 
     * @param baseUrl
     * @param url
     * @return
     */
    public static boolean withInURL(String baseUrl, String url) {
        if (baseUrl == null || url == null) {
            return false;
        }
        try {
            // If it's a URL in decoding format
            URI baseUrl0 = new URI(safeDecodeURL(cleanURI(baseUrl)));
            URI uri0 = new URI(safeDecodeURL(cleanURI(url)));
            return (StringUtils.startsWithIgnoreCase(uri0.getRawPath(), baseUrl0.getRawPath())
                    && StringUtils.equalsIgnoreCase(uri0.getScheme(), baseUrl0.getScheme())
                    && StringUtils.equalsIgnoreCase(uri0.getHost(), baseUrl0.getHost()) && uri0.getPort() == baseUrl0.getPort());
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    /**
     * Get HTTP request RFC standard based URI
     * 
     * @param request
     * @param hasCtxPath
     * @return
     */
    public static String getRFCBaseURI(HttpServletRequest request, boolean hasCtxPath) {
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

        // Debugging.
        if (JvmRuntimeKit.isJVMDebugging) {
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> en = request.getHeaderNames();
            while (en.hasMoreElements()) {
                String name = en.nextElement();
                headers.put(name, request.getHeader(name));
            }
            log.info("Debug print ::: Got the request RFC baseurl of proxied: {}, from headers: {}", baseURI, headers);
        }

        return baseURI;
    }

    /**
     * Obtain base URI for default. </br>
     * 
     * <pre>
     * getBaseURIForDefault("http", "my.com", 8080) == "http://my.com:8080"
     * getBaseURIForDefault("http", "my.com", 80) == "http://my.com"
     * getBaseURIForDefault("https", "my.com", 443) == "https://my.com"
     * getBaseURIForDefault("https", "my.com", -1) == "https://my.com"
     * </pre>
     * 
     * @param scheme
     * @param serverName
     * @param port
     * @return
     */
    public static String getBaseURIForDefault(String scheme, String serverName, int port) {
        notNull(scheme, "Http request scheme must not be empty");
        notNull(serverName, "Http request serverName must not be empty");
        StringBuffer baseUri = new StringBuffer(scheme).append("://").append(serverName);
        if (port > 0) {
            Assert2.isTrue((port > 0 && port < 65536), "Http server port must be greater than 0 and less than 65536");
            if (!((equalsIgnoreCase(scheme, "HTTP") && port == 80) || (equalsIgnoreCase(scheme, "HTTPS") && port == 443))) {
                baseUri.append(":").append(port);
            }
        }
        return baseUri.toString();
    }

    /**
     * Clean request URI. </br>
     * 
     * <pre>
     * cleanURI("https://my.domain.com//myapp///index?t=123") => "https://my.domain.com/myapp/index?t=123"
     * </pre>
     * 
     * @param uri
     * @return
     */
    @Beta
    public static String cleanURI(String uri) {
        if (isBlank(uri)) {
            return uri;
        }

        // Check syntax
        uri = URI.create(uri).toString();

        /**
         * Cleaning.</br>
         * Note: that you cannot change the original URI case.
         */
        try {
            String encodeUrl = safeEncodeURL(uri);
            String pathUrl = encodeUrl, schema = EMPTY;
            if (encodeUrl.toLowerCase(US).contains(URL_SEPAR_PROTO)) {
                // Start from "://"
                int startIndex = encodeUrl.toLowerCase(US).indexOf(URL_SEPAR_PROTO);
                schema = encodeUrl.substring(0, startIndex) + URL_SEPAR_PROTO;
                pathUrl = encodeUrl.substring(startIndex + URL_SEPAR_PROTO.length());
            }

            // Cleanup for: '/shopping/order//list' => '/shopping/order/list'
            String lastCleanUrl = pathUrl;
            for (int i = 0; i < 256; i++) { // https://www.ietf.org/rfc/rfc2616.txt#3.2.1
                String cleanUrl = replaceIgnoreCase(lastCleanUrl, (URL_SEPAR_SLASH2).toUpperCase(), URL_SEPAR_SLASH);
                if (StringUtils.equals(cleanUrl, lastCleanUrl)) {
                    break;
                } else {
                    lastCleanUrl = cleanUrl;
                }
            }
            return safeDecodeURL(schema + lastCleanUrl);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Obtain the available request remember URL, for example: used to log in
     * successfully and redirect to the last remembered URL
     * 
     * @param request
     * @return
     */
    public static String getAvaliableRequestRememberUrl(HttpServletRequest request) {
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
     * Check whether the URI is relative to the path.
     * 
     * e.g.</br>
     * 
     * <pre>
     * isRelativeUri("//my.wl4g.com/myapp1") = false </br>
     * isRelativeUri("http://my.wl4g.com/myapp1") = false </br>
     * isRelativeUri("https://my.wl4g.com:80/myapp1") = false </br>
     * isRelativeUri("/myapp1/api/v2/list") = true </br>
     * </pre>
     * 
     * @param uri
     * @return
     */
    public static boolean isRelativeUri(String uri) {
        if (isBlank(uri))
            return false;
        return isBlank(URI.create(safeDecodeURL(uri)).getScheme()) && !uri.startsWith("//");
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
        if (JvmRuntimeKit.isJVMDebugging) {
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
     * Generic dynamic web message response type processing enumeration.
     * 
     * @author Wangl.sir <983708408@qq.com>
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
        public static final String[] RESPTYPE_NAMES = { DEFAULT_RESPTYPE_NAME, "responsetype", "Response-Type" };

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
            return determineResponseWithJson(safeOf(respTypeValue), new RequestExtractor() {
                @Override
                public String getQueryParam(String name) {
                    return request.getParameter(name);
                }

                @Override
                public String getHeader(String name) {
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
            return isRespJSON(new RequestExtractor() {
                @Override
                public String getQueryParam(String name) {
                    return request.getParameter(name);
                }

                @Override
                public String getHeader(String name) {
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
        public static boolean isRespJSON(@NotNull RequestExtractor extractor, @Nullable String respTypeName) {
            notNullOf(extractor, "request");

            List<String> respTypeNames = asList(RESPTYPE_NAMES);
            if (!isBlank(respTypeName)) {
                respTypeNames.add(respTypeName);
            }

            for (String name : respTypeNames) {
                String respTypeValue = extractor.getQueryParam(name);
                respTypeValue = isBlank(respTypeValue) ? extractor.getHeader(name) : respTypeValue;
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
        private static boolean determineResponseWithJson(ResponseType respType, @NotNull RequestExtractor extractor) {
            notNullOf(extractor, "request");

            // Using default strategy
            if (Objects.isNull(respType)) {
                respType = ResponseType.AUTO;
            }

            // Has header(accept:application/json)
            boolean hasAccpetJson = false;
            for (String typePart : String.valueOf(extractor.getHeader("Accept")).split(",")) {
                if (startsWithIgnoreCase(typePart, "application/json")) {
                    hasAccpetJson = true;
                    break;
                }
            }

            // Has header(origin:xx.domain.com)
            boolean hasOrigin = !isBlank(extractor.getHeader("Origin"));

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
                return isBrowser(extractor.getHeader("User-Agent")) ? (isXhr || hasAccpetJson || hasOrigin) : true;
            default:
                throw new IllegalStateException(format("Illegal response type %s", respType));
            }
        }

    }

    /**
     * Request extractor wrapper, It is mainly to solve the request types of
     * different models or protocols, such as: Xxx{@link ServletRequest} or
     * {@link HttpServletRequest} or {@link ServerRequest}(reactive) etc
     * 
     * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
     * @version v1.0 2020-09-15
     * @since
     */
    public static interface RequestExtractor {

        /**
         * Gets query parameter by name.
         * 
         * @param name
         * @return
         */
        default String getQueryParam(String name) {
            throw new UnsupportedOperationException();
        }

        /**
         * Gets headers parameter by name.
         * 
         * @param name
         * @return
         */
        default String getHeader(String name) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * URL scheme(HTTPS)
     */
    public static final String URL_SCHEME_HTTPS = "https";

    /**
     * URL scheme(HTTP)
     */
    public static final String URL_SCHEME_HTTP = "http";

    /**
     * URL separator(/)
     */
    public static final String URL_SEPAR_SLASH = "%2f";

    /**
     * URL double separator(//)
     */
    public static final String URL_SEPAR_SLASH2 = URL_SEPAR_SLASH + URL_SEPAR_SLASH;

    /**
     * URL separator(?)
     */
    public static final String URL_SEPAR_QUEST = "%3f";

    /**
     * URL colon separator(:)
     */
    public static final String URL_SEPAR_COLON = "%3a";

    /**
     * Protocol separators, such as
     * https://my.domain.com=>https%3A%2F%2Fmy.domain.com
     */
    public static final String URL_SEPAR_PROTO = URL_SEPAR_COLON + URL_SEPAR_SLASH + URL_SEPAR_SLASH;

    /**
     * Request the header key name of real client IP. </br>
     * 
     * <pre>
     *	一、没有使用代理服务器的情况：
     *	      REMOTE_ADDR = 您的 IP
     *	      HTTP_VIA = 没数值或不显示
     *	      HTTP_X_FORWARDED_FOR = 没数值或不显示
     *	二、使用透明代理服务器的情况：Transparent Proxies
     *	      REMOTE_ADDR = 最后一个代理服务器 IP 
     *	      HTTP_VIA = 代理服务器 IP
     *	      HTTP_X_FORWARDED_FOR = 您的真实 IP ，经过多个代理服务器时，这个值类似如下：203.98.182.163, 203.98.182.163, 203.129.72.215。
     *	   这类代理服务器还是将您的信息转发给您的访问对象，无法达到隐藏真实身份的目的。
     *	三、使用普通匿名代理服务器的情况：Anonymous Proxies
     *	      REMOTE_ADDR = 最后一个代理服务器 IP 
     *	      HTTP_VIA = 代理服务器 IP
     *	      HTTP_X_FORWARDED_FOR = 代理服务器 IP ，经过多个代理服务器时，这个值类似如下：203.98.182.163, 203.98.182.163, 203.129.72.215。
     *	   隐藏了您的真实IP，但是向访问对象透露了您是使用代理服务器访问他们的。
     *	四、使用欺骗性代理服务器的情况：Distorting Proxies
     *	      REMOTE_ADDR = 代理服务器 IP 
     *	      HTTP_VIA = 代理服务器 IP 
     *	      HTTP_X_FORWARDED_FOR = 随机的 IP ，经过多个代理服务器时，这个值类似如下：203.98.182.163, 203.98.182.163, 203.129.72.215。
     *	   告诉了访问对象您使用了代理服务器，但编造了一个虚假的随机IP代替您的真实IP欺骗它。
     *	五、使用高匿名代理服务器的情况：High Anonymity Proxies (Elite proxies)
     *	      REMOTE_ADDR = 代理服务器 IP
     *	      HTTP_VIA = 没数值或不显示
     *	      HTTP_X_FORWARDED_FOR = 没数值或不显示 ，经过多个代理服务器时，这个值类似如下：203.98.182.163, 203.98.182.163, 203.129.72.215。
     * </pre>
     */
    public static final String[] HEADER_REAL_IP = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "X-Real-IP",
            "REMOTE_ADDR", "Remote-Addr", "RemoteAddr", // RemoteAddr
            "REMOTE_IP", "Remote-Ip", "RemoteIp", // RemoteIp: Aliyun-SLB
            "HTTP_X_FORWARDED_FOR", "Http-X-Forwarded-For", "HttpXForwardedFor", // HttpXForwardedFor
            "HTTP_X_FORWARDED", "Http-X-Forwarded", "HttpXForwarded", // HttpXForwarded
            "HTTP_Client_IP", "Http-Client-Ip", "HttpClientIp", // HttpClientIp
            "HTTP_X_CLUSTER_CLIENT_IP", "Http-X-Cluster-Client-Ip", "HttpXClusterClientIp", // HttpXClusterClientIp
            "HTTP_FORWARDED_FOR", "Http-Forwarded-For", "HttpForwardedFor", // HttpForwardedFor
            "HTTP_VIA ", "Http-Via", "HttpVia" }; // HttpVia

    /**
     * Request the header key name of real protocol scheme.
     */
    public static final String[] HEADER_REAL_PROTOCOL = { "X-Forwarded-Proto" };

    /**
     * Request the header key name of real host
     */
    public static final String[] HEADER_REAL_HOST = { "Host" };

    /**
     * Common media file suffix definitions
     */
    public static final String[] MEDIA_BASE = new String[] { "ico", "icon", "css", "js", "html", "shtml", "htm", "jsp", "jspx",
            "jsf", "aspx", "asp", "php", "jpeg", "jpg", "png", "bmp", "gif", "tif", "pic", "swf", "svg", "ttf", "eot", "eot@",
            "woff", "woff2", "wd3", "txt", "doc", "docx", "wps", "ppt", "pptx", "pdf", "excel", "xls", "xlsx", "avi", "wav",
            "mp3", "amr", "mp4", "aiff", "rar", "tar.gz", "tar", "zip", "gzip", "ipa", "plist", "apk", "7-zip" };

    /**
     * Controlling enabled unified exception handling stacktrace information.
     */
    public static final String PARAM_STACKTRACE = getenv().getOrDefault("XCLOUD_COMPONENT_REQUEST_STACKTRACE_PARAM",
            "x-stacktrace");

    private static final Predicate<String> defaultStringAnyFilter = name -> true;

}
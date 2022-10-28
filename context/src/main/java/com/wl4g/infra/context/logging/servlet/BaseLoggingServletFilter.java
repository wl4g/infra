/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.context.logging.servlet;

import static com.google.common.collect.Lists.newArrayList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.Collectors2.toCaseInsensitiveHashMap;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static java.util.Objects.isNull;
import static org.apache.commons.collections.EnumerationUtils.toList;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMapAdapter;

import com.wl4g.infra.context.constant.ContextInfraConstants;
import com.wl4g.infra.context.logging.LoggingMessageUtil;
import com.wl4g.infra.context.logging.config.LoggingMessageProperties;
import com.wl4g.infra.context.utils.web.ServletRequsetExtractor;
import com.wl4g.infra.context.web.matcher.SpelRequestMatcher;

import lombok.AllArgsConstructor;
import lombok.CustomLog;

/**
 * {@link BaseLoggingServletFilter}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-05-20 v3.0.0
 * @since v3.0.0
 */
@CustomLog
@AllArgsConstructor
public abstract class BaseLoggingServletFilter implements Filter {

    protected final LoggingMessageProperties loggingConfig;
    protected final Environment environment;
    protected final SpelRequestMatcher requestMatcher;

    public BaseLoggingServletFilter(LoggingMessageProperties loggingConfig, Environment environment) {
        this.loggingConfig = notNullOf(loggingConfig, "loggingConfig");
        this.environment = notNullOf(environment, "environment");
        // Build gray request matcher.
        this.requestMatcher = new SpelRequestMatcher(loggingConfig.getPreferMatchRuleDefinitions());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = ((HttpServletRequest) request);
        // Check if filtering flight logging is enabled.
        if (!isLoggingRequest(req)) {
            if (log.isDebugEnabled()) {
                log.debug("Not to meet the conditional rule to enable logging. - uri: {}, headers: {}, queryParams: {}",
                        req.getRequestURI(), req.getHeaderNames(), request.getParameterMap());
            }
            chain.doFilter(request, response);
            return;
        }

        final long beginTime = currentTimeMillis();
        req.setAttribute(LoggingMessageUtil.KEY_START_TIME, beginTime);
        HttpHeaders headers = createHttpHeaders(req);

        // Determine dyeing logs level.
        int verboseLevel = determineRequestVerboseLevel(req);
        if (verboseLevel <= 0) { // is disabled?
            chain.doFilter(request, response);
            return;
        }
        String traceId = headers.getFirst(ContextInfraConstants.TRACE_REQUEST_ID_HEADER);
        String requestMethod = req.getMethod();

        doFilterInternal(req, (HttpServletResponse) response, chain, headers, traceId, requestMethod);
    }

    protected abstract void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod) throws IOException, ServletException;

    /**
     * Check if enable print logs needs to be filtered
     * 
     * @param exchange
     * @return
     */
    protected boolean isLoggingRequest(HttpServletRequest req) {
        if (!loggingConfig.isEnabled()) {
            return false;
        }
        return requestMatcher.matches(new ServletRequsetExtractor(req), loggingConfig.getPreferOpenMatchExpression());
    }

    /**
     * Determine request logs verbose level.
     * 
     * @param request
     * @return
     */
    protected int determineRequestVerboseLevel(HttpServletRequest request) {
        int verboseLevel = LoggingMessageUtil.determineRequestVerboseLevel(loggingConfig, new ServletRequsetExtractor(request));
        request.setAttribute(LoggingMessageUtil.KEY_VERBOSE_LEVEL, verboseLevel);
        return verboseLevel;
    }

    /**
     * Check if the specified flight log level range is met.
     * 
     * @param request
     * @param lower
     * @param upper
     * @return
     */
    protected boolean isLoglevelRange(HttpServletRequest request, int lower, int upper) {
        int verboseLevel = (Integer) request.getAttribute(LoggingMessageUtil.KEY_VERBOSE_LEVEL);
        verboseLevel = isNull(verboseLevel) ? 0 : verboseLevel;
        return LoggingMessageUtil.isLoglevelRange(verboseLevel, lower, upper);
    }

    @SuppressWarnings("unchecked")
    protected HttpHeaders createHttpHeaders(HttpServletRequest request) {
        List<String> headerNames = safeList(toList(request.getHeaderNames()));
        Map<String, List<String>> headers = headerNames.stream().collect(
                toCaseInsensitiveHashMap(name -> name, name -> (List<String>) toList(request.getHeaders((String) name))));
        return new HttpHeaders(new MultiValueMapAdapter<>(headers));
    }

    protected HttpHeaders createHttpHeaders(HttpServletResponse response) {
        List<String> headerNames = safeList(response.getHeaderNames());
        Map<String, List<String>> headers = headerNames.stream()
                .collect(toCaseInsensitiveHashMap(name -> name, name -> newArrayList(response.getHeaders((String) name))));
        return new HttpHeaders(new MultiValueMapAdapter<>(headers));
    }

}

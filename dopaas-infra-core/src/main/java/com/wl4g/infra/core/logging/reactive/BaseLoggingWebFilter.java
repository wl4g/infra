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
package com.wl4g.infra.core.logging.reactive;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;

import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.wl4g.infra.core.constant.CoreInfraConstants;
import com.wl4g.infra.core.logging.LoggingMessageUtil;
import com.wl4g.infra.core.logging.config.LoggingMessageProperties;
import com.wl4g.infra.core.utils.web.ReactiveRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link BaseLoggingWebFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-09-02 v3.0.0
 * @since v3.0.0
 */
@Slf4j
public abstract class BaseLoggingWebFilter implements WebFilter, Ordered {

    protected final LoggingMessageProperties loggingConfig;
    protected final Environment environment;
    protected final SpelRequestMatcher requestMatcher;

    public BaseLoggingWebFilter(LoggingMessageProperties loggingConfig, Environment environment) {
        this.loggingConfig = notNullOf(loggingConfig, "loggingConfig");
        this.environment = notNullOf(environment, "environment");
        // Build gray request matcher.
        this.requestMatcher = new SpelRequestMatcher(loggingConfig.getPreferMatchRuleDefinitions());
    }

    /**
     * @see {@link org.springframework.cloud.gateway.handler.FilteringWebHandler#loadFilters()}
     */
    @Override
    public int getOrder() {
        return 0; // TODO
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Check if filtering flight logging is enabled.
        if (!isLoggingRequest(exchange)) {
            if (log.isDebugEnabled()) {
                ServerHttpRequest request = exchange.getRequest();
                log.debug("Not to meet the conditional rule to enable logging. - uri: {}, headers: {}, queryParams: {}",
                        request.getURI(), request.getHeaders(), request.getQueryParams());
            }
            return chain.filter(exchange);
        }

        final long beginTime = currentTimeMillis();
        exchange.getAttributes().put(LoggingMessageUtil.KEY_START_TIME, beginTime);
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        // Determine dyeing logs level.
        int verboseLevel = determineRequestVerboseLevel(exchange);
        if (verboseLevel <= 0) { // is disabled?
            return chain.filter(exchange);
        }
        String traceId = headers.getFirst(CoreInfraConstants.TRACE_REQUEST_ID_HEADER);
        String requestMethod = request.getMethodValue();

        return doFilterInternal(exchange, chain, headers, traceId, requestMethod);
    }

    protected abstract Mono<Void> doFilterInternal(
            ServerWebExchange exchange,
            WebFilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod);

    /**
     * Check if enable print logs needs to be filtered
     * 
     * @param exchange
     * @return
     */
    protected boolean isLoggingRequest(ServerWebExchange exchange) {
        if (!loggingConfig.isEnabled()) {
            return false;
        }

        return requestMatcher.matches(new ReactiveRequestExtractor(exchange.getRequest()),
                loggingConfig.getPreferOpenMatchExpression());
    }

    protected int determineRequestVerboseLevel(ServerWebExchange exchange) {
        int verboseLevel = LoggingMessageUtil.determineRequestVerboseLevel(loggingConfig,
                new ReactiveRequestExtractor(exchange.getRequest()));
        exchange.getAttributes().put(LoggingMessageUtil.KEY_VERBOSE_LEVEL, verboseLevel);
        return verboseLevel;
    }

    /**
     * Check if the specified flight log level range is met.
     * 
     * @param exchange
     * @param lower
     * @param upper
     * @return
     */
    public static boolean isLoglevelRange(ServerWebExchange exchange, int lower, int upper) {
        int verboseLevel = exchange.getAttribute(LoggingMessageUtil.KEY_VERBOSE_LEVEL);
        return LoggingMessageUtil.isLoglevelRange(verboseLevel, lower, upper);
    }

}

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

import static com.wl4g.infra.context.logging.LoggingMessageUtil.isCompatibleWithPlainBody;
import static com.wl4g.infra.context.logging.LoggingMessageUtil.isDownloadStreamMedia;
import static com.wl4g.infra.context.logging.LoggingMessageUtil.isUploadStreamMedia;
import static com.wl4g.infra.context.logging.LoggingMessageUtil.readToLogString;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.springframework.http.MediaType.parseMediaType;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.wl4g.infra.common.lang.FastTimeClock;
import com.wl4g.infra.context.logging.LoggingMessageUtil;
import com.wl4g.infra.context.logging.config.LoggingMessageProperties;

import lombok.CustomLog;

/**
 * {@link LoggingMessageServletFilter}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-05-20 v3.0.0
 * @since v3.0.0
 */
@CustomLog
public class LoggingMessageServletFilter extends BaseLoggingServletFilter {

    public LoggingMessageServletFilter(LoggingMessageProperties loggingConfig, Environment environment) {
        super(loggingConfig, environment);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            HttpHeaders headers,
            String traceId,
            String requestMethod) throws IOException, ServletException {

        CachedHttpServletRequestWrapper cacheRequest = new CachedHttpServletRequestWrapper(request);
        if (cacheRequest.isCachingBodySupport()) {
            logRequest(cacheRequest, traceId);
            try {
                // Must be called, otherwise the next read from request
                // will have no data.
                cacheRequest.getInputStream().reset();
            } catch (IOException e) {
                log.warn("Could't to reset with request: {}, input stream: {}, {}", request.getClass(),
                        request.getInputStream().getClass(), e.getMessage());
            }
        }

        ContentCachingResponseWrapper cacheResponse = new ContentCachingResponseWrapper(response);
        chain.doFilter(cacheRequest, cacheResponse);

        logResponse(cacheRequest, cacheResponse, traceId);

        // Must be called, otherwise the response will have no data.
        cacheResponse.copyBodyToResponse();
    }

    protected void logRequest(HttpServletRequest request, String traceId) throws IOException {
        String requestMethod = request.getMethod();
        URI uri = URI.create(request.getRequestURI());
        String requestPath = uri.getPath();
        HttpHeaders headers = createHttpHeaders(request);

        boolean log1_2 = isLoglevelRange(request, 1, 2);
        boolean log3_10 = isLoglevelRange(request, 3, 10);
        boolean log5_10 = isLoglevelRange(request, 5, 10);
        boolean log6_10 = isLoglevelRange(request, 6, 10);
        boolean log8_10 = isLoglevelRange(request, 8, 10);

        StringBuilder requestLog = new StringBuilder(300);
        List<Object> requestLogArgs = new ArrayList<>(16);
        if (log1_2) {
            requestLog.append("{} {}");
            requestLog.append(LINE_SEPARATOR);
            requestLogArgs.add(requestMethod);
            requestLogArgs.add(requestPath);
        } else if (log3_10) {
            requestLog.append(LoggingMessageUtil.LOG_REQUEST_BEGIN);
            // Print HTTP URI. (E.g: 997ac7d2-2056-419b-883b-6969aae77e3e ::
            // GET /example/foo/bar)
            requestLog.append("{} {} :: {}");
            requestLog.append(LINE_SEPARATOR);
            requestLogArgs.add(requestMethod);
            requestLogArgs.add(requestPath.concat("?").concat(trimToEmpty(uri.getQuery())));
            requestLogArgs.add(traceId);
        }
        // Print request headers.
        if (log5_10) {
            headers.forEach((headerName, headerValue) -> {
                if (log6_10 || LoggingMessageUtil.LOG_GENERIC_HEADERS.stream().anyMatch(h -> containsIgnoreCase(h, headerName))) {
                    requestLog.append(LINE_SEPARATOR);
                    requestLog.append("{}: {}");
                    requestLogArgs.add(headerName);
                    requestLogArgs.add(headerValue.toString());
                }
            });
        }
        // When the request has no body, print the end flag directly.
        boolean processBodyIfNeed = isCompatibleWithPlainBody(headers.getContentType());
        if (!processBodyIfNeed) {
            // If it is a file upload, direct printing does not display binary.
            if (isUploadStreamMedia(headers.getContentType())) {
                processBodyIfNeed = false;
                requestLog.append(LoggingMessageUtil.LOG_REQUEST_BODY);
                requestLog.append(LoggingMessageUtil.LOG_REQUEST_END);
                requestLogArgs.add("[Request Binary Data] ...");
                log.info(requestLog.toString(), requestLogArgs.toArray());
            } else {
                requestLog.append(LoggingMessageUtil.LOG_REQUEST_END);
                log.info(requestLog.toString(), requestLogArgs.toArray());
            }
        } else {
            // Add request body.
            if (log8_10) {
                requestLog.append(LoggingMessageUtil.LOG_REQUEST_BODY);
                requestLog.append(LoggingMessageUtil.LOG_REQUEST_END);
                // Note: Only get the first small part of the data of
                // the request body, which has prevented the amount of
                // data from being too large.
                int maxLen = loggingConfig.getMaxPrintRequestBodyLength();
                requestLogArgs.add(readToLogString(request.getInputStream(), maxLen));
                log.info(requestLog.toString(), requestLogArgs.toArray());
            } else if (log3_10) {
                requestLog.append(LoggingMessageUtil.LOG_REQUEST_END);
                log.info(requestLog.toString(), requestLogArgs.toArray());
            }
        }
    }

    protected void logResponse(HttpServletRequest request, ContentCachingResponseWrapper response, String traceId)
            throws IOException {
        MediaType contentType = nonNull(response.getContentType()) ? parseMediaType(response.getContentType()) : null;
        String requestMethod = request.getMethod();
        URI uri = URI.create(request.getRequestURI());
        String requestUri = uri.getPath();

        boolean log1_2 = isLoglevelRange(request, 1, 2);
        boolean log3_10 = isLoglevelRange(request, 3, 10);
        boolean log6_10 = isLoglevelRange(request, 6, 10);
        boolean log8_10 = isLoglevelRange(request, 8, 10);
        boolean log9_10 = isLoglevelRange(request, 9, 10);

        Long startTime = (Long) request.getAttribute(LoggingMessageUtil.KEY_START_TIME);
        startTime = nonNull(startTime) ? startTime : 0;
        long costTime = nonNull(startTime) ? (FastTimeClock.currentTimeMillis() - startTime) : 0L;

        StringBuilder responseLog = new StringBuilder(300);
        List<Object> responseLogArgs = new ArrayList<>(16);
        if (log1_2) {
            responseLog.append("{} {} {} {}\n");
            responseLogArgs.add(response.getStatus());
            responseLogArgs.add(requestMethod);
            responseLogArgs.add(requestUri);
            responseLogArgs.add(costTime + "ms");
        } else if (log3_10) {
            responseLog.append(LoggingMessageUtil.LOG_RESPONSE_BEGIN);
            // Print HTTP URI. (E.g:
            // 997ac7d2-2056-419b-883b-6969aae77e3e ::
            // 200 GET /example/foo/bar)
            responseLog.append("{} {} {} :: {} {}\n");
            responseLogArgs.add(response.getStatus());
            responseLogArgs.add(requestMethod);
            responseLogArgs.add(requestUri.concat("?").concat(trimToEmpty(uri.getQuery())));
            responseLogArgs.add(traceId);
            responseLogArgs.add(costTime + "ms");
        }

        // If it is a file download, direct printing does not display
        // binary.
        if (isDownloadStreamMedia(contentType)) {
            responseLog.append(LoggingMessageUtil.LOG_RESPONSE_BODY);
            responseLogArgs.add("[Response Binary Data] ...");
        } else {
            // When the response has no body, print the end flag
            // directly.
            boolean processBodyIfNeed = log9_10 && isCompatibleWithPlainBody(contentType);
            // Print response body.
            if (processBodyIfNeed) {
                // Full print response body.
                responseLog.append(LoggingMessageUtil.LOG_RESPONSE_BODY);
                // Note: Only get the first small part of the data of
                // the response body, which has prevented the amount of
                // data from being too large.
                int maxLen = loggingConfig.getMaxPrintResponseBodyLength();
                responseLogArgs.add(readToLogString(response.getContentInputStream(), maxLen));
            }
        }

        // If there is a response body, the response header has been added
        // before, no need to add.
        if (log6_10) {
            HttpHeaders headers = createHttpHeaders(response);
            headers.forEach((headerName, headerValue) -> {
                if (log8_10 || LoggingMessageUtil.LOG_GENERIC_HEADERS.stream().anyMatch(h -> containsIgnoreCase(h, headerName))) {
                    responseLog.append(LINE_SEPARATOR + "{}: {}");
                    responseLogArgs.add(headerName);
                    responseLogArgs.add(headerValue.toString());
                }
            });
        }
        if (log3_10) {
            responseLog.append(LoggingMessageUtil.LOG_RESPONSE_END);
            log.info(responseLog.toString(), responseLogArgs.toArray());
        }
    }

}

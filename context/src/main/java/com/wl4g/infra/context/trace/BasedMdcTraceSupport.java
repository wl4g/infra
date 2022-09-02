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
package com.wl4g.infra.context.trace;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.context.constant.ContextInfraConstants.TRACE_REQUEST_ID_HEADER;
import static com.wl4g.infra.context.constant.ContextInfraConstants.TRACE_REQUEST_SEQ_HEADER;
import static com.wl4g.infra.context.trace.BasedMdcTraceSupport.MDCKey.KEY_NEXT_REQUEST_SEQ;
import static com.wl4g.infra.context.trace.BasedMdcTraceSupport.MDCKey.KEY_PREFIX_COOKIE;
import static com.wl4g.infra.context.trace.BasedMdcTraceSupport.MDCKey.KEY_PREFIX_HEADER;
import static com.wl4g.infra.context.trace.BasedMdcTraceSupport.MDCKey.KEY_PREFIX_PARAMETER;
import static com.wl4g.infra.context.trace.BasedMdcTraceSupport.MDCKey.KEY_PRINCIPAL;
import static com.wl4g.infra.context.trace.BasedMdcTraceSupport.MDCKey.KEY_REQUEST_ID;
import static com.wl4g.infra.context.trace.BasedMdcTraceSupport.MDCKey.KEY_REQUEST_SEQ;
import static com.wl4g.infra.context.trace.BasedMdcTraceSupport.MDCKey.KEY_URI;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.security.Principal;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.MDC;
import org.springframework.core.env.Environment;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;
import com.wl4g.infra.context.constant.ContextInfraConstants;

/**
 * Abstract the MDC parameter option to the logback log output. Note that this
 * filter should be placed before other filters as much as possible. By default,
 * for example, "requestid", "requestseq", "timestamp", "uri" will be added to
 * the MDC context.</br>
 * </br>
 * 1) Among them, requestid and requestseq are used for call chain tracking, and
 * developers usually do not need to modify them manually.</br>
 * </br>
 * 2) Timestamp is the time stamp when the request starts to be processed by the
 * servlet. It is designed to be the start time when the filter executes. This
 * value can be used to determine the efficiency of internal program
 * execution.</br>
 * </br>
 * 3) Uri is the URI value of the current request.</br>
 * 
 * Use: We can use the variables in MDC through %X{key} in the layout section of
 * logback.xml, for example: vim application.yml
 * 
 * <pre>
 * %d{yyyy-MM-dd HH:mm:ss.SSS} %5p ${PID:- } <font color=
red>[%-32.32X{H:X-Request-ID}] [%-16.16X{H:X-Request-Seq}]</font> - %-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}'
 * </pre>
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-02-26
 * @since
 */
public abstract class BasedMdcTraceSupport {

    protected SmartLogger log = getLogger(getClass());

    protected final Environment environment;

    /**
     * Cache refresh time-stamp.
     */
    protected final AtomicLong lastReloadTime = new AtomicLong(0);

    /**
     * Whether to enable the headers mapping. for example:
     * <b>%X{_C_:JSESSIONID}</b></br>
     */
    protected AtomicBoolean bindCookies = new AtomicBoolean(false);

    /**
     * Whether to enable the headers mapping. for example:
     * <b>%X{_H_:X-Forwarded-For}</b></br>
     */
    protected AtomicBoolean bindHeaders = new AtomicBoolean(false);

    /**
     * Whether to enable the headers mapping. for example:
     * <b>%X{_P_:userId}</b></br>
     */
    protected AtomicBoolean bindParameters = new AtomicBoolean(false);

    public BasedMdcTraceSupport(Environment environment) {
        this.environment = notNullOf(environment, "environment");
    }

    protected void bindToMDC(WebRequestExtractor extractor) {
        try {
            reloadIfNecessary();
            doBind(extractor);
        } catch (Exception e) {
            log.error(format("Could't set logging MDC. uri: %s", extractor.getRequestURI()), e);
        }
    }

    /**
     * Bind trace attributes to logging MDC
     * 
     * @param extractor
     */
    protected void doBind(WebRequestExtractor extractor) {
        // Bind trace required fields.
        MDC.put(KEY_URI, extractor.getRequestURI().getPath());
        Principal principal = extractor.getPrincipal();
        if (nonNull(principal)) {
            MDC.put(KEY_PRINCIPAL, principal.getName());
        }
        MDC.put(KEY_REQUEST_ID, extractor.getHeaderValue(TRACE_REQUEST_ID_HEADER));
        String requestSeq = extractor.getHeaderValue(TRACE_REQUEST_SEQ_HEADER);
        MDC.put(KEY_REQUEST_SEQ, requestSeq);
        if (isBlank(requestSeq)) {
            MDC.put(KEY_NEXT_REQUEST_SEQ, "0");
        } else {
            // Sequence will be like:000, real sequence is the number of "0"
            String nextSeq = requestSeq.concat("0");
            MDC.put(KEY_NEXT_REQUEST_SEQ, nextSeq);
        }

        // Bind extra headers,
        if (bindHeaders.get()) {
            Collection<String> headerNames = extractor.getHeaderNames();
            if (!isEmpty(headerNames)) {
                for (String name : headerNames) {
                    if (isMDCField(name)) {
                        MDC.put(KEY_PREFIX_HEADER.concat(name), extractor.getHeaderValue(name));
                    }
                }
            }
        }

        // Bind extra parameters.
        if (bindParameters.get()) {
            Collection<String> parameterNames = extractor.getQueryNames();
            if (!isEmpty(parameterNames)) {
                for (String name : parameterNames) {
                    if (isMDCField(name)) {
                        MDC.put(KEY_PREFIX_PARAMETER.concat(name), extractor.getQueryValue(name));
                    }
                }
            }
        }

        // Bind extra cookies.
        if (bindCookies.get()) {
            Collection<String> cookieNames = extractor.getCookieNames();
            for (String name : cookieNames) {
                if (isMDCField(name)) {
                    MDC.put(KEY_PREFIX_COOKIE.concat(name), extractor.getCookieValue(name));
                }
            }
        }
    }

    /**
     * Reload MDC mapped via patterns, When the logging configuration is
     * modified, it can be updated in time.
     * 
     * @return
     */
    protected boolean reloadIfNecessary() {
        long now = currentTimeMillis();
        if ((now - lastReloadTime.get()) < DEFAULT_CACHE_REFRESH_MS) {
            return false;
        }
        String consolePattern = environment.getProperty("logging.pattern.console");
        String filePattern = environment.getProperty("logging.pattern.file");
        this.bindCookies.set(hasMDCField(consolePattern, filePattern, KEY_PREFIX_COOKIE));
        this.bindHeaders.set(hasMDCField(consolePattern, filePattern, KEY_PREFIX_HEADER));
        this.bindParameters.set(hasMDCField(consolePattern, filePattern, KEY_PREFIX_PARAMETER));
        this.lastReloadTime.set(now);
        return true;
    }

    /**
     * Check if bind to MDC header is required.
     * 
     * @param name
     * @return
     */
    protected boolean isMDCField(String name) {
        return startsWithIgnoreCase(name, "X-");
    }

    /**
     * Check if the extended MDC field is used in the log pattern.
     * 
     * @param consolePattern
     * @param filePattern
     * @param mdcField
     * @return
     */
    protected boolean hasMDCField(String consolePattern, String filePattern, String mdcField) {
        // e.g: %X{_C_:JSESSIONID} => %X{_C_:
        String key = "%X{".concat(mdcField);
        return contains(consolePattern, key) || contains(filePattern, key);
    }

    static class MDCKey {

        public static final String KEY_REQUEST_ID = ContextInfraConstants.getStringProperty("TRACE_MDC_REQUESTID", "requestId");

        public static final String KEY_REQUEST_SEQ = ContextInfraConstants.getStringProperty("TRACE_MDC_REQUESTSEQ", "requestSeq");

        /**
         * When the tracking chain is distributed, the used SEQ is generated by
         * filter, and usually the developer does not need to modify it.
         */
        public static final String KEY_NEXT_REQUEST_SEQ = ContextInfraConstants.getStringProperty("TRACE_MDC_NEXT_REQUESTSEQ",
                "nextRequestSeq");

        public static final String KEY_URI = "uri";

        public static final String KEY_PRINCIPAL = "principal";

        public static final String KEY_PREFIX_HEADER = "H:";

        public static final String KEY_PREFIX_PARAMETER = "P:";

        public static final String KEY_PREFIX_COOKIE = "C:";
    }

    public static final long DEFAULT_CACHE_REFRESH_MS = 2_000L;

}
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
package com.wl4g.infra.context.trace.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.core.env.Environment;

import com.wl4g.infra.context.trace.BasedMdcTraceSupport;
import com.wl4g.infra.context.utils.web.ServletRequsetExtractor;

/**
 * Add the MDC parameter option to the logback log output. Note that this filter
 * should be placed before other filters as much as possible. By default, for
 * example, "requestid", "requestseq", "timestamp", "uri" will be added to the
 * MDC context.</br>
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
 * logging:
 *   pattern:
 *     console: ${logging.pattern.file}
 *     #file: '%d{yyyy-MM-dd HH:mm:ss.SSS} ${LOG_LEVEL_PATTERN:-%5p} ${PID} --- [%t] %-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}'
 *     file: '%d{yy-MM-dd HH:mm:ss.SSS} ${LOG_LEVEL_PATTERN:%4p} ${PID} [%t] <font color
=
red>[%X{_H_:X-Request-Id}] [%X{_H_:X-Request-Seq}] [%X{_C_:${spring.iam.client.cookie.name}}]</font> - %-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}'
 * </pre>
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年2月26日
 * @since
 */
public class SimpleTraceMDCServletFilter extends BasedMdcTraceSupport implements Filter {

    public SimpleTraceMDCServletFilter(Environment environment) {
        super(environment);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            bindToMDC(new ServletRequsetExtractor((HttpServletRequest) request));
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // must
        }
    }

    @Override
    public void destroy() {
    }

}
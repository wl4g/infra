/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.infra.integration.feign.istio.context;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.integration.feign.core.config.FeignConsumerProperties;
import com.wl4g.infra.integration.feign.core.context.internal.FeignContextCoprocessor;

import feign.RequestTemplate;

/**
 * {@link TracingContextCoprocessor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-04-27
 * @sine v1.0
 * @see
 */
public class TracingContextCoprocessor implements FeignContextCoprocessor {

    private FeignConsumerProperties config;

    public TracingContextCoprocessor(FeignConsumerProperties config) {
        this.config = notNullOf(config, "config");
    }

    // Notice:https://blog.csdn.net/caoyi1207/article/details/92775211
    @Override
    public void prepareConsumerExecution(@NotNull RequestTemplate template, HttpServletRequest request) {
        // More(eg:Jaeger+Istio) tracing parameter through to the next service.
        Enumeration<String> e = request.getHeaderNames();
        if (e != null) {
            while (e.hasMoreElements()) {
                String name = e.nextElement();
                if (isTracingHeaders(name)) {
                    template.header(name, request.getHeader(name));
                }
            }
        }
    }

    private boolean isTracingHeaders(String name) {
        for (String prefix : safeList(config.getTracing().getPrefixHttpHeaders())) {
            if (startsWithIgnoreCase(name, prefix)) {
                return true;
            }
        }
        return false;
    }

}

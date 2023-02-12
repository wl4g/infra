/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <wanglsir@gmail.com>
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
package com.wl4g.infra.integration.feign.core.metrics;

import java.lang.reflect.Method;

import com.wl4g.infra.context.utils.AopUtils2;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link FeignMetricsUtil}
 * 
 * @author &lt;James Wong <jameswong1376@gmail.com>&gt;
 * @version 2022-07-12
 * @since v3.0.0
 */
public abstract class FeignMetricsUtil {

    public static String[] getDefaultMetricsTags(Object target, Method method, Object[] args) {
        Object obj = AopUtils2.getTarget(target);
        String service = obj.getClass().getSimpleName();
        return new String[] { MetricsTag.SERVICE, service, MetricsTag.METHOD, method.getName() };
    }

    @Getter
    @AllArgsConstructor
    public static enum MetricsName {

        consumer_total("consumer_total", "The stats of consumer total"),

        consumer_success("consumer_success", "The stats of consumer decode success"),

        consumer_failure("consumer_failure", "The stats of consumer decode failure"),

        consumer_cost("consumer_cost", "The stats of consumer call cost"),

        provider_total("provider_total", "The stats of provider total"),

        provider_success("provider_success", "The stats of provider decode success"),

        provider_failure("provider_failure", "The stats of provider decode failure"),

        provider_cost("provider_cost", "The stats of provider call cost");

        private final String name;
        private final String help;
    }

    public static abstract class MetricsTag {
        public static final String SERVICE = "service";
        public static final String METHOD = "method";
    }

}

/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * James Wong <jameswong1376@gmail.com> Technology CO.LTD.
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
 * Reference to website: https://wl4g.github.io
 */
package com.wl4g.infra.integration.feign.core.context.internal;

import org.springframework.context.annotation.Bean;

import com.wl4g.infra.metrics.MetricsFacade;

/***
 * Feign context auto configuration.</br>
 * (consumer/client|provider/server)
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2020-12-07
 * @since v2.0
 * @see
 */
public class FeignContextAutoConfiguration {

    @Bean
    public ConsumerFeignContextFilter consumerFeignContextFilter() {
        return new ConsumerFeignContextFilter();
    }

    @Bean
    public ProviderFeignContextFilter providerFeignContextFilter(MetricsFacade metricsFacade) {
        return new ProviderFeignContextFilter(metricsFacade);
    }

}

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
package com.wl4g.infra.metrics;

import org.springframework.core.env.Environment;

import com.wl4g.infra.common.metrics.PrometheusMeterFacade;
import com.wl4g.infra.common.net.InetUtils;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.Getter;

/**
 * {@link PrometheusMeterFacade}, Tends to use Prometheus standards.
 * 
 * Counter, Timer, Gauge, DistributionSummary, etc.
 * 
 * {@link PrometheusMeterRegistry}
 * 
 * @author &lt;James Wong James Wong <jameswong1376@gmail.com>&gt;
 * @version 2021-11-16 v1.0.0
 * @since v1.0.0
 */
@Getter
public class MetricsFacade extends PrometheusMeterFacade {

    public MetricsFacade(Environment env, PrometheusMeterRegistry registry, InetUtils inet) {
        super(registry, env.getRequiredProperty("spring.application.name"),
                env.getProperty("server.ssl.enabled", Boolean.class, false), inet,
                env.getRequiredProperty("server.port", Integer.class));
    }

}

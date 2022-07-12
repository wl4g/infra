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
package com.wl4g.infra.metrics.client.health.timed;

import static com.wl4g.infra.metrics.client.constants.MetricsInfraConstants.CONF_PREFIX_INFRA_HEALTH_TIMEED;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.CustomLog;

/**
 * {@link TimedMethodAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-11-30 v1.0.0
 * @since v1.0.0
 */
@CustomLog
@Configuration
@ConditionalOnProperty(name = CONF_PREFIX_INFRA_HEALTH_TIMEED + ".enabled", matchIfMissing = false)
public class TimedMethodAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_HEALTH_TIMEED)
    public TimedMethodProperties timedMethodProperties() {
        return new TimedMethodProperties();
    }

    @Bean
    public HealthIndicator timeoutMethodHealthIndicator(TimedMethodProperties config) {
        log.info("Initializing timingMethodsHealthIndicator. - {}", config);
        if (config.getSamples() == 0) {
            throw new IllegalArgumentException("Latest measure count is 0.");
        }
        return new TimedMethodHealthIndicator(config);
    }

}

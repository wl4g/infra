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
package com.wl4g.infra.core.logging.trace.reactive;

import static com.wl4g.infra.core.constant.CoreInfraConstants.CONF_PREFIX_INFRA_CORE_LOGGING_TRACE;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

/**
 * Reactive logging MDC auto configuration.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-11 v3.0.0
 * @since v3.0.0
 */
@Deprecated // use:https://github.com/spring-projects-experimental/spring-cloud-sleuth-otel
@Order(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnProperty(name = CONF_PREFIX_INFRA_CORE_LOGGING_TRACE + ".enabled", matchIfMissing = false)
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class SimpleReactiveTraceAutoConfiguration {

    @Bean
    public SimpleTraceMDCWebFilter simpleTraceMDCWebFilter(Environment environment) {
        return new SimpleTraceMDCWebFilter(environment);
    }

}

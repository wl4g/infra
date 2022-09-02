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
package com.wl4g.infra.context.trace.reactive;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.wl4g.infra.context.constant.ContextInfraConstants;
import com.wl4g.infra.context.trace.BasedMdcTraceSupport;
import com.wl4g.infra.context.utils.web.ReactiveRequestExtractor;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

/**
 * {@link SimpleTraceWebFilter}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-05-11 v3.0.0
 * @since v3.0.0
 */
public class SimpleTraceWebFilter extends BasedMdcTraceSupport implements WebFilter, Ordered {

    public SimpleTraceWebFilter(Environment environment) {
        super(environment);
    }

    @Override
    public int getOrder() {
        return ContextInfraConstants.TRACE_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        bindToMDC(new ReactiveRequestExtractor(exchange.getRequest()));
        // see:https://stackoverflow.com/questions/61409047/how-does-spring-cloud-sleuth-propagate-the-mdc-context-in-webflux-ouf-of-the-box\
        // see:https://simonbasle.github.io/2018/02/contextual-logging-with-reactor-context-and-mdc/
        return chain.filter(exchange).doOnEach(logOnNext(r -> {
            log.debug("found restaurant {} for ${}");
        })).doOnTerminate(() -> System.out.println()/* MDC.clear() */);
    }

    private static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
        return signal -> {
            // if (!signal.isOnNext()) {
            // return;
            // }
            Optional<String> traceIdOp = signal.getContextView().getOrEmpty("requestId");
            traceIdOp.ifPresent(requestId -> {
                try (MDC.MDCCloseable closeable = MDC.putCloseable("requestId", requestId)) {
                    logStatement.accept(signal.get());
                }
            });
        };
    }

}

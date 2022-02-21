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
package com.wl4g.component.integration.feign.context.sentinel;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.wl4g.component.common.log.SmartLogger;

import reactor.core.publisher.Mono;

/**
 * {@link WebFluxRequestContextConfigurer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-18
 * @sine v1.0
 * @see
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class WebFluxRequestContextConfigurer implements WebFluxConfigurer {
	protected final SmartLogger log = getLogger(getClass());

	@Bean
	public ReactiveRequestContextFilter reactiveRequestContextFilter() {
		return new ReactiveRequestContextFilter();
	}

	/**
	 * Reactive request context parameters extractor.
	 */
	class ReactiveRequestContextFilter implements WebFilter {
		private final PathPattern pathPattern;

		public ReactiveRequestContextFilter() {
			this.pathPattern = new PathPatternParser().parse("/**");
		}

		@Override
		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
			final ServerHttpRequest request = exchange.getRequest();

			if (pathPattern.matches(request.getPath().pathWithinApplication())) {
				// TODO
				// ...
			}

			return chain.filter(exchange);
		}
	}

}

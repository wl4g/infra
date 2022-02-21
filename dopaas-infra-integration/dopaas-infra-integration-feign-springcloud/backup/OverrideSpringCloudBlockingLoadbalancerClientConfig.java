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
package com.wl4g.component.integration.springcloud.feign.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.OnNoRibbonDefaultCondition;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * {@link OverrideSpringCloudBlockingLoadbalancerClientConfig}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-18
 * @sine v1.0
 * @see
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class OverrideSpringCloudBlockingLoadbalancerClientConfig {

	// [FIXED] Override default BlockingLoadBalancerClient
	/**
	 * {@link org.springframework.cloud.loadbalancer.config.BlockingLoadBalancerClientAutoConfiguration.BlockingLoadbalancerClientConfig#blockingLoadBalancerClient}
	 */
	@Bean
	@ConditionalOnBean(LoadBalancerClientFactory.class)
	@ConditionalOnClass(RestTemplate.class)
	@Conditional(OnNoRibbonDefaultCondition.class)
	@Primary
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public BlockingLoadBalancerClient blockingLoadBalancerClient(LoadBalancerClientFactory loadBalancerClientFactory) {
		return new BlockingLoadBalancerClient(loadBalancerClientFactory) {
			private final String PERCENTAGE_SIGN = "%";
			private final String DEFAULT_SCHEME = "http";
			private final String DEFAULT_SECURE_SCHEME = "https";
			private final Map<String, String> INSECURE_SCHEME_MAPPINGS = new HashMap<String, String>() {
				private static final long serialVersionUID = 1l;
				{
					put(DEFAULT_SCHEME, DEFAULT_SECURE_SCHEME);
					put("ws", "wss");
				}
			};

			@Override
			public URI reconstructURI(ServiceInstance serviceInstance, URI original) {
				if (serviceInstance == null) {
					throw new IllegalArgumentException("Service Instance cannot be null.");
				}

				String host = serviceInstance.getHost();
				String scheme = Optional.ofNullable(serviceInstance.getScheme()).orElse(computeScheme(original, serviceInstance));
				int port = computePort(serviceInstance.getPort(), scheme);

				if (Objects.equals(host, original.getHost()) && port == original.getPort()
						&& Objects.equals(scheme, original.getScheme())) {
					return original;
				}

				boolean encoded = containsEncodedParts(original);
				// [FIXED] use provider server.servlet.context-path
				String contextPath = serviceInstance.getMetadata().get("contextPath");
				return UriComponentsBuilder.fromUri(original).scheme(scheme).host(host).port(port).path(contextPath)
						.build(encoded).toUri();
			}

			private int computePort(int port, String scheme) {
				if (port >= 0) {
					return port;
				}
				if (Objects.equals(scheme, DEFAULT_SECURE_SCHEME)) {
					return 443;
				}
				return 80;
			}

			private String computeScheme(URI original, ServiceInstance serviceInstance) {
				String originalOrDefault = Optional.ofNullable(original.getScheme()).orElse(DEFAULT_SCHEME);
				if (serviceInstance.isSecure() && INSECURE_SCHEME_MAPPINGS.containsKey(originalOrDefault)) {
					return INSECURE_SCHEME_MAPPINGS.get(originalOrDefault);
				}
				return originalOrDefault;
			}

			// see original
			// https://github.com/spring-cloud/spring-cloud-gateway/blob/master/spring-cloud-gateway-core/
			// src/main/java/org/springframework/cloud/gateway/support/ServerWebExchangeUtils.java
			private boolean containsEncodedParts(URI uri) {
				boolean encoded = (uri.getRawQuery() != null && uri.getRawQuery().contains(PERCENTAGE_SIGN))
						|| (uri.getRawPath() != null && uri.getRawPath().contains(PERCENTAGE_SIGN))
						|| (uri.getRawFragment() != null && uri.getRawFragment().contains(PERCENTAGE_SIGN));
				// Verify if it is really fully encoded. Treat partial encoded
				// as unencoded.
				if (encoded) {
					try {
						UriComponentsBuilder.fromUri(uri).build(true);
						return true;
					} catch (IllegalArgumentException ignore) {
					}
					return false;
				}
				return false;
			}
		};
	}

}

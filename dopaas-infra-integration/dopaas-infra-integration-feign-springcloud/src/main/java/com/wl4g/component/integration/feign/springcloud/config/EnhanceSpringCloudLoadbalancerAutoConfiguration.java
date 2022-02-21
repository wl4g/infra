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
package com.wl4g.component.integration.feign.springcloud.config;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.component.common.reflect.ReflectionUtils2.setField;
import static com.wl4g.component.integration.feign.springcloud.constant.SpringCloudFeignConstant.*;
import static java.util.concurrent.ThreadLocalRandom.current;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientSpecification;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheManager;
import org.springframework.cloud.loadbalancer.core.CachingServiceInstanceSupplier;
import org.springframework.cloud.loadbalancer.core.DiscoveryClientServiceInstanceSupplier;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.utils.expression.SpelExpressions;

import reactor.core.publisher.Mono;

/**
 * {@link EnhanceSpringCloudLoadbalancerAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-14
 * @sine v1.0
 * @see {@link org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration}
 */
@SuppressWarnings("deprecation")
@LoadBalancerClients
@ConditionalOnDiscoveryEnabled
public class EnhanceSpringCloudLoadbalancerAutoConfiguration {

	// [FIXED] Override default LoadBalancerClientFactory.
	/**
	 * {@link org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration#loadBalancerClientFactory}
	 */
	@Bean
	@Primary
	@ConditionalOnMissingBean
	public LoadBalancerClientFactory extensionLoadBalancerClientFactory(
			ObjectProvider<List<LoadBalancerClientSpecification>> configurations) {
		LoadBalancerClientFactory clientFactory = new ExtensionLoadBalancerClientFactory();
		clientFactory.setConfigurations(configurations.getIfAvailable(Collections::emptyList));
		return clientFactory;
	}

	/**
	 * {@link org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient#choose}
	 */
	@Bean
	@ConditionalOnProperty(name = KEY_LOADBALANCER_RANDOM + ".enabled", matchIfMissing = false)
	@ConditionalOnMissingClass("org.springframework.cloud.loadbalancer.core.RandomLoadBalancer") // spring-cloud-loadbalancer-3.0.0.jar
	public RandomLoadBalancer randomReactorServiceInstanceLoadBalancer(Environment environment,
			LoadBalancerClientFactory loadBalancerClientFactory) {
		String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
		return new RandomLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
	}

	/**
	 * {@link org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient#choose}
	 */
	@Bean
	@ConditionalOnMissingBean({ RandomLoadBalancer.class })
	@ConditionalOnProperty(name = KEY_LOADBALANCER_GRAY + ".enabled", matchIfMissing = false)
	public GrayLoadBalancer grayReactorServiceInstanceLoadBalancer(Environment environment,
			LoadBalancerClientFactory loadBalancerClientFactory) {
		String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
		String chooseExpression = environment.getProperty(KEY_LOADBALANCER_GRAY + ".chooseExpression", "");
		chooseExpression = isBlank(chooseExpression) ? "#{true}" : chooseExpression; // by-default
		return new GrayLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name,
				chooseExpression);
	}

	/**
	 * {@link org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration#reactorServiceInstanceLoadBalancer}
	 */
	@Bean
	@ConditionalOnMissingBean({ GrayLoadBalancer.class, RandomLoadBalancer.class })
	public RoundRobinLoadBalancer reactorServiceInstanceLoadBalancer(Environment environment,
			LoadBalancerClientFactory loadBalancerClientFactory) {
		String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
		return new RoundRobinLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
				name);
	}

	public static class ExtensionLoadBalancerClientFactory extends LoadBalancerClientFactory {
		public ExtensionLoadBalancerClientFactory() {
			super();
			// [FIXED] Override default config type.
			Field defaultConfigTypeField = findField(getClass(), "defaultConfigType", Class.class);
			setField(defaultConfigTypeField, this, EnhanceSpringCloudLoadbalancerAutoConfiguration.class, true);
		}
	}

	/**
	 * Random load balancer.
	 * 
	 * @see {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer.RoundRobinLoadBalancer}
	 */
	public static class RandomLoadBalancer implements ReactorServiceInstanceLoadBalancer {
		protected final SmartLogger log = getLogger(getClass());
		protected final Random random = new Random();

		private final String serviceId;
		private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

		/**
		 * @param serviceInstanceListSupplierProvider
		 *            a provider of {@link ServiceInstanceListSupplier} that
		 *            will be used to get available instances
		 * @param serviceId
		 *            id of the service for which to choose an instance
		 * @param seedPosition
		 *            Round Robin element position marker
		 */
		public RandomLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
				String serviceId) {
			this.serviceId = serviceId;
			this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
		}

		@SuppressWarnings({ "rawtypes" })
		@Override
		public Mono<Response<ServiceInstance>> choose(Request request) {
			if (serviceInstanceListSupplierProvider != null) {
				ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
						.getIfAvailable(NoopServiceInstanceListSupplier::new);
				return supplier.get().next().map(this::getInstanceResponse);
			}
			return Mono.empty();
		}

		private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
			if (instances.isEmpty()) {
				log.warn("No servers available for service: {}", this.serviceId);
				return new EmptyResponse();
			}
			ServiceInstance instance = instances.get(current().nextInt(0, instances.size()));
			return new DefaultResponse(instance);
		}
	}

	/**
	 * Gray load balancer.
	 */
	public static class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {
		protected final SmartLogger log = getLogger(getClass());

		private final String serviceId;
		private final String chooseExpression;
		private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

		public GrayLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId,
				String chooseExpression) {
			this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
			this.serviceId = serviceId;
			this.chooseExpression = chooseExpression;
		}

		@SuppressWarnings({ "rawtypes" })
		@Override
		public Mono<Response<ServiceInstance>> choose(Request request) {
			if (serviceInstanceListSupplierProvider != null) {
				ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
						.getIfAvailable(NoopServiceInstanceListSupplier::new);
				return supplier.get().next().map(serviceInstances -> getInstanceResponse(serviceInstances, request));
			}
			return Mono.empty();
		}

		@SuppressWarnings("rawtypes")
		private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {
			if (instances.isEmpty()) {
				log.warn("No servers available for service: {}", this.serviceId);
				return new EmptyResponse();
			}

			// TODO
			request.getContext();// get http request is null???
			for (ServiceInstance instance : instances) {
				Map<String, Object> model = new HashMap<>();
				model.put("request", null); // TODO
				model.put("instance", instance);
				if ((Boolean) defaultSpel.resolve(chooseExpression, model)) {
					return new DefaultResponse(instance);
				}
			}
			return new EmptyResponse();
		}
	}

	@ConditionalOnReactiveDiscoveryEnabled
	@Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER)
	public static class ReactiveSupportConfiguration {

		@Bean
		@ConditionalOnBean(ReactiveDiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "default", matchIfMissing = true)
		public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withDiscoveryClient().withCaching().build(context);
		}

		@Bean
		@ConditionalOnBean(ReactiveDiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "zone-preference")
		public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(
				ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withDiscoveryClient().withZonePreference().withCaching().build(context);
		}

		@Bean
		@ConditionalOnBean(ReactiveDiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "health-check")
		public ServiceInstanceListSupplier healthCheckDiscoveryClientServiceInstanceListSupplier(
				ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withDiscoveryClient().withHealthChecks().withCaching().build(context);
		}

		@Bean
		@ConditionalOnBean(ReactiveDiscoveryClient.class)
		@ConditionalOnMissingBean
		public ServiceInstanceSupplier discoveryClientServiceInstanceSupplier(ReactiveDiscoveryClient discoveryClient,
				Environment env, ApplicationContext context) {
			DiscoveryClientServiceInstanceSupplier delegate = new DiscoveryClientServiceInstanceSupplier(discoveryClient, env);
			ObjectProvider<LoadBalancerCacheManager> cacheManagerProvider = context
					.getBeanProvider(LoadBalancerCacheManager.class);
			if (cacheManagerProvider.getIfAvailable() != null) {
				return new CachingServiceInstanceSupplier(delegate, cacheManagerProvider.getIfAvailable());
			}
			return delegate;
		}

	}

	@ConditionalOnBlockingDiscoveryEnabled
	@Order(REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER + 1)
	public static class BlockingSupportConfiguration {

		@Bean
		@ConditionalOnBean(DiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "default", matchIfMissing = true)
		public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient().withCaching().build(context);
		}

		@Bean
		@ConditionalOnBean(DiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "zone-preference")
		public ServiceInstanceListSupplier zonePreferenceDiscoveryClientServiceInstanceListSupplier(
				ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient().withZonePreference().withCaching()
					.build(context);
		}

		@Bean
		@ConditionalOnBean(DiscoveryClient.class)
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.loadbalancer.configurations", havingValue = "health-check")
		public ServiceInstanceListSupplier healthCheckDiscoveryClientServiceInstanceListSupplier(
				ConfigurableApplicationContext context) {
			return ServiceInstanceListSupplier.builder().withBlockingDiscoveryClient().withHealthChecks().withCaching()
					.build(context);
		}

		@Bean
		@ConditionalOnBean(DiscoveryClient.class)
		@ConditionalOnMissingBean
		public ServiceInstanceSupplier discoveryClientServiceInstanceSupplier(DiscoveryClient discoveryClient, Environment env,
				ApplicationContext context) {
			DiscoveryClientServiceInstanceSupplier delegate = new DiscoveryClientServiceInstanceSupplier(discoveryClient, env);
			ObjectProvider<LoadBalancerCacheManager> cacheManagerProvider = context
					.getBeanProvider(LoadBalancerCacheManager.class);
			if (cacheManagerProvider.getIfAvailable() != null) {
				return new CachingServiceInstanceSupplier(delegate, cacheManagerProvider.getIfAvailable());
			}
			return delegate;
		}

	}

	private static final int REACTIVE_SERVICE_INSTANCE_SUPPLIER_ORDER = 193827465;
	private static final SpelExpressions defaultSpel = SpelExpressions.create();

}

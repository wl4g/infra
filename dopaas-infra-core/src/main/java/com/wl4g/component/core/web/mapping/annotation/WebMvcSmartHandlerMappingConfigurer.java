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
package com.wl4g.component.core.web.mapping.annotation;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.lang.ClassUtils2.getPackageName;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.startsWithAny;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.INSTANCE;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.base.Predicate;
import com.wl4g.component.common.log.SmartLogger;

/**
 * Global delegate Web MVC {@link RequestMapping} unique handler mapping.
 * instances. (supports multi customization
 * {@link RequestMappingHandlerMapping}) </br>
 * </br>
 * Notes: The usage scenarios and instructions of this global delegation request
 * mapping registration program are as follows: </br>
 * The purpose {@link ServletHandlerMappingSupport} is to use custom global
 * delegation to uniformly register the mapping. Because when an interface
 * annotated with {@link RequestMapping} has multiple subclass instances, spring
 * will automatically register the mapping of two subclass instances by default,
 * which will lead to the exception of registration conflict. For related source
 * code analysis, please refer to:
 * {@link AbstractHandlerMethodMapping#isHandler()}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see {@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration#createRequestMappingHandlerMapping()}
 * @see http://www.kailaisii.com/archives/%E8%87%AA%E5%AE%9A%E4%B9%89RequestMappingHandlerMapping,%E5%86%8D%E4%B9%9F%E4%B8%8D%E7%94%A8%E5%86%99url%E4%BA%86~
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class WebMvcSmartHandlerMappingConfigurer implements WebMvcRegistrations {
	protected final SmartLogger log = getLogger(getClass());

	@Nullable
	private String[] packagePatterns;
	private boolean packagePatternsUseForInclude;
	@Nullable
	private Predicate<Class<?>>[] filters;
	private boolean overrideAmbiguousByOrder;

	@Lazy // Resolving cyclic dependency injection
	@Nullable
	@Autowired(required = false)
	private List<ServletHandlerMappingSupport> handlerMappings;

	public void setPackagePatterns(String[] packagePatterns) {
		this.packagePatterns = packagePatterns;
	}

	public void setPackagePatternsUseForInclude(boolean packagePatternsUseForInclude) {
		this.packagePatternsUseForInclude = packagePatternsUseForInclude;
	}

	public void setFilters(Predicate<Class<?>>[] filters) {
		this.filters = filters;
	}

	public void setOverrideAmbiguousByOrder(boolean overrideAmbiguousByOrder) {
		this.overrideAmbiguousByOrder = overrideAmbiguousByOrder;
	}

	/**
	 * Directly new create instance, spring is automatically injected into the
	 * container later. refer:
	 * {@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration#createRequestMappingHandlerMapping()}
	 * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#requestMappingHandlerMapping()}
	 * </br>
	 * </br>
	 * Notes: if using @ bean here will result in two instances in the ioc
	 * container.
	 */
	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return new SmartServletHandlerMapping(packagePatterns, packagePatternsUseForInclude, filters, handlerMappings);
	}

	/**
	 * Smart servlet mvc delegate handler mapping.
	 */
	final class SmartServletHandlerMapping extends RequestMappingHandlerMapping {

		/**
		 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.MappingRegistry#mappingLookup}
		 */
		private final Map<RequestMappingInfo, HandlerMethod> registeredMappings = synchronizedMap(new LinkedHashMap<>(32));

		/**
		 * Merged include filter conditionals predicate used to check whether
		 * the bean is a request handler.
		 */
		private final java.util.function.Predicate<Class<?>> mergedFilter;

		/**
		 * All extensions custom request handler mappings.
		 */
		private final List<ServletHandlerMappingSupport> handlerMappings;

		private boolean print = false;

		/**
		 * Notes: Must take precedence, otherwise invalid. refer:
		 * {@link org.springframework.web.servlet.DispatcherServlet#initHandlerMappings()}
		 */
		public SmartServletHandlerMapping(@Nullable String[] packagePatterns, boolean packagePatternsUseForInclude,
				@Nullable Predicate<Class<?>>[] filters, @Nullable List<ServletHandlerMappingSupport> handlerMappings) {
			setOrder(HIGHEST_PRECEDENCE); // Highest priority.

			// Merge predicate for filter condidtions.
			if (nonNull(packagePatterns) && packagePatterns.length > 0) {// If-necessary
				// Build package filter
				Predicate<Class<?>> packagesFilter = beanType -> asList(packagePatterns).stream()
						.anyMatch(pattern -> defaultPackagePatternMatcher.matchStart(pattern, getPackageName(beanType)));
				// Merge filter
				if (packagePatternsUseForInclude) {
					this.mergedFilter = and(packagesFilter, or(safeArrayToList(filters)));
				} else {
					this.mergedFilter = and(not(packagesFilter), or(safeArrayToList(filters)));
				}
			} else {
				this.mergedFilter = or(safeArrayToList(filters));
			}

			// The multiple custom handlers to adjust the execution
			// priority, must sorted.
			this.handlerMappings = safeList(handlerMappings);
			sort(this.handlerMappings);
		}

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE + 10;
		}

		@Override
		public void afterPropertiesSet() {
			super.afterPropertiesSet();

			// Clean useless mappings.
			this.registeredMappings.clear();
		}

		@Override
		protected boolean isHandler(Class<?> beanType) {
			// Ignore spring internal bean?
			if (startsWithAny(beanType.getName(), EXCLUDE_BASE_PACKAGES)) {
				return false;
			}
			return mergedFilter.test(beanType);
		}

		@Override
		protected void detectHandlerMethods(Object handler) {
			Class<?> handlerType = (handler instanceof String ? obtainApplicationContext().getType((String) handler)
					: handler.getClass());

			if (handlerType != null) {
				Class<?> userType = ClassUtils.getUserClass(handlerType);
				Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
						(MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
							try {
								return getMappingForMethod(handler, method, userType);
							} catch (Throwable ex) {
								throw new IllegalStateException(
										"Invalid mapping on handler class [" + userType.getName() + "]: " + method, ex);
							}
						});
				if (logger.isTraceEnabled()) {
					logger.trace(formatMappings(userType, methods));
				}

				methods.forEach((method, mapping) -> {
					Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
					registerHandlerMethod(handler, invocableMethod, mapping);
				});
			}
		}

		private String formatMappings(Class<?> userType, Map<Method, RequestMappingInfo> methods) {
			String formattedType = Arrays.stream(ClassUtils.getPackageName(userType).split("\\.")).map(p -> p.substring(0, 1))
					.collect(Collectors.joining(".", "", "." + userType.getSimpleName()));
			Function<Method, String> methodFormatter = method -> Arrays.stream(method.getParameterTypes())
					.map(Class::getSimpleName).collect(Collectors.joining(",", "(", ")"));
			return methods.entrySet().stream().map(e -> {
				Method method = e.getKey();
				return e.getValue() + ": " + method.getName() + methodFormatter.apply(method);
			}).collect(Collectors.joining("\n\t", "\n\t" + formattedType + ":" + "\n\t", ""));
		}

		/**
		 * Overrided from {@link #getMappingForMethod(Method, Class)}, Only to
		 * add the first parameter 'handler'
		 * 
		 * @param handler
		 * @param method
		 * @param handlerType
		 * @return
		 */
		private RequestMappingInfo getMappingForMethod(Object handler, Method method, Class<?> handlerType) {
			// a. Ensure the external handler mapping is performed first.
			for (ServletHandlerMappingSupport hm : safeList(handlerMappings)) {
				// Use supported custom handler mapping.
				if (hm.supportsHandlerMethod(handler, handlerType, method)) {
					logger.info(format("The method: '%s' is delegated to the request mapping handler registration: '%s'", method,
							hm));
					return hm.getMappingForMethod(method, handlerType);
				}
			}

			// b. Fallback, using default handler mapping.
			if (!print) {
				print = true;
				logger.info(format(
						"No suitable request handler mapping was found, fallback using spring default handler mapping, all handlerMappings: %s",
						handlerMappings));
			}
			return super.getMappingForMethod(method, handlerType);
		}

		/**
		 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.MappingRegistry#register()}
		 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.MappingRegistry#validateMethodMapping()}
		 * {@link org.springframework.web.method.HandlerMethod#equals()}
		 */
		@Override
		public void registerMapping(RequestMappingInfo mapping, Object handler, Method method) {
			doRegisterMapping(mapping, handler, method);
		}

		/**
		 * {@link #registerMapping(RequestMappingInfo, Object, Method)}
		 */
		@Override
		public void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
			doRegisterMapping(mapping, handler, method);
		}

		/**
		 * Smart overridable registration request handler mapping
		 * 
		 * @param mapping
		 *            Request mapping information wrapper.
		 * @param handler
		 *            Actual request mapping handler (beanName).
		 * @param method
		 *            Actual request mapping handler method.
		 */
		void doRegisterMapping(RequestMappingInfo mapping, Object handler, Method method) {
			if (!overrideAmbiguousByOrder) {
				log.debug("Register request mapping [{}] => [{}]", mapping, method.toGenericString());
				super.registerMapping(mapping, handler, method); // By default
				return;
			}

			HandlerMethod newHandlerMethod = createHandlerMethod(handler, method);
			HandlerMethod oldHandlerMethod = registeredMappings.get(mapping);

			// Compare the order of the old and new mapping objects to
			// determine whether to perform the logic to override or preserve
			// the old mapping.
			Object newObj = getApplicationContext().getBean((String) newHandlerMethod.getBean());
			Object oldObj = !isNull(oldHandlerMethod) ? getApplicationContext().getBean((String) oldHandlerMethod.getBean())
					: new Object();
			final boolean isOverridable = INSTANCE.compare(newObj, oldObj) < 0;

			if (isOverridable) {
				if (!isNull(oldHandlerMethod) && !oldHandlerMethod.equals(newHandlerMethod)) {
					// re-register
					super.unregisterMapping(mapping);
					logger.warn(format(
							"Override register mapping. Newer bean '%s' method '%s' to '%s': There is already '%s' older bean method '%s' mapped.",
							newHandlerMethod.getBean(), newHandlerMethod, mapping, oldHandlerMethod.getBean(), oldHandlerMethod));
				}
				log.debug("Register request mapping [{}] => [{}]", mapping, method.toGenericString());
				super.registerMapping(mapping, handler, method);
				registeredMappings.put(mapping, newHandlerMethod);
			} else {
				if ((isNull(oldHandlerMethod) || oldHandlerMethod.equals(newHandlerMethod))) {
					log.debug("Register request mapping [{}] => [{}]", mapping, method.toGenericString());
					super.registerMapping(mapping, handler, method);
					registeredMappings.put(mapping, newHandlerMethod);
				} else {
					logger.warn(format(
							"Skipped ambiguous mapping. Cannot bean '%s' method '%s' to '%s': There is already '%s' bean method '%s' mapped.",
							newHandlerMethod.getBean(), newHandlerMethod, mapping, oldHandlerMethod.getBean(), oldHandlerMethod));
				}
			}
		}

	}

	/**
	 * In order to enable global delegate of {@link RequestMapping} registration
	 * program supporteds.
	 */
	public static abstract class ServletHandlerMappingSupport extends RequestMappingHandlerMapping {

		private volatile SmartServletHandlerMapping delegate;

		public ServletHandlerMappingSupport() {
			setOrder(Ordered.HIGHEST_PRECEDENCE + 10); // By default order
		}

		@Override
		public void afterPropertiesSet() {
			// Must ignore, To prevent spring from automatically calling when
			// initializing the container, resulting in duplicate registration.
		}

		/**
		 * It can be ignored. The purpose is to reduce unnecessary execution and
		 * improve the speed when {@link DispatcherServlet#getHandler()} looks
		 * for mapping. (because spring will automatically add all instances of
		 * {@link HandlerMapping} interface to the candidate list for searching)
		 */
		@Override
		protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
			return null;
		}

		/**
		 * Check if the current the supports registration bean handler method
		 * mapping.
		 * 
		 * @param method
		 * @param handlerType
		 * @return
		 */
		protected abstract boolean supportsHandlerMethod(Object handler, Class<?> handlerType, Method method);

		@Override
		public RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
			return super.getMappingForMethod(method, handlerType);
		}

		// [final] override must not allowed.
		@Override
		public final void registerMapping(RequestMappingInfo mapping, Object handler, Method method) {
			getDelegate().doRegisterMapping(mapping, handler, method);
		}

		// [final] override must not allowed.
		@Override
		public final void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
			getDelegate().doRegisterMapping(mapping, handler, method);
		}

		/**
		 * The method of lazy loading must be used to obtain the delegate object
		 * here, because the subclass of this class may be created externally
		 * by @ bean earlier than the delegate instance created by
		 * {@link WebMvcConfigurationSupport#requestMappingHandlerMapping()}
		 * 
		 * @return
		 */
		private final SmartServletHandlerMapping getDelegate() {
			if (isNull(delegate)) {
				synchronized (this) {
					if (isNull(delegate)) {
						this.delegate = getApplicationContext().getBean(SmartServletHandlerMapping.class);
						// Must init.
						if (isNull(this.delegate.getApplicationContext())) {
							this.delegate.setApplicationContext(getApplicationContext());
						}
					}
				}
			}
			return this.delegate;
		}

	}

	/**
	 * Excludes bean class base packages.
	 */
	private static final String[] EXCLUDE_BASE_PACKAGES = { "org.springframework", "java.", "javax." };

	/**
	 * Class package patterns mapping matcher.
	 */
	private static final AntPathMatcher defaultPackagePatternMatcher = new AntPathMatcher(".");

}

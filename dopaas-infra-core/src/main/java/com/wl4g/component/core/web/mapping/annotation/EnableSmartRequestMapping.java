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
package com.wl4g.component.core.web.mapping.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxRegistrations;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.Predicate;
import com.wl4g.component.core.web.mapping.annotation.WebFluxSmartHandlerMappingConfigurer.ReactiveHandlerMappingSupport;
import com.wl4g.component.core.web.mapping.annotation.WebFluxSmartHandlerMappingConfigurer.SmartReactiveHandlerMapping;
import com.wl4g.component.core.web.mapping.annotation.WebMvcSmartHandlerMappingConfigurer.ServletHandlerMappingSupport;
import com.wl4g.component.core.web.mapping.annotation.WebMvcSmartHandlerMappingConfigurer.SmartServletHandlerMapping;

/**
 * The intelligent request (mvc/webflux) mapping processing component enhances
 * the following functions: Implement the interface of
 * {@link WebMvcRegistrations} and {@link WebFluxRegistrations}, unify the
 * request mapping handler. </br>
 * support: </br>
 * A. allow the same mapping handler to register according to priority,
 * reference: {@link SmartServletHandlerMapping#doRegisterMapping} and
 * {@link SmartReactiveHandlerMapping#doRegisterMapping}; </br>
 * B. control the judgment condition of handler,reference:
 * {@link ServletHandlerMappingSupport#isHandler} and
 * {@link ReactiveHandlerMappingSupport#isHandler}; </br>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-17
 * @sine v1.0
 * @see
 */
@Retention(RUNTIME)
@Target({ TYPE })
@Documented
@Indexed
@Import({ SmartHandlerMappingRegistrar.class })
public @interface EnableSmartRequestMapping {

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	@AliasFor(PACKAGE_PATTERNS)
	String[] value() default {};

	/**
	 * Base package patterns (ant style) to scan for annotated components.</br>
	 * </br>
	 * <font color=red> <b> Notes: When there is a value, the 'and' operation
	 * will be performed with the filter. When it is empty, this condition will
	 * be ignored. </b></font> </br>
	 * </br>
	 * refer to:
	 * {@link com.wl4g.component.core.web.mapping.annotation.WebMvcSmartHandlerMappingConfigurer.SmartServletHandlerMapping#SmartServletHandlerMapping}
	 * {@link com.wl4g.component.core.web.mapping.annotation.WebFluxSmartHandlerMappingConfigurer.SmartReactiveHandlerMapping#SmartReactiveHandlerMapping}
	 * 
	 * @return
	 */
	@AliasFor("value")
	String[] packagePatterns() default {};

	/**
	 * When {@link #packagePatterns()} is set, it indicates whether to execute
	 * inclusion logic (true: inclusion, false: exclusion)
	 * 
	 * @return
	 */
	boolean packagePatternsUseForInclude() default true;

	/**
	 * Request mapping handler configurer filters.
	 * 
	 * @return
	 */
	Class<? extends Predicate<Class<?>>>[] filters() default { DefaultMappingHandlerFilter.class };

	/**
	 * When the same handler mapping appears, whether to enable overlay in bean
	 * order or not.
	 * 
	 * @return
	 */
	boolean overrideAmbiguousByOrder() default false;

	/**
	 * Refer: {@link #packagePatterns()}
	 */
	public static final String PACKAGE_PATTERNS = "packagePatterns";

	/**
	 * Refer: {@link #packagePatternsUseForInclude()}
	 */
	public static final String PACKAGE_PATTERNS_FOR_INCLUDE = "packagePatternsUseForInclude";

	/**
	 * Refer: {@link #packagePatterns()}
	 */
	public static final String FILTERS = "filters";

	/**
	 * Refer: {@link #overrideAmbiguousByOrder()}
	 */
	public static final String OVERRIDE_AMBIGUOUS = "overrideAmbiguousByOrder";

	public static class DefaultMappingHandlerFilter implements Predicate<Class<?>> {
		@Override
		public boolean apply(@Nullable Class<?> beanType) {
			return hasAnnotation(beanType, Controller.class) || hasAnnotation(beanType, RequestMapping.class);
		}
	}

}

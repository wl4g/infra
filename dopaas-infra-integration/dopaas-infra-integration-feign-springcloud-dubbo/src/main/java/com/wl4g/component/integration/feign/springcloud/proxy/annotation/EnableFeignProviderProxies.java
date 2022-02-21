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
package com.wl4g.component.integration.feign.springcloud.proxy.annotation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Indexed;

import com.wl4g.component.core.web.mapping.annotation.EnableSmartRequestMapping;
import com.wl4g.component.integration.feign.springcloud.proxy.FeignProxyMvcAutoConfiguration;

import java.lang.annotation.*;

/**
 * Scans for interfaces that declare they are feign clients (via
 * {@link FeignClient <code>@FeignClient</code>}). Configures component scanning
 * directives for use with
 * {@link org.springframework.context.annotation.Configuration
 * <code>@Configuration</code>} classes.</br>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author Spencer Gibb
 * @author Dave Syer
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see Implementation simulated of refer:
 *      {@link org.springframework.cloud.openfeign.EnableFeignClients}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
@EnableSmartRequestMapping
@Import({ FeignProviderProxiesRegistrar.class, FeignProxyMvcAutoConfiguration.class/*, FeignContextAutoConfiguration.class*/ })
public @interface EnableFeignProviderProxies {

	/**
	 * Alias for the {@link #basePackages()} attribute. Allows for more concise
	 * annotation declarations e.g.: {@code @ComponentScan("org.my.pkg")}
	 * instead of {@code @ComponentScan(basePackages="org.my.pkg")}.
	 *
	 * @return the array of 'basePackages'.
	 */
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components.
	 * <p>
	 * {@link #value()} is an alias for (and mutually exclusive with) this
	 * attribute.
	 * <p>
	 * Use {@link #basePackageClasses()} for a type-safe alternative to
	 * String-based package names.
	 *
	 * @return the array of 'basePackages'.
	 */
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the
	 * packages to scan for annotated components. The package of each class
	 * specified will be scanned.
	 * <p>
	 * Consider creating a special no-op marker class or interface in each
	 * package that serves no purpose other than being referenced by this
	 * attribute.
	 *
	 * @return the array of 'basePackageClasses'.
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * List of classes annotated with {@code @FeignClient}. If not empty,
	 * disables classpath scanning.
	 *
	 * @return
	 */
	Class<?>[] clients() default {};

	Class<?> superClass() default Void.class;

}
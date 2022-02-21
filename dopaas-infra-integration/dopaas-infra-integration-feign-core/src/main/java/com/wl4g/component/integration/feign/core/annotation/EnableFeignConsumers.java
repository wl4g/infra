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
package com.wl4g.component.integration.feign.core.annotation;

import static com.wl4g.component.core.web.mapping.annotation.EnableSmartRequestMapping.PACKAGE_PATTERNS;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import com.wl4g.component.core.web.mapping.annotation.EnableSmartRequestMapping;

import feign.Logger;
import feign.Retryer;

/**
 * {@link EnableFeignConsumers}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
// 排除@FeignConsumer的接口,如:facade层启动需注入的是data层feign实例这个场景(不需要创建facade层接口的feign实例).
@EnableSmartRequestMapping(packagePatternsUseForInclude = false)
@Import({ FeignConsumerConfigurerRegistrar.class, FeignConsumersRegistrar.class, BridgeSpringCloudFeignClientsRegistrar.class })
public @interface EnableFeignConsumers {

	@AliasFor(annotation = EnableSmartRequestMapping.class, attribute = PACKAGE_PATTERNS)
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	@AliasFor(annotation = EnableSmartRequestMapping.class, attribute = PACKAGE_PATTERNS)
	String[] basePackages() default {};

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * The default custom <code>@Configuration</code> for all feign clients. Can
	 * contain override <code>@Bean</code> definition for the pieces that make
	 * up the client, for instance {@link feign.codec.Decoder},
	 * {@link feign.codec.Encoder}, {@link feign.Contract}, {@link Retryer},
	 * {@link Logger}. </br>
	 * </br>
	 * 
	 * The if empty, default refer to
	 * {@link FeignConsumerFactoryBean#mergeFeignConfigurationSet()}
	 * 
	 * @see {@link org.springframework.cloud.openfeign.FeignClientsConfiguration}
	 *      for the defaults
	 * @return list of default configurations
	 */
	Class<?>[] defaultConfiguration() default {};

	/**
	 * List of classes annotated with {@code @SpringBootFeignClient} or
	 * {@code @FeignClient}. If not empty, disables classpath scanning.</br>
	 * </br>
	 * Notes: Valid when the current environment is running in the springcloud
	 * environment.
	 * 
	 * @return list of FeignClient classes
	 */
	Class<?>[] clients() default {};

	/**
	 * Refer: {@link #basePackages()}
	 */
	public static final String BASE_PACKAGES = "basePackages";

	/**
	 * Refer: {@link #basePackageClasses()}
	 */
	public static final String BASE_PACKAGE_CLASSES = "basePackageClasses";

	/**
	 * Refer: {@link #defaultConfiguration()}
	 */
	public static final String DEFAULT_CONFIGURATION = "defaultConfiguration";

}

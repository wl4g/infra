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
package com.wl4g.component.integration.springboot.feign.annotation;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import com.wl4g.component.integration.springboot.feign.config.SpringBootFeignAutoConfiguration;

import feign.Logger;
import feign.Retryer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link EnableSpringBootFeignClients}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableFeignClients
@Import({ SpringBootFeignAutoConfiguration.class, SpringBootFeignClientsRegistrar.class })
// @Import(SpringBootFeignMarkerConfiguration.class)
public @interface EnableSpringBootFeignClients {

	@AliasFor(annotation = EnableFeignClients.class, attribute = "value")
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	@AliasFor(annotation = EnableFeignClients.class, attribute = "basePackages")
	String[] basePackages() default {};

	/**
	 * The default custom <code>@Configuration</code> for all feign clients. Can
	 * contain override <code>@Bean</code> definition for the pieces that make
	 * up the client, for instance {@link feign.codec.Decoder},
	 * {@link feign.codec.Encoder}, {@link feign.Contract}, {@link Retryer},
	 * {@link Logger}. </br>
	 * </br>
	 * 
	 * The if empty, default refer to
	 * {@link SpringBootFeignFactoryBean#mergeConfigurationSet()}
	 * 
	 * @see FeignClientsConfiguration for the defaults
	 * @return list of default configurations
	 */
	@AliasFor(annotation = EnableFeignClients.class, attribute = "defaultConfiguration")
	Class<?>[] defaultConfiguration() default {};

}

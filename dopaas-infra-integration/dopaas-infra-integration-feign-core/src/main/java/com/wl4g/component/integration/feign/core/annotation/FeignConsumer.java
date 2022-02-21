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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wl4g.component.integration.feign.core.config.FeignConsumerProperties;

import feign.Logger;
import feign.Retryer;

/**
 * {@link FeignConsumer} and {@link FeignClient} mutual equivalence.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
@Inherited
@FeignClient // for compatibility with springcloud-feign annotation
@ResponseBody // If use the springMVC annotation, will auto enable responseBody
public @interface FeignConsumer {

	// ---------------------------------------------------------------------
	// It works in both spring boot feign and spring cloud feign frameworks.
	// ---------------------------------------------------------------------

	/**
	 * The name of the service with optional protocol prefix. Synonym for
	 * {@link #name() name}. A name must be specified for all clients, whether
	 * or not a url is provided. Can be specified as property key, eg:
	 * ${propertyKey}.
	 * 
	 * @return the name of the service with optional protocol prefix
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "value")
	String value() default "";

	/**
	 * The service id with optional protocol prefix. Synonym for {@link #value()
	 * value}.
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "name")
	String name() default "";

	/**
	 * An absolute base URL or resolvable hostname (the protocol is optional).
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "url")
	String url() default "";

	/**
	 * whether 404s should be decoded instead of throwing FeignExceptions
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "decode404")
	boolean decode404() default false;

	/**
	 * A custom configuration class for the feign client. Can contain override
	 * <code>@Bean</code> definition for the pieces that make up the client, for
	 * instance {@link feign.codec.Decoder}, {@link feign.codec.Encoder},
	 * {@link feign.Contract}, {@link Retryer}, {@link Logger}. </br>
	 *
	 * Default configuration refer to:
	 * {@link EnableFeignConsumers#defaultConfiguration()}
	 * 
	 * @return list of configurations for feign client.
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "configuration")
	Class<?>[] configuration() default {};

	/**
	 * Whether to mark the feign proxy as a primary bean. Defaults to true.</br>
	 * </br>
	 * Notes: Valid when the current environment is running in the springcloud
	 * environment.
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "primary")
	boolean primary() default true;

	// ---------------------------------------------------------
	// --- It can only work in spring boot feign frameworks. ---
	// ---------------------------------------------------------

	/**
	 * Feign request {@link Logger.Level}
	 * 
	 * @return
	 */
	Logger.Level logLevel() default Logger.Level.NONE;

	/**
	 * The request connect timeout(ms)</br>
	 * for example: ${myapp.springboot.feign.connectTimeout:3000}
	 * 
	 * @return
	 */
	String connectTimeout() default FeignConsumerProperties.DEFAULT_CONNECT_TIMEOUT + "";

	/**
	 * The request read timeout(ms)</br>
	 * for example: ${myapp.springboot.feign.readTimeout:3000}
	 * 
	 * @return
	 */
	String readTimeout() default FeignConsumerProperties.DEFAULT_READ_TIMEOUT + "";

	/**
	 * The request write timeout(ms)</br>
	 * for example: ${myapp.springboot.feign.writeTimeout:3000}
	 * 
	 * @return
	 */
	@Deprecated
	String writeTimeout() default FeignConsumerProperties.DEFAULT_WRITE_TIMEOUT + "";

	/**
	 * The request support 3xx redirection.</br>
	 * for example: ${myapp.springboot.feign.followRedirects:true}
	 * 
	 * @return
	 */
	String followRedirects() default FeignConsumerProperties.DEFAULT_FOLLOWREDIRECTS + "";

	// ----------------------------------------------------------
	// --- It can only work in spring cloud feign frameworks. ---
	// ----------------------------------------------------------

	/**
	 * The service id with optional protocol prefix. Synonym for {@link #value()
	 * value}.
	 *
	 * @deprecated use {@link #name() name} instead
	 */
	@Deprecated
	@AliasFor(annotation = FeignClient.class, attribute = "serviceId")
	String serviceId() default "";

	/**
	 * @return path prefix to be used by all method-level mappings. Can be used
	 *         with or without <code>@RibbonClient</code>.
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "path")
	String path() default "";

	/**
	 * The <code>@Qualifier</code> value for the feign client.
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "qualifier")
	String qualifier() default "";

	/**
	 * Fallback class for the specified Feign client interface. The fallback
	 * class must implement the interface annotated by this annotation and be a
	 * valid spring bean.</br>
	 * </br>
	 * Notes: Valid when the current environment is running in the springcloud
	 * environment.
	 * 
	 * @return fallback class for the specified Feign client interface
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "fallback")
	Class<?> fallback() default void.class;

	/**
	 * Define a fallback factory for the specified Feign client interface. The
	 * fallback factory must produce instances of fallback classes that
	 * implement the interface annotated by {@link FeignClient}. The fallback
	 * factory must be a valid spring bean.</br>
	 * </br>
	 * Notes: Valid when the current environment is running in the springcloud
	 * environment.
	 *
	 * @see feign.hystrix.FallbackFactory for details.
	 * @return fallback factory for the specified Feign client interface
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "fallbackFactory")
	Class<?> fallbackFactory() default void.class;

}

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
package com.wl4g.component.core.web.versions.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Indexed;

import com.wl4g.component.common.lang.SimpleVersionComparator;
import com.wl4g.component.core.web.mapping.annotation.EnableSmartRequestMapping;

/**
 * When enabled, the API multi version request control mapping processor is
 * automatically configured.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
@Retention(RUNTIME)
@Target({ TYPE })
@Documented
@Indexed
@EnableSmartRequestMapping(overrideAmbiguousByOrder = true)
@Import({ ApiVersionMappingRegistrar.class })
public @interface EnableApiVersionManagement {

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	String[] basePackages() default {};

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * Whether to be case sensitive when matching request parameters.
	 * 
	 * @return
	 */
	boolean sensitiveParams() default false;

	/**
	 * Request parameter name for api version mappings. </br>
	 * </br>
	 * for example: {@linkplain #versionParams}({"_v", "_version",
	 * "${myconfig.apiVersion.myVersionParamName}"}) </br>
	 * </br>
	 * 
	 * Notes: The parameters are extracted from the request URL parameters
	 * first. If they are not found, they are extracted from the request header.
	 * If they are not found, then try to add the prefix <b>"x-"</b> to get them
	 * from scratch. If they are still not found, it means that they cannot be
	 * obtained.
	 * 
	 * @return
	 */
	String[] versionParams() default { "_v" };

	/**
	 * Request parameter name for api versions group mappings. </br>
	 * </br>
	 * for example: {@linkplain #groupParams}({"clientType", "platform",
	 * "${myconfig.apiVersion.myGroupParamName}"}) </br>
	 * </br>
	 * 
	 * Notes: The parameters are extracted from the request URL parameters
	 * first. If they are not found, they are extracted from the request header.
	 * If they are not found, then try to add the prefix <b>"x-"</b> to get them
	 * from scratch. If they are still not found, it means that they cannot be
	 * obtained.
	 * 
	 * @return
	 */
	String[] groupParams() default { "platform" };

	/**
	 * Version number size comparator for multi version automatic mapping. (bean
	 * type)
	 * 
	 * @return
	 */
	Class<? extends SimpleVersionComparator> versionComparator() default SimpleVersionComparator.class;

	/**
	 * Refer: {@link #basePackages()}
	 */
	public static final String BASE_PACKAGES = "basePackages";

	/**
	 * Refer: {@link #basePackageClasses()}
	 */
	public static final String BASE_PACKAGE_CLASSES = "basePackageClasses";

	/**
	 * Refer: {@link #sensitiveParams()}
	 */
	public static final String SENSITIVE_PARAMS = "sensitiveParams";

	/**
	 * Refer: {@link #versionParams()}
	 */
	public static final String VERSION_PARAMS = "versionParams";

	/**
	 * Refer: {@link #groupParams()}
	 */
	public static final String GROUP_PARAMS = "groupParams";

	/**
	 * Refer: {@link #comparator()}
	 */
	public static final String VERSION_COMPARATOR = "versionComparator";

}
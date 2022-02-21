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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Indexed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.RetentionPolicy;

/**
 * API version describe definition.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Indexed
public @interface ApiVersion {

	/**
	 * Custom client groups definition value (Usually the user client type).
	 * </br>
	 * </br>
	 * 
	 * for example:
	 * 
	 * <pre>
	 * {@code @}{@link ApiVersion}({@linkplain #groups}={"Android","iOS","iPad","WebPC","WechatMp","${myconfig.apiVersion.myMobileType}"})
	 * </pre>
	 * 
	 * </br>
	 * 
	 * Notes: Optional, when empty, the matching request ignores this condition.
	 * 
	 * @return
	 */
	String[] groups() default {};

	/**
	 * Custom API version number definition value </br>
	 * </br>
	 * format refer to: {major}.{minor}.{revision}.{extension} </br>
	 * </br>
	 * </br>
	 * 
	 * for example1:
	 * 
	 * <pre>
	 * {@code @}{@link ApiVersion}("1.0.2.10b")
	 * </pre>
	 * 
	 * for example2:
	 * 
	 * <pre>
	 * {@code @}{@link ApiVersion}("${myconfig.apiVersion.module1.version20201212}")
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @return
	 */
	String value();

}

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
package com.wl4g.component.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to inform users of a package, class or method's intended audience.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2019-05-20
 * @sine v1.0
 * @see
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public abstract class InterfaceAudience {

	/**
	 * Intended for use by any project or application.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Public {
	}

	/**
	 * Intended for use only within the project itself.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Private {
	}

	private InterfaceAudience() {
	} // Audience can't exist on its own

}
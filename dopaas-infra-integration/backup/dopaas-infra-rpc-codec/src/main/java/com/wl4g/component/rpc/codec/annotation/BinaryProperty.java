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
package com.wl4g.component.integration.codec.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * {@link BinaryProperty}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-14
 * @sine v1.0
 * @see
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
@Inherited
public @interface BinaryProperty {

	/**
	 * A unique position order identifier used to identify a field resolved from
	 * a TCP bytes stream. </br>
	 * (Must be greater than or equal to 0)
	 * 
	 * @return
	 */
	int position() default UNKNOWN_INT;

	/**
	 * The number of bytes occupied by this field. </br>
	 * (Must be greater than or equal to 0)
	 * 
	 * @return
	 */
	int bytes() default UNKNOWN_INT;

	/**
	 * Custom read write field mapping and binary process class.
	 * 
	 * @return
	 */
	Class<?> customCodec() default void.class;

	/**
	 * The number of bytes occupied by this field, which is obtained from the
	 * value of the previous field. </br>
	 * Refer to {@link #bytes()}
	 * 
	 * @return
	 */
	int bytesWithPosition() default UNKNOWN_INT;

	/**
	 * Is left padding used when bytes are under filled? Otherwise, fill from
	 * the right.</br>
	 * 
	 * @return
	 */
	boolean paddingWithLeft() default true;

	/**
	 * When filling is needed, what should be filled?</br>
	 * Refer to {@link #paddingWithLeft()}
	 * 
	 * @return
	 */
	byte padding() default 0;

	/**
	 * The field description.
	 * 
	 * @return
	 */
	String description() default "";

	public static final short UNKNOWN_SHROT = -1;
	public static final int UNKNOWN_INT = UNKNOWN_SHROT;
	public static final long UNKNOWN_LONG = UNKNOWN_SHROT;

}

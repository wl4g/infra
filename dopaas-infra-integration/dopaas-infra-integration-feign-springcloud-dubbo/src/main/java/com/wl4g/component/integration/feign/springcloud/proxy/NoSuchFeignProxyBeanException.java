/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.component.integration.feign.springcloud.proxy;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.lang.String.format;

import org.springframework.beans.BeansException;

/**
 * Thrown when a bean doesn't match the expected type.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-25
 * @sine v1.0
 * @see
 */
public class NoSuchFeignProxyBeanException extends BeansException {
	private static final long serialVersionUID = 7629360523694589546L;

	/** The offending type. */
	private final Class<?> targetInterfaceType;

	public NoSuchFeignProxyBeanException(Class<?> targetInterfaceType) {
		this(format("No such target instance of interface type '%s' was found for the feign proxy.", targetInterfaceType),
				targetInterfaceType);
	}

	public NoSuchFeignProxyBeanException(String msg, Class<?> targetInterfaceType) {
		super(msg);
		this.targetInterfaceType = notNullOf(targetInterfaceType, "targetInterfaceType");
	}

	/**
	 * Return the target interface type of the instance found.
	 */
	public Class<?> getTargetInterfaceType() {
		return this.targetInterfaceType;
	}

}

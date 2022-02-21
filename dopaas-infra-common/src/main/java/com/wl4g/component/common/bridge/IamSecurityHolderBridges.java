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
package com.wl4g.component.common.bridge;

import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethodNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.component.common.reflect.ReflectionUtils2.makeAccessible;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;

/**
 * {@link com.wl4g.iam.core.utils.IamSecurityHolder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-29
 * @sine v1.0
 * @see https://blog.csdn.net/zoinsung_lee/article/details/82529624
 */
public abstract class IamSecurityHolderBridges {

	public static Object invokeGetPrincipalInfo() {
		if (nonNull(getPrincipalInfoMethod)) {
			makeAccessible(getPrincipalInfoMethod);
			return invokeMethod(getPrincipalInfoMethod, null);
		}
		return null;
	}

	public static Object invokeGetBindValue(Object sessionKey) {
		if (nonNull(getBindValueMethod)) {
			makeAccessible(getBindValueMethod);
			return invokeMethod(getBindValueMethod, null, new Object[] { sessionKey });
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T invokeBind(Object sessionKey, T value) {
		if (nonNull(bindMethod)) {
			makeAccessible(bindMethod);
			return (T) invokeMethod(bindMethod, null, new Object[] { sessionKey, value });
		}
		return null;
	}

	public static boolean invokeUnbind(Object sessionKey) {
		if (nonNull(unBindMethod)) {
			makeAccessible(unBindMethod);
			return (boolean) invokeMethod(unBindMethod, null, new Object[] { sessionKey });
		}
		return false;
	}

	/**
	 * Check current runtime has {@link IamSecurityHolder} class
	 * 
	 * @return
	 */
	public static boolean hasIamSecurityHolderClass() {
		return nonNull(iamSecurityHolderClass);
	}

	public static final String iamSecurityHolderClassName = "com.wl4g.iam.core.utils.IamSecurityHolder";
	private static final Class<?> iamSecurityHolderClass = resolveClassNameNullable(iamSecurityHolderClassName);

	private static final Method getPrincipalInfoMethod = findMethodNullable(iamSecurityHolderClass, "getPrincipalInfo");
	private static final Method getBindValueMethod = findMethodNullable(iamSecurityHolderClass, "getBindValue", Object.class);
	private static final Method bindMethod = findMethodNullable(iamSecurityHolderClass, "bind",
			new Class[] { Object.class, Object.class });
	private static final Method unBindMethod = findMethodNullable(iamSecurityHolderClass, "bind", Object.class);

}

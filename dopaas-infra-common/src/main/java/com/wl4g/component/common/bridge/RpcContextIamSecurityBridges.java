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
 * The {@link RpcContextIamSecurityUtils} reflection bridges.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-03-09
 * @sine v1.0
 * @see https://blog.csdn.net/zoinsung_lee/article/details/82529624
 */
public abstract class RpcContextIamSecurityBridges {

	@SuppressWarnings("unchecked")
	public static <T> T currentIamPrincipal() {
		makeAccessible(currentIamPrincipalMethod);
		return (T) invokeMethod(currentIamPrincipalMethod, null);
	}

	public static String currentIamPrincipalId() {
		makeAccessible(currentIamPrincipalIdMethod);
		return (String) invokeMethod(currentIamPrincipalIdMethod, null);
	}

	public static String currentIamPrincipalName() {
		makeAccessible(currentIamPrincipalNameMethod);
		return (String) invokeMethod(currentIamPrincipalNameMethod, null);
	}

	/**
	 * Check current runtime has {@link RpcContextSecurityUtils} class
	 * 
	 * @return
	 */
	public static boolean hasRpcContextIamSecurityUtilsClass() {
		return nonNull(rpcContextIamSecurityUtilsClass);
	}

	public static final String rpcContextIamSecurityUtilsClassName = "com.wl4g.iam.common.utils.RpcContextIamSecurityUtils";
	private static final Class<?> rpcContextIamSecurityUtilsClass = resolveClassNameNullable(rpcContextIamSecurityUtilsClassName);

	private static final Method currentIamPrincipalMethod = findMethodNullable(rpcContextIamSecurityUtilsClass,
			"currentIamPrincipal");
	private static final Method currentIamPrincipalIdMethod = findMethodNullable(rpcContextIamSecurityUtilsClass,
			"currentIamPrincipalId");
	private static final Method currentIamPrincipalNameMethod = findMethodNullable(rpcContextIamSecurityUtilsClass,
			"currentIamPrincipalName");

}

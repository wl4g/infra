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

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethodNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.component.common.reflect.ReflectionUtils2.makeAccessible;
import static java.util.Objects.nonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * This tools class is specially used for reflection call
 * {@link #rpcContextHolderClass}, which provides very good stickiness for
 * supporting different framework architecture running environments to switch
 * between each other.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-29
 * @sine v1.0
 * @see https://blog.csdn.net/zoinsung_lee/article/details/82529624
 */
public abstract class RpcContextHolderBridges {

	// BASIC(static)

	public static Object getContext(boolean useServerContext) {
		return useServerContext ? invokeGetServerContext() : invokeGetContext();
	}

	public static Object invokeGetContext() {
		if (nonNull(getContextMethod)) {
			makeAccessible(getContextMethod);
			return notNullOf(invokeMethod(getContextMethod, null), "currentRpcContext");
		}
		return null;
	}

	public static Object invokeGetServerContext() {
		if (nonNull(getServerContextMethod)) {
			makeAccessible(getServerContextMethod);
			return notNullOf(invokeMethod(getServerContextMethod, null), "currentServerRpcContext");
		}
		return null;
	}

	public static void invokeRemoveContext() {
		if (nonNull(removeContextMethod)) {
			makeAccessible(removeContextMethod);
			invokeMethod(removeContextMethod, null);
		}
	}

	public static void invokeRemoveServerContext() {
		if (nonNull(removeServerContextMethod)) {
			makeAccessible(removeServerContextMethod);
			invokeMethod(removeServerContextMethod, null);
		}
	}

	// get/set

	@SuppressWarnings("unchecked")
	public static <T> T invokeGet(boolean useServerContext, @NotBlank String key, @NotNull Class<T> valueType) {
		if (nonNull(getMethod)) {
			makeAccessible(getMethod);
			return (T) invokeMethod(getMethod, getContext(useServerContext), new Object[] { key, valueType });
		}
		return null;
	}

	public static void invokeSet(boolean useServerContext, @NotBlank String key, @Nullable Object value) {
		if (nonNull(setMethod)) {
			makeAccessible(setMethod);
			invokeMethod(setMethod, getContext(useServerContext), new Object[] { key, value });
		}
	}

	// getAttachment/setAttachment/getAttachments/removeAttachment/clearAttachments

	public static String invokeGetAttachment(boolean useServerContext, @NotNull String key) {
		if (nonNull(getAttachmentMethod)) {
			makeAccessible(getAttachmentMethod);
			return (String) invokeMethod(getAttachmentMethod, getContext(useServerContext), new Object[] { key });
		}
		return null;
	}

	public static void invokeSetAttachment(boolean useServerContext, @NotNull String key, @Nullable String value) {
		if (nonNull(setAttachmentMethod)) {
			makeAccessible(setAttachmentMethod);
			invokeMethod(setAttachmentMethod, getContext(useServerContext), new Object[] { key, value });
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> invokeGetAttachments(boolean useServerContext, @NotNull String key) {
		if (nonNull(getAttachmentsMethod)) {
			makeAccessible(getAttachmentsMethod);
			return (Map<String, String>) invokeMethod(getAttachmentsMethod, getContext(useServerContext), new Object[] { key });
		}
		return null;
	}

	public static void invokeRemoveAttachment(boolean useServerContext, @NotNull String key) {
		if (nonNull(removeAttachmentMethod)) {
			makeAccessible(removeAttachmentMethod);
			invokeMethod(removeAttachmentMethod, getContext(useServerContext), new Object[] { key });
		}
	}

	public static void invokeClearAttachments() {
		if (nonNull(clearAttachmentsMethod)) {
			Object currentRpcContext = invokeGetContext();
			makeAccessible(clearAttachmentsMethod);
			invokeMethod(clearAttachmentsMethod, currentRpcContext);
		}
	}

	// (Reference) get/set

	@SuppressWarnings("unchecked")
	public static <T> T invokeGetRef(boolean useServerContext, @NotNull String key, @NotNull Class<T> valueType) {
		if (nonNull(getReferenceMethod) && nonNull(referenceKeyClass)) {
			try {
				Constructor<?> constructor = referenceKeyClass.getConstructor(String.class);
				makeAccessible(constructor);
				Object referenceKey = constructor.newInstance(key);
				makeAccessible(getReferenceMethod);
				return (T) invokeMethod(getReferenceMethod, getContext(useServerContext),
						new Object[] { referenceKey, valueType });
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}

	public static void invokeSetRef(boolean useServerContext, @NotNull String key, @Nullable Object value) {
		if (nonNull(setReferenceMethod) && nonNull(referenceKeyClass)) {
			try {
				Constructor<?> constructor = referenceKeyClass.getConstructor(String.class);
				makeAccessible(constructor);
				Object referenceKey = constructor.newInstance(key);
				makeAccessible(setReferenceMethod);
				invokeMethod(setReferenceMethod, getContext(useServerContext), new Object[] { referenceKey, value });
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	// Helper methods.

	/**
	 * Check current runtime has {@link RpcContextHolder} class
	 * 
	 * @return
	 */
	public static boolean hasRpcContextHolderClass() {
		return nonNull(rpcContextHolderClass);
	}

	public static final String rpcContextHolderClassName = "com.wl4g.component.integration.feign.core.context.RpcContextHolder";
	public static final String rpcContextHolderReferenceKeyClassName = "com.wl4g.component.integration.feign.core.context.RpcContextHolder.ReferenceKey";

	private static final Class<?> rpcContextHolderClass = resolveClassNameNullable(rpcContextHolderClassName);
	private static final Class<?> referenceKeyClass = resolveClassNameNullable(rpcContextHolderReferenceKeyClassName);

	private static final Method getContextMethod = findMethodNullable(rpcContextHolderClass, "getContext");
	private static final Method getServerContextMethod = findMethodNullable(rpcContextHolderClass, "getServerContext");
	private static final Method removeContextMethod = findMethodNullable(rpcContextHolderClass, "removeContext");
	private static final Method removeServerContextMethod = findMethodNullable(rpcContextHolderClass, "removeServerContext");

	private static final Method getMethod = findMethodNullable(rpcContextHolderClass, "get", String.class, Class.class);
	private static final Method setMethod = findMethodNullable(rpcContextHolderClass, "set", String.class, Object.class);

	private static final Method getAttachmentMethod = findMethodNullable(rpcContextHolderClass, "getAttachment", String.class);
	private static final Method setAttachmentMethod = findMethodNullable(rpcContextHolderClass, "setAttachment", String.class,
			String.class);
	private static final Method getAttachmentsMethod = findMethodNullable(rpcContextHolderClass, "getAttachments");
	private static final Method removeAttachmentMethod = findMethodNullable(rpcContextHolderClass, "removeAttachment",
			String.class);
	private static final Method clearAttachmentsMethod = findMethodNullable(rpcContextHolderClass, "clearAttachments");

	private static final Method getReferenceMethod = findMethodNullable(rpcContextHolderClass, "get", referenceKeyClass,
			Class.class);
	private static final Method setReferenceMethod = findMethodNullable(rpcContextHolderClass, "set", referenceKeyClass,
			Object.class);

}

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
package com.wl4g.component.integration.feign.context;

import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.component.common.lang.Assert2.notNull;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * {@link RpcContextHolder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-17
 * @sine v1.0
 * @see {@link com.alibaba.dubbo.rpc.RpcContext}
 */
public abstract class RpcContextHolder {

	private static final ThreadLocal<RpcContextHolder> original = new ThreadLocal<>();

	private static final ThreadLocal<Map<String, Supplier<Object>>> lambdaAttachments = ThreadLocal
			.withInitial(() -> synchronizedMap(new HashMap<>()));

	/**
	 * Gets current original context instance.
	 * 
	 * @return
	 */
	public static <T extends RpcContextHolder> T getContext() {
		return notNull(original.get(), IllegalStateException.class, "");
	}

	/**
	 * Gets current original context instance.
	 * 
	 * @return
	 */
	public static void setContext(RpcContextHolder originalRpcContext) {
		original.set(originalRpcContext);
	}

	@SuppressWarnings("unchecked")
	public <T> T getLambdaAttachment(@NotBlank String key) {
		return (T) lambdaAttachments.get().get(key);
	}

	public void setLambdaAttachment(@NotBlank String key, @NotNull Supplier<Object> lambdaGetter) {
		lambdaAttachments.get().put(key, lambdaGetter);
	}

	public void save(@NotBlank String key, @Nullable Object value) {
		hasTextOf(key, "attachmentKey");
		if (!isNull(value)) {
			setAttachment(key, toJSONString(value));
		}
	}

	/**
	 * Gets attribute from original Rpc context. {@link #save(String, Object)}
	 * 
	 * @param key
	 * @param valueType
	 * @return
	 */
	public <T> T get(@NotBlank String key, @NotNull Class<T> valueType) {
		hasTextOf(key, "attachmentKey");
		notNullOf(valueType, "attachmentKeyValueType");
		return parseJSON(getAttachment(key), valueType);
	}

	/**
	 * Gets current context attachment element value.
	 * 
	 * @return
	 */
	public abstract String getAttachment(String key);

	/**
	 * Sets current context attachment element value.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setAttachment(String key, String value);

	/**
	 * Remove current context attachment element.
	 * 
	 * @param key
	 */
	public abstract void removeAttachment(String key);

	public abstract void clearAttachments();

	public void clear() {
		clearAttachments();
		lambdaAttachments.remove();
	}

	public abstract int getRemotePort();

	public abstract String getRemoteHost();

	public abstract int getLocalPort();

	public abstract String getLocalHost();

}

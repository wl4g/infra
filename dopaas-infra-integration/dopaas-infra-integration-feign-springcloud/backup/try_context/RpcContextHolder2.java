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

import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.reflect.TypeUtils2.isSimpleType;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.ConvertUtilsBean;

import com.wl4g.component.core.utils.context.SpringContextHolder;

/**
 * {@link RpcContextHolder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-19
 * @sine v1.0
 * @see
 */
public abstract class RpcContextHolder {

	private static final ThreadLocal<Map<String, Supplier<Object>>> lambdaAttachments = ThreadLocal
			.withInitial(() -> synchronizedMap(new HashMap<>()));

	/** Singleton holder instance */
	private static volatile RpcContextHolder holder;

	/**
	 * Obtain singleton instance of {@link RpcContextHolder}
	 * 
	 * @return
	 */
	public static final RpcContextHolder get() {
		if (nonNull(holder)) {
			synchronized (RpcContextHolder.class) {
				if (nonNull(holder)) {
					return (holder = SpringContextHolder.getBean(RpcContextHolder.class));
				}
			}
		}
		return holder;
	}

	/**
	 * Gets attribute from original Rpc context. {@link #set(String, Object)}
	 * 
	 * @param key
	 * @param valueType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(@NotBlank String key, @NotNull Class<T> valueType) {
		hasTextOf(key, "attachmentKey");
		notNullOf(valueType, "attachmentValueType");
		Object value = getAttachment(key);
		if (value instanceof String) {
			return (T) value;
		} else if (isSimpleType(value.getClass())) {
			return (T) defaultConverter.convert(value, valueType);
		} else { // Other object
			return parseJSON((String) value, valueType);
		}
	}

	/**
	 * Sets attribute to original Rpc context. {@link #set(String, Object)}
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public void set(@NotBlank String key, @Nullable Object value) {
		hasTextOf(key, "attachmentKey");
		if (!isNull(value)) {
			if (value instanceof String) {
				setAttachment(key, (String) value);
			} else if (isSimpleType(value.getClass())) {
				setAttachment(key, defaultConverter.convert(value));
			} else { // Other object
				setAttachment(key, toJSONString(value));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getLambdaAttachment(@NotBlank String key) {
		return (T) lambdaAttachments.get().get(key);
	}

	public void setLambdaAttachment(@NotBlank String key, @NotNull Supplier<Object> lambdaGetter) {
		lambdaAttachments.get().put(key, lambdaGetter);
	}

	/**
	 * Gets attachment from current rpc context.
	 * 
	 * @return
	 */
	public abstract String getAttachment(String key);

	/**
	 * Sets attachment to current rpc context.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setAttachment(String key, String value);

	/**
	 * Sets attachments to current rpc context.
	 * 
	 * @param key
	 * @param value
	 */
	public void setAttachments(Map<? extends String, ? extends String> attachments) {
		safeMap(attachments).forEach((key, value) -> setAttachment(key, value));
	}

	/**
	 * Remove current context attachment element.
	 * 
	 * @param key
	 */
	public abstract void removeAttachment(String key);

	/**
	 * Remove all attachments to current rpc context.
	 */
	public abstract void clearAttachments();

	public Integer getRemotePort() {
		return get("remotePort", Integer.class);
	}

	public String getRemoteHost() {
		return get("remoteHost", String.class);
	}

	public Integer getLocalPort() {
		return get("localPort", Integer.class);
	}

	public String getLocalHost() {
		return get("localHost", String.class);
	}

	private static final ConvertUtilsBean defaultConverter = new ConvertUtilsBean();
}

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
package com.wl4g.component.integration.feign.core.context;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.reflect.TypeUtils2.isSimpleType;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static com.wl4g.component.integration.feign.core.constant.FeignConsumerConstant.RPC_ATTACTMENT_MAX_BYTES;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.wl4g.component.common.codec.CodecSource;
import com.wl4g.component.common.log.SmartLogger;
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
	protected static final SmartLogger log = getLogger(RpcContextHolder.class);

	/** Singleton holder instance */
	private static volatile RpcContextHolder provider;

	/** References attachments repository implementation. */
	private volatile ReferenceRepository repository;

	/** Attachments safe/compress codec implementation. */
	private volatile CompressCodec codec;

	protected RpcContextHolder() {
	}

	protected RpcContextHolder(@NotNull ReferenceRepository repository, @NotNull CompressCodec codec) {
		this.repository = notNullOf(repository, "referenceRepository");
		this.codec = notNullOf(codec, "referenceCodec");
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
		String value = getAttachment(key);
		if (!isNull(value)) {
			// Decode attachemnts value(http header safe)
			String val = getCompressCodec().decode(value);
			if (String.class.isAssignableFrom(valueType)) {
				return (T) val;
			} else if (isSimpleType(valueType)) {
				return (T) defaultConverter.convert(val, valueType);
			} else { // Custom object
				return parseJSON((String) val, valueType);
			}
		}
		return null;
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
		if (nonNull(value)) {
			String valStr = null;
			if (value instanceof String) {
				valStr = (String) value;
			} else if (isSimpleType(value.getClass())) {
				valStr = defaultConverter.convert(value);
			} else { // Other object
				valStr = toJSONString(value);
			}
			// Check safe max bytes limit
			if (valStr.getBytes(UTF_8).length > RPC_ATTACTMENT_MAX_BYTES) {
				throw new IllegalArgumentException(format(
						"Too large (%sbytes) attachment object, It is recommended to use parameters in the form of %s. - key: %s, value: %s",
						RPC_ATTACTMENT_MAX_BYTES, ReferenceKey.class.getSimpleName(), key, value));
			}
			// Encode attachemnts value(http header safe)
			setAttachment(key, getCompressCodec().encode(valStr));
		} else { // Remove attachment
			removeAttachment(key);
		}
	}

	/**
	 * Gets attachment from current rpc context.
	 * 
	 * @return
	 */
	public abstract String getAttachment(@Nullable String key);

	/**
	 * Gets all attachments from current rpc context.
	 * 
	 * @return
	 */
	public @Nullable abstract Map<String, String> getAttachments();

	/**
	 * Sets attachment to current rpc context.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setAttachment(@NotNull String key, @Nullable String value);

	/**
	 * Sets all attachments to current rpc context.
	 * 
	 * @param key
	 * @param value
	 */
	public void setAttachments(@Nullable Map<? extends String, ? extends String> attachments) {
		safeMap(attachments).forEach((key, value) -> setAttachment(key, value));
	}

	/**
	 * Remove current context attachment element.
	 * 
	 * @param key
	 */
	public abstract void removeAttachment(@NotNull String key);

	/**
	 * Remove all attachments to current rpc context.
	 */
	public abstract void clearAttachments();

	/**
	 * Gets current remote port.
	 * 
	 * @return
	 */
	public Integer getRemotePort() {
		return get("remotePort", Integer.class);
	}

	/**
	 * Gets current remote host.
	 * 
	 * @return
	 */
	public String getRemoteHost() {
		return get("remoteHost", String.class);
	}

	/**
	 * Gets current rpc local port
	 * 
	 * @return
	 */
	public Integer getLocalPort() {
		return get("localPort", Integer.class);
	}

	/**
	 * Gets current rpc local host.
	 * 
	 * @return
	 */
	public String getLocalHost() {
		return get("localHost", String.class);
	}

	// --- BASIC METHODS. ---

	/**
	 * Obtain current request {@link RpcContextHolder} instance.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends RpcContextHolder> T getContext() {
		return (T) obtainContextProvider().getContext0();
	}

	/**
	 * Obtain current response {@link RpcContextHolder} instance.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends RpcContextHolder> T getServerContext() {
		return (T) obtainContextProvider().getServerContext0();
	}

	/**
	 * Gets or create current this instance.
	 * 
	 * @return
	 */
	protected abstract RpcContextHolder getContext0();

	/**
	 * Gets or create current this instance.
	 * 
	 * @return
	 */
	protected abstract RpcContextHolder getServerContext0();

	/**
	 * Remove current request {@link RpcContextHolder} instance.
	 * 
	 * @return
	 */
	public static final void removeContext() {
		obtainContextProvider().removeContext0();
	}

	/**
	 * Remove current response {@link RpcContextHolder} instance.
	 * 
	 * @return
	 */
	public static final void removeServerContext() {
		obtainContextProvider().removeServerContext0();
	}

	/**
	 * Remove current request {@link RpcContextHolder} instance.
	 * 
	 * @return
	 */
	protected abstract void removeContext0();

	/**
	 * Remove current response {@link RpcContextHolder} instance.
	 * 
	 * @return
	 */
	protected abstract void removeServerContext0();

	/**
	 * Obtain singleton instance of {@link RpcContextHolder}
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static final <T extends RpcContextHolder> T obtainContextProvider() {
		if (isNull(provider)) {
			synchronized (RpcContextHolder.class) {
				if (isNull(provider)) {
					provider = initAvailableHolderProvider();
				}
			}
		}
		return (T) provider;
	}

	// --- REFERENCE ATTACHMENT METHODS. ---

	public <T> T get(@NotBlank ReferenceKey key, @NotNull Class<T> valueType) {
		notNullOf(key, "referenceKey");
		String valueRef = get(key.getKey(), String.class);
		return getReferenceRepository().doGetRefValue(valueRef, valueType);
	}

	public void set(@NotBlank ReferenceKey key, @Nullable Object value) {
		notNullOf(key, "referenceKey");
		if (nonNull(value)) {
			set(key.getKey(), key.getValueRef());
			getReferenceRepository().doSetRefValue(key.getValueRef(), value);
		} else { // Remove attachment
			removeAttachment(key.getKey());
			getReferenceRepository().doRemoveRefValue(key.getKey());
		}
	}

	private final ReferenceRepository getReferenceRepository() {
		if (isNull(repository)) {
			synchronized (this) {
				if (isNull(repository)) {
					List<ReferenceRepository> candidates = safeMap(SpringContextHolder.getBeans(ReferenceRepository.class))
							.values().stream().collect(toList());
					if (!candidates.isEmpty()) {
						AnnotationAwareOrderComparator.sort(candidates);
						this.repository = candidates.get(0);
					} else {
						this.repository = ReferenceRepository.NOOP;
					}
				}
			}
		}
		return this.repository;
	}

	private final CompressCodec getCompressCodec() {
		if (isNull(codec)) {
			synchronized (this) {
				if (isNull(codec)) {
					List<CompressCodec> candidates = safeMap(SpringContextHolder.getBeans(CompressCodec.class)).values().stream()
							.collect(toList());
					if (!candidates.isEmpty()) {
						AnnotationAwareOrderComparator.sort(candidates);
						this.codec = candidates.get(0);
					} else {
						this.codec = CompressCodec.DEFAULT;
					}
				}
			}
		}
		return this.codec;
	}

	/**
	 * Initilization obtain available candidate implements
	 * {@link RpcContextHolder} singleton instance.
	 * 
	 * @return
	 */
	private static final RpcContextHolder initAvailableHolderProvider() {
		List<RpcContextHolder> candidates = safeMap(SpringContextHolder.getBeans(RpcContextHolder.class)).values().stream()
				.collect(toList());

		// Check holders must have only one valid.
		if (candidates.isEmpty()) {
			throw new Error(format("Error, shouldn't be here, No found %s instance.", RpcContextHolder.class.getSimpleName()));
		} else if (candidates.size() > 1) {
			throw new IllegalStateException(format(
					"Found many %s instances, multiple Rpc frameworks coexistence (e.g. feign/dubbo/motan/istio) are not supported. Please check the conflicting framework dependencies.",
					RpcContextHolder.class.getSimpleName()));
		} else {
			return candidates.get(0);
		}
	}

	/**
	 * Reference attachment attribute key. Using this type of attachment key,
	 * only the referenced key will be passed in the RPC context, and the actual
	 * value object will not be passed directly. Usage scenario: it is very
	 * useful for attachments with large object value and low frequency of use,
	 * which can greatly improve performance.
	 */
	public static final class ReferenceKey {
		private final String key;

		public ReferenceKey(String key) {
			this.key = hasTextOf(key, "key");
		}

		public String getKey() {
			return key;
		}

		public String getValueRef() {
			return "ref@".concat(key);
		}
	}

	/**
	 * Reference attachment repository of {@link ReferenceKey}
	 */
	public static interface ReferenceRepository {
		default <T> T doGetRefValue(@NotBlank String refKey, @NotNull Class<T> valueType) {
			log.warn("Unable get reference attachments, because the not implemented! - {}", refKey);
			return null;
		}

		default boolean doSetRefValue(@NotBlank String refKey, @Nullable Object value) {
			log.warn("Unable set reference attachments, because the not implemented! - {}, {}", refKey, value);
			return false;
		}

		default boolean doRemoveRefValue(@NotBlank String refKey) {
			log.warn("Unable remove reference attachments, because the not implemented! - {}", refKey);
			return false;
		}

		public static final ReferenceRepository NOOP = new ReferenceRepository() {
		};
	}

	/**
	 * Reference attachment safe/compress codec.
	 */
	public static interface CompressCodec {
		default String encode(String value) {
			return new CodecSource(value).toHex();
		}

		default String decode(String value) {
			return CodecSource.fromHex(value).toString();
		}

		public static final CompressCodec DEFAULT = new CompressCodec() {
		};
	}

	/** Default types converter. */
	private static final ConvertUtilsBean defaultConverter = new ConvertUtilsBean();

}

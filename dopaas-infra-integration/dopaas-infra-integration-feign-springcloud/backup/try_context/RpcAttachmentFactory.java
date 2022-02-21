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

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.reflect.TypeUtils2.isSimpleType;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static java.util.Objects.isNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * {@link RpcAttachmentFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-19
 * @sine v1.0
 * @see
 */
public class RpcAttachmentFactory implements InitializingBean {

	private final RpcAttachmentRegistry registry = new RpcAttachmentRegistry();
	private final List<RpcAttachmentRegistrar> registrars;

	public RpcAttachmentFactory(List<RpcAttachmentRegistrar> registrars) {
		this.registrars = notNullOf(registrars, "registrars");
	}

	public RpcAttachmentRegistry getRegistry() {
		return registry;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		safeList(registrars).forEach(r -> {
			try {
				RpcAttachmentRegistry cloneRegistry = (RpcAttachmentRegistry) registry.clone();
				r.addAttachments(cloneRegistry);
				registry.putAll(cloneRegistry);
			} catch (CloneNotSupportedException e) {
				throw new IllegalStateException(e);
			}
		});
	}

	public static final class RpcAttachmentRegistry extends ConcurrentHashMap<String, String> {
		private static final long serialVersionUID = -7272212660951144864L;

		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}

		/**
		 * Sets attribute to attachments registry. {@link #load(String, Class)}
		 * 
		 * @param key
		 * @param valueType
		 * @return
		 */
		public void save(@NotBlank String key, @Nullable Object value) {
			hasTextOf(key, "attachmentKey");
			if (!isNull(value)) {
				if (value instanceof String) {
					put(key, (String) value);
				} else if (isSimpleType(value.getClass())) {
					put(key, defaultConverter.convert(value));
				} else { // Other object
					put(key, toJSONString(value));
				}
			}
		}

		/**
		 * Gets attribute from attachments registry.
		 * {@link #save(String, Object)}
		 * 
		 * @param key
		 * @param valueType
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public <T> T load(@NotBlank String key, @NotNull Class<T> valueType) {
			hasTextOf(key, "attachmentKey");
			notNullOf(valueType, "attachmentValueType");
			Object value = get(key);
			if (value instanceof String) {
				return (T) value;
			} else if (isSimpleType(value.getClass())) {
				return (T) defaultConverter.convert(value, valueType);
			} else { // Other object
				return parseJSON((String) value, valueType);
			}
		}

		public Integer getRemotePort() {
			return load("remotePort", Integer.class);
		}

		public String getRemoteHost() {
			return load("remoteHost", String.class);
		}

		public Integer getLocalPort() {
			return load("localPort", Integer.class);
		}

		public String getLocalHost() {
			return load("localHost", String.class);
		}

		private static final ConvertUtilsBean defaultConverter = new ConvertUtilsBean();
	}

	public static interface RpcAttachmentRegistrar {
		void addAttachments(RpcAttachmentRegistry registry);
	}

}

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
package com.wl4g.component.core.utils;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

/**
 * YAML and properties source resovler.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年10月30日
 * @since
 */
public abstract class PropertySources {

	/**
	 * Resolving configuration content of {@link ConfigType}
	 * 
	 * @param type
	 * @param content
	 * @return if resolved null return empty {@link Map}.
	 */
	public static Map<String, Object> resolve(@NotNull ConfigType type, @NotBlank String content) {
		notNullOf(type, "configType");
		hasTextOf(content, "configContent");
		return type.getHandle().resolve(content);
	}

	/**
	 * {@link ConfigType}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @sine v1.0
	 * @see
	 */
	public static enum ConfigType {
		YML(new YamlResolveHandler()), YAML(new YamlResolveHandler()), PROPS(new PropertiesResolveHandler());

		private ResolveHandler handle;

		private ConfigType(ResolveHandler handle) {
			this.handle = handle;
		}

		public ResolveHandler getHandle() {
			return handle;
		}

		public void setHandle(ResolveHandler handle) {
			this.handle = handle;
		}

		public static ConfigType of(String name) {
			for (ConfigType t : values()) {
				if (t.name().equalsIgnoreCase(String.valueOf(name))) {
					return t;
				}
			}
			throw new IllegalStateException(String.format(" 'name' : %s", String.valueOf(name)));
		}

	}

	/**
	 * {@link ResolveHandler}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @sine v1.0
	 * @see
	 */
	public static interface ResolveHandler {
		Map<String, Object> resolve(String content);
	}

	/**
	 * {@link YamlResolveHandler}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @sine v1.0
	 * @see
	 */
	private static class YamlResolveHandler implements ResolveHandler {

		@Override
		public Map<String, Object> resolve(String content) {
			YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
			factory.setResources(new ByteArrayResource(content.getBytes(UTF_8)));
			factory.afterPropertiesSet();

			// Properties to map
			Map<String, Object> map = new HashMap<>();
			if (nonNull(factory) && nonNull(factory.getObject())) {
				factory.getObject().forEach((k, v) -> map.put(valueOf(k), v));
			}

			return map;
		}

	}

	/**
	 * {@link PropertiesResolveHandler}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @sine v1.0
	 * @see
	 */
	private static class PropertiesResolveHandler implements ResolveHandler {

		@Override
		public Map<String, Object> resolve(String content) {
			Map<String, Object> result = new HashMap<>();
			try {
				Properties prop = new Properties();
				prop.load(new StringReader(content));
				// Copy and check.
				prop.forEach((k, v) -> {
					result.put(String.valueOf(k), v);
				});
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
			return result;
		}

	}

}
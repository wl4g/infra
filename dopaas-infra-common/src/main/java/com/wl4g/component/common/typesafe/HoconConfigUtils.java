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
package com.wl4g.component.common.typesafe;

import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notEmpty;
import static com.wl4g.component.common.resource.resolver.ResourceLoader.CLASSPATH_URL_PREFIX;
import static com.wl4g.component.common.resource.resolver.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
import static org.apache.commons.lang3.StringUtils.startsWithAny;

import java.io.IOException;
import java.util.Set;

import static com.typesafe.config.ConfigBeanFactory.create;
import static com.typesafe.config.ConfigFactory.parseURL;
import static com.typesafe.config.ConfigParseOptions.defaults;
import static com.typesafe.config.ConfigSyntax.CONF;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigParseOptions;
import com.wl4g.component.common.resource.StreamResource;
import com.wl4g.component.common.resource.resolver.ClassPathResourcePatternResolver;

/**
 * {@link HoconConfigUtils}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-08-13
 * @since
 */
public abstract class HoconConfigUtils {

	/**
	 * Load hocon configuration
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> T loadConfig(Class<T> clazz) {
		return loadConfig(CLASSPATH_ALL_URL_PREFIX.concat("application.conf"), clazz);
	}

	/**
	 * Load hocon configuration
	 * 
	 * @param location
	 * @param clazz
	 * @return
	 */
	public static <T> T loadConfig(String location, Class<T> clazz) {
		return create(loadConfig(location), clazz);
	}

	/**
	 * Load hocon configuration
	 * 
	 * @param location
	 * @param clazz
	 * @return
	 */
	public static Config loadConfig(String location) {
		hasTextOf(location, "location");

		// Correct scan path
		if (!startsWithAny(location, CLASSPATH_URL_PREFIX, CLASSPATH_ALL_URL_PREFIX)) {
			location = CLASSPATH_ALL_URL_PREFIX.concat(location);
		}

		ClassPathResourcePatternResolver resovler = new ClassPathResourcePatternResolver();
		try {
			Set<StreamResource> ress = resovler.getResources(location);
			notEmpty(ress, "Not found hocon configuration for %s", location);

			ConfigParseOptions options = defaults().setSyntax(CONF).setAllowMissing(true);
			return parseURL(ress.iterator().next().getURL(), options);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}

/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <James Wong@gmail.com, 983708408@qq.com> Technology CO.LTD.
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
package com.wl4g.infra.common.lang;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.TypeConverts.parseDoubleOrNull;
import static com.wl4g.infra.common.lang.TypeConverts.parseFloatOrNull;
import static com.wl4g.infra.common.lang.TypeConverts.parseIntOrNull;
import static com.wl4g.infra.common.lang.TypeConverts.parseLongOrNull;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * {@link EnvironmentUtil}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2021-01-05
 * @sine v1.0
 * @see
 */
public abstract class EnvironmentUtil {

    /** Process properties map cache. */
    public static final Properties PROPS = System.getProperties();

    /** Process environment map cache. */
    public static final Map<String, String> ENV = Collections.unmodifiableMap(System.getenv());

    public static String getStringProperty(@NotBlank String key, @Nullable String defaultValue) {
        hasTextOf(key, "key");
        String value = ENV.get(replace(key, ".", "_").toUpperCase());
        if (isNull(value)) {
            value = PROPS.getProperty(key);
        }
        return isNull(value) ? defaultValue : value;
    }

    public static long getLongProperty(@NotNull String key, @Nullable long defaultValue) {
        Long value = parseLongOrNull(getStringProperty(key, null));
        return nonNull(value) ? value : defaultValue;
    }

    public static int getIntProperty(@NotNull String key, @Nullable int defaultValue) {
        Integer value = parseIntOrNull(getStringProperty(key, null));
        return nonNull(value) ? value : defaultValue;
    }

    public static float getFloatProperty(@NotNull String key, @Nullable float defaultValue) {
        Float value = parseFloatOrNull(getStringProperty(key, null));
        return nonNull(value) ? value : defaultValue;
    }

    public static double getDoubleProperty(@NotNull String key, @Nullable double defaultValue) {
        Double value = parseDoubleOrNull(getStringProperty(key, null));
        return nonNull(value) ? value : defaultValue;
    }

    public static boolean getBooleanProperty(@NotNull String key, @Nullable boolean defaultValue) {
        String value = getStringProperty(key, null);
        return nonNull(value) ? Boolean.parseBoolean(value) : defaultValue;
    }

    public static Map<String, String> getConfigProperties(@NotBlank String prefix) {
        return getConfigProperties(ENV, PROPS, prefix);
    }

    public static Map<String, String> getConfigProperties(
            @NotEmpty Map<String, String> env,
            Properties props,
            @NotBlank String prefix) {
        hasTextOf(prefix, "prefix");
        String underscorePrefix = replace(prefix, ".", "_").toUpperCase();

        Map<String, String> props1 = props.entrySet()
                .stream()
                .filter(e -> startsWithIgnoreCase(String.valueOf(e.getKey()), underscorePrefix))
                .collect(toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));

        Map<String, String> props2 = env.entrySet()
                .stream()
                .filter(e -> startsWithIgnoreCase(e.getKey(), underscorePrefix))
                .collect(toMap(e -> e.getKey(), e -> e.getValue()));

        // Merge environment(prioritized) override to properties.
        props1.putAll(props2);

        return props1;
    }

}

/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * James Wong <jameswong1376@gmail.com> Technology CO.LTD.
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
 * Reference to website: https://wl4g.github.io
 */
package com.wl4g.infra.common.lang;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.TypeConverts.*;
import static java.util.Locale.US;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.replaceEach;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

/**
 * {@link EnvironmentUtil}
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2021-01-05
 * @since v2.0
 */
@SuppressWarnings("unused")
public abstract class EnvironmentUtil {

    /**
     * Process properties map cache.
     */
    public static final Properties PROPS = System.getProperties();

    /**
     * Process environment map cache.
     */
    public static final Map<String, String> ENV = Collections.unmodifiableMap(System.getenv());

    public static String getProperty(@NotBlank String key) {
        return getProperty(key, null);
    }

    public static String getProperty(@NotBlank String key, String defaultValue) {
        return getStringProperty(key, defaultValue);
    }

    public static String getStringProperty(@NotBlank String key) {
        return getStringProperty(key, null);
    }

    public static String getStringProperty(@NotBlank String key, String defaultValue) {
        hasTextOf(key, "key");
        String value = ENV.get(toEnvName(key));
        if (isNull(value)) {
            value = PROPS.getProperty(toPropertyName(key));
        }
        return isNull(value) ? defaultValue : value;
    }

    public static long getLongProperty(@NotNull String key) {
        return getLongProperty(key, -1L);
    }

    public static long getLongProperty(@NotNull String key, long defaultValue) {
        final Long value = parseLongOrNull(getStringProperty(key, null));
        return nonNull(value) ? value : defaultValue;
    }

    public static int getIntProperty(@NotNull String key) {
        return getIntProperty(key, -1);
    }

    public static int getIntProperty(@NotNull String key, int defaultValue) {
        final Integer value = parseIntOrNull(getStringProperty(key, null));
        return nonNull(value) ? value : defaultValue;
    }

    public static float getFloatProperty(@NotNull String key) {
        return getFloatProperty(key, -1f);
    }

    public static float getFloatProperty(@NotNull String key, float defaultValue) {
        final Float value = parseFloatOrNull(getStringProperty(key, null));
        return nonNull(value) ? value : defaultValue;
    }

    public static double getDoubleProperty(@NotNull String key) {
        return getDoubleProperty(key, -1d);
    }

    public static double getDoubleProperty(@NotNull String key, double defaultValue) {
        final Double value = parseDoubleOrNull(getStringProperty(key, null));
        return nonNull(value) ? value : defaultValue;
    }

    public static boolean getBooleanProperty(@NotNull String key) {
        return getBooleanProperty(key, false);
    }

    public static boolean getBooleanProperty(@NotNull String key, boolean defaultValue) {
        final String value = getStringProperty(key, null);
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
        final String underscorePrefix = toEnvName(prefix);

        final Map<String, String> props1 = props.entrySet()
                .stream()
                .filter(e -> startsWithIgnoreCase(String.valueOf(e.getKey()), underscorePrefix))
                .collect(toMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));

        final Map<String, String> props2 = env.entrySet()
                .stream()
                .filter(e -> startsWithIgnoreCase(e.getKey(), underscorePrefix))
                .collect(toMap(Entry::getKey, Entry::getValue));

        // Merge environment(prioritized) override to properties.
        props1.putAll(props2);

        return props1;
    }

    public static String toPropertyName(String name) {
        return replaceEach(name, new String[]{"_", "-"}, new String[]{".", ""}).toLowerCase(US);
    }

    public static String toEnvName(String name) {
        return replaceEach(name, new String[]{".", "-"}, new String[]{"_", ""}).toUpperCase(US);
    }

}

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

import static com.wl4g.infra.common.lang.TypeConverts.parseDoubleOrNull;
import static com.wl4g.infra.common.lang.TypeConverts.parseFloatOrNull;
import static com.wl4g.infra.common.lang.TypeConverts.parseIntOrNull;
import static com.wl4g.infra.common.lang.TypeConverts.parseLongOrNull;
import static java.lang.System.getProperty;
import static java.util.Objects.nonNull;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * {@link EnvironmentUtil}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version v1.0 2021-01-05
 * @sine v1.0
 * @see
 */
public abstract class EnvironmentUtil {

    /** OS environment map cache. */
    public static final Map<String, String> ENV = Collections.unmodifiableMap(System.getenv());

    public static String getStringProperty(@NotNull String key, @Nullable String defaultValue) {
        return getProperty(key, defaultValue);
    }

    public static long getLongProperty(@NotNull String key, @Nullable long defaultValue) {
        Long value = parseLongOrNull(getProperty(key));
        return nonNull(value) ? value : defaultValue;
    }

    public static int getIntProperty(@NotNull String key, @Nullable int defaultValue) {
        Integer value = parseIntOrNull(getProperty(key));
        return nonNull(value) ? value : defaultValue;
    }

    public static float getFloatProperty(@NotNull String key, @Nullable float defaultValue) {
        Float value = parseFloatOrNull(getProperty(key));
        return nonNull(value) ? value : defaultValue;
    }

    public static double getDoubleProperty(@NotNull String key, @Nullable double defaultValue) {
        Double value = parseDoubleOrNull(getProperty(key));
        return nonNull(value) ? value : defaultValue;
    }

    public static boolean getBooleanProperty(@NotNull String key, @Nullable boolean defaultValue) {
        String value = getProperty(key);
        return nonNull(value) ? Boolean.parseBoolean(value) : defaultValue;
    }

}

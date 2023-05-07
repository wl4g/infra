/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
package com.wl4g.infra.metrics.util;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.wl4g.infra.common.log.SmartLogger;

import io.micrometer.core.instrument.ImmutableTag;

/**
 * {@link MetricsUtil}
 * 
 * @author &lt;James Wong James Wong <jameswong1376@gmail.com>&gt;
 * @version 2021-11-30 v1.0.0
 * @since v1.0.0
 */
public abstract class MetricsUtil {
    protected static final SmartLogger log = getLogger(MetricsUtil.class);

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("Unaable get hostname.", e.getMessage());
            return "Unknown host";
        }
    }

    public static ImmutableTag hostnameTag(String key) {
        return new ImmutableTag(hasTextOf(key, "tagKey"), MetricsUtil.getHostname());
    }

}

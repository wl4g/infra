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
package com.wl4g.infra.common.arthas;

import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getStringProperty;
import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.invokeMethod;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.wl4g.infra.common.lang.ClassUtils2;
import com.wl4g.infra.common.reflect.ReflectionUtils2;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link ArthasAttacher}
 * 
 * @author James Wong
 * @version 2022-10-10
 * @since v3.0.0
 * @see https://arthas.aliyun.com/doc/spring-boot-starter.html#非-spring-boot-应用使用方式
 */
@Slf4j
public class ArthasAttacher {

    public static void attachIfNecessary(@Nullable String appName) {
        Class<?> arthasAgentClass = ClassUtils2.resolveClassNameNullable(ARTHAS_AGENT_CLASS);
        if (nonNull(arthasAgentClass)) {
            Method attachMethod = ReflectionUtils2.findMethodNullable(arthasAgentClass, "attach", Map.class);
            notNull(attachMethod, "Failed to load arthas agent, no such method '%s.attach(Map)'", ARTHAS_AGENT_CLASS);
            attachMethod.setAccessible(true);

            // Load ARTHAS properties.
            appName = getStringProperty("arthas.appName", (isBlank(appName) ? "defaultApp" : appName));
            Map<String, String> config = new HashMap<>();
            config.put("arthas.appName", appName);
            config.put("arthas.agentId", getStringProperty("arthas.agentId", appName + "-" + currentTimeMillis()));
            config.put("arthas.ip", getStringProperty("arthas.ip", "0.0.0.0"));
            config.put("arthas.telnetPort", getStringProperty("arthas.telnetPort", "3658"));
            config.put("arthas.httpPort", getStringProperty("arthas.httpPort", "8563"));
            config.put("arthas.tunnelServer", getStringProperty("arthas.tunnelServer", "ws://127.0.0.1:7777/ws"));
            config.put("arthas.sessionTimeout", getStringProperty("arthas.sessionTimeout", "1800")); // seconds
            config.put("arthas.disabledCommands", getStringProperty("arthas.disabledCommands", null)); // e.g:stop,dump
            config.put("arthas.outputPath", getStringProperty("arthas.outputPath", getDefaultArthasOutputPath()));

            log.info("Arthas agent attaching of config: {}", config);
            invokeMethod(attachMethod, null, config);
        } else {
            log.warn("Cannot to start arthas agent, becuase class not found '{}'", ARTHAS_AGENT_CLASS);
        }
    }

    static String getDefaultArthasOutputPath() {
        return USER_HOME.concat("/").concat(".arthas-output/");
    }

    public static final String ARTHAS_AGENT_CLASS = "com.taobao.arthas.agent.attach.ArthasAgent";

}

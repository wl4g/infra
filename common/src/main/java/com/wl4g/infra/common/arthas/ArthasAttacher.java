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
import static com.wl4g.infra.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.infra.common.lang.DateUtils2.getDate;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getStringProperty;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.invokeMethod;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import com.wl4g.infra.common.lang.SystemUtils2;
import com.wl4g.infra.common.reflect.ReflectionUtils2;

/**
 * {@link ArthasAttacher}
 * 
 * @author James Wong
 * @version 2022-10-10
 * @since v3.0.0
 * @see https://arthas.aliyun.com/doc/spring-boot-starter.html#非-spring-boot-应用使用方式
 */
public class ArthasAttacher {

    public static void attachIfNecessary(@Nullable String appName) {
        attachIfNecessary(appName, null);
    }

    public static void attachIfNecessary(@Nullable String appName, @Nullable Class<? extends ClassLoader> condition) {
        synchronized (ArthasAttacher.class) {
            if (tryAttachLock(condition)) {
                Class<?> arthasAgentClass = resolveClassNameNullable(ARTHAS_AGENT_CLASS);
                if (nonNull(arthasAgentClass)) {
                    Method attachMethod = ReflectionUtils2.findMethodNullable(arthasAgentClass, "attach", Map.class);
                    notNull(attachMethod, "Failed to load arthas agent, no such method '%s.attach(Map)'", ARTHAS_AGENT_CLASS);
                    attachMethod.setAccessible(true);

                    // Load ARTHAS properties.
                    appName = getStringProperty("arthas.appName", (isBlank(appName) ? "defaultApp" : appName));
                    Map<String, String> config = new HashMap<>();
                    config.put("arthas.appName", appName);
                    config.put("arthas.agentId", getStringProperty("arthas.agentId", generateDefaultAgentId(appName)));
                    config.put("arthas.ip", getStringProperty("arthas.ip", "0.0.0.0"));
                    config.put("arthas.telnetPort", getStringProperty("arthas.telnetPort", "3658"));
                    config.put("arthas.httpPort", getStringProperty("arthas.httpPort", "8563"));
                    config.put("arthas.tunnelServer", getStringProperty("arthas.tunnelServer", "ws://127.0.0.1:7777/ws"));
                    config.put("arthas.sessionTimeout", getStringProperty("arthas.sessionTimeout", "1800")); // seconds
                    config.put("arthas.disabledCommands", getStringProperty("arthas.disabledCommands", null)); // e.g:stop,dump
                    config.put("arthas.outputPath", getStringProperty("arthas.outputPath", getDefaultArthasOutputPath()));

                    logInfo("Arthas agent attaching of config: %s", config);
                    invokeMethod(attachMethod, null, config);
                } else {
                    logWarn("Cannot to start arthas agent, becuase class not found '%s'", ARTHAS_AGENT_CLASS);
                }
            }
        }
    }

    static boolean tryAttachLock(@Nullable Class<? extends ClassLoader> condition) {
        boolean lock = false;
        final File file = new File(SystemUtils.getJavaIoTmpDir(),
                SystemUtils2.LOCAL_PROCESS_ID.concat(".tmp.").concat(ArthasAttacher.class.getSimpleName()).concat(".lock"));

        final Class<? extends ClassLoader> currentCls = Thread.currentThread().getContextClassLoader().getClass();
        if (isNull(condition) || (nonNull(condition) && condition.isAssignableFrom(currentCls))) {
            if (!file.exists()) {
                try {
                    FileUtils.touch(file);
                    lock = true;
                } catch (IOException e) {
                    throw new IllegalStateException(
                            format("Failed to create attach lock : %s of classLoader: %s", file, currentCls), e);
                }
            }
        }

        logInfo("Try attach locks for : %s, %s with : %s", lock, file, currentCls);
        return lock;
    }

    static String getDefaultArthasOutputPath() {
        return USER_HOME.concat("/.arthas-output/");
    }

    static String generateDefaultAgentId(@NotBlank String appName) {
        String hostname = SystemUtils2.LOCAL_PROCESS_ID;
        try {
            hostname += "-".concat(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            // Ignore
        }
        return appName.concat("-").concat(hostname);
    }

    static void logInfo(String format, Object... args) {
        System.out.println("[".concat(getDate("yyyy-MM-dd HH:mm:ss")).concat("] ").concat(format(format, args)));
    }

    static void logWarn(String format, Object... args) {
        System.err.println("[".concat(getDate("yyyy-MM-dd HH:mm:ss")).concat("] ").concat(format(format, args)));
    }

    public static final String ARTHAS_AGENT_CLASS = "com.taobao.arthas.agent.attach.ArthasAgent";

}

/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.common.runtime;

import static org.apache.commons.lang3.StringUtils.startsWithAny;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Locale;

/**
 * JVM runtime kit utility
 * 
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2019年1月12日
 * @since
 */
public abstract class JvmRuntimeTool {

    /**
     * Check currently JVM runtime in debug mode.
     */
    public static final boolean isJvmInDebugging = isDebugMode0();

    /**
     * Check currently JVM runtime debug status. See: <a href=
     * "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/c30db4c968f6/src/share/classes/com/sun/tools/jdi/SunCommandLineLauncher.java#l216">OpenJDK8
     * source</a>
     * 
     * @return
     */
    private static boolean isDebugMode0() {
        List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String str : arguments) {
            if (startsWithAny(str.toLowerCase(Locale.US), "-agentlib", "-Xrunjdwp", "-Xdebug")) {
                return true;
            }
        }
        return false;
    }

}
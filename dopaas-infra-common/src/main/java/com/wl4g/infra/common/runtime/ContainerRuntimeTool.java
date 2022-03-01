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
package com.wl4g.infra.common.runtime;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;

import com.wl4g.infra.common.cli.ProcessUtils;
import com.wl4g.infra.common.log.SmartLogger;

/**
 * {@link ContainerRuntimeTool}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-01 v1.0.0
 * @since v1.0.0
 */
public abstract class ContainerRuntimeTool {
    private static final SmartLogger log = getLogger(ContainerRuntimeTool.class);

    /**
     * Check currently JVM runtime in container environment type.
     */
    public static final ContainerEnvType currentInContainerType = inContainerType0();

    /**
     * Check currently JVM runtime whether in container environment.
     */
    public static final boolean isCurrentInContainer = isInContainer0();

    private static ContainerEnvType inContainerType0() {
        try {
            if (IS_OS_LINUX) {
                String res = ProcessUtils.execSimpleString(new String[] { "cat", "/proc/1/cgroup" }, 2_000);
                for (ContainerEnvType c : ContainerEnvType.values()) {
                    for (String word : c.keywords) {
                        if (containsIgnoreCase(res, word)) {
                            return c;
                        }
                    }
                }
                return ContainerEnvType.HOST;
            }
        } catch (Exception e) {
            log.warn("Failed to get current JVM runtime container env. cause by: {}", e.getMessage());
        }
        return ContainerEnvType.UNKNOWN;
    }

    private static boolean isInContainer0() {
        return currentInContainerType != ContainerEnvType.HOST && currentInContainerType != ContainerEnvType.UNKNOWN;
    }

    public static enum ContainerEnvType {

        /**
         * The JVM is currently running in the container of the Docker engine.
         * 
         * for experiment:
         * 
         * <pre>
         * $ docker exec -it &lt;containerId&gt; sh
         * $ cat /proc/1/cgroup
         * 
         *   12:rdma:/
         *   11:freezer:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   10:memory:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   9:cpu,cpuacct:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   8:pids:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   7:cpuset:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   6:devices:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   5:perf_event:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   4:hugetlb:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   3:net_cls,net_prio:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   2:blkio:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   1:name=systemd:/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         *   0::/system.slice/docker-e3aa714a49a6d4f73f9b5e511be9453f65188a58db13d8790a8656f5f91cbf84.scope
         * </pre>
         */
        DOCKER(".slice/docker"),

        // TODO
        PODMAN(".slice/podman"),

        // TODO
        RKT(".slice/rkt"),

        /**
         * The JVM is currently running in the container of the Kubernetes
         * engine.
         * 
         * for experiment:
         * 
         * <pre>
         * $ docker exec -it &lt;containerId&gt; sh
         * $ cat /proc/1/cgroup
         * 
         *   12:rdma:/
         *   11:cpuset:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   10:blkio:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   9:devices:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   8:memory:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   7:cpu,cpuacct:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   6:hugetlb:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   5:perf_event:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   4:freezer:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   3:net_cls,net_prio:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   2:pids:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   1:name=systemd:/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         *   0::/kubepods.slice/kubepods-besteffort.slice/kubepods-besteffort-podce0df6b4_a5ca_4d75_985e_0bd284e9f9db.slice/crio-6a876a89da9a2f9fa06981848be05bb493dbb5ed67b35603404c2eb3cc9ce12c.scope
         * </pre>
         */
        KUBERNETES(".slice/kubepods"),

        /**
         * The JVM is currently running in the non-container on the Hosted.
         * 
         * for example:
         * 
         * <pre>
         * $ cat /proc/1/cgroup
         * 
         *   12:rdma:/
         *   11:freezer:/
         *   10:memory:/
         *   9:cpu,cpuacct:/
         *   8:pids:/
         *   7:cpuset:/
         *   6:devices:/
         *   5:perf_event:/
         *   4:hugetlb:/
         *   3:net_cls,net_prio:/
         *   2:blkio:/
         *   1:name=systemd:/init.scope
         *   0::/init.scope
         * </pre>
         */
        HOST,

        /**
         * if the current JVM is running on windows, and checking is not
         * supported for the time being.
         */
        UNKNOWN;

        private final String[] keywords;

        private ContainerEnvType(String... keywords) {
            this.keywords = keywords;
        }

    }

}

/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.integration.feign.core.annotation;

import static com.wl4g.infra.common.lang.ClassUtils2.isPresent;
import static java.lang.String.format;

/**
 * {@link FrameworkType}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-03-10 v1.0.0
 */
public enum FrameworkType {

    /**
     * The current is Feign+SpringBoot environment.
     */
    FEIGN_SPRINGBOOT {

        @Override
        public boolean detected() {
            if (FEIGN_SPRINGBOOT_ALIBABA_DUBBO.isActive()) {
                throw new IllegalStateException(format("Check the existing com.alibaba.dubbo.xx "
                        + "classes in the current classpath, the new version has been migrated to org.apache.dubbo.xx,"
                        + "please remove old-version dependency and use the new version!"));
            }
            return !FEIGN_SPRINGCLOUD.isActive() && !FEIGN_SPRINGBOOT_ISTIO.isActive()
                    && !FEIGN_SPRINGBOOT_APACHE_DUBBO.isActive();
        }
    },

    /**
     * The current is Feign+SpringCloud environment.
     */
    FEIGN_SPRINGCLOUD {

        @Override
        public boolean detected() {
            return isPresent("org.springframework.cloud.openfeign.FeignClientsRegistrar")
                    || isPresent("com.wl4g.infra.integration.feign.springcloud.constant.SpringCloudFeignConstant");
        }
    },

    /**
     * The current is Feign+SpringBoot+Istio environment.
     * 
     * @see https://github.com/wl4g-k8s/spring-cloud-kubernetes-with-istio
     */
    FEIGN_SPRINGBOOT_ISTIO {

        @Override
        public boolean detected() {
            return isPresent("com.wl4g.infra.integration.feign.istio.constant.IstioFeignConstant");
        }
    },

    /**
     * The current is Feign+SpringBoot+ApacheDubbo environment.
     */
    FEIGN_SPRINGBOOT_APACHE_DUBBO {

        @Override
        public boolean detected() {
            return isPresent("com.wl4g.infra.integration.feign.dubbo.constant.DubboFeignConstant")
                    || isPresent("org.apache.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor");
        }
    },

    /**
     * The current is Feign+SpringBoot+AlibabaDubbo environment.
     */
    @Deprecated
    FEIGN_SPRINGBOOT_ALIBABA_DUBBO {

        @Override
        public boolean detected() {
            return isPresent("com.wl4g.infra.integration.feign.dubbo.constant.DubboFeignConstant")
                    || isPresent("com.alibaba.dubbo.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor");
        }
    };

    /**
     * Determines if the framework type is active (i.e. the application is
     * running in it).
     * 
     * @return if the framework type is active.
     */
    public boolean isActive() {
        return detected();
    }

    /**
     * Determines if the framework is detected by looking for platform-specific
     * environment variables.
     * 
     * @return if the platform is auto-detected.
     */
    protected abstract boolean detected();

    /**
     * Returns the active {@link FrameworkType} or {@code null} if one is not
     * active.
     * 
     * @param environment
     *            the environment
     * @return the {@link FrameworkType} or {@code null}
     */
    public static FrameworkType getActive() {
        for (FrameworkType type : values()) {
            if (type.isActive()) {
                return type;
            }
        }
        return null;
    }

}

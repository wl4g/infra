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
package com.wl4g.infra.integration.feign.istio.config;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;

import java.util.Iterator;

import javax.validation.constraints.NotNull;

import com.google.common.base.Splitter;
import com.wl4g.infra.integration.feign.core.annotation.FeignSpringBootTargetFactory;
import com.wl4g.infra.integration.feign.core.config.FeignSpringBootProperties;

import feign.Target;

/**
 * {@link FeignSpringBootIstioTargetFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-10 v1.0.0
 * @since v1.0.0
 */
public class FeignSpringBootIstioTargetFactory implements FeignSpringBootTargetFactory {

    private FeignSpringBootIstioProperties istioConfig;

    public FeignSpringBootIstioTargetFactory(FeignSpringBootIstioProperties istioConfig) {
        this.istioConfig = notNullOf(istioConfig, "istioConfig");
    }

    @Override
    public <T> Target<T> create(FeignSpringBootProperties config, Class<T> type, String name, String url, String path) {
        return new IstioFeignUrlTarget<T>(config, type, name, url, path);
    }

    class IstioFeignUrlTarget<T> extends DefaultFeignUrlTarget<T> {

        public IstioFeignUrlTarget(@NotNull FeignSpringBootProperties config, @NotNull Class<T> type, String name, String url,
                String path) {
            super(config, type, name, url, path);
        }

        // see:https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/#namespaces-of-services
        @Override
        protected String buildByName() {
            String clusterDomain = istioConfig.getKubeConfig().getClusterDomain();
            String parts = name();
            String namespace = "default";
            String serviceId = parts;
            if (parts.contains(":")) {
                Iterator<String> it = Splitter.on(":").omitEmptyStrings().trimResults().split(parts).iterator();
                if (it.hasNext()) {
                    namespace = it.next();
                }
            }
            return new StringBuffer(64).append("http://")
                    .append(serviceId)
                    .append(".")
                    .append(namespace)
                    .append(".svc.")
                    .append(clusterDomain)
                    .append(cleanPath())
                    .toString();
        }

    }

}

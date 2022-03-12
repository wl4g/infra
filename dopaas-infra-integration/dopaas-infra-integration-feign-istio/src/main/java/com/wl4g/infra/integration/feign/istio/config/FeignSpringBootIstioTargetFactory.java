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
import static com.wl4g.infra.integration.feign.istio.config.FeignSpringBootIstioProperties.DEFAULT_SVC_NAMESPACE;
import static com.wl4g.infra.integration.feign.istio.config.FeignSpringBootIstioProperties.DEFAULT_SVC_SCHEMA;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URI;
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
        return new IstioFeignUrlTarget<T>(istioConfig, config, type, name, url, path);
    }

    public static class IstioFeignUrlTarget<T> extends FeignSpringBootUrlTarget<T> {
        private FeignSpringBootIstioProperties istioConfig;
        private String actualStdKubernetesSvcDomain;

        public IstioFeignUrlTarget(@NotNull FeignSpringBootIstioProperties istioConfig, @NotNull FeignSpringBootProperties config,
                @NotNull Class<T> type, String name, String url, String path) {
            super(config, type, name, url, path);
            this.istioConfig = istioConfig;
        }

        // see:https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/#namespaces-of-services
        @Override
        protected String buildByName() {
            // Because it will be intercepted and enhanced by sidecar, you can
            // use http fixedly.
            return DEFAULT_SVC_SCHEMA.concat(getStdKubernetesSvcDomain()).concat(cleanPath());
        }

        public String getStdKubernetesSvcDomain() {
            if (isBlank(actualStdKubernetesSvcDomain)) {
                synchronized (this) {
                    if (isBlank(actualStdKubernetesSvcDomain)) {
                        actualStdKubernetesSvcDomain = obtainStdKubernetesSvcDomain();
                    }
                }
            }
            return actualStdKubernetesSvcDomain;
        }

        private String obtainStdKubernetesSvcDomain() {
            String clusterDomain = istioConfig.getKubeConfig().getClusterDomain();
            String urlStr = name();

            // Define defaults
            String namespace = DEFAULT_SVC_NAMESPACE;
            String serviceId = urlStr;

            // Remove to schema
            URI resolved = URI.create(urlStr);
            if (!isBlank(resolved.getScheme())) {
                urlStr = urlStr.substring(0, resolved.getScheme().length());
            }

            // Splitting to namespace and serviceId.
            if (urlStr.contains(":")) {
                Iterator<String> it = Splitter.on(":").omitEmptyStrings().trimResults().split(urlStr).iterator();
                if (it.hasNext()) {
                    namespace = it.next();
                }
            }
            return new StringBuffer(64).append(serviceId)
                    .append(".")
                    .append(namespace)
                    .append(".svc.")
                    .append(clusterDomain)
                    .toString();
        }

    }

}

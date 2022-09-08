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
package com.wl4g.infra.integration.feign.istio.config;

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.integration.feign.istio.config.FeignSpringBootIstioProperties.FeignServiceProperties.DEFAULT_SVC_SCHEMA;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URI;

import javax.validation.constraints.NotNull;

import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.infra.integration.feign.core.annotation.FeignSpringBootTargetFactory;
import com.wl4g.infra.integration.feign.core.config.FeignSpringBootProperties;

import feign.Target;

/**
 * {@link FeignSpringBootIstioTargetFactory}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-03-10 v1.0.0
 * @since v1.0.0
 */
public class FeignSpringBootIstioTargetFactory implements FeignSpringBootTargetFactory {

    private FeignSpringBootIstioProperties istioConfig;

    public FeignSpringBootIstioTargetFactory(FeignSpringBootIstioProperties istioConfig) {
        this.istioConfig = notNullOf(istioConfig, "istioConfig");
    }

    @Override
    public <T> Target<T> create(
            FeignSpringBootProperties config,
            Class<T> type,
            String name,
            String namespace,
            String url,
            String path) {
        return new IstioFeignUrlTarget<T>(istioConfig, config, type, name, namespace, url, path);
    }

    public static class IstioFeignUrlTarget<T> extends FeignSpringBootUrlTarget<T> {
        private FeignSpringBootIstioProperties istioConfig;
        private String actualStdKubernetesSvcDomain;

        public IstioFeignUrlTarget(@NotNull FeignSpringBootIstioProperties istioConfig, @NotNull FeignSpringBootProperties config,
                @NotNull Class<T> type, String name, String namespace, String url, String path) {
            super(config, type, name, namespace, url, path);
            this.istioConfig = notNullOf(istioConfig, "istioConfig");
        }

        // see:https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/#namespaces-of-services
        @Override
        protected String buildByName() {
            // Because it will be intercepted and enhanced by sidecar, you can
            // use http fixedly.
            return DEFAULT_SVC_SCHEMA.concat(obtainStdKubernetesSvcDomainUri()).concat(cleanPath());
        }

        public String obtainStdKubernetesSvcDomainUri() {
            actualStdKubernetesSvcDomain = buildStdKubernetesSvcDomain();
            if (isBlank(actualStdKubernetesSvcDomain)) {
                synchronized (this) {
                    if (isBlank(actualStdKubernetesSvcDomain)) {
                        actualStdKubernetesSvcDomain = buildStdKubernetesSvcDomain();
                    }
                }
            }
            return actualStdKubernetesSvcDomain;
        }

        private String buildStdKubernetesSvcDomain() {
            String clusterDomain = istioConfig.getKubeConfig().getClusterDomain();
            String str = hasText(name(), "Feign service '%s' @%s name is required.", type(), FeignConsumer.class.getSimpleName());

            // make sure it can be parsed.
            URI uri = URI.create(str);
            if (!contains(str, "://")) {
                uri = URI.create(DEFAULT_SVC_SCHEMA.concat(str));
            }

            // Gets serviceId(host)
            String serviceId = str;
            if (!isBlank(uri.getHost())) {
                serviceId = uri.getHost();
            }

            // Gets port info
            String portInfo = "";
            if (uri.getPort() > 0) {
                portInfo = ":".concat(valueOf(uri.getPort()));
            }

            // // Resolve to namespace and serviceId.
            /// **
            // * <pre>
            // * &#64;FeignConsumer(name = "mynamespace(order-service)")
            // * &#64;RequestMapping("/order")
            // * public interface OrderService {
            // * }
            // * </pre>
            // */
            // int start = str.indexOf("(");
            // int end = str.indexOf(")");
            // int lstart = str.lastIndexOf("(");
            // int lend = str.indexOf(")");
            // if (start != lstart || end != lend) {
            // throw new IllegalArgumentException(format(
            // "Syntax error for @%s(name = \"%s\") configuration, because there
            // are multiple '(' or ')' characters",
            // FeignConsumer.class.getSimpleName(), str, start, lstart));
            // }
            // if (start >= 0 && start < end) {
            // namespace = str.substring(0, start);
            // serviceId = str.substring(start + 1, end);
            // }

            return new StringBuffer(64).append(serviceId)
                    .append(".")
                    .append(namespace())
                    .append(".svc.")
                    .append(clusterDomain)
                    .append(portInfo)
                    .toString();
        }

    }

}

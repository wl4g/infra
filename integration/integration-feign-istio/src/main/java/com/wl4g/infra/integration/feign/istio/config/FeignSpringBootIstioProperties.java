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
package com.wl4g.infra.integration.feign.istio.config;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Istio client properties.
 */
@Getter
@Setter
@ToString
public class FeignSpringBootIstioProperties {

    private KubeConfigProperties kubeConfig = new KubeConfigProperties();
    private FeignTracingProperties tracing = new FeignTracingProperties();
    private FeignServiceProperties service = new FeignServiceProperties();

    // private Integer envoyPort = 15090;
    // private String envoyProbePath = "stats/prometheus";

    @Getter
    @Setter
    @SuppressWarnings("deprecation")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true, allowSetters = true)
    public static class KubeConfigProperties extends io.fabric8.kubernetes.client.Config {
        private String clusterDomain = "cluster.local";
    }

    @Getter
    @Setter
    @ToString
    public static class FeignTracingProperties {
        public static final List<String> DEFAULT_PREFIX_TRACING_HEADERS = unmodifiableList(new ArrayList<String>() {
            private static final long serialVersionUID = 8480190950467098205L;
            {
                add("X-Forwarded-");
                add("X-Request-");
                // see: Jaeger+Istio(envoy)
                // see:https://www.servicemesher.com/envoy/configuration/http_filters/router_filter.html
                // see:https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_filters/router_filter
                add("X-B3-");
                add("X-Envoy-");
            }
        });

        private List<String> prefixHttpHeaders = DEFAULT_PREFIX_TRACING_HEADERS;
    }

    @Getter
    @Setter
    @ToString
    public static class FeignServiceProperties {
        public static final String DEFAULT_SVC_SCHEMA = "http://";
    }

}
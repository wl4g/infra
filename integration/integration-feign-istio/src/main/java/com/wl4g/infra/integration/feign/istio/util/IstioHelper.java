/// *
// * Copyright (C) 2017 ~ 2025 the original author or authors.
// * <James Wong@gmail.com, 983708408@qq.com> Technology CO.LTD.
// * All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// * Reference to web-site: http://infra.wl4g.com
// */
// package com.wl4g.infra.integration.feign.istio.util;
//
// import static com.wl4g.infra.common.lang.Assert2.notNullOf;
// import static java.lang.String.valueOf;
//
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.boot.web.client.RestTemplateBuilder;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.client.RestTemplate;
//
// import com.wl4g.infra.integration.feign.istio.config.IstioClientProperties;
//
/// **
// * Utility class to work with meshes.
// *
// * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
// * @version 2022-03-11 v1.0.0
// * @see
/// https://github.com/wl4g-k8s/spring-cloud-kubernetes-with-istio#istio-initial-startup-networking
// */
// public class IstioHelper {
// private static final Logger log = LoggerFactory.getLogger(IstioHelper.class);
//
// private final IstioClientProperties config;
// private final RestTemplate restTemplate;
//
// public IstioHelper(IstioClientProperties config) {
// this.config = notNullOf(config, "config");
// this.restTemplate = new RestTemplateBuilder().build();
// }
//
// public Boolean isIstioEnabled() {
// return probeIstioSidecar();
// }
//
// //
/// https://github.com/wl4g-k8s/spring-cloud-kubernetes-with-istio#istio-initial-startup-networking
// private synchronized boolean probeIstioSidecar() {
// try {
// // Check if Istio Envoy proxy is installed. Notice that the check is
// // done to localhost.
// // TODO: We can improve this initial detection if better methods are
// // found.
// String probeUrl = "http://127.0.0.1:".concat(valueOf(config.getEnvoyPort()))
// .concat("/".concat(config.getEnvoyProbePath()));
// ResponseEntity<String> resp = restTemplate.getForEntity(probeUrl,
/// String.class);
// if (resp.getStatusCode().is2xxSuccessful()) {
// log.info("Istio Resources Found.");
// return true;
// }
// log.warn("Although Envoy proxy did respond at port" + config.getEnvoyPort()
// + ", it did not respond with HTTP 200 to path: " + config.getEnvoyProbePath()
// + ". You may need to tweak the test path in order to get proper Istio
/// support");
// return false;
// } catch (Throwable t) {
// if (log.isDebugEnabled()) {
// log.debug("Envoy proxy could not be located at port: " +
/// config.getEnvoyPort()
// + ". Assuming that the application is not running inside the Istio Service
/// Mesh");
// }
// return false;
// }
// }
//
// }

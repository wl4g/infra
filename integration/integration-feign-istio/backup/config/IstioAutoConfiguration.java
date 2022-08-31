/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <James Wong@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.infra.integration.feign.istio.config;

//import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform; 
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.wl4g.infra.integration.feign.core.annotation.FeignTargetFactory;
import com.wl4g.infra.integration.feign.istio.constant.IstioFeignConstant;

//import io.fabric8.istio.client.DefaultIstioClient;
//import io.fabric8.istio.client.IstioClient;

/**
 * Auto configuration for Istio.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = IstioFeignConstant.KEY_ISTIO_PREFIX + ".enabled", matchIfMissing = true)
// @ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
public class IstioAutoConfiguration {

    // @Bean
    // @ConditionalOnMissingBean
    // public IstioClient istioClient(IstioClientProperties config) {
    // return new DefaultIstioClient(config.getKubeConfig());
    // }

    @Order(-100)
    @Bean
    public FeignTargetFactory istioFeignTargetFactory() {
        return new IstioFeignTargetFactory();
    }

}

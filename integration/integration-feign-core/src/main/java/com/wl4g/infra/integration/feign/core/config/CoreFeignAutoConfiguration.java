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
package com.wl4g.infra.integration.feign.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import com.wl4g.infra.integration.feign.core.constant.FeignConsumerConstant;

/**
 * {@link CoreFeignAutoConfiguration}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
public class CoreFeignAutoConfiguration {

    @Bean
    @Order(0)
    @ConfigurationProperties(prefix = FeignConsumerConstant.CONF_PREFIX_INFRA_FEIGN)
    public FeignSpringBootProperties feignSpringBootProperties() {
        return new FeignSpringBootProperties();
    }

}

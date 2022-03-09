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
package com.wl4g.infra.integration.feign.core.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.wl4g.infra.integration.feign.core.annotation.mvc.SpringMvcContract;

/**
 * {@link SpringBootFeignAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@ImportAutoConfiguration({ OkhttpSpringBootFeignAutoConfiguration.class, Http2SpringBootFeignAutoConfiguration.class })
public class SpringBootFeignAutoConfiguration {
 
    @Bean(BEAN_SPRINGMVC_CONTRACT)
    public SpringMvcContract springMvcContract() {
        return new SpringMvcContract();
    }

    public static final String BEAN_SPRINGMVC_CONTRACT = "infraSpringBootFeign.defaultConsumerMvcContract";
    public static final String BEAN_DEFAULT_FEIGN_CLIENT = "infraSpringBootFeign.defaultFeignClient";

}

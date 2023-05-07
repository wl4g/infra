package com.wl4g;
/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <jameswong1376@gmail.com> Technology CO.LTD.
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
 * Reference to website: https://wl4g.github.io
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.wl4g.infra.integration.feign.core.annotation.EnableFeignConsumers;

/**
 * {@link FeginSpringCloudExampleWeb}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2021-03-18
 * @since v2.0
 * @see
 */
// @EnableFeignClients("com.wl4g.infra.integration.feign.springcloud.example.service")
@EnableFeignConsumers("com.wl4g.infra.integration.feign.springcloud.example.service")
@SpringBootApplication(scanBasePackages = "com.wl4g.infra.integration")
public class FeginSpringCloudExampleWeb {

    public static void main(String[] args) {
        SpringApplication.run(FeginSpringCloudExampleWeb.class, args);
    }

}

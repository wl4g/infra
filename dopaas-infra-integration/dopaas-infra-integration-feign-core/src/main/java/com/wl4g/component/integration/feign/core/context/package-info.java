/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
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
package com.wl4g.component.integration.feign.core.context;
/**
 * 关于配置类、注册类的启用说明: </br>
 * 1. 当运行在 SpringBoot+Feign 环境下将启用以下配置类:
 * {@link FeignRpcContextAutoConfiguration} </br>
 * {@link JedisReferenceRepositoryAutoConfiguration} </br>
 * {@link DefaultFeignContextAutoConfiguration} </br>
 * 以及其他包如: feign.core.annotation/feign.core.config 下的配置类、注册类启用 </br>
 * 
 * 2. 当运行在 SpringBoot+Feign+Istio 环境和 Feign+SpringCloud 环境下将启用以下配置类:
 * {@link FeignRpcContextAutoConfiguration} </br>
 * {@link JedisReferenceRepositoryAutoConfiguration} </br>
 * 以及其他包如: feign.core.annotation/feign.core.config 下的配置类、注册类启用 </br>
 * 
 * 3. 当运行在 Feign+SpringCloud+Dubbo 环境下将启用以下配置类:
 * {@link JedisReferenceRepositoryAutoConfiguration}</br>
 * 以及其他包如: feign.core.annotation/feign.core.config 下的配置类、注册类不启用 </br>
 */
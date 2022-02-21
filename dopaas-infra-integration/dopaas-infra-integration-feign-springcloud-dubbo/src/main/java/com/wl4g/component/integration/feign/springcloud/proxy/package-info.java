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
package com.wl4g.component.integration.feign.springcloud.proxy;
/**
 * The purpose of this module is to dynamically add the service API of @
 * feignclient to the rest controller through AOP (cglib). In this way, the @
 * restcontroller is not required to be used on the implementation class. It
 * solves the problem that the same project can not only support multi JVM
 * process startup in distributed mode, but also can be started by local
 * debugging single JVM process.</br>
 * </br>
 * Thanks to refer: https://gitee.com/leecho/spring-cloud-feign-proxy
 */
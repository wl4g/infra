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
package org.springframework.cloud.openfeign;

/**
 * 作用： </br>
 * 仅仅为了让服务运行在 如：springboot-feign + istio架构 与 springcloud + feign架构下，可以任意切换(业务代码依赖注解，让其可编译).
 * 
 * 1. @FeignClient 类来自于springCloud，目的是解决在某些spring版本下，
 * 当没有引用spring-cloud-openfeign依赖时，可能注解查找时无法正确获取合并后(被@AliasFor修饰)的注解元信息
 * (不过目前在spring-boot 2.3.2下没有此类在 SpringBootFeignClientScanner#doScan()
 * 中也能获取到，为了可靠还是加上了此类). </br>
 * 
 * 2. 其他说明: @SpringBootFeignClients 中引用 @FeignClients 目的是为了兼容springcloud环境，如，
 * 这样可以实现 springboot + istio 架构与 springCloud无缝迁移，所有api上的注解可共用，只需更换pom依赖及配置。
 */
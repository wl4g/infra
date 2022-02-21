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
package com.wl4g.component.integration.dubbo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Scans for interfaces that declare they are feign clients (via
 * {@link FeignClient <code>@FeignClient</code>}). Configures component scanning
 * directives for use with
 * {@link org.springframework.context.annotation.Configuration
 * <code>@Configuration</code>} classes.</br>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author Spencer Gibb
 * @author Dave Syer
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see Implementation simulated of refer:
 *      {@link org.springframework.cloud.openfeign.EnableFeignClients}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ FeignDubboAutoConfiguration.class })
public @interface EnableFeignDubboProvider {
}
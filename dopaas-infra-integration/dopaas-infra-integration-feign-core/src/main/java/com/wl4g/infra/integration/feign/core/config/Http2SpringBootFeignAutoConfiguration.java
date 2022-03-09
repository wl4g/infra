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
package com.wl4g.infra.integration.feign.core.config;

//import static com.wl4g.infra.integration.feign.core.config.FeignConsumerAutoConfiguration.BEAN_DEFAULT_FEIGN_CLIENT;
//import static com.wl4g.infra.integration.feign.core.constant.FeignConsumerConstant.KEY_CLIENT_PRIVODER;
//
//import java.time.Duration;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
//import org.springframework.boot.system.JavaVersion;
//import org.springframework.context.annotation.Bean;
//
//import feign.http2client.Http2Client;
//
//@ConditionalOnExpression("'http2'.equalsIgnoreCase('${" + KEY_CLIENT_PRIVODER + ":http2}')")
// @ConditionalOnClass(HttpClient.class)
public class Http2SpringBootFeignAutoConfiguration {

    // @Bean(BEAN_DEFAULT_FEIGN_CLIENT)
    // @ConditionalOnJava(JavaVersion.ELEVEN)
    // @ConditionalOnClass(HttpClient.class)
    // public Client http2FeignClient(FeignConsumerProperties config) {
    // HttpClient httpClient = HttpClient.newBuilder()
    // .followRedirects(HttpClient.Redirect.ALWAYS)
    // .version(HttpClient.Version.HTTP_2)
    // .connectTimeout(Duration.ofMillis(config.getConnectTimeout()))
    // .build();
    // return new Http2Client(httpClient);
    // }

}
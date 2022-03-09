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

import static com.wl4g.infra.integration.feign.core.config.SpringBootFeignAutoConfiguration.BEAN_DEFAULT_FEIGN_CLIENT;
import static com.wl4g.infra.integration.feign.core.constant.FeignConsumerConstant.KEY_CLIENT_PRIVODER;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;

import feign.Client;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

@ConditionalOnExpression("'okhttp3'.equalsIgnoreCase('${" + KEY_CLIENT_PRIVODER + ":okhttp3}')")
@ConditionalOnClass(OkHttpClient.class)
public class OkhttpSpringBootFeignAutoConfiguration {

    @Bean(BEAN_OKHTTP3_POOL)
    public ConnectionPool okHttp3ConnectionPool(FeignConsumerProperties config) {
        return new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), MINUTES);
    }

    @Bean(BEAN_DEFAULT_FEIGN_CLIENT)
    public Client feignOkHttp3Client(FeignConsumerProperties config, @Qualifier(BEAN_OKHTTP3_POOL) ConnectionPool pool) {
        OkHttpClient delegate = new OkHttpClient().newBuilder()
                .connectionPool(pool)
                .connectTimeout(config.getConnectTimeout(), MILLISECONDS)
                .readTimeout(config.getReadTimeout(), MILLISECONDS)
                .writeTimeout(config.getWriteTimeout(), MILLISECONDS)
                .build();
        return new feign.okhttp.OkHttpClient(delegate);
    }

    private static final String BEAN_OKHTTP3_POOL = "infraSpringBootFeign.okhttp3Pool";

}
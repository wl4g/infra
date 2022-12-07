/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
package com.wl4g.infra.common.minio;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * {@link OkHttpClientConfig}
 * 
 * @author James Wong
 * @version 2022-09-18
 * @since v3.0.0
 */
@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
public class OkHttpClientConfig {
    private @Default Duration connectTimeout = Duration.ofSeconds(10);
    private @Default Duration writeTimeout = Duration.ofMinutes(5);
    private @Default Duration readTimeout = Duration.ofMinutes(5);
    private @Default Proxy proxy = Proxy.NO_PROXY;
    // private int maxIdleConnections = 1024;
    // private int idleConnectionCount = 10;
    // private Duration keepAliveDuration = Duration.ofMillis(15);

    public OkHttpClient newOkHttpClient() {
        return new OkHttpClient().newBuilder()
                .connectTimeout(connectTimeout.toMillis(), MILLISECONDS)
                .writeTimeout(writeTimeout.toMillis(), MILLISECONDS)
                .readTimeout(readTimeout.toMillis(), MILLISECONDS)
                .proxy(proxy)
                .protocols(asList(Protocol.HTTP_1_1, Protocol.HTTP_2, Protocol.QUIC))
                .build();
    }

    public static final Proxy DEFAULT_PROXY = new Proxy(java.net.Proxy.Type.SOCKS, new InetSocketAddress("localhost", 8889));
}
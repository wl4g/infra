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

import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;

import feign.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link FeignSpringBootProperties}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@Getter
@Setter
@ToString
public class FeignSpringBootProperties {
    public static final long DEFAULT_CONNECT_TIMEOUT = 3 * 1000L;
    public static final long DEFAULT_READ_TIMEOUT = 6 * 1000L;
    public static final long DEFAULT_WRITE_TIMEOUT = 6 * 1000L;
    public static final boolean DEFAULT_FOLLOWREDIRECTS = true;

    /**
     * The default absolute base URL or resolvable hostname (the protocol is
     * optional). Will be used when not set in {@link FeignConsumer#url()}
     */
    private String defaultUrl;

    /**
     * The default request base URL, Will be used when not set in
     * {@link FeignConsumer#logLevel()}
     * 
     * @return
     */
    private Logger.Level defaultLogLevel = Logger.Level.NONE;

    /** Max Idle time of connections */
    private int maxIdleConnections = 200;

    /** The keep alive default is 5 minutes. */
    private long keepAliveDuration = 5;

    /** The connect timeout default is 10 seconds. */
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    /** The read timeout default is 10 seconds. */
    private long readTimeout = DEFAULT_READ_TIMEOUT;

    /** The write timeout default is 10 seconds. */
    private long writeTimeout = DEFAULT_WRITE_TIMEOUT;

    /** follow Redirects. */
    private boolean followRedirects;

}

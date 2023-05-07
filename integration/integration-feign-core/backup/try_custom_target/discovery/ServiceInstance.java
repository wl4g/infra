/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * James Wong <jameswong1376@gmail.com> Technology CO.LTD.
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
package com.wl4g.infra.integration.feign.core.discovery;

import java.net.URI;
import java.util.Map;

/**
 * Represents an instance of a service in a discovery system.
 */
public interface ServiceInstance {

    /**
     * The unique instance ID as registered.
     */
    default String getInstanceId() {
        return null;
    }

    /**
     * The service ID as registered.
     */
    String getServiceId();

    /**
     * The host-name of the registered service instance.
     */
    String getHost();

    /**
     * The port of the registered service instance.
     */
    int getPort();

    /**
     * Whether the port of the registered service instance uses HTTPS.
     */
    boolean isSecure();

    /**
     * The service URI address.
     */
    URI getUri();

    /**
     * The key / value pair meta-data associated with the service instance.
     */
    Map<String, String> getMetadata();

    /**
     * The scheme of the service instance.
     */
    default String getScheme() {
        return null;
    }

}

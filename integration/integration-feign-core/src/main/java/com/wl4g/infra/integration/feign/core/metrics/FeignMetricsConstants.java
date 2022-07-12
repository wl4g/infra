/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <wanglsir@gmail.com>
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
package com.wl4g.infra.integration.feign.core.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link FeignMetricsConstants}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-07-12
 * @since v3.0.0
 */
public class FeignMetricsConstants {

    @Getter
    @AllArgsConstructor
    public static enum MetricsName {

        //
        // .
        //

        SIMPLE_SIGN_BLOOM_SUCCESS_TOTAL("iscg_simple_sign_bloom_success_total",
                "The total number of success bloom validate for simple signature authenticator"),

        SIMPLE_SIGN_TIME("iscg_simple_sign_time", "The number of simple signature execution cost time");

        private final String name;
        private final String help;
    }

    public static abstract class MetricsTag {

        // for Common tags.

        public static final String ROUTE_ID = "routeId";

        public static final String SELF_INSTANCE_ID = "self";

        // for Security tags.

        public static final String SIGN_ALG = "alg";

    }

}

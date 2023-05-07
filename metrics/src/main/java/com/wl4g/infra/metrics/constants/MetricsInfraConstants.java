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
package com.wl4g.infra.metrics.constants;

import com.wl4g.infra.core.constant.CoreInfraConstants;

/**
 * {@link MetricsConstants}
 * 
 * @author &lt;James Wong James Wong <jameswong1376@gmail.com>&gt;
 * @version 2022-07-12
 * @since v3.0.0
 */
public class MetricsInfraConstants extends CoreInfraConstants {

    public static final String CONF_PREFIX_INFRA_METRICS = CONF_PREFIX_INFRA + ".metrics";

    public static final String CONF_PREFIX_INFRA_UTIL = CONF_PREFIX_INFRA_METRICS + ".utils";

    public static final String CONF_PREFIX_INFRA_TIMED = CONF_PREFIX_INFRA_METRICS + ".timed";

    public static final String CONF_PREFIX_INFRA_COUNTER = CONF_PREFIX_INFRA_METRICS + ".counter";

    public static final String CONF_PREFIX_INFRA_COLLECTOR_TIMING = CONF_PREFIX_INFRA_METRICS + ".collector.timing";

    public static final String CONF_PREFIX_INFRA_HEALTH_TIMEED = CONF_PREFIX_INFRA_METRICS + ".health.timed";

}

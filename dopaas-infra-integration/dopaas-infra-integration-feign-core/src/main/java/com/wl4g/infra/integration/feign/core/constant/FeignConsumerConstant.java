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
package com.wl4g.infra.integration.feign.core.constant;

import com.wl4g.infra.core.constant.BaseConstants;

/**
 * {@link FeignConsumerConstant}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-14
 * @sine v1.0
 * @see
 */
public abstract class FeignConsumerConstant extends BaseConstants {

    public static final String CONFIG_PREFIX_FEIGN = "spring.infra.integration.feign";

    public static final String CONFIG_PREFIX_FEIGN_ENABLE = CONFIG_PREFIX_FEIGN + ".enabled";

    public static final String CONFIG_PREFIX_FEIGN_CLIENT_PRIVODER = CONFIG_PREFIX_FEIGN + ".client-provider";

    public static final String CONFIG_PREFIX_FEIGN_PLUGIN_CLASSES = CONFIG_PREFIX_FEIGN + ".plugin-classes";

    // For example, Tomcat 8.0 allows a maximum of 8KB of HTTP request headers
    // by default
    public static final long RPC_ATTACTMENT_MAX_BYTES = getLongProperty(CONFIG_PREFIX_FEIGN + ".context.attachments-max-bytes",
            6144L);

}

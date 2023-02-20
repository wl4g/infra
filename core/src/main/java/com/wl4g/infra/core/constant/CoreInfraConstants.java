/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <James Wong@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License";
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
package com.wl4g.infra.core.constant;

import com.wl4g.infra.context.constant.ContextInfraConstants;

/**
 * {@link CoreInfraConstants}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2020-12-31
 * @sine v1.0
 * @see
 */
public abstract class CoreInfraConstants extends ContextInfraConstants {

    // Configuration key.

    public static final String CONF_PREFIX_INFRA_CORE = CONF_PREFIX_INFRA + ".core";

    public static final String CONF_PREFIX_INFRA_CORE_SMART_PROXY = CONF_PREFIX_INFRA_CORE + ".smart-proxies";

    public static final String CONF_PREFIX_INFRA_CORE_WEB_HUMAN_DATE_CONVERTER = CONF_PREFIX_INFRA_CORE
            + ".web.human-date-converter";

    public static final String CONF_PREFIX_INFRA_CORE_WEB_GLOBAL_ERROR = CONF_PREFIX_INFRA_CORE + ".web.global-error";

    public static final String CONF_PREFIX_INFRA_CORE_WEB_EMBED_WEBAPP = CONF_PREFIX_INFRA_CORE + ".web.embedded-webapps";

    public static final String CONF_PREFIX_INFRA_CONTEXT_HTTP_REMOTE = CONF_PREFIX_INFRA_CONTEXT + ".http-remote";
}

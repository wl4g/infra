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
package com.wl4g.infra.context.constant;

import com.wl4g.infra.common.lang.EnvironmentUtil;

/**
 * {@link ContextInfraConstants}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2020-12-31
 * @sine v1.0
 * @see
 */
public abstract class ContextInfraConstants extends EnvironmentUtil {

    // Configuration key.

    public static final String CONF_PREFIX_INFRA = "spring.infra";

    public static final String CONF_PREFIX_INFRA_CONTEXT = CONF_PREFIX_INFRA + ".context";

    public static final String CONF_PREFIX_INFRA_CONTEXT_BOOTSTRAPPING = CONF_PREFIX_INFRA_CONTEXT + ".bootstrapping";

    public static final String CONF_PREFIX_INFRA_CONTEXT_LOGGING = CONF_PREFIX_INFRA_CONTEXT + ".logging";

    public static final String CONF_PREFIX_INFRA_CONTEXT_TRACE = CONF_PREFIX_INFRA_CONTEXT + ".trace";

    public static final String CONF_PREFIX_INFRA_CORE_NAMING_PROTOYPE = CONF_PREFIX_INFRA_CONTEXT_TRACE + ".naming-beanfactory";

    public static final String CONF_PREFIX_INFRA_CORE_OPERATOR = CONF_PREFIX_INFRA_CONTEXT_TRACE + ".generic-operator";

    /**
     * The alias for OpenTracing 'traceId'.
     */
    public static final String TRACE_REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * The alias for OpenTracing 'spanId'.
     */
    public static final String TRACE_REQUEST_SEQ_HEADER = "X-Request-Seq";

    /**
     * Reactive trace webFilter order. Note: If it is integrated in the
     * Iam-Gateway project, you need to take care of him, hereby the default
     * definition is: -50
     */
    public static final int TRACE_ORDER = getIntProperty("TRACE_ORDER", -50);

}

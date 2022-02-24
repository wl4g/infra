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
package com.wl4g.infra.support.constant;

import com.wl4g.infra.core.constant.BaseConstant;

/**
 * {@link SupportConfigConstant}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-20
 * @sine v1.0
 * @see
 */
public abstract class SupportConfigConstant extends BaseConstant {

    public static final String KEY_PREFIX_MODULE_SUPPORT = "spring.dopaas-infra.support.";

    public static final String KEY_SUPPORT_CLI_PREFIX = KEY_PREFIX_MODULE_SUPPORT + "cli";

    public static final String KEY_SUPPORT_NOTIFY_PREFIX = KEY_PREFIX_MODULE_SUPPORT + "notification";

    public static final String KEY_SUPPORT_JEDIS_PREFIX = KEY_PREFIX_MODULE_SUPPORT + "jedis";

}

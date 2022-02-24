/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
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

/**
 * {@link CoreConfigConstant}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-31
 * @sine v1.0
 * @see
 */
public abstract class CoreConfigConstant extends BaseConstant {

    public static final String KEY_PREFIX_MODULE_CORE = "spring.infra.core.";

    public static final String KEY_BOOTSTRAPPING = KEY_PREFIX_MODULE_CORE + "bootstrapping";

    public static final String KEY_NAMING_PROTOYPE_FACTORY = KEY_PREFIX_MODULE_CORE + "naming-beanfactory";

    public static final String KEY_GENERIC_OPERATOR = KEY_PREFIX_MODULE_CORE + "generic-operator";

    public static final String KEY_SMART_PROXY = KEY_PREFIX_MODULE_CORE + "smart-proxies";

    public static final String KEY_WEB_HUMAN_DATE_CONVERTER = KEY_PREFIX_MODULE_CORE + "web.human-date-converter";

    public static final String KEY_WEB_GLOBAL_ERROR = KEY_PREFIX_MODULE_CORE + "web.global-error";

    public static final String KEY_WEB_EMBEDDED_WEBAPP = KEY_PREFIX_MODULE_CORE + "web.embedded-webapps";

    public static final String KEY_REMOTE_CLIENT = KEY_PREFIX_MODULE_CORE + "remote";

}

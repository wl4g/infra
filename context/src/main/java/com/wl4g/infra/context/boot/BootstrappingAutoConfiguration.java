/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.context.boot;

import static com.wl4g.infra.context.constant.ContextInfraConstants.CONF_PREFIX_INFRA_CORE_BOOTSTRAPPING;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import com.wl4g.infra.common.web.rest.RespBase.ErrorPromptMessageBuilder;

/**
 * System boot defaults auto configuration.
 * 
 * @author James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2020年2月20日
 * @since
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ConditionalOnProperty(name = CONF_PREFIX_INFRA_CORE_BOOTSTRAPPING + ".enabled", matchIfMissing = true)
public class BootstrappingAutoConfiguration implements InitializingBean {

    private @Autowired ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        initGlobalErrorPrompt(applicationContext.getEnvironment());
    }

    /**
     * Initializing API error prompt.
     * 
     * @param environment
     */
    protected void initGlobalErrorPrompt(Environment environment) {
        String appName = environment.getRequiredProperty("spring.application.name");
        if (appName.length() < DEFAULT_PROMPT_MAX_LENGTH) {
            ErrorPromptMessageBuilder.setPrompt(appName);
        } else {
            ErrorPromptMessageBuilder.setPrompt(appName.substring(0, 4));
        }
    }

    // --- C U S T O M A T I O N _ S E R V L E T _ C O N T A I N E R. ---

    /**
     * API prompt max length.
     */
    final private static int DEFAULT_PROMPT_MAX_LENGTH = 4;

}
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
package com.wl4g.infra.common.notification;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.log.SmartLogger;

/**
 * {@link AbstractMessageNotifier}
 *
 * @param <C>
 * @param <T>
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v1.0.0
 * @see
 */
public abstract class AbstractMessageNotifier<C extends NotifyProperties> implements MessageNotifier {

    protected final SmartLogger log = getLogger(getClass());

    /**
     * Notify properties.
     */
    protected @NotNull final C config;

    protected @Nullable final Validator validator;

    public AbstractMessageNotifier(final @NotNull C config, final @Nullable Validator validator) {
        this.config = notNullOf(config, "config");
        // this.validator = notNullOf(validator, "validator");
        this.validator = validator;
    }

    @PostConstruct
    public void init() throws Exception {
    }

    @Override
    public boolean preHandle(Method method, Object[] args) {
        if (nonNull(args) && nonNull(validator)) {
            for (Object arg : args) {
                validator.validate(arg);
            }
        }

        // Check notify message templateKey
        if (config instanceof AbstractNotifyProperties) {
            AbstractNotifyProperties conf = (AbstractNotifyProperties) config;
            for (Object arg : args) {
                if (arg instanceof GenericNotifyMessage) {
                    GenericNotifyMessage msg = (GenericNotifyMessage) arg;
                    // No such templateKey?
                    if (!conf.hasTemplateKey(msg.getTemplateKey())) {
                        log.warn("No such notification template key of: {}", msg.getTemplateKey());
                        return false;
                    }
                    break;
                }
            }
        }

        return true;
    }

}
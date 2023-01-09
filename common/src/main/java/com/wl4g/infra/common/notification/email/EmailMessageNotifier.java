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
package com.wl4g.infra.common.notification.email;

import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.annotation.Stable;
import com.wl4g.infra.common.notification.AbstractMessageNotifier;
import com.wl4g.infra.common.notification.GenericNotifierParam;
import com.wl4g.infra.common.notification.email.internal.EmailSenderAPI;
import com.wl4g.infra.common.notification.email.internal.JavaMailSender;

/**
 * {@link EmailMessageNotifier}, Full compatibility with native spring mail!
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v1.0.0
 * @see
 */
@Stable
public class EmailMessageNotifier extends AbstractMessageNotifier<EmailNotifierProperties> {

    /**
     * Java mail sender.
     */
    protected final JavaMailSender mailSender;

    public EmailMessageNotifier(EmailNotifierProperties config, Validator validator) {
        super(config, validator);
        this.mailSender = EmailSenderAPI.buildSender(config);
    }

    @Override
    public NotifierKind kind() {
        return NotifierKind.EMAIL;
    }

    @Override
    public Object send(final @NotNull GenericNotifierParam msg) {
        return EmailSenderAPI.send(mailSender, config, msg);
    }

}
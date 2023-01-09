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
package com.wl4g.infra.common.notification.apns;

import javax.validation.Validator;

import com.wl4g.infra.common.annotation.Todo;
import com.wl4g.infra.common.notification.AbstractMessageNotifier;
import com.wl4g.infra.common.notification.GenericNotifierParam;

@Todo
public class ApnsMessageNotifier extends AbstractMessageNotifier<ApnsNotifierProperties> {

    public ApnsMessageNotifier(ApnsNotifierProperties config, Validator validator) {
        super(config, validator);
    }

    @Override
    public NotifierKind kind() {
        return NotifierKind.APNS;
    }

    @Override
    public Object send(GenericNotifierParam message) {
        throw new UnsupportedOperationException();
    }

}
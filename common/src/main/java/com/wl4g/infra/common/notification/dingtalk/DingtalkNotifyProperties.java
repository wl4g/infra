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
package com.wl4g.infra.common.notification.dingtalk;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;

import java.util.Properties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.wl4g.infra.common.notification.NotifyProperties;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * {@link DingtalkNotifyProperties}
 * 
 * @author James Wong
 * @version 2022-09-15
 * @since v3.0.0
 * @see https://open-dev.dingtalk.com/apiExplorer?#/?devType=org&api=dingtalk.oapi.gettoken
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class DingtalkNotifyProperties implements NotifyProperties {

    private String agentId;
    private @NotBlank String appKey;
    private @NotBlank String appSecret;
    private @NotEmpty @Default Properties templates = new Properties();

    @Override
    public void validate() {
        hasTextOf(appKey, "appKey");
        hasTextOf(appSecret, "appSecret");
    }

}
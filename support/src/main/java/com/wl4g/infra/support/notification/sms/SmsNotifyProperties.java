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
package com.wl4g.infra.support.notification.sms;

import javax.validation.constraints.NotBlank;

import com.wl4g.infra.support.notification.AbstractNotifyProperties;
import com.wl4g.infra.support.notification.NotifyProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link SmsNotifyProperties}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v1.0.0
 * @see
 */
@Getter
@Setter
public class SmsNotifyProperties implements NotifyProperties {

    private AliyunSmsNotifyProperties aliyun = new AliyunSmsNotifyProperties();

    @Override
    public void validate() {
    }

    @Getter
    @Setter
    public static class AliyunSmsNotifyProperties extends AbstractNotifyProperties {
        private @NotBlank String regionId = "cn-hangzhou";
        // private @NotBlank String product = "Dysmsapi";
        // private @NotBlank String domain = "dysmsapi.aliyuncs.com";
        private @NotBlank String accessKeyId;
        private @NotBlank String accessKeySecret;
        private @NotBlank String signName;
        private @NotBlank String defaultConnectTimeout = "5_000";
        private @NotBlank String defaultReadTimeout = "10_000";
    }

}
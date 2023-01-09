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

import javax.validation.Validator;

import com.wl4g.infra.common.notification.apns.ApnsMessageNotifier;
import com.wl4g.infra.common.notification.apns.ApnsNotifierProperties;
import com.wl4g.infra.common.notification.bark.BarkMessageNotifier;
import com.wl4g.infra.common.notification.bark.BarkNotifierProperties;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier;
import com.wl4g.infra.common.notification.dingtalk.DingtalkNotifierProperties;
import com.wl4g.infra.common.notification.email.EmailMessageNotifier;
import com.wl4g.infra.common.notification.email.EmailNotifierProperties;
import com.wl4g.infra.common.notification.facebook.FacebookMessageNotifier;
import com.wl4g.infra.common.notification.facebook.FacebookNotifierProperties;
import com.wl4g.infra.common.notification.qq.QqMessageNotifier;
import com.wl4g.infra.common.notification.qq.QqNotifierProperties;
import com.wl4g.infra.common.notification.sms.AliyunSmsMessageNotifier;
import com.wl4g.infra.common.notification.sms.SmsNotifierProperties;
import com.wl4g.infra.common.notification.twitter.TwitterMessageNotifier;
import com.wl4g.infra.common.notification.twitter.TwitterNotifierProperties;
import com.wl4g.infra.common.notification.vms.AliyunVmsMessageNotifier;
import com.wl4g.infra.common.notification.vms.VmsNotifierProperties;
import com.wl4g.infra.common.notification.wechat.WechatMessageNotifier;
import com.wl4g.infra.common.notification.wechat.WechatNotifierProperties;

/**
 * Notification message service auto configuration
 * 
 * @author James Wong
 * @version 2019-09-28
 * @since v2.0.0
 */
public class NotificationBuilder {

    public ApnsMessageNotifier buildApnsMessageNotifier(ApnsNotifierProperties config, Validator validator) {
        return new ApnsMessageNotifier(config, validator);
    }

    public BarkMessageNotifier barkMessageNotifier(BarkNotifierProperties config, Validator validator) {
        return new BarkMessageNotifier(config, validator);
    }

    public DingtalkMessageNotifier dingtalkMessageNotifier(DingtalkNotifierProperties config, Validator validator) {
        return new DingtalkMessageNotifier(config, validator);
    }

    public FacebookMessageNotifier facebookMessageNotifier(FacebookNotifierProperties config, Validator validator) {
        return new FacebookMessageNotifier(config, validator);
    }

    public EmailMessageNotifier emailMessageNotifier(EmailNotifierProperties config, Validator validator) {
        return new EmailMessageNotifier(config, validator);
    }

    public QqMessageNotifier qqMessageNotifier(QqNotifierProperties config, Validator validator) {
        return new QqMessageNotifier(config, validator);
    }

    public AliyunSmsMessageNotifier aliyunSmsMessageNotifier(SmsNotifierProperties config, Validator validator) {
        return new AliyunSmsMessageNotifier(config, validator);
    }

    public AliyunVmsMessageNotifier aliyunVmsMessageNotifier(VmsNotifierProperties config, Validator validator) {
        return new AliyunVmsMessageNotifier(config, validator);
    }

    public WechatMessageNotifier wechatMessageNotifier(WechatNotifierProperties config, Validator validator) {
        return new WechatMessageNotifier(config, validator);
    }

    public TwitterMessageNotifier twitterMessageNotifier(TwitterNotifierProperties config, Validator validator) {
        return new TwitterMessageNotifier(config, validator);
    }

    public NoneMessageNotifier buildNoneMessageNotifier() {
        return new NoneMessageNotifier();
    }

}
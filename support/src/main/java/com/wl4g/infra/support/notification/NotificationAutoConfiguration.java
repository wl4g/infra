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
package com.wl4g.infra.support.notification;

import static com.wl4g.infra.common.notification.NoneMessageNotifier.DEFAULT_NO_OP;
import static com.wl4g.infra.support.constant.SupportInfraConstant.CONF_PREFIX_INFRA_SUPPORT_NOTIFY;
import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.validation.Validator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.infra.common.framework.operator.GenericOperatorAdapter;
import com.wl4g.infra.common.notification.MessageNotifier;
import com.wl4g.infra.common.notification.MessageNotifier.NotifierKind;
import com.wl4g.infra.common.notification.apns.ApnsMessageNotifier;
import com.wl4g.infra.common.notification.apns.ApnsNotifyProperties;
import com.wl4g.infra.common.notification.bark.BarkMessageNotifier;
import com.wl4g.infra.common.notification.bark.BarkNotifyProperties;
import com.wl4g.infra.common.notification.dingtalk.DingtalkMessageNotifier;
import com.wl4g.infra.common.notification.dingtalk.DingtalkNotifyProperties;
import com.wl4g.infra.common.notification.email.EmailMessageNotifier;
import com.wl4g.infra.common.notification.email.EmailNotifyProperties;
import com.wl4g.infra.common.notification.facebook.FacebookMessageNotifier;
import com.wl4g.infra.common.notification.facebook.FacebookNotifyProperties;
import com.wl4g.infra.common.notification.qq.QqMessageNotifier;
import com.wl4g.infra.common.notification.qq.QqNotifyProperties;
import com.wl4g.infra.common.notification.sms.AliyunSmsMessageNotifier;
import com.wl4g.infra.common.notification.sms.SmsNotifyProperties;
import com.wl4g.infra.common.notification.twitter.TwitterMessageNotifier;
import com.wl4g.infra.common.notification.twitter.TwitterNotifyProperties;
import com.wl4g.infra.common.notification.vms.AliyunVmsMessageNotifier;
import com.wl4g.infra.common.notification.vms.VmsNotifyProperties;
import com.wl4g.infra.common.notification.wechat.WechatMessageNotifier;
import com.wl4g.infra.common.notification.wechat.WechatNotifyProperties;

/**
 * Notification message service auto configuration
 * 
 * @author James Wong
 * @version 2019-09-28
 * @since v2.0.0
 */
public class NotificationAutoConfiguration {

    //
    // --- Notify properties. ---
    //

    @Bean(name = "apnsNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".apns.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".apns")
    public ApnsNotifyProperties apnsNotifyProperties() {
        return new ApnsNotifyProperties();
    }

    @Bean(name = "barkNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".bark.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".bark")
    public BarkNotifyProperties barkNotifyProperties() {
        return new BarkNotifyProperties();
    }

    @Bean(name = "dingtalkNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".dingtalk.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".dingtalk")
    public DingtalkNotifyProperties dingtalkNotifyProperties() {
        return new DingtalkNotifyProperties();
    }

    @Bean(name = "facebookNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".facebook.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".facebook")
    public FacebookNotifyProperties facebookNotifyProperties() {
        return new FacebookNotifyProperties();
    }

    @Bean(name = "mailNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".mail.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".mail")
    public EmailNotifyProperties emailNotifyProperties() {
        return new EmailNotifyProperties();
    }

    @Bean(name = "qqNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".qq.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".qq")
    public QqNotifyProperties qqNotifyProperties() {
        return new QqNotifyProperties();
    }

    @Bean(name = "smsNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".sms.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".sms")
    public SmsNotifyProperties smsNotifyProperties() {
        return new SmsNotifyProperties();
    }

    @Bean(name = "vmsNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".vms.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".vms")
    public VmsNotifyProperties vmsNotifyProperties() {
        return new VmsNotifyProperties();
    }

    @Bean(name = "wechatNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".wechat.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".wechat")
    public WechatNotifyProperties wechatNotifyProperties() {
        return new WechatNotifyProperties();
    }

    @Bean(name = "twitterNotifyProperties")
    @ConditionalOnProperty(name = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".twitter.enabled", matchIfMissing = false)
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_SUPPORT_NOTIFY + ".twitter")
    public TwitterNotifyProperties twitterNotifyProperties() {
        return new TwitterNotifyProperties();
    }

    //
    // --- Message notifier. ---
    //

    @Bean
    @ConditionalOnBean(ApnsNotifyProperties.class)
    public ApnsMessageNotifier apnsMessageNotifier(ApnsNotifyProperties config, Validator validator) {
        return new ApnsMessageNotifier(config, validator);
    }

    @Bean
    @ConditionalOnBean(BarkNotifyProperties.class)
    public BarkMessageNotifier barkMessageNotifier(BarkNotifyProperties config, Validator validator) {
        return new BarkMessageNotifier(config, validator);
    }

    @Bean
    @ConditionalOnBean(DingtalkNotifyProperties.class)
    public DingtalkMessageNotifier dingtalkMessageNotifier(DingtalkNotifyProperties config, Validator validator) {
        return new DingtalkMessageNotifier(config, validator);
    }

    @Bean
    @ConditionalOnBean(FacebookNotifyProperties.class)
    public FacebookMessageNotifier facebookMessageNotifier(FacebookNotifyProperties config, Validator validator) {
        return new FacebookMessageNotifier(config, validator);
    }

    @Bean
    @ConditionalOnBean(EmailNotifyProperties.class)
    public EmailMessageNotifier emailMessageNotifier(EmailNotifyProperties config, Validator validator) {
        return new EmailMessageNotifier(config, validator);
    }

    @Bean
    @ConditionalOnBean(QqNotifyProperties.class)
    public QqMessageNotifier qqMessageNotifier(QqNotifyProperties config, Validator validator) {
        return new QqMessageNotifier(config, validator);
    }

    @Bean
    @ConditionalOnBean(SmsNotifyProperties.class)
    public AliyunSmsMessageNotifier aliyunSmsMessageNotifier(SmsNotifyProperties config, Validator validator) {
        return new AliyunSmsMessageNotifier(config, validator);
    }

    @Bean
    @ConditionalOnBean(VmsNotifyProperties.class)
    public AliyunVmsMessageNotifier aliyunVmsMessageNotifier(VmsNotifyProperties config, Validator validator) {
        return new AliyunVmsMessageNotifier(config, validator);
    }

    @Bean
    @ConditionalOnBean(WechatNotifyProperties.class)
    public WechatMessageNotifier wechatMessageNotifier(WechatNotifyProperties config, Validator validator) {
        return new WechatMessageNotifier(config, validator);
    }

    @Bean
    @ConditionalOnBean(TwitterNotifyProperties.class)
    public TwitterMessageNotifier twitterMessageNotifier(TwitterNotifyProperties config, Validator validator) {
        return new TwitterMessageNotifier(config, validator);
    }

    /**
     * 1, Cannot inject at this time:
     * 
     * <pre>
     * &#64;Bean
     * public CompositeMessageNotifier compositeMessageNotifier(List&lt;MessageNotifier&lt;NotifyMessage&gt;&gt; notifiers) {
     *   ...
     * }
     * </pre>
     * 
     * 2, Can operate correctly:
     * 
     * <pre>
     * &#64;Bean
     * public CompositeMessageNotifier compositeMessageNotifier(List&lt;MessageNotifier&lt;? extends NotifyMessage&gt;&gt; notifiers) {
     *   ...
     * }
     * </pre>
     * 
     * @param notifiers
     * @return
     */
    @Bean(BEAN_NOTIFIER_ADAPTER)
    @ConditionalOnBean(MessageNotifier.class)
    public GenericOperatorAdapter<NotifierKind, MessageNotifier> compositeMessageNotifierAdapter(
            List<MessageNotifier> notifiers) {
        return new GenericOperatorAdapter<NotifierKind, MessageNotifier>(
                notifiers.stream().map(n -> ((MessageNotifier) n)).collect(toList()), DEFAULT_NO_OP) {
        };
    }

    /**
     * Default NONE message notifier.
     */
    @Bean
    @ConditionalOnMissingBean(name = BEAN_NOTIFIER_ADAPTER)
    public NoneMessageNotifierAdapter noneCompositeMessageNotifierAdapter() {
        return new NoneMessageNotifierAdapter();
    }

    /**
     * {@link NoneMessageNotifierAdapter}
     *
     * @author Wangl.sir James Wong <jameswong1376@gmail.com>>
     * @version v1.0 2020年6月4日
     * @since
     */
    public static class NoneMessageNotifierAdapter extends GenericOperatorAdapter<NotifierKind, MessageNotifier> {

        public NoneMessageNotifierAdapter() {
            super(DEFAULT_NO_OP);
        }
    }

    public static final String BEAN_NOTIFIER_ADAPTER = "compositeMessageNotifierAdapter";

}
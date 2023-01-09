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

import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.framework.operator.Operator;
import com.wl4g.infra.common.notification.MessageNotifier.NotifierKind;
import com.wl4g.infra.common.notification.email.EmailMessageNotifier;

/**
 * <b>- Use for example:</b>
 * 
 * {@link @Service} public class ExampleAlerter {
 *
 * // Injection message notifier adapter. {@link @Autowired} private
 * GenericOperatorAdapter&lt;{@link NotifierKind}, {@link MessageNotifier}&gt;
 * notifierAdapter;
 * 
 * public void doNotify() { {@link GenericNotifierParam} msg = new
 * {@link GenericNotifierParam}("wanglsir@gmail.com", "alaramTpl1");
 *
 * // Sets common parameters. <font color=red><b>(Required)</b></font>
 * msg.addParameter("appName", "bizService1"); msg.addParameter("status",
 * "DOWN"); msg.addParameter("cause", "Host.cpu.utilization > 200%"); // More
 * customize variables(Note: just match the template) ...
 *
 * // Sets mail special parameters. <font color= red><b>(Optional)</b></font>
 * //msg.addParameter({@link EmailMessageNotifier#KEY_MAILMSG_SUBJECT}, "This is
 * a test message");
 * //msg.addParameter({@link EmailMessageNotifier#KEY_MAILMSG_CC},
 * "test1@gmail.com");
 * //msg.addParameter({@link EmailMessageNotifier#KEY_MAILMSG_BCC},
 * "test2@qq.com");
 * //msg.addParameter({@link EmailMessageNotifier#KEY_MAILMSG_REPLYTO},
 * "test3@163.com");
 *
 * // Do sent
 * notifierAdapter.forOperator({@link NotifierKind#ALIYUN_SMS}).send(msg);
 * //notifierAdapter.forOperator({@link NotifierKind#EMAIL}).send(msg); // ... }
 * 
 * }
 * 
 * </pre>
 * 
 * @param <T>
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v1.0.0
 * @see
 */
public interface MessageNotifier extends Operator<NotifierKind> {

    NotifierKind kind();

    /**
     * Sending notification message.
     * 
     * @param msg
     */
    Object send(final @NotNull GenericNotifierParam msg);

    /**
     * Notification privoder kind.
     * 
     * @author James Wong &lt;jameswong1376@gmail.com&gt;
     * @version 2020年1月8日 v1.0.0
     * @see
     */
    public static enum NotifierKind {

        EMAIL,

        DINGTALK,

        FACEBOOK,

        ALIYUN_SMS,

        ALIYUN_VMS,

        WECHAT_MP,

        QQ,

        TWITTER,

        APNS,

        BARK,

        /**
         * MessageNotifier that must be instantiated. The default implementation
         * when all other message notifiers are not available solves the spring
         * bean injection problem.
         * 
         * @see {@link com.wl4g.infra.common.notification.NoneMessageNotifier}
         */
        NONE;

    }

}
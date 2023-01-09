/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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

import static com.wl4g.infra.common.lang.EnvironmentUtil.getIntProperty;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getStringProperty;

import org.junit.Test;

import com.wl4g.infra.common.notification.GenericNotifyMessage;

/**
 * {@link EmailMessageNotifierTests}
 * 
 * @author James Wong
 * @version 2023-01-09
 * @since v3.0.0
 */
public class EmailMessageNotifierTests {

    static String test_serveHost = getStringProperty("TEST_SERVE_HOST", "smtp.exmail.qq.com");
    static int test_servePort = getIntProperty("TEST_SERVE_PORT", 465);
    static String test_fromUser = getStringProperty("TEST_FROM_USER", "sysnotification01@xxxxxx.com");
    static String test_password = getStringProperty("TEST_PASSWORD", "CAGd9ZHZMCUHXEVV");
    static String test_toUser = getStringProperty("TEST_TO_USER", "983708408@qq.com");

    @Test
    public void testEmailSendSimpleMessage() {
        final EmailNotifyProperties config = new EmailNotifyProperties();
        config.setProtocol("smtp");
        config.setHost(test_serveHost);
        config.setPort(test_servePort);
        config.setUsername(test_fromUser);
        config.setPassword(test_password);
        final String testTemplateKey = "test_template";
        config.getTemplates().put(testTemplateKey, "This testing simple email message!!!");

        final EmailMessageNotifier notifier = new EmailMessageNotifier(config, null);

        final GenericNotifyMessage msg = new GenericNotifyMessage(test_toUser, testTemplateKey)
                .addParameter(EmailMessageNotifier.KEY_MAILMSG_TYPE, EmailMessageNotifier.VALUE_MAILMSG_SIMPLE)
                .addParameter(EmailMessageNotifier.KEY_MAILMSG_SUBJECT, "Testing Sender with Simple Msg");

        System.out.println("Sending ...");
        final Object result = notifier.send(msg);
        System.out.println(result);
    }

    @Test
    public void testEmailSendMimeMessage() {
        final EmailNotifyProperties config = new EmailNotifyProperties();
        config.setProtocol("smtp");
        config.setHost(test_serveHost);
        config.setPort(test_servePort);
        config.setUsername(test_fromUser);
        config.setPassword(test_password);
        final String testTemplateKey = "test_template";
        config.getTemplates()
                .put(testTemplateKey,
                        "<h1>This testing mime email message!!!</h1></br><p><font color=red>It's a red word.</font></p>");

        final EmailMessageNotifier notifier = new EmailMessageNotifier(config, null);

        final GenericNotifyMessage msg = new GenericNotifyMessage(test_toUser, testTemplateKey)
                .addParameter(EmailMessageNotifier.KEY_MAILMSG_TYPE, EmailMessageNotifier.VALUE_MAILMSG_MIME)
                .addParameter(EmailMessageNotifier.KEY_MAILMSG_SUBJECT, "Testing Sender with Mime Msg");

        System.out.println("Sending ...");
        final Object result = notifier.send(msg);
        System.out.println(result);
    }

}

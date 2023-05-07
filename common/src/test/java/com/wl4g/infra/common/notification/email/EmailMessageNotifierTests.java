/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong James Wong <jameswong1376@gmail.com>
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

import com.wl4g.infra.common.notification.GenericNotifierParam;
import com.wl4g.infra.common.notification.email.internal.EmailSenderAPI;

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
        final EmailNotifierProperties config = new EmailNotifierProperties();
        config.setProtocol("smtp");
        config.setHost(test_serveHost);
        config.setPort(test_servePort);
        config.setUsername(test_fromUser);
        config.setPassword(test_password);
        final String testTemplateKey = "test_template";
        config.getTemplates().put(testTemplateKey, "This testing simple email message!!!");

        final EmailMessageNotifier notifier = new EmailMessageNotifier(config, null);

        final GenericNotifierParam msg = new GenericNotifierParam(test_toUser, testTemplateKey)
                .addParameter(EmailSenderAPI.KEY_MAIL_TYPE, EmailSenderAPI.VALUE_MAIL_SIMPLE)
                .addParameter(EmailSenderAPI.KEY_MAIL_SUBJECT, "Testing Sender(simple)");

        System.out.println("Sending ...");
        final Object result = notifier.send(msg);
        System.out.println(result);
    }

    @Test
    public void testEmailSendMimeMessage() { 
        final EmailNotifierProperties config = new EmailNotifierProperties();
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

        final GenericNotifierParam msg = new GenericNotifierParam(test_toUser, testTemplateKey)
                .addParameter(EmailSenderAPI.KEY_MAIL_TYPE, EmailSenderAPI.VALUE_MAIL_MIME)
                .addParameter(EmailSenderAPI.KEY_MAIL_SUBJECT, "Testing Sender(mime)");

        System.out.println("Sending ...");
        final Object result = notifier.send(msg);
        System.out.println(result);
    }

}

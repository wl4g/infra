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

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.annotation.Stable;
import com.wl4g.infra.common.notification.AbstractMessageNotifier;
import com.wl4g.infra.common.notification.GenericNotifyMessage;
import com.wl4g.infra.common.notification.email.internal.JavaMailSenderImpl;
import com.wl4g.infra.common.notification.email.internal.MimeMailMessage;
import com.wl4g.infra.common.notification.email.internal.MimeMessageHelper;
import com.wl4g.infra.common.notification.email.internal.SimpleMailMessage;

/**
 * {@link EmailMessageNotifier}, Full compatibility with native spring mail!
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v1.0.0
 * @see
 */
@Stable
public class EmailMessageNotifier extends AbstractMessageNotifier<EmailNotifyProperties> {

    /**
     * Java mail sender.
     */
    protected final JavaMailSenderImpl mailSender;

    public EmailMessageNotifier(EmailNotifyProperties config, Validator validator) {
        super(config, validator);
        this.mailSender = new JavaMailSenderImpl();
        if (!isNull(config.getProperties())) {
            final Properties settings = new Properties(DEFAULT_PROPERTIES);
            settings.putAll(config.getProperties());
            this.mailSender.setJavaMailProperties(settings);
        }
        this.mailSender.setDefaultEncoding(config.getDefaultEncoding().name());
        this.mailSender.setProtocol(config.getProtocol());
        this.mailSender.setHost(config.getHost());
        this.mailSender.setPort(config.getPort());
        this.mailSender.setUsername(config.getUsername());
        this.mailSender.setPassword(config.getPassword());
    }

    @Override
    public NotifierKind kind() {
        return NotifierKind.EMAIL;
    }

    @Override
    public Object send(final @NotNull GenericNotifyMessage msg) {
        notNullOf(msg, "msg");

        final String mailMsgType = msg.getParameterAsString(KEY_MAILMSG_TYPE, "simple");
        Object sendMsg = null;
        switch (mailMsgType) {
        case VALUE_MAILMSG_SIMPLE:
            final SimpleMailMessage simpleMsg = new SimpleMailMessage();
            // Add "<>" symbol to send out?
            /*
             * Preset from account, otherwise it would be wrong: 501 mail from
             * address must be same as authorization user.
             */
            simpleMsg.setFrom(config.getUsername() + "<" + config.getUsername() + ">");
            simpleMsg.setTo(
                    msg.getToObjects().stream().map(to -> to = to + "<" + to + ">").collect(toList()).toArray(new String[] {}));
            simpleMsg.setSubject(msg.getParameterAsString(KEY_MAILMSG_SUBJECT, "Unnamed Subject Message"));
            simpleMsg.setSentDate(msg.getParameter(KEY_MSG_SENDDATE, new Date()));
            simpleMsg.setBcc(safeList(msg.getParameter(KEY_MAILMSG_BCC)).toArray(new String[] {}));
            simpleMsg.setCc(safeList(msg.getParameter(KEY_MAILMSG_CC)).toArray(new String[] {}));
            simpleMsg.setReplyTo(msg.getParameter(KEY_MAILMSG_REPLYTO));
            simpleMsg.setText(config.resolveMessage(msg.getTemplateKey(), msg.getParameters()));
            sendMsg = simpleMsg;

            mailSender.send(simpleMsg);
            break;
        case VALUE_MAILMSG_MIME:
            try {
                final MimeMessage mimeMsg = mailSender.createMimeMessage();
                final MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, "utf-8");
                helper.setFrom(config.getUsername() + "<" + config.getUsername() + ">");
                helper.setTo(msg.getToObjects()
                        .stream()
                        .map(to -> to = to + "<" + to + ">")
                        .collect(toList())
                        .toArray(new String[] {}));
                helper.setSubject(msg.getParameterAsString(KEY_MAILMSG_SUBJECT, "Unnamed Subject Message"));
                // Use this or below line
                // mimeMessage.setContent(htmlMsg, "text/html");
                // Use this or above line.
                helper.setText(config.resolveMessage(msg.getTemplateKey(), msg.getParameters()), true);
                sendMsg = helper;

                mailSender.send(mimeMsg);
            } catch (MessagingException e) {
                throw new IllegalStateException(e);
            }
            break;
        default:
            throw new UnsupportedOperationException(format("No supported email message type of %s", mailMsgType));
        }
        return sendMsg;
    }

    /**
     * <pre>
     * <b>simple</b> => {@link SimpleMailMessage}
     * <b>mime</b> => {@link MimeMailMessage}
     * </pre>
     */
    public static final String KEY_MAILMSG_TYPE = "emailMsgType";
    public static final String VALUE_MAILMSG_SIMPLE = "SIMPLE";
    public static final String VALUE_MAILMSG_MIME = "MIME";

    public static final String KEY_MAILMSG_SUBJECT = "emailMsgSubject";
    public static final String KEY_MAILMSG_REPLYTO = "emailMsgReplyTo";
    public static final String KEY_MAILMSG_CC = "emailMsgCc";
    public static final String KEY_MAILMSG_BCC = "emailMsgBcc";

    public static final Properties DEFAULT_PROPERTIES = new Properties() {
        private static final long serialVersionUID = 1L;
        {
            put("mail.transport.portocol", "smtp");
            put("mail.smtp.host", "smtp.exmail.qq.com");
            put("mail.smtp.port", "465");
            put("mail.smtp.auth", "true");
            put("mail.smtp.timeout", "10000");
            put("mail.smtp.ssl.enable", "true");
            put("mail.imap.ssl.socketFactory.fallback", "false");
            put("mail.imap.ssl.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            put("mail.smtp.starttls.enable", "true");
            put("mail.smtp.starttls.required", "true");
            put("mail.smtp.ssl.portocols", "TLSv1.2");
        }
    };

}
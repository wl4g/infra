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
package com.wl4g.infra.common.notification.email.internal;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notEmptyOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Date;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.notification.GenericNotifierParam;
import com.wl4g.infra.common.notification.email.EmailNotifierProperties;

/**
 * {@link EmailSenderAPI}
 * 
 * @author James Wong
 * @version 2023-01-09
 * @since v3.0.0
 */
public class EmailSenderAPI {

    public static JavaMailSender buildSender(final @NotNull EmailNotifierProperties config) {
        notNullOf(config, "config");
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        if (!isNull(config.getProperties())) {
            final Properties settings = new Properties(DEFAULT_PROPERTIES);
            settings.putAll(config.getProperties());
            mailSender.setJavaMailProperties(settings);
        }
        mailSender.setDefaultEncoding(config.getDefaultEncoding());
        mailSender.setProtocol(config.getProtocol());
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());
        return mailSender;
    }

    public static Object send(
            final @NotNull JavaMailSender sender,
            final @NotNull EmailNotifierProperties config,
            final @NotNull GenericNotifierParam msg) {
        return send(sender, config, msg, null);
    }

    public static Object send(
            final @NotNull JavaMailSender sender,
            final @NotNull EmailNotifierProperties config,
            final @NotNull GenericNotifierParam msg,
            final @Nullable String message) {
        notNullOf(sender, "sender");
        notNullOf(config, "config");
        notNullOf(msg, "msg");

        final String mailMsgType = msg.getParameterAsString(KEY_MAIL_TYPE, VALUE_MAIL_SIMPLE);
        hasTextOf(mailMsgType, "mailMsgType");
        hasTextOf(config.getUsername(), "fromUser");
        final String fromUser = config.getUsername() + "<" + config.getUsername() + ">";
        final String subject = msg.getParameterAsString(KEY_MAIL_SUBJECT, "Unnamed Mail");
        final Date sentDate = msg.getParameter(KEY_MAIL_SENDDATE, new Date());
        final String[] toObjects = msg.getToObjects()
                .stream()
                .map(to -> to = to + "<" + to + ">")
                .collect(toList())
                .toArray(new String[] {});
        notEmptyOf(toObjects, "toObjects");
        final String[] bcc = safeList(msg.getParameter(KEY_MAIL_BCC)).toArray(new String[] {});
        final String[] cc = safeList(msg.getParameter(KEY_MAIL_CC)).toArray(new String[] {});
        final String content = isBlank(message) ? config.resolveMessage(msg.getTemplateKey(), msg.getParameters()) : message;
        final String replyTo = msg.getParameter(KEY_MAIL_REPLYTO);

        Object sendMsg = null;
        switch (mailMsgType) {
        case VALUE_MAIL_SIMPLE:
            final SimpleMailMessage simple = new SimpleMailMessage();
            // Add "<>" symbol to send out?
            /*
             * Preset from account, otherwise it would be wrong: 501 mail from
             * address must be same as authorization user.
             */
            simple.setFrom(fromUser);
            simple.setTo(toObjects);
            simple.setSubject(subject);
            simple.setSentDate(sentDate);
            simple.setBcc(bcc);
            simple.setCc(cc);
            if (isBlank(replyTo)) {
                simple.setReplyTo(replyTo);
            }
            simple.setText(content);
            sendMsg = simple;
            sender.send(simple);
            break;
        case VALUE_MAIL_MIME:
            try {
                final MimeMessage mime = sender.createMimeMessage();
                final MimeMessageHelper helper = new MimeMessageHelper(mime, "utf-8");
                helper.setFrom(fromUser);
                helper.setTo(toObjects);
                helper.setSubject(subject);
                helper.setSentDate(sentDate);
                helper.setBcc(bcc);
                helper.setCc(cc);
                if (isBlank(replyTo)) {
                    helper.setReplyTo(replyTo);
                }
                // Use this or below line
                // mimeMessage.setContent(htmlMsg, "text/html");
                // Use this or above line.
                helper.setText(content);
                sendMsg = helper;
                sender.send(mime);
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
    public static final String KEY_MAIL_TYPE = "msgType";
    public static final String VALUE_MAIL_SIMPLE = "SIMPLE";
    public static final String VALUE_MAIL_MIME = "MIME";

    public static final String KEY_MAIL_SUBJECT = "subject";
    public static final String KEY_MAIL_SENDDATE = "sendDate";
    public static final String KEY_MAIL_REPLYTO = "replyTo";
    public static final String KEY_MAIL_CC = "cc";
    public static final String KEY_MAIL_BCC = "bcc";

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

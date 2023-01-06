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
package com.wl4g.infra.common.notification.mail;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import java.util.Date;
import java.util.Properties;

import javax.validation.Validator;

import com.wl4g.infra.common.annotation.Stable;
import com.wl4g.infra.common.notification.AbstractMessageNotifier;
import com.wl4g.infra.common.notification.GenericNotifyMessage;
import com.wl4g.infra.common.notification.mail.internal.JavaMailSenderImpl;
import com.wl4g.infra.common.notification.mail.internal.MimeMailMessage;
import com.wl4g.infra.common.notification.mail.internal.SimpleMailMessage;

/**
 * {@link MailMessageNotifier}, Full compatibility with native spring mail!
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v1.0.0
 * @see
 */
@Stable
public class MailMessageNotifier extends AbstractMessageNotifier<MailNotifyProperties> {

    /**
     * Java mail sender.
     */
    protected final JavaMailSenderImpl mailSender;

    public MailMessageNotifier(MailNotifyProperties config, Validator validator) {
        super(config, validator);
        this.mailSender = new JavaMailSenderImpl();
        if (!isNull(config.getProperties())) {
            this.mailSender.setJavaMailProperties(new Properties() {
                private static final long serialVersionUID = 1395782904610029089L;

                {
                    putAll(config.getProperties());
                }
            });
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

    /**
     * Send mail messages.
     *
     * @param simpleMessages
     */
    @Override
    public Object send(GenericNotifyMessage msg) {
        String mailMsgType = msg.getParameterAsString(KEY_MAILMSG_TYPE, "simple");
        switch (mailMsgType) {
        case KEY_MAILMSG_VALUE_SIMPLE:
            SimpleMailMessage simpleMsg = new SimpleMailMessage();
            // Add "<>" symbol to send out?
            /*
             * Preset from account, otherwise it would be wrong: 501 mail from
             * address must be same as authorization user.
             */
            simpleMsg.setFrom(config.getUsername() + "<" + config.getUsername() + ">");
            simpleMsg.setTo(
                    msg.getToObjects().stream().map(to -> to = to + "<" + to + ">").collect(toList()).toArray(new String[] {}));
            simpleMsg.setSubject(msg.getParameterAsString(KEY_MAILMSG_SUBJECT, "Super Devops Messages"));
            simpleMsg.setSentDate(msg.getParameter(KEY_MSG_SENDDATE, new Date()));
            simpleMsg.setBcc(safeList(msg.getParameter(KEY_MAILMSG_BCC)).toArray(new String[] {}));
            simpleMsg.setCc(safeList(msg.getParameter(KEY_MAILMSG_CC)).toArray(new String[] {}));
            simpleMsg.setReplyTo(msg.getParameter(KEY_MAILMSG_REPLYTO));
            simpleMsg.setText(config.resolveMessage(msg.getTemplateKey(), msg.getParameters()));

            mailSender.send(simpleMsg);
            break;
        case KEY_MAILMSG_VALUE_MIME: // TODO implements!!!
            log.warn("No implements MimeMailMessage!!!");
            break;
        default:
            throw new UnsupportedOperationException(format("No supported mail message type of %s", mailMsgType));
        }
        return null;
    }

    /**
     * Send mail message type definitions. </br>
     *
     * <pre>
     * <b>simple</b> => {@link SimpleMailMessage}
     * <b>mime</b> => {@link MimeMailMessage}
     * </pre>
     */
    final public static String KEY_MAILMSG_TYPE = "mailMsgType";
    final public static String KEY_MAILMSG_VALUE_SIMPLE = "simple";
    final public static String KEY_MAILMSG_VALUE_MIME = "mime";

    /**
     * EMAIL message builder subject keyname.
     */
    final public static String KEY_MAILMSG_SUBJECT = "mailMsgSubject";

    /**
     * EMAIL message builder replyTo keyname.
     */
    final public static String KEY_MAILMSG_REPLYTO = "mailMsgReplyTo";

    /**
     * EMAIL message builder cc keyname.
     */
    final public static String KEY_MAILMSG_CC = "mailMsgCc";

    /**
     * EMAIL message builder bcc keyname.
     */
    final public static String KEY_MAILMSG_BCC = "mailMsgBcc";

}
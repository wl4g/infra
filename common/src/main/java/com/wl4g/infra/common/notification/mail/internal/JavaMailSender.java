/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.notification.mail.internal;

import java.io.InputStream;

import javax.mail.internet.MimeMessage;

/**
 * Extended {@link org.springframework.mail.MailSender} interface for JavaMail,
 * supporting MIME messages both as direct arguments and through preparation
 * callbacks. Typically used in conjunction with the {@link MimeMessageHelper}
 * class for convenient creation of JavaMail {@link MimeMessage MimeMessages},
 * including attachments etc.
 *
 * <p>
 * Clients should talk to the mail sender through this interface if they need
 * mail functionality beyond {@link com.wl4g.infra.common.notification.mail.internal.springframework.mail.SimpleMailMessage}.
 * The production implementation is {@link JavaMailSenderImpl}; for testing,
 * mocks can be created based on this interface. Clients will typically receive
 * the JavaMailSender reference through dependency injection.
 *
 * <p>
 * The recommended way of using this interface is the
 * {@link MimeMessagePreparator} mechanism, possibly using a
 * {@link MimeMessageHelper} for populating the message. See
 * {@link MimeMessageHelper MimeMessageHelper's javadoc} for an example.
 *
 * <p>
 * The entire JavaMail {@link javax.mail.Session} management is abstracted by
 * the JavaMailSender. Client code should not deal with a Session in any way,
 * rather leave the entire JavaMail configuration and resource handling to the
 * JavaMailSender implementation. This also increases testability.
 *
 * <p>
 * A JavaMailSender client is not as easy to test as a plain
 * {@link org.springframework.mail.MailSender} client, but still straightforward
 * compared to traditional JavaMail code: Just let {@link #createMimeMessage()}
 * return a plain {@link MimeMessage} created with a
 * {@code Session.getInstance(new Properties())} call, and check the passed-in
 * messages in your mock implementations of the various {@code send} methods.
 *
 * @author Juergen Hoeller
 * @since 07.10.2003
 * @see javax.mail.internet.MimeMessage
 * @see javax.mail.Session
 * @see JavaMailSenderImpl
 * @see MimeMessagePreparator
 * @see MimeMessageHelper
 */
public interface JavaMailSender {

    /**
     * Create a new JavaMail MimeMessage for the underlying JavaMail Session of
     * this sender. Needs to be called to create MimeMessage instances that can
     * be prepared by the client and passed to send(MimeMessage).
     * 
     * @return the new MimeMessage instance
     * @see #send(MimeMessage)
     * @see #send(MimeMessage[])
     */
    MimeMessage createMimeMessage();

    /**
     * Create a new JavaMail MimeMessage for the underlying JavaMail Session of
     * this sender, using the given input stream as the message source.
     * 
     * @param contentStream
     *            the raw MIME input stream for the message
     * @return the new MimeMessage instance
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailParseException
     *             in case of message creation failure
     */
    MimeMessage createMimeMessage(InputStream contentStream) throws MailException;

    /**
     * Send the given simple mail message.
     * 
     * @param simpleMessage
     *            the message to send
     * @throws MailParseException
     *             in case of failure when parsing the message
     * @throws MailAuthenticationException
     *             in case of authentication failure
     * @throws MailSendException
     *             in case of failure when sending the message
     */
    void send(SimpleMailMessage simpleMessage) throws MailException;

    /**
     * Send the given array of simple mail messages in batch.
     * 
     * @param simpleMessages
     *            the messages to send
     * @throws MailParseException
     *             in case of failure when parsing a message
     * @throws MailAuthenticationException
     *             in case of authentication failure
     * @throws MailSendException
     *             in case of failure when sending a message
     */
    void send(SimpleMailMessage... simpleMessages) throws MailException;

    /**
     * Send the given JavaMail MIME message. The message needs to have been
     * created with {@link #createMimeMessage()}.
     * 
     * @param mimeMessage
     *            message to send
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailAuthenticationException
     *             in case of authentication failure
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailSendException
     *             in case of failure when sending the message
     * @see #createMimeMessage
     */
    void send(MimeMessage mimeMessage) throws MailException;

    /**
     * Send the given array of JavaMail MIME messages in batch. The messages
     * need to have been created with {@link #createMimeMessage()}.
     * 
     * @param mimeMessages
     *            messages to send
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailAuthenticationException
     *             in case of authentication failure
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailSendException
     *             in case of failure when sending a message
     * @see #createMimeMessage
     */
    void send(MimeMessage... mimeMessages) throws MailException;

    /**
     * Send the JavaMail MIME message prepared by the given
     * MimeMessagePreparator.
     * <p>
     * Alternative way to prepare MimeMessage instances, instead of
     * {@link #createMimeMessage()} and {@link #send(MimeMessage)} calls. Takes
     * care of proper exception conversion.
     * 
     * @param mimeMessagePreparator
     *            the preparator to use
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailPreparationException
     *             in case of failure when preparing the message
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailParseException
     *             in case of failure when parsing the message
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailAuthenticationException
     *             in case of authentication failure
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailSendException
     *             in case of failure when sending the message
     */
    void send(MimeMessagePreparator mimeMessagePreparator) throws MailException;

    /**
     * Send the JavaMail MIME messages prepared by the given
     * MimeMessagePreparators.
     * <p>
     * Alternative way to prepare MimeMessage instances, instead of
     * {@link #createMimeMessage()} and {@link #send(MimeMessage[])} calls.
     * Takes care of proper exception conversion.
     * 
     * @param mimeMessagePreparators
     *            the preparator to use
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailPreparationException
     *             in case of failure when preparing a message
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailParseException
     *             in case of failure when parsing a message
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailAuthenticationException
     *             in case of authentication failure
     * @throws com.wl4g.infra.common.notification.mail.internal.springframework.mail.MailSendException
     *             in case of failure when sending a message
     */
    void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException;

    /**
     * Callback interface for the preparation of JavaMail MIME messages.
     *
     * <p>
     * The corresponding {@code send} methods of {@link JavaMailSender} will
     * take care of the actual creation of a {@link MimeMessage} instance, and
     * of proper exception conversion.
     *
     * <p>
     * It is often convenient to use a {@link MimeMessageHelper} for populating
     * the passed-in MimeMessage, in particular when working with attachments or
     * special character encodings. See {@link MimeMessageHelper
     * MimeMessageHelper's javadoc} for an example.
     *
     * @author Juergen Hoeller
     * @since 07.10.2003
     * @see JavaMailSender#send(MimeMessagePreparator)
     * @see JavaMailSender#send(MimeMessagePreparator[])
     * @see MimeMessageHelper
     */
    @FunctionalInterface
    public static interface MimeMessagePreparator {

        /**
         * Prepare the given new MimeMessage instance.
         * 
         * @param mimeMessage
         *            the message to prepare
         * @throws javax.mail.MessagingException
         *             passing any exceptions thrown by MimeMessage methods
         *             through for automatic conversion to the MailException
         *             hierarchy
         * @throws java.io.IOException
         *             passing any exceptions thrown by MimeMessage methods
         *             through for automatic conversion to the MailException
         *             hierarchy
         * @throws Exception
         *             if mail preparation failed, for example when a FreeMarker
         *             template cannot be rendered for the mail text
         */
        void prepare(MimeMessage mimeMessage) throws Exception;

    }
}

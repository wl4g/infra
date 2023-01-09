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

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.Assert2.stateOf;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

import com.wl4g.infra.common.notification.AbstractNotifyProperties;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link EmailNotifierProperties}, Full compatibility with native spring mail!
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2020年1月9日 v1.0.0
 * @see {@link org.springframework.boot.autoconfigure.mail.MailProperties}
 */
@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
public class EmailNotifierProperties extends AbstractNotifyProperties {

    /**
     * Protocol used by the Email(SMTP) server.
     */
    private @NotBlank @Default String protocol = "smtp";

    /**
     * Email(SMTP) server host.
     */
    private @NotBlank @Default String host = "smtp.exmail.qq.com";

    /**
     * Email(SMTP) server port.
     */
    private @NotBlank @Default Integer port = 465;

    /**
     * Login user of the Email(SMTP) server.
     */
    private @NotBlank String username;

    /**
     * Login password of the Email(SMTP) server.
     */
    private @NotBlank String password;

    /**
     * Default MimeMessage encoding.
     */
    private @NotBlank @Default String defaultEncoding = "UTF-8";

    /**
     * Additional JavaMail session properties.
     */
    private @Nullable @Default Map<String, String> properties = new HashMap<>();

    /**
     * Session JNDI name. When set, takes precedence to others mail settings.
     */
    private String jndiName;

    @Override
    public void validate() {
        hasTextOf(getHost(), "host");
        notNullOf(getPort(), "port");
        stateOf(getPort() > 0, "port must >0");
        hasTextOf(getUsername(), "username");
        hasTextOf(getPassword(), "password");
        hasTextOf(getProtocol(), "protocol");
        notNullOf(getDefaultEncoding(), "defaultEncoding");
    }
}
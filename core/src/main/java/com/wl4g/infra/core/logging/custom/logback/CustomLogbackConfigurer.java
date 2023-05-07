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
package com.wl4g.infra.core.logging.custom.logback;

import java.nio.charset.Charset;

import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.logback.ColorConverter;
import org.springframework.boot.logging.logback.ExtendedWhitespaceThrowableProxyConverter;
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.OptionHelper;

/**
 * Custom logback configuration used by Spring Boot. Uses
 * {@link LogbackConfigurator} to improve startup time. See also the
 * {@code defaults.xml}, {@code console-appender.xml} and
 * {@code file-appender.xml} files provided for classic {@code logback.xml} use.
 * </br>
 * </br>
 * 
 * for example: <b>application.yml</b>
 * 
 * <pre>
 * ## Logging configuration.
 * ## see:https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#features.logging.custom-log-configuration
 * ## see:com.wl4g.infra.core.logging.custom.logback.CustomLogbackConfigurer
 * logging:
 *   register-shutdown-hook: false
 *   charset.file: UTF-8
 *   ## Log levels belonging to this group will take effect synchronously.(TRACE|DEBUG|INFO|WARN|ERROR|FATAL|OFF)
 *   ## see:https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#features.logging.log-groups
 *   group:
 *     config: "org.springframework.boot.context.config"
 *     tomcat: "org.apache.catalina,org.apache.coyote,org.apache.tomcat"
 *   file: ## see:org.springframework.boot.logging.LogFile#toString
 *     name: /mnt/disk1/log/${spring.application.name}/${spring.application.name}.log
 *   pattern:
 *     console: '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx'
 *     file: '%d{yyyy-MM-dd HH:mm:ss.SSS} %5p ${PID:- } [%X{_H_:X-Request-ID}] [%X{_H_:X-Request-Seq}] --- [%t] %-40.40logger{39} : %m%n%wEx'
 *     dateformat: yyyy-MM-dd HH:mm:ss.SSS
 *     level: '%5p'
 *   logback:
 *     rollingpolicy:
 *       file-name-pattern: ${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz
 *       clean-history-on-start: false ## Default by false
 *       max-file-size: 1GB ## Default by 200MB
 *       max-history: 30 ## Default by 7
 *       total-size-cap: 100GB ## Default by 10GB
 *   level:
 *     root: INFO
 *     tomcat: INFO
 *     config: INFO
 *     ## The built-in log groups:
 *     ## web(org.springframework.core.codec,org.springframework.http,org.springframework.web,org.springframework.boot.actuate.endpoint.web,org.springframework.boot.web.servlet.ServletContextInitializerBeans)
 *     ## sql(org.springframework.jdbc.core,org.hibernate.SQL,org.jooq.tools.LoggerListener)
 *     web: INFO
 *     sql: INFO
 *     reactor:
 *       netty.http.client: INFO
 *     org:
 *       springframework: INFO
 *       apache: INFO
 *     feign: DEBUG
 *     com:
 *       wl4g.iam.gateway: INFO
 * </pre>
 * 
 * @author Wangl.sir
 * @author Phillip Webb
 * @since 1.1.2
 * @see {@link org.springframework.boot.logging.logback.DefaultLogbackConfiguration.DefaultLogbackConfiguration}
 */
class CustomLogbackConfigurer {

    private final LogFile logFile;

    CustomLogbackConfigurer(LogFile logFile) {
        this.logFile = logFile;
    }

    void apply(LogbackConfigurator config) {
        synchronized (config.getConfigurationLock()) {
            defaults(config);
            Appender<ILoggingEvent> consoleAppender = consoleAppender(config);
            if (this.logFile != null) {
                Appender<ILoggingEvent> fileAppender = fileAppender(config, this.logFile.toString());
                config.root(Level.INFO, consoleAppender, fileAppender);
            } else {
                config.root(Level.INFO, consoleAppender);
            }
        }
    }

    private void defaults(LogbackConfigurator config) {
        config.conversionRule("clr", ColorConverter.class);
        config.conversionRule("wex", WhitespaceThrowableProxyConverter.class);
        config.conversionRule("wEx", ExtendedWhitespaceThrowableProxyConverter.class);
        config.getContext().putProperty("CONSOLE_LOG_PATTERN",
                resolve(config, "${CONSOLE_LOG_PATTERN:-"
                        + "%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) "
                        + "%clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} "
                        + "%clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}"));
        String defaultCharset = Charset.defaultCharset().name();
        config.getContext().putProperty("CONSOLE_LOG_CHARSET", resolve(config, "${CONSOLE_LOG_CHARSET:-" + defaultCharset + "}"));
        config.getContext().putProperty("FILE_LOG_PATTERN",
                resolve(config, "${FILE_LOG_PATTERN:-"
                        + "%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}} ${LOG_LEVEL_PATTERN:-%5p} ${PID:- } --- [%t] "
                        + "%-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}"));
        config.getContext().putProperty("FILE_LOG_CHARSET", resolve(config, "${FILE_LOG_CHARSET:-" + defaultCharset + "}"));
        config.logger("org.apache.catalina.startup.DigesterFactory", Level.ERROR);
        config.logger("org.apache.catalina.util.LifecycleBase", Level.ERROR);
        config.logger("org.apache.coyote.http11.Http11NioProtocol", Level.WARN);
        config.logger("org.apache.sshd.common.util.SecurityUtils", Level.WARN);
        config.logger("org.apache.tomcat.util.net.NioSelectorPool", Level.WARN);
        config.logger("org.eclipse.jetty.util.component.AbstractLifeCycle", Level.ERROR);
        config.logger("org.hibernate.validator.internal.util.Version", Level.WARN);
        config.logger("org.springframework.boot.actuate.endpoint.jmx", Level.WARN);
    }

    private Appender<ILoggingEvent> consoleAppender(LogbackConfigurator config) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern(resolve(config, "${CONSOLE_LOG_PATTERN}"));
        encoder.setCharset(resolveCharset(config, "${CONSOLE_LOG_CHARSET}"));
        config.start(encoder);
        appender.setEncoder(encoder);
        config.appender("CONSOLE", appender);
        return appender;
    }

    private Appender<ILoggingEvent> fileAppender(LogbackConfigurator config, String logFile) {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern(resolve(config, "${FILE_LOG_PATTERN}"));
        encoder.setCharset(resolveCharset(config, "${FILE_LOG_CHARSET}"));
        appender.setEncoder(encoder);
        config.start(encoder);
        appender.setFile(logFile);
        setRollingPolicy(appender, config);
        config.appender("FILE", appender);
        return appender;
    }

    private void setRollingPolicy(RollingFileAppender<ILoggingEvent> appender, LogbackConfigurator config) {
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(config.getContext());

        rollingPolicy.setFileNamePattern(
                resolve(config, "${LOGBACK_ROLLINGPOLICY_FILE_NAME_PATTERN:-${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz}"));
        rollingPolicy.setCleanHistoryOnStart(resolveBoolean(config, "${LOGBACK_ROLLINGPOLICY_CLEAN_HISTORY_ON_START:-false}"));
        //
        // [Start] modified OLD
        //
        // rollingPolicy.setMaxFileSize(resolveFileSize(config,"${LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE:-10MB}"));
        rollingPolicy.setMaxFileSize(resolveFileSize(config, "${LOGBACK_ROLLINGPOLICY_MAX_FILE_SIZE:-200MB}"));
        // rollingPolicy.setTotalSizeCap(resolveFileSize(config,"${LOGBACK_ROLLINGPOLICY_TOTAL_SIZE_CAP:-0}"));
        rollingPolicy.setTotalSizeCap(resolveFileSize(config, "${LOGBACK_ROLLINGPOLICY_TOTAL_SIZE_CAP:-10GB}"));
        rollingPolicy.setMaxHistory(resolveInt(config, "${LOGBACK_ROLLINGPOLICY_MAX_HISTORY:-7}"));
        //
        // [End] modified OLD
        //

        appender.setRollingPolicy(rollingPolicy);
        rollingPolicy.setParent(appender);
        config.start(rollingPolicy);
    }

    private boolean resolveBoolean(LogbackConfigurator config, String val) {
        return Boolean.parseBoolean(resolve(config, val));
    }

    private int resolveInt(LogbackConfigurator config, String val) {
        return Integer.parseInt(resolve(config, val));
    }

    private FileSize resolveFileSize(LogbackConfigurator config, String val) {
        return FileSize.valueOf(resolve(config, val));
    }

    private Charset resolveCharset(LogbackConfigurator config, String val) {
        return Charset.forName(resolve(config, val));
    }

    private String resolve(LogbackConfigurator config, String val) {
        return OptionHelper.substVars(val, config.getContext());
    }

}
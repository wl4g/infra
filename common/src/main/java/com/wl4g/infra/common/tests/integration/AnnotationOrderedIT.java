/**
 * Copyright (C) 2023 ~ 2035 the original authors WL4G (James Wong).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.tests.integration;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * The {@link AnnotationOrderedIT}
 *
 * @author James Wong
 * @since v3.1
 **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Category(IntegrationTest.class) // Junit4, for supported at the same time, can be removed after.
@Tag("integration") // Junit5
public abstract class AnnotationOrderedIT {
    protected final Map<String, ThreadLocal<Pattern>> logPatternMap = new ConcurrentHashMap<>(2);
    protected final Logger log = getLogger(getClass());

    public AnnotationOrderedIT() {
        this(null, null);
    }

    public AnnotationOrderedIT(@Nullable Map<String, String> logPatterns,
                               @Nullable BiConsumer<String, String> logConsumer) {
        // Register log pattern local.
        if (nonNull(logPatterns)) {
            logPatterns.forEach((name, logPattern) -> {
                if (isBlank(logPattern)) {
                    throw new IllegalArgumentException(String.format("Log pattern for '%s' must not be empty", name));
                }
                this.logPatternMap.put(name, ThreadLocal.withInitial(() -> Pattern.compile(logPattern)));
            });
        }
        // Register log appender/listener and callback matched message with pattern group.
        final AppenderBase<ILoggingEvent> listener = new AppenderBase<ILoggingEvent>() {
            @Override
            protected void append(ILoggingEvent event) {
                if (nonNull(logConsumer)) {
                    safeMap(logPatternMap).forEach((name, patternLocal) -> {
                        final String logMsg = event.getFormattedMessage();
                        if (patternLocal.get().matcher(logMsg).matches()) {
                            logConsumer.accept(name, logMsg);
                        }
                    });
                }
            }
        };
        ((ch.qos.logback.classic.Logger) getLogger(Logger.ROOT_LOGGER_NAME))
                .addAppender(listener);
        listener.start();
    }

}

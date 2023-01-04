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
package com.wl4g.infra.common.graalvm.polyglot;

import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Exceptions.getStackTraceAsString;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.SystemUtils.JAVA_IO_TMPDIR;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;

import com.google.common.base.Charsets;

import lombok.Getter;

/**
 * {@link JdkLoggingOutputStream}
 * 
 * @author James Wong
 * @version 2023-01-04
 * @since v1.0.0
 */
@Getter
public class JdkLoggingOutputStream extends OutputStream {
    private final static ThreadLocal<DateTimeFormatter> dateFormatterLocal = ThreadLocal
            .withInitial(() -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault()));

    private final String filePattern;
    private final Level level;
    private final int fileMaxSize;
    private final int fileMaxCount;
    private final Logger logger;
    private final boolean isStdErr;

    public JdkLoggingOutputStream() {
        this(false);
    }

    public JdkLoggingOutputStream(boolean isStdErr) {
        this(null, null, null, null, true, isStdErr);
    }

    public JdkLoggingOutputStream(@Nullable String filePattern, @Nullable Level level, @Nullable @Min(1024) Integer fileMaxSize,
            @Nullable @Min(1) Integer fileMaxCount, boolean enableConsole, boolean isStdErr) {
        this.filePattern = nonNull(filePattern) ? filePattern
                : JAVA_IO_TMPDIR.concat("/").concat(JdkLoggingOutputStream.class.getSimpleName()).concat(".log");
        this.level = nonNull(level) ? level : Level.ALL;
        if (nonNull(fileMaxSize)) {
            isTrueOf(fileMaxSize >= 1024, "fileMaxSize >= 1024");
        }
        if (nonNull(fileMaxCount)) {
            isTrueOf(fileMaxCount >= 1, "fileMaxCount >= 1");
        }
        // The default by 512MB
        this.fileMaxSize = nonNull(fileMaxSize) ? fileMaxSize : 512 * 1024 * 1024;
        this.fileMaxCount = nonNull(fileMaxSize) ? fileMaxSize : 10;
        try {
            this.logger = Logger.getLogger(JdkLoggingOutputStream.class.getName());
            final FileHandler handler = new FileHandler(this.filePattern, this.fileMaxSize, this.fileMaxCount, true);
            handler.setEncoding("UTF-8");
            handler.setLevel(this.level);
            handler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    final Map<String, Object> json = new HashMap<>();
                    json.put("level", record.getLevel().getName());
                    json.put("date", dateFormatterLocal.get().format(record.getInstant()));
                    json.put("threadId", record.getThreadID());
                    json.put("sequence", record.getSequenceNumber());
                    json.put("message", record.getMessage());
                    json.put("cause", getStackTraceAsString(record.getThrown()));
                    return toJSONString(json);
                }
            });
            this.logger.setUseParentHandlers(false);
            this.logger.addHandler(handler);
            if (enableConsole) {
                this.logger.addHandler(new ConsoleHandler());
            }
            this.isStdErr = isStdErr;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize logger", e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        final String message = new String(b, off, len, Charsets.UTF_8);
        if (isStdErr) {
            logger.warning(message);
        } else {
            logger.info(message);
        }
    }

}

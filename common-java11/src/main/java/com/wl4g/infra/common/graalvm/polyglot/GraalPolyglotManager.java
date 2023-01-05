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

import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getBooleanProperty;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getIntProperty;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getLongProperty;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getProperty;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.SystemUtils.JAVA_IO_TMPDIR;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import com.wl4g.infra.common.io.FileIOUtils;
import com.wl4g.infra.common.lang.EnvironmentUtil;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link GraalPolyglotManager}
 * 
 * @author James Wong
 * @version 2022-09-23
 * @since v3.0.0
 * @see https://www.graalvm.org/22.0/reference-manual/js/Modules/
 */
@Slf4j
public class GraalPolyglotManager implements Closeable {

    private @NotNull SimpleFastContextPool contextPool;

    public static GraalPolyglotManager newDefaultGraalJS(
            @Nullable String workingRootDir,
            @Nullable Function<Map<String, Object>, OutputStream> stdoutCreator,
            @Nullable Function<Map<String, Object>, OutputStream> stderrCreator) {
        try {
            final String _workingRootDir = isBlank(workingRootDir) ? JAVA_IO_TMPDIR.concat("__graaljs_script_caches")
                    : workingRootDir;

            // Extraction graal.js from environment.
            final Map<String, String> options = EnvironmentUtil.getConfigProperties("graaljs.options.");

            // Enable CommonJS experimental support.
            // see:https://www.graalvm.org/22.0/reference-manual/js/Modules/
            options.put("js.commonjs-require", "true");

            // (optional) folder where the NPM modules to be loaded are located.
            final File commonjsDir = new File(_workingRootDir.concat("/commonjs-root"));
            FileIOUtils.forceMkdir(commonjsDir);
            options.put("js.commonjs-require-cwd", commonjsDir.getAbsolutePath());

            final File workingDir = new File(getProperty("graaljs.currentWorkingDir", _workingRootDir.concat("/working")));
            FileIOUtils.forceMkdir(workingDir);

            return new GraalPolyglotManager(getIntProperty("graaljs.context.pool.max", 1024), metadata -> {
                final OutputStream stdout = isNull(stdoutCreator) ? null : stdoutCreator.apply(metadata);
                final OutputStream stderr = isNull(stderrCreator) ? null : stderrCreator.apply(metadata);
                return Context.newBuilder("js") // Only-allowed-JS-language
                        .allowAllAccess(getBooleanProperty("graaljs.allowAllAccess", true))
                        .allowExperimentalOptions(getBooleanProperty("graaljs.allowExperimentalOptions", true))
                        .allowIO(getBooleanProperty("graaljs.allowIO", true))
                        .allowCreateProcess(getBooleanProperty("graaljs.allowCreateProcess", true))
                        .allowCreateThread(getBooleanProperty("graaljs.allowCreateThread", true))
                        .allowNativeAccess(getBooleanProperty("graaljs.allowNativeAccess", true))
                        .allowHostClassLoading(getBooleanProperty("graaljs.allowHostClassLoading", true))
                        .allowValueSharing(getBooleanProperty("graaljs.allowValueSharing", true))
                        .allowPolyglotAccess(PolyglotAccess.ALL)
                        .useSystemExit(getBooleanProperty("graaljs.useSystemExit", false))
                        .currentWorkingDirectory(workingDir.toPath())
                        .resourceLimits(ResourceLimits.newBuilder()
                                .statementLimit(getLongProperty("graaljs.resourceLimits", Long.MAX_VALUE), null)
                                .build())
                        // Note: The custom logHandler is invalid for
                        // 'console.log'
                        // .logHandler(null)
                        // see:com.oracle.truffle.polyglot.PolyglotEngineImpl#createContext()
                        // see:com.oracle.truffle.polyglot.PolyglotLoggers#asHandler()
                        .out(isNull(stdout) ? System.out : stdout)
                        .err(isNull(stderr) ? System.err : stderr)
                        .options(options)
                        .build();
            });
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public GraalPolyglotManager(@Min(1) int maxSize, String... permittedLanguages) {
        this(maxSize, metadata -> Context.newBuilder(permittedLanguages).allowAllAccess(true).build());
    }

    public GraalPolyglotManager(@Min(1) int maxSize, Function<Map<String, Object>, Context> instantiator) {
        try {
            log.info("Initialzing graalvm polyglot context pool ...");
            this.contextPool = new SimpleFastContextPool(maxSize, instantiator);
            log.info("Initialzed graalvm polyglot context pool for {}", contextPool);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void close() {
        log.info("Closing graalvm polyglot context pool ...");
        if (nonNull(contextPool)) {
            try {
                contextPool.close();
            } catch (Exception e) {
                log.error("Failed to destroy graalvm polyglot context pool.", e);
            }
        }
    }

    public ContextWrapper getContext(@Nullable Map<String, Object> metadata) {
        return getContext(true, metadata);
    }

    public ContextWrapper getContext(boolean must, @Nullable Map<String, Object> metadata) {
        return contextPool.take(must, metadata);
    }

    @Slf4j
    public static class SimpleFastContextPool implements AutoCloseable {

        private final ContextWrapper[] contextCached;

        private final int maxSize;

        private final AtomicInteger totalSize = new AtomicInteger(0);

        private final Function<Map<String, Object>, Context> instantiator;

        public SimpleFastContextPool(@Min(1) int maxSize, Function<Map<String, Object>, Context> instantiator) {
            isTrue(maxSize >= 1, "maxSize >= 1");
            notNullOf(instantiator, "instantiator");
            this.maxSize = maxSize;
            this.instantiator = instantiator;
            this.contextCached = new ContextWrapper[maxSize];
        }

        // public ContextWrapper take(long tryMillis) {
        // long begin = currentTimeMillis();
        // ContextWrapper context = take(false);
        // while (isNull(context) && (currentTimeMillis() - begin) < tryMillis)
        // {
        // Thread.yield();
        // if (nonNull(context = take(false))) {
        // return context;
        // }
        // }
        // throw new IllegalStateException("Could not got context from pool");
        // }

        protected ContextWrapper take(boolean must, @Nullable Map<String, Object> metadata) {
            // Gets statistics
            int usedSize = 0;
            ContextWrapper takeContext = null;
            for (int i = 0; i < contextCached.length; i++) {
                final ContextWrapper context = contextCached[i];
                if (nonNull(context)) {
                    if (context.isOpened()) {
                        ++usedSize;
                    } else if (nonNull(metadata)) {
                        final Map<String, Object> _metadata = safeMap(context.getMetadata());
                        if (safeMap(metadata).entrySet()
                                .stream()
                                .anyMatch(e -> eqIgnCase(_metadata.get(e.getKey()), e.getValue()))) {
                            takeContext = context;
                        }
                    } else {
                        takeContext = context;
                    }
                }
            }
            // Checking for new instantiate context.
            if (checkNewInstantiate(takeContext, usedSize)) {
                synchronized (this) {
                    if (checkNewInstantiate(takeContext, usedSize)) { // limit-max
                        takeContext = contextCached[totalSize.get()] = newInstance(metadata);
                    }
                }
            }
            if (nonNull(takeContext)) {
                takeContext.open(metadata);
                return takeContext;
            }
            if (must) {
                throw new IllegalStateException("Could not got context from pool");
            }
            return null;
        }

        private ContextWrapper newInstance(@Nullable Map<String, Object> metadata) {
            try {
                return new ContextWrapper("graal-ctx-pool-" + totalSize.getAndIncrement(), instantiator.apply(metadata));
            } catch (Exception e) {
                throw new IllegalStateException("Unable to new instantiate context object.", e);
            }
        }

        private boolean checkNewInstantiate(ContextWrapper takeContext, int usedSize) {
            return (isNull(takeContext) || usedSize >= totalSize.get()) && totalSize.get() < maxSize;
        }

        @Override
        public void close() throws Exception {
            if (nonNull(contextCached)) {
                synchronized (this) {
                    for (int i = 0; i < contextCached.length; i++) {
                        ContextWrapper ctx = contextCached[i];
                        if (nonNull(ctx)) {
                            try {
                                // Actual close context.
                                ctx.getContext().close();
                            } catch (Exception e) {
                                log.error(format("Failed to context actual close ... %s", ctx), e);
                            } finally {
                                contextCached[i] = null;
                            }
                        }
                    }
                }
            }
        }

    }

    @Getter
    @ToString
    public static class ContextWrapper implements Closeable {
        private @NotBlank String id;
        private @NotNull Context context;
        private @Nullable Map<String, Object> metadata;

        public ContextWrapper(@NotBlank String id, @NotNull Context context) {
            this.id = hasTextOf(id, "id");
            this.context = notNullOf(context, "context");
        }

        /**
         * Pooling context marker, because js does not support multi-threaded
         * execution in the same context at the same time. </br>
         * </br>
         * Multi threaded access requested by thread
         * Thread[executor-thread-1,5,main] but is not allowed for language(s)
         * js.
         */
        private @Getter(value = AccessLevel.PRIVATE) final AtomicBoolean opened = new AtomicBoolean(false);

        @Override
        public synchronized void close() throws IOException {
            // Mark closed with pool.
            this.opened.set(false);
            try {
                // see:https://github.com/oracle/graaljs/blob/master/graal-js/src/com.oracle.truffle.js.test.threading/src/com/oracle/truffle/js/test/threading/ConcurrentAccess.java#L202
                getContext().leave();
            } catch (IllegalStateException e) {
                // Ignore
            } finally {
                this.metadata = null;
            }
        }

        public boolean isOpened() {
            return this.opened.get();
        }

        public synchronized void open(@Nullable Map<String, Object> metadata) {
            this.opened.set(true);
            this.metadata = metadata;
            try {
                // see:https://github.com/oracle/graaljs/blob/master/graal-js/src/com.oracle.truffle.js.test.threading/src/com/oracle/truffle/js/test/threading/ConcurrentAccess.java#L198
                getContext().enter();
            } catch (IllegalStateException e) {
                // Ignore
            }
        }

        public Value eval(Source source) {
            return getContext().eval(source);
        }

        public Value eval(String languageId, CharSequence source) {
            return getContext().eval(languageId, source);
        }

        public Value parse(Source source) {
            return getContext().parse(source);
        }

        public Value parse(String languageId, CharSequence source) {
            return getContext().parse(languageId, source);
        }

        public Value getPolyglotBindings() {
            return getContext().getPolyglotBindings();
        }

        public Value getBindings(String languageId) {
            return getContext().getBindings(languageId);
        }

        public boolean initialize(String languageId) {
            return getContext().initialize(languageId);
        }

        public void resetLimits() {
            getContext().resetLimits();
        }

        public Value asValue(Object hostValue) {
            return getContext().asValue(hostValue);
        }

        public void enter() {
            getContext().enter();
        }

        @Override
        public boolean equals(Object obj) {
            return getContext().equals(obj);
        }

        @Override
        public int hashCode() {
            return getContext().hashCode();
        }

        public void leave() {
            getContext().leave();
        }

        public void interrupt(Duration timeout) throws TimeoutException {
            getContext().interrupt(timeout);
        }

        public void safepoint() {
            getContext().safepoint();
        }

    }

    @Getter
    @ToString
    public static class Slf4jOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
        }
    }

}

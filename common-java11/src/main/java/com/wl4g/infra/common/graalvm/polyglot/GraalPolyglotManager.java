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
import org.graalvm.polyglot.io.FileSystem;

import com.wl4g.infra.common.io.FileIOUtils;
import com.wl4g.infra.common.lang.EnvironmentUtil;
import com.wl4g.infra.common.lang.tuples.Tuple3;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link GraalPolyglotManager}
 * 
 * @author James Wong
 * @version 2022-09-23
 * @since v3.0.0
 * @see https://www.graalvm.org/22.2/reference-manual/embed-languages/#compile-and-run-a-polyglot-application
 */
@Getter
@Slf4j
public class GraalPolyglotManager implements Closeable {

    private final @NotNull IContextPool contextPool;

    public static GraalPolyglotManager newDefaultForJS(
            @Nullable String workingRootDir,
            @Nullable FileSystem fileSystem,
            @Nullable Function<Map<String, Object>, OutputStream> stdoutCreator,
            @Nullable Function<Map<String, Object>, OutputStream> stderrCreator) {

        // Add graal.js suffix path.
        workingRootDir = isBlank(workingRootDir) ? JAVA_IO_TMPDIR.concat("__graaljs_working") : workingRootDir;

        // Extraction graal.js from environment.
        final var options = EnvironmentUtil.getConfigProperties("graal.polyglot.options.");
        options.put("js.shared-array-buffer", "true");

        // Enable CommonJS experimental support.
        // see:https://www.graalvm.org/22.0/reference-manual/js/Modules/
        options.put("js.commonjs-require", "true");

        // (optional) folder where the NPM modules to be loaded are located.
        final var commonjsDir = new File(workingRootDir.concat("/commonjs-root"));
        try {
            FileIOUtils.forceMkdir(commonjsDir);
            options.put("js.commonjs-require-cwd", commonjsDir.getAbsolutePath());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        return newDefaultFor("js", workingRootDir, options, null, fileSystem, null, null);
    }

    public static GraalPolyglotManager newDefaultForPython(
            @Nullable String workingRootDir,
            @Nullable FileSystem fileSystem,
            @Nullable Function<Map<String, Object>, OutputStream> stdoutCreator,
            @Nullable Function<Map<String, Object>, OutputStream> stderrCreator) {

        // Add graal for python suffix path.
        workingRootDir = isBlank(workingRootDir) ? JAVA_IO_TMPDIR.concat("__graalpy_working") : workingRootDir;

        // Extraction graal.js from environment.
        final var options = EnvironmentUtil.getConfigProperties("graal.polyglot.options.");

        return newDefaultFor("python", workingRootDir, options, null, fileSystem, null, null);
    }

    public static GraalPolyglotManager newDefaultForR(
            @Nullable String workingRootDir,
            @Nullable FileSystem fileSystem,
            @Nullable Function<Map<String, Object>, OutputStream> stdoutCreator,
            @Nullable Function<Map<String, Object>, OutputStream> stderrCreator) {

        // Add graal for R suffix path.
        workingRootDir = isBlank(workingRootDir) ? JAVA_IO_TMPDIR.concat("__graalr_working") : workingRootDir;

        // Extraction graal.js from environment.
        final var options = EnvironmentUtil.getConfigProperties("graal.polyglot.options.");

        return newDefaultFor("r", workingRootDir, options, null, fileSystem, null, null);
    }

    public static GraalPolyglotManager newDefaultForRuby(
            @Nullable String workingRootDir,
            @Nullable FileSystem fileSystem,
            @Nullable Function<Map<String, Object>, OutputStream> stdoutCreator,
            @Nullable Function<Map<String, Object>, OutputStream> stderrCreator) {

        // Add graal for ruby suffix path.
        workingRootDir = isBlank(workingRootDir) ? JAVA_IO_TMPDIR.concat("__graalrb_working") : workingRootDir;

        // Extraction graal.js from environment.
        final var options = EnvironmentUtil.getConfigProperties("graal.polyglot.options.");

        return newDefaultFor("ruby", workingRootDir, options, null, null, null, null);
    }

    /**
     * Create graal polgylot manager instance with default. </br>
     * </br>
     * for example:
     * 
     * <pre>
     * var virtualRootDir = Path.of("/path/to/rootdir");
     * var virtualRootFS = FileSystems.newFileSystem(virtualRootDir, null);
     * </pre>
     * 
     * @param language
     *            see to
     *            https://www.graalvm.org/22.2/reference-manual/polyglot-programming/#passing-options-for-language-launchers
     * @param workingRootDir
     * @param options
     * @param polyglotAccess
     * @param fileSystem
     * @param stdoutCreator
     * @param stderrCreator
     * @return
     */
    public static GraalPolyglotManager newDefaultFor(
            @NotBlank String language,
            @NotBlank String workingRootDir,
            @Nullable Map<String, String> options,
            @Nullable PolyglotAccess polyglotAccess,
            @Nullable FileSystem fileSystem,
            @Nullable Function<Map<String, Object>, OutputStream> stdoutCreator,
            @Nullable Function<Map<String, Object>, OutputStream> stderrCreator) {
        hasTextOf(language, "language");
        hasTextOf(workingRootDir, "workingRootDir");
        try {
            final File workingDir = new File(getProperty("graal.polyglot.currentWorkingDir", workingRootDir.concat("/working")));
            FileIOUtils.forceMkdir(workingDir);

            return new GraalPolyglotManager(getIntProperty("graal.polyglot.context.pool.max", 1024), metadata -> {
                final OutputStream stdout = isNull(stdoutCreator) ? null : stdoutCreator.apply(metadata);
                final OutputStream stderr = isNull(stderrCreator) ? null : stderrCreator.apply(metadata);

                // Addidtion to metadata.
                final Context.Builder builder = Context.newBuilder(language) // Only-allowed-JS-language
                        .allowAllAccess(getBooleanProperty("graal.polyglot.allowAllAccess", true))
                        .allowExperimentalOptions(getBooleanProperty("graal.polyglot.allowExperimentalOptions", true))
                        .allowIO(getBooleanProperty("graal.polyglot.allowIO", true))
                        .allowCreateProcess(getBooleanProperty("graal.polyglot.allowCreateProcess", true))
                        .allowCreateThread(getBooleanProperty("graal.polyglot.allowCreateThread", true))
                        .allowNativeAccess(getBooleanProperty("graal.polyglot.allowNativeAccess", true))
                        .allowHostClassLoading(getBooleanProperty("graal.polyglot.allowHostClassLoading", true))
                        .allowValueSharing(getBooleanProperty("graal.polyglot.allowValueSharing", true))
                        .allowPolyglotAccess(isNull(polyglotAccess) ? PolyglotAccess.ALL : polyglotAccess)
                        .useSystemExit(getBooleanProperty("graal.polyglot.useSystemExit", false))
                        .currentWorkingDirectory(workingDir.toPath())
                        .resourceLimits(ResourceLimits.newBuilder()
                                .statementLimit(getLongProperty("graal.polyglot.resourceLimits", Long.MAX_VALUE), null)
                                .build())
                        // Note: The custom logHandler is invalid for
                        // 'console.log'
                        // .logHandler(null)
                        // see:com.oracle.truffle.polyglot.PolyglotEngineImpl#createContext()
                        // see:com.oracle.truffle.polyglot.PolyglotLoggers#asHandler()
                        .out(isNull(stdout) ? System.out : stdout)
                        .err(isNull(stderr) ? System.err : stderr)
                        .options(safeMap(options));
                if (nonNull(fileSystem)) {
                    builder.fileSystem(fileSystem);
                }

                return new Tuple3(builder.build(), stdout, stderr);
            });
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    public GraalPolyglotManager(@Min(1) int maxPoolSize, String... permittedLanguages) {
        this(maxPoolSize,
                metadata -> new Tuple3(Context.newBuilder(permittedLanguages).allowAllAccess(true).build(), null, null));
    }

    public GraalPolyglotManager(@Min(1) int maxPoolSize, Function<Map<String, Object>, Tuple3> instantiator) {
        this(new SynchronousContextPool(maxPoolSize, instantiator));
    }

    public GraalPolyglotManager(IContextPool contextPool) {
        try {
            notNullOf(contextPool, "contextPool");
            log.info("Initialzing graal polyglot context pool ...");
            this.contextPool = contextPool;
            log.info("Initialzed graal polyglot context pool for {}", contextPool);
        } catch (Throwable ex) {
            throw ex;
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

    public ContextWrapper getContext(@Nullable Map<String, Object> requestMetadata) {
        return getContext(true, requestMetadata);
    }

    public ContextWrapper getContext(boolean must, @Nullable Map<String, Object> requestMetadata) {
        return contextPool.take(must, requestMetadata);
    }

    public static interface IContextPool extends AutoCloseable {
        ContextWrapper take(boolean must, @Nullable Map<String, Object> requestMetadata);
    }

    @Getter
    @Slf4j
    public static class SynchronousContextPool implements IContextPool {
        private final ContextWrapper[] contextCached;
        private final int maxPoolSize;
        private final AtomicInteger seq = new AtomicInteger(0);
        private final Function<Map<String, Object>, Tuple3> instantiator;

        public SynchronousContextPool(@Min(1) int maxPoolSize, Function<Map<String, Object>, Tuple3> instantiator) {
            isTrue(maxPoolSize >= 1, "maxPoolSize >= 1");
            notNullOf(instantiator, "instantiator");
            this.maxPoolSize = maxPoolSize;
            this.instantiator = instantiator;
            this.contextCached = new ContextWrapper[maxPoolSize];
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

        @Override
        public synchronized ContextWrapper take(boolean must, @Nullable Map<String, Object> requestMetadata) {
            int openedSize = 0;
            ContextWrapper takeContext = null;
            for (int i = 0; i < contextCached.length; i++) {
                final ContextWrapper context = contextCached[i];
                if (nonNull(context)) {
                    if (context.isOpened()) {
                        ++openedSize;
                    } else {
                        if (nonNull(requestMetadata)) {
                            final var md = safeMap(context.getMetadata());
                            if (safeMap(requestMetadata).entrySet()
                                    .stream()
                                    .anyMatch(e -> eqIgnCase(md.get(e.getKey()), e.getValue()))) {
                                takeContext = context;
                            }
                        } else {
                            takeContext = context;
                        }
                    }
                }
            }
            // Checking for new instantiate context.
            if (checkNewCreate(takeContext, openedSize)) { // limit-max
                takeContext = contextCached[seq.get()] = newInstance(requestMetadata);
            }
            if (nonNull(takeContext)) {
                takeContext.open();
                return takeContext;
            }
            if (must) {
                throw new NoPolyglotContextException(format("Could not obtain context from pool of '%s'", requestMetadata));
            }
            return null;
        }

        private ContextWrapper newInstance(@Nullable Map<String, Object> metadata) {
            try {
                final var newContext = instantiator.apply(metadata);
                return new ContextWrapper(seq.incrementAndGet(), (Context) newContext.getItem1(),
                        (OutputStream) newContext.getItem2(), (OutputStream) newContext.getItem3(), metadata);
            } catch (Throwable ex) {
                throw new IllegalStateException("Unable to new instantiate context object.", ex);
            }
        }

        private boolean checkNewCreate(ContextWrapper takeContext, int openedSize) {
            return (isNull(takeContext) || openedSize >= seq.get()) && seq.get() < maxPoolSize;
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
                            } catch (Throwable ex) {
                                log.error(format("Failed to context actual closing for %s", ctx), ex);
                            } finally {
                                contextCached[i] = null;
                            }
                        }
                    }
                }
            }
        }
    }

    @CustomLog
    @Getter
    @ToString
    public static class ContextWrapper implements Closeable {
        private @NotNull int id;
        private @NotBlank String name;
        private @NotNull Context context;
        private @Nullable OutputStream stdout;
        private @Nullable OutputStream stderr;
        private @Nullable Map<String, Object> metadata;
        /**
         * Pooling context marker, because js does not support multi-threaded
         * execution in the same context at the same time. </br>
         * </br>
         * Multi threaded access requested by thread
         * Thread[executor-thread-1,5,main] but is not allowed for language(s)
         * js.
         */
        private @Getter(value = AccessLevel.PRIVATE) final AtomicBoolean opened = new AtomicBoolean(false);

        public ContextWrapper(@NotBlank int id, @NotNull Context context, @Nullable OutputStream stdout,
                @Nullable OutputStream stderr, @Nullable Map<String, Object> metadata) {
            this.id = id;
            this.name = "graal-ctx-pool-" + id;
            this.context = notNullOf(context, "context");
            this.stdout = stdout;
            this.stderr = stderr;
            this.metadata = metadata;
        }

        @Override
        public synchronized void close() throws IOException {
            if (opened.compareAndSet(true, false)) {
                try {
                    // see:https://github.com/oracle/graaljs/blob/master/graal-js/src/com.oracle.truffle.js.test.threading/src/com/oracle/truffle/js/test/threading/ConcurrentAccess.java#L202
                    getContext().leave();
                    this.opened.set(false);
                } catch (Throwable ex) {
                    log.warn(format("Unable to closing context of %s", name), ex);
                }
            } else {
                throw new Error("Should't to be here");
            }
        }

        public boolean isOpened() {
            return this.opened.get();
        }

        public synchronized void open() {
            if (opened.compareAndSet(false, true)) {
                try {
                    // see:https://github.com/oracle/graaljs/blob/master/graal-js/src/com.oracle.truffle.js.test.threading/src/com/oracle/truffle/js/test/threading/ConcurrentAccess.java#L198
                    getContext().enter();
                } catch (Throwable ex) {
                    log.warn(format("Unable to opening context of %s", name), ex);
                    throw ex;
                }
            } else {
                throw new Error("Should't to be here");
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

    @Getter
    public static class NoPolyglotContextException extends RuntimeException {
        private static final long serialVersionUID = 5177120828249689148L;

        public NoPolyglotContextException(String message) {
            super(message);
        }

        public NoPolyglotContextException(String message, Throwable cause) {
            super(message, cause);
        }

        public NoPolyglotContextException(Throwable cause) {
            super(cause);
        }

    }

}
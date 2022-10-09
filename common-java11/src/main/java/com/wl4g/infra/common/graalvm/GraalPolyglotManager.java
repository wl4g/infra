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
package com.wl4g.infra.common.graalvm;

import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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

    public GraalPolyglotManager(@Min(0) int initSize, @Min(1) int maxSize, Boolean allowIO, String... permittedLanguages) {
        this(initSize, maxSize, () -> Context.newBuilder(permittedLanguages).allowIO(allowIO).build());
    }

    public GraalPolyglotManager(@Min(0) int initSize, @Min(1) int maxSize, String... permittedLanguages) {
        this(initSize, maxSize, () -> Context.newBuilder(permittedLanguages).allowAllAccess(true).build());
    }

    public GraalPolyglotManager(@Min(0) int initSize, @Min(1) int maxSize, Callable<Context> instantiator) {
        try {
            log.info("Initialzing graalvm polyglot context pool ...");
            this.contextPool = new SimpleFastContextPool(initSize, maxSize, instantiator);
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

    public ContextWrapper getContext() {
        return contextPool.take();
    }

    @Slf4j
    public static class SimpleFastContextPool implements AutoCloseable {

        private final ContextWrapper[] contextCached;

        private final int initSize;

        private final int maxSize;

        private final AtomicInteger totalSize = new AtomicInteger(0);

        private final Callable<Context> instantiator;

        public SimpleFastContextPool(@Min(0) int initSize, @Min(1) int maxSize, Callable<Context> instantiator) {
            isTrue(initSize > 0, "initSize >= 0");
            isTrue(maxSize >= 1, "maxSize >= 1");
            notNullOf(instantiator, "instantiator");
            this.initSize = initSize;
            this.maxSize = maxSize;
            this.instantiator = instantiator;
            this.contextCached = new ContextWrapper[maxSize];
            init();
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

        public ContextWrapper take() {
            return take(true);
        }

        protected ContextWrapper take(boolean must) {
            // Gets statistics
            int usedSize = 0;
            ContextWrapper takeContext = null;
            for (int i = 0; i < contextCached.length; i++) {
                ContextWrapper context = contextCached[i];
                if (nonNull(context)) {
                    if (context.isOpened()) {
                        ++usedSize;
                    } else {
                        takeContext = context;
                    }
                }
            }
            // New instantiate context.
            if (checkNewInstantiate(takeContext, usedSize)) { // limit-max
                synchronized (this) {
                    if (checkNewInstantiate(takeContext, usedSize)) { // limit-max
                        takeContext = contextCached[totalSize.get()] = newInstantiate();
                    }
                }
            }
            if (nonNull(takeContext)) {
                takeContext.open();
                return takeContext;
            }
            if (must) {
                throw new IllegalStateException("Could not got context from pool");
            }
            return null;
        }

        private void init() {
            for (int i = 0; i < initSize; i++) {
                contextCached[totalSize.get()] = newInstantiate();
            }
        }

        private ContextWrapper newInstantiate() {
            try {
                return new ContextWrapper("context-pool-" + totalSize.getAndIncrement(), instantiator.call());
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

    @Getter
    @ToString
    @AllArgsConstructor
    public static class ContextWrapper implements Closeable {
        private String id;
        private Context context;

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
        public void close() throws IOException {
            // Mark closed with pool.
            this.opened.set(false);
            try {
                getContext().leave();
            } catch (IllegalStateException e) {
                // Ignore
            }
        }

        public boolean isOpened() {
            return this.opened.get();
        }

        public void open() {
            this.opened.set(true);
            try {
                getContext().enter();
            } catch (IllegalStateException e) {
                // Ignore
            }
        }

        public Value eval(Source source) throws IOException {
            return getContext().eval(source);
        }

        public Value eval(String languageId, CharSequence source) throws IOException {
            return getContext().eval(languageId, source);
        }

        public Value parse(Source source) throws PolyglotException, IOException {
            return getContext().parse(source);
        }

        public Value parse(String languageId, CharSequence source) throws IOException {
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

}

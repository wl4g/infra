/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.common.eventbus;

import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * {@link EventBusSupport}
 *
 * @author Wangl.sir <James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2020-08-18
 * @since
 */
public class EventBusSupport implements Closeable {

    /** {@link EventBus} */
    protected final EventBus bus;

    /** {@link ThreadPoolExecutor} */
    protected ThreadPoolExecutor executor;

    public EventBusSupport(int eventThreads) {
        this(null, eventThreads);
    }

    public EventBusSupport(String prefix, int eventThreads) {
        isTrueOf(eventThreads > 0, "eventThreads >0");
        this.bus = initEventBus(prefix, eventThreads);
    }

    /**
     * Gets or create default singleton instance of {@link EventBusSupport}.
     * 
     * @param config
     * @return
     */
    public static EventBusSupport getDefault() {
        if (isNull(DEFAULT)) { // Single checked
            synchronized (EventBusSupport.class) {
                if (isNull(DEFAULT)) { // Double checked
                    DEFAULT = new EventBusSupport(1);
                }
            }
        }
        return DEFAULT;
    }

    /**
     * Gets {@link EventBus} instance.
     * 
     * @return
     */
    public EventBus getBus() {
        return bus;
    }

    /**
     * Registers all subscriber methods. </br>
     * see {@link EventBus#register(Object)}
     * 
     * @param objects
     *            object whose subscriber methods should be registered.
     */
    public void register(Object... objects) {
        if (nonNull(objects)) {
            for (Object obj : objects) {
                this.bus.register(obj);
            }
        }
    }

    /**
     * Unregisters all subscriber methods. </br>
     * see {@link EventBus#unregister(Object)}
     * 
     * @param objects
     *            object whose subscriber methods should be registered.
     */
    public void unregister(Object... objects) {
        if (nonNull(objects)) {
            for (Object obj : objects) {
                this.bus.unregister(obj);
            }
        }
    }

    /**
     * Post events to bus. </br>
     * see {@link EventBus#post(Object)}
     * 
     * @param events
     */
    public void post(Object... events) {
        if (isActive()) {
            for (Object event : events) {
                getBus().post(event);
            }
        }
    }

    /**
     * Check {@link EventBus} worker is active?
     * 
     * @return
     */
    public boolean isActive() {
        return nonNull(executor) && !executor.isShutdown();
    }

    @Override
    public void close() throws IOException {
        if (isActive()) {
            executor.shutdown();
        }
    }

    /**
     * Init create {@link EventBus}
     * 
     * @param prefix
     * @param eventThreads
     * @return
     */
    private final EventBus initEventBus(String prefix, int eventThreads) {
        String _prefix = isBlank(prefix) ? "eventbus" : prefix;
        final AtomicInteger incr = new AtomicInteger(0);
        this.executor = new ThreadPoolExecutor(eventThreads, eventThreads, 0, MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread t = new Thread(r, _prefix.concat("-").concat(valueOf(incr.getAndIncrement())));
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });
        return new AsyncEventBus(getClass().getSimpleName(), executor);
    }

    /** Single default instance of {@link EventBusSupport} */
    private static volatile EventBusSupport DEFAULT;

}

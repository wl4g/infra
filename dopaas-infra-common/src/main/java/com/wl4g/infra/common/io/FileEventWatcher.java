/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.infra.common.io;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.wl4g.infra.common.eventbus.EventBusSupport;
import com.wl4g.infra.common.log.SmartLogger;

/**
 * {@link FileEventWatcher}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-02-25 v1.0.0
 * @since v1.0.0
 */
@SuppressWarnings("unchecked")
public class FileEventWatcher implements Runnable, Closeable {
    private final SmartLogger log = getLogger(getClass());

    private final List<Object> listeners = new ArrayList<>(4);
    private final File targetdir;
    private final EventBusSupport eventbus;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread scanner;

    public FileEventWatcher(File targetdir) {
        this(targetdir, 1);
    }

    public FileEventWatcher(File target, int eventThreads) {
        this.targetdir = notNullOf(target, "target");
        this.eventbus = new EventBusSupport(eventThreads);
    }

    public FileEventWatcher addListenrs(Object... listeners) {
        this.listeners.addAll(safeArrayToList(listeners));
        return this;
    }

    public FileEventWatcher clearListeners() {
        this.listeners.clear();
        return this;
    }

    @Override
    public final void run() {
        if (!targetdir.exists()) {
            targetdir.mkdirs();
        } else if (!targetdir.isDirectory()) {
            throw new IllegalStateException(format("Watching target: %s is not a directory.", targetdir));
        }
        if (running.compareAndSet(false, true)) {
            eventbus.register(listeners.toArray());

            scanner = new Thread(() -> {
                try {
                    WatchService watcher = FileSystems.getDefault().newWatchService();
                    targetdir.toPath().register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                    while (!scanner.isInterrupted()) {
                        WatchKey key = watcher.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            log.debug("event kind: {}, context: {}", event.kind(), event.context());
                            eventbus.getBus().post(new FileChangedEvent((Kind<Path>) event.kind(), event.context()));
                        }
                        key.reset();
                    }
                } catch (Exception e) {
                    log.error(format("Failed to watching process. - %s", targetdir), e);
                }
            });
            scanner.setDaemon(true);
            scanner.start();
        }
    }

    @Override
    public void close() throws IOException {
        scanner.interrupt();
    }

    public static class FileChangedEvent extends EventObject {
        private static final long serialVersionUID = 5522604006585596093L;
        private final WatchEvent.Kind<Path> eventType;

        public FileChangedEvent(WatchEvent.Kind<Path> eventType, Object source) {
            super(source);
            this.eventType = eventType;
        }

        public WatchEvent.Kind<Path> getEventType() {
            return eventType;
        }

        @Override
        public Path getSource() {
            return (Path) super.getSource();
        }

    }

}

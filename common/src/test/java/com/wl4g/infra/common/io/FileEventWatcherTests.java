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
package com.wl4g.infra.common.io;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import com.google.common.eventbus.Subscribe;
import com.wl4g.infra.common.io.FileEventWatcher.FileChangedEvent;

public class FileEventWatcherTests {

    @Test
    public void testWatchFiles() throws Exception {
        // Initial target.
        File targetdir = new File(SystemUtils.JAVA_IO_TMPDIR.concat("/").concat(FileEventWatcherTests.class.getSimpleName()));
        targetdir.mkdirs();
        System.out.println(format("Created target directory: %s", targetdir));
        File targetfile1 = File.createTempFile("application", ".yaml", targetdir);
        System.out.println(format("Created target file1: %s", targetfile1));

        // Watching.
        try (FileEventWatcher watcher = new FileEventWatcher(singletonList(targetdir));) {
            watcher.addListenrs(new MyFileEventListener()).run();

            // Testing change.
            Thread updater = new Thread(() -> {
                try {
                    System.out.println(format("Started testing updater ..."));
                    for (int i = 0; i < 10; i++) {
                        String content = "name: 'Im is jack " + i + "'";
                        System.out.println(format("%s => %s", content, targetfile1));
                        FileIOUtils.writeFile(targetfile1, content);
                        Thread.sleep(2000L);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            updater.start();
            updater.join();
        }

    }

    static class MyFileEventListener {
        @Subscribe
        public void onEvent(FileChangedEvent event) {
            System.out.println(event.getEventType() + ", " + event.getSource());
        }
    }

}
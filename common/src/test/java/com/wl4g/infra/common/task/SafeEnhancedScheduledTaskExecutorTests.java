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
package com.wl4g.infra.common.task;

import static java.lang.System.currentTimeMillis;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class SafeEnhancedScheduledTaskExecutorTests {

    @Test
    public void testScheduleQueueRejected() throws Exception {
        CountDownLatch latch = new CountDownLatch(20);

        SafeScheduledTaskPoolExecutor executor = createSafeEnhancedScheduledExecutor(2);

        for (int i = 0; i < 20; i++) {
            final String idStr = "testjob-" + i;
            executor.submit(new Runnable() {
                private String id = idStr;

                @Override
                public void run() {
                    try {
                        System.out.println("Starting... testjob-" + id);
                        Thread.sleep(3000L);
                        System.out.println("Completed. testjob-" + id);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    @Test
    public void testScheduleWithFixedErrorInterrupted() throws Exception {
        CountDownLatch latch = new CountDownLatch(3);

        SafeScheduledTaskPoolExecutor executor = createSafeEnhancedScheduledExecutor(2);

        // Task1(Error):
        executor.scheduleAtFixedRate(() -> {
            System.out.println(currentTimeMillis() + " - Error of task1..." + executor);
            latch.countDown();
            throw new RuntimeException(currentTimeMillis() + " - Error of task1...");
        }, 1, 2, TimeUnit.SECONDS);

        // Task2(Normal):
        executor.scheduleAtFixedRate(() -> {
            System.out.println(currentTimeMillis() + " - Normal of task2..." + executor);
            latch.countDown();
        }, 1, 2, TimeUnit.SECONDS);

        // Task3(Normal):
        executor.scheduleAtFixedRate(() -> {
            System.out.println(currentTimeMillis() + " - Normal of task3..." + executor);
            latch.countDown();
        }, 1, 2, TimeUnit.SECONDS);

        latch.await();
        executor.shutdown();
    }

    @Test
    public void testScheduleWithRandomErrorInterrupted() throws Exception {
        CountDownLatch latch = new CountDownLatch(3);

        SafeScheduledTaskPoolExecutor executor = createSafeEnhancedScheduledExecutor(2);

        // Task1(Error):
        executor.scheduleAtRandomRate(() -> {
            System.out.println(currentTimeMillis() + " - Error of task1..." + executor);
            latch.countDown();
            throw new RuntimeException(currentTimeMillis() + " - Error of task1...");
        }, 1, 1, 2, TimeUnit.SECONDS);

        // Task2(Normal):
        executor.scheduleAtRandomRate(() -> {
            System.out.println(currentTimeMillis() + " - Normal of task2..." + executor);
            latch.countDown();
        }, 1, 1, 6, TimeUnit.SECONDS);

        // Task3(Normal):
        executor.scheduleAtRandomRate(() -> {
            System.out.println(currentTimeMillis() + " - Normal of task3..." + executor);
            latch.countDown();
        }, 1, 1, 6, TimeUnit.SECONDS);

        latch.await();
        executor.shutdown();
    }

    private static SafeScheduledTaskPoolExecutor createSafeEnhancedScheduledExecutor(int concurrencyPoolSize) throws Exception {
        return new SafeScheduledTaskPoolExecutor(concurrencyPoolSize, 0L, Executors.defaultThreadFactory(), 2,
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        System.err.println("ERROR ==>> " + r);
                    }
                });
    }

}
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

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.wl4g.infra.common.lang.ThreadUtils2;
import com.wl4g.infra.common.task.SafeScheduledTaskPoolExecutor.CompleteResult;

import lombok.AllArgsConstructor;
import lombok.ToString;

public class SafeScheduledTaskPoolExecutorTests {

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
                        System.out.println("Starting... " + id);
                        Thread.sleep(3000L);
                        System.out.println("Completed. " + id);
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

    @Test
    public void testCountDownLatch() throws Exception {
        long begin = currentTimeMillis();
        int total = 3;
        CountDownLatch latch = new CountDownLatch(total);
        for (int i = 0; i < total; i++) {
            final int index = i;
            new Thread(() -> {
                System.out.println(System.nanoTime() + " starting testjob-" + index + ", latch: " + latch.getCount());
                ThreadUtils2.sleep((index + 1) * 1000L);
                latch.countDown();
                System.out.println(System.nanoTime() + " finished testjob-" + index + ", latch: " + latch.getCount());
            }).start();
        }

        // latch.await();
        // assert !latch.await(2000L, TimeUnit.MILLISECONDS);
        assert latch.await(4000L, TimeUnit.MILLISECONDS);

        long now = currentTimeMillis();
        System.out.println(format("%s completed cost: %sms, latch: %s", System.nanoTime(), (now - begin), latch.getCount()));
    }

    @Test
    public void testSubmitForComplete() throws Exception {
        long begin = currentTimeMillis();

        SafeScheduledTaskPoolExecutor executor = createSafeEnhancedScheduledExecutor(3);

        List<Callable<String>> jobs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final String idStr = "testjob-" + i;
            jobs.add(new TestJob(idStr));
        }

        final CompleteResult<String> result = executor.submitForComplete(jobs, 10_000);
        System.out.println("result => " + result);
        long cost = currentTimeMillis() - begin;
        System.out.println("cost: " + cost + "ms");

        executor.shutdown();

        assert jobs.size() == result.getCompleted().size() + result.getUncompleted().size();
    }

    static SafeScheduledTaskPoolExecutor createSafeEnhancedScheduledExecutor(int concurrencyPoolSize) throws Exception {
        return new SafeScheduledTaskPoolExecutor(concurrencyPoolSize, 0L, Executors.defaultThreadFactory(), 2,
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        System.err.println("ERROR ==>> " + r);
                    }
                });
    }

    @ToString
    @AllArgsConstructor
    static class TestJob implements Callable<String> {
        String id;

        @Override
        public String call() {
            System.out.println("Starting... " + id);
            ThreadUtils2.sleepRandom(1_000L, 2_000L);
            System.out.println("Completed. " + id);
            return id;
        }
    }
}
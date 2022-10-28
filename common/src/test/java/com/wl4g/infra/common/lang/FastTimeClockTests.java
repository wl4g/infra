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
package com.wl4g.infra.common.lang;

import static java.lang.String.format;
import static java.lang.System.out;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * {@link FastTimeClockTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2021-09-17 v1.0.0
 * @since v1.0.0
 */
public class FastTimeClockTests {

    @Test
    public void testFastCurrentTimeMillisalmostEqToNativeRealMillis() {
        long fastCurrentTimeMillis = FastTimeClock.currentTimeMillis();
        long currentTimeMillis = System.currentTimeMillis();
        System.out.println(format("Fast Current Time Millis: %s", fastCurrentTimeMillis));
        System.out.println(format("     Current Time Millis: %s", currentTimeMillis));

        assert (fastCurrentTimeMillis - currentTimeMillis) < 2 : "Wrong fast current time millis";

        System.out.println(format("         After Nano Time: %s", System.nanoTime()));

        System.out.println("successful pass tested and finished!");
    }

    @Test
    public void test100WFastCurrentTimeMillisalmostEqToNativeRealMillis() throws InterruptedException {
        int maxThreads = 50;
        CountDownLatch latch = new CountDownLatch(maxThreads);
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        for (int t = 0; t < maxThreads; t++) {
            final int index = t;
            System.out.println(format("Thread %s starting ...", index));
            executor.submit(() -> {
                try {
                    // System.out.println(format("Thread %s started.", index));
                    for (int i = 0; i < 100_0000; i++) {
                        long fastCurrentTimeMillis = FastTimeClock.currentTimeMillis();
                        long currentTimeMillis = System.currentTimeMillis();

                        assert (fastCurrentTimeMillis - currentTimeMillis) < 2 : "Wrong fast current time millis";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(2L, TimeUnit.MINUTES);
        System.out.println("successful pass tested and finished!");
    }

    @Test
    public void testNativeCurrentTimeMillis() {
        int count = 1_000_000_000; // 100亿次
        long begin = System.nanoTime();
        for (int i = 0; i < count; i++) {
            System.currentTimeMillis();
        }
        long cost = (long) (Math.abs(System.nanoTime() - begin) / 1e6);
        out.println(format("Testing currentTimeMillis() (%s) completed. cost: %sms", count, cost));
    }

    @Test
    public void testNativeNanotime() {
        int count = 1_000_000_000; // 100亿次
        long begin = System.nanoTime();
        for (int i = 0; i < count; i++) {
            System.nanoTime();
        }
        long cost = (long) (Math.abs(System.nanoTime() - begin) / 1e6);
        out.println(format("Testing nanoTime() (%s) completed. cost: %sms", count, cost));
    }

    @Test
    public void testFastTime() {
        int count = 1_000_000_000; // 100亿次
        long begin = System.nanoTime();
        for (int i = 0; i < count; i++) {
            FastTimeClock.currentTimeMillis();
        }
        long cost = (long) (Math.abs(System.nanoTime() - begin) / 1e6);
        out.println(format("Testing fastTime() (%s) completed. cost: %sms", count, cost));
    }

}

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
package com.wl4g.infra.common.task;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.wl4g.infra.common.lang.ThreadUtils2;

/**
 * {@link GenericTaskRunnerTests}
 * 
 * @author James Wong
 * @version 2023-01-06
 * @since v1.0.0
 */
public class GenericTaskRunnerTests {

    @Test
    public void testNewDefaultSchedulerExecutor() throws Exception {
        final SafeScheduledTaskPoolExecutor executor = GenericTaskRunner.newDefaultScheduledExecutor("test-", 5, 5);
        final CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            final int index = i;
            executor.submit(() -> {
                System.out.println("The run of : " + index);
                ThreadUtils2.sleepRandom(1000L, 2000L);
                latch.countDown();
            });
        }
        executor.shutdown();
        latch.await();
    }

}

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
package com.wl4g.infra.common.lang;

/**
 * {@link FastTimeClock}
 * 
 * This class is used to solve the problem under the performance of calling
 * {@link java.lang.System#currentTimeMillis()} in some OS platform kernels, and
 * quickly calculate the current absolute milliseconds by using the relative
 * nanosecond time and initial gap of the JVM
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2021-09-17 v1.0.0
 * @since v1.0.0
 * @see https://stackoverflow.com/questions/510462/is-system-nanotime-completely-useless
 */
public abstract class FastTimeClock {

    /**
     * The difference between any absolute time and relative nanosecond time at
     * JVM startup.
     */
    private static final long vmTimeDiff = (long) (System.currentTimeMillis() - System.nanoTime() / 100_0000);

    public static long currentTimeMillis() {
        return (long) ((System.nanoTime() / 100_0000) + vmTimeDiff);
    }

}

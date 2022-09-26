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
package com.wl4g.infra.metrics.health.timed;

/**
 * {@link TimedHealthProperties}
 * 
 * Deprecated: It should not be checked on the indicator monitoring client side,
 * but should be checked uniformly on the prometheus server side (which is also
 * convenient for unified configuration)
 * 
 * @author Wangl.sir &lt;James Wong <jameswong1376@gmail.com>&gt;
 * @version 2021-10-30 v1.0.0
 * @since v1.0.0
 */
@Deprecated
public class TimedHealthProperties {

    public static final int DEFAULT_SAMPLES = 32;
    public static final long DEFAULT_TIMEOUT_THRESHOLD = 5_000L;

    /**
     * AOP intercepts the number of historical records saved by statistical
     * calls.
     */
    private int samples = DEFAULT_SAMPLES;

    /**
     * AOP intercept call time consuming timeout alarm threshold.
     */
    private long timeoutThresholdMs = DEFAULT_TIMEOUT_THRESHOLD;

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public long getTimeoutThresholdMs() {
        return timeoutThresholdMs;
    }

    public void setTimeoutThresholdMs(long timeoutThresholdMs) {
        this.timeoutThresholdMs = timeoutThresholdMs;
    }

}
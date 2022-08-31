/*
 * Copyright 2017 ~ 2050 the original author or authors <James Wong@gmail.com, 983708408@qq.com>.
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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import com.wl4g.infra.metrics.health.HealthUtil;

import lombok.CustomLog;

/**
 * Analysis and statistical call time dimension related health messages .
 * 
 * Deprecated: It should not be checked on the indicator monitoring client side,
 * but should be checked uniformly on the prometheus server side (which is also
 * convenient for unified configuration)
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年6月1日
 * @since
 */
@CustomLog
@Deprecated
public class TimedHealthIndicator extends AbstractHealthIndicator {

    private final Map<String, Deque<Long>> records = new ConcurrentHashMap<>(32);
    private TimedHealthProperties config;

    public TimedHealthIndicator(TimedHealthProperties config) {
        this.config = notNullOf(config, "simpleTimingMetricsConfig");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        try {
            // Gets the statistical (MAX/MIN/AVG/LATEST/..).
            TimedStat stat = getLargestStat();
            log.debug("Times stat: {}", () -> toJSONString(stat));

            if (isNull(stat)) {
                HealthUtil.up(builder, "Healthy (No telemetry data)");
                return;
            } else if (stat.getMax() < config.getTimeoutThresholdMs()) {
                HealthUtil.up(builder, "Healthy");
            } else {
                HealthUtil.down(builder,
                        new StringBuilder("Method ").append(stat.getMetricsName())
                                .append(" executes ")
                                .append(stat.getLatest())
                                .append("ms with a response exceeding the threshold value of ")
                                .append(config.getTimeoutThresholdMs())
                                .append("ms.")
                                .toString());
                // When the timeout exception is detected, the exception record
                // is cleared.
                resetIfNecessary(stat);
            }
            builder.withDetail("Method", stat.getMetricsName())
                    .withDetail("Least", stat.getMin())
                    .withDetail("Largest", stat.getMax())
                    .withDetail("Avg", stat.getAvg())
                    .withDetail("Latest", stat.getLatest())
                    .withDetail("Samples", stat.getSamples())
                    .withDetail("Threshold", config.getTimeoutThresholdMs() + "ms");
        } catch (Exception ex) {
            HealthUtil.down(builder, "UnHealthy", ex);
            log.error("Failed to detected timeout.method.calling", ex);
        }
    }

    public void record(String metricName, long time) {
        int latestCount = config.getSamples();
        log.debug("Add times metric: {}, latestCount: {}, time={}", metricName, latestCount, time);

        Deque<Long> deque = records.get(metricName);
        if (isNull(deque)) {
            synchronized (this) {
                deque = records.get(metricName);
                if (isNull(deque)) {
                    records.put(metricName, deque = new ConcurrentLinkedDeque<>());
                }
            }
        }

        // Overflow check
        if (deque.size() >= (latestCount - 1)) {
            deque.poll(); // Remove first
        }
        deque.offer(time);
    }

    /**
     * Gets the most statistics largest time out messages (including average
     * time, maximum duration, and longest time) of `latestMeasureCount` call
     * records.
     * 
     * @return
     */
    protected TimedStat getLargestStat() {
        List<TimedStat> stats = new ArrayList<>();
        // Calculate the maximum value of each execution record queue.
        for (Entry<String, Deque<Long>> ent : records.entrySet()) {
            Deque<Long> deque = ent.getValue();
            if (!deque.isEmpty()) {
                long queueMax = deque.stream().reduce(Long::max).get();
                stats.add(new TimedStat(ent.getKey(), queueMax));
            }
        }
        if (stats.isEmpty()) {
            return null;
        }

        // Gets the maximum value in the list of the maximum values in all
        // queues.
        TimedStat statMax = Collections.max(stats, MAX_COMPARATOR);
        Deque<Long> deque = records.get(statMax.getMetricsName());

        statMax.setSamples(deque.size());
        statMax.setMin(deque.stream().reduce(Long::min).get());
        statMax.setLatest(deque.peekLast());
        // Average length of time.
        long totalTime = deque.stream().collect(Collectors.summingLong(Long::longValue));
        long count = deque.stream().count();
        statMax.setAvg(BigDecimal.valueOf(totalTime / count).setScale(2, RoundingMode.HALF_EVEN).longValue());

        // Remove current-largest metrics(timeouts record).
        deque.remove(statMax.getMax());
        // Deque.clear();
        return statMax;
    }

    protected void resetIfNecessary(TimedStat stat) {
        Deque<Long> deque = records.get(stat.getMetricsName());
        if (deque != null && !deque.isEmpty()) {
            deque.remove(stat.getMax());
        }
    }

    private static final Comparator<TimedStat> MAX_COMPARATOR = new Comparator<TimedStat>() {
        @Override
        public int compare(TimedStat o1, TimedStat o2) {
            return (int) (o1.getMax() - o2.getMax());
        }
    };

}
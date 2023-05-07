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
package com.wl4g.infra.common.metrics;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.wl4g.infra.common.net.InetUtils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.prometheus.PrometheusCounter;
import io.micrometer.prometheus.PrometheusDistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.prometheus.PrometheusTimer;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.SummaryMetricFamily;
import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.Getter;

/**
 * {@link PrometheusMeterFacade}, Tends to use Prometheus standards.
 * 
 * Counter, Timer, Gauge, DistributionSummary, etc.
 * 
 * {@link PrometheusMeterRegistry}
 * 
 * @author &lt;James Wong James Wong <jameswong1376@gmail.com>&gt;
 * @version 2021-11-16 v1.0.0
 * @since v1.0.0
 */
@Getter
@CustomLog
public class PrometheusMeterFacade {
    private final String serviceId;
    private final boolean secure;
    private final int port;
    private final PrometheusMeterRegistry meterRegistry;
    private final Map<String, MetricFamilySamples> sampleRegistry = new ConcurrentHashMap<>(16);
    private final InetUtils inet;
    private final LocalInstanceSpec localSpec;

    public PrometheusMeterFacade(PrometheusMeterRegistry meterRegistry, String serviceId, boolean secure, InetUtils inet,
            int port) {
        this.meterRegistry = notNullOf(meterRegistry, "meterRegistry");
        this.serviceId = hasTextOf(serviceId, "serviceId");
        this.secure = secure;
        this.inet = notNullOf(inet, "inet");
        this.port = port;
        this.localSpec = createLocalInstanceSpec();
    }

    protected LocalInstanceSpec createLocalInstanceSpec() {
        String host = inet.findFirstNonLoopbackHostInfo().getHostname();
        String instanceId = host.concat(":").concat(valueOf(port));
        return new LocalInstanceSpec(instanceId, serviceId, host, port, secure);
    }

    protected String[] applyDefaultTags(String... tags) {
        List<String> _tags = Lists.newArrayList(tags);
        _tags.add(TAG_SELF_ID);
        _tags.add(localSpec.getInstanceId());
        return _tags.toArray(new String[0]);
    }

    //
    // --- The Meter Metrics Active Recorder. ---
    //

    // --- Counter. ---

    public Counter counter(String name, String help, String... tags) {
        try {
            return Counter.builder(name).description(help).tags(applyDefaultTags(tags)).register(meterRegistry);
        } catch (Throwable e) {
            log.error(format("Unable to counter meter for metrics: '%s', help: '%s', tags: %s", name, help, asList(tags)), e);
            return EMPTY_COUNTER;
        }
    }

    // --- Gauge. ---

    public Gauge gauge(String name, String help, double number, String... tags) {
        try {
            return gauge(name, help, () -> number, tags);
        } catch (Throwable e) {
            log.error(format("Unable to gauge meter for metrics: '%s', help: '%s', tags: %s", name, help, asList(tags)), e);
            return EMPTY_GAUGE;
        }
    }

    public Gauge gauge(String name, String help, Supplier<Number> supplier, String... tags) {
        try {
            return Gauge.builder(name, supplier).description(help).tags(applyDefaultTags(tags)).register(meterRegistry);
        } catch (Throwable e) {
            log.error(format("Unable to gauge meter for metrics: '%s', help: '%s', tags: %s", name, help, asList(tags)), e);
            return EMPTY_GAUGE;
        }
    }

    // --- Timer. ---

    public Timer timer(String name, String help, double[] percentiles, String... tags) {
        try {
            return Timer.builder(name)
                    .description(help)
                    .tags(applyDefaultTags(tags))
                    .publishPercentiles(percentiles)
                    .publishPercentileHistogram()
                    .register(meterRegistry);
        } catch (Throwable e) {
            log.error(format("Unable to timer meter for metrics: '%s', help: '%s', tags: %s", name, help, asList(tags)), e);
            return EMPTY_TIMER;
        }
    }

    // --- Summary. ---

    /**
     * Used default configuration refer to:
     * {@link DistributionStatisticConfig#DEFAULT}
     * 
     * @param name
     * @param help
     * @param scale
     * @param slos
     * @param tags
     * @return
     */
    public DistributionSummary summary(String name, String help, double scale, double[] percentiles, String... tags) {
        return DistributionSummary.builder(name)
                .description(help)
                .tags(applyDefaultTags(tags))
                .publishPercentiles(percentiles)
                .publishPercentileHistogram()
                .scale(scale)
                .register(meterRegistry);
    }

    /**
     * Used default configuration refer to:
     * {@link DistributionStatisticConfig#DEFAULT}
     * 
     * @param name
     * @param help
     * @param scale
     * @param slos
     * @param tags
     * @return
     */
    public DistributionSummary summarySlos(String name, String help, double scale, double[] slos, String... tags) {
        return DistributionSummary.builder(name)
                .description(help)
                .tags(applyDefaultTags(tags))
                .serviceLevelObjectives(slos)
                .scale(scale)
                .register(meterRegistry);
    }

    public DistributionSummary summary(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig, double scale) {
        List<Tag> tags = Lists.newArrayList(id.getTags());
        tags.add(new ImmutableTag(TAG_SELF_ID, localSpec.getInstanceId()));
        Meter.Id newId = new Meter.Id(id.getName(), Tags.of(tags), id.getBaseUnit(), id.getDescription(), id.getType());
        return meterRegistry.newDistributionSummary(newId, distributionStatisticConfig, scale);
    }

    //
    // --- The Metrics Passive Collector. ---
    //

    public GaugeMetricFamily getGauge(String name, String help, String... labelNames) {
        MetricFamilySamples samples = sampleRegistry.get(name);
        if (isNull(samples)) {
            synchronized (this) {
                samples = sampleRegistry.get(name);
                if (isNull(samples)) {
                    sampleRegistry.put(name, samples = new GaugeMetricFamily(name, help, asList(labelNames)));
                }
            }
        }
        samples = new GaugeMetricFamily(name, help, asList(labelNames));
        return (GaugeMetricFamily) samples;
    }

    public CounterMetricFamily getCounter(String name, String help, String... labelNames) {
        MetricFamilySamples samples = sampleRegistry.get(name);
        if (isNull(samples)) {
            synchronized (this) {
                samples = sampleRegistry.get(name);
                if (isNull(samples)) {
                    sampleRegistry.put(name, samples = new CounterMetricFamily(name, help, asList(labelNames)));
                }
            }
        }
        return (CounterMetricFamily) samples;
    }

    public SummaryMetricFamily getSummary(String name, String help, String... labelNames) {
        MetricFamilySamples samples = sampleRegistry.get(name);
        if (isNull(samples)) {
            synchronized (this) {
                samples = sampleRegistry.get(name);
                if (isNull(samples)) {
                    sampleRegistry.put(name, samples = new SummaryMetricFamily(name, help, asList(labelNames)));
                }
            }
        }
        return (SummaryMetricFamily) samples;
    }

    //
    // --- Tools Function. ---
    //

    public static Counter newPrometheusCounter(Meter.Id id) {
        return newConstructor(PrometheusCounter.class, new Object[] { id });
    }

    public static Timer newPrometheusTimer(Meter.Id id) {
        return newConstructor(PrometheusTimer.class, new Object[] { id });
    }

    public static DistributionSummary newPrometheusDistributionSummary(Meter.Id id) {
        return newConstructor(PrometheusDistributionSummary.class, new Object[] { id });
    }

    @SuppressWarnings("unchecked")
    public static <T> T newConstructor(Class<T> clazz, Object... args) {
        try {
            Constructor<T> constructor = (Constructor<T>) CONSTRUCTOR_MAP.get(clazz);
            if (isNull(constructor)) {
                synchronized (PrometheusMeterFacade.class) {
                    if (isNull(constructor)) {
                        CONSTRUCTOR_MAP.put(clazz, constructor = getConstructor0(clazz));
                    }
                }
            }
            return (T) constructor.newInstance(args);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static <T> Constructor<T> getConstructor0(Class<T> clazz) throws NoSuchMethodException, SecurityException {
        Constructor<T> constructor = clazz.getDeclaredConstructor(Meter.Id.class);
        constructor.setAccessible(true);
        return constructor;
    }

    static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_MAP = new ConcurrentHashMap<>(8);

    @Getter
    @AllArgsConstructor
    protected static class LocalInstanceSpec {
        private String instanceId;
        private String serviceId;
        private String host;
        private int port;
        private boolean secure;
    }

    public static final String TAG_SELF_ID = "self";

    public static final Counter EMPTY_COUNTER = new Counter() {
        @Override
        public Id getId() {
            return null;
        }

        @Override
        public void increment(double amount) {
        }

        @Override
        public double count() {
            return 0;
        }
    };

    public static final Gauge EMPTY_GAUGE = new Gauge() {
        @Override
        public Id getId() {
            return null;
        }

        @Override
        public double value() {
            return 0;
        }
    };

    public static final Timer EMPTY_TIMER = new Timer() {

        @Override
        public Id getId() {
            return null;
        }

        @Override
        public HistogramSnapshot takeSnapshot() {
            return null;
        }

        @Override
        public void record(long amount, TimeUnit unit) {
        }

        @Override
        public <T> T record(Supplier<T> f) {
            return null;
        }

        @Override
        public <T> T recordCallable(Callable<T> f) throws Exception {
            return null;
        }

        @Override
        public void record(Runnable f) {
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public double totalTime(TimeUnit unit) {
            return 0;
        }

        @Override
        public double max(TimeUnit unit) {
            return 0;
        }

        @Override
        public TimeUnit baseTimeUnit() {
            return null;
        }
    };

}

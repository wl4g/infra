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
package com.wl4g.infra.metrics;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.springframework.core.env.Environment;

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
import io.micrometer.prometheus.PrometheusCounter;
import io.micrometer.prometheus.PrometheusDistributionSummary;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.micrometer.prometheus.PrometheusTimer;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.SummaryMetricFamily;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link MetricsFacade}, Tends to use Prometheus standards.
 * 
 * Counter, Timer, Gauge, DistributionSummary, etc.
 * 
 * {@link PrometheusMeterRegistry}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-11-16 v1.0.0
 * @since v1.0.0
 */
@Getter
public class MetricsFacade {

    private final Environment environment;
    private final PrometheusMeterRegistry meterRegistry;
    private final Map<String, MetricFamilySamples> sampleRegistry = new ConcurrentHashMap<>(16);
    private final InetUtils inet;
    private final LocalInstanceSpec localSpec;

    public MetricsFacade(Environment environment, PrometheusMeterRegistry registry, InetUtils inet) {
        this.environment = notNullOf(environment, "environment");
        this.meterRegistry = notNullOf(registry, "registry");
        this.inet = notNullOf(inet, "inet");
        this.localSpec = initLocalInstance();
    }

    protected LocalInstanceSpec initLocalInstance() {
        boolean secure = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        String serviceId = environment.getRequiredProperty("spring.application.name");
        String host = inet.findFirstNonLoopbackHostInfo().getHostname();
        int port = environment.getRequiredProperty("server.port", Integer.class);
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

    public Counter counter(String metricsName, String help, String... tags) {
        return Counter.builder(metricsName).description(help).tags(applyDefaultTags(tags)).register(meterRegistry);
    }

    // --- Gauge. ---

    public Gauge gauge(String metricsName, String help, double number, String... tags) {
        return gauge(metricsName, help, () -> number, tags);
    }

    public Gauge gauge(String metricsName, String help, Supplier<Number> supplier, String... tags) {
        return Gauge.builder(metricsName, supplier).description(help).tags(applyDefaultTags(tags)).register(meterRegistry);
    }

    // --- Timer. ---

    public Timer timer(String metricsName, String help, String... tags) {
        return Timer.builder(metricsName).description(help).tags(applyDefaultTags(tags)).register(meterRegistry);
    }

    // --- Summary. ---

    /**
     * Used default configuration refer to:
     * {@link DistributionStatisticConfig#DEFAULT}
     * 
     * @param name
     * @param tags
     * @return
     */
    public DistributionSummary summary(String metricsName, String help, double scale, String... tags) {
        return DistributionSummary.builder(metricsName).description(help).tags(applyDefaultTags(tags)).scale(scale).register(
                meterRegistry);
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

    public GaugeMetricFamily getGauge(String metricsName, String help, String... labelNames) {
        MetricFamilySamples samples = sampleRegistry.get(metricsName);
        if (isNull(samples)) {
            synchronized (this) {
                samples = sampleRegistry.get(metricsName);
                if (isNull(samples)) {
                    sampleRegistry.put(metricsName, samples = new GaugeMetricFamily(metricsName, help, asList(labelNames)));
                }
            }
        }
        samples = new GaugeMetricFamily(metricsName, help, asList(labelNames));
        return (GaugeMetricFamily) samples;
    }

    public CounterMetricFamily getCounter(String metricsName, String help, String... labelNames) {
        MetricFamilySamples samples = sampleRegistry.get(metricsName);
        if (isNull(samples)) {
            synchronized (this) {
                samples = sampleRegistry.get(metricsName);
                if (isNull(samples)) {
                    sampleRegistry.put(metricsName, samples = new CounterMetricFamily(metricsName, help, asList(labelNames)));
                }
            }
        }
        return (CounterMetricFamily) samples;
    }

    public SummaryMetricFamily getSummary(String metricsName, String help, String... labelNames) {
        MetricFamilySamples samples = sampleRegistry.get(metricsName);
        if (isNull(samples)) {
            synchronized (this) {
                samples = sampleRegistry.get(metricsName);
                if (isNull(samples)) {
                    sampleRegistry.put(metricsName, samples = new SummaryMetricFamily(metricsName, help, asList(labelNames)));
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
    static <T> T newConstructor(Class<T> clazz, Object... args) {
        try {
            Constructor<T> constructor = (Constructor<T>) CONSTRUCTOR_MAP.get(clazz);
            if (isNull(constructor)) {
                synchronized (MetricsFacade.class) {
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

}

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
package com.wl4g.infra.common.metrics;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;

import javax.validation.constraints.NotNull;

import org.junit.Before;
import org.junit.Test;

import com.wl4g.infra.common.net.InetUtils;
import com.wl4g.infra.common.net.InetUtils.InetUtilsProperties;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusCounter;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * {@link PrometheusMeterFacadeTests}
 * 
 * @author Wangl.sir &lt;James Wong <jameswong1376@gmail.com>&gt;
 * @version 2021-11-16 v1.0.0
 * @since v1.0.0
 * @see https://www.baeldung.com/micrometer
 */
public class PrometheusMeterFacadeTests {

    private @NotNull PrometheusMeterFacade facade;

    @Before
    public void init() {
        InetUtils inet = new InetUtils(new InetUtilsProperties());
        this.facade = new PrometheusMeterFacade(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT), "testApp", false, inet,
                8080);
    }

    @Test
    public void testNewConstructor() {
        Meter.Id id = new Meter.Id("test_metric1", Tags.empty(), "", "Nothing", Type.COUNTER);
        PrometheusCounter counter = PrometheusMeterFacade.newConstructor(PrometheusCounter.class, id);
        System.out.println(counter);
    }

    @Test
    public void testUseCounter() {
        // Gets or create counter by metrics name and tags.
        Counter counter = facade.counter("test_metric2", "", "key1", "value1");

        // increment by 1
        counter.increment(1);

        System.out.println(counter.count());
        System.out.println(toJSONString(counter));
    }

    @Test
    public void testUseTimer() {
        // Gets or create timer by metrics name and tags.
        Timer timer = facade.timer("test_metric3", "", new double[] { 0.3, 0.5, 0.9, 0.95 }, "key1", "value1");

        // For the convenience of calculation, the test data set are all
        // integers.
        timer.record(100, MILLISECONDS);
        timer.record(200, MILLISECONDS);
        timer.record(300, MILLISECONDS);
        timer.record(400, MILLISECONDS);
        timer.record(500, MILLISECONDS);
        timer.record(600, MILLISECONDS);
        timer.record(700, MILLISECONDS);
        timer.record(800, MILLISECONDS);
        timer.record(900, MILLISECONDS);
        timer.record(1000, MILLISECONDS);
        timer.record(1100, MILLISECONDS);
        timer.record(1200, MILLISECONDS);
        timer.record(1300, MILLISECONDS);
        timer.record(1400, MILLISECONDS);
        timer.record(1500, MILLISECONDS);
        timer.record(1600, MILLISECONDS);
        timer.record(1700, MILLISECONDS);
        timer.record(1800, MILLISECONDS);
        timer.record(1900, MILLISECONDS);
        timer.record(2000, MILLISECONDS);

        System.out.println("count: " + timer.count());
        System.out.println("total: " + timer.totalTime(MILLISECONDS));
        System.out.println("  max: " + timer.max(MILLISECONDS));
        System.out.println(" mean: " + timer.mean(MILLISECONDS));
        System.out.println(" json: " + toJSONString(timer));

        System.out.println("--------------------");
        HistogramSnapshot snapshot = timer.takeSnapshot();
        System.out.println("        snapshot: " + snapshot);
        System.out.println(" histogramCounts: " + asList(snapshot.histogramCounts()));
        System.out.println("percentileValues: " + asList(snapshot.percentileValues()));

        System.out.println("--------------------");
        snapshot.outputSummary(System.out, 1d);
    }

    @Test
    public void testUseGauge() {
        // Gets or create gauge by metrics name and tags, and record statistics
        // value.
        Gauge gauge = facade.gauge("test_metric4", "", 100.123d, "key1", "value1");
        System.out.println(gauge);
    }

    @Test
    public void testUseSummary() {
        // Gets or create distribution summary by metrics name and tags.
        DistributionSummary summary = facade.summary("test_metric5", "", 1d, new double[] { 0.2, 0.5, 0.9, 0.95 }, "key1",
                "value1");

        // statistics cost time
        summary.record(201);
        summary.record(101);
        summary.record(400);
        summary.record(400);
        summary.record(401);
        summary.record(403);
        summary.record(403);
        summary.record(403);
        summary.record(403);
        summary.record(403);
        summary.record(403);
        summary.record(403);
        summary.record(400);
        summary.record(500);
        summary.record(500);
        summary.record(502);
        summary.record(503);
        summary.record(502);

        System.out.println("count: " + summary.count());
        System.out.println("  max: " + summary.max());
        System.out.println(" mean: " + summary.mean());
        System.out.println(" json: " + toJSONString(summary));

        System.out.println("--------------------");
        HistogramSnapshot snapshot = summary.takeSnapshot();
        System.out.println("        snapshot: " + snapshot);
        System.out.println(" histogramCounts: " + asList(snapshot.histogramCounts()));
        System.out.println("percentileValues: " + asList(snapshot.percentileValues()));

        System.out.println("--------------------");
        snapshot.outputSummary(System.out, 1d);
    }

    @Test
    public void testUseSummary2() {
        // Gets or create distribution summary by metrics name and tags.
        DistributionSummary summary = facade.summarySlos("test_metric5", "", 0.1d, new double[] { 1, 10, 5 }, "key1", "value1");

        // statistics cost time
        summary.record(301.133d);
        summary.record(440.143d);
        summary.record(720.523d);
        summary.record(850.143d);
        summary.record(2340.156d);

        System.out.println("count: " + summary.count());
        System.out.println("  max: " + summary.max());
        System.out.println(" mean: " + summary.mean());
        System.out.println(" json: " + toJSONString(summary));

        System.out.println("--------------------");
        HistogramSnapshot snapshot = summary.takeSnapshot();
        System.out.println("        snapshot: " + snapshot);
        System.out.println(" histogramCounts: " + asList(snapshot.histogramCounts()));
        System.out.println("percentileValues: " + asList(snapshot.percentileValues()));

        System.out.println("--------------------");
        snapshot.outputSummary(System.out, 0.1d);
    }

    @Test
    public void testUseSummary3() {
        DistributionStatisticConfig dsconfig = DistributionStatisticConfig.builder()
                .percentilesHistogram(false)
                .percentilePrecision(1)
                .minimumExpectedValue(1.0)
                .maximumExpectedValue(Double.POSITIVE_INFINITY)
                .expiry(Duration.ofMinutes(2))
                .bufferLength(3)
                .build();

        // Gets or create distribution summary by configuration.
        Meter.Id id = new Meter.Id("test_metric6", Tags.empty(), "", "Nothing", Type.DISTRIBUTION_SUMMARY);
        DistributionSummary summary = facade.summary(id, dsconfig, 0.1d);

        // statistics cost time
        summary.record(300.123d);

        System.out.println("count: " + summary.count());
        System.out.println("  max: " + summary.max());
        System.out.println(" mean: " + summary.mean());
        System.out.println(" json: " + toJSONString(summary));

        System.out.println("--------------------");
        HistogramSnapshot snapshot = summary.takeSnapshot();
        System.out.println("        snapshot: " + snapshot);
        System.out.println(" histogramCounts: " + asList(snapshot.histogramCounts()));
        System.out.println("percentileValues: " + asList(snapshot.percentileValues()));

        System.out.println("--------------------");
        snapshot.outputSummary(System.out, 0.1d);
    }

}

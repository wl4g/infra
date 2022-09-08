/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <wanglsir@gmail.com>
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

import static java.util.Arrays.asList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

import io.micrometer.core.instrument.Counter;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import lombok.AllArgsConstructor;

@SpringBootApplication
public class PrometheusCollectorTests {

    public static void main(String[] args) {
        SpringApplication.run(PrometheusCollectorTests.class, args);
    }

    @Configuration
    static class TestCollectorAutoConfiguration {

        /**
         * @see {@link org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration}
         */
        @Bean
        public TestPrometheusCollector testPrometheusCollector(CollectorRegistry registry, MetricsFacade metricsFacade) {
            TestPrometheusCollector collector = new TestPrometheusCollector(metricsFacade);
            registry.register(collector);
            return collector;
        }
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {
        private @Autowired MetricsFacade metricsFacade;

        @RequestMapping("/hello")
        public String hello(@RequestParam(required = false, defaultValue = "james wrong") String name) {

            // for testing requests counter.

            Counter requestsCounter = metricsFacade.counter("test_metrics_requests", "The testing metrics requests.", "name",
                    name);
            requestsCounter.increment(1);

            return "hello: " + name;
        }
    }

    @AllArgsConstructor
    static class TestPrometheusCollector extends Collector {
        private final MetricsFacade metricsFacade;

        @Override
        public List<MetricFamilySamples> collect() {
            List<MetricFamilySamples> result = new LinkedList<>();

            // for testing dataSource connections states.

            GaugeMetricFamily dataSourceStateGauge = metricsFacade.getGauge("test_multi_datasource_state",
                    "The testing muti datasources connection states.", "dsname");
            result.add(dataSourceStateGauge);

            getAllDataSourceStatus().forEach((dsname, state) -> {
                List<String> labelValues = asList(dsname);
                dataSourceStateGauge.addMetric(labelValues, "active".equals(state) ? 1 : 0);
            });

            return result;
        }

        private Map<String, String> getAllDataSourceStatus() {
            Map<String, String> map = Maps.newLinkedHashMap();
            map.put("ds1", "active");
            map.put("ds2", "inactive");
            return map;
        }
    }

}

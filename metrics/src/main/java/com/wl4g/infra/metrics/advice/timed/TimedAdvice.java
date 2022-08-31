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
package com.wl4g.infra.metrics.advice.timed;

import static java.util.Objects.nonNull;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.aopalliance.intercept.MethodInvocation;

import com.wl4g.infra.common.lang.FastTimeClock;
import com.wl4g.infra.metrics.advice.MetricsAdviceBase;
import com.wl4g.infra.metrics.exception.MetricsException;
import com.wl4g.infra.metrics.health.timed.TimedHealthIndicator;

import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;

/**
 * A simple statistical method to perform time-consuming aspects. If you want a
 * more comprehensive APM analysis, please use frameworks such as
 * skywalking/elasticAPM/zipkin</br>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-11-19 v1.0.0
 * @since v1.0
 * @see https://github.com/apache/skywalking/pull/1118
 * @see {@link io.micrometer.core.aop.TimedAspect}
 */
@SuppressWarnings("deprecation")
@AllArgsConstructor
public class TimedAdvice extends MetricsAdviceBase {

    @Nullable
    private final TimedHealthIndicator timedIndicator; // Optional

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            // Gets metric name by method.
            final String metricName = getMetricName(invocation);

            final long start = FastTimeClock.currentTimeMillis();
            final Object res = invocation.proceed();
            final long deltaMs = FastTimeClock.currentTimeMillis() - start;

            Timer timer = registry.timer(transformTimerName(metricName));
            timer.record(deltaMs, TimeUnit.MILLISECONDS);

            postProperties(metricName, deltaMs);
            return res;
        } catch (Throwable e) {
            throw new MetricsException(e);
        }
    }

    /**
     * Post properties.
     * 
     * @param metricName
     * @param deltaMs
     */
    protected void postProperties(String metricName, long deltaMs) {
        if (nonNull(timedIndicator)) {
            timedIndicator.record(metricName, deltaMs);
        }
    }

    /**
     * Note that gaugeService includes not only timer metrics, but also other
     * metrics such as counter and histogram etc, the same method of AOP
     * interception should also use different metricNames for recording each
     * metric information using gaugeService, <font color=red>otherwise, it will
     * throw "IllegalArgumentException: A metric named xxx already exists"
     * exception.</font> </br>
     * See: org.springframework.boot.actuate.metrics.dropwizard.
     * DropwizardMetricServices.submit
     * 
     * @param name
     * @return
     */
    protected String transformTimerName(String name) {
        // return "lossTime." + name; // Common type of meter.
        // It corresponds to a special timer type meter(Automatically calculate
        // the maximum and minimum mean value.).
        return "timer." + name;
    }

}
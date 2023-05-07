/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * James Wong <jameswong1376@gmail.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: https://wl4g.github.io
 */
package com.wl4g.infra.integration.feign.core.context.internal;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static java.lang.System.nanoTime;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.Map;

import com.wl4g.infra.integration.feign.core.context.internal.FeignContextCoprocessor.Invokers;
import com.wl4g.infra.integration.feign.core.metrics.FeignMetricsUtil;
import com.wl4g.infra.integration.feign.core.metrics.FeignMetricsUtil.MetricsName;
import com.wl4g.infra.metrics.MetricsFacade;

import feign.Feign;
import feign.InvocationHandlerFactory;
import feign.InvocationHandlerFactory.MethodHandler;
import feign.Target;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;

/**
 * {@link FeignContextBuilder}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2021-05-20
 * @since v2.0
 * @see
 */
@AllArgsConstructor
public class FeignContextBuilder extends Feign.Builder {

    private final MetricsFacade metricsFacade;

    /**
     * refer to:{@link feign.hystrix.HystrixFeign.Builder#build} and
     * {@link com.alibaba.cloud.sentinel.feign.SentinelFeign.Builder#build}
     */
    @Override
    public Feign build() {
        // Gets origin InvocationHandlerFactory.
        final InvocationHandlerFactory originalFactory = getField(invocationHandlerFactoryField, this, true);

        // Override sets to InvocationHandlerFactory.
        super.invocationHandlerFactory(new InvocationHandlerFactory() {
            @SuppressWarnings("rawtypes")
            @Override
            public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
                return new FeignContextInvocationHandler(metricsFacade, originalFactory, target, dispatch);
            }
        });

        return super.build();
    }

    /**
     * {@link FeignContextInvocationHandler}
     * 
     * @author James Wong &lt;jameswong1376@gmail.com&gt;
     * @version v1.0 2021-05-20
     * @since v2.0
     * @see {@link com.alibaba.cloud.sentinel.feign.SentinelInvocationHandler}
     */
    static class FeignContextInvocationHandler implements InvocationHandler {
        private final MetricsFacade metricsFacade;
        private final InvocationHandlerFactory originalFactory;
        private final Target<?> target;
        private final Map<Method, MethodHandler> dispatch;

        public FeignContextInvocationHandler(MetricsFacade metricsFacade, InvocationHandlerFactory originalFactory,
                Target<?> target, Map<Method, MethodHandler> dispatch) {
            this.metricsFacade = notNullOf(metricsFacade, "metricsFacade");
            this.originalFactory = notNullOf(originalFactory, "originalFactory");
            this.target = notNullOf(target, "target");
            this.dispatch = notNullOf(dispatch, "dispatch");
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //
            // Notice: early exit if the invoked method is from java.lang.Object
            // code is the same as ReflectiveFeign.FeignInvocationHandler
            //
            if ("equals".equals(method.getName())) {
                try {
                    Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                    return equals(otherHandler);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            } else if ("hashCode".equals(method.getName())) {
                return hashCode();
            } else if ("toString".equals(method.getName())) {
                return toString();
            }

            // Call the coprocessor first.
            Invokers.beforeConsumerExecution(proxy, method, args);

            // Gets metrics tags.
            String[] tags = getMetricsTags(proxy, method, args);

            // Add total metrics.
            Counter success = metricsFacade.counter(MetricsName.consumer_total.getName(), MetricsName.consumer_total.getHelp(),
                    tags);
            Counter failure = metricsFacade.counter(MetricsName.consumer_failure.getName(),
                    MetricsName.consumer_failure.getHelp(), tags);
            Timer cost = metricsFacade.timer(MetricsName.consumer_cost.getName(), MetricsName.consumer_cost.getHelp(),
                    // TODO use configuration
                    new double[] { 0.3, 0.5, 0.9, 0.95, 0.99 }, tags);

            try {
                final long beginTime = nanoTime();

                // @see:feign.ReflectiveFeign.FeignInvocationHandler#invoke
                Object result = originalFactory.create(target, dispatch).invoke(proxy, method, args);

                // Add cost metrics.
                cost.record(Duration.ofNanos(nanoTime() - beginTime));

                // Add success metrics.
                success.increment();

                return result;
            } catch (Exception e) {
                // Add failure metrics.
                failure.increment();
                throw e;
            }
        }

        private String[] getMetricsTags(Object target, Method method, Object[] args) {
            return FeignMetricsUtil.getDefaultMetricsTags(target, method, args);
        }

    }

    private static final Field invocationHandlerFactoryField = findField(feign.Feign.Builder.class, "invocationHandlerFactory",
            InvocationHandlerFactory.class);

}

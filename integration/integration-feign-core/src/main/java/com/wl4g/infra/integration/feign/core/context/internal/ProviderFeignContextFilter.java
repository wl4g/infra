/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <James Wong@gmail.com, 983708408@qq.com> Technology CO.LTD.
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
 * Reference to website: http://wl4g.com
 */
package com.wl4g.infra.integration.feign.core.context.internal;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.core.utils.web.WebUtils3.currentServletRequest;
import static com.wl4g.infra.core.utils.web.WebUtils3.currentServletResponse;
import static java.lang.System.nanoTime;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.core.framework.proxy.InvocationChain;
import com.wl4g.infra.core.framework.proxy.SmartProxyFilter;
import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.infra.integration.feign.core.context.RpcContextHolder;
import com.wl4g.infra.integration.feign.core.metrics.FeignMetricsUtil;
import com.wl4g.infra.integration.feign.core.metrics.FeignMetricsUtil.MetricsName;
import com.wl4g.infra.metrics.MetricsFacade;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.AllArgsConstructor;

/**
 * Notes: {@link RequestBodyAdvice} should not be used here because not all
 * request parameters are in the request.body , which may not perform binding to
 * {@link RpcContextHolder}.
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see
 */
@AllArgsConstructor
public class ProviderFeignContextFilter implements SmartProxyFilter {
    protected final SmartLogger log = getLogger(getClass());

    private final MetricsFacade metricsFacade;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public boolean supportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
        return checkSupportTypeProxy(target, actualOriginalTargetClass);
    }

    @Override
    public boolean supportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass, Object... args) {
        return checkSupportMethodProxy(target, method, actualOriginalTargetClass, args);
    }

    @Override
    public Object doInvoke(@NotNull InvocationChain chain, @NotNull Object target, @NotNull Method method, Object[] args)
            throws Exception {

        // Gets metrics tags.
        String[] tags = getMetricsTags(target, method, args);

        // Add total metrics.
        Counter success = metricsFacade.counter(MetricsName.provider_success.getName(), MetricsName.provider_total.getHelp(),
                tags);
        Counter failure = metricsFacade.counter(MetricsName.provider_failure.getName(), MetricsName.provider_failure.getHelp(),
                tags);
        Timer cost = metricsFacade.timer(MetricsName.provider_failure.getName(), MetricsName.provider_failure.getHelp(),
                // TODO use configuration
                new double[] { 0.3, 0.5, 0.9, 0.95, 0.99 }, tags);

        try {
            final long beginTime = nanoTime();

            preHandle(target, method, args);
            Object result = chain.doInvoke(target, method, args);

            // Add cost metrics.
            cost.record(Duration.ofNanos(nanoTime() - beginTime));

            // Add success metrics.
            success.increment();

            return result;
        } catch (Exception e) {
            // Add failure metrics.
            failure.increment();
            throw e;
        } finally {
            postHandle(target, method, args);
        }
    }

    private void preHandle(@NotNull Object target, @NotNull Method method, Object[] args) {
        if (!isConsumerSide(target)) {
            // FIXED: Only the feign remote instance is processed, ignore local
            // instance. For example, in the provider layer, there is no request
            // object when the ApplicationRunner#run() executes the task.
            HttpServletRequest request = currentServletRequest();
            if (nonNull(request)) {
                // When receiving RPC requests, the attachment info should be
                // extracted and bound to the local context.
                FeignRpcContextBinders.bindAttachmentsFromRequest(request);
            }
            // Call coprocessor.
            FeignContextCoprocessor.Invokers.beforeProviderExecution(request, target, method, args);
        }
    }

    private void postHandle(@NotNull Object target, @NotNull Method method, Object[] args) {
        if (!isConsumerSide(target)) {
            try {
                // FIXED: Only the feign remote instance is processed, ignore
                // local instance. For example, in the provider layer, there is
                // no request object when the ApplicationRunner#run() executes
                // the task.
                HttpServletResponse response = currentServletResponse();
                if (nonNull(response)) {
                    // When responding to RPC, the attachment information
                    // returned should be added.
                    FeignRpcContextBinders.writeAttachemntsToResponse(response);
                }
                // Call coprocessor.
                FeignContextCoprocessor.Invokers.afterProviderExecution(target, method, args);
            } finally {
                // Refer to apache-dubbo(2.0.10 ~ 2.7.9):ContextFilter.java,
                // after responding to RPC, should cleanup the context and
                // server context.
                RpcContextHolder.removeContext();
                RpcContextHolder.removeServerContext();
            }
        }
    }

    private String[] getMetricsTags(Object target, Method method, Object[] args) {
        return FeignMetricsUtil.getDefaultMetricsTags(target, method, args);
    }

    /**
     * Check whether the service role executing the current request to the
     * consumer side.
     * 
     * @param target
     * @return
     */
    protected boolean isConsumerSide(@NotNull Object target) {
        notNullOf(target, "target");
        return (target instanceof feign.Target);
    }

    public static boolean checkSupportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
        List<Class<?>> interfaceClasses = safeArrayToList(actualOriginalTargetClass.getInterfaces());
        interfaceClasses.add(actualOriginalTargetClass);
        for (Class<?> interfaceClass : interfaceClasses) {
            if (hasAnnotation(interfaceClass, FeignConsumer.class)
                    || (nonNull(FEIGN_CLIENT_CLASS) && hasAnnotation(interfaceClass, FEIGN_CLIENT_CLASS))) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkSupportMethodProxy(
            Object target,
            Method method,
            Class<?> actualOriginalTargetClass,
            Object... args) {
        return hasAnnotation(method.getDeclaringClass(), ResponseBody.class) || hasAnnotation(method, ResponseBody.class);
    }

    public static final Class<? extends Annotation> FEIGN_CLIENT_CLASS = resolveClassNameNullable(
            "org.springframework.cloud.openfeign.FeignClient");
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;
}
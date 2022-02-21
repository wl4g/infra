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
package com.wl4g.component.core.framework.proxy;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.reflect.ReflectionUtils2.makeAccessible;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * {@link DispatcherSmartProxyInvocation}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author liqiu
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see
 */
public class DispatcherSmartProxyInvocation extends AbstractDispatcherProxyInvocation {

    protected final Supplier<Object> targetSuplier;
    protected Object target; // typeof object(not FactoryBean)

    public DispatcherSmartProxyInvocation(DefaultListableBeanFactory beanFactory, SmartProxyAutoConfiguration configurer,
            String targetBeanName, Class<?> targetClass, Supplier<Object> targetSuplier) {
        super(beanFactory, configurer, targetBeanName, targetClass);
        this.targetSuplier = notNullOf(targetSuplier, "targetSuplier");
    }

    @Override
    public Object getTarget() {
        // TODO use cache?
        // e.g: but org.mybatis.spring.MapperFactoryBean#getObject()
        // Returns a new object each time.
        /**
         * Notes: do not use {@link BeanFactory#getBean(targetBeanName)} to get
         * the actual original object here, because it may have been overridden
         * and registered as a proxy object, and there will be a dead loop when
         * calling.
         */
        return (target = targetSuplier.get());
    }

    @Override
    public Object doInvoke(final Object target, final Method method, final Object[] args) throws Throwable {
        if (!method.isAccessible()) {
            makeAccessible(method);
        }

        // Match all filters that support intercepting target methods.
        List<SmartProxyFilter> matchedFilters = safeList(configurer.getProcessors(targetClass)).stream()
                .filter(p -> p.supportMethodProxy(target, method, targetClass, args)).collect(toList());

        // Invoke target(actual) method.
        Object result = null;
        try {
            result = new InvocationChain(matchedFilters).doInvoke(target, method, args);
        } catch (Throwable ex) {
            if (isNull(ex) || isNull(ex.getCause())) {
                throw new InvocationTargetException(null,
                        format("Failed to invocation target: %s, method: %s, args: %s", target, method, args));
            } else {
                // Solved: java.lang.reflect.InvocationTargetException-->null
                throw ex.getCause();
            }
        }

        log.debug("Invoked proxied origin method: {}, args:{}, return: {}", method, args, result);
        return result;
    }

}
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
package com.wl4g.infra.core.framework.proxy;

import java.lang.reflect.Method;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Cglib-aop-based intelligent agent custom interception handler, such as: it
 * can proxy controller instance, {@link org.mybatis.spring.mapper.MapperProxy}
 * instance of mybatis and any {@link FactoryBean}, and it can configure
 * multiple (executed in orders), but it should be noted: because spring cglib
 * does not support return values of incompatible types, if you need to return
 * values of incompatible types, Please use {@link ResponseBodyAdvice}.
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version v1.0 2020-12-29
 * @sine v1.0
 * @see
 */
public interface SmartProxyFilter extends Ordered {

    /**
     * Check whether the current class proxy is supported.
     * 
     * @param target
     * @param actualOriginalTargetClass
     * @return
     */
    boolean supportTypeProxy(Object target, Class<?> actualOriginalTargetClass);

    /**
     * Check whether the current method proxy is supported.
     * 
     * @param method
     * @param actualOriginalTargetClass
     * @param args
     * @return
     */
    default boolean supportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass, Object... args) {
        return true;
    }

    default Object doInvoke(@NotNull InvocationChain chain, @NotNull Object target, @NotNull Method method,
            @Nullable Object[] args) throws Exception {
        return chain.doInvoke(target, method, args);
    }

}

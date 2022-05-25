/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
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

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.wl4g.infra.common.log.SmartLogger;

/**
 * {@link InvocationChain}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-05-02
 * @sine v1.0
 * @see {@link org.apache.catalina.core.ApplicationFilterChain#internalDoFilter()}
 */
public class InvocationChain {
    protected final SmartLogger log = getLogger(getClass());

    private final List<SmartProxyFilter> filters;
    private int index;

    public InvocationChain(List<SmartProxyFilter> filters) {
        notNullOf(filters, "filters");
        AnnotationAwareOrderComparator.sort(filters);
        this.filters = filters;
        this.index = 0;
    }

    public Object doInvoke(@NotNull Object target, @NotNull Method method, @Nullable Object[] args) throws Exception {
        log.trace("Invoking smart proxied filter at index: {}", index);
        if (index >= filters.size()) {
            // When none of the filters execute the actual target method, it
            // must be called at the end.
            return method.invoke(target, args);
        }
        return filters.get(index++).doInvoke(this, target, method, args);
    }

}

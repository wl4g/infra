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
package com.wl4g.infra.core.web.error.handler;

import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.state;
import static com.wl4g.infra.common.web.rest.RespBase.RetCode.SYS_ERR;
import static java.lang.String.format;
import static java.util.Collections.sort;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.Order;

import com.wl4g.infra.common.collection.OnceUnmodifiableList;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration.ErrorHandlerProperties;

/**
 * Composite smart error handler adapter.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019-11-01
 * @since
 */
public class CompositeSmartErrorHandler extends AbstractSmartErrorHandler {

    protected final List<AbstractSmartErrorHandler> errorHandlers = new OnceUnmodifiableList<>(new ArrayList<>());

    public CompositeSmartErrorHandler(ErrorHandlerProperties config, List<AbstractSmartErrorHandler> handlers) {
        super(config);
        state(!isEmpty(handlers), "Error handlers has at least one.");
        // Sort by order.
        sort(handlers, (o1, o2) -> {
            Order order1 = findAnnotation(o1.getClass(), Order.class);
            Order order2 = findAnnotation(o2.getClass(), Order.class);
            notNull(order1, "ErrorConfigure implements must Order must be annotated.");
            notNull(order2, "ErrorConfigure implements must Order must be annotated.");
            int compare = order1.value() - order2.value();
            state(compare != 0,
                    format("%s implements %s:%s and %s:%s order conflict!", AbstractSmartErrorHandler.class.getSimpleName(),
                            o1.getClass(), order1.value(), o2.getClass(), order2.value()));
            return compare;
        });
        this.errorHandlers.addAll(handlers);
    }

    @Override
    public Integer getStatus(Map<String, Object> model, Throwable th) {
        for (AbstractSmartErrorHandler c : errorHandlers) {
            Integer status = c.getStatus(model, th);
            if (nonNull(status)) {
                return status;
            }
        }
        return SYS_ERR.getErrcode(); // fall-back
    }

    @Override
    public String getRootCause(Map<String, Object> model, Throwable th) {
        for (AbstractSmartErrorHandler c : errorHandlers) {
            String errmsg = c.getRootCause(model, th);
            if (!isBlank(errmsg)) {
                return errmsg;
            }
        }
        return "Unknown or Servers internal error"; // fall-back
    }

}
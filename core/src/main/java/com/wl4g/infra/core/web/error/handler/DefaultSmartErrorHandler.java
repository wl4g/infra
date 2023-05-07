/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
import static com.wl4g.infra.common.lang.Exceptions.getRootCausesString;
import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static com.wl4g.infra.common.web.rest.RespBase.getRestfulCode;
import static com.wl4g.infra.common.web.rest.RespBase.RetCode.BAD_PARAMS;
import static com.wl4g.infra.common.web.rest.RespBase.RetCode.UNSUPPORTED;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.annotation.Order;
import org.springframework.validation.FieldError;

import com.wl4g.infra.common.web.rest.RespBase.RetCode;
import com.wl4g.infra.common.web.rest.RespBase.RetCodeSpec;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration.ErrorHandlerProperties;

/**
 * Default smart error handler.
 * 
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2019-11-01
 * @since
 */
@Order(DefaultSmartErrorHandler.ORDER_DEFAULT_SMART_ERROR_HANDLER)
public class DefaultSmartErrorHandler extends AbstractSmartErrorHandler {

    public DefaultSmartErrorHandler(ErrorHandlerProperties config) {
        super(config);
    }

    @Override
    public Integer getStatus(Map<String, Object> model, Throwable th) {
        Integer statusCode = (Integer) model.get("status");
        // Eliminate meaningless status code: 999
        // see:org.springframework.boot.autoconfigure.web.DefaultErrorAttributes#addStatus()
        if (isNull(statusCode) || statusCode == 999) {
            RetCodeSpec retCode = getRestfulCode(th);
            if (!isNull(retCode)) {
                statusCode = retCode.getErrcode();
            } else if (th instanceof IllegalArgumentException) {
                statusCode = BAD_PARAMS.getErrcode();
            } else if (th instanceof UnsupportedOperationException) {
                statusCode = UNSUPPORTED.getErrcode();
            } else { // status=999?
                // statusCode = (Integer)
                // equest.getAttribute("javax.servlet.error.status_code");
            }
        }
        return isNull(statusCode) ? RetCode.SYS_ERR.getErrcode() : statusCode;
    }

    /**
     * Extract meaningful valid errors messages.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public String getRootCause(Map<String, Object> model, Throwable th) {
        notNull(model, "Shouldn't be here");

        StringBuffer errmsg = new StringBuffer(16);
        // message
        // Object message = model.get("message");
        // if (message != null) {
        // errmsg.append(message);
        // }
        // exception
        // Object exception = model.get("exception");
        // if (exception != null && exception instanceof Throwable) {
        // if (exception instanceof String) {
        // errmsg.append(exception);
        // } else if (exception instanceof Throwable) {
        // errmsg.append(((Throwable) exception).getMessage());
        // }
        // }
        // trace
        // Object trace = model.get("trace");
        // if (trace != null) {
        // if (trace instanceof String) {
        // errmsg.append(trace);
        // } else if (trace instanceof Throwable) {
        // errmsg.append(((Throwable) trace).getMessage());
        // }
        // }
        // error
        final Object error = model.get("error");
        if (nonNull(error) && !eqIgnCase(error, "None")) {
            if (error instanceof String) {
                errmsg.append(error);
            } else if (error instanceof Throwable) {
                errmsg.append(((Throwable) error).getMessage());
            }
        }
        final Object errors = model.get("errors"); // @NotNull?
        if (nonNull(errors)) {
            errmsg.setLength(0); // Print only errors information
            if (errors instanceof Collection) {
                // Used to remove duplication
                List<String> fieldErrs = new ArrayList<>(8);

                Collection<Object> _errors = (Collection) errors;
                Iterator<Object> it = _errors.iterator();
                while (it.hasNext()) {
                    Object err = it.next();
                    if (err instanceof FieldError) {
                        FieldError ferr = (FieldError) err;
                        /*
                         * Remove duplicate field validation errors,
                         * e.g. @NotNull and @NotEmpty
                         */
                        String fieldErr = ferr.getField();
                        if (!fieldErrs.contains(fieldErr)) {
                            errmsg.append("'");
                            errmsg.append(fieldErr);
                            errmsg.append("' ");
                            errmsg.append(ferr.getDefaultMessage());
                            errmsg.append(", ");
                        }
                        fieldErrs.add(fieldErr);
                    } else {
                        errmsg.append(err.toString());
                        errmsg.append(", ");
                    }
                }
            } else {
                errmsg.append(errors.toString());
            }
        }
        final Object exception = model.get("exception");
        if (nonNull(exception) && exception instanceof Throwable) {
            errmsg.append(getRootCausesString((Throwable) exception, true));
        }
        final Object stacktrace = model.get("trace");
        if (nonNull(stacktrace) && !stacktrace.toString().isEmpty()) {
            errmsg.append(extractRootCauseMessage((String) stacktrace));
        }

        return errmsg.toString();
    }

    public static String extractRootCauseMessage(String stacktrace) {
        if (nonNull(stacktrace) && stacktrace instanceof String) {
            final List<String> errStacks = new ArrayList<>(4);
            final Matcher matcher = Pattern.compile(DEFAULT_ERRMSG_REGEX).matcher(stacktrace);
            while (matcher.find()) {
                errStacks.add(matcher.group());
            }
            return trimToEmpty(join(errStacks.toArray(), LINE_SEPARATOR));
        }
        return stacktrace;
    }

    public static final int ORDER_DEFAULT_SMART_ERROR_HANDLER = 1000;
    public static final String DEFAULT_ERRMSG_REGEX = "Caused by:(.+)|at <js>(\\s|\\.)([a-zA-Z0-9]+)\\((.+)\\)";

}
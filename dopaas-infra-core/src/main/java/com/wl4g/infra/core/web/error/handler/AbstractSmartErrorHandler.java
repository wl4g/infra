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

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.Exceptions.getStackTraceAsString;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.web.WebUtils2.ResponseType.isRespJSON;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.springframework.boot.web.error.ErrorAttributeOptions.of;
import static org.springframework.boot.web.error.ErrorAttributeOptions.Include.BINDING_ERRORS;
import static org.springframework.boot.web.error.ErrorAttributeOptions.Include.EXCEPTION;
import static org.springframework.boot.web.error.ErrorAttributeOptions.Include.MESSAGE;
import static org.springframework.boot.web.error.ErrorAttributeOptions.Include.STACK_TRACE;
import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.view.Freemarkers;
import com.wl4g.infra.common.web.WebUtils.RequestExtractor;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration.ErrorHandlerProperties;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import reactor.core.publisher.Mono;

/**
 * Abstract smart error handler.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019-11-01
 * @since
 * @see https://http.cat
 */
public abstract class AbstractSmartErrorHandler implements InitializingBean {

    protected final SmartLogger log = getLogger(getClass());

    /** Errors configuration properties. */
    protected final ErrorHandlerProperties config;

    /** Errors {@link Template} cache. */
    protected final Map<Integer, Template> errorTplMappingCache;

    public AbstractSmartErrorHandler(ErrorHandlerProperties config) {
        this.config = notNullOf(config, "config");
        this.errorTplMappingCache = new ConcurrentHashMap<>(4);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing global smart error configurer ...");

        Configuration fmc = Freemarkers.create(config.getBasePath()).build();
        safeMap(config.getRenderingMapping()).entrySet().stream().forEach(p -> {
            try {
                if (!isErrorRedirectURI(p.getValue())) { // e.g 404.tpl.html
                    Template tpl = fmc.getTemplate(p.getValue(), UTF_8.name());
                    notNull(tpl, "Default (%s) error template must not be null", p.getKey());
                    errorTplMappingCache.put(p.getKey(), tpl);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    /**
     * Obtain exception http status. {@link HttpStatus}
     * 
     * @param th
     * @return
     */
    public Integer getStatus(Throwable th) {
        return getStatus(emptyMap(), th);
    }

    /**
     * Obtain exception http status. {@link HttpStatus}
     * 
     * @param model
     * @param th
     * @return
     */
    public abstract Integer getStatus(Map<String, Object> model, Throwable th);

    /**
     * Obtain exception as string.
     * 
     * @param model
     * @param th
     * @return
     */
    public abstract String getRootCause(Map<String, Object> model, Throwable th);

    /**
     * Handle automatic errors & rendering.
     * 
     * @param extractor
     * @param model
     * @param th
     * @param errorRender
     * @return handle errors result(if necessary). for example: {@link Mono}
     */
    public Object rendering(
            @NotNull RequestExtractor extractor,
            @NotNull Map<String, Object> model,
            @NotNull Throwable th,
            @NotNull ErrorRender errorRender) {
        try {
            // Obtain custom extension response status.
            int status = getStatus(model, th);
            String errmsg = getRootCause(model, th);
            Object uriOrTpl = loadRedirectUriOrRenderView(status);

            // When the client is not a browser or the exception rendering
            // configuration is empty, the JSON message is returned by default.
            if (isNull(uriOrTpl) || isRespJSON(extractor, null)) {
                RespBase<Object> resp = new RespBase<>(RespBase.RetCode.newCode(status, errmsg));
                if (!(uriOrTpl instanceof Template)) {
                    resp.forMap().put(DEFAULT_REDIRECT_KEY, uriOrTpl);
                }
                log.error("resp:error - {}", resp.asJson());
                errorRender.renderingJson(model, resp);
            }
            // Rendering error to HTML/Location
            else {
                if (uriOrTpl instanceof Template) {
                    log.error("redirect:error - {}", status);
                    // Merge configuration.
                    model.putAll(config.asMap());
                    String renderString = processTemplateIntoString((Template) uriOrTpl, model);
                    errorRender.renderingTemplate(model, status, renderString);
                } else {
                    log.error("redirect:error - {}", uriOrTpl);
                    errorRender.redirectLocation(model, (String) uriOrTpl);
                }
            }
        } catch (Throwable th0) {
            log.error("Failed to handle global errors, at cause: \n{} and root causes:\n{}", getStackTraceAsString(th0),
                    getStackTraceAsString(th));
        }
        return null;
    }

    /**
     * Load redirect URI rendering errors page view.
     * 
     * @param status
     * @return
     * @throws TemplateException
     * @throws IOException
     */
    private Object loadRedirectUriOrRenderView(int status) throws IOException, TemplateException {
        Template tpl = errorTplMappingCache.get(status);
        if (nonNull(tpl)) { // error template?
            return tpl;
        }

        // error redirect URI
        String errorRedirectUri = config.getRenderingMapping().get(status);
        if (isBlank(errorRedirectUri)) {
            log.debug("No found render template or redirection uri for error status: %s", status);
            return null;
        }
        return errorRedirectUri.substring(DEFAULT_REDIRECT_PREFIX.length());
    }

    /**
     * Check redirection error URI.
     * 
     * @param uriOrTpl
     * @return
     */
    private boolean isErrorRedirectURI(String uriOrTpl) {
        return startsWithIgnoreCase(uriOrTpl, DEFAULT_REDIRECT_PREFIX);
    }

    /**
     * Extract meaningful valid errors messages.
     * 
     * @param model
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String extractValidErrorsMessage(@NotNull Map<String, Object> model) {
        notNull(model, "Shouldn't be here");

        StringBuffer errmsg = new StringBuffer();
        Object message = model.get("message");
        if (message != null) {
            errmsg.append(message);
        }

        Object errors = model.get("errors"); // @NotNull?
        if (errors != null) {
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

        return errmsg.toString();
    }

    /**
     * Obtain error attribute options.
     * 
     * @param isStacktrace
     * @return
     */
    public static ErrorAttributeOptions obtainErrorAttributeOptions(boolean isStacktrace) {
        return isStacktrace ? of(STACK_TRACE, MESSAGE, BINDING_ERRORS, EXCEPTION) : of(MESSAGE, BINDING_ERRORS, EXCEPTION);
    }

    public static interface ErrorRender {
        default void renderingJson(Map<String, Object> model, RespBase<Object> resp) throws Exception {
            throw new UnsupportedOperationException();
        }

        default void renderingTemplate(Map<String, Object> model, int status, String templateString) throws Exception {
            throw new UnsupportedOperationException();
        }

        default void redirectLocation(Map<String, Object> model, String errorRedirectUri) throws Exception {
            throw new UnsupportedOperationException();
        }

        default Object getHttpResponse() {
            throw new UnsupportedOperationException();
        }
    }

    private static final String DEFAULT_REDIRECT_PREFIX = "redirect:";
    private static final String DEFAULT_REDIRECT_KEY = "redirectUrl";

}
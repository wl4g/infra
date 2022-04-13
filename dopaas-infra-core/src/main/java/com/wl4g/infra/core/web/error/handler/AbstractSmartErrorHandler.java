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
import static com.wl4g.infra.common.lang.StringUtils2.startsWithIgnoreCase;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.http.HttpStatus;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.view.Freemarkers;
import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;
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
    protected final Map<Integer, Template> errorTemplateCache;

    public AbstractSmartErrorHandler(ErrorHandlerProperties config) {
        this.config = notNullOf(config, "config");
        this.errorTemplateCache = new ConcurrentHashMap<>(4);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing global smart error configurer ...");

        Configuration fmc = Freemarkers.create(config.getBasePath()).build();
        safeMap(config.getRenderingMapping()).entrySet().stream().forEach(p -> {
            try {
                if (!isRedirectUri(p.getValue())) { // E.g: 404.tpl.html
                    Template tpl = fmc.getTemplate(p.getValue(), UTF_8.name());
                    notNull(tpl, "Default (%s) error template must not be null", p.getKey());
                    errorTemplateCache.put(p.getKey(), tpl);
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
            @NotNull WebRequestExtractor extractor,
            @NotNull Map<String, Object> model,
            @NotNull Throwable th,
            @NotNull ErrorRender errorRender) {
        try {
            // Obtain custom extension response status.
            int status = getStatus(model, th);
            String errmsg = getRootCause(model, th);

            // When the client is not a browser or the exception rendering
            // configuration is empty, the JSON message is returned by default.
            if (isRespJSON(extractor, null)) {
                RespBase<Object> resp = new RespBase<>(RespBase.RetCode.newCode(status, errmsg));
                Object redirectUri = loadRedirectUri(status);
                if (nonNull(redirectUri)) {
                    resp.forMap().put(DEFAULT_REDIRECT_KEY, redirectUri);
                }
                log.error("resp:error - {}", resp.asJson());
                return errorRender.renderingJson(model, resp);
            }
            // Rendering to error HTML
            else {
                Object tpl = loadRenderTemplate(status);
                if (nonNull(tpl)) {
                    log.error("rendering:error - {}", status);
                    // Merge configuration.
                    model.putAll(config.asMap());
                    String renderString = processTemplateIntoString((Template) tpl, model);
                    return errorRender.renderingTemplate(model, status, renderString);
                }
                // Rendering to error Location
                else {
                    Object redirectUri = loadRedirectUri(status);
                    if (nonNull(redirectUri)) {
                        log.error("redirect:error - {}", redirectUri);
                        return errorRender.redirectLocation(model, (String) redirectUri);
                    }
                }
            }
        } catch (Throwable th0) {
            log.error("Failed to handle global errors, at cause: \n{} and root causes:\n{}", getStackTraceAsString(th0),
                    getStackTraceAsString(th));
        }
        return null;
    }

    /**
     * Load rendering errors page template.
     * 
     * @param status
     * @return
     * @throws TemplateException
     * @throws IOException
     */
    private Object loadRenderTemplate(int status) throws IOException, TemplateException {
        Template tpl = errorTemplateCache.get(status);
        if (isNull(tpl)) { // error template?
            log.warn("No found render template for error status: {}", status);
            return null;
        }
        return tpl;
    }

    /**
     * Load error redirect URI.
     * 
     * @param status
     * @return
     * @throws TemplateException
     * @throws IOException
     */
    private Object loadRedirectUri(int status) throws IOException, TemplateException {
        String errorRedirectUri = config.getRenderingMapping().get(status);
        if (isBlank(errorRedirectUri)) {
            log.warn("No found render redirect uri for error status: {}", status);
            return null;
        }
        // Only the 'redirect:' prefix indicates the redirect URI, otherwise it
        // is the HTML rendering template.
        if (startsWithIgnoreCase(errorRedirectUri, DEFAULT_REDIRECT_PREFIX)) {
            return errorRedirectUri.substring(DEFAULT_REDIRECT_PREFIX.length());
        }
        // If it is a rendering template, no need to use.
        return null;
    }

    /**
     * Check redirection error URI.
     * 
     * @param uriOrTpl
     * @return
     */
    private boolean isRedirectUri(String uriOrTpl) {
        return startsWithIgnoreCase(uriOrTpl, DEFAULT_REDIRECT_PREFIX);
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
        default Object renderingJson(Map<String, Object> model, RespBase<Object> resp) throws Exception {
            throw new UnsupportedOperationException();
        }

        default Object renderingTemplate(Map<String, Object> model, int status, String templateString) throws Exception {
            throw new UnsupportedOperationException();
        }

        default Object redirectLocation(Map<String, Object> model, String errorRedirectUri) throws Exception {
            throw new UnsupportedOperationException();
        }

        default Object getHttpResponse() {
            throw new UnsupportedOperationException();
        }
    }

    private static final String DEFAULT_REDIRECT_PREFIX = "redirect:";
    private static final String DEFAULT_REDIRECT_KEY = "redirectUrl";

}
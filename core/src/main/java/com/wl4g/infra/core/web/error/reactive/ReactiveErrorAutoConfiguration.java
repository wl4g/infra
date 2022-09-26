/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.core.web.error.reactive;

import static com.wl4g.infra.core.constant.CoreInfraConstants.CONF_PREFIX_INFRA_CORE_WEB_GLOBAL_ERROR;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.TEMPORARY_REDIRECT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.web.error.AbstractErrorAutoConfiguration;
import com.wl4g.infra.core.web.error.handler.AbstractSmartErrorHandler;
import com.wl4g.infra.core.web.error.handler.CompositeSmartErrorHandler;

/**
 * Global error controller handler auto configuration.
 * 
 * @author wangl.sir
 * @version v1.0 2019年1月10日
 * @since
 */
@ConditionalOnProperty(value = CONF_PREFIX_INFRA_CORE_WEB_GLOBAL_ERROR + ".enabled", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(ViewResolver.class)
public class ReactiveErrorAutoConfiguration extends AbstractErrorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AbstractSmartErrorHandler.ErrorRender defaultReactiveSmartErrorRender(ErrorHandlerProperties config) {
        return new AbstractSmartErrorHandler.ErrorRender() {
            @Override
            public Object renderingJson(Map<String, Object> model, RespBase<Object> resp) throws Exception {
                return ServerResponse.ok().contentType(APPLICATION_JSON).body(fromValue(resp));
            }

            @Override
            public Object renderingTemplate(Map<String, Object> model, int status, String templateString) throws Exception {
                return ServerResponse.status(status).contentType(TEXT_HTML).body(fromValue(templateString));
            }

            @Override
            public Object redirectLocation(Map<String, Object> model, String errorRedirectUri) throws Exception {
                return ServerResponse.status(TEMPORARY_REDIRECT.value())
                        .contentType(TEXT_HTML)
                        .location(URI.create(errorRedirectUri))
                        .build();
            }
        };
    }

    /**
     * @see {@link org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration#errorWebExceptionHandler}
     */
    @Bean
    @Order(-2) // Takes precedence over the default handler
    public ReactiveSmartErrorController reactiveSmartErrorController(
            org.springframework.boot.web.reactive.error.ErrorAttributes errorAttributes,
            WebProperties webProperties,
            ObjectProvider<ViewResolver> viewResolvers,
            ServerCodecConfigurer codecConfigurer,
            ApplicationContext actx,
            ErrorHandlerProperties config,
            CompositeSmartErrorHandler errorHandler,
            AbstractSmartErrorHandler.ErrorRender errorRender) {
        ReactiveSmartErrorController errorController = new ReactiveSmartErrorController(errorAttributes,
                webProperties.getResources(), actx, config, errorHandler, errorRender);
        errorController.setViewResolvers(viewResolvers.orderedStream().collect(toList()));
        errorController.setMessageWriters(codecConfigurer.getWriters());
        errorController.setMessageReaders(codecConfigurer.getReaders());
        return errorController;
    }

}
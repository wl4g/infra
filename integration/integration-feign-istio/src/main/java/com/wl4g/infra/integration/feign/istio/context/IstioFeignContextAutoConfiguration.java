/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <jameswong1376@gmail.com> Technology CO.LTD.
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
package com.wl4g.infra.integration.feign.istio.context;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.wl4g.infra.integration.feign.core.context.DefaultFeignContextAutoConfiguration.DefaultFeignRpcContextHolder;
import com.wl4g.infra.integration.feign.core.context.RpcContextHolder;
import com.wl4g.infra.integration.feign.istio.config.FeignSpringBootIstioProperties;

/**
 * Istio SpringBoot feign {@link RpcContextHolder} auto configuration. (Both the
 * consumer side and the production side should be configured)
 *
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2021-01-01
 * @since v2.0
 * @see
 */
@Configuration
public class IstioFeignContextAutoConfiguration {

    @Bean
    @Primary
    public RpcContextHolder istioSpringBootFeignRpcContextHolder() {
        return new IstioSpringBootFeignRpcContextHolder();
    }

    @Bean
    public IstioBasicContextCoprocessor istioBasicContextCoprocessor(FeignSpringBootIstioProperties config) {
        return new IstioBasicContextCoprocessor(config);
    }

    @Bean
    public IstioTracingContextCoprocessor istioTracingContextCoprocessor(FeignSpringBootIstioProperties config) {
        return new IstioTracingContextCoprocessor(config);
    }

    @Bean
    public IstioFeignContextServletInterceptor istioFeignContextServletInterceptor() {
        return new IstioFeignContextServletInterceptor();
    }

    @Bean
    public IstioFeignContextWebMvcConfigurer istioFeignContextWebMvcConfigurer(IstioFeignContextServletInterceptor interceptor) {
        return new IstioFeignContextWebMvcConfigurer(interceptor);
    }

    static class IstioFeignContextWebMvcConfigurer implements WebMvcConfigurer {
        private final IstioFeignContextServletInterceptor interceptor;

        public IstioFeignContextWebMvcConfigurer(IstioFeignContextServletInterceptor interceptor) {
            this.interceptor = notNullOf(interceptor, "interceptor");
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(interceptor).addPathPatterns("/**");
        }
    }

    static class IstioFeignContextServletInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            RpcContextHolder.getContext().clearAttachments();
            RpcContextHolder.getServerContext().clearAttachments();
        }
    }

    static class IstioSpringBootFeignRpcContextHolder extends DefaultFeignRpcContextHolder {
    }

}

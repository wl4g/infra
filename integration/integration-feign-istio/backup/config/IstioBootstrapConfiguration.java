///*
// * Copyright (C) 2017 ~ 2025 the original author or authors.
// * <James Wong@gmail.com, 983708408@qq.com> Technology CO.LTD.
// * All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * 
// * Reference to website: http://wl4g.com
// */
//package com.wl4g.infra.integration.feign.istio.config;
//
//import java.util.Arrays;
//
//import javax.annotation.PostConstruct;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.ConfigurableEnvironment;
//import org.springframework.core.env.Environment;
//
//import com.wl4g.infra.integration.feign.istio.util.IstioHelper;
//
///**
// * Auto configuration for Istio bootstrap.
// */
//@Configuration(proxyBeanMethods = false)
//@ConditionalOnProperty(value = "spring.cloud.istio.enabled", matchIfMissing = true)
//@EnableConfigurationProperties(IstioClientProperties.class)
//public class IstioBootstrapConfiguration {
//
//    private static final Log LOG = LogFactory.getLog(IstioBootstrapConfiguration.class);
//
//    private static final String ISTIO_PROFILE = "istio";
//
//    @Bean
//    @ConditionalOnMissingBean
//    public IstioHelper istioHelper(IstioClientProperties config) {
//        return new IstioHelper(config);
//    }
//
//    @EnableConfigurationProperties(IstioClientProperties.class)
//    protected static class IstioDetectionConfiguration {
//
//        private final IstioHelper helper;
//        private final ConfigurableEnvironment environment;
//
//        public IstioDetectionConfiguration(IstioHelper utils, ConfigurableEnvironment environment) {
//            this.helper = utils;
//            this.environment = environment;
//        }
//
//        @PostConstruct
//        public void detectIstio() {
//            addIstioProfile(this.environment);
//        }
//
//        void addIstioProfile(ConfigurableEnvironment environment) {
//            if (this.helper.isIstioEnabled()) {
//                if (hasIstioProfile(environment)) {
//                    if (LOG.isDebugEnabled()) {
//                        LOG.debug("'istio' already in list of active profiles");
//                    }
//                } else {
//                    if (LOG.isDebugEnabled()) {
//                        LOG.debug("Adding 'istio' to list of active profiles");
//                    }
//                    environment.addActiveProfile(ISTIO_PROFILE);
//                }
//            } else {
//                if (LOG.isDebugEnabled()) {
//                    LOG.debug("Not running inside kubernetes with istio enabled. Skipping 'istio' profile activation.");
//                }
//            }
//        }
//
//        private boolean hasIstioProfile(Environment environment) {
//            return Arrays.stream(environment.getActiveProfiles()).anyMatch(ISTIO_PROFILE::equalsIgnoreCase);
//        }
//
//    }
//
//}

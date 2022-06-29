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
package com.wl4g.infra.core.boot.listener;

import java.util.Collection;
import java.util.Properties;

import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * {@link IBootstrappingConfigurer}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version v1.0 2021-01-07
 * @sine v1.0
 * @see
 */
public interface IBootstrappingConfigurer extends Ordered {

    @Override
    default int getOrder() {
        return 0; // Default ordered
    }

    /**
     * Resolve for preset {@link SpringApplication#setHeadless(boolean)}
     * 
     * @param prevHeadless
     * @return
     */
    default Boolean headless(Boolean prevHeadless) {
        return null;
    }

    /**
     * Resolve for preset {@link SpringApplication#setBanner(Banner)}
     * 
     * @param prevBanner
     * @return
     */
    default Banner banner(Banner prevBanner) {
        return null;
    }

    /**
     * Resolve for preset {@link SpringApplication#setBannerMode(Banner.Mode)}
     * 
     * @param prevMode
     * @return
     */
    default Banner.Mode bannerMode(Banner.Mode prevMode) {
        return null;
    }

    /**
     * Resolve for preset {@link SpringApplication#setLogStartupInfo(boolean)}
     * 
     * @param prevLogStartupInfo
     * @return
     */
    default Boolean logStartupInfo(Boolean prevLogStartupInfo) {
        return null;
    }

    /**
     * Resolve for preset
     * {@link SpringApplication#setAddCommandLineProperties(boolean)}
     * 
     * @param prevAddCommandLineProperties
     * @return
     */
    default Boolean addCommandLineProperties(Boolean prevAddCommandLineProperties) {
        return null;
    }

    /**
     * Resolve for preset
     * {@link SpringApplication#setApplicationContextClass(Class)}
     * 
     * @param prevApplicationContextClass
     * @return
     */
    default ApplicationContextFactory setApplicationContextFactory(ApplicationContextFactory applicationContextFactory) {
        return null;
    }

    /**
     * Resolve for preset
     * {@link SpringApplication#setInitializers(java.util.Collection)}
     * 
     * @param prevInitializers
     * @return
     */
    default Collection<? extends ApplicationContextInitializer<?>> initializers(
            Collection<? extends ApplicationContextInitializer<?>> prevInitializers) {
        return null;
    }

    /**
     * Resolve for preset {@link SpringApplication#setListeners(Collection)}
     * 
     * @param prevListeners
     * @return
     */
    default Collection<? extends ApplicationListener<?>> listeners(Collection<? extends ApplicationListener<?>> prevListeners) {
        return null;
    }

    /**
     * Resolve for preset {@link SpringApplication#addListeners(Collection)}
     * 
     * @param prevAddListeners
     * @return
     */
    default ApplicationListener<?>[] addListeners(ApplicationListener<?>[] prevAddListeners) {
        return null;
    }

    /**
     * Resolve for preset
     * {@link SpringApplication#setLazyInitialization(boolean)}
     * 
     * @param prevLazyInitialization
     * @return
     */
    default Boolean lazyInitialization(Boolean prevLazyInitialization) {
        return null;
    }

    /**
     * Resolve for preset
     * {@link SpringApplication#setAllowBeanDefinitionOverriding(boolean)}
     * 
     * @param prevAllowBeanDefinitionOverriding
     * @return
     */
    default Boolean allowBeanDefinitionOverriding(Boolean prevAllowBeanDefinitionOverriding) {
        // In order to encapsulate the upper frame, it must be opened.
        return true;
    }

    /**
     * Resolve for preset
     * {@link SpringApplication#setAdditionalProfiles(String...)}
     * 
     * @param prevAdditionalProfiles
     * @return
     */
    default String[] additionalProfiles(String[] prevAdditionalProfiles) {
        return null;
    }

    /**
     * Resolve for preset
     * {@link SpringApplication#setDefaultProperties(Properties)}
     * 
     * @param prevDefaultProperties
     * @return
     */
    default Properties defaultProperties(Properties prevDefaultProperties) {
        return null;
    }

}

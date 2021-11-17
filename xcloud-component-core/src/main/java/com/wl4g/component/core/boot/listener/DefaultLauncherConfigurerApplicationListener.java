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
package com.wl4g.component.core.boot.listener;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.component.common.collection.CollectionUtils2.extractFirst;
import static com.wl4g.component.common.collection.CollectionUtils2.safeArray;
import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.mustAssignableFrom;
import static com.wl4g.component.common.lang.StringUtils2.isTrue;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.component.common.reflect.ReflectionUtils2.getField;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.replaceAll;
import static org.apache.commons.lang3.StringUtils.replaceEach;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.springframework.boot.context.config.ConfigFileApplicationListener.ACTIVE_PROFILES_PROPERTY;
import static org.springframework.boot.context.config.ConfigFileApplicationListener.CONFIG_ADDITIONAL_LOCATION_PROPERTY;
import static org.springframework.boot.context.config.ConfigFileApplicationListener.CONFIG_LOCATION_PROPERTY;
import static org.springframework.boot.context.config.ConfigFileApplicationListener.CONFIG_NAME_PROPERTY;
import static org.springframework.boot.context.config.ConfigFileApplicationListener.INCLUDE_PROFILES_PROPERTY;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ReflectionUtils;

import com.google.common.io.Resources;
import com.wl4g.component.common.collection.CollectionUtils2;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.resource.StreamResource;
import com.wl4g.component.common.resource.resolver.ClassPathResourcePatternResolver;

import groovy.lang.GroovyClassLoader;

/**
 * Config bootstrap application listener. Before executing
 * {@link ConfigFileApplicationListener}, in order to set the boot
 * configuration.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年5月20日
 * @since
 */
public class DefaultLauncherConfigurerApplicationListener implements GenericApplicationListener {
    protected final SmartLogger log = getLogger(getClass());

    // Notes: If you need to customize boot configuration (override this kind of
    // logic), please inherit this class and rewrite this method, and set the
    // return value to be larger.
    //
    // 注：如果需要自定义启动引导配置（覆盖此类逻辑），请继承此类并重写此方法，设置返回值大于此值即可
    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        return isAssignableFrom(resolvableType.getRawClass(), ApplicationStartingEvent.class);
    }

    /**
     * Refer to {@link LoggingApplicationListener} implemention
     */
    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return isAssignableFrom(sourceType, SpringApplication.class, ApplicationContext.class);
    }

    /**
     * Refer to: </br>
     * {@link org.springframework.boot.SpringApplication#run(String)} and
     * {@link org.springframework.boot.SpringApplicationRunListeners#starting()}
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            try {
                ApplicationStartingEvent starting = (ApplicationStartingEvent) event;
                presetSpringApplication(starting, starting.getSpringApplication());
            } catch (Exception e) {
                throw new IllegalStateException("Cannot preset SpringApplication properties", e);
            }
        }
    }

    /**
     * Preset {@link SpringApplication} properties.
     * 
     * @param event
     * @param application
     * @throws Exception
     */
    protected void presetSpringApplication(ApplicationStartingEvent event, SpringApplication application) throws Exception {
        // Parse command-line arguments.
        ApplicationArguments args = new DefaultApplicationArguments(event.getArgs());
        // Skip disable configurer?
        if (isTrue(extractFirst(args.getOptionValues(PROPERTY_DISABLE)), false)) {
            return;
        }

        // Load spring launcher configurer.
        boolean isDebug = isTrue(extractFirst(args.getOptionValues(PROPERTY_DEBUG)), false);
        ISpringLauncherConfigurer configurer = loadClassAndInstantiateSpringLauncherConfigurer();
        if (nonNull(configurer)) {
            presetDefaultProperties(isDebug, args, event, application, configurer);
            presetAdditionalProfiles(isDebug, args, event, application, configurer);
            presetOtherProperties(isDebug, args, event, application, configurer);
        }
    }

    protected void presetDefaultProperties(boolean isDebug, ApplicationArguments args, ApplicationStartingEvent event,
            SpringApplication application, ISpringLauncherConfigurer configurer) throws Exception {
        Properties presetProperties = new Properties();
        // defaultProperties.put("spring.main.allow-bean-definition-overriding","true");
        if (nonNull(configurer.defaultProperties())) {
            safeMap(configurer.defaultProperties()).forEach((key, value) -> presetProperties.put(key,
                    defaultSafeCommClear.apply(defaultTrim2EmptyClear.apply((String) value))));
        }

        // Priority order: 1 start args properties, 2 existing properties, 3
        // script automatically read properties.

        // Command-line args preferred.
        for (String argName : args.getOptionNames()) {
            presetProperties.remove(argName);
        }

        // Merge existing properties(key-values).
        Map<String, Object> existingDefaultProperties = getField(
                findField(SpringApplication.class, "defaultProperties", Map.class), application, true);
        safeMap(existingDefaultProperties).forEach((key, value) -> {
            if (DEFAULT_PROPERTIES_MERGE_KEYS.contains(key)) { // Merge(if-necessary)
                String presetValue = presetProperties.getProperty(key);
                if (nonNull(presetValue)) {
                    value = value + "," + presetValue;
                }
            }
            presetProperties.put(key, value);
        });

        if (isDebug) {
            log.debug("Preset SpringApplication#setDefaultProperties(Final): {}", presetProperties);
        }
        application.setDefaultProperties(presetProperties);
    }

    protected void presetAdditionalProfiles(boolean isDebug, ApplicationArguments args, ApplicationStartingEvent event,
            SpringApplication application, ISpringLauncherConfigurer configurer) throws Exception {
        if (nonNull(configurer.additionalProfiles())) {
            String[] additionalProfiles = safeArrayToList(configurer.additionalProfiles()).stream()
                    .map(p -> defaultTrim2EmptyClear.apply(p)).toArray(String[]::new);

            if (isDebug) {
                log.debug("Preset SpringApplication#setAdditionalProfiles: {}", asList(additionalProfiles));
            }
            application.setAdditionalProfiles(additionalProfiles);
        }
    }

    protected void presetOtherProperties(boolean isDebug, ApplicationArguments args, ApplicationStartingEvent event,
            SpringApplication application, ISpringLauncherConfigurer configurer) throws Exception {
        if (nonNull(configurer.headless())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setHeadless: {}", configurer.headless());
            }
            application.setHeadless(configurer.headless());
        }
        if (nonNull(configurer.logStartupInfo())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setLogStartupInfo: {}", configurer.logStartupInfo());
            }
            application.setLogStartupInfo(configurer.logStartupInfo());
        }
        if (nonNull(configurer.banner())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setBanner: {}", configurer.banner());
            }
            application.setBanner(configurer.banner());
        }
        if (nonNull(configurer.bannerMode())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setBannerMode: {}", configurer.bannerMode());
            }
            application.setBannerMode(configurer.bannerMode());
        }
        if (nonNull(configurer.allowBeanDefinitionOverriding())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setAllowBeanDefinitionOverriding: {}",
                        configurer.allowBeanDefinitionOverriding());
            }
            application.setAllowBeanDefinitionOverriding(configurer.allowBeanDefinitionOverriding());
        }
        if (nonNull(configurer.addCommandLineProperties())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setAddCommandLineProperties: {}", configurer.addCommandLineProperties());
            }
            application.setAddCommandLineProperties(configurer.addCommandLineProperties());
        }
        if (nonNull(configurer.lazyInitialization())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setLazyInitialization: {}", configurer.lazyInitialization());
            }
            application.setLazyInitialization(configurer.lazyInitialization());
        }
        if (nonNull(configurer.applicationContextClass())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setApplicationContextClass: {}", configurer.applicationContextClass());
            }
            application.setApplicationContextClass(configurer.applicationContextClass());
        }
        if (nonNull(configurer.initializers())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setInitializers: {}", configurer.initializers());
            }
            application.setInitializers(configurer.initializers());
        }
        if (nonNull(configurer.listeners())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#setListeners: {}", configurer.listeners());
            }
            application.setListeners(configurer.listeners());
        }
        if (nonNull(configurer.addListeners())) {
            if (isDebug) {
                log.debug("Preset SpringApplication#addListeners: {}", asList(configurer.addListeners()));
            }
            application.addListeners(configurer.addListeners());
        }
    }

    /**
     * Resolve launcher class and instantiate.
     * 
     * @return
     * @throws Exception
     */
    private ISpringLauncherConfigurer loadClassAndInstantiateSpringLauncherConfigurer() throws Exception {
        // Load launcher classes.
        List<Class<?>> classes = emptyList();
        try (GroovyClassLoader gcl = new GroovyClassLoader()) {
            ClassPathResourcePatternResolver resolver = new ClassPathResourcePatternResolver();
            Set<StreamResource> ress = resolver.getResources(DEFAULT_LAUNCHER_CLASSNAME);
            classes = ress.stream().map(r -> {
                try {
                    return gcl.parseClass(Resources.toString(r.getURL(), UTF_8),
                            defaultClassNameConverter.apply(r.getFilename()));
                } catch (CompilationFailedException | IOException e) {
                    throw new IllegalStateException(e);
                }
            }).map(c -> mustAssignableFrom(ISpringLauncherConfigurer.class, c)).collect(toList());
        }
        if (!CollectionUtils2.isEmpty(classes)) {
            List<Object> candidates = classes.stream().map(cls -> {
                try {
                    return ReflectionUtils.accessibleConstructor(cls).newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).collect(toList());
            AnnotationAwareOrderComparator.sort(candidates);
            return (ISpringLauncherConfigurer) candidates.get(0); // Using best
        }
        return null;
    }

    /**
     * Check type is assignable from supportedTypes
     * 
     * @param type
     * @param supportedTypes
     * @return
     */
    private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
        if (type != null) {
            for (Class<?> supportedType : safeArray(Class.class, supportedTypes)) {
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Standard java class name converter.
    private static final Function<String, String> defaultClassNameConverter = filename -> replaceEach(filename,
            new String[] { "!", "@", "#", "-", "&", "*" }, new String[] { "_", "_", "_", "_", "_", "_" });
    // Newline and invalid char clear.
    private static final Function<String, String> defaultTrim2EmptyClear = value -> replaceAll(value, "\\s*| |\t|\r|\\r|\n|\\n",
            "");
    // Spring boot config end comm clear.
    private static final Function<String, String> defaultSafeCommClear = value -> join(split(trimToEmpty(value), ","), ",");
    private static final String DEFAULT_LAUNCHER_CLASSNAME = "classpath*:/META-INF/spring-launcher.groovy";
    private static final String PROPERTY_DISABLE = "spring.launcher.configurer.disable";
    private static final String PROPERTY_DEBUG = "spring.launcher.configurer.debug";
    private static final List<String> DEFAULT_PROPERTIES_MERGE_KEYS = asList(ACTIVE_PROFILES_PROPERTY, INCLUDE_PROFILES_PROPERTY,
            CONFIG_NAME_PROPERTY, CONFIG_ADDITIONAL_LOCATION_PROPERTY, CONFIG_LOCATION_PROPERTY);

    public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 5;
}
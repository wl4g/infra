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
package com.wl4g.infra.context.boot.listener;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeArray;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.mustAssignableFrom;
import static com.wl4g.infra.common.lang.StringUtils2.isTrue;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static java.lang.String.format;
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
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ReflectionUtils;

import com.google.common.io.Resources;
import com.wl4g.infra.common.collection.CollectionUtils2;
import com.wl4g.infra.common.lang.Assert2;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.resource.StreamResource;
import com.wl4g.infra.common.resource.resolver.ClassPathResourcePatternResolver;

import groovy.lang.GroovyClassLoader;

/**
 * Config bootstrap application listener. Before executing
 * {@link ConfigFileApplicationListener}, in order to set the boot
 * configuration.
 * 
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2020年5月20日
 * @since
 */
@SuppressWarnings("deprecation")
public class BootstrappingConfigApplicationListener implements GenericApplicationListener {
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
                throw new IllegalStateException("Could't preset SpringApplication properties", e);
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
        // Enabled bootstrapping?
        if (!isTrue(System.getenv(ENV_ENABLED), true)) {
            return;
        }

        // Load BootstrappingConfigurers.
        boolean isDebug = isTrue(System.getenv(ENV_DEBUG), false);
        List<IBootstrappingConfigurer> configurers = loadClassAndInstantiateConfigurers();
        if (!isEmpty(configurers)) {
            presetDefaultProperties(isDebug, args, event, application, configurers);
            presetAdditionalProfiles(isDebug, args, event, application, configurers);
            presetOtherProperties(isDebug, args, event, application, configurers);
        }
    }

    /**
     * Priority orders: </br>
     * 
     * <pre>
     * 1) Startup arguments properties;
     * 2) Existing properties;
     * 3) Groovy script automatically read properties;
     * </pre>
     * 
     * @param isDebug
     * @param args
     * @param event
     * @param application
     * @param configurers
     * @throws Exception
     */
    protected void presetDefaultProperties(
            boolean isDebug,
            ApplicationArguments args,
            ApplicationStartingEvent event,
            SpringApplication application,
            List<IBootstrappingConfigurer> configurers) throws Exception {
        Properties mergedProps = new Properties();
        // mergedProps.put("spring.main.allow-bean-definition-overriding","true");

        // Gets default properties in chains.
        Properties addDefaultProps = new Properties();
        for (IBootstrappingConfigurer configure : configurers) {
            configure.defaultProperties(addDefaultProps);
            if (nonNull(addDefaultProps)) {
                // Safe clean value.
                safeMap(addDefaultProps).forEach((key, value) -> mergedProps.put(key,
                        defaultSafeCommClear.apply(defaultTrim2EmptyClear.apply((String) value))));
            }
        }

        // Command-line arguments preferred.
        for (String argName : args.getOptionNames()) {
            mergedProps.remove(argName);
        }

        // Merge existing properties(key-values).
        Map<String, Object> existingDefaultProps = getField(findField(SpringApplication.class, "defaultProperties", Map.class),
                application, true);
        safeMap(existingDefaultProps).forEach((key, value) -> {
            if (DEFAULT_PROPERTIES_MERGE_KEYS.contains(key)) { // Merge(if-necessary)
                String presetValue = mergedProps.getProperty(key);
                if (nonNull(presetValue)) {
                    value = value + "," + presetValue;
                }
            }
            mergedProps.put(key, value);
        });

        if (isDebug) {
            log.debug("Preset SpringApplication#setDefaultProperties(Final): {}", mergedProps);
        }
        application.setDefaultProperties(mergedProps);
    }

    protected void presetAdditionalProfiles(
            boolean isDebug,
            ApplicationArguments args,
            ApplicationStartingEvent event,
            SpringApplication application,
            List<IBootstrappingConfigurer> configurers) throws Exception {
        Set<String> currentAdditionalProfiles = new HashSet<>();
        for (IBootstrappingConfigurer configure : configurers) {
            configure.additionalProfiles(currentAdditionalProfiles);
        }
        if (!isEmpty(currentAdditionalProfiles)) {
            String[] additionalProfiles = currentAdditionalProfiles.stream()
                    .map(p -> defaultTrim2EmptyClear.apply(p))
                    .toArray(String[]::new);
            if (isDebug) {
                log.debug("Preset SpringApplication#setAdditionalProfiles: {}", asList(additionalProfiles));
            }
            application.setAdditionalProfiles(additionalProfiles);
        }
    }

    protected void presetOtherProperties(
            boolean isDebug,
            ApplicationArguments args,
            ApplicationStartingEvent event,
            SpringApplication application,
            List<IBootstrappingConfigurer> configurers) throws Exception {
        Boolean currentHeadless = null;
        Boolean currentLogStartupInfo = null;
        Banner currentBanner = null;
        Banner.Mode currentMode = null;
        Boolean currentAllowBeanDefinitionOverriding = null;
        Boolean currentCommandLineProperties = null;
        Boolean currentAddCommandLineProperties = null;
        Boolean currentLazyInitialization = null;
        ApplicationContextFactory currentApplicationContextFactory = null;
        Collection<? extends ApplicationContextInitializer<?>> currentInitializers = null;
        Collection<? extends ApplicationListener<?>> currentListeners = null;
        ApplicationListener<?>[] currentAddListeners = null;
        for (IBootstrappingConfigurer configure : configurers) {
            currentHeadless = configure.headless(currentHeadless);
            currentLogStartupInfo = configure.logStartupInfo(currentLogStartupInfo);
            currentBanner = configure.banner(currentBanner);
            currentMode = configure.bannerMode(currentMode);
            currentAllowBeanDefinitionOverriding = configure.allowBeanDefinitionOverriding(currentAllowBeanDefinitionOverriding);
            currentCommandLineProperties = configure.addCommandLineProperties(currentCommandLineProperties);
            currentLazyInitialization = configure.lazyInitialization(currentLazyInitialization);
            currentApplicationContextFactory = configure.setApplicationContextFactory(currentApplicationContextFactory);
            currentInitializers = configure.initializers(currentInitializers);
            currentListeners = configure.listeners(currentListeners);
            currentAddListeners = configure.addListeners(currentAddListeners);
        }
        if (nonNull(currentHeadless)) {
            application.setHeadless(currentHeadless);
        }
        if (nonNull(currentLogStartupInfo)) {
            application.setLogStartupInfo(currentLogStartupInfo);
        }
        if (nonNull(currentBanner)) {
            application.setBanner(currentBanner);
        }
        if (nonNull(currentMode)) {
            application.setBannerMode(currentMode);
        }
        if (nonNull(currentAllowBeanDefinitionOverriding)) {
            application.setAllowBeanDefinitionOverriding(currentAllowBeanDefinitionOverriding);
        }
        if (nonNull(currentCommandLineProperties)) {
            application.setAddCommandLineProperties(currentCommandLineProperties);
        }
        if (nonNull(currentLazyInitialization)) {
            application.setLazyInitialization(currentLazyInitialization);
        }
        if (nonNull(currentApplicationContextFactory)) {
            application.setApplicationContextFactory(currentApplicationContextFactory);
        }
        if (nonNull(currentInitializers)) {
            application.setInitializers(currentInitializers);
        }
        if (nonNull(currentListeners)) {
            application.setListeners(currentListeners);
        }
        if (nonNull(currentAddListeners)) {
            application.addListeners(currentAddListeners);
        }
        if (isDebug) {
            log.debug("Preset SpringApplication#setHeadless: {}", currentHeadless);
            log.debug("Preset SpringApplication#setLogStartupInfo: {}", currentLogStartupInfo);
            log.debug("Preset SpringApplication#setBanner: {}", currentBanner);
            log.debug("Preset SpringApplication#setBannerMode: {}", currentMode);
            log.debug("Preset SpringApplication#setAllowBeanDefinitionOverriding: {}", currentAllowBeanDefinitionOverriding);
            log.debug("Preset SpringApplication#setAddCommandLineProperties: {}", currentAddCommandLineProperties);
            log.debug("Preset SpringApplication#setLazyInitialization: {}", currentLazyInitialization);
            log.debug("Preset SpringApplication#setApplicationContextFactory: {}", currentApplicationContextFactory);
            log.debug("Preset SpringApplication#setInitializers: {}", currentInitializers);
            log.debug("Preset SpringApplication#setListeners: {}", currentListeners);
            log.debug("Preset SpringApplication#addListeners: {}", asList(currentAddListeners));
        }
    }

    /**
     * Resolve {@link IBootstrappingConfigurer} class and instantiate.
     * 
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private List<IBootstrappingConfigurer> loadClassAndInstantiateConfigurers() throws Exception {
        // Load launcher classes.
        List<Class<?>> classes = emptyList();
        try (GroovyClassLoader gcl = new GroovyClassLoader()) {
            ClassPathResourcePatternResolver resolver = new ClassPathResourcePatternResolver(
                    Thread.currentThread().getContextClassLoader());
            Set<StreamResource> ress = resolver.getResources(BOOTSTRAPPING_RESOURCE_NAME);
            classes = ress.stream().map(r -> {
                try {
                    return gcl.parseClass(Resources.toString(r.getURL(), UTF_8),
                            defaultClassNameConverter.apply(r.getFilename()));
                } catch (CompilationFailedException | IOException e) {
                    throw new IllegalStateException(format("resource: %s", r), e);
                }
            }).map(c -> mustAssignableFrom(IBootstrappingConfigurer.class, c)).collect(toList());
        }
        if (!CollectionUtils2.isEmpty(classes)) {
            List<?> candidates = classes.stream().map(cls -> {
                try {
                    return ReflectionUtils.accessibleConstructor(cls).newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).collect(toList());
            AnnotationAwareOrderComparator.sort(candidates);
            // The highest priority is executed last, overwriting the previous.
            Collections.reverse(candidates);
            return (List<IBootstrappingConfigurer>) candidates;
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
    // Spring boot configuration end comm clear.
    private static final Function<String, String> defaultSafeCommClear = value -> join(split(trimToEmpty(value), ","), ",");
    private static final String BOOTSTRAPPING_RESOURCE_NAME = "classpath*:/META-INF/bootstrapping.groovy";
    private static final String ENV_ENABLED = "SPRING_BOOTSTRAPPING_ENABLED";
    private static final String ENV_DEBUG = "SPRING_BOOTSTRAPPING_DEBUG";
    private static final List<String> DEFAULT_PROPERTIES_MERGE_KEYS = asList(ACTIVE_PROFILES_PROPERTY, INCLUDE_PROFILES_PROPERTY,
            CONFIG_NAME_PROPERTY, CONFIG_ADDITIONAL_LOCATION_PROPERTY, CONFIG_LOCATION_PROPERTY);

    public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 5;

    private static void checkVMPluginSupported() {
        Assert2.notNull(VMPluginFactory.getPlugin(),
                "Your JVM version is not supported by groovy. Please check the source codes: org.codehaus.groovy.vmplugin.VMPluginFactory. "
                        + "Or you can force the bootstrapping features to be disabled with -D%s=false",
                ENV_ENABLED);
    }

    static {
        checkVMPluginSupported();
    }
}
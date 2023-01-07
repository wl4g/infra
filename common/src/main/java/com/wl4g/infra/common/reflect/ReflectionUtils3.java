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
package com.wl4g.infra.common.reflect;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeSet;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.StringUtils2.replace;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.startsWithAny;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import javax.validation.constraints.NotNull;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import com.wl4g.infra.common.lang.ClassUtils2;

import lombok.CustomLog;

/**
 * {@link ReflectionUtils3}
 * 
 * @author James Wong
 * @version 2023-01-07
 * @since v3.0.0
 */
@CustomLog
public abstract class ReflectionUtils3 extends ReflectionUtils2 {

    /**
     * Create {@link Reflections} instance with default config.
     * 
     * @param classPackages
     * @return
     */
    public static Reflections buildDefaultResourceReflections(final @NotNull Set<String> classPackages) {
        notNullOf(classPackages, "classPackages");
        ConfigurationBuilder config = ConfigurationBuilder.build(classPackages)
                .setScanners(Scanners.Resources, Scanners.TypesAnnotated, Scanners.SubTypes);
        return new Reflections(config);
    }

    @SuppressWarnings("unchecked")
    public static Collection<Class<?>> findClassesByAnnotation(
            final @NotNull Class<? extends Annotation> annotation,
            final @NotNull Set<String> classPackages,
            final @NotNull Collection<URL> findUrls) {
        notNullOf(annotation, "annotation");
        return (Collection<Class<?>>) doFind(classPackages, findUrls,
                reflections -> reflections.getTypesAnnotatedWith(annotation));
    }

    @SuppressWarnings("unchecked")
    public static Collection<Class<?>> findClassesBySuperClass(
            final @NotNull Class<?> superClass,
            final @NotNull Set<String> classPackages,
            final @NotNull Collection<URL> findUrls) {
        notNullOf(superClass, "superClass");
        return (Collection<Class<?>>) doFind(classPackages, findUrls, reflections -> reflections.getSubTypesOf(superClass));
    }

    @SuppressWarnings("unchecked")
    public static Collection<Class<?>> findClassesAll(
            final @NotNull Set<String> classPackages,
            final @NotNull Collection<URL> findUrls) {
        final String[] classPackageArray = classPackages.toArray(new String[0]);
        return (Collection<Class<?>>) doFind(classPackages, findUrls,
                reflections -> safeSet(reflections.getAll(Scanners.SubTypes)).stream()
                        .filter(name -> startsWithAny(name, classPackageArray))
                        .map(name -> {
                            try {
                                return ClassUtils2.forName(name, null);
                            } catch (ClassNotFoundException | LinkageError e) {
                                log.warn("Unable to load class. reason: {}", e.getMessage());
                                return null;
                            }
                        })
                        .filter(cls -> nonNull(cls))
                        .collect(toSet()));
    }

    @SuppressWarnings("unchecked")
    public static Collection<Class<?>> findClassesAllWithResource(
            final @NotNull Set<String> classPackages,
            final @NotNull Collection<URL> findUrls) {
        final String[] classPackageArray = safeSet(classPackages).stream().collect(toList()).toArray(new String[0]);
        return (Collection<Class<?>>) doFind(classPackages, findUrls,
                reflections -> safeSet(reflections.getAll(Scanners.Resources)).stream()
                        .filter(name -> endsWith(name, ".class") && startsWithAny(replace(name, "/", "."), classPackageArray))
                        .map(name -> {
                            try {
                                return ClassUtils2.forName(replace(name, "/", "."), null);
                            } catch (ClassNotFoundException | LinkageError e) {
                                log.warn("Unable to load class. reason: {}", e.getMessage());
                                return null;
                            }
                        })
                        .filter(cls -> nonNull(cls))
                        .collect(toSet()));
    }

    public static Object doFind(
            final @NotNull Set<String> classPackages,
            final @NotNull Collection<URL> findUrls,
            final @NotNull Function<Reflections, Object> process) {
        notNullOf(classPackages, "classPackages");
        notNullOf(findUrls, "findUrls");
        notNullOf(process, "process");
        final ClassLoader parent = Thread.currentThread().getContextClassLoader();
        try {
            final ClassLoader targetClassLoader = new URLClassLoader(findUrls.toArray(new URL[0]), parent);

            // set the TCCL before everything else
            Thread.currentThread().setContextClassLoader(targetClassLoader);

            return process.apply(buildDefaultResourceReflections(classPackages));
        } catch (Exception e) {
            throw e;
        } finally {
            // reset the TCCL back to the original class loader
            Thread.currentThread().setContextClassLoader(parent);
        }
    }

}

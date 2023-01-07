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
import org.reflections.scanners.SubTypesScanner;
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
@SuppressWarnings("deprecation")
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
        // @formatter:off
        final ConfigurationBuilder config = ConfigurationBuilder.build()
                .forPackages(classPackages.toArray(new String[0]))
                .setParallel(false)
                // 虽然新版推荐使用枚举 Scanners.SubTypes 但它存在巨坑, 即默认扫描会排除超类是 java.lang.Object 的所有类, 因此这里必须使用自定义(或设置)的 SubTypeScanner.
                // 问题源码分析参见:
                // https://github.com/ronmamo/reflections/blob/0.10.2/src/main/java/org/reflections/scanners/Scanners.java#L50
                // https://github.com/ronmamo/reflections/blob/0.10.2/src/main/java/org/reflections/Reflections.java#L186
                // https://github.com/ronmamo/reflections/blob/0.10.2/src/main/java/org/reflections/scanners/Scanners.java#L201
                .setScanners(new SubTypesScanner(false), Scanners.TypesAnnotated, Scanners.Resources /*Scanners.SubTypes*//*, Scanners.TypesAnnotated, Scanners.Resources, Scanners.ConstructorsAnnotated,
                        Scanners.ConstructorsParameter, Scanners.ConstructorsSignature, Scanners.FieldsAnnotated,
                        Scanners.ConstructorsSignature, Scanners.MethodsAnnotated, Scanners.MethodsParameter,
                        Scanners.MethodsReturn, Scanners.MethodsSignature*/);
        // @formatter:on
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
        return (Collection<Class<?>>) doFind(classPackages, findUrls, reflections -> {
            Set<String> subTypesAll = reflections.getAll(Scanners.SubTypes);
            // Set<String> typesAnnotatedAll =
            // reflections.getAll(Scanners.TypesAnnotated);
            // Set<String> resourcesAnnotatedAll =
            // reflections.getAll(Scanners.Resources);
            // Set<String> constructorsAnnotatedAll =
            // reflections.getAll(Scanners.ConstructorsAnnotated);
            // Set<String> constructorsParameterAll =
            // reflections.getAll(Scanners.ConstructorsParameter);
            // Set<String> constructorsSignatureAll =
            // reflections.getAll(Scanners.ConstructorsSignature);
            // Set<String> fieldsAnnotatedAll =
            // reflections.getAll(Scanners.FieldsAnnotated);
            // Set<String> methodsAnnotatedAll =
            // reflections.getAll(Scanners.MethodsAnnotated);
            // Set<String> methodsParameterAll =
            // reflections.getAll(Scanners.MethodsParameter);
            // Set<String> methodsReturnAll =
            // reflections.getAll(Scanners.MethodsReturn);
            // Set<String> methodsSignatureAll =
            // reflections.getAll(Scanners.MethodsSignature);
            return safeSet(subTypesAll).stream().filter(name -> startsWithAny(name, classPackageArray)).map(name -> {
                try {
                    return ClassUtils2.forName(name, null);
                } catch (ClassNotFoundException | LinkageError e) {
                    log.warn("Unable to load class. reason: {}", e.getMessage());
                    return null;
                }
            }).filter(cls -> nonNull(cls)).collect(toSet());
        });
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

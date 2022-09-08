/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.common.bean;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.isCompatibleType;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.makeAccessible;
import static com.wl4g.infra.common.reflect.TypeUtils2.isSimpleCollectionType;
import static com.wl4g.infra.common.reflect.TypeUtils2.isSimpleType;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.startsWithAny;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.wl4g.infra.common.reflect.ReflectionUtils2.FieldFilter;

/**
 * {@link ConfigBeanUtils}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-04-20 v3.0.0
 * @since v3.0.0
 */
public abstract class ConfigBeanUtils {

    /**
     * The copies default configuration object properties deep to the target
     * configuration object, overriding as needed.
     * 
     * @param initTargetConfig
     * @param targetConfig
     * @param defaultConfig
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static @NotNull <I extends D, T extends D, D> T configureWithDefault(
            @NotNull I initTargetConfig,
            @NotNull T targetConfig,
            @NotNull D defaultConfig) throws IllegalArgumentException, IllegalAccessException {
        notNullOf(initTargetConfig, "initTargetConfig");
        notNullOf(targetConfig, "targetConfig");
        notNullOf(defaultConfig, "defaultConfig");

        if (initTargetConfig.getClass() != targetConfig.getClass()) {
            throw new IllegalArgumentException(format(
                    "The target configuration object must be of the exact same type as the initial configuration object. %s != %s",
                    targetConfig.getClass(), initTargetConfig.getClass()));
        }

        deepCopyFieldStateWithInit(initTargetConfig, targetConfig, defaultConfig, BeanUtils2.DEFAULT_FIELD_FILTER,
                (
                        @Nullable Object initTarget,
                        @NotNull Object target,
                        @NotNull Field tf,
                        @NotNull Field sf,
                        @Nullable Object sourcePropertyValue) -> {

                    if (nonNull(sourcePropertyValue)) {
                        Object initTargetPropertyValue = tf.get(initTarget);
                        Object targetPropertyValue = tf.get(target);
                        boolean flag = false;
                        if (isNull(initTargetPropertyValue)) {
                            if (isNull(targetPropertyValue)) {
                                flag = true;
                            }
                        } else if (!initTargetPropertyValue.equals(sourcePropertyValue)
                                && initTargetPropertyValue.equals(targetPropertyValue)) {
                            flag = true;
                        }
                        if (flag) {
                            tf.setAccessible(true);
                            tf.set(target, sourcePropertyValue);
                        }
                    }
                });

        return targetConfig;
    }

    /**
     * Calls the given callback on all fields of the target class, recursively
     * running the class hierarchy up to copy all declared fields.</br>
     * It will contain all the fields defined by all parent or superclasses. At
     * the same time, the target and the source object must be compatible.
     * 
     * @param target
     *            The target object to copy to
     * @param src
     *            Source object
     * @param ff
     *            Field filter
     * @param fp
     *            Customizable copyer
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static void deepCopyFieldStateWithInit(
            @Nullable Object initTarget,
            @NotNull Object target,
            @NotNull Object src,
            @NotNull FieldFilter ff,
            @NotNull FieldProcessor2 fp) throws IllegalArgumentException, IllegalAccessException {

        if (!(target != null && src != null && ff != null && fp != null)) {
            throw new IllegalArgumentException("Target and source FieldFilter and FieldProcessor2 must not be null");
        }

        // Check if the target is compatible with the source object
        Class<?> targetClass = target.getClass(), sourceClass = src.getClass();
        if (!isCompatibleType(target.getClass(), src.getClass())) {
            throw new IllegalArgumentException(
                    format("Incompatible the objects, target class: %s, source class: %s", targetClass, sourceClass));
        }

        Class<?> targetCls = target.getClass(); // [MARK0]
        do {
            doDeepCopyFieldsWithInit(targetCls, initTarget, target, src, ff, fp);
        } while ((targetCls = targetCls.getSuperclass()) != Object.class);
    }

    /**
     * Calls the given callback on all fields of the target class, recursively
     * running the class hierarchy up to copy all declared fields.</br>
     * Note: that it does not contain fields defined by the parent or super
     * class. At the same time, the target and the source object must be
     * compatible.</br>
     * Note: Attribute fields of parent and superclass are not included
     * 
     * @param currentTargetClass
     *            The level of the class currently copied to (upward recursion)
     * @param target
     *            The target object to copy to
     * @param src
     *            Source object
     * @param ff
     *            Field filter
     * @param fp
     *            Customizable copyer
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static void doDeepCopyFieldsWithInit(
            Class<?> currentTargetClass,
            @Nullable Object initTarget,
            @NotNull Object target,
            @NotNull Object src,
            @NotNull FieldFilter ff,
            @NotNull FieldProcessor2 fp) throws IllegalArgumentException, IllegalAccessException {

        if (isNull(currentTargetClass) || isNull(ff) || isNull(fp)) {
            throw new IllegalArgumentException(
                    "Hierarchy current target class or source FieldFilter and FieldProcessor2 can't null");
        }
        // Skip the current level copy.
        if (isNull(src) || isNull(target)) {
            return;
        }
        // Check is only required when the initial object is not empty.
        if (nonNull(initTarget) && initTarget.getClass() != target.getClass()) {
            throw new IllegalArgumentException(
                    format("The initial target object must be of the exact same type as the target object. %s != %s",
                            initTarget.getClass(), target.getClass()));
        }

        // Recursive traversal matching and processing
        Class<?> sourceClass = src.getClass();
        for (Field tf : currentTargetClass.getDeclaredFields()) {
            // Must be filtered over.
            // [BUGFIX]: for example when recursively getting
            // java.nio.charset.Charset, there will be an infinite loop stack
            // overflow (jvm8 defaults to 1024)
            if (Modifier.isFinal(tf.getModifiers())
                    || startsWithAny(tf.getDeclaringClass().getName(), "java.nio", "java.util", "org.apache.commons.lang",
                            "org.springframework.util", "org.springframework.web.util", "org.springframework.boot.util")) {
                continue;
            }

            makeAccessible(tf);
            Object initTargetPropertyValue = nonNull(initTarget) ? tf.get(initTarget) : null;
            Object targetPropertyValue = tf.get(target); // See:[MARK0]
            Object sourcePropertyValue = null;
            Field sf = findField(sourceClass, tf.getName());
            if (nonNull(sf)) {
                makeAccessible(sf);
                sourcePropertyValue = sf.get(src);
            }

            // Base type or collection type or enum?
            if (isSimpleType(tf.getType()) || isSimpleCollectionType(tf.getType()) || tf.getType().isEnum()) {
                // [MARK2] Filter matching property
                if (nonNull(fp) && ff.matches(tf)) {
                    fp.doProcess(initTarget, target, tf, sf, sourcePropertyValue);
                }
            } else {
                doDeepCopyFieldsWithInit(tf.getType(), initTargetPropertyValue, targetPropertyValue, sourcePropertyValue, ff, fp);
            }
        }
    }

    public static interface FieldProcessor2 {
        void doProcess(
                @Nullable Object initTarget,
                @NotNull Object target,
                @NotNull Field tf,
                @NotNull Field sf,
                @Nullable Object sourcePropertyValue) throws IllegalArgumentException, IllegalAccessException;
    }
}

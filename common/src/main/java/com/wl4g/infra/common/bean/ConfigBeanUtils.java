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
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-04-20 v3.0.0
 * @since v3.0.0
 */
public abstract class ConfigBeanUtils {

    /**
     * The copies default configuration object properties deep to the destObj
     * configuration object, overriding as needed.
     * 
     * @param initObj
     * @param destObj
     * @param defaultObj
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static @NotNull <I extends D, T extends D, D> T configureWithDefault(
            @NotNull I initObj,
            @NotNull T destObj,
            @NotNull D defaultObj) throws IllegalArgumentException, IllegalAccessException {
        notNullOf(initObj, "initObj");
        notNullOf(destObj, "destObj");
        notNullOf(defaultObj, "defaultObj");

        if (initObj.getClass() != destObj.getClass()) {
            throw new IllegalArgumentException(format(
                    "The destObj configuration object must be of the exact same type as the initial configuration object. %s != %s",
                    destObj.getClass(), initObj.getClass()));
        }

        deepCopyFieldStateWithInit(initObj, destObj, defaultObj, BeanUtils2.DEFAULT_FIELD_FILTER,
                (
                        @Nullable Object init,
                        @NotNull Object dest,
                        @NotNull Field tf,
                        @NotNull Field sf,
                        @Nullable Object defaultPropertyValue) -> {

                    if (nonNull(defaultPropertyValue)) {
                        Object initObjPropertyValue = tf.get(init);
                        Object destObjPropertyValue = tf.get(dest);
                        boolean flag = false;
                        if (isNull(initObjPropertyValue)) {
                            if (isNull(destObjPropertyValue)) {
                                flag = true;
                            }
                        } else {
                            if ((isNull(destObjPropertyValue) || destObjPropertyValue.equals(initObjPropertyValue))
                                    && !defaultPropertyValue.equals(initObjPropertyValue)) {
                                flag = true;
                            }
                        }
                        if (flag) {
                            tf.setAccessible(true);
                            tf.set(dest, defaultPropertyValue);
                        }
                    }
                });

        return destObj;
    }

    /**
     * Calls the given callback on all fields of the destObj class, recursively
     * running the class hierarchy up to copy all declared fields.</br>
     * It will contain all the fields defined by all parent or superclasses. At
     * the same time, the destObj and the source object must be compatible.
     * 
     * @param destObj
     *            The destObj object to copy to
     * @param defaultObj
     *            Default/Source object
     * @param ff
     *            Field filter
     * @param fp
     *            Customizable copyer
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static void deepCopyFieldStateWithInit(
            @Nullable Object initObj,
            @NotNull Object destObj,
            @NotNull Object defaultObj,
            @NotNull FieldFilter ff,
            @NotNull FieldProcessor2 fp) throws IllegalArgumentException, IllegalAccessException {

        if (!(destObj != null && defaultObj != null && ff != null && fp != null)) {
            throw new IllegalArgumentException("Target and source FieldFilter and FieldProcessor2 must not be null");
        }

        // Check if the destObj is compatible with the source object
        Class<?> destObjClass = destObj.getClass(), sourceClass = defaultObj.getClass();
        if (!isCompatibleType(destObj.getClass(), defaultObj.getClass())) {
            throw new IllegalArgumentException(
                    format("Incompatible the objects, destObj class: %s, source class: %s", destObjClass, sourceClass));
        }

        Class<?> destObjCls = destObj.getClass(); // [MARK0]
        do {
            doDeepCopyFieldsWithInit(destObjCls, initObj, destObj, defaultObj, ff, fp);
        } while ((destObjCls = destObjCls.getSuperclass()) != Object.class);
    }

    /**
     * Calls the given callback on all fields of the destObj class, recursively
     * running the class hierarchy up to copy all declared fields.</br>
     * Note: that it does not contain fields defined by the parent or super
     * class. At the same time, the destObj and the source object must be
     * compatible.</br>
     * Note: Attribute fields of parent and superclass are not included
     * 
     * @param currentTargetClass
     *            The level of the class currently copied to (upward recursion)
     * @param destObj
     *            The destObj object to copy to
     * @param defaultObj
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
            @Nullable Object initObj,
            @NotNull Object destObj,
            @NotNull Object defaultObj,
            @NotNull FieldFilter ff,
            @NotNull FieldProcessor2 fp) throws IllegalArgumentException, IllegalAccessException {

        if (isNull(currentTargetClass) || isNull(ff) || isNull(fp)) {
            throw new IllegalArgumentException(
                    "Hierarchy current destObj class or source FieldFilter and FieldProcessor2 can't null");
        }
        // Skip the current level copy.
        if (isNull(defaultObj) || isNull(destObj)) {
            return;
        }
        // Check is only required when the initial object is not empty.
        // @formatter:off
      //  if (nonNull(initObj) && initObj.getClass() != destObj.getClass()) {
        // @formatter:on
        if (nonNull(initObj) && !destObj.getClass().isAssignableFrom(initObj.getClass())) {
            throw new IllegalArgumentException(
                    format("The initial destObj object must be of the exact same type as the destObj object. %s != %s",
                            initObj.getClass(), destObj.getClass()));
        }

        // Recursive traversal matching and processing
        Class<?> sourceClass = defaultObj.getClass();
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
            Object initObjPropertyValue = nonNull(initObj) ? tf.get(initObj) : null;
            Object destObjPropertyValue = tf.get(destObj); // See:[MARK0]
            Object defaultPropertyValue = null;
            Field sf = findField(sourceClass, tf.getName());
            if (nonNull(sf)) {
                makeAccessible(sf);
                defaultPropertyValue = sf.get(defaultObj);
            }

            // Base type or collection type or enum?
            if (isSimpleType(tf.getType()) || isSimpleCollectionType(tf.getType()) || tf.getType().isEnum()) {
                // [MARK2] Filter matching property
                if (nonNull(fp) && ff.matches(tf)) {
                    fp.doProcess(initObj, destObj, tf, sf, defaultPropertyValue);
                }
            } else {
                doDeepCopyFieldsWithInit(tf.getType(), initObjPropertyValue, destObjPropertyValue, defaultPropertyValue, ff, fp);
            }
        }
    }

    public static interface FieldProcessor2 {
        void doProcess(
                @Nullable Object initObj,
                @NotNull Object destObj,
                @NotNull Field tf,
                @NotNull Field sf,
                @Nullable Object defaultPropertyValue) throws IllegalArgumentException, IllegalAccessException;
    }
}

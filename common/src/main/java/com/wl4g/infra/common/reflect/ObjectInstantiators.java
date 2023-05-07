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

import static java.lang.String.format;
import static java.lang.Thread.currentThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * {@link ObjectInstantiators}
 *
 * @author James Wong <jameswong1376@gmail.com>
 * @version v1.0 2020-08-24
 * @since
 */
public abstract class ObjectInstantiators {

    /**
     * New create instance by object class.
     * 
     * @param objectClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> objectClass) {
        if (!Objects.isNull(objenesis)) {
            try {
                return (T) objenesisStdNewInstanceMethod.invoke(objenesis, new Object[] { objectClass });
            } catch (Exception e) {
                throw new Error(format("Unexpected reflection exception of %s: %s", e.getClass().getName(), e.getMessage()), e);
            }
        }
        try {
            return (T) objectClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new Error(format("Instantiate class without access for %s", objectClass), e);
        } catch (InstantiationException e) {
            throw new Error(format("Cannot instantiate class without default constructor for %s", objectClass), e);
        }
    }

    // Spring packaging compatible object creator.
    final private static String OBJENSIS_CLASS = "org.springframework.objenesis.ObjenesisStd";
    final private static Object objenesis;
    final private static Method objenesisStdNewInstanceMethod;

    static {
        Object _objenesis = null;
        Method _objenesisStdNewInstanceMethod = null;
        try {
            Class<?> objenesisClass = Class.forName(OBJENSIS_CLASS, false, currentThread().getContextClassLoader());
            if (!Objects.isNull(objenesisClass)) {
                _objenesisStdNewInstanceMethod = objenesisClass.getMethod("newInstance", Class.class);
                // Objenesis object.
                for (Constructor<?> c : objenesisClass.getConstructors()) {
                    Class<?>[] paramClasses = c.getParameterTypes();
                    if (paramClasses != null && paramClasses.length == 1 && boolean.class.isAssignableFrom(paramClasses[0])) {
                        _objenesis = c.newInstance(new Object[] { true });
                        break;
                    }
                }
            }
        } catch (ClassNotFoundException e) { // Ignore
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        objenesis = _objenesis;
        objenesisStdNewInstanceMethod = _objenesisStdNewInstanceMethod;
    }

}
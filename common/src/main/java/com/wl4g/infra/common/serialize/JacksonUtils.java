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
package com.wl4g.infra.common.serialize;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wl4g.infra.common.reflect.ResolvableType;
import com.wl4g.infra.common.reflect.TypeUtils2;

/**
 * JACKSON utility tools.
 * 
 * @author Wangl.sir <James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2019年05月22日
 * @since
 */
@SuppressWarnings("deprecation")
public abstract class JacksonUtils {

    /**
     * Object to JSON strings.
     * 
     * @param object
     * @return
     */
    public static String toJSONString(@Nullable Object object) {
        if (isNull(object)) {
            return null;
        }
        try {
            return defaultObjectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse object from JSON strings.
     * 
     * @param content
     * @param clazz
     * @return
     */
    public static <T> T parseJSON(@Nullable String content, @NotNull Class<T> clazz) {
        notNullOf(clazz, "clazz");
        if (isNull(content)) {
            return null;
        }
        try {
            return defaultObjectMapper.readValue(content, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse object from JSON {@link InputStream}.
     * 
     * @param src
     * @param clazz
     * @return
     */
    public static <T> T parseJSON(@Nullable InputStream src, @NotNull Class<T> clazz) {
        notNullOf(clazz, "clazz");
        if (isNull(src)) {
            return null;
        }
        try {
            return defaultObjectMapper.readValue(src, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse object from JSON {@link File}.
     * 
     * @param content
     * @param valueTypeRef
     * @return
     */
    public static <T> T parseJSON(@Nullable File src, @NotNull Class<T> clazz) {
        notNullOf(clazz, "clazz");
        if (isNull(src)) {
            return null;
        }
        try {
            return defaultObjectMapper.readValue(src, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse object from JSON {@link File}.
     * 
     * @param content
     * @param valueTypeRef
     * @return
     */
    public static <T> T parseJSON(@Nullable File src, @NotNull TypeReference<T> valueTypeRef) {
        notNullOf(valueTypeRef, "valueTypeRef");
        if (isNull(src)) {
            return null;
        }
        try {
            return defaultObjectMapper.readValue(src, valueTypeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse object from JSON {@link InputStream}.
     * 
     * @param content
     * @param valueTypeRef
     * @return
     */
    public static <T> T parseJSON(@Nullable InputStream src, @NotNull TypeReference<T> valueTypeRef) {
        notNullOf(valueTypeRef, "valueTypeRef");
        if (isNull(src)) {
            return null;
        }
        try {
            return defaultObjectMapper.readValue(src, valueTypeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param content
     * @param valueTypeRef
     * @return
     */
    public static <T> T parseJSON(@Nullable String content, @NotNull TypeReference<T> valueTypeRef) {
        notNullOf(valueTypeRef, "valueTypeRef");
        if (isNull(content)) {
            return null;
        }
        try {
            return defaultObjectMapper.readValue(content, valueTypeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse array parameterized map string from JSON strings.
     * 
     * @param content
     * @param valueTypeRef
     * @return
     */
    public static List<String> parseArrayString(@Nullable String content) {
        if (isNull(content)) {
            return null;
        }
        try {
            return defaultObjectMapper.readValue(content, listStringTypeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse array parameterized map string from JSON strings.
     * 
     * @param content
     * @param valueTypeRef
     * @return
     */
    public static List<Map<String, String>> parseArrayMapString(@Nullable String content) {
        if (isNull(content)) {
            return null;
        }
        try {
            return defaultObjectMapper.readValue(content, listMapStringTypeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse array parameterized map object from JSON strings.
     * 
     * @param content
     * @param valueTypeRef
     * @return
     */
    public static List<Map<String, Object>> parseArrayMapObject(@Nullable String content) {
        if (isNull(content)) {
            return null;
        }
        try {
            return defaultObjectMapper.readValue(content, listMapObjectTypeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse object to {@link JsonNode}.
     * 
     * @param object
     * @param extractPathExpr
     * @return
     */
    public static JsonNode parseJsonNode(@Nullable String content, @Nullable String extractPathExpr) {
        if (isNull(content)) {
            return null;
        }
        try {
            return defaultObjectMapper.readTree(content).requiredAt(extractPathExpr);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert value to target type.</br>
     * 
     * @see com.fasterxml.jackson.databind.ObjectMapper#convertValue(Object,
     *      Class)
     * @param <T>
     * @param bean
     * @param toType
     * @return
     */
    public static <T> T convertBean(@Nullable Object bean, @NotNull Class<T> toType) {
        notNullOf(toType, "toType");
        if (isNull(bean)) {
            return null;
        }
        return defaultObjectMapper.convertValue(bean, toType);
    }

    /**
     * Convert value to reference type.</br>
     * 
     * @see com.fasterxml.jackson.databind.ObjectMapper#convertValue(Object,
     *      TypeReference)
     * @param <T>
     * @param bean
     * @param typeRef
     * @return
     */
    public static <T> T convertBean(@Nullable Object bean, @NotNull TypeReference<T> valueTypeRef) {
        notNullOf(valueTypeRef, "valueTypeRef");
        if (isNull(bean)) {
            return null;
        }
        return defaultObjectMapper.convertValue(bean, valueTypeRef);
    }

    /**
     * Convert value to Java type.</br>
     * 
     * @see com.fasterxml.jackson.databind.ObjectMapper#convertValue(Object,
     *      JavaType)
     * @param <T>
     * @param bean
     * @param toJavaType
     * @return
     */
    public static <T> T convertBean(@Nullable Object bean, @NotNull JavaType toJavaType) {
        notNullOf(toJavaType, "toJavaType");
        if (isNull(bean)) {
            return null;
        }
        return defaultObjectMapper.convertValue(bean, toJavaType);
    }

    /**
     * Deep cloning object with JSON serial-deserial.
     * 
     * @param obj
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T deepClone(@Nullable T obj) {
        if (isNull(obj)) {
            return null;
        }

        ResolvableType resolver = ResolvableType.forClass(obj.getClass());
        if (Collection.class.isAssignableFrom(obj.getClass())) {
            ObjectReader reader = null;
            ResolvableType[] generics = resolver.getGenerics();
            if (!isNull(generics) && generics.length == 1) {
                Class<?> clazz = generics[0].getRawClass();
                if (!isNull(clazz)) {
                    reader = defaultObjectMapper.readerForListOf(clazz);
                }
            }
            if (isNull(reader)) { // Fallback
                Collection collect = (Collection) obj;
                if (collect.isEmpty()) {
                    return obj;
                }
                Iterator<Object> it = collect.iterator();
                if (it.hasNext()) {
                    reader = defaultObjectMapper.readerForListOf(it.next().getClass());
                }
            }
            try {
                return reader.readValue(toJSONString(obj));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        } else if (Map.class.isAssignableFrom(obj.getClass())) {
            Map map = (Map) obj;
            Map cloneMap = new LinkedHashMap<>(map.size());
            map.forEach((key, val) -> cloneMap.put(deepClone(key), deepClone(val)));
            return (T) cloneMap;
        } else if (TypeUtils2.isSimpleType(obj.getClass())) { // Simple Class
            return obj;
        }

        // Custom bean(obj field after recursion)
        return (T) parseJSON(toJSONString(obj), obj.getClass());
    }

    /**
     * Gets default {@link ObjectMapper}
     * 
     * @return
     */
    @NotNull
    public static final ObjectMapper getDefaultObjectMapper() {
        return defaultObjectMapper;
    }

    /**
     * Default {@link ObjectMapper} instance.
     */
    private static final ObjectMapper defaultObjectMapper = new ObjectMapper();

    private static final TypeReference<List<String>> listStringTypeRef = new TypeReference<List<String>>() {
    };
    private static final TypeReference<List<Map<String, String>>> listMapStringTypeRef = new TypeReference<List<Map<String, String>>>() {
    };
    private static final TypeReference<List<Map<String, Object>>> listMapObjectTypeRef = new TypeReference<List<Map<String, Object>>>() {
    };

    static {
        getDefaultObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        getDefaultObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        getDefaultObjectMapper().registerModule(new SimpleModule());
        getDefaultObjectMapper().registerModule(new JavaTimeModule());
        getDefaultObjectMapper().registerModule(new JSR310Module());
    }

}
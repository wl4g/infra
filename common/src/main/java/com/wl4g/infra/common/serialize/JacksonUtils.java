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
package com.wl4g.infra.common.serialize;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Sets;
import com.wl4g.infra.common.reflect.ResolvableType;
import com.wl4g.infra.common.reflect.TypeUtils2;

import lombok.Getter;

/**
 * JACKSON utility tools.
 * 
 * @author Wangl.sir James Wong <jameswong1376@gmail.com>>
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
        return toJSONString(object, false);
    }

    /**
     * Object to JSON strings.
     * 
     * @param object
     * @param isPretty
     * @return
     */
    public static String toJSONString(@Nullable Object object, boolean isPretty) {
        return toJSONString(object, isPretty, null, DEFAULT_IGNORE_PROPERTIES);
    }

    /**
     * Object to JSON strings.
     * 
     * @param object
     * @param isPretty
     * @param ignoreProperties
     * @return
     */
    public static String toJSONString(@Nullable Object object, @Nullable String... ignoreProperties) {
        return toJSONString(object, false, null, ignoreProperties);
    }

    /**
     * Object to JSON strings.
     * 
     * @param object
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static String toJSONString(
            @Nullable Object object,
            @Nullable Map<String, String> transformProperties,
            @Nullable String... ignoreProperties) {
        return toJSONString(object, false, transformProperties, ignoreProperties);
    }

    /**
     * Object to JSON strings.
     * 
     * @param object
     * @param isPretty
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static String toJSONString(
            @Nullable Object object,
            boolean isPretty,
            @Nullable Map<String, String> transformProperties,
            @Nullable String... ignoreProperties) {
        if (isNull(object)) {
            return null;
        }
        try {
            ObjectWriter writer = isPretty ? DEFAULT_OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    : DEFAULT_OBJECT_MAPPER.writer();
            if (nonNull(ignoreProperties) && ignoreProperties.length > 0) {
                final FilterProvider provider = new SimpleFilterProvider().addFilter(ExcludePropertyFilter.FILTER_ID,
                        new ExcludePropertyFilter(transformProperties, Sets.newHashSet(ignoreProperties)));
                writer = DEFAULT_OBJECT_MAPPER.writer(provider);
            }
            return writer.writeValueAsString(object);
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
        if (isBlank(content)) {
            return null;
        }
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(content, clazz);
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
            return DEFAULT_OBJECT_MAPPER.readValue(src, clazz);
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
            return DEFAULT_OBJECT_MAPPER.readValue(src, clazz);
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
            return DEFAULT_OBJECT_MAPPER.readValue(src, valueTypeRef);
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
            return DEFAULT_OBJECT_MAPPER.readValue(src, valueTypeRef);
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
        if (isBlank(content)) {
            return null;
        }
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(content, valueTypeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse {@link TreeNode} to object.
     * 
     * @param object
     * @param extractPathExpr
     * @return
     */
    public static <T> T parseFromNode(@Nullable TreeNode node, @NotBlank String extractPathExpr, @NotNull Class<T> clazz) {
        hasTextOf(extractPathExpr, "extractPathExpr");
        if (isNull(node)) {
            return null;
        }
        try {
            final TreeNode objNode = node.at(extractPathExpr);
            if (objNode.size() > 0) {
                return DEFAULT_OBJECT_MAPPER.treeToValue(objNode, clazz);
            }
            return null;
        } catch (JsonProcessingException e) {
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
    public static JsonNode parseToNode(@Nullable String content) {
        return parseToNode(content, null);
    }

    /**
     * Parse object to {@link JsonNode}.
     * 
     * @param object
     * @param atPathExpr
     * @return
     */
    public static JsonNode parseToNode(@Nullable String content, @Nullable String atPathExpr) {
        if (isBlank(content)) {
            return null;
        }
        try {
            return DEFAULT_OBJECT_MAPPER.readTree(content).requiredAt(atPathExpr);
        } catch (JsonProcessingException e) {
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
            return DEFAULT_OBJECT_MAPPER.readValue(content, listStringTypeRef);
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
            return DEFAULT_OBJECT_MAPPER.readValue(content, listMapStringTypeRef);
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
            return DEFAULT_OBJECT_MAPPER.readValue(content, listMapObjectTypeRef);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Parse map parameterized object from JSON strings.
     * 
     * @param content
     * @param valueTypeRef
     * @return
     */
    public static Map<String, Object> parseMapObject(@Nullable String content) {
        if (isNull(content)) {
            return null;
        }
        try {
            return DEFAULT_OBJECT_MAPPER.readValue(content, mapObjectTypeRef);
        } catch (Exception e) {
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
        return DEFAULT_OBJECT_MAPPER.convertValue(bean, toType);
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
        return DEFAULT_OBJECT_MAPPER.convertValue(bean, valueTypeRef);
    }

    /**
     * The convert value to Java type.</br>
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
        return DEFAULT_OBJECT_MAPPER.convertValue(bean, toJavaType);
    }

    /**
     * The deep cloning object with JSON serial-deserial.
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
                    reader = DEFAULT_OBJECT_MAPPER.readerForListOf(clazz);
                }
            }
            if (isNull(reader)) { // Fallback
                Collection collect = (Collection) obj;
                if (collect.isEmpty()) {
                    return obj;
                }
                Iterator<Object> it = collect.iterator();
                if (it.hasNext()) {
                    reader = DEFAULT_OBJECT_MAPPER.readerForListOf(it.next().getClass());
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
     * The override merge object to target.
     * 
     * @param <T>
     * @param obj
     * @param overrideObj
     * @return
     */
    public static <T> T mergeWithOverride(@Nullable T obj, @Nullable T overrideObj) {
        if (isNull(obj) && nonNull(overrideObj)) {
            return overrideObj;
        }
        if (isNull(overrideObj) && nonNull(obj)) {
            return obj;
        }
        if (isNull(obj) && isNull(overrideObj)) {
            return null;
        }
        try {
            ObjectReader reader = DEFAULT_OBJECT_MAPPER.readerForUpdating(obj);
            return reader.readValue(toJSONString(overrideObj));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets default {@link ObjectMapper}
     * 
     * @return
     */
    @NotNull
    public static final ObjectMapper getDefaultObjectMapper() {
        return DEFAULT_OBJECT_MAPPER;
    }

    @Getter
    static class ExcludePropertyFilter extends SimpleBeanPropertyFilter {
        static final String FILTER_ID = ExcludePropertyFilter.class.getSimpleName();

        final @Nullable Map<String, String> transformProperties;
        final @Nullable Set<String> excludeProperties;

        public ExcludePropertyFilter(Map<String, String> transformProperties, Set<String> excludeProperties) {
            this.transformProperties = transformProperties;
            this.excludeProperties = excludeProperties;
        }

        @Override
        protected boolean include(BeanPropertyWriter writer) {
            return !excludeProperties.contains(writer.getName());
        }

        @Override
        protected boolean include(PropertyWriter writer) {
            return !excludeProperties.contains(writer.getName());
        }
    }

    static class ExcludePropertiesSerializerModifier extends BeanSerializerModifier {

        // In this method you can add, remove or replace any of passed
        // properties.
        @Override
        public List<BeanPropertyWriter> changeProperties(
                SerializationConfig config,
                BeanDescription beanDesc,
                List<BeanPropertyWriter> beanProperties) {
            final FilterProvider provider = config.getFilterProvider();
            if (nonNull(provider)) {
                // Ignore for exclude properties.
                final ExcludePropertyFilter filter = notNullOf(provider.findPropertyFilter(ExcludePropertyFilter.FILTER_ID, null),
                        "filter");
                final Map<String, String> transformProperties = safeMap(filter.getTransformProperties());
                return safeList(beanProperties).stream()
                        .filter(bp -> !filter.getExcludeProperties().contains(bp.getName()))
                        .map(bp -> bp.rename(new NameTransformer() {
                            @Override
                            public String transform(String name) {
                                return transformProperties.getOrDefault(name, name);
                            }

                            @Override
                            public String reverse(String transformed) {
                                return transformed;
                            }
                        }))
                        .collect(toList());
            }
            return beanProperties;
        }
    }

    /**
     * Default {@link ObjectMapper} instance.
     */
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();
    private static final String[] DEFAULT_IGNORE_PROPERTIES = new String[0];

    private static final TypeReference<List<String>> listStringTypeRef = new TypeReference<List<String>>() {
    };
    private static final TypeReference<List<Map<String, String>>> listMapStringTypeRef = new TypeReference<List<Map<String, String>>>() {
    };
    private static final TypeReference<List<Map<String, Object>>> listMapObjectTypeRef = new TypeReference<List<Map<String, Object>>>() {
    };
    private static final TypeReference<Map<String, Object>> mapObjectTypeRef = new TypeReference<Map<String, Object>>() {
    };

    static {
        getDefaultObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        getDefaultObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        getDefaultObjectMapper().registerModule(new JavaTimeModule());
        getDefaultObjectMapper().registerModule(new JSR310Module());
        getDefaultObjectMapper().registerModule(new SimpleModule() {
            private static final long serialVersionUID = 1L;

            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                context.addBeanSerializerModifier(new ExcludePropertiesSerializerModifier());
            }
        });
    }

}
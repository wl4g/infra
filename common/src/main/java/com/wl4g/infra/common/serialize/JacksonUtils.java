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

import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToSet;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.filter.TokenFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
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
import com.wl4g.infra.common.reflect.ResolvableType;
import com.wl4g.infra.common.reflect.TypeUtils2;

import lombok.Getter;

/**
 * JACKSON utility tools.
 * 
 * @author James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2019年05月22日
 * @since
 */
@SuppressWarnings("deprecation")
public abstract class JacksonUtils {

    //
    // --- Serialize Function. ---
    //

    /**
     * Object to JSON strings.
     * 
     * @param object
     * @return
     */
    public static String toJSONString(final @Nullable Object object) {
        return toJSONString(object, false);
    }

    /**
     * Object to JSON strings.
     * 
     * @param object
     * @param isPretty
     * @return
     */
    public static String toJSONString(final @Nullable Object object, final boolean isPretty) {
        return toJSONString(DEFAULT_OBJECT_MAPPER, object, isPretty, null, DEFAULT_IGNORE_PROPERTIES);
    }

    /**
     * Object to JSON strings.
     * 
     * @param mapper
     *            When using a custom modifier, you should use an independent
     *            objectMapper, because the same objectmapper instance will
     *            cache the serializer of the target bean, which may cause the
     *            modifier to fail, see source code:
     *            {@link com.fasterxml.jackson.databind.SerializerProvider#findTypedValueSerializer(java.lang.Class,boolean,com.fasterxml.jackson.databind.BeanProperty)}
     *            {@link com.fasterxml.jackson.databind.SerializerProvider#_knownSerializers}
     *            {@link com.fasterxml.jackson.databind.ser.impl.ReadOnlyClassToSerializerMap#typedValueSerializer(com.fasterxml.jackson.databind.JavaType)}
     * @param object
     * @param isPretty
     * @param ignoreProperties
     * @return
     */
    public static String toJSONString(
            final @NotNull ObjectMapper mapper,
            final @Nullable Object object,
            final @Nullable String... ignoreProperties) {
        return toJSONString(mapper, object, false, null, ignoreProperties);
    }

    /**
     * Object to JSON strings.
     * 
     * @param mapper
     *            When using a custom modifier, you should use an independent
     *            objectMapper, because the same objectmapper instance will
     *            cache the serializer of the target bean, which may cause the
     *            modifier to fail, see source code:
     *            {@link com.fasterxml.jackson.databind.SerializerProvider#findTypedValueSerializer(java.lang.Class,boolean,com.fasterxml.jackson.databind.BeanProperty)}
     *            {@link com.fasterxml.jackson.databind.SerializerProvider#_knownSerializers}
     *            {@link com.fasterxml.jackson.databind.ser.impl.ReadOnlyClassToSerializerMap#typedValueSerializer(com.fasterxml.jackson.databind.JavaType)}
     * @param object
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static String toJSONString(
            final @NotNull ObjectMapper mapper,
            final @Nullable Object object,
            final @Nullable Map<String, String> transformProperties,
            final @Nullable String... ignoreProperties) {
        return toJSONString(mapper, object, false, transformProperties, ignoreProperties);
    }

    /**
     * Object to JSON strings. </br>
     * see: https://www.baeldung.com/jackson-ignore-properties-on-serialization
     * 
     * @param mapper
     *            When using a custom modifier, you should use an independent
     *            objectMapper, because the same objectmapper instance will
     *            cache the serializer of the target bean, which may cause the
     *            modifier to fail, see source code:
     *            {@link com.fasterxml.jackson.databind.SerializerProvider#findTypedValueSerializer(java.lang.Class,boolean,com.fasterxml.jackson.databind.BeanProperty)}
     *            {@link com.fasterxml.jackson.databind.SerializerProvider#_knownSerializers}
     *            {@link com.fasterxml.jackson.databind.ser.impl.ReadOnlyClassToSerializerMap#typedValueSerializer(com.fasterxml.jackson.databind.JavaType)}
     * @param object
     * @param isPretty
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static String toJSONString(
            final @NotNull ObjectMapper mapper,
            final @Nullable Object object,
            final boolean isPretty,
            final @Nullable Map<String, String> transformProperties,
            final @Nullable String... ignoreProperties) {
        return _toJSONString(mapper, null, object, isPretty, transformProperties, ignoreProperties);
    }

    /**
     * Object to JSON strings.
     * 
     * @param view
     * @param isPretty
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static String toJSONString(
            final @Nullable Class<?> view,
            final @Nullable Object object,
            final boolean isPretty,
            final @Nullable Map<String, String> transformProperties,
            final @Nullable String... ignoreProperties) {
        return _toJSONString(DEFAULT_OBJECT_MAPPER, view, object, isPretty, transformProperties, ignoreProperties);
    }

    /**
     * Object to JSON strings. </br>
     * see: https://www.baeldung.com/jackson-ignore-properties-on-serialization
     * 
     * @param mapper
     *            When using a custom modifier, you should use an independent
     *            objectMapper, because the same objectmapper instance will
     *            cache the serializer of the target bean, which may cause the
     *            modifier to fail, see source code:
     *            {@link com.fasterxml.jackson.databind.SerializerProvider#findTypedValueSerializer(java.lang.Class,boolean,com.fasterxml.jackson.databind.BeanProperty)}
     *            {@link com.fasterxml.jackson.databind.SerializerProvider#_knownSerializers}
     *            {@link com.fasterxml.jackson.databind.ser.impl.ReadOnlyClassToSerializerMap#typedValueSerializer(com.fasterxml.jackson.databind.JavaType)}
     * @param object
     * @param view
     * @param isPretty
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static String _toJSONString(
            final @NotNull ObjectMapper mapper,
            final @Nullable Class<?> view,
            final @Nullable Object object,
            final boolean isPretty,
            final @Nullable Map<String, String> transformProperties,
            final @Nullable String... ignoreProperties) {
        notNullOf(mapper, "mapper");
        if (isNull(object)) {
            return null;
        }
        try {
            final SerializationConfig config = mapper.getSerializationConfig()
                    .withFilters(new SimpleFilterProvider().addFilter(TransformPropertyFilter.FILTER_ID,
                            new TransformPropertyFilter(safeMap(transformProperties), safeArrayToSet(ignoreProperties))))
                    .withView(view);

            final PrettyPrinter pp = isPretty ? config.getDefaultPrettyPrinter() : null;
            return new CustomObjectWriter(mapper, config, null, pp).writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //
    // --- Deserialize with Collection Function. ---
    //

    /**
     * Parse array parameterized map string from JSON strings.
     * 
     * @param json
     * @param valueTypeRef
     * @return
     */
    public static List<String> parseArrayString(final @Nullable String jsonString) {
        return parseArrayString(null, jsonString);
    }

    /**
     * Parse array parameterized map string from JSON strings.
     * 
     * @param view
     * @param json
     * @param valueTypeRef
     * @return
     */
    public static List<String> parseArrayString(final @Nullable Class<?> view, final @Nullable String jsonString) {
        return parseJSON(DEFAULT_OBJECT_MAPPER, view, jsonString, LIST_STRING_TYPE_REF, null);
    }

    /**
     * Parse array parameterized map string from JSON strings.
     * 
     * @param json
     * @param valueTypeRef
     * @return
     */
    public static List<Map<String, String>> parseArrayMapString(@Nullable String jsonString) {
        return parseArrayMapString(null, jsonString);
    }

    /**
     * Parse array parameterized map string from JSON strings.
     * 
     * @param view
     * @param json
     * @param valueTypeRef
     * @return
     */
    public static List<Map<String, String>> parseArrayMapString(final @Nullable Class<?> view, @Nullable String jsonString) {
        return parseJSON(DEFAULT_OBJECT_MAPPER, view, jsonString, LIST_MAP_STRING_TYPE_REF, null);
    }

    /**
     * Parse array parameterized map object from JSON strings.
     * 
     * @param view
     * @param json
     * @param valueTypeRef
     * @return
     */
    public static List<Map<String, Object>> parseArrayMapObject(final @Nullable Class<?> view, @Nullable String jsonString) {
        return parseJSON(DEFAULT_OBJECT_MAPPER, view, jsonString, LIST_MAP_OBJECT_TYPE_REF, null);
    }

    /**
     * Parse map parameterized object from JSON strings.
     * 
     * @param json
     * @param valueTypeRef
     * @return
     */
    public static Map<String, Object> parseMapObject(@Nullable String jsonString) {
        return parseMapObject(null, jsonString);
    }

    /**
     * Parse map parameterized object from JSON strings.
     * 
     * @param view
     * @param json
     * @param valueTypeRef
     * @return
     */
    public static Map<String, Object> parseMapObject(final @Nullable Class<?> view, @Nullable String jsonString) {
        return parseJSON(DEFAULT_OBJECT_MAPPER, view, jsonString, MAP_OBJECT_TYPE_REF, null);
    }

    //
    // --- Deserialize with JavaBean Function. ---
    //

    /**
     * Parse object from JSON strings.
     * 
     * @param json
     * @param valueType
     * @return
     */
    public static <T> T parseJSON(final @Nullable String jsonString, final @NotNull Class<T> valueType) {
        return parseJSON(DEFAULT_OBJECT_MAPPER, jsonString, valueType, null);
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param view
     * @param jsonFile
     * @param valueType
     * @return
     */
    public static <T> T parseJSON(
            final @Nullable Class<?> view,
            final @Nullable File jsonFile,
            final @NotNull Class<T> valueType) {
        final ObjectMapper mapper = DEFAULT_OBJECT_MAPPER;
        return _parseJSON(mapper, null, () -> {
            try {
                return isNull(jsonFile) ? null : mapper.getFactory().createParser(jsonFile);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, () -> mapper.getTypeFactory().constructType(valueType), null);
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param view
     * @param jsonInputStream
     * @param valueType
     * @return
     */
    public static <T> T parseJSON(
            final @Nullable Class<?> view,
            final @Nullable InputStream jsonInputStream,
            final @NotNull Class<T> valueType) {
        final ObjectMapper mapper = DEFAULT_OBJECT_MAPPER;
        return _parseJSON(mapper, null, () -> {
            try {
                return isNull(jsonInputStream) ? null : mapper.getFactory().createParser(jsonInputStream);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, () -> mapper.getTypeFactory().constructType(valueType), null);
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param view
     * @param json
     * @param valueType
     * @return
     */
    public static <T> T parseJSON(
            final @Nullable Class<?> view,
            final @Nullable String jsonString,
            final @NotNull Class<T> valueType) {
        final ObjectMapper mapper = DEFAULT_OBJECT_MAPPER;
        return _parseJSON(mapper, null, () -> {
            try {
                return isNull(jsonString) ? null : mapper.getFactory().createParser(jsonString);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, () -> mapper.getTypeFactory().constructType(valueType), null);
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param mapper
     * @param view
     * @param json
     * @param valueType
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static <T> T parseJSON(
            final @NotNull ObjectMapper mapper,
            final @Nullable String jsonString,
            final @NotNull Class<T> valueType,
            final @Nullable Map<String, String> transformProperties,
            final @Nullable String... ignoreProperties) {
        return _parseJSON(mapper, null, () -> {
            try {
                return mapper.getFactory().createParser(jsonString);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, () -> mapper.getTypeFactory().constructType(valueType), transformProperties, ignoreProperties);
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param json
     * @param valueTypeRef
     * @return
     */
    public static <T> T parseJSON(final @Nullable String jsonString, final @NotNull TypeReference<T> valueTypeRef) {
        return parseJSON(DEFAULT_OBJECT_MAPPER, null, jsonString, valueTypeRef, null);
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param view
     * @param json
     * @param valueTypeRef
     * @return
     */
    public static <T> T parseJSON(
            final @Nullable Class<?> view,
            final @Nullable String jsonString,
            final @NotNull TypeReference<T> valueTypeRef) {
        return parseJSON(DEFAULT_OBJECT_MAPPER, view, jsonString, valueTypeRef, null);
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param mapper
     * @param json
     * @param valueTypeRef
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static <T> T parseJSON(
            final @NotNull ObjectMapper mapper,
            final @Nullable String jsonString,
            final @NotNull TypeReference<T> valueTypeRef,
            final @Nullable Map<String, String> transformProperties,
            final @Nullable String... ignoreProperties) {
        return parseJSON(mapper, null, jsonString, valueTypeRef, transformProperties, ignoreProperties);
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param mapper
     * @param view
     * @param json
     * @param valueTypeRef
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static <T> T parseJSON(
            final @NotNull ObjectMapper mapper,
            final @Nullable Class<?> view,
            final @Nullable String jsonString,
            final @NotNull TypeReference<T> valueTypeRef,
            final @Nullable Map<String, String> transformProperties,
            final @Nullable String... ignoreProperties) {
        return _parseJSON(mapper, view, () -> {
            try {
                return isNull(jsonString) ? null : mapper.getFactory().createParser(jsonString);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }, () -> mapper.getTypeFactory().constructType(valueTypeRef), transformProperties, ignoreProperties);
    }

    /**
     * Parse object string from JSON strings.
     * 
     * @param mapper
     * @param view
     * @param jsonParserSupplier
     * @param valueJavaTypeSupplier
     * @param transformProperties
     * @param ignoreProperties
     * @return
     */
    public static <T> T _parseJSON(
            final @NotNull ObjectMapper mapper,
            final @Nullable Class<?> view,
            final @NotNull Supplier<JsonParser> jsonParserSupplier,
            final @NotNull Supplier<JavaType> valueJavaTypeSupplier,
            final @Nullable Map<String, String> transformProperties,
            final @Nullable String... ignoreProperties) {
        notNullOf(mapper, "mapper");
        notNullOf(jsonParserSupplier, "jsonParserSupplier");
        notNullOf(valueJavaTypeSupplier, "valueJavaTypeSupplier");
        final JsonParser jsonParser = jsonParserSupplier.get();
        if (isNull(jsonParser)) {
            return null;
        }
        try {
            final DeserializationConfig config = mapper.getDeserializationConfig()
                    .with(new TransformContextAttributes(safeMap(transformProperties), safeArrayToSet(ignoreProperties)));
            return new CustomObjectReader(mapper, config).withView(view).readValue(jsonParser, valueJavaTypeSupplier.get());
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    //
    // --- Deserialize with JsonNode Function. ---
    //

    /**
     * Parse {@link TreeNode} to object.
     * 
     * @param object
     * @param atPathExpr
     * @return
     */
    public static <T> T parseFromNode(@Nullable TreeNode node, @NotBlank String atPathExpr, @NotNull Class<T> valueType) {
        hasTextOf(atPathExpr, "atPathExpr");
        if (isNull(node)) {
            return null;
        }
        try {
            final TreeNode objNode = node.at(atPathExpr);
            if (objNode.size() > 0) {
                return DEFAULT_OBJECT_MAPPER.treeToValue(objNode, valueType);
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
     * @param atPathExpr
     * @return
     */
    public static JsonNode parseToNode(@Nullable String jsonString) {
        return parseToNode(jsonString, null);
    }

    /**
     * Parse object to {@link JsonNode}.
     * 
     * @param object
     * @param atPathExpr
     * @return
     */
    public static JsonNode parseToNode(@Nullable String jsonString, @Nullable String atPathExpr) {
        return parseToNode(DEFAULT_OBJECT_MAPPER, null, jsonString, atPathExpr);
    }

    /**
     * Parse object to {@link JsonNode}.
     * 
     * @param view
     * @param object
     * @param atPathExpr
     * @return
     */
    public static JsonNode parseToNode(
            final @Nullable Class<?> view,
            final @Nullable String jsonString,
            final @Nullable String atPathExpr) {
        return parseToNode(DEFAULT_OBJECT_MAPPER, view, jsonString, atPathExpr);
    }

    /**
     * Parse object to {@link JsonNode}.
     * 
     * @param mapper
     * @param view
     * @param jsonString
     * @param atPathExpr
     * @return
     */
    public static JsonNode parseToNode(
            final @NotNull ObjectMapper mapper,
            final @Nullable Class<?> view,
            final @Nullable String jsonString,
            final @Nullable String atPathExpr) {
        notNullOf(mapper, "mapper");
        if (isBlank(jsonString)) {
            return null;
        }
        try {
            return mapper.readerWithView(view).readTree(jsonString).requiredAt(atPathExpr);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //
    // --- Conversion Function. ---
    //

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
                Class<?> valueType = generics[0].getRawClass();
                if (!isNull(valueType)) {
                    reader = DEFAULT_OBJECT_MAPPER.readerForListOf(valueType);
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

    //
    // --- Helper Function. ---
    //

    @NotNull
    public static final ObjectMapper newDefaultObjectMapper() {
        return newDefaultObjectMapper(true);
    }

    /**
     * Create default {@link ObjectMapper}
     * 
     * @param enableExtension
     * @return
     */
    @NotNull
    public static final ObjectMapper newDefaultObjectMapper(final boolean enableExtension) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new JSR310Module());
        mapper.registerModule(new SimpleModule() {
            private static final long serialVersionUID = 1L;

            @Override
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                if (enableExtension) {
                    context.addBeanSerializerModifier(new TransformPropertiesSerializerModifier());
                    context.addBeanDeserializerModifier(new TransformPropertiesDeserializerModifier());
                }
            }
        });
        return mapper;
    }

    public static class CustomObjectWriter extends ObjectWriter {
        private static final long serialVersionUID = 1L;

        protected CustomObjectWriter(ObjectMapper mapper, SerializationConfig config, JavaType rootType, PrettyPrinter pp) {
            super(mapper, config, rootType, pp);
        }

        protected CustomObjectWriter(ObjectMapper mapper, SerializationConfig config) {
            super(mapper, config);
        }

        protected CustomObjectWriter(ObjectMapper mapper, SerializationConfig config, FormatSchema s) {
            super(mapper, config, s);
        }

        protected CustomObjectWriter(ObjectWriter base, SerializationConfig config, GeneratorSettings genSettings,
                Prefetch prefetch) {
            super(base, config, genSettings, prefetch);
        }

        protected CustomObjectWriter(ObjectWriter base, SerializationConfig config) {
            super(base, config);
        }

        protected CustomObjectWriter(ObjectWriter base, JsonFactory f) {
            super(base, f);
        }
    }

    public static class CustomObjectReader extends ObjectReader {
        private static final long serialVersionUID = 1L;

        protected CustomObjectReader(ObjectMapper mapper, DeserializationConfig config) {
            super(mapper, config);
        }

        protected CustomObjectReader(ObjectMapper mapper, DeserializationConfig config, JavaType valueType, Object valueToUpdate,
                FormatSchema schema, InjectableValues injectableValues) {
            super(mapper, config, valueType, valueToUpdate, schema, injectableValues);
        }

        protected CustomObjectReader(ObjectReader base, DeserializationConfig config, JavaType valueType,
                JsonDeserializer<Object> rootDeser, Object valueToUpdate, FormatSchema schema, InjectableValues injectableValues,
                DataFormatReaders dataFormatReaders) {
            super(base, config, valueType, rootDeser, valueToUpdate, schema, injectableValues, dataFormatReaders);
        }

        protected CustomObjectReader(ObjectReader base, DeserializationConfig config) {
            super(base, config);
        }

        protected CustomObjectReader(ObjectReader base, JsonFactory f) {
            super(base, f);
        }

        protected CustomObjectReader(ObjectReader base, TokenFilter filter) {
            super(base, filter);
        }
    }

    @Getter
    public static class TransformPropertyFilter extends SimpleBeanPropertyFilter {
        static final String FILTER_ID = TransformPropertyFilter.class.getSimpleName();

        final @NotNull Map<String, String> transformProperties;
        final @NotNull Set<String> excludeProperties;

        public TransformPropertyFilter(Map<String, String> transformProperties, Set<String> excludeProperties) {
            this.transformProperties = notNullOf(transformProperties, "transformProperties");
            this.excludeProperties = notNullOf(excludeProperties, "excludeProperties");
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

    public static class TransformPropertiesSerializerModifier extends BeanSerializerModifier {
        // In this method you can add, remove or replace any of passed
        // properties.
        @Override
        public List<BeanPropertyWriter> changeProperties(
                SerializationConfig config,
                BeanDescription beanDesc,
                List<BeanPropertyWriter> beanProperties) {

            final FilterProvider provider = config.getFilterProvider();
            if (nonNull(provider)) {
                // Filtering for ignore properties.
                final TransformPropertyFilter filter = notNullOf(
                        provider.findPropertyFilter(TransformPropertyFilter.FILTER_ID, null), "filter");

                // Transform properties.
                return safeList(beanProperties).stream()
                        .filter(bp -> !filter.getExcludeProperties().contains(bp.getName()))
                        .map(bp -> bp.rename(new NameTransformer() {
                            @Override
                            public String transform(String name) {
                                return filter.getTransformProperties().getOrDefault(name, name);
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

    @Getter
    public static class TransformContextAttributes extends ContextAttributes.Impl {
        private static final long serialVersionUID = 1L;
        static final String ATTRIBUTES_ID = TransformContextAttributes.class.getSimpleName();

        final @NotNull Map<String, String> transformProperties;
        final @NotNull Set<String> excludeProperties;

        public TransformContextAttributes(Map<String, String> transformProperties, Set<String> excludeProperties) {
            super(emptyMap()); // Nothing shared
            this.transformProperties = notNullOf(transformProperties, "transformProperties");
            this.excludeProperties = notNullOf(excludeProperties, "excludeProperties");
        }
    }

    public static class TransformBeanDeserializer extends BeanDeserializer {
        private static final long serialVersionUID = 1L;

        protected TransformBeanDeserializer(BeanDeserializerBase src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }
    }

    public static class TransformPropertiesDeserializerModifier extends BeanDeserializerModifier {

        @Override
        public JsonDeserializer<?> modifyDeserializer(
                DeserializationConfig config,
                BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
            if (deserializer instanceof BeanDeserializerBase) {
                final ContextAttributes attr = config.getAttributes();
                notNullOf(attr, "attributes");
                if (attr instanceof TransformContextAttributes) {
                    final TransformContextAttributes attributes = (TransformContextAttributes) attr;
                    // Notice: It needs to be reversed here, that is, from
                    // key->value to value->key, because the java bean property
                    // name is modified here, that is, the modification of the
                    // java bean property name to be consistent with the key in
                    // the json string will take effect.
                    final Map<String, String> reverseTransformProperties = attributes.getTransformProperties()
                            .entrySet()
                            .stream()
                            .collect(toMap(e -> e.getValue(), e -> e.getKey()));

                    // Transform properties.
                    return new TransformBeanDeserializer((BeanDeserializerBase) deserializer, new NameTransformer() {
                        @Override
                        public String transform(String name) {
                            final Object transformed = reverseTransformProperties.getOrDefault(name, name);
                            return (String) transformed;
                        }

                        @Override
                        public String reverse(String transformed) {
                            return transformed;
                        }
                    });
                }
            }
            return deserializer;
        }

        @Override
        public List<BeanPropertyDefinition> updateProperties(
                DeserializationConfig config,
                BeanDescription beanDesc,
                List<BeanPropertyDefinition> propDefs) {
            final TransformContextAttributes attributes = notNullOf(config.getAttributes(), "attributes");
            // Remove for ignore properties.
            final Iterator<BeanPropertyDefinition> it = safeList(propDefs).iterator();
            while (it.hasNext()) {
                BeanPropertyDefinition definition = it.next();
                if (attributes.getExcludeProperties().contains(definition.getName())) {
                    it.remove();
                }
            }
            return propDefs;
        }
    }

    /**
     * Default {@link ObjectMapper} instance.
     */
    public static final ObjectMapper DEFAULT_OBJECT_MAPPER = newDefaultObjectMapper();
    private static final String[] DEFAULT_IGNORE_PROPERTIES = new String[0];

    private static final TypeReference<List<String>> LIST_STRING_TYPE_REF = new TypeReference<List<String>>() {
    };
    private static final TypeReference<List<Map<String, String>>> LIST_MAP_STRING_TYPE_REF = new TypeReference<List<Map<String, String>>>() {
    };
    private static final TypeReference<List<Map<String, Object>>> LIST_MAP_OBJECT_TYPE_REF = new TypeReference<List<Map<String, Object>>>() {
    };
    private static final TypeReference<Map<String, Object>> MAP_OBJECT_TYPE_REF = new TypeReference<Map<String, Object>>() {
    };

}
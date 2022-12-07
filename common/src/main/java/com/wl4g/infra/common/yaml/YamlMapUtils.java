/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.yaml;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.yaml.snakeyaml.constructor.Constructor;

import com.wl4g.infra.common.resource.ByteArrayStreamResource;
import com.wl4g.infra.common.resource.StreamResource;

/**
 * {@link YamlMapUtils}
 * 
 * @author James Wong
 * @version 2022-12-07
 * @since v3.0.0
 */
public abstract class YamlMapUtils {

    public static <T> T parse(final @NotNull List<StreamResource> resources) {
        return parse(resources, null, null);
    }

    public static <T> T parse(final @NotBlank String yaml) {
        return parse(yaml, null, null);
    }

    public static <T> T parse(final @NotBlank String yaml, final @Nullable Constructor constructor, final String rootPath) {
        return parse(singletonList(new ByteArrayStreamResource(((String) hasTextOf(yaml, "yaml")).getBytes(UTF_8))), constructor,
                rootPath);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T parse(
            final @NotNull List<StreamResource> resources,
            final @Nullable Constructor constructor,
            final @Nullable String rootPath) {
        notNullOf(resources, "resources");

        final String[] parts = split(trimToEmpty(rootPath), "/");
        final YamlMapProcessor processor = new YamlMapProcessor(resources);
        if (nonNull(constructor)) {
            processor.setConstructor(constructor);
        }
        Object root = processor.getObject();
        for (int i = 0, size = parts.length; i < size; i++) {
            final String part = parts[i];
            if (root instanceof Map) {
                final Object value = ((Map) root).get(part);
                if (value instanceof Map) {
                    root = value;
                } else if (i < (size - 1)) {
                    throw new IllegalArgumentException(
                            format("Invalid root path '%s', exceeding the depth of map nested objects.", rootPath));
                }
            }
        }

        return (T) root;
    }

    static class YamlMapProcessor extends YamlProcessor {

        @Nullable
        private Map<String, Object> yamlMap;

        public YamlMapProcessor(@NotBlank String yaml) {
            this(singletonList(new ByteArrayStreamResource(((String) hasTextOf(yaml, "yaml")).getBytes(UTF_8))));
        }

        public YamlMapProcessor(@NotNull List<StreamResource> resources) {
            notNullOf(resources, "resources");
            setResources(resources.toArray(new StreamResource[0]));
        }

        @Nullable
        public Map<String, Object> getObject() {
            return (nonNull(yamlMap) ? yamlMap : (yamlMap = createMap()));
        }

        /**
         * Template method that subclasses may override to construct the object
         * returned by this factory.
         * <p>
         * Invoked lazily the first time {@link #getObject()} is invoked in case
         * of a shared singleton; else, on each {@link #getObject()} call.
         * <p>
         * The default implementation returns the merged {@code Map} instance.
         * 
         * @return the object returned by this factory
         * @see #process(MatchCallback)
         */
        protected Map<String, Object> createMap() {
            Map<String, Object> result = new LinkedHashMap<>();
            process((properties, yamlMap) -> merge(result, yamlMap));
            return result;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private void merge(Map<String, Object> output, Map<String, Object> yamlMap) {
            yamlMap.forEach((key, value) -> {
                Object existing = output.get(key);
                if (value instanceof Map && existing instanceof Map) {
                    // Inner cast required by Eclipse IDE.
                    Map<String, Object> result = new LinkedHashMap<>((Map<String, Object>) existing);
                    merge(result, (Map) value);
                    output.put(key, result);
                } else {
                    output.put(key, value);
                }
            });
        }

    }

}

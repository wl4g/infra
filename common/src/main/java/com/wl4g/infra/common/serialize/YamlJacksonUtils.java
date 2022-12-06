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
package com.wl4g.infra.common.serialize;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * {@link YamlJacksonUtils}
 * 
 * @author James Wong
 * @version 2022-12-07
 * @since v3.0.0
 */
public abstract class YamlJacksonUtils {

    public static <T> T parseObject(@NotNull final Class<T> clazz, @NotBlank final String yaml, @NotBlank final String rootPath) {
        notNullOf(clazz, "clazz");
        hasTextOf(yaml, "yaml");
        hasTextOf(rootPath, "rootPath");
        try {
            return defaultObjectMapper.readerFor(clazz).at(rootPath).readValue(yaml);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Default {@link ObjectMapper} instance.
     */
    private static final ObjectMapper defaultObjectMapper = new ObjectMapper(new YAMLFactory());

    static {
        defaultObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

}

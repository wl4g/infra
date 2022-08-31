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
package com.wl4g.infra.common.minio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link S3Policy}
 * 
 * @author James Wong
 * @version 2022-08-31
 * @since v3.0.0
 */
@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
public class S3Policy {

    private @JsonProperty("Version") String version;
    private @JsonProperty("Statement") List<Statement> statement;

    @Getter
    @Setter
    @SuperBuilder
    @ToString
    @NoArgsConstructor
    public static class Statement {
        private @JsonProperty("Effect") EffectType effect;
        private @JsonProperty("Action") List<String> action;
        private @JsonProperty("Resource") List<String> resource;
    }

    public static enum EffectType {
        Allow, Deny
    }

    public static String DEFAULT_POLICY_VERSION = "2012-10-17";

}

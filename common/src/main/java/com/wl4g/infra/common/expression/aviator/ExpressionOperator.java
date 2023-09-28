/*
 *  Copyright (C) 2023 ~ 2035 the original authors WL4G (James Wong).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.wl4g.infra.common.expression.aviator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;

/**
 * The {@link ExpressionOperator}
 *
 * @author James Wong
 * @since v3.0
 **/
@Schema(oneOf = {ExpressionOperator.LogicalOperator.class, ExpressionOperator.RelationOperator.class}, discriminatorProperty = "type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({@JsonSubTypes.Type(value = ExpressionOperator.LogicalOperator.class, name = "LOGICAL"),
        @JsonSubTypes.Type(value = ExpressionOperator.RelationOperator.class, name = "RELATION")})
@Getter
@Setter
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor
public abstract class ExpressionOperator implements Function<JsonNode, Boolean> {
    private @NotBlank String name;
    @Schema(name = "type", implementation = OperatorType.class)
    @JsonProperty(value = "type", access = JsonProperty.Access.WRITE_ONLY)
    private @NotBlank String type;

    public void validate() {
        hasTextOf(name, "name");
        OperatorType.of(type);
    }

    @Getter
    @AllArgsConstructor
    public enum OperatorType {

        LOGICAL(LogicalOperator.class),

        RELATION(RelationOperator.class);

        private final Class<? extends ExpressionOperator> clazz;

        @JsonCreator
        public static OperatorType of(String type) {
            for (OperatorType a : OperatorType.values()) {
                if (a.name().equalsIgnoreCase(type)) {
                    return a;
                }
            }
            throw new IllegalArgumentException(String.format("Invalid node type for '%s'", type));
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class LogicalOperator extends ExpressionOperator {
        private @NotNull LogicalType logical;
        private @NotEmpty List<ExpressionOperator> subConditions;

        @Override
        public Boolean apply(JsonNode record) {
            validate();
            switch (logical) {
                case AND:
                    return safeList(subConditions).stream().allMatch(sub -> sub.apply(record));
                case OR:
                    return safeList(subConditions).stream().anyMatch(sub -> sub.apply(record));
                case NOT:
                    return safeList(subConditions).stream().noneMatch(sub -> sub.apply(record));
                default:
                    throw new Error(String.format("Unsupported logical type '%s'", logical));
            }
        }

        @Override
        public void validate() {
            super.validate();
            notNullOf(logical, "logical");
            safeList(subConditions).forEach(ExpressionOperator::validate);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum LogicalType {
        AND((subFilters, record) -> safeList(subFilters).stream().allMatch(sub -> sub.apply(record))),

        OR((subFilters, record) -> safeList(subFilters).stream().anyMatch(sub -> sub.apply(record))),

        NOT((subFilters, record) -> safeList(subFilters).stream().noneMatch(sub -> sub.apply(record)));

        private final BiFunction<List<RelationOperator>, JsonNode, Boolean> fn;
    }

    @Getter
    @Setter
    @SuperBuilder
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class RelationOperator extends ExpressionOperator {
        private @NotNull AviatorFunction fn;

        @Override
        public Boolean apply(JsonNode record) {
            validate();
            return fn.apply(record);
        }

        @Override
        public void validate() {
            super.validate();
            notNullOf(fn, "aviatorFunction");
        }
    }

}

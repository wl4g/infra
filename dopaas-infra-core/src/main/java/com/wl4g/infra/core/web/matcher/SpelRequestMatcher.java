/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.infra.core.web.matcher;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsAny;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import com.wl4g.infra.core.utils.expression.SpelExpressions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;

/**
 * {@link SpelRequestMatcher}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-07 v3.0.0
 * @since v3.0.0
 */
public class SpelRequestMatcher {

    private @Nullable final List<MatchHttpRequestRule> ruleDefinitions;
    private @NotBlank final SpelExpressions spel = SpelExpressions.create();

    public SpelRequestMatcher(List<MatchHttpRequestRule> ruleDefinitions) {
        this.ruleDefinitions = isEmpty(ruleDefinitions) ? emptyList() : ruleDefinitions;
        this.ruleDefinitions.forEach(rule -> rule.validate()); // Validation
    }

    public List<MatchHttpRequestRule> find(@NotNull RequestExtractor extractor, @NotBlank String expression) {
        notNullOf(extractor, "extractor");
        hasTextOf(expression, "expression");

        // Make model.
        Map<String, Object> model = safeList(ruleDefinitions).stream().collect(toMap(r -> "$".concat(r.getName()), r -> r));
        model.put("$".concat(SPEL_KEYWORDS_REQUEST), extractor);

        // find resolve.
        List<MatchHttpRequestRule> result = ruleDefinitions.stream().filter(e -> {
            model.put("$".concat(SPEL_KEYWORDS_RULE), e);
            return spel.resolve(expression, model);
        }).collect(toList());

        return unmodifiableList(result);
    }

    public boolean matches(@NotNull RequestExtractor extractor, @NotBlank String expression) {
        notNullOf(extractor, "extractor");
        hasTextOf(expression, "expression");

        // Add '$' prefix to all key.
        Map<String, Object> model = ruleDefinitions.stream().collect(toMap(r -> "$".concat(r.getName()), r -> r));
        model.put("$".concat(SPEL_KEYWORDS_REQUEST), extractor);
        model.put("$".concat(SPEL_KEYWORDS_RULES), ruleDefinitions.stream().collect(toMap(r -> r.getName(), r -> r)));

        return spel.resolve(expression, model);
    }

    public static interface RequestExtractor {

        default String getMethod() {
            return null;
        }

        default String getScheme() {
            return null;
        }

        default String getHost() {
            return null;
        }

        default Integer getPort() {
            return null;
        }

        default String getPath() {
            return null;
        }

        default String getHeaderValue(String name) {
            return null;
        }

        default String getCookieValue(String name) {
            return null;
        }

        default String getQueryValue(String name) {
            return null;
        }
    }

    /**
     * @see {@link org.springframework.web.reactive.function.server.RequestPredicates}
     */
    @With
    @Getter
    @Setter
    @ToString
    @SuperBuilder
    @AllArgsConstructor
    public static class MatchHttpRequestRule implements Predicate<RequestExtractor> {

        /**
         * The name of the matching rule (required).
         */
        private @NotBlank String name;

        /**
         * This option is to specify the way to combine the matching result of
         * the header with the matching result of the query parameter. By
         * default, or is used. </br>
         * It does not affect the use of this merge result with schema, host,
         * port, etc. and the final merge method.
         */
        // private boolean orMatchHeaderQuery;

        /**
         * (Optional)The used to match the current request HTTP method. </br>
         * for example: POST
         */
        private @Nullable String method;

        /**
         * (Optional) The used to match the current request HTTP schema. </br>
         * for example: https://
         */
        private @Nullable String scheme;

        /**
         * (Optional) The used to match the current request HTTP host. </br>
         * for example: example.com
         */
        private @Nullable String host;

        /**
         * (Optional)The used to match the current request HTTP port. </br>
         * for example: 443
         */
        private @Nullable Integer port;

        /**
         * (Optional)The used to match the current request HTTP path. </br>
         * for example: /foo/bar/list
         */
        private @Nullable String pathPattern;

        /**
         * (Optional) The name-value used to match the current request HTTP
         * header.
         */
        private @Nullable MatchProperty header;

        /**
         * (Optional) The name-value used to match the current request HTTP
         * query parameter. </br>
         * for example: __iam_gateway_log
         */
        private @Nullable MatchProperty query;

        //
        // Temporary fields.
        //
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private AntPathMatcher pathMatcher;

        public MatchHttpRequestRule() {
            // this.orMatchHeaderQuery = true;
            this.pathMatcher = new AntPathMatcher("/");
        }

        @Override
        public boolean test(RequestExtractor extractor) {
            return doMatchRequest(extractor);
        }

        private boolean doMatchRequest(@NotNull RequestExtractor extractor) {
            notNullOf(extractor, "extractor");

            // Match HTTP method
            boolean flagMethod = isBlank(getMethod());
            if (!flagMethod && equalsIgnoreCase(extractor.getMethod(), getMethod())) {
                flagMethod = true;
            }
            // Match HTTP schema
            boolean flagSchema = isBlank(getScheme());
            if (!flagSchema && equalsIgnoreCase(extractor.getScheme(), getScheme())) {
                flagSchema = true;
            }
            // Match HTTP host
            boolean flagHost = isBlank(getHost());
            if (!flagHost && equalsIgnoreCase(extractor.getHost(), getHost())) {
                flagHost = true;
            }
            // Match HTTP port
            boolean flagPort = (isNull(getPort()) || getPort() <= 0);
            if (!flagPort && equalsIgnoreCase(extractor.getPort() + "", getPort() + "")) {
                flagPort = true;
            }
            // Match HTTP path
            boolean flagPath = isBlank(getPathPattern());
            if (!flagPath && pathMatcher.matchStart(getPathPattern(), extractor.getPath())) {
                flagPath = true;
            }
            // Match HTTP headers.
            boolean flagHeader = isNull(getHeader());
            if (!flagHeader && getHeader().getSymbol().getFunction().apply(
                    trimToEmpty(extractor.getHeaderValue(getHeader().getKey())), getHeader().getValue())) {
                flagHeader = true;
            }
            // Match HTTP query parameter.
            boolean flagQuery = isNull(getQuery());
            if (!flagQuery && getQuery().getSymbol().getFunction().apply(
                    trimToEmpty(extractor.getQueryValue(getQuery().getKey())), getQuery().getValue())) {
                flagQuery = true;
            }
            // if (isOrMatchHeaderQuery()) {
            // return flagMethod && flagSchema && flagHost && flagPort &&
            // flagPath && (flagHeader || flagQuery);
            // }
            return flagMethod && flagSchema && flagHost && flagPort && flagPath && flagHeader && flagQuery;
        }

        public MatchHttpRequestRule validate() {
            // Validation for name.
            hasTextOf(getName(), "rule name is required");
            isTrue(!equalsAny(getName(), SPEL_KEYWORDS_REQUEST, SPEL_KEYWORDS_RULES, SPEL_KEYWORDS_RULE),
                    "Invalid rule name '%s', Cannot conflict with built-in keywords", getName());
            // Validation for header.
            if (nonNull(getHeader())) {
                getHeader().validate();
            }
            // Validation for query.
            if (nonNull(getQuery())) {
                getQuery().validate();
            }
            return this;
        }
    }

    @With
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchProperty {

        /**
         * For example, the matching mode used to match the current request
         * parameter value prefix,suffix,include etc.
         */
        private @NotNull MatchSymbol symbol = MatchSymbol.EQ;

        /**
         * Object key.
         */
        private @NotBlank String key;

        /**
         * Object value.
         */
        private @NotBlank String value;

        public MatchProperty validate() {
            notNull(symbol, "matchMode is required");
            hasText(key, "key is required");
            hasText(value, "value is required");
            return this;
        }
    }

    @Getter
    @AllArgsConstructor
    public static enum MatchSymbol {
        EQ((v1, v2) -> StringUtils.equals(v1, v2)),

        IGNORECASE_EQ((v1, v2) -> equalsIgnoreCase(v1, v2)),

        PREFIX((v1, v2) -> startsWith(v1, v2)),

        IGNORECASE_PREFIX((v1, v2) -> endsWithIgnoreCase(v1, v2)),

        SUFFIX((v1, v2) -> endsWith(v1, v2)),

        IGNORECASE_SUFFIX((v1, v2) -> equalsIgnoreCase(v1, v2)),

        INCLUDE((v1, v2) -> contains(v1, v2)),

        IGNORECASE_INCLUDE((v1, v2) -> containsIgnoreCase(v1, v2));

        private final BiFunction<String, String, Boolean> function;
    }

    public static final String SPEL_KEYWORDS_REQUEST = "request";
    public static final String SPEL_KEYWORDS_RULES = "rules";
    public static final String SPEL_KEYWORDS_RULE = "rule";

}

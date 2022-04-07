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

import static com.wl4g.infra.common.lang.Assert2.hasText;
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.net.URI;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.wl4g.infra.core.utils.expression.SpelExpressions;

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
@Getter
@Setter
@ToString
public class SpelRequestMatcher {

    private @Nullable final Map<String, MatchHttpRequest> definitions;
    private @NotBlank final SpelExpressions spel = SpelExpressions.create(MatchHttpRequest.class, RequestExtractor.class);

    public SpelRequestMatcher(Map<String, MatchHttpRequest> definitions) {
        this.definitions = isEmpty(definitions) ? emptyMap() : definitions;
    }

    public boolean matches(@NotNull RequestExtractor extractor, @NotBlank String expression) {
        notNullOf(extractor, "extractor");
        hasTextOf(expression, "expression");

        Map<String, Object> model = Maps.newHashMap(definitions);
        model.put("definitions", definitions);
        model.put("request", extractor);
        return spel.resolve(expression, model);
    }

    public static interface RequestExtractor {
        default String getMethod() {
            return null;
        }

        default URI getURI() {
            return null;
        }

        default String getScheme() {
            return null;
        }

        default String getHost() {
            return null;
        }

        default String getPort() {
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
    public static class MatchHttpRequest implements Predicate<RequestExtractor> {

        /**
         * This option is used to specify the way to combine the matching result
         * of the header with the matching result of the query parameter. By
         * default, or is used. </br>
         * It does not affect the use of this merge result with schema, host,
         * port, etc. and the final merge method.
         */
        private boolean orMatchHeaderQuery;

        /**
         * (Optional) The value used to match the current request HTTP schema.
         * </br>
         * for example: https://
         */
        private @Nullable String scheme;

        /**
         * (Optional)The value used to match the current request HTTP method.
         * </br>
         * for example: POST
         */
        private @Nullable String method;

        /**
         * (Optional) The value used to match the current request HTTP host.
         * </br>
         * for example: example.com
         */
        private @Nullable String host;

        /**
         * (Optional)The name used to match the current request HTTP port. </br>
         * for example: 443
         */
        private @Nullable Integer port;

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

        public MatchHttpRequest() {
            this.orMatchHeaderQuery = true;
        }

        @Override
        public boolean test(RequestExtractor extractor) {
            return doMatchRequest(extractor);
        }

        private boolean doMatchRequest(@NotNull RequestExtractor extractor) {
            notNullOf(extractor, "extractor");

            // Matches HTTP schema
            boolean flagSchema = isBlank(getScheme());
            if (!flagSchema && equalsIgnoreCase(extractor.getScheme(), getScheme())) {
                flagSchema = true;
            }
            // Matches HTTP method
            boolean flagMethod = isBlank(getMethod());
            if (!flagMethod && equalsIgnoreCase(extractor.getMethod(), getMethod())) {
                flagMethod = true;
            }
            // Matches HTTP host
            boolean flagHost = isBlank(getHost());
            if (!flagHost && equalsIgnoreCase(extractor.getHost(), getHost())) {
                flagHost = true;
            }
            // Matches HTTP port
            boolean flagPort = isNull(getPort());
            if (!flagPort && equalsIgnoreCase(extractor.getPort() + "", getPort() + "")) {
                flagPort = true;
            }
            // Matches HTTP headers.
            boolean flagHeader = isNull(getHeader());
            if (!flagHeader && getHeader().getSymbol().getFunction().apply(
                    trimToEmpty(extractor.getHeaderValue(getHeader().getKey())), getHeader().getValue())) {
                flagHeader = true;
            }
            // Matches HTTP query parameter.
            boolean flagQuery = isNull(getQuery());
            if (!flagQuery && getHeader().getSymbol().getFunction().apply(
                    trimToEmpty(extractor.getQueryValue(getQuery().getKey())), getQuery().getValue())) {
                flagQuery = true;
            }
            if (isOrMatchHeaderQuery()) {
                return flagSchema && flagHost && flagPort && flagMethod && (flagHeader || flagQuery);
            }
            return flagSchema && flagHost && flagPort && flagMethod && flagHeader && flagQuery;
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

}

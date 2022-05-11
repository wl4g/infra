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

import static java.util.Collections.singletonMap;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.wl4g.infra.common.web.WebUtils.WebRequestExtractor;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequestRule;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchProperty;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchSymbol;

/**
 * {@link SpelRequestMatcherTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-07 v3.0.0
 * @since v3.0.0
 */
public class SpelRequestMatcherTests {

    @Test
    public void testFailRequestMatcherByFail2InvalidName() {
        List<MatchHttpRequestRule> ruleDefinitions = Lists.newArrayList();

        // Invalid for 'request'
        ruleDefinitions.add(MatchHttpRequestRule.builder().name("request").build());
        try {
            new SpelRequestMatcher(ruleDefinitions);
            throw new IllegalStateException("Assertion fail of illegal name for 'request'");
        } catch (IllegalArgumentException e) {
            // Ignore for success
        }

        // Invalid for 'rules'
        ruleDefinitions.add(MatchHttpRequestRule.builder().name("rule").build());
        try {
            new SpelRequestMatcher(ruleDefinitions);
            throw new IllegalStateException("Assertion fail of illegal name for 'rules'");
        } catch (IllegalArgumentException e) {
            // Ignore for success
        }

        // Invalid for 'rule'
        ruleDefinitions.add(MatchHttpRequestRule.builder().name("rule").build());
        try {
            new SpelRequestMatcher(ruleDefinitions);
            throw new IllegalStateException("Assertion fail of illegal name for 'rule'");
        } catch (IllegalArgumentException e) {
            // Ignore for success
        }
    }

    @Test
    public void testSuccessAndRequestMatches() {
        WebRequestExtractor mockRequest = new WebRequestExtractor() {
            @Override
            public String getHost() {
                return "portal.example.com";
            }

            @Override
            public String getHeaderValue(String name) {
                if (StringUtils.equals(name, "X-User-Label")) {
                    return "Beta";
                }
                return null;
            }

            @Override
            public String getQueryValue(String name) {
                if (StringUtils.equals(name, "version")) {
                    return "v2";
                }
                return null;
            }
        };

        List<MatchHttpRequestRule> ruleDefinitions = Lists.newArrayList();
        // [Note]: Define a name that conforms to the java method naming
        // specification, which can be used directly in SPEL expressions,
        // otherwise only the method obtained in definitions can be used, for
        // example: definitions.get('my-rule')
        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("userLabelBasedCanaryRule")
                .host("portal.example.com")
                .header(new MatchProperty(MatchSymbol.EQ, "X-User-Label", "Beta"))
                .build());

        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("query-v2-based-canary-rule")
                .host("portal.example.com")
                .query(new MatchProperty(MatchSymbol.EQ, "version", "v2"))
                .build());

        // [Note]: Names that meet the java method naming convention can be used
        // directly.
        String expression = "#{$userLabelBasedCanaryRule.and($rules.get('query-v2-based-canary-rule')).test($request)}";
        SpelRequestMatcher matcher = new SpelRequestMatcher(ruleDefinitions);

        boolean result = matcher.matches(mockRequest, expression);
        System.out.println(result);

        assert result;
    }

    @Test
    public void testSuccessComplexRequestMatches() {
        WebRequestExtractor mockRequest = new WebRequestExtractor() {
            @Override
            public String getMethod() {
                return "POST";
            }

            @Override
            public String getHost() {
                return "portal.example.com";
            }

            @Override
            public String getHeaderValue(String name) {
                if (StringUtils.equals(name, "X-User-Label")) {
                    return "Beta";
                }
                return null;
            }

            @Override
            public String getQueryValue(String name) {
                if (StringUtils.equals(name, "version")) {
                    return "v2";
                }
                return null;
            }
        };

        List<MatchHttpRequestRule> ruleDefinitions = Lists.newArrayList();
        ruleDefinitions.add(
                MatchHttpRequestRule.builder().name("methodBasedCanaryRule").method("GET").host("portal.example.com").build());

        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("userLabelBasedCanaryRule")
                .host("portal.example.com")
                .header(new MatchProperty(MatchSymbol.EQ, "X-User-Label", "Beta"))
                .build());

        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("headerV2BasedCanaryRule")
                .host("portal.example.com")
                .header(new MatchProperty(MatchSymbol.EQ, "version", "v2"))
                .build());

        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("queryV2BasedCanaryRule")
                .host("portal.example.com")
                .query(new MatchProperty(MatchSymbol.EQ, "version", "v2"))
                .build());

        // [Note]: Names that meet the java method naming convention can be used
        // directly.
        String expression = "#{$userLabelBasedCanaryRule.and($headerV2BasedCanaryRule.or($queryV2BasedCanaryRule)).or($methodBasedCanaryRule).test($request)}";
        SpelRequestMatcher matcher = new SpelRequestMatcher(ruleDefinitions);

        boolean result = matcher.matches(mockRequest, expression);
        System.out.println(result);

        assert result;
    }

    @Test
    public void testFailComplexRequestMatches() {
        WebRequestExtractor mockRequest = new WebRequestExtractor() {
            @Override
            public String getMethod() {
                return "POST";
            }

            @Override
            public String getHost() {
                return "portal.example.com";
            }

            @Override
            public String getHeaderValue(String name) {
                if (StringUtils.equals(name, "X-User-Label")) {
                    return "Beta";
                }
                return null;
            }

            @Override
            public String getQueryValue(String name) {
                if (StringUtils.equals(name, "version")) {
                    return "v2";
                }
                return null;
            }
        };

        List<MatchHttpRequestRule> ruleDefinitions = Lists.newArrayList();
        ruleDefinitions.add(
                MatchHttpRequestRule.builder().name("methodBasedCanaryRule").method("GET").host("portal.example.com").build());

        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("userLabelBasedCanaryRule")
                .host("portal.example.com")
                .header(new MatchProperty(MatchSymbol.EQ, "X-User-Label", "Beta"))
                .build());

        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("headerV2BasedCanaryRule")
                .host("portal.example.com")
                .header(new MatchProperty(MatchSymbol.EQ, "version", "v2"))
                .build());

        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("queryV2BasedCanaryRule")
                .host("portal.example.com")
                .query(new MatchProperty(MatchSymbol.EQ, "version", "v2"))
                .build());

        // [Note]: Names that meet the java method naming convention can be used
        // directly.
        String expression = "#{$userLabelBasedCanaryRule.and($headerV2BasedCanaryRule.or($queryV2BasedCanaryRule)).and($methodBasedCanaryRule).test($request)}";
        SpelRequestMatcher matcher = new SpelRequestMatcher(ruleDefinitions);

        boolean result = matcher.matches(mockRequest, expression);
        System.out.println(result);

        assert !result;
    }

    @Test
    public void testSuccessFindRequestMatches() {
        WebRequestExtractor mockRequest = new WebRequestExtractor() {
            @Override
            public String getHost() {
                return "portal.example.com";
            }

            @Override
            public String getHeaderValue(String name) {
                if (StringUtils.equals(name, "X-User-Label")) {
                    return "Beta";
                }
                return null;
            }

            @Override
            public String getQueryValue(String name) {
                if (StringUtils.equals(name, "version")) {
                    return "v2";
                }
                return null;
            }
        };

        List<MatchHttpRequestRule> ruleDefinitions = Lists.newArrayList();
        // [Note]: Define a name that conforms to the java method naming
        // specification, which can be used directly in SPEL expressions,
        // otherwise only the method obtained in definitions can be used, for
        // example: definitions.get('my-rule')
        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("userLabelBasedCanaryRule")
                .host("portal.example.com")
                .header(new MatchProperty(MatchSymbol.EQ, "X-User-Label", "Beta"))
                .build());

        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("query-version-based-canary-rule")
                .host("portal.example.com")
                .query(new MatchProperty(MatchSymbol.EQ, "version", "v2"))
                .build());

        // [Note]: Names that meet the java method naming convention can be used
        // directly.
        String expression = "#{$rule.test($request)}";
        SpelRequestMatcher matcher = new SpelRequestMatcher(ruleDefinitions);

        List<MatchHttpRequestRule> result = matcher.find(mockRequest, expression);
        System.out.println(result);

        assert !isEmpty(result);
    }

    @Test
    public void testAddOtherExtraVarsSuccessFindRequestMatches() {
        WebRequestExtractor mockRequest = new WebRequestExtractor() {
            @Override
            public String getHost() {
                return "portal.example.com";
            }

            @Override
            public String getHeaderValue(String name) {
                if (StringUtils.equals(name, "X-User-Label")) {
                    return "Beta";
                }
                return null;
            }

            @Override
            public String getQueryValue(String name) {
                if (StringUtils.equals(name, "version")) {
                    return "v2";
                }
                return null;
            }
        };

        List<MatchHttpRequestRule> ruleDefinitions = Lists.newArrayList();
        // [Note]: Define a name that conforms to the java method naming
        // specification, which can be used directly in SPEL expressions,
        // otherwise only the method obtained in definitions can be used, for
        // example: definitions.get('my-rule')
        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("userLabelBasedCanaryRule")
                .host("portal.example.com")
                .header(new MatchProperty(MatchSymbol.EQ, "X-User-Label", "Beta"))
                .build());

        ruleDefinitions.add(MatchHttpRequestRule.builder()
                .name("query-version-based-canary-rule")
                .host("portal.example.com")
                .query(new MatchProperty(MatchSymbol.EQ, "version", "v2"))
                .build());

        // [Note]: Names that meet the java method naming convention can be used
        // directly.
        String expression = "#{$rule.or($routeId.get()).test($request)}";

        // Add extra build-in predicates.
        Map<String, Supplier<Predicate<String>>> extraPredicateVariableSuppliers = singletonMap("routeId",
                () -> Predicates.equalTo("example-service-route"));

        SpelRequestMatcher matcher = new SpelRequestMatcher(ruleDefinitions, extraPredicateVariableSuppliers);

        List<MatchHttpRequestRule> result = matcher.find(mockRequest, expression);
        System.out.println(result);

        assert !isEmpty(result);
    }

}

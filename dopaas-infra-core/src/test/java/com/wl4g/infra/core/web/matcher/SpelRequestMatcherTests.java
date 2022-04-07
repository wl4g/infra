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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchHttpRequest;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchProperty;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.MatchSymbol;
import com.wl4g.infra.core.web.matcher.SpelRequestMatcher.RequestExtractor;

/**
 * {@link SpelRequestMatcherTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-07 v3.0.0
 * @since v3.0.0
 */
public class SpelRequestMatcherTests {

    @Test
    public void testAndRequestMatches() {
        Map<String, MatchHttpRequest> definitions = Maps.newHashMap();

        definitions.put("header-label-based-canary",
                MatchHttpRequest.builder()
                        .host("portal.example.com")
                        .header(new MatchProperty(MatchSymbol.EQ, "X-User-Group", "group1"))
                        .build());

        definitions.put("query-version-based-canary",
                MatchHttpRequest.builder()
                        .host("portal.example.com")
                        .query(new MatchProperty(MatchSymbol.EQ, "version", "v1"))
                        .build());

        String expression = "#{definitions.get('header-label-based-canary').and(definitions.get('query-version-based-canary')).test(request)}";
        SpelRequestMatcher matcher = new SpelRequestMatcher(definitions);

        boolean result = matcher.matches(new RequestExtractor() {
            @Override
            public String getHeaderValue(String name) {
                if (StringUtils.equals(name, "X-User-Group")) {
                    return "group1";
                }
                return null;
            }

            @Override
            public String getQueryValue(String name) {
                if (StringUtils.equals(name, "version")) {
                    return "v1";
                }
                return null;
            }
        }, expression);

        System.out.println(result);
    }

}

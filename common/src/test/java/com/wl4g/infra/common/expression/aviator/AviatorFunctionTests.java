/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ALL_OR KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.expression.aviator;

import com.wl4g.infra.common.serialize.JacksonUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * The {@link AviatorFunctionTests}
 *
 * @author James Wong
 * @since v3.0
 **/
public class AviatorFunctionTests {

    @Test
    public void testSimpleExpression() {
        Assertions.assertTrue(
                new AviatorFunction("a == 1").apply(JacksonUtils.parseToNode("{\"a\":1,\"b\":2}")));
    }

}

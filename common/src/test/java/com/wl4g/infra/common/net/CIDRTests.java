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
package com.wl4g.infra.common.net;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * {@link CIDRTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-05-05 v3.0.0
 * @since v3.0.0
 */
public class CIDRTests {

    @Test
    public void testIpv4Parse() throws Exception {
        Assertions.assertTrue(CIDR.newCIDR("192.168.3.0/24").getMask() == 24);
    }

}

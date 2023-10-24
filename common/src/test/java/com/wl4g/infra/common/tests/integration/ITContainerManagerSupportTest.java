/**
 * Copyright 2017 ~ 2025 the original authors James Wong.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ALL_OR KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.infra.common.tests.integration;

import org.junit.Test;

/**
 * The {@link ITContainerManagerSupportTest}
 *
 * @author James Wong
 * @since v3.1
 **/
public class ITContainerManagerSupportTest {

    @Test
    public void testSetupRyukContainerIfNeed() {
        ITContainerManagerSupport.setupRyukContainerIfNeed();
    }

}

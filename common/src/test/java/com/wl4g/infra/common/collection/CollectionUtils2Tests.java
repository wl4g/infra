/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * James Wong <jameswong1376@gmail.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: https://wl4g.github.io
 */
package com.wl4g.infra.common.collection;

import static com.wl4g.infra.common.collection.CollectionUtils2.extractElement;
import static com.wl4g.infra.common.collection.CollectionUtils2.toFirstElement;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * {@link CollectionUtils2Tests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2021-01-12
 * @since v2.0
 * @see
 */
public class CollectionUtils2Tests {

    @Test
    public void testExtractElement() {
        List<String> list = new ArrayList<>();
        list.add("Trump");
        list.add("Biden");
        list.add("Pelosi-West");
        list.add("Obama-West");
        System.out.println(extractElement(list, 0, null));
    }

    @Test
    public void testToFirstElement() {
        List<String> list = new ArrayList<>();
        list.add("Trump");
        list.add("Biden");
        list.add("Pelosi-West");
        list.add("Obama-West");
        toFirstElement(list, s -> s.contains("West"));
        System.out.println(list);
    }

}

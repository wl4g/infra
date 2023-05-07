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
package com.wl4g.infra.common.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * {@link Collector2Tests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-01-07 v1.0.0
 * @since v1.0.0
 */
public class Collector2Tests {

    @Test
    public void testToLinkedHashMap() {
        List<String> list = new ArrayList<>();
        list.add("jack1");
        list.add("tom2");
        list.add("mary3");
        list.add("MARY3");
        Map<String, Integer> result = list.stream().collect(Collectors2.toLinkedHashMap(e -> e, e -> 1));
        System.out.println(result.getClass());
        System.out.println(result);
    }

    @Test
    public void testToCaseInsensitiveHashMap() {
        List<String> list = new ArrayList<>();
        list.add("jack1");
        list.add("tom2");
        list.add("mary3");
        list.add("MARY3");
        Map<String, Integer> result = list.stream().collect(Collectors2.toCaseInsensitiveHashMap(e -> e, e -> 1));
        System.out.println(result.getClass());
        System.out.println(result);
    }

}

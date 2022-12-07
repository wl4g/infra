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
package com.wl4g.infra.common.yaml;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link YamlMapUtilsTests}
 * 
 * @author James Wong
 * @version 2022-12-07
 * @since v1.0.0
 */
public class YamlMapUtilsTests {

    String yamlStr;
    Constructor constructor;

    @Before
    public void setup() throws Exception {
        // @formatter:off
         this.yamlStr = "myservice1:\n"
                + "  config:\n"
                + "    cars:\n"
                + "      - !tesla\n"
                + "        model: M3\n"
                + "        speed: 200\n"
                + "        attributes:\n"
                + "          foo1: bar1\n"
                + "      - !motorcycle\n"
                + "        model: Q3\n"
                + "        speed: 120\n"
                + "        attributes:\n"
                + "          foo2: bar2\n";
        // @formatter:on

        final LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(Integer.MAX_VALUE);
        options.setAllowRecursiveKeys(true);
        this.constructor = new Constructor(options);
        constructor.addTypeDescription(new TypeDescription(TestTesla.class, "!tesla"));
        constructor.addTypeDescription(new TypeDescription(TestMotorcycle.class, "!motorcycle"));
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void testParseYamlToObject() throws Exception {
        // // @formatter:off
        // YAMLMapper mapper = new YAMLMapper(new YAMLFactory());
        // JsonNode node = mapper.readTree(yamlStr);
        // JsonNode resourcesNode = node.at("/myservice1/module1");
        // String jsonAsYaml = new YAMLMapper().writeValueAsString(resourcesNode);
        // System.out.println(jsonAsYaml);
        // // @formatter:on

        Object cars = YamlMapUtils.parse(yamlStr, constructor, "/myservice1/config/cars");
        System.out.println(cars);
        assert cars instanceof List;
        assert ((List<TestCar>) cars).size() == 2;
        assert ((List<TestCar>) cars).get(0) instanceof TestTesla;
        assert ((List<TestCar>) cars).get(1) instanceof TestMotorcycle;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithRootPathFail() throws Exception {
        Object cars = YamlMapUtils.parse(yamlStr, constructor, "/myservice1/config/cars/abc");
        System.out.println(cars);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TestCar {
        private String model;
        private String speed;
        private Map<String, String> attributes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TestTesla extends TestCar {
        private int batteryLife;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TestMotorcycle extends TestCar {
        private String petrolType;
    }

}

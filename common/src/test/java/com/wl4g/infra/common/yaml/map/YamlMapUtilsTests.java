/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong James Wong <jameswong1376@gmail.com>
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
package com.wl4g.infra.common.yaml.map;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link YamlMapUtilsTests}
 * 
 * @author James Wong
 * @version 2022-12-07
 * @since v1.0.0
 */
@Deprecated
public class YamlMapUtilsTests {

    String yamlText;
    LoaderOptions options;
    Constructor constructor;

    @Before
    public void setup() throws Exception {
        // @formatter:off
         this.yamlText = "myservice:\n"
                //+ "  config: !config\n"
                + "  config:\n"
                //+ "    owner: !owner\n"
                + "    owner:\n"
                + "      firstName: jack\n"
                + "      lastName: abcd\n"
                + "    cars:\n"
                + "      - !tesla\n"
                + "        model: M3\n"
                + "        speed: 200\n"
                + "        price:\n"
                + "          proposedPrice: 290000\n"
                + "          dealPrice: 270000\n"
                + "        attributes:\n"
                + "          foo1: bar1\n"
                + "      - !motorcycle\n"
                + "        model: Q3\n"
                + "        speed: 120\n"
                + "        price:\n"
                + "          proposedPrice: 8000\n"
                + "          dealPrice: 7000\n"
                + "        attributes:\n"
                + "          foo2: bar2\n";
        // @formatter:on

        options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(Integer.MAX_VALUE);
        options.setAllowRecursiveKeys(true);
        constructor = new Constructor(options);
        // @formatter:off
        //constructor.addTypeDescription(new TypeDescription(TestConfig.class, "!config"));
        //constructor.addTypeDescription(new TypeDescription(TestOnwer.class, "!owner"));
        // @formatter:on
        constructor.addTypeDescription(new TypeDescription(TestTesla.class, "!tesla"));
        constructor.addTypeDescription(new TypeDescription(TestMotorcycle.class, "!motorcycle"));
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void testParseYamlToObject() throws Exception {
        // // @formatter:off
        // YAMLMapper mapper = new YAMLMapper(new YAMLFactory());
        // JsonNode node = mapper.readTree(yamlText);
        // JsonNode resourcesNode = node.at("/myservice/config");
        // String jsonAsYaml = new YAMLMapper().writeValueAsString(resourcesNode);
        // System.out.println(jsonAsYaml);
        // TestConfig config = new Yaml(constructor).loadAs(jsonAsYaml, TestConfig.class);
        // // @formatter:on

        Object cars = YamlMapUtils.parse(yamlText, constructor, "/myservice/config");
        System.out.println(cars);
        System.out.println(toJSONString(cars, true));
        assert cars instanceof List;
        assert ((List<TestCar>) cars).size() == 2;
        assert ((List<TestCar>) cars).get(0) instanceof TestTesla;
        assert ((List<TestCar>) cars).get(1) instanceof TestMotorcycle;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithRootPathFail() throws Exception {
        Object cars = YamlMapUtils.parse(yamlText, constructor, "/myservice/config/cars");
        System.out.println(cars);
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class TestConfig {
        private TestOnwer owner;
        private List<TestCar> cars;
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestOnwer {
        private String firstName;
        private String lastName;
        private int age;
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class TestCar {
        private String model;
        private String speed;
        private TestPrice price;
        private Map<String, String> attributes;
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class TestTesla extends TestCar {
        private int batteryLife;
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class TestMotorcycle extends TestCar {
        private String petrolType;
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class TestPrice {
        private double proposedPrice;
        private double dealPrice;
    }

}

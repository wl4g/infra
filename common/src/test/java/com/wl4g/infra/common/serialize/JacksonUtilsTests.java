/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.common.serialize;

import static com.wl4g.infra.common.serialize.JacksonUtils.deepClone;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseArrayMapString;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseArrayString;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJsonNode;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.System.out;
import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

public class JacksonUtilsTests {

    //
    // ----- General parse. -----
    //

    @Test
    public void testDirectParseArray() {
        List<Map<String, String>> parsed1 = parseArrayMapString("[{\"key1\":\"value1\"}]");
        System.out.println(parsed1);

        List<String> parsed2 = parseArrayString("[\"value1\",\"value2\"]");
        System.out.println(parsed2);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestUserBean {
        private long id;
        private String name;
        private Map<String, String> attributes = new HashMap<>();
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestBarBean {
        private String barName;
    }

    @Test
    public void testDeepClone() {
        TestBarBean bar = new TestBarBean("myBar");
        TestUserBean bean1 = new TestUserBean(1313466574534868992L, "jack", singletonMap("foo", toJSONString(bar)));

        String json = toJSONString(bean1);
        out.println("Serialization...");
        out.println(json);

        out.println("Deserialization...");
        out.println(parseJSON(json, TestUserBean.class));

        out.println("deepClone0...");
        out.println(deepClone(new ArrayList<>()));

        out.println("deepClone1...");
        List<TestBarBean> list1 = new ArrayList<>();
        list1.add(new TestBarBean("myBar00"));
        out.println(deepClone(list1));

        out.println("deepClone2...");
        Map<String, TestBarBean> map = new HashMap<>();
        map.put("bar1", new TestBarBean("myBar11"));
        out.println(deepClone(map));

        out.println("deepClone3...");
        Map<String, List<Map<String, TestBarBean>>> map2 = new HashMap<>();
        Map<String, TestBarBean> map21 = new HashMap<>();
        map21.put("bar21", new TestBarBean("myBar211"));
        List<Map<String, TestBarBean>> list2 = new ArrayList<>();
        list2.add(map21);
        map2.put("bar2", list2);
        out.println(deepClone(map2));
    }

    @Test
    public void testParseToJsonNode() {
        String json = "{\"name\":\"jack\",\"expire\":1000,\"children\":{\"name\":\"tom\",\"expire\":2000}}";
        JsonNode jsonNode = parseJSON(json, JsonNode.class);
        System.out.println(jsonNode);

        String name = jsonNode.at("/name").asText();
        System.out.println(name);
        Assertions.assertEquals("jack", name);

        String subname = jsonNode.at("/children/name").asText();
        System.out.println(subname);
        Assertions.assertEquals("tom", subname);
    }

    @Test
    public void testParseJsonNode() {
        String json = "{\"name\":\"jack\",\"expire\":1000,\"children\":{\"name\":\"tom\",\"expire\":2000}}";
        JsonNode jsonNode = parseJsonNode(json, "");
        System.out.println(jsonNode);

        String subname = jsonNode.at("/children/name").asText();
        System.out.println(subname);
        Assertions.assertEquals("tom", subname);
    }

    //
    // ----- Sub type parse. -----
    //

    @Test
    public void testPolymorphicMappingToJson() {
        List<TestVehicle> vehicles = new ArrayList<>();
        vehicles.add(TestCar.builder().type("T1").model("X1").power("petroleum").build());
        vehicles.add(TestCar.builder().type("T1").model("X2").power("electric").build());
        vehicles.add(TestBicycle.builder().type("T2").model("X101").color("red").build());
        out.println(toJSONString(vehicles));
    }

    @Test
    public void testParseFromPolymorphicMappingObjectJson() {
        String json1 = "{\"@type\":\"car\",\"model\":\"X1\",\"power\":\"petroleum\"}";
        TestVehicle vehicle1 = parseJSON(json1, TestVehicle.class);
        out.println("object: " + vehicle1);
        out.println("class: " + vehicle1.getClass());

        out.println("-----------------");

        String json2 = "{\"@type\":\"bicycle\",\"model\":\"X101\",\"color\":\"red\"}";
        TestVehicle vehicle2 = parseJSON(json2, TestVehicle.class);
        out.println("object: " + vehicle2);
        out.println("class: " + vehicle2.getClass());
    }

    @Test
    public void testParseFromPolymorphicMappingArrayJson() {
        String arrJson = "[{\"@type\":\"car\",\"model\":\"X1\",\"power\":\"petroleum\"},{\"@type\":\"bicycle\",\"model\":\"X101\",\"color\":\"red\"}]";
        List<TestVehicle> vehicles = parseJSON(arrJson, new TypeReference<List<TestVehicle>>() {
        });
        out.println("array vehicle: " + vehicles);
        out.println("class: " + vehicles.toString());
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
    @JsonSubTypes({ @Type(value = TestCar.class, name = "car"), @Type(value = TestBicycle.class, name = "bicycle"), })
    @Getter
    @Setter
    @SuperBuilder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestVehicle {
        private String model;
        private @JsonProperty(value = "@type") String type;
    }

    @Getter
    @Setter
    @SuperBuilder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestCar extends TestVehicle {
        private String power;
    }

    @Getter
    @Setter
    @SuperBuilder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestBicycle extends TestVehicle {
        private String color;
    }

    @Getter
    @Setter
    @SuperBuilder
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestWrapper {
        private List<TestVehicle> vehicles;
    }

}
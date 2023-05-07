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
package com.wl4g.infra.common.remoting;

import java.net.URI;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.experimental.SuperBuilder;

public class RestClientSimpleTests {

    @Test
    public void testGetForEntity() {
        try {
            String uri = "http://api.map.baidu.com/telematics/v3/weather?location=嘉兴&output=json&ak=5slgyqGDENN7Sy7pw29IUvrZ";
            HttpResponseEntity<String> resp = new RestClient().getForEntity(URI.create(uri), String.class);
            System.out.println(resp.getBody());
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testGetForObject() {
        try {
            String uri = "http://api.map.baidu.com/telematics/v3/weather?location=嘉兴&output=json&ak=5slgyqGDENN7Sy7pw29IUvrZ";
            BaiduWeatherModelResult resp = new RestClient().getForObject(URI.create(uri), BaiduWeatherModelResult.class);
            System.out.println(resp);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testPostForObject() {
        try {
            String uri = "http://httpbin.org/post";
            JsonNode resp = new RestClient().postForObject(URI.create(uri), MyUser.builder().name("jack001").age(18).build(),
                    JsonNode.class);
            System.out.println(resp);
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Data
    public static class BaiduWeatherModelResult {
        private String status;
        private String message;
    }

    @Data
    @SuperBuilder
    public static class MyUser {
        private String name;
        private int age;
    }

}
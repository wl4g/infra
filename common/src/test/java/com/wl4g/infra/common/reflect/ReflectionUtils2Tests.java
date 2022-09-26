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
package com.wl4g.infra.common.reflect;

import static com.wl4g.infra.common.reflect.ReflectionUtils2.findAllDeclaredFields;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl4g.infra.common.remoting.uri.UriComponentsBuilder;

import lombok.Getter;
import lombok.Setter;

public class ReflectionUtils2Tests {

    @Test
    public void testFindFieldWithTreeObjectUriComponentsBuilder() {
        System.out.println(findField(UriComponentsBuilder.class, "host"));
    }

    @Test
    public void testFindAllDeclaredFieldsAndOrder() {
        List<Field> fields = findAllDeclaredFields(B.class, true);
        for (Field f : fields) {
            System.out.println(f);
        }
    }

    @Getter
    @Setter
    public static class A {
        @JsonProperty(index = 2)
        private String name;
        @JsonProperty(index = 1)
        private String type;
    }

    @Getter
    @Setter
    public static class B extends A {
        @JsonProperty(index = 3)
        private int age;
        @JsonProperty(index = 4)
        private C c;
    }

    @Getter
    @Setter
    public static class C {
        @JsonProperty(index = 0)
        private boolean enable;
    }

}
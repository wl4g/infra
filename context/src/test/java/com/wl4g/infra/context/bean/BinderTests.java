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
package com.wl4g.infra.context.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link BinderTests}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-04-28 v3.0.0
 * @since v3.0.0
 */
public class BinderTests {

    @Test
    public void testBinderBindToBean() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("myuser.firstName", "T");
        properties.put("myuser.lastName", "Tom");
        properties.put("myuser.age", 18);
        properties.put("type", "user");
        properties.put("myuser.accounts[0]", "admin");
        properties.put("myuser.attributes['isAdmin']", "true");
        properties.put("myuser.extra", "abcd"); // for test redundant fields
        Binder binder = new Binder(new MapConfigurationPropertySource(properties));
        BindResult<MyUser> result = binder.bind("myuser", MyUser.class);
        System.out.println(result.get());
    }

    @Test
    public void testBinderBindToBeanWithEmptyPrefix() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("firstName", "T");
        properties.put("lastName", "Tom");
        properties.put("age", 18);
        properties.put("type", "user");
        properties.put("accounts[0]", "admin");
        properties.put("attributes['isAdmin']", "true");
        properties.put("extra", "abcd"); // for test redundant fields
        Binder binder = new Binder(new MapConfigurationPropertySource(properties));
        BindResult<MyUser> result = binder.bind("", MyUser.class);
        System.out.println(result.get());
    }

    @Test
    public void testBinderBindMultiValuesToBeanWithEmptyPrefix() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("firstName", "T");
        properties.put("lastName", "Tom");
        properties.put("age", 18);
        properties.put("type", "user");
        properties.put("accounts", "['admin','admin1']");
        properties.put("attributes['isAdmin']", "true");
        properties.put("extra", "abcd"); // for test redundant fields
        Binder binder = new Binder(new MapConfigurationPropertySource(properties));
        BindResult<MyUser> result = binder.bind("", MyUser.class);
        System.out.println(result.get());
    }

    @Getter
    @Setter
    @ToString
    public static class MyUser {
        private String firstName = "M";
        private String lastName = "Mary";
        private MyUserType type = MyUserType.User;
        private int age = 17;
        private List<String> accounts = new ArrayList<>();
        private Map<String, Boolean> attributes = new HashMap<>();
    }

    public static enum MyUserType {
        Admin, User
    }

}

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
package com.wl4g.infra.common.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean2;
import org.junit.Test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link ApacheCommonsBeanUtils2}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-04-28 v3.0.0
 * @since v3.0.0
 */
public class ApacheCommonsBeanUtils2 {

    @Test
    public void testPopulateBean() throws Exception {
        MyUser user = new MyUser();
        Map<String, Object> properties = new HashMap<>();
        properties.put("firstName", "T");
        properties.put("lastName", "Tom");
        properties.put("age", 18);
        // properties.put("accounts[0]", "admin");
        BeanUtilsBean2.getInstance().populate(user, properties);
        System.out.println(user);
    }

    @Getter
    @Setter
    @ToString
    public static class MyUser {
        private String firstName = "M";
        private String lastName = "Mary";
        private int age = 17;
        private List<String> accounts = new ArrayList<>();
    }

}

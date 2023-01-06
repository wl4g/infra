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
package com.wl4g.infra.common.notification;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * {@link NotifierPropertiesTests}
 * 
 * @author James Wong
 * @version 2020-01-05
 * @since v1.0.0
 */
public class NotifierPropertiesTests {

    @Test
    public void testConfigPropertiesResolve() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "jack");
        parameters.put("status", "OK");

        AbstractNotifyProperties config = new AbstractNotifyProperties() {
        };
        config.getTemplates().put("tpl1", "测试消息，名称：${name}，当前状态为：${status}");

        System.out.println(config.resolveMessage("tpl1", parameters));
    }

}
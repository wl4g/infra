/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.infra.core.utils.bean;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.System.out;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class BeanCopierUtilsTests {

    @Test
    public void test1() {
        //
        // 注：bean的setter方法必须是标准的，如：setter方法有返回值也会导致无法复制
        //
        MyUserInfo p1 = new MyUserInfo();
        p1.setPrincipalId("001");
        p1.setPrincipal("zs");
        p1.attributes().put("aa", "11");
        out.println("source p1 object: " + toJSONString(p1) + ", hashCode: " + p1.hashCode());

        MyUserInfo p2 = BeanCopierUtils.clone(p1);
        out.println("clone p2 object: " + toJSONString(p2) + ", hashCode: " + p2.hashCode());

        // for test Error
        BeanCopierUtils.clone(new ArrayList<>());
    }

    @Test
    public void testDeepCopyWithout() throws Exception {
        SubLbProperties targetConfig = new SubLbProperties();
        targetConfig.setName("tom");
        targetConfig.setMaxChooseTries(20);

        LbProperties defaultConfig = new LbProperties();
        defaultConfig.setName("jack");
        defaultConfig.setType("A");
        defaultConfig.setMaxChooseTries(30);

        BeanCopierUtils.deepCopyWithDefault(targetConfig, defaultConfig);
        System.out.println(targetConfig);

        assert targetConfig.getName().equals("tom");
        assert targetConfig.getMaxChooseTries() == 20;
        assert targetConfig.getType().equals("A");
    }

    public class SubLbProperties extends LbProperties {
    }

    @Getter
    @Setter
    @ToString
    public class LbProperties {
        private String name;
        private String type;
        private List<String> definitions = new ArrayList<>();
        private Algorithm algorithm = Algorithm.R;
        private int maxChooseTries = 10;
        private PingProperties ping = new PingProperties();
    }

    @Getter
    @Setter
    @ToString
    static class PingProperties {
        private boolean debug = false;
        private long timeoutMs = 10_000;
        private String path = "/healthz";
        private int expectStatus = 200;
    }

    static enum Algorithm {
        R
    }

}
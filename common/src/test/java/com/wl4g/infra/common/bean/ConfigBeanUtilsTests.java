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
import java.util.List;

import org.junit.Test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link ConfigBeanUtilsTests}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-04-20 v3.0.0
 * @since v3.0.0
 */
public class ConfigBeanUtilsTests {

    @Test
    public void testConfigureWithDefault() throws Exception {
        SubConfig targetConfig = new SubConfig();
        targetConfig.setSubName("mary");
        targetConfig.setName("tom"); // priority
        targetConfig.setMaxTries(30); // priority

        ParentConfig defaultConfig = new ParentConfig();
        defaultConfig.setAlgorithm(AlgorithmType.R); // by covered of default
        defaultConfig.setName("jack"); // by covered of target
        defaultConfig.setType("Canary"); // by default
        defaultConfig.setMaxTries(20); // by covered of target
        defaultConfig.getChilren().setPath("/hello"); // by covered of default

        targetConfig = ConfigBeanUtils.configureWithDefault(new SubConfig(), targetConfig, defaultConfig);

        assert targetConfig.getSubName().equals("mary");
        assert targetConfig.getName().equals("tom");
        assert targetConfig.getAlgorithm() == AlgorithmType.R;// Default value
                                                              // overwrites
                                                              // initial value
        assert targetConfig.getMaxTries() == 30;
        assert targetConfig.getType().equals("Canary");
        assert targetConfig.getChilren().getTimeoutMs() == 6000;
        assert targetConfig.getChilren().getPath() == "/hello";
    }

    @Getter
    @Setter
    @ToString
    public class SubConfig extends ParentConfig {
        private String subName;
    }

    @Getter
    @Setter
    @ToString
    public class ParentConfig {
        private String name;
        private String type;
        private int maxTries = 10;
        private List<String> collection = new ArrayList<>();
        private AlgorithmType algorithm = AlgorithmType.WR;
        private ChilrenConfig chilren = new ChilrenConfig();
    }

    @Getter
    @Setter
    @ToString
    static class ChilrenConfig {
        private boolean debug = false;
        private long timeoutMs = 6000;
        private String path = "/healthz";
    }

    static enum AlgorithmType {
        R, WR
    }

}

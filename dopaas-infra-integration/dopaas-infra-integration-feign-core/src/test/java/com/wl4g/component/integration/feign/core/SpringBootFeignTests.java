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
package com.wl4g.component.integration.feign.core;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;
import com.wl4g.component.core.boot.BootstrappingAutoConfiguration;
import com.wl4g.component.integration.feign.core.GithubService1.GitHubContributor;
import com.wl4g.component.integration.feign.core.SpringBootFeignTests.SampleRetryer;
import com.wl4g.component.integration.feign.core.annotation.EnableFeignConsumers;
import com.wl4g.component.support.cache.jedis.JedisClientAutoConfiguration;

import feign.Retryer;

@RunWith(SpringRunner.class)
@ActiveProfiles("test1")
@SpringBootTest(classes = SpringBootFeignTests.class)
@EnableAutoConfiguration(exclude = { BootstrappingAutoConfiguration.class, JedisClientAutoConfiguration.class })
@EnableFeignConsumers(basePackages = "com.wl4g.component.integration.feign.core", defaultConfiguration = { SampleRetryer.class })
public class SpringBootFeignTests {

    private Logger log = LoggerFactory.getLogger(SpringBootFeignTests.class);

    @Autowired
    private GithubService1 githubService1;

    @Autowired
    private GithubService2 githubService2;

    @Autowired
    private GithubService3 githubService3;

    @Test
    public void test1() {
        List<GitHubContributor> contributors = githubService1.getContributors("wl4g", "dopass-infra");
        log.info(">>> Result:");
        log.info("contributors={}", new Gson().toJson(contributors));
    }

    @Test
    public void test2() {
        List<GitHubRepoModel> repos = githubService2.getRepos("wl4g");
        log.info(">>> Result:");
        log.info("repos={}", new Gson().toJson(repos));
    }

    @Test
    public void test3() {
        List<GitHubRepoModel> repos = githubService3.getRepos("wl4g");
        log.info(">>> Result:");
        log.info("repos={}", new Gson().toJson(repos));
    }

    public static class SampleRetryer extends Retryer.Default {
        public SampleRetryer() {
            super(200, SECONDS.toMillis(2), 3);
        }
    }

}

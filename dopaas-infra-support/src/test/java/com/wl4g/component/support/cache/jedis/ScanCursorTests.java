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
package com.wl4g.component.support.cache.jedis;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wl4g.component.support.cache.jedis.JedisClientAutoConfiguration.JedisProperties;
import com.wl4g.component.support.cache.jedis.ScanCursor.ClusterScanParams;

/**
 * {@link ScanCursorTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-06-12 v1.0.0
 * @see v1.0.0
 */
public class ScanCursorTests {

    private static JedisClient jedisClient;

    @BeforeClass
    public static void initJedisClient() throws Exception {
        JedisProperties config = new JedisProperties();
        config.setPasswd("zzx!@#$%");
        config.setNodes(asList("127.0.0.1:6379", "127.0.0.1:6380", "127.0.0.1:6381", "127.0.0.1:7379", "127.0.0.1:7380",
                "127.0.0.1:7381"));
        JedisClientFactoryBean factory = new JedisClientFactoryBean(config);
        factory.afterPropertiesSet();
        jedisClient = factory.getObject();
    }

    @Test
    public void nextTotalLimitCase1() throws Exception {
        int total = 5;
        List<String> result = doScanWithCursor(total);
        assert result.size() == total;
        System.out.println("Succcessful assertion of next total limit(" + total + ") case!");
    }

    private List<String> doScanWithCursor(int total) throws Exception {
        jedisClient.set("foo1{abc}", "bar1");
        jedisClient.set("foo2{abc}", "bar2");
        jedisClient.set("foo3{abc}", "bar3");

        jedisClient.set("foo4{abcd}", "bar4");
        jedisClient.set("foo5{abcd}", "bar5");
        jedisClient.set("foo6{abcd}", "bar6");

        jedisClient.set("foo7{abcde}", "bar7");
        jedisClient.set("foo8{abcde}", "bar8");
        jedisClient.set("foo9{abcde}", "bar9");

        jedisClient.set("foo10{abcdef}", "bar10");
        jedisClient.set("foo11{abcdef}", "bar11");
        jedisClient.set("foo12{abcdef}", "bar12");

        jedisClient.set("foo13{abcdefg}", "bar13");
        jedisClient.set("foo14{abcdefg}", "bar14");
        jedisClient.set("foo15{abcdefg}", "bar15");

        jedisClient.set("foo16{abcdefgh}", "bar16");
        jedisClient.set("foo17{abcdefgh}", "bar17");
        jedisClient.set("foo18{abcdefgh}", "bar18");

        System.out.println("Starting scaning tests...");
        List<String> result = new ArrayList<>();

        ClusterScanParams params = new ClusterScanParams(total, "foo*");
        ScanCursor<String> cursor = new ScanCursor<String>(jedisClient, String.class, params).open();
        while (cursor.hasNext()) {
            String value = cursor.next();
            System.out.println("scan value: " + value);
            result.add(value);
        }
        return result;
        // return cursor.readValues();
    }

}

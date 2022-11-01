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
package com.wl4g.infra.common.cache.jedis;

import static java.lang.System.out;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import com.wl4g.infra.common.cache.jedis.JedisClientBuilder.JedisConfig;
import com.wl4g.infra.common.cache.jedis.cursor.HashScanCursor;
import com.wl4g.infra.common.cache.jedis.cursor.HashScanCursor.HashScanParams;

/**
 * {@link HashScanCursorTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2021-06-12 v1.0.0
 * @see v1.0.0
 */
public class HashScanCursorTests {

    JedisClient jedisClient;

    @Before
    public void setup() throws Exception {
        JedisConfig config = new JedisConfig();
        config.setNodes(asList(new String[] { "127.0.0.1:6379", "127.0.0.1:6380", "127.0.0.1:6381", "127.0.0.1:7379",
                "127.0.0.1:7380", "127.0.0.1:7381" }));
        config.setPasswd("zzx!@#$%");

        out.println("Instantiating composite operators adapter with cluster ...");
        jedisClient = new JedisClientBuilder(config).build();
    }

    @Test
    public void testNextTotalLimit() throws Exception {
        int total = 3;
        List<String> result = doScan(total);
        assert result.size() == total;
        System.out.println("Succcessful assertion of next total limit(" + total + ") case!");
    }

    List<String> doScan(int total) throws Exception {
        jedisClient.hset("mykey1", "foo1{abc}", "bar1");
        jedisClient.hset("mykey1", "foo2{abc}", "bar2");
        jedisClient.hset("mykey1", "foo3{abc}", "bar3");

        jedisClient.hset("mykey1", "foo4{abcd}", "bar4");
        jedisClient.hset("mykey1", "foo5{abcd}", "bar5");
        jedisClient.hset("mykey1", "foo6{abcd}", "bar6");

        jedisClient.hset("mykey1", "foo7{abcde}", "bar7");
        jedisClient.hset("mykey1", "foo8{abcde}", "bar8");
        jedisClient.hset("mykey1", "foo9{abcde}", "bar9");

        jedisClient.hset("mykey1", "foo10{abcdef}", "bar10");
        jedisClient.hset("mykey1", "foo11{abcdef}", "bar11");
        jedisClient.hset("mykey1", "foo12{abcdef}", "bar12");

        jedisClient.hset("mykey1", "foo13{abcdefg}", "bar13");
        jedisClient.hset("mykey1", "foo14{abcdefg}", "bar14");
        jedisClient.hset("mykey1", "foo15{abcdefg}", "bar15");

        jedisClient.hset("mykey1", "foo16{abcdefgh}", "bar16");
        jedisClient.hset("mykey1", "foo17{abcdefgh}", "bar17");
        jedisClient.hset("mykey1", "foo18{abcdefgh}", "bar18");

        System.out.println("Starting hash scaning tests...");
        List<String> result = new ArrayList<>();

        HashScanParams params = new HashScanParams(total, "foo*");
        HashScanCursor<String> cursor = new HashScanCursor<String>(jedisClient, "mykey1".getBytes(), String.class, params).open();
        while (cursor.hasNext()) {
            Entry<String, String> entry = cursor.next();
            System.out.println(entry.getKey() + " ==> " + entry.getValue());
            result.add(entry.getValue());
        }
        return result;
    }

}

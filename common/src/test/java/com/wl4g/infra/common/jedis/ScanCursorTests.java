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
package com.wl4g.infra.common.jedis;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.wl4g.infra.common.jedis.cursor.ScanCursor;
import com.wl4g.infra.common.jedis.cursor.ScanCursor.ClusterScanParams;

/**
 * {@link ScanCursorTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2021-06-12 v1.0.0
 * @see v1.0.0
 */
public class ScanCursorTests extends BaseJedisServiceTests {

    @Test
    public void testNextTotalLimit() throws Exception {
        int total = 5;
        List<String> result = doScan(total);
        assert result.size() == total;
        System.out.println("Succcessful assertion of next total limit(" + total + ") case!");
    }

    List<String> doScan(int total) throws Exception {
        jedisService.getJedisClient().set("foo1{abc}", "bar1");
        jedisService.getJedisClient().set("foo2{abc}", "bar2");
        jedisService.getJedisClient().set("foo3{abc}", "bar3");

        jedisService.getJedisClient().set("foo4{abcd}", "bar4");
        jedisService.getJedisClient().set("foo5{abcd}", "bar5");
        jedisService.getJedisClient().set("foo6{abcd}", "bar6");

        jedisService.getJedisClient().set("foo7{abcde}", "bar7");
        jedisService.getJedisClient().set("foo8{abcde}", "bar8");
        jedisService.getJedisClient().set("foo9{abcde}", "bar9");

        jedisService.getJedisClient().set("foo10{abcdef}", "bar10");
        jedisService.getJedisClient().set("foo11{abcdef}", "bar11");
        jedisService.getJedisClient().set("foo12{abcdef}", "bar12");

        jedisService.getJedisClient().set("foo13{abcdefg}", "bar13");
        jedisService.getJedisClient().set("foo14{abcdefg}", "bar14");
        jedisService.getJedisClient().set("foo15{abcdefg}", "bar15");

        jedisService.getJedisClient().set("foo16{abcdefgh}", "bar16");
        jedisService.getJedisClient().set("foo17{abcdefgh}", "bar17");
        jedisService.getJedisClient().set("foo18{abcdefgh}", "bar18");

        System.out.println("Starting scaning tests...");
        List<String> result = new ArrayList<>();

        ClusterScanParams params = new ClusterScanParams(total, "foo*");
        ScanCursor<String> cursor = new ScanCursor<String>(jedisService.getJedisClient(), String.class, params).open();
        while (cursor.hasNext()) {
            String value = cursor.next();
            System.out.println("scan value: " + value);
            result.add(value);
        }
        return result;
    }

}

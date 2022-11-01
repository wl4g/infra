/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
package com.wl4g.infra.common.rocksdb;

import static com.google.common.base.Charsets.UTF_8;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * {@link RocksDBServiceTests}
 * 
 * @author James Wong
 * @version 2022-11-01
 * @since v3.0.0
 * @see https://github1s.com/facebook/rocksdb/blob/v6.20.3/java/src/test/java/org/rocksdb/RocksIteratorTest.java#L25-L26
 */
public class RocksDBServiceTests {

    RocksDBService rocksDBService;

    @Before
    public void setup() {
        this.rocksDBService = new RocksDBService(RocksDBConfig.builder().build());
    }

    @Test
    public void testPut_Get_GetKeys_Iterator() throws Exception {
        // put
        System.out.println("-----------testing for put ... -----------");
        rocksDBService.put("f1", "key1", "value1".getBytes());
        rocksDBService.put("f1", "key2", "value2".getBytes());
        rocksDBService.put("f1", "key3", "value3".getBytes());
        rocksDBService.put("f1", "key4", "value4".getBytes());

        // get
        System.out.println("-----------testing for get ... -----------");
        String value1 = new String(rocksDBService.get("f1", "key1"), UTF_8);
        System.out.println(value1);
        Assertions.assertEquals(value1, "value1");

        // getKeys
        System.out.println("-----------testing for getKeys ... -----------");
        String lastKey = null;
        List<String> keys = null;
        while (!(keys = rocksDBService.getKeys("f1", lastKey, 2)).isEmpty()) {
            if (!keys.isEmpty()) {
                lastKey = keys.get(keys.size() - 1);
            }
            System.out.println(keys);
        }

        // iterator1
        System.out.println("----------- testing for iterator1 ... -----------");
        int count1 = 0;
        Iterator<Entry<String, byte[]>> it1 = rocksDBService.iterator("f1");
        while (it1.hasNext()) {
            Entry<String, byte[]> entry = it1.next();
            System.out.println(entry.getKey() + "  =>  " + new String(entry.getValue(), UTF_8));
            if (count1 == 0) {
                Assertions.assertEquals(entry.getKey(), "key1");
            } else if (count1 == 1) {
                Assertions.assertEquals(entry.getKey(), "key2");
            } else if (count1 == 2) {
                Assertions.assertEquals(entry.getKey(), "key3");
            } else if (count1 == 3) {
                Assertions.assertEquals(entry.getKey(), "key4");
            }
            ++count1;
        }

        // iterator1
        System.out.println("----------- testing for iterator2 ... -----------");
        int count2 = 0;
        Iterator<Entry<String, String>> it2 = rocksDBService.iterator("f1", String.class, value -> new String(value, UTF_8));
        while (it2.hasNext()) {
            Entry<String, String> entry = it2.next();
            System.out.println(entry.getKey() + "  =>  " + entry.getValue());
            if (count2 == 0) {
                Assertions.assertEquals(entry.getKey(), "key1");
                Assertions.assertEquals(entry.getValue(), "value1");
            } else if (count2 == 1) {
                Assertions.assertEquals(entry.getKey(), "key2");
                Assertions.assertEquals(entry.getValue(), "value2");
            } else if (count2 == 2) {
                Assertions.assertEquals(entry.getKey(), "key3");
                Assertions.assertEquals(entry.getValue(), "value3");
            } else if (count2 == 3) {
                Assertions.assertEquals(entry.getKey(), "key4");
                Assertions.assertEquals(entry.getValue(), "value4");
            }
            ++count2;
        }

    }

}

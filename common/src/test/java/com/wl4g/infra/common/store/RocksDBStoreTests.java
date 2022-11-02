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
package com.wl4g.infra.common.store;

import org.junit.Test;

import com.wl4g.infra.common.store.MapStoreConfig.RocksDBStoreConfig;

/**
 * {@link RocksDBStoreTests}
 * 
 * @author James Wong
 * @version 2022-11-02
 * @since v3.0.0
 */
public class RocksDBStoreTests {

    @Test
    public void testPutGetRemove() throws Exception {
        try (RocksDBMapStore store = new RocksDBMapStore(RocksDBStoreConfig.builder().build(), MyUser.class, "teststore");) {
            store.put("jack01", MyUser.builder().name("jack001").age(99).build());
            store.put("jack02", MyUser.builder().name("jack002").age(44).build());
            store.put("jack03", MyUser.builder().name("jack003").age(35).build());
            store.put("jack04", MyUser.builder().name("jack004").age(88).build());

            MyUser user01 = (MyUser) store.get("jack01");
            System.out.println(user01);
            MyUser user02 = (MyUser) store.get("jack02");
            System.out.println(user02);
            MyUser user03 = (MyUser) store.get("jack03");
            System.out.println(user03);
            MyUser user04 = (MyUser) store.get("jack04");
            System.out.println(user04);

            store.remove("jack04");
            user04 = (MyUser) store.get("jack04");
            System.out.println(user04);
        }
    }

}

/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong James Wong <jameswong1376@gmail.com>
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
package com.wl4g.infra.common.crypto.symmetric;

import org.junit.Test;

import com.wl4g.infra.common.codec.CodecSource;

/**
 * {@link AESBouncycastleUtilsTests}
 * 
 * @author James Wong
 * @version 2023-01-05
 * @since v1.0.0
 */
public class AESBouncycastleUtilsTests {

    @Test
    public void testAes128Cbc() throws Exception {
        final String srcStr = "abcdefghijklmnopqrstuvwxyz123456";
        final byte[] src = srcStr.getBytes();
        final byte[] key = "1234567890abcdef".getBytes();
        final byte[] iv = key;

        // The encryption
        final byte[] encrypted = AESBouncycastleUtils.aes128Cbc(key, iv, src, true);
        System.out.println("encrypted(hex):" + new CodecSource(encrypted).toHex());

        // The decryption
        final byte[] src2 = AESBouncycastleUtils.aes128Cbc(key, iv, encrypted, false);
        final String decrypted = new CodecSource(src2).toString();
        System.out.println("decrypted:" + decrypted);

        assert decrypted.equals(srcStr);
    }

}

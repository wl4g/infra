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
package com.wl4g.infra.common.crypto.symmetric;

import org.junit.Test;

import com.wl4g.infra.common.codec.CodecSource;

/**
 * {@link AES128CBCNoPaddingTests}
 * 
 * @author James Wong
 * @version 2023-01-05
 * @since v1.0.0
 */
public class AES128CBCNoPaddingTests {

    @Test
    public void testAes128CbcNoPadding() throws Exception {
        // 由于使用 NoPadding 模式, 因此加密数据字节长度必须为 16 的倍数
        final String srcStr = "abcdefghijklmnopqrstuvwxyz123456";
        final byte[] key = "1234567890abcdef".getBytes();
        final byte[] iv = key;

        // The encryption
        final CodecSource encrypted = new AES128CBCNoPadding().encrypt(key, iv, new CodecSource(srcStr));
        System.out.println("encrypted(hex):" + encrypted.toHex());

        // The decryption
        final CodecSource src2 = new AES128CBCNoPadding().decrypt(key, iv, encrypted);
        final String decrypted = src2.toString();
        System.out.println("decrypted:" + decrypted);

        assert decrypted.equals(srcStr);
    }

}

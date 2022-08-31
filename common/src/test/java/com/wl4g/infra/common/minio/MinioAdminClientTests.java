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
package com.wl4g.infra.common.minio;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Collections.emptyList;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.Test;

import com.wl4g.infra.common.minio.v8_4.MinioAdminClient;
import com.wl4g.infra.common.minio.v8_4.UserInfo;
import com.wl4g.infra.common.minio.v8_4.UserInfo.Status;

/**
 * {@link MinioAdminClientTests}
 * 
 * @author James Wong
 * @version 2022-08-31
 * @since v3.0.0
 */
public class MinioAdminClientTests {

    static String TEST_ADD_ACCESSKEY1 = "test_tenant1001_user1";
    static String TEST_ADD_SECRETKEY1 = "12345678";
    static String TEST_ADD_POLICY_NAME1 = "test_tenant1001_policy1";
    static String TEST_ADD_POLICY_JSON1 = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"s3:PutObject\",\"s3:GetBucketLocation\",\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::tenant1001/*\"]}]}";

    @Test
    public void testGetUsers() throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint("localhost", 9000, false)
                .credentials("minioadmin", "minioadmin")
                .build();
        Map<String, UserInfo> users = adminCilent.listUsers();
        System.out.println(toJSONString(users));
    }

    @Test
    public void testCreatePolicy() throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint("localhost", 9000, false)
                .credentials("minioadmin", "minioadmin")
                .build();
        adminCilent.addCannedPolicy(TEST_ADD_POLICY_NAME1, TEST_ADD_POLICY_JSON1);
    }

    @Test
    public void testCreateUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint("localhost", 9000, false)
                .credentials("minioadmin", "minioadmin")
                .build();
        adminCilent.addUser(TEST_ADD_ACCESSKEY1, Status.ENABLED, TEST_ADD_SECRETKEY1, TEST_ADD_POLICY_NAME1, emptyList());
    }

}

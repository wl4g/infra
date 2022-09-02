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
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.Test;

import com.wl4g.infra.common.minio.S3Policy.EffectType;
import com.wl4g.infra.common.minio.S3Policy.Statement;
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

    public static final String ENDPOINT = "http://localhost:9000";
    public static final String REGION = "us-east-1";

    public static final String SUPER_ADMIN_ACCESSKEY = "minioadmin";
    public static final String SUPER_ADMIN_SECRETKEY = "minioadmin";

    public static final String TENANT_ACCESSKEY = "tenant1001";
    public static final String TENANT_SECRETKEY = "12345678";
    public static final String TENANT_BUCKET = "tenant1001";
    public static final String TENANT_POLICY_NAME = "tenant1001policy1";
    public static final String TENANT_POLICY_JSON = S3Policy.builder()
            .version(S3Policy.DEFAULT_POLICY_VERSION)
            .statement(singletonList(Statement.builder()
                    .effect(EffectType.Allow)
                    // see:https://docs.aws.amazon.com/service-authorization/latest/reference/list_amazons3.html#amazons3-actions-as-permissions
                    .action(asList("s3:GetBucketLocation", "s3:GetBucketPolicyStatus", "s3:ListBucket", "s3:GetObject",
                            "s3:PutObject"))
                    .resource(singletonList("arn:aws:s3:::" + TENANT_BUCKET + "/*"))
                    .build()))
            .build()
            .toString();

    @Test
    public void testGetUsers() throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint(ENDPOINT)
                .region(REGION)
                .credentials(SUPER_ADMIN_ACCESSKEY, SUPER_ADMIN_SECRETKEY)
                .build();
        Map<String, UserInfo> users = adminCilent.listUsers();
        System.out.println(toJSONString(users));
    }

    @Test
    public void testCreatePolicy() throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint(ENDPOINT)
                .region(REGION)
                .credentials(SUPER_ADMIN_ACCESSKEY, SUPER_ADMIN_SECRETKEY)
                .build();
        adminCilent.addCannedPolicy(TENANT_POLICY_NAME, TENANT_POLICY_JSON);
    }

    @Test
    public void testCreateUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint(ENDPOINT)
                .region(REGION)
                .credentials(SUPER_ADMIN_ACCESSKEY, SUPER_ADMIN_SECRETKEY)
                .build();
        // Won't automatically assign policies? ? ?
        adminCilent.addUser(TENANT_ACCESSKEY, Status.ENABLED, TENANT_SECRETKEY, TENANT_POLICY_NAME, emptyList());
    }

    @Test
    public void testAssignPolicyToUser()
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint(ENDPOINT)
                .region(REGION)
                .credentials(SUPER_ADMIN_ACCESSKEY, SUPER_ADMIN_SECRETKEY)
                .build();
        adminCilent.setPolicy(TENANT_ACCESSKEY, false, TENANT_POLICY_NAME);
    }

}

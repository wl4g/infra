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

import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.credentials.AssumeRoleProvider;
import io.minio.credentials.Credentials;
import io.minio.http.HttpUtils;
import okhttp3.OkHttpClient;

/**
 * {@link MinioClientTests}
 * 
 * @author James Wong
 * @version 2022-08-31
 * @since v3.0.0
 */
public class MinioClientTests {

    static OkHttpClient defaultHttpClient = HttpUtils.newDefaultHttpClient(15_000, 15_000, 15_000);

    @Test
    public void testBucketExists() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint("localhost", 9000, false)
                .credentials("minioadmin", "minioadmin")
                .build();
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket("test1").build());
        System.out.println(exists);
    }

    /**
     * <pre>
     * 前置条件:
     * 1. 在 MinIO 控制台新建存储桶 test_tenant1001
     * 2. 在 MinIO 控制台新建策略 test_tenant1001_policy1, 其策略值例如(对桶 test_tenant1001/ 下的所有对象授权了3种操作权限):
     *    {
     *        "Version": "2012-10-17", // 版本号固定值
     *        "Statement": [
     *            {
     *                "Effect": "Allow",
     *                "Action": [
     *                    "s3:GetBucketLocation",
     *                    "s3:GetObject",
     *                    "s3:PutObject"
     *                ],
     *                "Resource": [
     *                    "arn:aws:s3:::test_tenant1001/*"
     *                ]
     *            }
     *        ]
     *    }
     * 3. 在 MinIO 控制台新建用户 accessKey=test_tenant1001_user1, secretKey=12345678
     * 4. 在 MinIO 控制台给用户 test_tenant1001_user1 分配策略 test_tenant1001_policy1
     * </pre>
     *
     * 也可以使用如{@link com.wl4g.infra.common.minio.MinioAdminClientTests#testCreatePolicy()}的SDK实现自动创建.
     * 
     * @see https://github.com/minio/minio/delete/8.4.3/docs/sts/client-grants.md
     */
    @Test
    public void testGetSTSWithAssumeRoleWithClientGrants() throws NoSuchAlgorithmException {
        String stsEndpoint = "http://localhost:9000";
        String accessKey = "test_tenant1001_user1";
        String secretKey = "12345678";
        int durationSeconds = 5 * 60;
        // 本次操作需要申请的策略(最多可对桶 tenant1001/* 申请3种权限)
        String applyPolicy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::test_tenant1001/*\"]}]}";
        String region = "us-east-1";
        String roleSessionName = "anySession"; // 任意
        // 本次申请资源标识
        String roleArn = "arn:aws:s3:::test_tenant1001/*";
        String externalId = "myapp";

        AssumeRoleProvider provider = new AssumeRoleProvider(stsEndpoint, accessKey, secretKey, durationSeconds, applyPolicy,
                region, roleArn, roleSessionName, externalId, defaultHttpClient);

        Credentials credentials = provider.fetch();
        System.out.println("accessKey:" + credentials.accessKey());
        System.out.println("secretKey:" + credentials.secretKey());
        System.out.println("sessionToken:" + credentials.sessionToken());
        System.out.println("expired:" + credentials.isExpired());
    }
}

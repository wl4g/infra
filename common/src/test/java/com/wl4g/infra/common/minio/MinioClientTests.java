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

import static com.wl4g.infra.common.lang.FastTimeClock.currentTimeMillis;
import static com.wl4g.infra.common.minio.MinioAdminClientTests.ENDPOINT;
import static com.wl4g.infra.common.minio.MinioAdminClientTests.REGION;
import static com.wl4g.infra.common.minio.MinioAdminClientTests.SUPER_ADMIN_ACCESSKEY;
import static com.wl4g.infra.common.minio.MinioAdminClientTests.TENANT_ACCESSKEY;
import static com.wl4g.infra.common.minio.MinioAdminClientTests.TENANT_BUCKET;
import static com.wl4g.infra.common.minio.MinioAdminClientTests.TENANT_SECRETKEY;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import com.wl4g.infra.common.io.FileIOUtils;
import com.wl4g.infra.common.minio.S3Policy.EffectType;
import com.wl4g.infra.common.minio.S3Policy.Statement;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.credentials.AssumeRoleProvider;
import io.minio.credentials.Credentials;
import io.minio.credentials.Provider;
import io.minio.errors.ErrorResponseException;
import io.minio.http.HttpUtils;
import io.minio.messages.Bucket;
import okhttp3.OkHttpClient;

/**
 * {@link MinioClientTests}
 * 
 * @author James Wong
 * @version 2022-08-31
 * @since v3.0.0
 */
public class MinioClientTests {

    public static final OkHttpClient defaultHttpClient = HttpUtils.newDefaultHttpClient(15_000, 15_000, 15_000);

    // STS temporary user
    public static final String USER_PREFIX = "library";
    public static final String USER_OBJECT_NAME = "test1.txt";

    public static final String USER_POLICY_NAME = "tenant1001policy2";

    @Test
    public void testBucketExistsWithinVisiblePermissions() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(ENDPOINT)
                .region(REGION)
                .credentials(TENANT_ACCESSKEY, TENANT_SECRETKEY)
                .build();
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(TENANT_BUCKET).build());
        System.out.println(exists);
    }

    // MiniIO only super administrators can create buckets???
    @Test
    public void testCreateBucketWithSuperAdminIfNotExist() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(ENDPOINT)
                .region(REGION)
                .credentials(SUPER_ADMIN_ACCESSKEY, SUPER_ADMIN_ACCESSKEY)
                .build();
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(TENANT_BUCKET).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(TENANT_BUCKET).build());
        }
    }

    @Test
    public void testListBucketsWithinVisiblePermissions() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(ENDPOINT)
                .region(REGION)
                .credentials(TENANT_ACCESSKEY, TENANT_SECRETKEY)
                .build();
        for (Bucket bucket : client.listBuckets()) {
            System.out.println("bucketName: " + bucket.name());
            System.out.println("bucketCreationDate: " + bucket.creationDate());
        }
    }

    @Test
    public void testUseAssumeSTSAndPutObjectSuccess() throws Exception {
        Provider provider = createSTSAssumeCredentialsProvider();

        // 创建测试文件
        File testfile = new File("/tmp/s3test-" + currentTimeMillis());
        FileIOUtils.writeFile(testfile, "abcdefghijklmnopqrstuvwxyz");

        // 使用 STS 测试 PutObject
        MinioClient client = MinioClient.builder().endpoint(ENDPOINT).region(REGION).credentialsProvider(provider).build();
        ObjectWriteResponse resp = client.putObject(PutObjectArgs.builder()
                .bucket(TENANT_BUCKET)
                // .object(USER_OBJECT_NAME)
                .object(USER_PREFIX + "/" + USER_OBJECT_NAME)
                .stream(new FileInputStream(testfile), testfile.length(), 5 * 1024 * 1024)
                .build());

        System.out.println("result bucket: " + resp.bucket());
        System.out.println("result object: " + resp.object());
        System.out.println("result versionId: " + resp.versionId());
        System.out.println("result etag: " + resp.etag());
        System.out.println("-------------------------------");
        System.out.println("result headers: \n" + resp.headers());
    }

    @Test
    public void testUseAssumeSTSAndPutObject403() throws Exception {
        Provider provider = createSTSAssumeCredentialsProvider();

        // 创建测试文件
        File testfile = new File("/tmp/s3test-" + currentTimeMillis());
        FileIOUtils.writeFile(testfile, "abcdefghijklmnopqrstuvwxyz");

        // 使用 STS 测试 PutObject
        MinioClient client = MinioClient.builder().endpoint(ENDPOINT).region(REGION).credentialsProvider(provider).build();
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(TENANT_BUCKET)
                    .object(USER_OBJECT_NAME)
                    .stream(new FileInputStream(testfile), testfile.length(), 5 * 1024 * 1024)
                    .build());
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("AccessDenied")) {
                System.out.println(
                        format("No permission to write object to '%s/%s' tested succeeded.", TENANT_BUCKET, USER_OBJECT_NAME));
            } else {
                throw e;
            }
        }
    }

    /**
     * <pre>
     * 前置条件:
     * 1. 在 MinIO 控制台新建存储桶 tenant1001
     * 2. 在 MinIO 控制台新建策略 tenant1001policy1, 其策略值例如(对桶 tenant1001/ 下的所有对象授权了3种操作权限):
     *    {
     *        "Version": "2012-10-17", // 版本号固定值
     *        "Statement": [
     *            {
     *                "Effect": "Allow",
     *                "Action": [
     *                    "s3:GetBucketLocation",
     *                    "s3:GetObject",
     *                    "s3:PutObject",
     *                    "s3:ListBucket",
     *                    "s3:GetBucketPolicyStatus"
     *                ],
     *                "Resource": [
     *                    "arn:aws:s3:::tenant1001/*"
     *                ]
     *            }
     *        ]
     *    }
     * 3. 在 MinIO 控制台新建用户 accessKey=tenant1001, secretKey=12345678
     * 4. 在 MinIO 控制台给用户 tenant1001 分配策略 tenant1001policy1
     * </pre>
     *
     * 等价的使用 Java-SDK 客户端自动创建, 参见
     * {@link com.wl4g.infra.common.minio.MinioAdminClientTests#testCreatePolicy()}
     * 
     * @see https://github.com/minio/minio/delete/8.4.3/docs/sts/client-grants.md
     */
    private Provider createSTSAssumeCredentialsProvider() throws Exception {
        /**
         * 为本次操作申请的最小权限策略, 最多可对桶
         * {@link MinioAdminClientTests#TENANT_POLICY_JSON} 申请
         * {@link MinioAdminClientTests#TENANT_POLICY_JSON} 种权限
         */
        S3Policy applyPolicy = S3Policy.builder()
                .version(S3Policy.DEFAULT_POLICY_VERSION)
                .statement(singletonList(Statement.builder()
                        .effect(EffectType.Allow)
                        .action(asList("s3:PutObject"))
                        // 资源标识
                        .resource(singletonList("arn:aws:s3:::" + TENANT_BUCKET + "/" + USER_PREFIX + "/*"))
                        .build()))
                .build();
        int durationSeconds = 5 * 60;
        String roleSessionName = "anySession"; // 任意
        String externalId = "myapp";

        // 获取有限权限的STS(临时)
        Provider provider = new AssumeRoleProvider(ENDPOINT, TENANT_ACCESSKEY, TENANT_SECRETKEY, durationSeconds,
                applyPolicy.toString(), REGION, null, roleSessionName, externalId, defaultHttpClient);
        Credentials credentials = provider.fetch();
        System.out.println("assume sts accessKey:" + credentials.accessKey());
        System.out.println("assume sts secretKey:" + credentials.secretKey());
        System.out.println("assume sts sessionToken:" + credentials.sessionToken());
        System.out.println("assume sts expired:" + credentials.isExpired());
        System.out.println("-------------------------------");
        return provider;
    }

}

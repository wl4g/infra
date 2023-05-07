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
package com.wl4g.infra.common.minio;

import static com.wl4g.infra.common.minio.S3Policy.Action.CreateBucketAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.GetBucketLocationAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.GetBucketPolicyStatusAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.GetObjectAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.GetObjectLegalHoldAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.ListAllMyBucketsAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.ListBucketAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.ListBucketMultipartUploadsAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.ListMultipartUploadPartsAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.PutObjectAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.PutObjectLegalHoldAction;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.InvalidCipherTextException;

import com.wl4g.infra.common.cli.CommandLineTool;
import com.wl4g.infra.common.cli.CommandLineTool.CommandLineFacade;
import com.wl4g.infra.common.minio.S3Policy.EffectType;
import com.wl4g.infra.common.minio.S3Policy.Statement;
import com.wl4g.infra.common.minio.v8_4.MinioAdminClient;
import com.wl4g.infra.common.minio.v8_4.UserInfo.Status;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

/**
 * {@link MinioAdminTool}
 * 
 * @author James Wong
 * @version 2022-09-16
 * @since v3.0.0
 */
public final class MinioAdminTool {

    public static final String DEFAULT_ENDPOINT = "http://localhost:9000";
    public static final String DEFAULT_REGION = "us-east-1";

    public static final String DEFAULT_ADMIN_ACCESSKEY = "minioadmin";
    public static final String DEFAULT_ADMIN_SECRETKEY = "minioadmin";

    public static final String DEFAULT_TENANT_ACCESSKEY = "tenant01";
    public static final String DEFAULT_TENANT_SECRETKEY = "12345678";
    public static final String DEFAULT_TENANT_BUCKET = DEFAULT_TENANT_ACCESSKEY;

    public static final String DEFAULT_TENANT_POLICY_NAME = DEFAULT_TENANT_ACCESSKEY + "_readwrite";

    public static void main(String[] args) throws Exception {
        CommandLineFacade line = CommandLineTool.builder()
                .option("e", "endpoint", DEFAULT_ENDPOINT, "MinIO server endpoint.")
                .option("r", "region", DEFAULT_REGION, "MinIO server region.")
                .option("A", "adminAccessKey", DEFAULT_ADMIN_ACCESSKEY, "Admin accessKey.")
                .option("S", "adminSecretKey", DEFAULT_ADMIN_SECRETKEY, "Admin secretKey.")
                .option("a", "tenantAccessKey", DEFAULT_TENANT_ACCESSKEY, "Tenant accessKey.")
                .option("s", "tenantSecretKey", DEFAULT_TENANT_SECRETKEY, "Tenant secretKey.")
                .option("b", "tenantBucket", DEFAULT_TENANT_BUCKET, "Tenant bucket.")
                .option("n", "policyName", DEFAULT_TENANT_POLICY_NAME, "Tenant policy name.")
                .option("j", "policyJson", null, "Tenant policy json.")
                .build(args);
        String endpoint = line.get("e");
        String region = line.get("r");
        String adminAccessKey = line.get("A");
        String adminSecretKey = line.get("S");
        String tenantAccessKey = line.get("a");
        String tenantSecretKey = line.get("s");
        String tenantBucket = line.get("b");
        String tenantPolicyName = line.get("n");
        String tenantPolicyJson = line.get("j");

        // Use default policy
        if (isBlank(tenantPolicyJson)) {
            tenantPolicyJson = S3Policy.builder()
                    .version(S3Policy.DEFAULT_POLICY_VERSION)
                    .statement(singletonList(Statement.builder()
                            .effect(EffectType.Allow)
                            // see:https://docs.aws.amazon.com/service-authorization/latest/reference/list_amazons3.html#amazons3-actions-as-permissions
                            .action(asList(CreateBucketAction, GetBucketLocationAction, GetBucketPolicyStatusAction,
                                    ListBucketAction, ListAllMyBucketsAction, ListBucketMultipartUploadsAction,
                                    ListMultipartUploadPartsAction, PutObjectAction, PutObjectLegalHoldAction, GetObjectAction,
                                    GetObjectLegalHoldAction))
                            .resource(singletonList("arn:aws:s3:::" + tenantBucket + "/*"))
                            .build()))
                    .build()
                    .toString();
        }

        System.out.println("Using configuration arguments:");
        System.out.println("---------------------------------------");
        System.out.println("         endpoint: " + endpoint);
        System.out.println("           region: " + region);
        System.out.println("   adminAccessKey: " + adminAccessKey);
        System.out.println("   adminSecretKey: " + adminSecretKey);
        System.out.println("  tenantAccessKey: " + tenantAccessKey);
        System.out.println("  tenantSecretKey: " + tenantSecretKey);
        System.out.println("     tenantBucket: " + tenantBucket);
        System.out.println(" tenantPolicyName: " + tenantPolicyName);
        System.out.println(" tenantPolicyJson: " + tenantPolicyJson);
        System.out.println("---------------------------------------");
        System.out.println("\nRequest to MinIO Server ...\n");

        createTenantPolicy(endpoint, region, adminAccessKey, adminSecretKey, tenantPolicyName, tenantPolicyJson);
        createTenantUser(endpoint, region, adminAccessKey, adminSecretKey, tenantAccessKey, tenantSecretKey, tenantPolicyName);
        assignTenantPolicyToUser(endpoint, region, adminAccessKey, adminSecretKey, tenantAccessKey, tenantSecretKey,
                tenantPolicyName);
        createTenantBucket(endpoint, region, tenantAccessKey, tenantSecretKey, tenantBucket);
    }

    private static void createTenantPolicy(
            String endpoint,
            String region,
            String adminAccessKey,
            String adminSecretKey,
            String tenantPolicyName,
            String tenantPolicyJson)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint(endpoint)
                .region(region)
                .credentials(adminAccessKey, adminSecretKey)
                .build();
        adminCilent.addCannedPolicy(tenantPolicyName, tenantPolicyJson);
        System.out.println(" Created Policy: " + tenantPolicyName + " => " + tenantPolicyJson);
    }

    private static void createTenantUser(
            String endpoint,
            String region,
            String adminAccessKey,
            String adminSecretKey,
            String tenantAccessKey,
            String tenantSecretKey,
            String tenantPolicyName)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint(endpoint)
                .region(region)
                .credentials(adminAccessKey, adminSecretKey)
                .build();
        // Won't automatically assign policies? ? ?
        adminCilent.addUser(tenantAccessKey, Status.ENABLED, tenantSecretKey, tenantPolicyName, emptyList());
        System.out.println("   Created User: " + tenantAccessKey);
    }

    private static void assignTenantPolicyToUser(
            String endpoint,
            String region,
            String adminAccessKey,
            String adminSecretKey,
            String tenantAccessKey,
            String tenantSecretKey,
            String tenantPolicyName)
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidCipherTextException, IOException {
        MinioAdminClient adminCilent = MinioAdminClient.builder()
                .endpoint(endpoint)
                .region(region)
                .credentials(adminAccessKey, adminSecretKey)
                .build();
        adminCilent.setPolicy(tenantAccessKey, false, tenantPolicyName);
        System.out.println("Assigned Policy: " + tenantPolicyName + " => " + tenantAccessKey);
    }

    private static void createTenantBucket(
            String endpoint,
            String region,
            String tenantAccessKey,
            String tenantSecretKey,
            String tenantBucket) throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .region(region)
                .credentials(tenantAccessKey, tenantSecretKey)
                .build();
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(tenantBucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(tenantBucket).build());
            System.out.println(" Created Bucket: " + tenantBucket);
        } else {
            System.out.println(" Already Bucket: " + tenantBucket);
        }
    }

}

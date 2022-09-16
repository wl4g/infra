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

import static com.wl4g.infra.common.minio.MinioAdminClientTests.ENDPOINT;
import static com.wl4g.infra.common.minio.MinioAdminClientTests.TENANT_BUCKET;
import static com.wl4g.infra.common.minio.MinioClientTests.USER_PREFIX;
import static com.wl4g.infra.common.minio.MinioClientTests.USER_OBJECT_NAME;
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
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.wl4g.infra.common.minio.S3Policy.EffectType;
import com.wl4g.infra.common.minio.S3Policy.Statement;

import io.minio.credentials.Credentials;
import io.minio.credentials.Jwt;
import io.minio.credentials.Provider;
import io.minio.credentials.WebIdentityProvider;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * {@link MinioIdentityProviderTests}
 * 
 * @author James Wong
 * @version 2022-08-30
 * @since v3.0.0
 * @see More examples see to:
 *      {@link com.wl4g.infra.common.minio.MinioClientTests}
 */
public class MinioIdentityProviderTests {

    // public static final OkHttpClient defaultHttpClient =
    // io.minio.http.HttpUtils.newDefaultHttpClient(15_000, 15_000, 15_000);
    public static final OkHttpClient defaultHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(15_000, TimeUnit.MILLISECONDS)
            .writeTimeout(15_000, TimeUnit.MILLISECONDS)
            .readTimeout(15_000, TimeUnit.MILLISECONDS)
            .protocols(Arrays.asList(Protocol.HTTP_1_1))
            // .proxy(new java.net.Proxy(java.net.Proxy.Type.SOCKS, new
            // java.net.InetSocketAddress("localhost", 8889)))
            .build();

    // TODO
    public static Supplier<Jwt> jwtSuppiler = () -> new Jwt("access_token1111xxxxx", 7200);

    /**
     * @see https://github.com/minio/minio/blob/8.4.3/docs/sts/web-identity.md
     * @see https://github.com/minio/minio/blob/master/docs/sts/keycloak.md
     */
    @Test
    public void testGetSTSWithWebIdentity() {
        String resource = format("arn:aws:s3:::%s/%s/%s", TENANT_BUCKET, USER_PREFIX, USER_OBJECT_NAME);

        S3Policy applyStsPolicy = S3Policy.builder()
                .version(S3Policy.DEFAULT_POLICY_VERSION)
                .statement(singletonList(Statement.builder()
                        .effect(EffectType.Allow)
                        .resource(singletonList(resource))
                        .action(asList(GetBucketLocationAction, GetBucketPolicyStatusAction, ListBucketAction,
                                ListAllMyBucketsAction, ListBucketMultipartUploadsAction, ListMultipartUploadPartsAction,
                                PutObjectAction, PutObjectLegalHoldAction, GetObjectAction, GetObjectLegalHoldAction))
                        .build()))
                .build();
        int durationSeconds = 5 * 60;

        Provider provider = new WebIdentityProvider(jwtSuppiler, ENDPOINT, durationSeconds, applyStsPolicy.toString(), null, null,
                defaultHttpClient);
        Credentials credentials = provider.fetch();
        System.out.println("assume sts accessKey:" + credentials.accessKey());
        System.out.println("assume sts secretKey:" + credentials.secretKey());
        System.out.println("assume sts sessionToken:" + credentials.sessionToken());
        System.out.println("assume sts expired:" + credentials.isExpired());
        System.out.println("-------------------------------");
    }

}

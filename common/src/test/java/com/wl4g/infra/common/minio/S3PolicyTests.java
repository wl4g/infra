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

import static com.wl4g.infra.common.minio.S3Policy.Action.GetObjectAction;
import static com.wl4g.infra.common.minio.S3Policy.Action.PutObjectAction;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import org.junit.Test;

import com.wl4g.infra.common.minio.S3Policy.EffectType;
import com.wl4g.infra.common.minio.S3Policy.Statement;

/**
 * {@link S3PolicyTests}
 * 
 * @author James Wong
 * @version 2022-09-14
 * @since v3.0.0
 */
public class S3PolicyTests {

    @Test
    public void testS3PolicyToJson() {
        S3Policy policy = S3Policy.builder()
                .version(S3Policy.DEFAULT_POLICY_VERSION)
                .statement(singletonList(Statement.builder()
                        .effect(EffectType.Allow)
                        .resource(singletonList("arn:aws:s3:::bucket001/sub01/*"))
                        .action(asList(PutObjectAction, GetObjectAction))
                        .build()))
                .build();

        System.out.println(toJSONString(policy));
    }

    @Test
    public void testS3PolicyFromJson() {
        String json = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"s3:PutObject\",\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::bucket001/sub01/*\"]}]}";

        S3Policy policy = parseJSON(json, S3Policy.class);

        System.out.println(policy.getStatement().get(0).getAction());
    }
}

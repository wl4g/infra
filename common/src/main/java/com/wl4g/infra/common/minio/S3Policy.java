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
import static java.lang.String.format;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link S3Policy}
 * 
 * @author James Wong
 * @version 2022-08-31
 * @since v3.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class S3Policy {

    private @JsonProperty("Version") String version;
    private @JsonProperty("Statement") List<Statement> statement;

    @Override
    public String toString() {
        return toJSONString(this);
    }

    @Getter
    @Setter
    @SuperBuilder
    @ToString
    @NoArgsConstructor
    public static class Statement {
        private @JsonProperty("Effect") EffectType effect;
        private @JsonProperty("Action") List<Action> action;
        private @JsonProperty("Resource") List<String> resource;
    }

    public static enum EffectType {
        Allow, Deny
    }

    /**
     * see:https://github.com/minio/pkg/blob/v1.3.0/iam/policy/action.go#L30
     */
    @Getter
    @AllArgsConstructor
    public static enum Action {
        // AbortMultipartUploadAction - AbortMultipartUpload Rest API action.
        AbortMultipartUploadAction("s3:AbortMultipartUpload"),

        // CreateBucketAction - CreateBucket Rest API action.
        CreateBucketAction("s3:CreateBucket"),

        // DeleteBucketAction - DeleteBucket Rest API action.
        DeleteBucketAction("s3:DeleteBucket"),

        // ForceDeleteBucketAction - DeleteBucket Rest API action when
        // x-minio-force-delete flag
        // is specified.
        ForceDeleteBucketAction("s3:ForceDeleteBucket"),

        // DeleteBucketPolicyAction - DeleteBucketPolicy Rest API action.
        DeleteBucketPolicyAction("s3:DeleteBucketPolicy"),

        // DeleteObjectAction - DeleteObject Rest API action.
        DeleteObjectAction("s3:DeleteObject"),

        // GetBucketLocationAction - GetBucketLocation Rest API action.
        GetBucketLocationAction("s3:GetBucketLocation"),

        // GetBucketNotificationAction - GetBucketNotification Rest API action.
        GetBucketNotificationAction("s3:GetBucketNotification"),

        // GetBucketPolicyAction - GetBucketPolicy Rest API action.
        GetBucketPolicyAction("s3:GetBucketPolicy"),

        // GetObjectAction - GetObject Rest API action.
        GetObjectAction("s3:GetObject"),

        // HeadBucketAction - HeadBucket Rest API action. This action is unused
        // in minio.
        HeadBucketAction("s3:HeadBucket"),

        // ListAllMyBucketsAction - ListAllMyBuckets (List buckets) Rest API
        // action.
        ListAllMyBucketsAction("s3:ListAllMyBuckets"),

        // ListBucketAction - ListBucket Rest API action.
        ListBucketAction("s3:ListBucket"),

        // GetBucketPolicyStatusAction - Retrieves the policy status for a
        // bucket.
        GetBucketPolicyStatusAction("s3:GetBucketPolicyStatus"),

        // ListBucketVersionsAction - ListBucketVersions Rest API action.
        ListBucketVersionsAction("s3:ListBucketVersions"),

        // ListBucketMultipartUploadsAction - ListMultipartUploads Rest API
        // action.
        ListBucketMultipartUploadsAction("s3:ListBucketMultipartUploads"),

        // ListenNotificationAction - ListenNotification Rest API action.
        // This is MinIO extension.
        ListenNotificationAction("s3:ListenNotification"),

        // ListenBucketNotificationAction - ListenBucketNotification Rest API
        // action.
        // This is MinIO extension.
        ListenBucketNotificationAction("s3:ListenBucketNotification"),

        // ListMultipartUploadPartsAction - ListParts Rest API action.
        ListMultipartUploadPartsAction("s3:ListMultipartUploadParts"),

        // PutBucketLifecycleAction - PutBucketLifecycle Rest API action.
        PutBucketLifecycleAction("s3:PutLifecycleConfiguration"),

        // GetBucketLifecycleAction - GetBucketLifecycle Rest API action.
        GetBucketLifecycleAction("s3:GetLifecycleConfiguration"),

        // PutBucketNotificationAction - PutObjectNotification Rest API action.
        PutBucketNotificationAction("s3:PutBucketNotification"),

        // PutBucketPolicyAction - PutBucketPolicy Rest API action.
        PutBucketPolicyAction("s3:PutBucketPolicy"),

        // PutObjectAction - PutObject Rest API action.
        PutObjectAction("s3:PutObject"),

        // DeleteObjectVersionAction - DeleteObjectVersion Rest API action.
        DeleteObjectVersionAction("s3:DeleteObjectVersion"),

        // DeleteObjectVersionTaggingAction - DeleteObjectVersionTagging Rest
        // API action.
        DeleteObjectVersionTaggingAction("s3:DeleteObjectVersionTagging"),

        // GetObjectVersionAction - GetObjectVersionAction Rest API action.
        GetObjectVersionAction("s3:GetObjectVersion"),

        // GetObjectVersionTaggingAction - GetObjectVersionTagging Rest API
        // action.
        GetObjectVersionTaggingAction("s3:GetObjectVersionTagging"),

        // PutObjectVersionTaggingAction - PutObjectVersionTagging Rest API
        // action.
        PutObjectVersionTaggingAction("s3:PutObjectVersionTagging"),

        // BypassGovernanceRetentionAction - bypass governance retention for
        // PutObjectRetention, PutObject and DeleteObject Rest API action.
        BypassGovernanceRetentionAction("s3:BypassGovernanceRetention"),

        // PutObjectRetentionAction - PutObjectRetention Rest API action.
        PutObjectRetentionAction("s3:PutObjectRetention"),

        // GetObjectRetentionAction - GetObjectRetention, GetObject, HeadObject
        // Rest API action.
        GetObjectRetentionAction("s3:GetObjectRetention"),

        // GetObjectLegalHoldAction - GetObjectLegalHold, GetObject Rest API
        // action.
        GetObjectLegalHoldAction("s3:GetObjectLegalHold"),

        // PutObjectLegalHoldAction - PutObjectLegalHold, PutObject Rest API
        // action.
        PutObjectLegalHoldAction("s3:PutObjectLegalHold"),

        // GetBucketObjectLockConfigurationAction -
        // GetBucketObjectLockConfiguration Rest API action
        GetBucketObjectLockConfigurationAction("s3:GetBucketObjectLockConfiguration"),

        // PutBucketObjectLockConfigurationAction -
        // PutBucketObjectLockConfiguration Rest API action
        PutBucketObjectLockConfigurationAction("s3:PutBucketObjectLockConfiguration"),

        // GetBucketTaggingAction - GetBucketTagging Rest API action
        GetBucketTaggingAction("s3:GetBucketTagging"),

        // PutBucketTaggingAction - PutBucketTagging Rest API action
        PutBucketTaggingAction("s3:PutBucketTagging"),

        // GetObjectTaggingAction - Get Object Tags API action
        GetObjectTaggingAction("s3:GetObjectTagging"),

        // PutObjectTaggingAction - Put Object Tags API action
        PutObjectTaggingAction("s3:PutObjectTagging"),

        // DeleteObjectTaggingAction - Delete Object Tags API action
        DeleteObjectTaggingAction("s3:DeleteObjectTagging"),

        // PutBucketEncryptionAction - PutBucketEncryption REST API action
        PutBucketEncryptionAction("s3:PutEncryptionConfiguration"),

        // GetBucketEncryptionAction - GetBucketEncryption REST API action
        GetBucketEncryptionAction("s3:GetEncryptionConfiguration"),

        // PutBucketVersioningAction - PutBucketVersioning REST API action
        PutBucketVersioningAction("s3:PutBucketVersioning"),

        // GetBucketVersioningAction - GetBucketVersioning REST API action
        GetBucketVersioningAction("s3:GetBucketVersioning"),

        // GetReplicationConfigurationAction - GetReplicationConfiguration REST
        // API action
        GetReplicationConfigurationAction("s3:GetReplicationConfiguration"),

        // PutReplicationConfigurationAction - PutReplicationConfiguration REST
        // API action
        PutReplicationConfigurationAction("s3:PutReplicationConfiguration"),

        // ReplicateObjectAction - ReplicateObject REST API action
        ReplicateObjectAction("s3:ReplicateObject"),

        // ReplicateDeleteAction - ReplicateDelete REST API action
        ReplicateDeleteAction("s3:ReplicateDelete"),

        // ReplicateTagsAction - ReplicateTags REST API action
        ReplicateTagsAction("s3:ReplicateTags"),

        // GetObjectVersionForReplicationAction - GetObjectVersionForReplication
        // REST API action
        GetObjectVersionForReplicationAction("s3:GetObjectVersionForReplication"),

        // AllActions - all API actions
        AllActions("s3:*");

        @JsonValue
        private final String value;

        @JsonCreator
        public static Action of(String action) {
            for (Action a : values()) {
                if (a.getValue().equals(action)) {
                    return a;
                }
            }
            throw new IllegalArgumentException(format("Invalid s3 policy action for '%s'", action));
        }
    }

    public static String DEFAULT_POLICY_VERSION = "2012-10-17";

}

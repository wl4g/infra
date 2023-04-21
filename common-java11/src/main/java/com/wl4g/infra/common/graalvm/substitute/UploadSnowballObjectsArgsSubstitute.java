// @formatter:off
///*
// * MinIO Java SDK for Amazon S3 Compatible Cloud Storage, (C) 2021 MinIO, Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.wl4g.infra.common.graalvm.substitute;
//
//import java.util.Objects;
//import java.util.Random;
//
//import com.oracle.svm.core.annotate.Alias;
//import com.oracle.svm.core.annotate.Delete;
//import com.oracle.svm.core.annotate.Substitute;
//import com.oracle.svm.core.annotate.TargetClass;
//
//import io.minio.ObjectWriteArgs;
//import io.minio.SnowballObject;
//
///**
// * {@link UploadSnowballObjectsArgsSubstitute}
// * 
// * @author James Wong
// * @since v1.0.0
// */
//@TargetClass(className = "io.minio.UploadSnowballObjectsArgs")
//public class UploadSnowballObjectsArgsSubstitute extends ObjectWriteArgs {
//
//    @Delete
//    private static Random random;
//
//    @Alias
//    private Iterable<SnowballObject> objects;
//    @Alias
//    private String stagingFilename;
//    @Alias
//    private boolean compression;
//
//    @Alias
//    public Iterable<SnowballObject> objects() {
//        return this.objects;
//    }
//
//    @Alias
//    public String stagingFilename() {
//        return stagingFilename;
//    }
//
//    @Alias
//    public boolean compression() {
//        return compression;
//    }
//
//    @Alias
//    public static Builder builder() {
//        return new Builder();
//    }
//
//    /** Argument builder of {@link UploadSnowballObjectsArgsSubstitute}. */
//    @TargetClass(className = "io.minio.UploadSnowballObjectsArgs$Builder")
//    public static final class Builder extends ObjectWriteArgs.Builder<Builder, UploadSnowballObjectsArgsSubstitute> {
//
//        @Alias
//        private void validateObjects(Iterable<SnowballObject> objects) {
//            validateNotNull(objects, "objects");
//        }
//
//        @Substitute
//        @Override
//        protected void validate(UploadSnowballObjectsArgsSubstitute args) {
//            args.objectName = "snowball." + RandomDefault.getDefault().nextLong() + ".tar";
//            validateObjects(args.objects);
//            super.validate(args);
//        }
//
//        @Alias
//        public Builder objects(Iterable<SnowballObject> objects) {
//            validateObjects(objects);
//            operations.add(args -> args.objects = objects);
//            return this;
//        }
//
//        @Alias
//        public Builder stagingFilename(String stagingFilename) {
//            if (stagingFilename != null && stagingFilename.isEmpty()) {
//                throw new IllegalArgumentException("staging filename must not be empty");
//            }
//            operations.add(args -> args.stagingFilename = stagingFilename);
//            return this;
//        }
//
//        @Alias
//        public Builder compression(boolean compression) {
//            operations.add(args -> args.compression = compression);
//            return this;
//        }
//    }
//
//    @Alias
//    @Override
//    public boolean equals(Object o) {
//        if (this == o)
//            return true;
//        if (!(o instanceof UploadSnowballObjectsArgsSubstitute))
//            return false;
//        if (!super.equals(o))
//            return false;
//        UploadSnowballObjectsArgsSubstitute that = (UploadSnowballObjectsArgsSubstitute) o;
//        return Objects.equals(objects, that.objects) && Objects.equals(stagingFilename, that.stagingFilename)
//                && compression == that.compression;
//    }
//
//    @Alias
//    @Override
//    public int hashCode() {
//        return Objects.hash(super.hashCode(), objects, stagingFilename, compression);
//    }
//
//}

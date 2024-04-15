/*
 * Copyright 2023 ~ 2030 the original author or authors. James Wong <jameswong1376@gmail.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.dataformat.orc;

import lombok.NoArgsConstructor;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Properties;

/**
 * The {@link GsonOrcHolder} class provides conversion utilities between ORC and Fastjson.
 */
@NoArgsConstructor
public class GsonOrcHolder extends OrcJsonHolder {

    private static final GsonOrcHolder DEFAULT = new GsonOrcHolder();

    public static GsonOrcHolder getDefault() {
        return DEFAULT;
    }

    public GsonOrcHolder(boolean usePhysicalFsWriter,
                         @Min(0) int batchMaxSize,
                         @Nullable String timestampFormat,
                         @Nullable Properties options) {
        super(usePhysicalFsWriter, batchMaxSize, timestampFormat, options);
    }

    // ----- Get ORC schema from JSON -----

    /**
     * Get the ORC schema type for the given Fastjson json.
     *
     * @param jsonNode The json node.
     * @return The ORC schema type.
     */
    @Override
    public TypeDescription getSchemaFromJsonObject(@NotNull Object jsonNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // ----- Write ORC from JSON -----

    @Override
    protected byte[] toJsonByteArray(Object record) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // ----- Read ORC to JSON -----

    @Override
    protected RecordReader createRecordReader(FSDataInputStream orcInput,
                                              int length,
                                              TypeDescription schema,
                                              @Nullable String timestampFormat) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object createObjectJsonNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Iterable<Object> createArrayJsonNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void putObjectJsonNode(Object objectNode, String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void addArrayJsonNode(Object arrayNode, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object convertToJsonNode(TypeDescription fieldSchema, ColumnVector colVector, int row) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

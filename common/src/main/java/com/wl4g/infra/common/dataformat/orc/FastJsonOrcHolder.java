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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.DecimalColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.DoubleColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.ListColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.MapColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.StructColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.TimestampColumnVector;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.DateUtils2.formatDate;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

/**
 * The {@link FastJsonOrcHolder} class provides conversion utilities between ORC and Fastjson.
 */
public abstract class FastJsonOrcHolder extends OrcJsonHolder {
    private static final FastJsonOrcHolder INSTANCE = new FastJsonOrcHolder() {
    };

    public static FastJsonOrcHolder getInstance() {
        return INSTANCE;
    }

    protected FastJsonOrcHolder() {
    }

    // ----- Get ORC schema from JSON -----

    /**
     * Get the ORC schema type for the given Fastjson json.
     *
     * @param node The Fastjson json
     * @return The ORC schema type.
     */
    @Override
    public TypeDescription getSchemaFromJsonObject(@NotNull Object node) {
        notNullOf(node, "jsonNode");
        if (node instanceof JSONObject) {
            final TypeDescription structSchema = TypeDescription.createStruct();
            for (Map.Entry<String, Object> entry : ((JSONObject) node).entrySet()) {
                final Object subNode = entry.getValue();
                if (subNode instanceof JSONArray) {
                    structSchema.addField(entry.getKey(), getListSchemaFromArrayNode((JSONArray) subNode));
                } else if (subNode instanceof JSONObject) {
                    structSchema.addField(entry.getKey(), getSchemaFromJsonObject(subNode));
                } else {
                    structSchema.addField(entry.getKey(), getPrimitiveTypeDescription(subNode));
                }
            }
            return structSchema;
        } else if (node instanceof JSONArray) {
            return getListSchemaFromArrayNode(((JSONArray) node));
        } else {
            return getPrimitiveTypeDescription(node);
        }
    }

    /**
     * Get the ORC schema type for the given Fastjson node.
     *
     * @param node The Fastjson node
     * @return The ORC schema type
     */
    @SuppressWarnings("all")
    private TypeDescription getPrimitiveTypeDescription(Object node) {
        final Class<?> nodeType = node.getClass();
        if (node instanceof JSONObject) {
            return getSchemaFromJsonObject(node);
        } else if (node instanceof JSONArray) {
            return getListSchemaFromArrayNode((JSONArray) node);
        } else if (node instanceof Collection) {
            return getListSchemaFromArrayNode((JSONArray) node);
        } else if (node instanceof Boolean || nodeType == boolean.class) {
            return TypeDescription.createBoolean();
        } else if (node instanceof Integer || nodeType == int.class) {
            return TypeDescription.createInt();
        } else if (node instanceof Long || nodeType == long.class) {
            return TypeDescription.createLong();
        } else if (node instanceof Float || nodeType == float.class) {
            return TypeDescription.createFloat();
        } else if (node instanceof Double || nodeType == double.class) {
            return TypeDescription.createDouble();
        } else if (node instanceof String) {
            return TypeDescription.createString();
        } else if (node instanceof Date || node instanceof java.sql.Date) {
            return TypeDescription.createDate();
        } else if (node instanceof BigDecimal) {
            return TypeDescription.createDecimal();
        } else {
            //return TypeDescription.createBinary();
            throw new IllegalArgumentException("Unsupported the FastJson node type: " + node);
        }
    }

    // ----- Write ORC from JSON -----

    @Override
    protected byte[] toJsonByteArray(Object record) {
        return ((JSONObject) record).toJSONString().getBytes(UTF_8);
    }

    // ----- Read ORC to JSON -----

    @Override
    protected RecordReader createRecordReader(FSDataInputStream orcInput,
                                              int length,
                                              TypeDescription schema,
                                              @Nullable String timestampFormat) throws IOException {
        return new FastJsonReader(orcInput, length, schema, timestampFormat);
    }

    @Override
    protected Object createObjectJsonNode() {
        return new JSONObject();
    }

    @Override
    protected Iterable<Object> createArrayJsonNode() {
        return new JSONArray();
    }

    @Override
    protected void putObjectJsonNode(Object objectNode, String key, Object value) {
        ((JSONObject) objectNode).put(key, value);
    }

    @Override
    protected void addArrayJsonNode(Object arrayNode, Object value) {
        ((JSONArray) arrayNode).add(value);
    }

    @Override
    protected Object convertToJsonNode(TypeDescription fieldSchema, ColumnVector colVector, int row) {
        if (colVector.isNull[row]) {
            return null;
        }
        switch (fieldSchema.getCategory()) {
            case BOOLEAN:
                return ((LongColumnVector) colVector).vector[row] == 1;
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
                return ((LongColumnVector) colVector).vector[row];
            case FLOAT:
            case DOUBLE:
                return ((DoubleColumnVector) colVector).vector[row];
            case DECIMAL:
                return ((DecimalColumnVector) colVector).vector[row].getHiveDecimal().bigDecimalValue();
            case CHAR:
            case STRING:
                final BytesColumnVector bytesColVector = (BytesColumnVector) colVector;
                return bytesColVector.toString(row);
            case BINARY:
                final BytesColumnVector binaryColVector = (BytesColumnVector) colVector;
                final byte[] binaryBytes = binaryColVector.vector[row];
                if (nonNull(binaryBytes)) {
                    final byte[] binary = new byte[binaryColVector.length[row]];
                    System.arraycopy(binaryBytes, binaryColVector.start[row], binary, 0, binaryColVector.length[row]);
                    return binary;
                }
            case DATE:
                final Date date = new Date(((LongColumnVector) colVector).vector[row]);
                return formatDate(date, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            case TIMESTAMP:
                return ((TimestampColumnVector) colVector).getTimestampAsLong(row);
            case LIST:
                final JSONArray arrayNode = new JSONArray();
                final ColumnVector colChild = ((ListColumnVector) colVector).child;
                // first child field type is the list element fields schema.
                final TypeDescription childType = fieldSchema.getChildren().get(0);
                final int colOffset = (int) ((ListColumnVector) colVector).offsets[row];
                final int colLength = (int) ((ListColumnVector) colVector).lengths[row];
                for (int i = 0; i < colLength; i++) {
                    arrayNode.add(convertToJsonNode(childType, colChild, colOffset));
                }
                return arrayNode;
            case MAP:
                final Object mapObjectNode = createObjectJsonNode();
                final MapColumnVector mapVector = (MapColumnVector) colVector;
                final TypeDescription keySchema = fieldSchema.getChildren().get(0);
                final TypeDescription valueSchema = fieldSchema.getChildren().get(1);
                for (int i = 0; i < mapVector.lengths[row]; i++) {
                    final int elementRow = (int) mapVector.offsets[row] + i;
                    final Object keyValue = convertToJsonNode(keySchema, mapVector.keys, elementRow);
                    final Object valueValue = convertToJsonNode(valueSchema, mapVector.values, elementRow);
                    assert keyValue != null;
                    putObjectJsonNode(mapObjectNode, keyValue.toString(), valueValue);
                }
                return mapObjectNode;
            case STRUCT:
                final Object structObjectNode = createObjectJsonNode();
                final StructColumnVector structVector = (StructColumnVector) colVector;
                final List<TypeDescription> childSchemas = fieldSchema.getChildren();
                final List<String> fieldNames = fieldSchema.getFieldNames();
                for (int i = 0; i < childSchemas.size(); i++) {
                    putObjectJsonNode(structObjectNode, fieldNames.get(i), convertToJsonNode(childSchemas.get(i), structVector.fields[i], row));
                }
                return structObjectNode;
            default:
                throw new IllegalArgumentException("Unknown the field schema " + fieldSchema);
        }
    }

}

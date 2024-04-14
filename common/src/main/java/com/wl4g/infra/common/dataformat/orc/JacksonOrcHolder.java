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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.wl4g.infra.common.lang.Assert2.isInstanceOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.DateUtils2.formatDate;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

/**
 * The {@link JacksonOrcHolder} class provides conversion utilities between ORC and Fastjson.
 */
public abstract class JacksonOrcHolder extends OrcJsonHolder {
    private static final JacksonOrcHolder INSTANCE = new JacksonOrcHolder() {
    };

    public static JacksonOrcHolder getInstance() {
        return INSTANCE;
    }

    protected JacksonOrcHolder() {
    }

    // ----- Get ORC schema from JSON -----

    /**
     * Get the ORC schema type for the given Fastjson json.
     *
     * @param node The json node.
     * @return The ORC schema type.
     */
    @Override
    public TypeDescription getSchemaFromJsonObject(@NotNull Object node) {
        notNullOf(node, "node");
        isInstanceOf(JsonNode.class, node, "node must instance of JsonNode");

        final JsonNode jsonNode = (JsonNode) node;
        if (jsonNode.isObject()) {
            final TypeDescription structSchema = TypeDescription.createStruct();
            final Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                final Map.Entry<String, JsonNode> field = fields.next();
                structSchema.addField(field.getKey(), getSchemaFromJsonObject(field.getValue()));
            }
            return structSchema;
        } else if (jsonNode.isArray()) {
            return getListSchemaFromArrayNode(jsonNode);
        } else {
            return getPrimitiveTypeDescription(jsonNode);
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
        final JsonNode jsonNode = (JsonNode) node;
        switch (jsonNode.getNodeType()) {
            case NUMBER:
                if (jsonNode.isLong() || jsonNode.isInt() || jsonNode.isShort() || jsonNode.isBigInteger()) {
                    return TypeDescription.createLong();
                } else {
                    return TypeDescription.createDouble();
                }
            case STRING:
                return TypeDescription.createString();
            case BOOLEAN:
                return TypeDescription.createBoolean();
            case BINARY:
                return TypeDescription.createBinary();
            case NULL:
                // Null values are not supported in ORC schema
                throw new IllegalArgumentException("Null values are not supported in ORC schema");
            default:
                throw new IllegalArgumentException("Unsupported JSON node type: " + jsonNode.getNodeType());
        }
    }

    // ----- Write ORC from JSON -----

    @Override
    protected byte[] toJsonByteArray(Object record) {
        return record.toString().getBytes(UTF_8);
    }

    // ----- Read ORC to JSON -----

    @Override
    protected RecordReader createRecordReader(FSDataInputStream orcInput,
                                              int length,
                                              TypeDescription schema,
                                              @Nullable String timestampFormat) throws IOException {
        return new JacksonReader(orcInput, length, schema, timestampFormat);
    }

    @Override
    protected Object createObjectJsonNode() {
        return JsonNodeFactory.instance.objectNode();
    }

    @SuppressWarnings("all")
    @Override
    protected Iterable createArrayJsonNode() {
        return JsonNodeFactory.instance.arrayNode();
    }

    @Override
    protected void putObjectJsonNode(Object objectNode, String key, Object value) {
        ((ObjectNode) objectNode).set(key, (JsonNode) value);
    }

    @Override
    protected void addArrayJsonNode(Object arrayNode, Object value) {
        ((ArrayNode) arrayNode).add((JsonNode) value);
    }

    @Override
    protected JsonNode convertToJsonNode(TypeDescription fieldSchema, ColumnVector colVector, int row) {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        if (colVector.isNull[row]) {
            return factory.nullNode();
        }
        switch (fieldSchema.getCategory()) {
            case BOOLEAN:
                return factory.booleanNode(((LongColumnVector) colVector).vector[row] == 1);
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
                return factory.numberNode(((LongColumnVector) colVector).vector[row]);
            case FLOAT:
            case DOUBLE:
                return factory.numberNode(((DoubleColumnVector) colVector).vector[row]);
            case DECIMAL:
                return factory.numberNode(((DecimalColumnVector) colVector).vector[row].getHiveDecimal().bigDecimalValue());
            case CHAR:
            case STRING:
                final BytesColumnVector bytesColVector = (BytesColumnVector) colVector;
                return factory.textNode(bytesColVector.toString(row));
            case BINARY:
                final BytesColumnVector binaryColVector = (BytesColumnVector) colVector;
                final byte[] binaryBytes = binaryColVector.vector[row];
                if (nonNull(binaryBytes)) {
                    return factory.binaryNode(binaryColVector.vector[row], binaryColVector.start[row], binaryColVector.length[row]);
                }
            case DATE:
                final Date date = new Date(((LongColumnVector) colVector).vector[row]);
                return factory.textNode(formatDate(date, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
            case TIMESTAMP:
                return factory.numberNode(((TimestampColumnVector) colVector).getTimestampAsLong(row));
            case LIST:
                final ArrayNode arrayNode = factory.arrayNode();
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
                final ObjectNode mapObjectNode = factory.objectNode();
                final MapColumnVector mapVector = (MapColumnVector) colVector;
                final TypeDescription keySchema = fieldSchema.getChildren().get(0);
                final TypeDescription valueSchema = fieldSchema.getChildren().get(1);
                for (int i = 0; i < mapVector.lengths[row]; i++) {
                    final int elementRow = (int) mapVector.offsets[row] + i;
                    final JsonNode keyValue = convertToJsonNode(keySchema, mapVector.keys, elementRow);
                    final JsonNode valueValue = convertToJsonNode(valueSchema, mapVector.values, elementRow);
                    mapObjectNode.set(keyValue.asText(), valueValue);
                }
                return mapObjectNode;
            case STRUCT:
                final ObjectNode structObjectNode = factory.objectNode();
                final StructColumnVector structVector = (StructColumnVector) colVector;
                final List<TypeDescription> childSchemas = fieldSchema.getChildren();
                final List<String> fieldNames = fieldSchema.getFieldNames();
                for (int i = 0; i < childSchemas.size(); i++) {
                    final JsonNode childNode = convertToJsonNode(childSchemas.get(i), structVector.fields[i], row);
                    structObjectNode.set(fieldNames.get(i), childNode);
                }
                return structObjectNode;
            default:
                throw new IllegalArgumentException("Unknown the fieldSchema " + fieldSchema);
        }
    }

}

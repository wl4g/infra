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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.DecimalColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.DoubleColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.ListColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.MapColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.StructColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.TimestampColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAccessor;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.threeten.bp.format.DateTimeFormatter.ofPattern;

/**
 * The Jackson reader implementation for ORC data format.
 *
 * @author James Wong
 * @since 1.0
 */
@Getter
public class JacksonReader implements RecordReader {
    private final static String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private final static ObjectMapper defaultObjectMapper = new ObjectMapper();
    private final static JsonFactory defaultJsonFactory = new JsonFactory();

    private final ObjectMapper objectMapper;
    private final TypeDescription schema;
    private final JsonParser parser;
    private final JsonConverter[] converters;
    private final long totalSize;
    private final FSDataInputStream input;
    private final DateTimeFormatter dateTimeFormatter;
    private long rowNumber;

    public JacksonReader(@NotNull FSDataInputStream underlying,
                         @Min(0) long totalSize,
                         @NotNull TypeDescription schema,
                         @Nullable String timestampFormat) throws IOException {
        this(defaultObjectMapper, defaultJsonFactory.createParser(new InputStreamReader(underlying.getWrappedStream(), UTF_8)),
                underlying, totalSize, schema, timestampFormat);
    }

    @SuppressWarnings("ConstantConditions")
    public JacksonReader(@Nullable ObjectMapper objectMapper,
                         @NotNull JsonParser parser,
                         @NotNull FSDataInputStream underlying,
                         @Min(0) long totalSize,
                         @NotNull TypeDescription schema,
                         @Nullable String timestampFormat) {
        notNullOf(parser, "parser");
        notNullOf(underlying, "underlying");
        notNullOf(schema, "schema");
        isTrueOf(totalSize >= 0, "totalSize must be greater than or equal to 0");

        this.objectMapper = objectMapper;
        this.rowNumber = 0L;
        this.schema = schema;
        if (schema.getCategory() != TypeDescription.Category.STRUCT) {
            throw new IllegalArgumentException("Root must be struct - " + schema);
        }
        this.input = underlying;
        this.totalSize = totalSize;
        this.parser = parser;
        this.dateTimeFormatter = isBlank(timestampFormat) ? ofPattern(DEFAULT_TIMESTAMP_FORMAT) : ofPattern(timestampFormat);

        final List<TypeDescription> fieldTypes = schema.getChildren();
        this.converters = new JsonConverter[fieldTypes.size()];

        for (int c = 0; c < this.converters.length; ++c) {
            this.converters[c] = createConverter(fieldTypes.get(c));
        }
    }

    private JsonConverter createConverter(TypeDescription schema) {
        switch (schema.getCategory()) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
                return new LongColumnConverter();
            case FLOAT:
            case DOUBLE:
                return new DoubleColumnConverter();
            case CHAR:
            case VARCHAR:
            case STRING:
                return new StringColumnConverter();
            case DECIMAL:
                return new DecimalColumnConverter();
            case TIMESTAMP:
                return new TimestampColumnConverter();
            case BINARY:
                return new BinaryColumnConverter();
            case BOOLEAN:
                return new BooleanColumnConverter();
            case STRUCT:
                return new StructColumnConverter(schema);
            case LIST:
                return new ListColumnConverter(schema);
            case MAP:
                return new MapColumnConverter(schema);
            default:
                throw new IllegalArgumentException("Unhandled type " + schema);
        }
    }

    @Override
    public boolean nextBatch(VectorizedRowBatch rowBatch) throws IOException {
        rowBatch.reset();
        final int maxSize = rowBatch.getMaxSize();

        for (List<String> fieldNames = schema.getFieldNames(); nonNull(parser.nextToken()) && rowBatch.size < maxSize; ++rowBatch.size) {
            final JsonNode record = objectMapper.readTree(parser);
            for (int c = 0; c < converters.length; ++c) {
                final JsonNode field = record.get(fieldNames.get(c));
                if (isNull(field)) {
                    rowBatch.cols[c].noNulls = false;
                    rowBatch.cols[c].isNull[rowBatch.size] = true;
                } else {
                    converters[c].convert(field, rowBatch.cols[c], rowBatch.size);
                }
            }
        }

        this.rowNumber += rowBatch.size;
        return rowBatch.size != 0; // There is added data in this batch?
    }

    @Override
    public float getProgress() throws IOException {
        long pos = this.input.getPos();
        return this.totalSize != 0L && pos < this.totalSize ? (float) pos / (float) this.totalSize : 1.0F;
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }

    @Override
    public void seekToRow(long rowCount) {
        throw new UnsupportedOperationException("Seek is not supported by JacksonReader");
    }

    private class MapColumnConverter implements JsonConverter {
        private final JsonConverter keyConverter;
        private final JsonConverter valueConverter;

        public MapColumnConverter(TypeDescription schema) {
            final TypeDescription keyType = schema.getChildren().get(0);
            if (keyType.getCategory() != TypeDescription.Category.STRING) {
                throw new IllegalArgumentException("JSON can only support MAP key in STRING type: " + schema);
            }
            this.keyConverter = createConverter(keyType);
            this.valueConverter = createConverter(schema.getChildren().get(1));
        }

        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                final MapColumnVector vector = (MapColumnVector) vec;
                final Iterator<String> fieldNames = value.fieldNames();
                vector.lengths[row] = value.size();
                vector.offsets[row] = vector.childCount;
                vector.childCount = (int) ((long) vector.childCount + vector.lengths[row]);
                vector.keys.ensureSize(vector.childCount, true);
                vector.values.ensureSize(vector.childCount, true);
                int cnt = 0;
                while (fieldNames.hasNext()) {
                    String key = fieldNames.next();
                    JsonNode elem = value.get(key);
                    int offset = (int) vector.offsets[row] + cnt++;
                    this.keyConverter.convert(defaultObjectMapper.valueToTree(key), vector.keys, offset);
                    this.valueConverter.convert(elem, vector.values, offset);
                }
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private class ListColumnConverter implements JsonConverter {
        private final JsonConverter childrenConverter;

        public ListColumnConverter(TypeDescription schema) {
            this.childrenConverter = createConverter(schema.getChildren().get(0));
        }

        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                final ListColumnVector vector = (ListColumnVector) vec;
                final ArrayNode arrayNode = (ArrayNode) value;
                vector.lengths[row] = arrayNode.size();
                vector.offsets[row] = vector.childCount;
                vector.childCount = (int) ((long) vector.childCount + vector.lengths[row]);
                vector.child.ensureSize(vector.childCount, true);

                for (int c = 0; c < arrayNode.size(); ++c) {
                    this.childrenConverter.convert(arrayNode.get(c), vector.child, (int) vector.offsets[row] + c);
                }
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private class StructColumnConverter implements JsonConverter {
        private final JsonConverter[] childrenConverters;
        private final List<String> fieldNames;

        public StructColumnConverter(TypeDescription schema) {
            final List<TypeDescription> kids = schema.getChildren();
            this.childrenConverters = new JsonConverter[kids.size()];

            for (int c = 0; c < this.childrenConverters.length; ++c) {
                this.childrenConverters[c] = createConverter(kids.get(c));
            }

            this.fieldNames = schema.getFieldNames();
        }

        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                final StructColumnVector vector = (StructColumnVector) vec;
                final ObjectNode obj = (ObjectNode) value;

                for (int c = 0; c < this.childrenConverters.length; ++c) {
                    JsonNode elem = obj.get(this.fieldNames.get(c));
                    this.childrenConverters[c].convert(elem, vector.fields[c], row);
                }
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class DecimalColumnConverter implements JsonConverter {
        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                DecimalColumnVector vector = (DecimalColumnVector) vec;
                vector.vector[row].set(HiveDecimal.create(value.asText()));
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private class TimestampColumnConverter implements JsonConverter {
        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                final TimestampColumnVector vector = (TimestampColumnVector) vec;
                final TemporalAccessor temporalAccessor = dateTimeFormatter.parseBest(value.asText(),
                        ZonedDateTime.FROM, LocalDateTime.FROM);
                ZonedDateTime tz;
                Timestamp timestamp;
                if (temporalAccessor instanceof ZonedDateTime) {
                    tz = (ZonedDateTime) temporalAccessor;
                    timestamp = new Timestamp(tz.toEpochSecond() * 1000L);
                    timestamp.setNanos(tz.getNano());
                    vector.set(row, timestamp);
                } else if (temporalAccessor instanceof LocalDateTime) {
                    tz = ((LocalDateTime) temporalAccessor).atZone(ZoneId.systemDefault());
                    timestamp = new Timestamp(tz.toEpochSecond() * 1000L);
                    timestamp.setNanos(tz.getNano());
                    vector.set(row, timestamp);
                } else {
                    vec.noNulls = false;
                    vec.isNull[row] = true;
                }
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class BinaryColumnConverter implements JsonConverter {
        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                final BytesColumnVector vector = (BytesColumnVector) vec;
                final String binStr = value.asText();
                final byte[] bytes = new byte[binStr.length() / 2];

                for (int i = 0; i < bytes.length; ++i) {
                    bytes[i] = (byte) Integer.parseInt(binStr.substring(i * 2, i * 2 + 2), 16);
                }
                vector.setRef(row, bytes, 0, bytes.length);
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class StringColumnConverter implements JsonConverter {
        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                BytesColumnVector vector = (BytesColumnVector) vec;
                byte[] bytes = value.asText().getBytes();
                vector.setRef(row, bytes, 0, bytes.length);
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class DoubleColumnConverter implements JsonConverter {
        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                final DoubleColumnVector vector = (DoubleColumnVector) vec;
                vector.vector[row] = value.asDouble();
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class LongColumnConverter implements JsonConverter {
        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                final LongColumnVector vector = (LongColumnVector) vec;
                vector.vector[row] = value.asLong();
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class BooleanColumnConverter implements JsonConverter {
        @Override
        public void convert(JsonNode value, ColumnVector vec, int row) {
            if (value != null && !value.isNull()) {
                LongColumnVector vector = (LongColumnVector) vec;
                vector.vector[row] = value.asBoolean() ? 1L : 0L;
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private interface JsonConverter {
        void convert(JsonNode value, ColumnVector vec, int row);
    }

}
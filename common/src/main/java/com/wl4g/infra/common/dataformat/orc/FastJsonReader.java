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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.wl4g.infra.common.dataformat.orc.OrcJsonHolder.DEFAULT_DATE_FORMATTER;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.threeten.bp.LocalDateTime.FROM;
import static org.threeten.bp.format.DateTimeFormatter.ofPattern;

/**
 * The Fastjson reader implementation for ORC data format.
 *
 * @author James Wong
 * @since 1.0
 */
@Slf4j
@Getter
public class FastJsonReader implements RecordReader {
    private final TypeDescription schema;
    private final Iterator<JSONObject> parser;
    private final JsonConverter[] converters;
    private final long totalSize;
    private final FSDataInputStream input;
    private final DateTimeFormatter dateTimeFormatter;
    private long rowNumber;

    @SuppressWarnings("ConstantConditions")
    public FastJsonReader(@NotNull FSDataInputStream underlying,
                          @Min(0) long totalSize,
                          @NotNull TypeDescription schema,
                          @Nullable String timestampFormat) throws IOException {
        notNullOf(underlying, "underlying");
        notNullOf(schema, "schema");
        isTrueOf(totalSize >= 0, "totalSize must be greater than or equal to 0");

        final List<JSONObject> records = new ArrayList<>((int) totalSize);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(underlying, UTF_8))) {
            String line;
            while (nonNull((line = reader.readLine()))) {
                records.add(JSON.parseObject(line));
            }
        }
        this.parser = records.iterator();
        this.input = underlying;
        this.schema = schema;
        if (schema.getCategory() != TypeDescription.Category.STRUCT) {
            throw new IllegalArgumentException("Root must be struct - " + schema);
        }
        this.totalSize = totalSize;
        this.dateTimeFormatter = isBlank(timestampFormat) ? ofPattern(DEFAULT_DATE_FORMATTER) :
                ofPattern(timestampFormat);
        List<TypeDescription> fieldTypes = schema.getChildren();
        this.converters = new JsonConverter[fieldTypes.size()];

        for (int c = 0; c < this.converters.length; ++c) {
            this.converters[c] = createConverter(fieldTypes.get(c));
        }
        this.rowNumber = 0L;
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
            case DECIMAL:
                return new DecimalColumnConverter();
            case CHAR:
            case VARCHAR:
            case STRING:
                return new StringColumnConverter();
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
    public boolean nextBatch(VectorizedRowBatch rowBatch) {
        rowBatch.reset();
        final int maxSize = rowBatch.getMaxSize();

        for (List<String> fieldNames = schema.getFieldNames(); parser.hasNext() && rowBatch.size < maxSize; ++rowBatch.size) {
            final JSONObject record = parser.next();
            for (int c = 0; c < converters.length; ++c) {
                final Object field = record.get(fieldNames.get(c));
                if (isNull(field)) {
                    rowBatch.cols[c].noNulls = false;
                    rowBatch.cols[c].isNull[rowBatch.size] = true;
                } else {
                    converters[c].convert(field, rowBatch.cols[c], rowBatch.size);
                }
            }
        }

        this.rowNumber += rowBatch.size;
        return rowBatch.size != 0; // There is added data in this batch
    }

    @Override
    public float getProgress() throws IOException {
        long pos = this.input != null ? this.input.getPos() : 0;
        return this.totalSize != 0L && pos < this.totalSize ? (float) pos / (float) this.totalSize : 1.0F;
    }

    @Override
    public void close() throws IOException {
        if (this.input != null) {
            this.input.close();
        }
    }

    @Override
    public void seekToRow(long rowCount) {
        throw new UnsupportedOperationException("Seek is not supported by FastjsonReader");
    }

    private class MapColumnConverter implements JsonConverter {
        private final JsonConverter keyConverter;
        private final JsonConverter valueConverter;

        public MapColumnConverter(TypeDescription schema) {
            final TypeDescription keyType = schema.getChildren().get(0);
            if (!keyType.getCategory().equals(TypeDescription.Category.STRING)) {
                throw new IllegalArgumentException("Fastjson can only support MAP key in STRING type: " + schema);
            }
            this.keyConverter = createConverter(keyType);
            this.valueConverter = createConverter(schema.getChildren().get(1));
        }

        @Override
        public void convert(Object value, ColumnVector vec, int row) {
            if (value instanceof JSONObject) {
                final MapColumnVector vector = (MapColumnVector) vec;
                final JSONObject map = (JSONObject) value;
                vector.lengths[row] = map.size();
                vector.offsets[row] = vector.childCount;
                vector.childCount = (int) ((long) vector.childCount + vector.lengths[row]);
                vector.keys.ensureSize(vector.childCount, true);
                vector.values.ensureSize(vector.childCount, true);
                int cnt = 0;
                for (String key : map.keySet()) {
                    Object elem = map.get(key);
                    int offset = (int) vector.offsets[row] + cnt++;
                    this.keyConverter.convert(key, vector.keys, offset);
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
        public void convert(Object value, ColumnVector vec, int row) {
            if (value instanceof JSONArray) {
                final ListColumnVector vector = (ListColumnVector) vec;
                final JSONArray array = (JSONArray) value;
                vector.lengths[row] = array.size();
                vector.offsets[row] = vector.childCount;
                vector.childCount = (int) ((long) vector.childCount + vector.lengths[row]);
                vector.child.ensureSize(vector.childCount, true);

                for (int c = 0; c < array.size(); ++c) {
                    this.childrenConverter.convert(array.get(c), vector.child, (int) vector.offsets[row] + c);
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
        public void convert(Object value, ColumnVector vec, int row) {
            if (value instanceof JSONObject) {
                final StructColumnVector vector = (StructColumnVector) vec;
                final JSONObject obj = (JSONObject) value;

                for (int c = 0; c < this.childrenConverters.length; ++c) {
                    final String fieldName = fieldNames.get(c);
                    final Object elem = obj.get(fieldName);
                    if (isNull(elem)) {
                        vec.isNull[row] = true;
                    } else {
                        this.childrenConverters[c].convert(elem, vector.fields[c], row);
                    }
                }
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class DecimalColumnConverter implements JsonConverter {
        @Override
        public void convert(Object value, ColumnVector vec, int row) {
            if (nonNull(value)) {
                final DecimalColumnVector vector = (DecimalColumnVector) vec;
                vector.vector[row].set(HiveDecimal.create(value.toString()));
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private class TimestampColumnConverter implements JsonConverter {
        @Override
        public void convert(Object value, ColumnVector vec, int row) {
            if (nonNull(value)) {
                final TimestampColumnVector vector = (TimestampColumnVector) vec;
                final TemporalAccessor temporal = dateTimeFormatter.parseBest(value.toString(), ZonedDateTime.FROM, FROM);
                ZonedDateTime tz;
                Timestamp timestamp;
                if (temporal instanceof ZonedDateTime) {
                    tz = (ZonedDateTime) temporal;
                    timestamp = new Timestamp(tz.toEpochSecond() * 1000L);
                    timestamp.setNanos(tz.getNano());
                    vector.set(row, timestamp);
                } else if (temporal instanceof LocalDateTime) {
                    tz = ((LocalDateTime) temporal).atZone(ZoneId.systemDefault());
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
        public void convert(Object value, ColumnVector vec, int row) {
            if (nonNull(value)) {
                final BytesColumnVector vector = (BytesColumnVector) vec;
                final byte[] bytes = value.toString().getBytes(UTF_8);
                vector.setRef(row, bytes, 0, bytes.length);
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class StringColumnConverter implements JsonConverter {
        @Override
        public void convert(Object value, ColumnVector vec, int row) {
            if (nonNull(value)) {
                final BytesColumnVector vector = (BytesColumnVector) vec;
                final byte[] bytes = value.toString().getBytes();
                vector.setRef(row, bytes, 0, bytes.length);
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class DoubleColumnConverter implements JsonConverter {
        @Override
        public void convert(Object value, ColumnVector vec, int row) {
            if (nonNull(value)) {
                final DoubleColumnVector vector = (DoubleColumnVector) vec;
                if (value instanceof Float) {
                    vector.vector[row] = parseFloat(value.toString());
                } else if (value instanceof Double) {
                    vector.vector[row] = parseDouble(value.toString());
                } else {
                    log.debug("Invalid double value: " + value);
                }
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class LongColumnConverter implements JsonConverter {
        @Override
        public void convert(Object value, ColumnVector vec, int row) {
            if (nonNull(value)) {
                final LongColumnVector vector = (LongColumnVector) vec;
                if (value instanceof Integer) {
                    vector.vector[row] = (Integer) value;
                } else if (value instanceof Long) {
                    vector.vector[row] = (Long) value;
                } else {
                    log.debug("Invalid long value: " + value);
                }
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private static class BooleanColumnConverter implements JsonConverter {
        @Override
        public void convert(Object value, ColumnVector vec, int row) {
            if (nonNull(value)) {
                LongColumnVector vector = (LongColumnVector) vec;
                vector.vector[row] = (boolean) value ? 1L : 0L;
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }
        }
    }

    private interface JsonConverter {
        void convert(Object value, ColumnVector vec, int row);
    }

}




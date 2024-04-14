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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;
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
import org.apache.orc.TypeDescription.Category;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.TemporalAccessor;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.threeten.bp.format.DateTimeFormatter.ofPattern;

/**
 * The {@link GsonReader} class is a record reader for reading JSON data.
 *
 * @author James Wong
 * @since 1.0
 */
public class GsonReader implements RecordReader {
    private final TypeDescription schema;
    private final Iterator<JsonElement> parser;
    private final JsonConverter[] converters;
    private final long totalSize;
    private final FSDataInputStream input;
    @Getter
    private long rowNumber;
    private final DateTimeFormatter dateTimeFormatter;

    JsonConverter createConverter(TypeDescription schema) {
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

    public GsonReader(Reader reader, FSDataInputStream underlying, long size, TypeDescription schema,
                      String timestampFormat) {
        this(new JsonStreamParser(reader), underlying, size, schema, timestampFormat);
    }

    public GsonReader(Iterator<JsonElement> parser,
                      FSDataInputStream underlying,
                      long size,
                      TypeDescription schema,
                      String timestampFormat) {
        this.rowNumber = 0L;
        this.schema = schema;
        if (schema.getCategory() != Category.STRUCT) {
            throw new IllegalArgumentException("Root must be struct - " + schema);
        } else {
            this.input = underlying;
            this.totalSize = size;
            this.parser = parser;
            this.dateTimeFormatter = ofPattern(timestampFormat);
            List<TypeDescription> fieldTypes = schema.getChildren();
            this.converters = new JsonConverter[fieldTypes.size()];

            for (int c = 0; c < this.converters.length; ++c) {
                this.converters[c] = this.createConverter(fieldTypes.get(c));
            }

        }
    }

    public boolean nextBatch(VectorizedRowBatch batch) {
        batch.reset();
        final int maxSize = batch.getMaxSize();

        for (List<String> fieldNames = schema.getFieldNames(); parser.hasNext() && batch.size < maxSize; ++batch.size) {
            final JsonObject record = parser.next().getAsJsonObject();
            for (int c = 0; c < converters.length; ++c) {
                final JsonElement field = record.get(fieldNames.get(c));
                if (isNull(field)) {
                    batch.cols[c].noNulls = false;
                    batch.cols[c].isNull[batch.size] = true;
                } else {
                    converters[c].convert(field, batch.cols[c], batch.size);
                }
            }
        }

        this.rowNumber += batch.size;
        return batch.size != 0;
    }

    public float getProgress() throws IOException {
        long pos = this.input.getPos();
        return this.totalSize != 0L && pos < this.totalSize ? (float) pos / (float) this.totalSize : 1.0F;
    }

    public void close() throws IOException {
        this.input.close();
    }

    public void seekToRow(long rowCount) {
        throw new UnsupportedOperationException("Seek is not supported by JsonReader");
    }

    class MapColumnConverter implements JsonConverter {
        private final JsonConverter keyConverter;
        private final JsonConverter valueConverter;

        public MapColumnConverter(TypeDescription schema) {
            TypeDescription keyType = schema.getChildren().get(0);
            if (keyType.getCategory() != Category.STRING) {
                throw new IllegalArgumentException("JSON can only support MAP key in STRING type: " + schema);
            } else {
                this.keyConverter = GsonReader.this.createConverter(keyType);
                this.valueConverter = GsonReader.this.createConverter(schema.getChildren().get(1));
            }
        }

        public void convert(JsonElement value, ColumnVector vect, int row) {
            if (value != null && !value.isJsonNull()) {
                MapColumnVector vector = (MapColumnVector) vect;
                JsonObject obj = value.getAsJsonObject();
                vector.lengths[row] = obj.entrySet().size();
                vector.offsets[row] = vector.childCount;
                vector.childCount = (int) ((long) vector.childCount + vector.lengths[row]);
                vector.keys.ensureSize(vector.childCount, true);
                vector.values.ensureSize(vector.childCount, true);
                Iterator<Map.Entry<String, JsonElement>> it = obj.entrySet().iterator();
                int cnt = 0;
                while (it.hasNext()) {
                    Map.Entry<String, JsonElement> entry = it.next();
                    int offset = (int) vector.offsets[row] + cnt++;
                    this.keyConverter.convert(new JsonPrimitive(entry.getKey()), vector.keys, offset);
                    this.valueConverter.convert(entry.getValue(), vector.values, offset);
                }
            } else {
                vect.noNulls = false;
                vect.isNull[row] = true;
            }

        }
    }

    class ListColumnConverter implements JsonConverter {
        private final JsonConverter childrenConverter;

        public ListColumnConverter(TypeDescription schema) {
            this.childrenConverter = GsonReader.this.createConverter(schema.getChildren().get(0));
        }

        public void convert(JsonElement value, ColumnVector vect, int row) {
            if (value != null && !value.isJsonNull()) {
                ListColumnVector vector = (ListColumnVector) vect;
                JsonArray obj = value.getAsJsonArray();
                vector.lengths[row] = obj.size();
                vector.offsets[row] = vector.childCount;
                vector.childCount = (int) ((long) vector.childCount + vector.lengths[row]);
                vector.child.ensureSize(vector.childCount, true);

                for (int c = 0; c < obj.size(); ++c) {
                    this.childrenConverter.convert(obj.get(c), vector.child, (int) vector.offsets[row] + c);
                }
            } else {
                vect.noNulls = false;
                vect.isNull[row] = true;
            }

        }
    }

    class StructColumnConverter implements JsonConverter {
        private final JsonConverter[] childrenConverters;
        private final List<String> fieldNames;

        public StructColumnConverter(TypeDescription schema) {
            List<TypeDescription> kids = schema.getChildren();
            this.childrenConverters = new JsonConverter[kids.size()];

            for (int c = 0; c < this.childrenConverters.length; ++c) {
                this.childrenConverters[c] = GsonReader.this.createConverter(kids.get(c));
            }

            this.fieldNames = schema.getFieldNames();
        }

        public void convert(JsonElement value, ColumnVector vect, int row) {
            if (value != null && !value.isJsonNull()) {
                StructColumnVector vector = (StructColumnVector) vect;
                JsonObject obj = value.getAsJsonObject();

                for (int c = 0; c < this.childrenConverters.length; ++c) {
                    JsonElement elem = obj.get(this.fieldNames.get(c));
                    this.childrenConverters[c].convert(elem, vector.fields[c], row);
                }
            } else {
                vect.noNulls = false;
                vect.isNull[row] = true;
            }
        }
    }

    static class DecimalColumnConverter implements JsonConverter {
        DecimalColumnConverter() {
        }

        public void convert(JsonElement value, ColumnVector vect, int row) {
            if (value != null && !value.isJsonNull()) {
                DecimalColumnVector vector = (DecimalColumnVector) vect;
                vector.vector[row].set(HiveDecimal.create(value.getAsString()));
            } else {
                vect.noNulls = false;
                vect.isNull[row] = true;
            }

        }
    }

    class TimestampColumnConverter implements JsonConverter {
        TimestampColumnConverter() {
        }

        public void convert(JsonElement value, ColumnVector vect, int row) {
            if (value != null && !value.isJsonNull()) {
                TimestampColumnVector vector = (TimestampColumnVector) vect;
                TemporalAccessor temporalAccessor = GsonReader.this.dateTimeFormatter.parseBest(value.getAsString(),
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
                    vect.noNulls = false;
                    vect.isNull[row] = true;
                }
            } else {
                vect.noNulls = false;
                vect.isNull[row] = true;
            }

        }
    }

    static class BinaryColumnConverter implements JsonConverter {
        BinaryColumnConverter() {
        }

        public void convert(JsonElement value, ColumnVector vec, int row) {
            if (value != null && !value.isJsonNull()) {
                BytesColumnVector vector = (BytesColumnVector) vec;
                String binStr = value.getAsString();
                byte[] bytes = new byte[binStr.length() / 2];

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

    static class StringColumnConverter implements JsonConverter {
        StringColumnConverter() {
        }

        public void convert(JsonElement value, ColumnVector vect, int row) {
            if (value != null && !value.isJsonNull()) {
                BytesColumnVector vector = (BytesColumnVector) vect;
                byte[] bytes = value.getAsString().getBytes(StandardCharsets.UTF_8);
                vector.setRef(row, bytes, 0, bytes.length);
            } else {
                vect.noNulls = false;
                vect.isNull[row] = true;
            }

        }
    }

    static class DoubleColumnConverter implements JsonConverter {
        DoubleColumnConverter() {
        }

        public void convert(JsonElement value, ColumnVector vec, int row) {
            if (value != null && !value.isJsonNull()) {
                DoubleColumnVector vector = (DoubleColumnVector) vec;
                vector.vector[row] = value.getAsDouble();
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }

        }
    }

    static class LongColumnConverter implements JsonConverter {
        LongColumnConverter() {
        }

        public void convert(JsonElement value, ColumnVector vec, int row) {
            if (value != null && !value.isJsonNull()) {
                LongColumnVector vector = (LongColumnVector) vec;
                vector.vector[row] = value.getAsLong();
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }

        }
    }

    static class BooleanColumnConverter implements JsonConverter {
        BooleanColumnConverter() {
        }

        public void convert(JsonElement value, ColumnVector vec, int row) {
            if (value != null && !value.isJsonNull()) {
                LongColumnVector vector = (LongColumnVector) vec;
                vector.vector[row] = value.getAsBoolean() ? 1L : 0L;
            } else {
                vec.noNulls = false;
                vec.isNull[row] = true;
            }

        }
    }

    interface JsonConverter {
        void convert(JsonElement var1, ColumnVector var2, int var3);
    }
}

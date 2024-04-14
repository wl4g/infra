/**
 * Copyright (C) 2023 ~ 2035 the original authors WL4G (James Wong).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.dataformat.orc;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Files;
import com.wl4g.infra.common.io.FileIOUtils;
import com.wl4g.infra.common.math.Maths;
import com.wl4g.infra.common.dataformat.DataFormatTestsSupport;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.DoubleColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.TimestampColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcConf;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.wl4g.infra.common.serialize.JacksonUtils.parseToNode;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The {@link OrcSimpleCompressTests}
 *
 * @author James Wong
 * @since v1.0
 **/
//@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class OrcSimpleCompressTests extends DataFormatTestsSupport {
    private final static List<File> testFiles = new ArrayList<>();
    private static File orcFile;

    @SuppressWarnings("all")
    @BeforeClass
    public static void setup() throws IOException {
        for (int i = 0; i < 1000; i++) {
            final File testFile = generateTestData1(String.valueOf(i)).toFile();
            testFiles.add(testFile);
        }
        orcFile = java.nio.file.Files.createTempFile("input-json-" + currentTimeMillis(), ".orc").toFile();
        if (orcFile.exists()) {
            orcFile.delete();
        }
    }

    @Test
    public void testSimpleOrc() throws Exception {
        testSimpleOrcSerialization();
        testSimpleOrcDeserialization();
    }

    @SuppressWarnings("UnstableApiUsage")
    void testSimpleOrcSerialization() throws Exception {
        final TypeDescription schema = TypeDescription.createStruct().addField("__thingName__", TypeDescription.createString()).addField("__deviceId__", TypeDescription.createString()).addField("__modelType__", TypeDescription.createString()).addField("__logicalInterfaceId__", TypeDescription.createString())
                                                      .addField("__metricsType__", TypeDescription.createInt()).addField("__tenantId__", TypeDescription.createString()).addField("__calculate_time__", TypeDescription.createLong()).addField("__assetId__", TypeDescription.createString())
                                                      .addField("__physicalInterfaceId__", TypeDescription.createString()).addField("__deviceTypeId__", TypeDescription.createString()).addField("__deptScope__", TypeDescription.createString()).addField("__timestamp__", TypeDescription.createLong())
                                                      .addField("__cloud_time__", TypeDescription.createLong()).addField("__online__connected", TypeDescription.createBoolean()).addField("__online__directlyLinked", TypeDescription.createBoolean());
        out.printf(">>> Writing to ORC file: %s%n", orcFile.getAbsolutePath());

        final Properties props = new Properties();
        props.setProperty(OrcConf.COMPRESSION_STRATEGY.name(), OrcFile.CompressionStrategy.COMPRESSION.name());
        final OrcFile.WriterOptions options = OrcFile.writerOptions(props, new Configuration());

        try (final Writer writer = OrcFile.createWriter(new Path(orcFile.getAbsolutePath()), options.setSchema(schema))) {
            // Write the testFiles to records to Orc file.
            int row = 0;
            for (File testFile : testFiles) {
                final JsonNode jsonNode = parseToNode(Files.toString(testFile, UTF_8));
                final JsonNode onlineNode = jsonNode.get("__online__");

                // Populate column vectors with data from the Json
                final VectorizedRowBatch batch = schema.createRowBatch();
                row = batch.size++;

                final BytesColumnVector thingNameColumn = (BytesColumnVector) batch.cols[0];
                final BytesColumnVector deviceIdColumn = (BytesColumnVector) batch.cols[1];
                final BytesColumnVector modelTypeColumn = (BytesColumnVector) batch.cols[2];
                final BytesColumnVector logicalInterfaceIdColumn = (BytesColumnVector) batch.cols[3];
                final LongColumnVector metricsTypeColumn = (LongColumnVector) batch.cols[4];
                final BytesColumnVector tenantIdColumn = (BytesColumnVector) batch.cols[5];
                final LongColumnVector calculateTimeColumn = (LongColumnVector) batch.cols[6];
                final BytesColumnVector assetIdColumn = (BytesColumnVector) batch.cols[7];
                final BytesColumnVector physicalInterfaceIdColumn = (BytesColumnVector) batch.cols[8];
                final BytesColumnVector deviceTypeIdColumn = (BytesColumnVector) batch.cols[9];
                final BytesColumnVector deptScopeColumn = (BytesColumnVector) batch.cols[10];
                final LongColumnVector timestampColumn = (LongColumnVector) batch.cols[11];
                final LongColumnVector cloudTimeColumn = (LongColumnVector) batch.cols[12];
                final LongColumnVector onlineConnectedColumn = (LongColumnVector) batch.cols[13];
                final LongColumnVector onlineDirectlyLinkedColumn = (LongColumnVector) batch.cols[14];

                thingNameColumn.setVal(row, jsonNode.get("__thingName__").toString().getBytes());
                deviceIdColumn.setVal(row, jsonNode.get("__deviceId__").toString().getBytes());
                modelTypeColumn.setVal(row, jsonNode.get("__modelType__").toString().getBytes());
                logicalInterfaceIdColumn.setVal(row, jsonNode.get("__logicalInterfaceId__").toString().getBytes());
                metricsTypeColumn.vector[row] = jsonNode.get("__metricsType__").asLong();
                tenantIdColumn.setVal(row, jsonNode.get("__tenantId__").toString().getBytes());
                calculateTimeColumn.vector[row] = jsonNode.get("__calculate_time__").asLong();
                assetIdColumn.setVal(row, jsonNode.get("__assetId__").toString().getBytes());
                physicalInterfaceIdColumn.setVal(row, jsonNode.get("__physicalInterfaceId__").toString().getBytes());
                deviceTypeIdColumn.setVal(row, jsonNode.get("__deviceTypeId__").toString().getBytes());
                deptScopeColumn.setVal(row, jsonNode.get("__deptScope__").toString().getBytes());
                timestampColumn.vector[row] = jsonNode.get("__timestamp__").asLong();
                cloudTimeColumn.vector[row] = jsonNode.get("__cloud_time__").asLong();
                onlineConnectedColumn.vector[row] = onlineNode.get("connected").asLong();
                onlineDirectlyLinkedColumn.vector[row] = onlineNode.get("directlyLinked").asLong();

                writer.addRowBatch(batch);
                if (row == batch.getMaxSize()) {
                    row = 0;
                }
            }
        }

        final byte[] orcBytes = FileIOUtils.readFileToByteArray(orcFile);
        final long originalSize = testFiles.stream().mapToLong(File::length).sum();

        out.printf(">>> Compared rate: %s (%s/%s)%n", Maths.divide(orcBytes.length, originalSize, 4), orcBytes.length, originalSize);
    }

    void testSimpleOrcDeserialization() throws IOException {
        out.printf(">>> Reading to ORC file: %s%n", orcFile.getAbsolutePath());

        final OrcFile.ReaderOptions options = OrcFile.readerOptions(new Configuration());
        try (final Reader reader = OrcFile.createReader(new Path(orcFile.getAbsolutePath()), options)) {
            final TypeDescription schema = reader.getSchema();
            final VectorizedRowBatch batch = schema.createRowBatch();

            final RecordReader it = reader.rows(reader.options().schema(schema));
            final List<Map<String, Object>> records = new ArrayList<>();

            while (it.nextBatch(batch)) {
                List<TypeDescription> types = schema.getChildren();
                List<String> fieldNames = schema.getFieldNames();
                for (int row = 0; row < batch.size; ++row) {
                    Map<String, Object> record = new LinkedHashMap<>();
                    // 读取第 row 行数据
                    for (int col = 0; col < batch.numCols; ++col) {
                        // 获取第 col 列的数据
                        ColumnVector colVector = batch.cols[col];
                        String fieldName = fieldNames.get(col);
                        Object value = null;
                        if (colVector.isNull[row]) {
                            record.put(fieldName, null);
                            continue;
                        }
                        final TypeDescription type = types.get(col);
                        if (type.getCategory() == TypeDescription.Category.BOOLEAN) {
                            value = ((LongColumnVector) colVector).vector[row] == 1;
                        } else if (type.getCategory() == TypeDescription.Category.INT) {
                            value = ((LongColumnVector) colVector).vector[row];
                        } else if (type.getCategory() == TypeDescription.Category.LONG) {
                            value = ((LongColumnVector) colVector).vector[row];
                        } else if (type.getCategory() == TypeDescription.Category.FLOAT) {
                            value = ((DoubleColumnVector) colVector).vector[row];
                        } else if (type.getCategory() == TypeDescription.Category.DOUBLE) {
                            value = ((DoubleColumnVector) colVector).vector[row];
                        } else if (type.getCategory() == TypeDescription.Category.STRING) {
                            byte[] bytes = ((BytesColumnVector) colVector).vector[0];
                            if (bytes != null) {
                                value = new String(bytes, ((BytesColumnVector) colVector).start[0], ((BytesColumnVector) colVector).length[0]);
                            }
                        } else if (type.getCategory() == TypeDescription.Category.TIMESTAMP) {
                            Timestamp ts = ((TimestampColumnVector) colVector).asScratchTimestamp(row);
                            // TODO format to 'yyyy-MM-dd HH:mm:ss'
                            value = ts.toString();
                        } else {
                            throw new IllegalArgumentException("Unknown type " + type);
                        }
                        record.put(fieldName, value);
                    }
                    records.add(record);
                }
            }

            out.printf(">>> Records count: %s, => %s%n", records.size(), toJSONString(records));
        }
    }

}

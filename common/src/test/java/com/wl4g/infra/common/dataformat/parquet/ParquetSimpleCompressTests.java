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

package com.wl4g.infra.common.dataformat.parquet;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Files;
import com.wl4g.infra.common.dataformat.DataFormatTestsSupport;
import com.wl4g.infra.common.io.FileIOUtils;
import com.wl4g.infra.common.math.Maths;
import com.wl4g.infra.common.serialize.JacksonUtils;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The {@link ParquetSimpleCompressTests}
 *
 * @author James Wong
 * @since v1.0
 **/
public class ParquetSimpleCompressTests extends DataFormatTestsSupport {

    private final static List<File> testFiles = new ArrayList<>();

    @BeforeClass
    public static void setup() throws IOException {
        for (int i = 0; i < 1000; i++) {
            testFiles.add(generateTestData1(String.valueOf(i)).toFile());
        }
    }

    @Test
    public void testSimpleParquetSerialization() throws Exception {
        // Define Parquet schema
        //MessageType schema = Types.buildMessage()
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BINARY).named("__thingName__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BINARY).named("__deviceId__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BINARY).named("__modelType__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BINARY).named("__logicalInterfaceId__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.INT32).named("__metricsType__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BINARY).named("__tenantId__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.INT64).named("__calculate_time__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BINARY).named("__assetId__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BINARY).named("__physicalInterfaceId__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BINARY).named("__deviceTypeId__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BINARY).named("__deptScope__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.INT64).named("__timestamp__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.INT64).named("__cloud_time__"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BOOLEAN).named("__online__connected"))
        //        .addField(Types.required(PrimitiveType.PrimitiveTypeName.BOOLEAN).named("__online__directlyLinked"))
        //        .named("root");

        final Schema rootSchema =
                SchemaBuilder.record("rootRecord").fields()
                             .name("__thingName__").type().stringType().noDefault()
                             .name("__deviceId__").type().stringType().noDefault()
                             .name("__modelType__").type().stringType().noDefault()
                             .name("__logicalInterfaceId__").type().stringType().noDefault()
                             .name("__metricsType__").type().intType().noDefault()
                             .name("__tenantId__").type().stringType().noDefault()
                             .name("__calculate_time__").type().longType().noDefault()
                             .name("__assetId__").type().stringType().noDefault()
                             .name("__physicalInterfaceId__").type().stringType().noDefault()
                             .name("__deviceTypeId__").type().stringType().noDefault()
                             .name("__deptScope__").type().stringType().noDefault()
                             .name("__timestamp__").type().longType().noDefault()
                             .name("__cloud_time__").type().longType().noDefault()
                             .name("__online__").type().record("__online__Record").fields()
                             .name("connected").type().booleanType().noDefault()
                             .name("directlyLinked")
                             .type().booleanType().noDefault().endRecord().noDefault() // 这里需要添加 endRecord() 来结束 __online__Record 的定义
                             .endRecord();

        final Configuration conf = new Configuration();
        //conf.set(AvroReadSupport.PARQUET_READ_SCHEMA, "");

        final File parquetFile = java.nio.file.Files.createTempFile("input-json-" + currentTimeMillis(), ".parquet").toFile();
        if (parquetFile.exists()) {
            parquetFile.delete();
        }
        out.printf(">>> Parquet file: %s%n", parquetFile.getAbsolutePath());

        try (final ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(new Path(parquetFile.getAbsolutePath()))
                                                                          .withSchema(rootSchema).withConf(conf)
                                                                          .withCompressionCodec(CompressionCodecName.GZIP)
                                                                          .withRowGroupSize(1024 * 1024)
                                                                          .withPageSize(1024 * 1024)
                                                                          .build()) {
            // Write the testFiles to records to Parquet file
            for (File testFile : testFiles) {
                final JsonNode jsonNode = JacksonUtils.parseToNode(Files.toString(testFile, UTF_8));
                final JsonNode onlineNode = jsonNode.get("__online__");

                final GenericData.Record record = new GenericRecordBuilder(rootSchema)
                        .set("__thingName__", jsonNode.get("__thingName__").asText())
                        .set("__deviceId__", jsonNode.get("__deviceId__").asText())
                        .set("__modelType__", jsonNode.get("__modelType__").asText())
                        .set("__logicalInterfaceId__", jsonNode.get("__logicalInterfaceId__").asText())
                        .set("__metricsType__", jsonNode.get("__metricsType__").asInt())
                        .set("__tenantId__", jsonNode.get("__tenantId__").asText())
                        .set("__calculate_time__", jsonNode.get("__calculate_time__").asLong())
                        .set("__assetId__", jsonNode.get("__assetId__").asText())
                        .set("__physicalInterfaceId__", jsonNode.get("__physicalInterfaceId__").asText())
                        .set("__deviceTypeId__", jsonNode.get("__deviceTypeId__").asText())
                        .set("__deptScope__", jsonNode.get("__deptScope__").asText())
                        .set("__timestamp__", jsonNode.get("__timestamp__").asLong())
                        .set("__cloud_time__", jsonNode.get("__cloud_time__").asLong())
                        .set("__online__", new GenericRecordBuilder(rootSchema.getField("__online__").schema())
                                .set("connected", onlineNode.get("connected").asBoolean())
                                .set("directlyLinked", onlineNode.get("directlyLinked").asBoolean()).build()).build();
                writer.write(record);
            }
        }

        // Notice: Reading Parquet bytes is not typically done in this way; it's more common to use a ParquetReader.
        // However, if you need to read it as bytes for comparison purposes:
        final byte[] parquetBytes = FileIOUtils.readFileToByteArray(new File(parquetFile.toString()));
        final long originalSize = testFiles.stream().mapToLong(File::length).sum();

        System.out.printf(">>> Compared rate: %s (%s/%s)%n",
                Maths.divide(parquetBytes.length, originalSize, 4),
                parquetBytes.length,
                originalSize);
    }

}

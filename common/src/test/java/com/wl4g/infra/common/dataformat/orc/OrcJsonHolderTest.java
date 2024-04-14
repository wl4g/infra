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

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Files;
import com.wl4g.infra.common.io.FileIOUtils;
import com.wl4g.infra.common.math.Maths;
import com.wl4g.infra.common.dataformat.DataFormatTestsSupport;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.TypeDescription;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseToNode;
import static java.lang.System.out;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The {@link OrcJsonHolderTest}
 *
 * @author James Wong
 * @since v1.0
 **/
public class OrcJsonHolderTest extends DataFormatTestsSupport {
    private final static List<File> testFiles = new ArrayList<>();
    private final static List<JsonNode> testJacksonNodes = new ArrayList<>();
    private final static List<JSONObject> testFastJsonNodes = new ArrayList<>();

    @SuppressWarnings("all")
    @BeforeClass
    public static void setup() throws IOException {
        for (int i = 0; i < 1000; i++) {
            final File testFile = generateTestData2(String.valueOf(i)).toFile();
            testFiles.add(testFile);
            final String json = Files.toString(testFile, UTF_8);
            testJacksonNodes.add(parseToNode(json));
            testFastJsonNodes.add(parseObject(json));
        }
    }

    @Test
    public void testJacksonOrcCompression() throws Exception {
        doTestJsonOrcCompression(JacksonOrcHolder.getInstance(), testJacksonNodes);
    }

    @Test
    public void testFastJsonOrcCompression() throws Exception {
        doTestJsonOrcCompression(FastJsonOrcHolder.getInstance(), testFastJsonNodes);
    }

    @SuppressWarnings("all")
    void doTestJsonOrcCompression(OrcJsonHolder holder, List testJsonNodes) throws Exception {
        final TypeDescription schema = holder.getSchemaFromJsonObject(testJsonNodes.get(0));

        // Serialization
        //
        final ByteArrayOutputStream output = new ByteArrayOutputStream(10240);
        holder.writeToOrc(testJsonNodes, schema, output, 1024, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", null);
        final byte[] orcBytes = output.toByteArray();

        // for test: shell> orc-tools meta /tmp/1.orc
        FileIOUtils.writeFile(new File("/tmp/1.orc"), orcBytes, false);

        final long originalSize = testFiles.stream().mapToLong(File::length).sum();
        out.printf(">>> Compared rate: %s (%s/%s)%n", Maths.divide(orcBytes.length, originalSize, 4), orcBytes.length, originalSize);

        // Deserialization 1
        //
        final Reader reader = OrcFile.createReader(new Path("/tmp/1.orc"), OrcFile.readerOptions(new Configuration()));
        final Iterator<Iterable<Object>> rowBatchIter = holder.readFromOrc(reader);

        out.printf(">>> Downsampling print top 10/%s records: %n", reader.getNumberOfRows());
        if (rowBatchIter.hasNext()) {
            final Iterator<Object> recordsIter = rowBatchIter.next().iterator(); // e.g: ArrayNode or JSONArray
            for (int i = 0; i < 10 && recordsIter.hasNext(); i++) {
                final Object record = recordsIter.next(); // e.g: JsonNode or JSONObject
                out.println(record);
            }
        }

        // Deserialization 2
        //
        final List<Object> records = holder.readFromOrcWithFirstBatch(orcBytes); // e.g: List<JsonNode|JSONObject>
        out.printf(">>> Downsampling print top 10 records: %n");
        records.subList(0, Math.min(10, records.size())).forEach(out::println);
    }

}

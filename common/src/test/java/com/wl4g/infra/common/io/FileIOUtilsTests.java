/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.infra.common.io;

import static com.google.common.base.Charsets.ISO_8859_1;
import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.io.FileIOUtils.ensureFile;
import static com.wl4g.infra.common.io.FileIOUtils.readLines;
import static com.wl4g.infra.common.io.FileIOUtils.seekReadLines;
import static com.wl4g.infra.common.io.FileIOUtils.seekReadString;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.SystemUtils.USER_DIR;

import java.io.File;
import java.io.RandomAccessFile;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import com.wl4g.infra.common.io.FileIOUtils.ReadTailFrame;

public class FileIOUtilsTests {

    private static String filename = USER_DIR + "/src/test/java/"
            + FileIOUtilsTests.class.getName().replaceAll("\\.", "/").replace(FileIOUtilsTests.class.getSimpleName(), "")
            + "/randomAccessFileTests.log";

    @Test
    public void seekReadTest1() {
        out.println(SystemUtils.LINE_SEPARATOR);
        out.println(readLines("C:\\Users\\Administrator\\Desktop\\aaa.txt", 2, 12));
        out.println("--------------------");
        out.println(seekReadString("C:\\Users\\Administrator\\Desktop\\aaa.txt", 3L, 12));
        out.println("--------------------");
        out.println(seekReadLines("C:\\Users\\Administrator\\Desktop\\aaa.txt", 13L, 6, line -> {
            return line.equalsIgnoreCase("EOF"); // End if 'EOF'
        }));
    }

    @Test
    public void ensureFileTest2() {
        ensureFile(new File("c:\\mydir1\\a.txt"));
    }

    @Test
    public void randomAccessFilePointerTest3() {
        int startPos = 1;
        int aboutLimit = 100;

        try (RandomAccessFile raf = new RandomAccessFile(filename, "r")) {
            raf.seek(startPos);
            long c = 0, lastPos = -1, endPos = (startPos + aboutLimit);
            while (raf.getFilePointer() > lastPos && (lastPos = raf.getFilePointer()) < endPos && ++c < 100_000) {
                String line = raf.readLine();
                if (nonNull(line)) {
                    line = new String(line.getBytes(ISO_8859_1), UTF_8);
                    out.println(String.format("startPos=%s, length=%s, pointer=%s, line=%s", startPos, raf.length(),
                            raf.getFilePointer(), line));
                } else {
                    out.println(format("startPos=%s, length=%s, pointer=%s", startPos, raf.length(), raf.getFilePointer()));
                    break;
                }
            }
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    public void deserializeReadTailFrameTest4() {
        String s = "{\"startPos\":1,\"endPos\":2,\"length\":4,\"lines\":[\"aa\"],\"hasNext\":true}";
        out.println(parseJSON(s, ReadTailFrame.class));
    }

}
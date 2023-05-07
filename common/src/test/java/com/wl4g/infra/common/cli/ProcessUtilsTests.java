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
package com.wl4g.infra.common.cli;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.infra.common.cli.ProcessUtils.buildCrossSingleCommands;
import static com.wl4g.infra.common.cli.ProcessUtils.execMulti;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ProcessUtilsTests {

    @Test
    public void buildCrossSingleCommandsTest1() throws Exception {
        String[] cmdarray = buildCrossSingleCommands("mvn -version", new File("c:\\out"), new File("c:\\err"), false, false);
        Runtime.getRuntime().exec(cmdarray).waitFor();
    }

    @Test
    public void execMultiWithWindowsTest2() throws Exception {
        if (IS_OS_WINDOWS) {
            execMulti("echo \"start...\"\njps \necho \"end\"", new File("d:\\"), new File("c:\\out"), new File("c:\\err"), true,
                    false);
        }
    }

    @Test
    public void execProgressTest3() throws Exception {
        int whole = 120;
        for (int i = 0; i < whole; i++) {
            ProcessUtils.printProgress("正在分析...", i, whole, '#');
        }
    }

    @Test
    public void execInteractiveCommandTest4() throws Exception {
        // Generate sample data.
        File file = new File("/tmp/test_vim_file.txt");
        FileUtils.write(file, "abcdefghijklmnopqrstuvwxyz", UTF_8);

        // Blocking...
        // String res1 = ProcessUtils.execSimpleString("vim " +
        // file.getAbsolutePath());
        // System.out.println(res1);

        String res2 = ProcessUtils.execSimpleString("cat /proc/1/cgroup");
        System.out.println(res2);
    }

}
/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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

import org.apache.commons.cli.ParseException;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.wl4g.infra.common.cli.CommandLineTool.CommandLineWrapper;

/**
 * {@link CommandLineToolTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-31 v3.0.0
 * @since v3.0.0
 */
public class CommandLineToolTests {

    @Test
    public void testSuccessParse() throws ParseException {
        String[] args = { "--name", "my-name2", "--start-time", "161234567890" };

        CommandLineWrapper line = CommandLineTool.builder()
                .option(null, "name", "my-name1", "The option of name")
                .option("c", "count", "10", "The option of count")
                .mustOption("s", "start-time", "The option of start time") // required
                .helpIfEmpty(args, false)
                .build(args);

        String name = line.get("name");
        Long count = line.getLong("count");
        String startTime = line.get("start-time");
        System.out.printf("name=%s,count=%s,startTime=%s", name, count, startTime);
    }

    @Test
    public void testUseInvalidOption() throws ParseException {
        String[] args = { "--name", "my-name2", "--start-time", "161234567890" };

        CommandLineWrapper line = CommandLineTool.builder()
                .option("n", "name", "my-name1", "The option of name")
                .mustOption("s", "start-time", "The option of start time") // required
                .helpIfEmpty(args, false)
                .build(args);

        Assertions.assertThrows(ParseException.class, () -> {
            Long count = line.getLong("count"); // use invalid option
            System.out.printf("count=%s", count);
        }, "expected invalid option exception");
    }

    @Test
    public void testMissingOption() throws ParseException {
        String[] args = {};

        CommandLineWrapper line = CommandLineTool.builder()
                .option("n", "name", "my-name1", "The option of name")
                .option("c", "count", "10", "The option of count")
                .mustOption("s", "start-time", "The option of start time") // required
                .helpIfEmpty(args, false)
                .build(args);

        String name = line.get("name");
        Long count = line.getLong("count");
        String startTime = line.get("start-time");
        System.out.printf("name=%s,count=%s,startTime=%s", name, count, startTime);
    }

}

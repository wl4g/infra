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

import com.wl4g.infra.common.cli.CommandLineTool.CommandLineWrapper;

/**
 * {@link CommandLineToolTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-05-31 v3.0.0
 * @since v3.0.0
 */
public class CommandLineToolTests {

    public static void main(String[] args) throws Exception {
        CommandLineWrapper line = CommandLineTool.builder()
                .option("a", "aaa", "111", "The option of aaa")
                .option("b", "bbb", "222", "The option of bbb")
                .option("c", "ccc", "333", "The option of ccc")
                .option("d", "ddd", null, "The option of ddd") // required
                // .printUsageIfEmpty(args)
                .build(args);

        String aaa = line.get("aaa");
        String bbb = line.get("bbb");
        String ccc = line.get("ccc");
        Long ddd = line.getLong("ddd");
        System.out.printf("aaa=%s,bbb=%s,ccc=%s,ddd=%s", aaa, bbb, ccc, ddd);
    }

}

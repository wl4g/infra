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
package com.wl4g.infra.common.graalvm;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.wl4g.infra.common.graalvm.GraalJsScriptManager.ContextWrapper;
import com.wl4g.infra.common.graalvm.GraalJsScriptManager.SimpleFastContextPool;
import com.wl4g.infra.common.io.FileIOUtils;

/**
 * {@link GraalJsScriptManagerTests}
 * 
 * @author James Wong
 * @version 2022-09-23
 * @since v3.0.0
 */
public class GraalJsScriptManagerTests {

    @Test
    public void testTakeNoOverflow() throws Exception {
        try (SimpleFastContextPool pool = new SimpleFastContextPool(1, 10, () -> Context.create());) {
            try {
                for (int i = 0; i < 10; i++) {
                    ContextWrapper context = pool.take();
                    System.out.println("The " + i + " take context: " + context);
                }
                System.out.println("Assertion success");
            } catch (IllegalStateException e) {
                e.printStackTrace();
                throw new IllegalStateException("Assertion failed", e);
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testTakeOverflow() throws Exception {
        try (SimpleFastContextPool pool = new SimpleFastContextPool(1, 10, () -> Context.create());) {
            try {
                for (int i = 0; i < 11; i++) {
                    ContextWrapper context = pool.take();
                    System.out.println("The " + i + " take context: " + context);
                }
            } catch (IllegalStateException e) {
                // e.printStackTrace();
                System.out.println("Assertion success");
                throw e;
            }
        }
    }

    @Test
    public void testTakeNoOverflowWithRelease() throws Exception {
        try (SimpleFastContextPool pool = new SimpleFastContextPool(1, 10, () -> Context.create());) {
            try {
                for (int i = 0; i < 20; i++) {
                    try (ContextWrapper context = pool.take();) {
                        System.out.println("The " + i + " take context: " + context);
                    }
                }
                System.out.println("Assertion success");
            } catch (IllegalStateException e) {
                System.out.println(format("Assertion failed - %s", e.getMessage()));
                throw e;
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testMultiThreadExecutionWithOverflow() throws Exception {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false"); // disabled-warning

        int threadCount = 100;
        int poolMaxSize = 20;

        String script = format("function process(name) { console.log('[  js] - The name is:', name); return 'Hello ' + name; }");
        File localFile = new File("/tmp/test-graaljs-" + currentTimeMillis() + ".js");
        FileIOUtils.writeFile(localFile, script, Charsets.UTF_8, false);

        List<Exception> exceptions = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadCount);

        try (SimpleFastContextPool pool = new SimpleFastContextPool(1, poolMaxSize,
                () -> Context.newBuilder("js").allowIO(true).build());) {
            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    long begin = currentTimeMillis();

                    try (ContextWrapper context = pool.take();) {
                        context.leave();
                        System.out.println(format("[java] [%s] - running ...", Thread.currentThread().getName()));

                        context.eval(Source.newBuilder("js", localFile).build());
                        Value bindings = context.getBindings("js");
                        Value processFunction = bindings.getMember("process");

                        String helloName = format("jack from %s", Thread.currentThread().getName());
                        Value result = processFunction.execute(helloName);

                        System.out.println(format("[java] [%s] - finished, cost(execute): %sms, result: %s",
                                Thread.currentThread().getName(), (currentTimeMillis() - begin), result));

                    } catch (Exception e) {
                        System.err.println(format("[java] [%s] - %s", Thread.currentThread().getName(), e));
                        exceptions.add(e);
                    } finally {
                        latch.countDown();
                    }
                }, "test-thread-" + i).start();
                Thread.sleep(10L);
            }
            latch.await();

            if (exceptions.isEmpty()) {
                System.out.println("Assertion success");
            } else {
                throw new IllegalStateException(format("Assertion failed - %s", exceptions.get(0).getMessage()),
                        exceptions.get(0));
            }
        }
    }

    // see:https://www.graalvm.org/22.0/reference-manual/js/Modules/
    // Notice: The js language support for the native-image plugin has been
    // moved out by default since graalvm-22.2
    @Test
    public void testJavaToESModuleCall() throws Exception {
        String esmModuleScript = "export class Foo { square(x) { return x * x; } }";

        File localFile = new File("/tmp/test-graaljs-" + currentTimeMillis() + ".mjs");
        FileIOUtils.writeFile(localFile, esmModuleScript, Charsets.UTF_8, false);

        String esmScript = "import {Foo} from '" + localFile.getAbsolutePath()
                + "'; const foo = new Foo(); console.log(foo.square(64));";

        try (GraalJsScriptManager manager = new GraalJsScriptManager(1, 10,
                () -> Context.newBuilder("js").allowIO(true).build());) {
            // Source source =
            // Source.newBuilder("js",localFile).mimeType("application/javascript+module").build();
            Source source = Source.newBuilder("js", esmScript, "test.mjs").build();
            Value result = manager.getContext().eval(source);
            System.out.println(result);
        }
    }

    // see:https://www.graalvm.org/22.0/reference-manual/js/Modules/
    // Notice: The js language support for the native-image plugin has been
    // moved out by default since graalvm-22.2
    @Test
    public void testESModuleCommonJsNPMRequireImport() throws Exception {
        File npmModulesDir = new File("/tmp/test-graaljs-esm-require-commonjs-npm-modules/");
        FileIOUtils.forceMkdir(npmModulesDir);

        String globalScript = "var RengineContext = fucntion() { }";
        File localGlobalFile = new File("/tmp/test-graaljs-globals-" + currentTimeMillis() + ".js");
        FileIOUtils.writeFile(localGlobalFile, globalScript, Charsets.UTF_8, false);

        String esmModuleScript = "export class Foo { square(x) { return x * x; } }";
        File localFile = new File("/tmp/test-graaljs-" + currentTimeMillis() + ".mjs");
        FileIOUtils.writeFile(localFile, esmModuleScript, Charsets.UTF_8, false);

        Map<String, String> options = new HashMap<>();
        // Enable CommonJS experimental support.
        options.put("js.commonjs-require", "true");
        // (optional) folder where the NPM modules to be loaded are located.
        options.put("js.commonjs-require-cwd", npmModulesDir.getAbsolutePath());
        // (optional) initialization script to pre-define globals.
        options.put("js.commonjs-global-properties", localFile.getAbsolutePath());
        // (optional) Node.js built-in replacements as a comma separated list.
        // options.put("js.commonjs-core-modules-replacements","buffer:buffer/,path:path-browserify");

        try (GraalJsScriptManager manager = new GraalJsScriptManager(1, 10,
                // Create context with IO support and experimental options.
                () -> Context.newBuilder("js").allowExperimentalOptions(true).allowIO(true).options(options).build());) {
            // Require a module
            Value module = manager.getContext().eval("js", "require('Foo');");
            System.out.println(module);
        }
    }

}

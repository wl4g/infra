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

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.google.common.base.Charsets;
import com.wl4g.infra.common.graalvm.GraalJsScriptManager.FastContextPool;
import com.wl4g.infra.common.graalvm.GraalJsScriptManager.FastContextPool.ContextWrapper;
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
        try (FastContextPool pool = new FastContextPool(1, 10, () -> {
            return Context.create();
        });) {
            try {
                for (int i = 0; i < 10; i++) {
                    ContextWrapper context = pool.take();
                    System.out.println("The " + i + " take context: " + context);
                }
                System.out.println("Assertion success");
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                throw new IllegalStateException("Assertion failed", e);
            }
        }
    }

    @Test
    public void testTakeOverflow() throws Exception {
        try (FastContextPool pool = new FastContextPool(1, 10, () -> {
            return Context.create();
        });) {
            try {
                for (int i = 0; i < 11; i++) {
                    ContextWrapper context = pool.take();
                    System.out.println("The " + i + " take context: " + context);
                }
                throw new IllegalStateException("Assertion failed");
            } catch (NoSuchElementException e) {
                // e.printStackTrace();
                System.out.println("Assertion success");
            }
        }
    }

    @Test
    public void testTakeNoOverflowWithRelease() throws Exception {
        try (FastContextPool pool = new FastContextPool(1, 10, () -> {
            return Context.create();
        });) {
            try {
                for (int i = 0; i < 11; i++) {
                    try (ContextWrapper context = pool.take();) {
                        System.out.println("The " + i + " take context: " + context);
                    }
                }
                System.out.println("Assertion success");
            } catch (NoSuchElementException e) {
                e.printStackTrace();
                throw new IllegalStateException("Assertion failed", e);
            }
        }
    }

    // see:https://www.graalvm.org/22.0/reference-manual/js/Modules/
    // Notice: The js language support for the native-image plugin has been
    // moved out by default since graalvm-22.2
    @Test
    public void testESModuleImport() throws Exception {
        String esmModuleScript = "export class Foo { square(x) { return x * x; } }";

        File localFile = new File("/tmp/test-graaljs-" + currentTimeMillis() + ".mjs");
        FileIOUtils.writeFile(localFile, esmModuleScript, Charsets.UTF_8, false);

        String esmScript = "import {Foo} from '" + localFile.getAbsolutePath()
                + "'; const foo = new Foo(); console.log(foo.square(42));";

        try (GraalJsScriptManager manager = new GraalJsScriptManager(() -> Context.newBuilder("js").allowIO(true).build());) {
            manager.eval(Source.newBuilder("js", esmScript, "test.mjs").build());
        }
    }

    // see:https://www.graalvm.org/22.0/reference-manual/js/Modules/
    // Notice: The js language support for the native-image plugin has been
    // moved out by default since graalvm-22.2
    @Test
    public void testESModuleExports() throws Exception {
        String esmModuleScript = "export const foo = 42;";

        File localFile = new File("/tmp/test-graaljs-" + currentTimeMillis() + ".mjs");
        FileIOUtils.writeFile(localFile, esmModuleScript, Charsets.UTF_8, false);

        try (GraalJsScriptManager manager = new GraalJsScriptManager(() -> Context.newBuilder("js")
                .allowExperimentalOptions(true)
                .allowIO(true)
                .option("js.esm-eval-returns-exports", "true")
                .build());) {

            Source source = Source.newBuilder("js", localFile).mimeType("application/javascript+module").build();
            Value exports = manager.eval(source);

            // now the `exports` object contains the ES module exported symbols.
            // prints `42`
            final String result = exports.getMember("foo").toString();
            System.out.println(result);
            Assertions.assertEquals(result, "42");
        }
    }

    // see:https://www.graalvm.org/22.0/reference-manual/js/Modules/
    // Notice: The js language support for the native-image plugin has been
    // moved out by default since graalvm-22.2
    @Test
    public void testESModuleRequireCommonJsNPM() throws Exception {
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

        try (GraalJsScriptManager manager = new GraalJsScriptManager(
                // Create context with IO support and experimental options.
                () -> Context.newBuilder("js").allowExperimentalOptions(true).allowIO(true).options(options).build());) {
            // Require a module
            Value module = manager.eval("js", "require('Foo');");
            System.out.println(module);
        }
    }

}

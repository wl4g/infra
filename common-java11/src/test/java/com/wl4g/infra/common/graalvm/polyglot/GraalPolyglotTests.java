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
package com.wl4g.infra.common.graalvm.polyglot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Set;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.management.ExecutionListener;
import org.junit.Test;

/**
 * {@link GraalPolyglotTests}
 * 
 * @author James Wong
 * @version 2023-03-31
 * @since v1.0.0
 */
public class GraalPolyglotTests {

    // see:https://www.graalvm.org/22.2/reference-manual/embed-languages/#build-a-shell-for-many-languages
    @Test
    public void testShellConsoleWithAnyLanguage() throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        PrintStream output = System.out;
        Context context = Context.newBuilder().allowAllAccess(true).build();
        Set<String> languages = context.getEngine().getLanguages().keySet();
        output.println("Shell for " + languages + ":");
        String language = languages.iterator().next();
        for (;;) {
            try {
                output.print(language + "> ");
                String line = input.readLine();
                if (line == null) {
                    break;
                } else if (languages.contains(line)) {
                    language = line;
                    continue;
                }
                Source source = Source.newBuilder(language, line, "<shell>").interactive(true).buildLiteral();
                context.eval(source);
            } catch (PolyglotException t) {
                if (t.isExit()) {
                    break;
                }
                t.printStackTrace();
            }
        }

    }

    // 可用于实现debug
    // see:https://www.graalvm.org/22.2/reference-manual/embed-languages/#step-through-with-execution-listeners
    public void testListenStepToStepExecution() {
        try (Context context = Context.create("js")) {
            ExecutionListener listener = ExecutionListener.newBuilder()
                    .onEnter(event -> System.out.println(event.getLocation().getCharacters()))
                    .statements(true)
                    .attach(context.getEngine());
            context.eval("js", "for (var i = 0; i < 2; i++);");
            listener.close();
        }
    }

}

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

import java.io.File;
import java.io.IOException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.Test;

/**
 * {@link GraalJava2JSTests}
 * 
 * @author James Wong
 * @version 2022-09-24
 * @since v3.0.0
 * @see https://github.com/AMIS-Services/jfall2019-graalvm/blob/master/polyglot/java2js/nl/amis/java2js/HelloWorld.java
 */
public class GraalJava2JSTests {

    @Test
    public void testHelloWorld() {
        Context context = Context.create();
        context.eval("js", "print('Hello JavaScript!')");

        Value helloWorldFunction = context.eval("js",
                "(function(name) { return `Hello ${name}, welcome to the world of JavaScript` })");

        // Use the function
        String greeting = helloWorldFunction.execute("John Doe").asString();
        System.out.println(greeting);

        // Handle Exception Thrown in JavaScript
        try {
            context.eval("js", "print('Hello JavaScript!'); throw 'I do not feel like executing this';");
        } catch (PolyglotException e) {
            System.out.println("Exception caught from JavaScript Execution. Orginal Exception: " + e.getMessage());
        }
    }

    @Test
    public void testRunSimpleJS() throws IOException {
        Context context = Context.newBuilder("js").allowIO(true).build();

        File calculatorJS = new File(getClass().getClassLoader().getResource("graalvm/js/test-calculator.js").getFile());
        context.eval(Source.newBuilder("js", calculatorJS)
                /* .mimeType("application/javascript+module") */.build());

        Value fibonacciFunction = context.getBindings("js").getMember("fibonacci");
        Integer fibonacciResult = fibonacciFunction.execute(12).asInt();
        System.out.println("Calculation Result for Fibonacci (12) " + fibonacciResult);

        Value sqrtFunction = context.getBindings("js").getMember("squareRoot");
        Double sqrtResult = sqrtFunction.execute(42).asDouble();
        System.out.println("Calculation Result for Square Root (42) " + sqrtResult);
    }

    @Test
    public void testRunDependsJS() throws IOException {
        Context context = Context.newBuilder("js").allowIO(true).build();

        File commonsJS = new File(getClass().getClassLoader().getResource("graalvm/js/commons-lang-3.0.0.js").getFile());
        context.eval(Source.newBuilder("js", commonsJS).build());

        File testDependsJS = new File(getClass().getClassLoader().getResource("graalvm/js/test-depends.js").getFile());
        context.eval(Source.newBuilder("js", testDependsJS).build());

        Value processFunction = context.getBindings("js").getMember("process");
        Value result = processFunction.execute("10");
        System.out.println("Calculation Result for: " + result);
    }

    // TODO:Failure:because unsupported window/document browser build-in object.
    // Cannot read property 'createElement' of undefined
    // @Test
    public void testRunJQueryJS() throws IOException {
        Context context = Context.newBuilder("js").allowIO(true).build();

        File jqueryFile = new File(getClass().getClassLoader().getResource("graalvm/js/jquery-3.6.1.min.js").getFile());
        context.eval(Source.newBuilder("js", jqueryFile).build());

        context.eval(Source.newBuilder("js",
                "function httpGet(url){$.getJSON(url,function(data){console.log(JSON.stringify(data))});}", "test-call-jquery.js")
                .build());

        Value httpGetFunction = context.getBindings("js").getMember("httpGet");
        httpGetFunction.execute("https://httpbin.org/get").asInt();
    }

}
/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.common.lang;

import static com.wl4g.infra.common.lang.Assert2.isAssignable;
import static com.wl4g.infra.common.lang.Assert2.notNull;

import org.junit.Test;

public class AssertTests {

    @Test
    public void testIncompatible() {
        try {
            isAssignable(int.class, String.class, "Incompatible types");
        } catch (IllegalArgumentException e) {
            // success
        }
        // isTrue(false, IllegalArgumentException.class, "Failed to for
        // aa=%s",
        // "11");
        // notNull(null, IllegalArgumentException.class, "Must be not
        // null");
        // hasText(null, IllegalArgumentException.class, "Must be not
        // empty");
        // isInstanceOf(String.class, new Object(), "Must be not empty");
    }

    @Test
    public void testAssertionWithExceptionClassSuccess() {
        Object obj = null;
        try {
            notNull(obj, MyRuntimeException1.class, "Failed to xxx1");
            throw new IllegalStateException("Assertion failed");
        } catch (MyRuntimeException1 | IllegalArgumentException e) {
            // success
            System.out.println("Assertion Result: " + e.getMessage());
        }
    }

    @Test
    public void testAssertionWithExceptionClassFailure() {
        try {
            Object obj = null;
            notNull(obj, MyRuntimeException2.class, "Failed to xxx2");
        } catch (Error e) {
            // success
            System.out.println("Assertion Result: " + e.getMessage());
        } catch (Throwable e) {
            // failure
            throw e;
        }
    }

    public static class MyRuntimeException1 extends RuntimeException {
        private static final long serialVersionUID = 5430673385973945882L;
        private String name;

        public MyRuntimeException1() {
            super();
        }

        public MyRuntimeException1(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class MyRuntimeException2 extends RuntimeException {
        private static final long serialVersionUID = 5430673385973945181L;
        private final String name;

        public MyRuntimeException2(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
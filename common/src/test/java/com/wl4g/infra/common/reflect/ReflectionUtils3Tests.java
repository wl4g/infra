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
package com.wl4g.infra.common.reflect;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.SystemUtils.USER_DIR;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.wl4g.infra.common.annotation.Todo;

/**
 * {@link ReflectionUtils3Tests}
 * 
 * @author James Wong
 * @version 2023-01-07
 * @since v1.0.0
 */
public class ReflectionUtils3Tests {

    final static List<Class<?>> validExampleClasses12 = asList(ExampleType1.class, ExampleType2.class);

    final static List<Class<?>> validExampleClassesAll = asList(ExampleType0.class, ExampleType1.class, ExampleType2.class);

    @SuppressWarnings({ "deprecation" })
    @Test
    public void testFindClassesWithUrls() throws Exception {
        final URL findUrl = new File(replace(USER_DIR.concat("/src/test/java"), "/", File.separator)).toURL();
        System.out.println("find URL: " + findUrl);

        // method1
        final Collection<Class<?>> classes1 = ReflectionUtils3.findClassesBySuperClass(ExampleType0.class,
                singleton("com.wl4g.infra.common.reflect"), singleton(findUrl));
        System.out.println(classes1);

        assert validExampleClasses12.size() == classes1.size();
        for (Class<?> cls : validExampleClasses12) {
            assert validExampleClasses12.contains(cls);
        }

        // method2
        final Collection<Class<?>> classes2 = ReflectionUtils3.findClassesByAnnotation(Todo.class,
                singleton("com.wl4g.infra.common.reflect"), singleton(findUrl));
        System.out.println(classes2);

        assert validExampleClassesAll.size() == classes2.size();
        for (Class<?> cls : validExampleClassesAll) {
            assert validExampleClassesAll.contains(cls);
        }

        // method3
        final URL findUrl3 = new File(
                replace(USER_DIR.substring(0, USER_DIR.lastIndexOf("/")).concat("/context/target/classes"), "/", File.separator))
                        .toURL();
        System.out.println("find URL3: " + findUrl3);
        final Collection<Class<?>> classes3 = ReflectionUtils3.findClassesAll(singleton("com.wl4g.infra.context.web"),
                singleton(findUrl3));
        System.out.println(classes3);
        assert validExampleClassesAll.size() < classes3.size();
    }

}

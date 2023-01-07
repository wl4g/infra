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

import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.split;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.wl4g.infra.common.reflect.ReflectionUtils3;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * {@link NativeReflectionConfigGenerateTool}
 * 
 * @author James Wong
 * @version 2023-01-07
 * @since v1.0.0
 */
public class NativeReflectionConfigGenerateTool {

    @SuppressWarnings({ "deprecation" })
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println(
                    "Usage: {<classesDirs> <classPackages>}\n\tclassesDirs    The target classes base directory. eg: $PROJECT/target/classes"
                            + "\n\tclassPackages  The find package filter. eg: com.xxx.xx.service");
            System.exit(1);
        }
        final String baseDirs = args[0];
        final String classPackages = args[1];

        final Set<URL> findUrls = safeArrayToList(split(baseDirs, ",")).stream().map(baseDir -> {
            try {
                return new File(replace(baseDir, "/", File.separator)).toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }).collect(toSet());
        System.out.println("finding of URLs: " + findUrls);

        final Set<String> findClassPackages = safeArrayToList(split(classPackages, ",")).stream().collect(toSet());
        System.out.println("finding of classPackages: " + findClassPackages);

        final Collection<Class<?>> classes = ReflectionUtils3.findClassesAll(findClassPackages, findUrls);
        final String classesString = classes.stream().map(cls -> cls.getName()).collect(joining("\n"));
        System.out.println("--- Found classes: ---\n\n" + classesString);

        final List<ReflectionConfigItem> items = buildReflectionConfigItems(classes);
        System.out.println("\n\n--- Generated reflection config items json: ---\n\n");
        String reflectConfigJson = toJSONString(items, true);

        // Custom humnan line and space format.
        reflectConfigJson = reflectConfigJson.replaceAll("\\},\\{\n  \"name\"", "\\},\n\\{\n  \"name\"")
                .replace("  },{\n    \"name\"", "  },\n    {\n    \"name\"")
                .replace(",\n    \"parameterTypes\" :", ",\"parameterTypes\":")
                .replace("  },\n    {\n    \"name\" :", "    },\n    {\"name\" :")
                .replace("]\n    },", "]},")
                .replace("  \"methods\" : [{", "  \"methods\": [\n    {")
                .replace("{\"name\" :", "{\"name\":")
                .replace("    {\n    \"name\" :", "    {\"name\":")
                .replace("\" :", "\":")
                .replace("[ ]", "[]");

        System.out.println(reflectConfigJson);
    }

    public static List<ReflectionConfigItem> buildReflectionConfigItems(Collection<Class<?>> classes) {
        return safeList(classes).stream().map(cls -> {
            final List<ReflectionConfigItemMethod> constructorMethods = safeArrayToList(cls.getDeclaredConstructors()).stream()
                    .map(m -> {
                        return ReflectionConfigItemMethod.builder()
                                .name("<init>")
                                .parameterTypes(safeArrayToList(m.getParameterTypes()).stream()
                                        .map(p -> p.getTypeName())
                                        .collect(toList()))
                                .build();
                    })
                    .collect(toList());

            final Method[] declaredMethods = cls.getDeclaredMethods();
            final List<ReflectionConfigItemMethod> memberMethods = safeArrayToList(declaredMethods).stream()
                    .filter(m -> !Modifier.isStatic(m.getModifiers()))
                    .map(m -> {
                        return ReflectionConfigItemMethod.builder()
                                .name(m.getName())
                                .parameterTypes(safeArrayToList(m.getParameterTypes()).stream()
                                        .map(p -> p.getTypeName())
                                        .collect(toList()))
                                .build();
                    })
                    .collect(toList());

            final List<ReflectionConfigItemMethod> staticMethods = safeArrayToList(cls.getDeclaredMethods()).stream()
                    .filter(m -> Modifier.isStatic(m.getModifiers()))
                    .map(m -> {
                        return ReflectionConfigItemMethod.builder()
                                .name(m.getName())
                                .parameterTypes(safeArrayToList(m.getParameterTypes()).stream()
                                        .map(p -> p.getTypeName())
                                        .collect(toList()))
                                .build();
                    })
                    .collect(toList());

            constructorMethods.addAll(memberMethods);
            constructorMethods.addAll(staticMethods);
            return ReflectionConfigItem.builder().name(cls.getName()).methods(constructorMethods).build();
        }).collect(toList());
    }

    @Getter
    @Setter
    @ToString
    @SuperBuilder
    @NoArgsConstructor
    public static class ReflectionConfigItem {
        private String name;
        private @Default boolean allDeclaredFields = true;
        private @Default boolean allDeclaredMethods = true;
        private @Default boolean allDeclaredConstructors = true;
        private @Default boolean queryAllDeclaredMethods = true;
        private @Default boolean queryAllDeclaredConstructors = true;
        private List<ReflectionConfigItemMethod> methods;
    }

    @Getter
    @Setter
    @ToString
    @SuperBuilder
    @NoArgsConstructor
    public static class ReflectionConfigItemMethod {
        private String name;
        private List<String> parameterTypes;

    }
}

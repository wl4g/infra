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
import static java.lang.String.format;
import static java.lang.System.err;
import static java.lang.System.out;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.endsWithAny;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWithAny;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.wl4g.infra.common.cli.CommandLineTool;
import com.wl4g.infra.common.cli.CommandLineTool.CommandLineFacade;
import com.wl4g.infra.common.reflect.ReflectionUtils3;

import lombok.AllArgsConstructor;
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
        final CommandLineFacade line = CommandLineTool.builder()
                .mustLongOption("classDirs",
                        "The target classes base directorys. eg: $PROJECT1/target/classes,$PROJECT2/target/classes")
                .mustLongOption("classPackages",
                        "The find class packages filter. eg: com.myproject.module1.service,com.myproject.module2.service")
                .longOption("includeLombokTypeMethods", "false",
                        "The whether to includes lombok-generated classes and methods. eg: MyServiceBuilderImpl, MyServiceBuilder.access$4000(MyServiceBuilder), MyService(MyServiceBuilder)")
                .build(args);

        final String classDirs = line.get("classDirs");
        final String classPackages = line.get("classPackages");
        final boolean includeLombokTypeMethods = line.getBoolean("includeLombokTypeMethods");

        final Set<URL> findUrls = safeArrayToList(split(classDirs, ",")).stream().map(baseDir -> {
            try {
                return new File(replace(baseDir, "/", File.separator)).toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }).collect(toSet());
        out.println("finding of URLs: " + findUrls);

        final Set<String> findClassPackages = safeArrayToList(split(classPackages, ",")).stream().collect(toSet());
        out.println("finding of classPackages: " + findClassPackages);

        final Collection<Class<?>> classes = ReflectionUtils3.findClassesAll(findClassPackages, findUrls);
        final String classesString = classes.stream().map(cls -> cls.getName()).collect(joining("\n"));
        out.println("\n----- Found All candidate classes -----\n\n" + classesString);

        final List<ReflectionConfigItem> items = buildReflectionConfigItems(includeLombokTypeMethods, classes);
        out.println("\n\n----- Generated determined reflect-config.json -----\n\n");
        String reflectConfigJson = toJSONString(items, true);

        // Custom humnan line and space format.
        reflectConfigJson = reflectConfigJson.replaceAll("\\},\\{\n  \"name\"", "\\},\n\\{\n  \"name\"")
                .replace("  },{\n    \"name\"", "  },\n    {\n    \"name\"")
                .replace(",\n    \"parameterTypes\" :", ", \"parameterTypes\":")
                .replace("  },\n    {\n    \"name\" :", "    },\n    {\"name\" :")
                .replace("]\n    },", "]},")
                .replace("  \"methods\" : [{", "  \"methods\": [\n    {")
                .replace("{\"name\" :", "{\"name\":")
                .replace("    {\n    \"name\" :", "    {\"name\":")
                .replace("\" :", "\":")
                .replace("[ ]", "[]")
                .replace("]\n  }]", "]}\n  ]");

        out.println(reflectConfigJson);
    }

    public static List<ReflectionConfigItem> buildReflectionConfigItems(
            final boolean includeLombokTypeMethods,
            final Collection<Class<?>> classes) {

        final var lombokTypePredicate = new LombokTypePredicate(includeLombokTypeMethods, classes);
        final var lombokConstructorPredicate = new LombokConstructorPredicate(includeLombokTypeMethods, classes);
        final var lombokMethodPredicate = new LombokMethodPredicate(includeLombokTypeMethods, classes);

        return safeList(classes).stream().filter(lombokTypePredicate).map(cls -> {
            final List<ReflectionConfigItemMethod> allItem = new ArrayList<>(classes.size());
            try {
                final List<ReflectionConfigItemMethod> constructorMethodItems = safeArrayToList(cls.getDeclaredConstructors())
                        .stream()
                        .filter(lombokConstructorPredicate)
                        .map(m -> {
                            try {
                                return ReflectionConfigItemMethod.builder()
                                        .name("<init>")
                                        .parameterTypes(safeArrayToList(m.getParameterTypes()).stream()
                                                .map(p -> p.getTypeName())
                                                .collect(toList()))
                                        .build();
                            } catch (Exception e) {
                                err.println(format("[WARNING] Unable to load constructor methods of %s#%s. reason: %s",
                                        cls.getName(), m.getName(), e.getMessage()));
                                return null;
                            }
                        })
                        .filter(m -> nonNull(m))
                        .collect(toList());
                allItem.addAll(constructorMethodItems);
            } catch (Throwable e) {
                err.println(
                        format("[WARNING] Unable to load constructor methods of %s. reason: %s", cls.getName(), e.getMessage()));
            }

            try {
                final List<ReflectionConfigItemMethod> memberMethodItems = safeArrayToList(cls.getDeclaredMethods()).stream()
                        .filter(m -> !Modifier.isStatic(m.getModifiers()))
                        .filter(lombokMethodPredicate)
                        .map(m -> {
                            try {
                                return ReflectionConfigItemMethod.builder()
                                        .name(m.getName())
                                        .parameterTypes(safeArrayToList(m.getParameterTypes()).stream()
                                                .map(p -> p.getTypeName())
                                                .collect(toList()))
                                        .build();
                            } catch (Exception e) {
                                err.println(format("[WARNING] Unable to load member methods of %s#%s. reason: %s", cls.getName(),
                                        m.getName(), e.getMessage()));
                                return null;
                            }
                        })
                        .filter(m -> nonNull(m))
                        .collect(toList());
                allItem.addAll(memberMethodItems);
            } catch (Throwable e) {
                err.println(format("[WARNING] Unable to load member methods of %s. reason: %s", cls.getName(), e.getMessage()));
            }

            try {
                final List<ReflectionConfigItemMethod> staticMethodItems = safeArrayToList(cls.getDeclaredMethods()).stream()
                        .filter(m -> Modifier.isStatic(m.getModifiers()))
                        .filter(lombokMethodPredicate)
                        .map(m -> {
                            try {
                                return ReflectionConfigItemMethod.builder()
                                        .name(m.getName())
                                        .parameterTypes(safeArrayToList(m.getParameterTypes()).stream()
                                                .map(p -> p.getTypeName())
                                                .collect(toList()))
                                        .build();
                            } catch (Exception e) {
                                err.println(format("[WARNING] Unable to load static methods of %s#%s. reason: %s", cls.getName(),
                                        m.getName(), e.getMessage()));
                                return null;
                            }
                        })
                        .filter(m -> nonNull(m))
                        .collect(toList());
                allItem.addAll(staticMethodItems);
            } catch (Throwable e) {
                err.println(format("[WARNING] Unable to load static methods of %s. reason: %s", cls.getName(), e.getMessage()));
            }

            return ReflectionConfigItem.builder().name(cls.getName()).methods(allItem).build();
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

    @AllArgsConstructor
    public static class LombokTypePredicate implements Predicate<Class<?>> {
        final boolean includeLombokTypeMethods;
        final Collection<Class<?>> classes;

        // for example:
        // MyService(MyServiceBuilder)
        // MyService.$default$myfield(),
        // MyService.builder(),
        // MyService$MyServiceBuilder.build(),
        // MyService$MyServiceBuilder.access$4000(MyServiceBuilder),
        // MyService$MyServiceBuilderImpl,
        @Override
        public boolean test(Class<?> cls) {
            final String className = cls.getSimpleName();
            // Check the inner class and specification suffix.
            if (cls.isMemberClass() && endsWithAny(className, "Builder", "BuilderImpl")) {
                // final List<String> parts = safeArrayToList(split(className,
                // "$"));
                // if (parts.stream().filter(p -> "$".equals(p)).count() >= 1) {
                // return true;
                // }
                return includeLombokTypeMethods;
            }
            return true;
        }
    }

    @AllArgsConstructor
    public static class LombokConstructorPredicate implements Predicate<Constructor<?>> {
        final boolean includeLombokTypeMethods;
        final Collection<Class<?>> classes;

        // for example:
        // MyService(MyServiceBuilder)
        // MyService.$default$myfield(),
        // MyService.builder(),
        // MyService$MyServiceBuilder.build(),
        // MyService$MyServiceBuilder.access$4000(MyServiceBuilder),
        // MyService$MyServiceBuilderImpl,
        @Override
        public boolean test(Constructor<?> c) {
            final var cls = c.getDeclaringClass();
            final var className = cls.getSimpleName();
            final var parameterTypes = c.getParameterTypes();
            // Check the inner class and specification suffix.
            if (cls.isMemberClass() && endsWithAny(className, "Builder", "BuilderImpl")
                    || (parameterTypes.length == 1 && endsWithAny(parameterTypes[0].getTypeName(), "Builder"))) {
                return includeLombokTypeMethods;
            }
            return true;
        }
    }

    @AllArgsConstructor
    public static class LombokMethodPredicate implements Predicate<Method> {
        final boolean includeLombokTypeMethods;
        final Collection<Class<?>> classes;

        // for example:
        // MyService(MyServiceBuilder)
        // MyService.$default$myfield(),
        // MyService.builder(),
        // MyService$MyServiceBuilder.build(),
        // MyService$MyServiceBuilder.access$4000(MyServiceBuilder),
        // MyService$MyServiceBuilderImpl,
        @Override
        public boolean test(Method m) {
            final Class<?> cls = m.getDeclaringClass();
            final String className = cls.getSimpleName();
            final String methodName = m.getName();
            // Check the inner class and specification suffix.
            if (cls.isMemberClass() && endsWithAny(className, "Builder", "BuilderImpl")
                    || startsWithAny(methodName, "builder", "access$", "self", "$default$")) {
                return includeLombokTypeMethods;
            }
            return true;
        }
    }

}

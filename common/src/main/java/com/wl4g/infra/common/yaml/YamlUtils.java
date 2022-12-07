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
package com.wl4g.infra.common.yaml;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.env.EnvScalarConstructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.parser.Parser;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.resolver.Resolver;
import static org.yaml.snakeyaml.env.EnvScalarConstructor.ENV_FORMAT;
import static org.yaml.snakeyaml.env.EnvScalarConstructor.ENV_TAG;

/**
 * {@link YamlUtils}
 * 
 * @author James Wong
 * @version 2022-12-07
 * @since v1.0.0
 * @see https://github1s.com/snakeyaml/snakeyaml/blob/HEAD/src/test/java/org/yaml/snakeyaml/partialconstruct/FragmentComposerTest.java
 * @see https://github1s.com/snakeyaml/snakeyaml/blob/HEAD/src/test/java/org/yaml/snakeyaml/env/EnvLombokTest.java
 */
public abstract class YamlUtils {

    public static <T> T parse(@NotBlank String yaml, @Nullable String rootPath, @NotNull Class<T> clazz) {
        return parse(yaml, null, null, null, clazz);
    }

    public static <T> T parse(
            @NotBlank String yaml,
            @Nullable Constructor constructor,
            @Nullable String rootPath,
            @NotNull Class<T> clazz) {
        return parse(yaml, constructor, null, rootPath, clazz);
    }

    public static <T> T parse(
            @NotBlank String yaml,
            @Nullable LoaderOptions options,
            @Nullable String rootPath,
            @NotNull Class<T> clazz) {
        return parse(yaml, null, options, null, clazz);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> T parse(
            @NotBlank String yaml,
            @Nullable Constructor constructor,
            @Nullable LoaderOptions options,
            @Nullable String rootPath,
            @NotNull Class<T> clazz) {
        hasTextOf(yaml, "yaml");
        if (isNull(options)) {
            options = defaultOptions;
        }
        if (isNull(constructor)) {
            constructor = new Constructor(options);
        }
        final StreamReader reader = new StreamReader(yaml);
        final Resolver resolver = new Resolver();
        if (constructor instanceof EnvScalarConstructor) {
            // see:https://github1s.com/snakeyaml/snakeyaml/blob/HEAD/src/test/java/org/yaml/snakeyaml/env/EnvLombokTest.java#L30
            resolver.addImplicitResolver(ENV_TAG, ENV_FORMAT, "$");
        }
        final Composer composer = new FragmentComposer(new ParserImpl(reader), resolver, options, rootPath);
        constructor.setComposer(composer);
        return (T) constructor.getSingleData(clazz);
    }

    final static class FragmentComposer extends Composer {
        private final String rootPath;
        private final List<String> rootPathParts;

        public FragmentComposer(Parser parser, Resolver resolver, LoaderOptions options, String rootPath) {
            super(parser, resolver, options);
            this.rootPath = trimToEmpty(rootPath);
            this.rootPathParts = nonNull(rootPath) ? asList(split(rootPath, "/")) : emptyList();
        }

        @Override
        public Node getSingleNode() {
            Node node = super.getSingleNode();
            if (!MappingNode.class.isAssignableFrom(node.getClass())) {
                throw new IllegalStateException("Document is not structured as expected.  Root element should be a map!");
            }
            List<String> pathParts = new ArrayList<>(rootPath.length());
            MappingNode root = findRootNode(pathParts, node);
            if (nonNull(root)) {
                return root;
            }
            throw new IllegalArgumentException("Did not find key \"" + rootPath + "\" in document-level map");
        }

        private MappingNode findRootNode(List<String> pathParts, Node node) {
            // if (index >= parts.length) {
            // // return null;
            // throw new IllegalArgumentException(
            // format("Invalid root path '%s', exceeding the depth of map nested
            // objects.", rootPath));
            // }
            if (node instanceof MappingNode) {
                for (NodeTuple tuple : ((MappingNode) node).getValue()) {
                    Node keyNode = tuple.getKeyNode();
                    if (ScalarNode.class.isAssignableFrom(keyNode.getClass())) {
                        String nodeName = ((ScalarNode) keyNode).getValue();
                        pathParts.add(nodeName);
                        if (CollectionUtils.isEqualCollection(pathParts, rootPathParts)) {
                            return (MappingNode) tuple.getValueNode();
                        } else {
                            return findRootNode(pathParts, tuple.getValueNode());
                        }
                    }
                }
            }
            return null;
        }
    }

    final static LoaderOptions defaultOptions = new LoaderOptions();

    static {
        defaultOptions.setAllowDuplicateKeys(false);
        defaultOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
        defaultOptions.setAllowRecursiveKeys(true);
    }

}

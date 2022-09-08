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
package com.wl4g.infra.common.collection;

import static java.util.stream.Collectors.toMap;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.CaseInsensitiveMap;

/**
 * Enhanced collectors utility.
 * 
 * {@link Collectors}
 * 
 * @author James Wong <jameswong1376@gmail.com>
 * @version 2019年12月26日 v1.0.0
 * @see
 */
public abstract class Collectors2 {

    /**
     * Returns a {@code Collector} that accumulates the input elements into a
     * new {@code Set}. There are no guarantees on the type, mutability,
     * serializability, or thread-safety of the {@code Set} returned; if more
     * control over the returned {@code Set} is required, use
     * {@link Collectors#toCollection(Supplier)}.
     *
     * <p>
     * This is an {@link Collector.Characteristics#UNORDERED unordered}
     * Collector.
     *
     * @param <T>
     *                the type of the input elements
     * @return a {@code Collector} which collects all the input elements into a
     *         {@code Set}
     */
    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ?, Set<T>> toLinkedHashSet() {
        return Collector.of(LinkedHashSet::new, Set::add, (s, rs) -> {
            s.add((T) rs);
            return s;
        }, Characteristics.IDENTITY_FINISH);
    }

    /**
     * Returns a {@code Collector} that accumulates elements into a {@code Map}
     * whose keys and values are the result of applying the provided mapping
     * functions to the input elements.
     *
     * <p>
     * If the mapped keys contains duplicates (according to
     * {@link Object#equals(Object)}), the value mapping function is applied to
     * each equal element, and the results are merged using the provided merging
     * function. The {@code Map} is created by a provided supplier function.
     *
     * @implNote The returned {@code Collector} is not concurrent. For parallel
     *           stream pipelines, the {@code combiner} function operates by
     *           merging the keys from one map into another, which can be an
     *           expensive operation. If it is not required that results are
     *           merged into the {@code Map} in encounter order, using
     *           {@link #toConcurrentMap(Function, Function, BinaryOperator, Supplier)}
     *           may offer better parallel performance.
     *
     * @param <T>
     *                          the type of the input elements
     * @param <K>
     *                          the output type of the key mapping function
     * @param <U>
     *                          the output type of the value mapping function
     * @param <M>
     *                          the type of the resulting {@code Map}
     * @param keyMapper
     *                          a mapping function to produce keys
     * @param valueMapper
     *                          a mapping function to produce values
     * @param mergeFunction
     *                          a merge function, used to resolve collisions
     *                          between values associated with the same key, as
     *                          supplied to
     *                          {@link Map#merge(Object, Object, BiFunction)}
     * @param mapSupplier
     *                          a function which returns a new, empty
     *                          {@code Map} into which the results will be
     *                          inserted
     * @return a {@code Collector} which collects elements into a {@code Map}
     *         whose keys are the result of applying a key mapping function to
     *         the input elements, and whose values are the result of applying a
     *         value mapping function to all input elements equal to the key and
     *         combining them using the merge function
     *
     * @see #toMap(Function, Function)
     * @see #toMap(Function, Function, BinaryOperator)
     * @see #toConcurrentMap(Function, Function, BinaryOperator, Supplier)
     */
    @SuppressWarnings("unchecked")
    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toLinkedHashMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return (Collector<T, ?, M>) toMap(keyMapper, valueMapper, (oldValue, newValue) -> oldValue, LinkedHashMap::new);
    }

    /**
     * Returns a {@code Collector} that accumulates elements into a {@code Map}
     * whose keys and values are the result of applying the provided mapping
     * functions to the input elements.
     *
     * <p>
     * If the mapped keys contains duplicates (according to
     * {@link Object#equals(Object)}), the value mapping function is applied to
     * each equal element, and the results are merged using the provided merging
     * function. The {@code Map} is created by a provided supplier function.
     *
     * @implNote The returned {@code Collector} is not concurrent. For parallel
     *           stream pipelines, the {@code combiner} function operates by
     *           merging the keys from one map into another, which can be an
     *           expensive operation. If it is not required that results are
     *           merged into the {@code Map} in encounter order, using
     *           {@link #toConcurrentMap(Function, Function, BinaryOperator, Supplier)}
     *           may offer better parallel performance.
     *
     * @param <T>
     *                          the type of the input elements
     * @param <K>
     *                          the output type of the key mapping function
     * @param <U>
     *                          the output type of the value mapping function
     * @param <M>
     *                          the type of the resulting {@code Map}
     * @param keyMapper
     *                          a mapping function to produce keys
     * @param valueMapper
     *                          a mapping function to produce values
     * @param mergeFunction
     *                          a merge function, used to resolve collisions
     *                          between values associated with the same key, as
     *                          supplied to
     *                          {@link Map#merge(Object, Object, BiFunction)}
     * @param mapSupplier
     *                          a function which returns a new, empty
     *                          {@code Map} into which the results will be
     *                          inserted
     * @return a {@code Collector} which collects elements into a {@code Map}
     *         whose keys are the result of applying a key mapping function to
     *         the input elements, and whose values are the result of applying a
     *         value mapping function to all input elements equal to the key and
     *         combining them using the merge function
     *
     * @see #toMap(Function, Function)
     * @see #toMap(Function, Function, BinaryOperator)
     * @see #toConcurrentMap(Function, Function, BinaryOperator, Supplier)
     * @see https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/java/util/stream/Collectors.java
     */
    @SuppressWarnings("unchecked")
    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toCaseInsensitiveHashMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        // if (java.util.Objects.nonNull(TOMAP_METHOD_WITH_JDK11_PLUS)) {
        return (Collector<T, ?, M>) toMap(keyMapper, valueMapper, (oldValue, newValue) -> oldValue,
                () -> (M) new CaseInsensitiveMap());
        // }
        // return (Collector<T, ?, M>) toMap(keyMapper, valueMapper,
        // (oldValue,newValue) -> oldValue, CaseInsensitiveMap::new);
    }

    // public static final java.lang.reflect.Method
    // TOMAP_METHOD_WITH_JDK11_PLUS =
    // com.wl4g.infra.common.lang.ClassUtils2.getMethodIfAvailable(Collectors.class,
    // "toMap",Function.class,Function.class,BinaryOperator.class,Supplier.class);

}
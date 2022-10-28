/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.core.framework.operator;

import static com.wl4g.infra.common.lang.Assert2.notNull;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.Assert2.state;
import static com.wl4g.infra.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static java.lang.String.format;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.core.ResolvableType;

import com.wl4g.infra.common.collection.OnceUnmodifiableMap;
import com.wl4g.infra.common.log.SmartLogger;

/**
 * Composite generic operator adapter.
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2019年11月1日
 * @since
 */
public abstract class GenericOperatorAdapter<K extends Enum<?>, O extends Operator<K>> implements Operator<K> {

    protected final SmartLogger log = getLogger(getClass());

    /**
     * Generic registrar of operator alias names.
     */
    protected final Map<K, O> operatorAliasRegistry = synchronizedMap(new OnceUnmodifiableMap<>(new HashMap<>()));

    /**
     * Generic registrar of operator classes.
     */
    protected final Map<Class<? extends Operator<Enum<?>>>, O> operatorClassRegistry = synchronizedMap(
            new OnceUnmodifiableMap<>(new HashMap<>()));

    /**
     * Kind type class of operator provider.
     */
    private final Class<? extends Enum<?>> kindClass;

    /**
     * Fallback no operation of operator.
     */
    private final O fallbackNoOp;

    public GenericOperatorAdapter() {
        this(null, null);
    }

    public GenericOperatorAdapter(O fallbackNoOp) {
        this(null, fallbackNoOp);
    }

    public GenericOperatorAdapter(List<O> operators) {
        this(operators, null);
    }

    @SuppressWarnings("unchecked")
    public GenericOperatorAdapter(List<O> operators, O fallbackNoOp) {
        this.fallbackNoOp = fallbackNoOp;

        // Resolving real Kind class.
        ResolvableType resolveType = ResolvableType.forClass(getClass());
        this.kindClass = (Class<? extends Enum<?>>) resolveType.getSuperType().getGeneric(0).resolve();
        Class<?> adapterInterfaceClass = resolveType.getSuperType().getGeneric(1).resolve();
        notNullOf(kindClass, "kindClass");
        notNullOf(adapterInterfaceClass, "adapterInterfaceClass");

        // Maybe none of the specific operators has been instantiated.
        if (!isEmpty(operators)) {
            cleanIgnoreOperators(operators);

            // Duplicate checks.
            Set<K> kinds = new HashSet<>();
            operators.forEach(o -> {
                notNull(o.kind(), format("Provider kind can't empty, operator: %s", o));
                state(!kinds.contains(o.kind()), format("Repeated definition operator with kind: %s", o.kind()));
                kinds.add(o.kind());
            });

            // Register of kind aliases.
            this.operatorAliasRegistry.putAll(operators.stream().collect(toMap(O::kind, o -> o)));
            // Register of kind classes.
            this.operatorClassRegistry
                    .putAll(operators.stream().collect(toMap(o -> (Class<Operator<Enum<?>>>) o.getClass(), o -> o)));

            log.info("Registered operator '{}' instances of: {}", adapterInterfaceClass, operators);
        } else {
            log.warn("Skip '{}' composite adapter registered, because inject operators is empty.", adapterInterfaceClass);
        }

    }

    @Override
    public K kind() {
        // No such situation, It must be ignored.
        throw new UnsupportedOperationException();
    }

    /**
     * Making the adaptation actually execute {@link O}.
     * 
     * @param vcs
     * @return
     * @throws NoSuchOperatorException
     */
    public <T> O forOperator(@NotNull Class<T> operatorClass) throws NoSuchOperatorException {
        O operator = ensureOperator(operatorClassRegistry.get(operatorClass),
                format("No such operator instance of class: '%s'", operatorClass));
        return operator;
    }

    /**
     * Making the adaptation actually execute {@link O}.
     * 
     * @param vcs
     * @return
     */
    public O forOperator(@NotNull K k) throws NoSuchOperatorException {
        return forOperator(k.name());
    }

    /**
     * Making the adaptation actually execute {@link O}.
     *
     * @param P
     * @return
     * @throws NoSuchOperatorException
     */
    public O forOperator(@NotNull String kindName) throws NoSuchOperatorException {
        K kind = parseKind(kindName);
        O operator = ensureOperator(operatorAliasRegistry.get(kind),
                format("No such operator bean instance for kind name: '%s'", kind));
        return operator;
    }

    /**
     * Gets the currently running operator kind.
     * 
     * @return
     */
    public Set<K> getRunningKinds() {
        return operatorAliasRegistry.keySet();
    }

    /**
     * Ensure operator.
     * 
     * @param operator
     * @param assertMsg
     * @return
     */
    private O ensureOperator(O operator, String assertMsg) {
        if (isNull(operator)) {
            if (isNull(fallbackNoOp)) {
                notNull(operator, NoSuchOperatorException.class, assertMsg);
            } else {
                log.warn("Using default fallbackNoOp, caused by: {}", assertMsg);
                operator = fallbackNoOp;
            }
        }
        return operator;
    }

    /**
     * Parse kind instance of kind name.
     * 
     * @param kindName
     * @return
     * @throws NoSuchOperatorException
     */
    @SuppressWarnings("unchecked")
    private K parseKind(@NotNull String kindName) throws NoSuchOperatorException {
        for (Enum<?> kind : kindClass.getEnumConstants()) {
            if (kind.name().equalsIgnoreCase(kindName)) {
                return (K) kind;
            }
        }
        throw new NoSuchOperatorException(format("No such kind: %s of kind class: %s", kindName, kindClass));
    }

    /**
     * Remove the instance that is not supported. For example, it may be the
     * mapperproxy object of mybatis, which cannot generate the kind () method
     * using JDK proxy, and an error will be reported.
     * 
     * @param operators
     */
    private void cleanIgnoreOperators(List<O> operators) {
        if (!isEmpty(operators)) {
            Iterator<O> it = operators.iterator();
            while (it.hasNext()) {
                O o = it.next();
                if (Proxy.class.isInstance(o)) { // JDK proxy bean
                    Object proxyHandler = getField(jdkProxyHandlerField, o);
                    // Remove MapperProxy instance.
                    if (nonNull(mapperProxyClass) && mapperProxyClass.isInstance(proxyHandler)) {
                        it.remove();
                    }
                }
            }
        }
    }

    public static final Field jdkProxyHandlerField = findField(Proxy.class, "h", InvocationHandler.class);
    public static final Class<?> mapperProxyClass = resolveClassNameNullable("org.apache.ibatis.binding.MapperProxy");

}
/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.component.core.framework.operator;

import static java.lang.reflect.Modifier.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import static com.wl4g.component.core.constant.CoreConfigConstant.KEY_GENERIC_OPERATOR;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.AbstractGenericPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * System boot defaults auto configuration.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年2月20日
 * @since
 */
@ConditionalOnProperty(name = KEY_GENERIC_OPERATOR + ".enabled", matchIfMissing = true)
public class GenericOperatorAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(Operator.class)
	public Operator<Enum<?>> emptyOperator() {
		return new EmptyOperator();
	}

	@Bean
	@ConditionalOnBean(Operator.class)
	public OperatorAutoHandlerInterceptor operatorAutoHandlerInterceptor() {
		return new OperatorAutoHandlerInterceptor();
	}

	@Bean
	@ConditionalOnBean(OperatorAutoHandlerInterceptor.class)
	public PointcutAdvisor compositeOperatorAspectJExpressionPointcutAdvisor(OperatorAutoHandlerInterceptor advice) {
		AbstractGenericPointcutAdvisor advisor = new AbstractGenericPointcutAdvisor() {
			final private static long serialVersionUID = 1L;

			@Override
			public Pointcut getPointcut() {
				return new Pointcut() {

					final private List<String> EXCLUDE_METHODS = new ArrayList<String>(4) {
						private static final long serialVersionUID = 3369346948736795743L;
						{
							addAll(asList(Operator.class.getDeclaredMethods()).stream().map(m -> m.getName()).collect(toList()));
							addAll(asList(GenericOperatorAdapter.class.getDeclaredMethods()).stream().map(m -> m.getName())
									.collect(toList()));
							addAll(asList(Object.class.getDeclaredMethods()).stream().map(m -> m.getName()).collect(toList()));
						}
					};

					@Override
					public MethodMatcher getMethodMatcher() {
						return new MethodMatcher() {

							@Override
							public boolean matches(Method method, Class<?> targetClass) {
								Class<?> declareClass = method.getDeclaringClass();
								int mod = method.getModifiers();
								String name = method.getName();
								return !isAbstract(mod) && isPublic(mod) && !isInterface(declareClass.getModifiers())
										&& !EXCLUDE_METHODS.contains(name);
							}

							@Override
							public boolean isRuntime() {
								return false;
							}

							@Override
							public boolean matches(Method method, Class<?> targetClass, Object... args) {
								throw new Error("Shouldn't be here");
							}
						};
					}

					@Override
					public ClassFilter getClassFilter() {
						return clazz -> {
							return Operator.class.isAssignableFrom(clazz) && !GenericOperatorAdapter.class.isAssignableFrom(clazz)
									&& !isAbstract(clazz.getModifiers()) && !isInterface(clazz.getModifiers());
						};
					}
				};
			}
		};
		advisor.setAdvice(advice);
		return advisor;
	}

}
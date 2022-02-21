/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.component.integration.feign.core.context.internal;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.component.common.reflect.ReflectionUtils2.getField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.wl4g.component.integration.feign.core.context.internal.FeignContextCoprocessor.Invokers;

import feign.Feign;
import feign.Feign.Builder;
import feign.InvocationHandlerFactory;
import feign.InvocationHandlerFactory.MethodHandler;
import feign.Target;

/**
 * {@link FeignContextBuilder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-05-20
 * @sine v1.0
 * @see
 */
public class FeignContextBuilder extends Feign.Builder {

	private final Feign.Builder builder;

	public FeignContextBuilder() {
		this(Feign.builder());
	}

	public FeignContextBuilder(Builder builder) {
		this.builder = notNullOf(builder, "builder");
	}

	@Override
	public Feign build() {
		// Let the actual builder set invocationhandlerfactory first.
		// @see:feign.hystrix.HystrixFeign.Builder#build
		// @see:com.alibaba.cloud.sentinel.feign.SentinelFeign.Builder#build
		builder.build();

		// Gets origin invocationHandlerFactory.
		final InvocationHandlerFactory originFactory = getField(invocationHandlerFactoryField, this, true);

		// Override sets detegate invocationHandlerFactory.
		super.invocationHandlerFactory(new InvocationHandlerFactory() {
			@SuppressWarnings("rawtypes")
			@Override
			public InvocationHandler create(Target target, Map<Method, MethodHandler> dispatch) {
				return new FeignContextInvocationHandler(originFactory, target, dispatch);
			}
		});

		return super.build();
	}

	/**
	 * {@link FeignContextInvocationHandler}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2021-05-20
	 * @sine v1.0
	 * @see {@link com.alibaba.cloud.sentinel.feign.SentinelInvocationHandler}
	 */
	static class FeignContextInvocationHandler implements InvocationHandler {
		protected final InvocationHandlerFactory originFactory;
		protected final Target<?> target;
		protected final Map<Method, MethodHandler> dispatch;

		public FeignContextInvocationHandler(InvocationHandlerFactory originFactory, Target<?> target,
				Map<Method, MethodHandler> dispatch) {
			this.originFactory = notNullOf(originFactory, "originFactory");
			this.target = notNullOf(target, "target");
			this.dispatch = notNullOf(dispatch, "dispatch");
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// early exit if the invoked method is from java.lang.Object
			// code is the same as ReflectiveFeign.FeignInvocationHandler
			if ("equals".equals(method.getName())) {
				try {
					Object otherHandler = args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
					return equals(otherHandler);
				} catch (IllegalArgumentException e) {
					return false;
				}
			} else if ("hashCode".equals(method.getName())) {
				return hashCode();
			} else if ("toString".equals(method.getName())) {
				return toString();
			}

			// Call the coprocessor first.
			Invokers.beforeConsumerExecution(proxy, method, args);

			// @see:feign.ReflectiveFeign.FeignInvocationHandler#invoke
			return originFactory.create(target, dispatch).invoke(proxy, method, args);
		}
	}

	private static final Field invocationHandlerFactoryField = findField(feign.Feign.Builder.class, "invocationHandlerFactory",
			InvocationHandlerFactory.class);

}

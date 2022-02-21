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
package com.wl4g.component.core.framework.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.junit.Test;

/**
 * {@link InvocationChainTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-05-02
 * @sine v1.0
 * @see
 */
public class InvocationChainTests {

	@Test
	public void multiFiltersChainInvokeCase1() throws Exception {
		List<SmartProxyFilter> filters = new ArrayList<>();
		filters.add(new MyProxyFilter("filter1"));
		filters.add(new MyProxyFilter("filter2"));
		filters.add(new MyProxyFilter("filter3"));
		Method method = MyHandler.class.getMethod("print");
		new InvocationChain(filters).doInvoke(new MyHandler(), method, new String[0]);
	}

	static class MyProxyFilter implements SmartProxyFilter {

		private final String name;

		public MyProxyFilter(String name) {
			super();
			this.name = name;
		}

		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public boolean supportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
			return true;
		}

		@Override
		public Object doInvoke(@NotNull InvocationChain chain, @NotNull Object target, @NotNull Method method, Object[] args)
				throws Exception {
			System.out.println("before invoking(" + name + ")... target: " + target);
			try {
				return SmartProxyFilter.super.doInvoke(chain, target, method, args);
			} finally {
				System.out.println("after invoked(" + name + ")... target: " + target);
			}
		}

	}

	static class MyHandler {
		public void print() {
			System.out.println("do some thing ....!");
		}
	}

}

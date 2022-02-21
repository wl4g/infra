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
package com.wl4g.component.core.utils.expression;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.wl4g.component.core.utils.expression.SpelExpressions;

/**
 * {@link SpelExpressionsTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-15
 * @sine v1.0.0
 * @see
 */
public class SpelExpressionsTests {

	@Test
	public void mapSpelCase() {
		Map<String, Object> model = new HashMap<>();
		model.put("name", "Mia");
		model.put("age", 25);
		model.put("isAmerican", true);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("workyear", 1);
		attributes.put("income", 20000);
		model.put("attributes", attributes);

		String result1 = SpelExpressions.create().resolve("#{name}", model);
		System.out.println(result1);

		Object result2 = SpelExpressions.create().resolve("#{isAmerican}", model);
		System.out.println(result2);
	}

	@Test
	public void stringMethodSpelCase() {
		String expression = "#{'Hi, everybody'.contains('Hi')}";
		System.out.println("contains: " + SpelExpressions.create().resolve(expression, null));
	}

	@Test
	public void directCallMethodSpelCase() {
		String expression = "#{T(com.wl4g.component.core.utils.expression.SpelExpressionsTests.JoinUtil).join(name)}";
		Map<String, Object> model = new HashMap<>();
		model.put("name", "Mia");
		System.out.println("result: " + SpelExpressions.create().resolve(expression, model));
	}

	@Test
	public void aliasCallMethodSpelCase() {
		String expression = "#{T(SpelExpressionsTests$JoinUtil).join(name)}";
		Map<String, Object> model = new HashMap<>();
		model.put("name", "Mia");
		System.out.println("result: " + SpelExpressions.create(JoinUtil.class).resolve(expression, model));
	}

	@Test
	public void modelCallMethodSpelCase() {
		String expression = "#{JoinUtil.join(name)}";
		Map<String, Object> model = new HashMap<>();
		model.put("name", "Mia");
		model.put("JoinUtil", JoinUtil.class);
		System.out.println("result: " + SpelExpressions.create().resolve(expression, model));
	}

	@Test
	public void nonStringArgCallMethodSpelCase() {
		String expression = "#{T(com.wl4g.component.core.utils.expression.SpelExpressionsTests.JoinUtil).show(T(String).class)}";
		System.out.println("result: " + SpelExpressions.create().resolve(expression, null));
	}

	@Test
	public void modelCallObjectMethodSpelCase() {
		String expression = "#{joinUtil.join(name)}";
		Map<String, Object> model = new HashMap<>();
		model.put("joinUtil", new JoinUtil());
		model.put("name", "Mia");
		System.out.println("result: " + SpelExpressions.create().resolve(expression, model));
	}

	/**
	 * <b style='color:red'>[Warning]</b> this is a reflection of remote command
	 * execution attack test, please call the external program must pay
	 * attention to, such as setting white list filtering.</br>
	 * </br>
	 * 
	 * for example1:
	 * 
	 * <pre>
	 * String expression = "#{T(com.wl4g.component.common.reflect.ReflectionUtils2).invokeMethod(T(com.wl4g.component.common.reflect.ReflectionUtils2).findMethod(T(java.lang.Class).forName(\"com.wl4g.component.common.cli.ProcessUtils\"),\"execSingle\"),null,\"rm -rf /tmp/test1\")}";
	 * System.out.println("result: " + SpelExpressions.create().resolve(expression, null));
	 * </pre>
	 * 
	 * for example2:
	 * 
	 * <pre>
	 * String expression = "#{T(com.wl4g.component.common.cli.ProcessUtils).execSingle(\"rm -rf /tmp/test1\")}";
	 * System.out.println("result: " + SpelExpressions.create().resolve(expression, null));
	 * </pre>
	 * 
	 * @throws ClassNotFoundException
	 */
	@Test
	public void invokeAttackSpelCase() throws ClassNotFoundException {
		String expression = "#{T(com.wl4g.component.common.cli.ProcessUtils).execSingle(\"rm -rf /tmp/test1\")}";
		System.out.println("result: " + SpelExpressions.create().resolve(expression, null));
	}

	public static class JoinUtil {
		public static String join(String str) {
			return format("%s nationality is America", str);
		}

		public static String show(Class<?> clazz) {
			return format("Show input parameters: %s", clazz);
		}
	}

}

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
package com.wl4g.component.common.view;

import static java.lang.String.format;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import freemarker.template.TemplateException;

import static com.wl4g.component.common.view.Freemarkers.renderingTemplateToString;

/**
 * {@link FreemarkersTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-15
 * @sine v1.0.0
 * @see
 */
public class FreemarkersTests {

	@Test
	public void mapRenderingCase() throws Exception {
		String templateString = "${name}";

		Map<String, Object> model = new HashMap<>();
		model.put("name", "Mia");
		model.put("age", 25);
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("workyear", 1);
		attributes.put("income", 20000);
		model.put("attributes", attributes);

		System.out.println(renderingTemplateToString("mapRenderingCase", templateString, model));
	}

	@Test
	public void stringMethodSpelCase() throws IOException, TemplateException {
		String templateString = "${'Hi, everybody'.contains('Hi')}";
		System.out.println("contains: " + renderingTemplateToString("stringMethodSpelCase", templateString, null));
	}

	@Test
	public void modelCallObjectMethodRenderingCase() throws IOException, TemplateException {
		String templateString = "${joinUtil.join(name)}";
		Map<String, Object> model = new HashMap<>();
		model.put("name", "Mia");
		model.put("joinUtil", new JoinHolder());

		System.out.println("result: " + renderingTemplateToString("modelCallObjectMethodRenderingCase", templateString, model));
	}

	@Test
	public void modelCallMethodRenderingCase_with_error() throws IOException, TemplateException {
		String templateString = "${JoinUtil.join(name)}";
		Map<String, Object> model = new HashMap<>();
		model.put("name", "Mia");
		model.put("JoinUtil", JoinHolder.class);

		System.out.println("result: " + renderingTemplateToString("modelCallMethodRenderingCase", templateString, model));
	}

	public static class JoinHolder {
		public static String join(String str) {
			return format("%s nationality is America", str);
		}
	}

}

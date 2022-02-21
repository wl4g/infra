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
package com.wl4g.component.common.serialize;

import static com.wl4g.component.common.serialize.JacksonUtils.deepClone;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static java.lang.System.out;
import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacksonUtilsTests {

	public static void main(String[] args) {
		TestBar bar = new TestBar("myBar");
		TestBean1 bean1 = new TestBean1(1313466574534868992L, "jack", singletonMap("foo", toJSONString(bar)));

		String json = toJSONString(bean1);
		out.println("Serialization...");
		out.println(json);

		out.println("Deserialization...");
		out.println(parseJSON(json, TestBean1.class));

		out.println("deepClone0...");
		out.println(deepClone(new ArrayList<>()));

		out.println("deepClone1...");
		List<TestBar> list1 = new ArrayList<>();
		list1.add(new TestBar("myBar00"));
		out.println(deepClone(list1));

		out.println("deepClone2...");
		Map<String, TestBar> map = new HashMap<>();
		map.put("bar1", new TestBar("myBar11"));
		out.println(deepClone(map));

		out.println("deepClone3...");
		Map<String, List<Map<String, TestBar>>> map2 = new HashMap<>();
		Map<String, TestBar> map21 = new HashMap<>();
		map21.put("bar21", new TestBar("myBar211"));
		List<Map<String, TestBar>> list2 = new ArrayList<>();
		list2.add(map21);
		map2.put("bar2", list2);
		out.println(deepClone(map2));
	}

	public static class TestBean1 {

		private long id;

		private String name;

		private Map<String, String> attributes = new HashMap<>();

		public TestBean1() {
			super();
		}

		public TestBean1(long id, String name, Map<String, String> attributes) {
			super();
			this.id = id;
			this.name = name;
			this.attributes = attributes;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Map<String, String> getAttributes() {
			return attributes;
		}

		public void setAttributes(Map<String, String> attributes) {
			this.attributes = attributes;
		}

		@Override
		public String toString() {
			return "TestBean1 [id=" + id + ", name=" + name + ", attributes=" + attributes + "]";
		}

	}

	public static class TestBar {

		private String barName;

		public TestBar() {
			super();
		}

		public TestBar(String barName) {
			super();
			this.barName = barName;
		}

		public String getBarName() {
			return barName;
		}

		public void setBarName(String barName) {
			this.barName = barName;
		}

	}

}
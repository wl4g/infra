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
package com.wl4g.component.integration.feign.core.codec;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link GsonTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-28
 * @sine v1.0
 * @see
 */
public class GsonTests {
	static String arrayJson = "[{\"id\":\"123\",\"name\":\"jack123\"}]";

	@Test
	public void deserializeObjectListCase1() {
		Gson gson = new Gson();
		List<JsonObject> jsonArray = gson.fromJson(arrayJson, new TypeToken<ArrayList<JsonObject>>() {
		}.getType());
		System.out.println(jsonArray);
	}

	@Test
	public void deserializeObjectListCase2() {
		Gson gson = new Gson();
		List<JsonObject> jsonArray = gson.fromJson(arrayJson, TypeToken.getParameterized(List.class, MyUser.class).getType());
		System.out.println(jsonArray);
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	static class MyUser {
		private String id;
		private String name;
	}

}

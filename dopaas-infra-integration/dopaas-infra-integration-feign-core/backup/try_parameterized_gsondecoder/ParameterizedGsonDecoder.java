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
package com.wl4g.component.integration.springboot.feign.codec;

import static feign.Util.UTF_8;
import static feign.Util.ensureClosed;
import static feign.Util.resolveLastTypeParameter;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import feign.Response;
import feign.gson.DoubleToIntMapTypeAdapter;
import feign.gson.GsonDecoder;

/**
 * Enhanced {@link GsonDecoder} implementation, support {@link List} and
 * {@link Set} deserialization.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-28
 * @sine v1.0
 * @see
 */
@Deprecated
public class ParameterizedGsonDecoder extends SmartErrorDecoder {

	private final Gson gson;

	public ParameterizedGsonDecoder(Iterable<TypeAdapter<?>> adapters) {
		this(GsonFactory.create(adapters));
	}

	public ParameterizedGsonDecoder() {
		this(Collections.<TypeAdapter<?>> emptyList());
	}

	public ParameterizedGsonDecoder(Gson gson) {
		this.gson = gson;
	}

	@Override
	public Object doDecode(Response response, Type type) throws IOException {
		if (response.body() == null)
			return null;
		Reader reader = response.body().asReader(UTF_8);
		// String json = CharStreams.toString(reader);
		try {
			// if (anyTypeOf(type, List.class, Set.class)) {
			// // return
			// // gson.fromJson(reader,TypeToken.getArray(type).getType());
			// Type arrayType = new TypeToken<List<JsonObject>>() {
			// }.getType();
			// List<JsonObject> jsonArray = gson.fromJson(json, arrayType);
			// if (type instanceof ParameterizedType) {
			// Type actualType = ((ParameterizedType)
			// type).getActualTypeArguments()[0];
			// return safeList(jsonArray).stream().map(jsonObj ->
			// gson.fromJson(jsonObj, actualType));
			// }
			// throw new UnsupportedOperationException();
			// } else {
			// return gson.fromJson(json, type);
			// }

			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				return gson.fromJson(reader,
						TypeToken.getParameterized(pType.getRawType(), pType.getActualTypeArguments()).getType());
			}
			return gson.fromJson(reader, type);
		} catch (Exception e) {
			if (e.getCause() != null && e.getCause() instanceof IOException) {
				throw IOException.class.cast(e.getCause());
			}
			throw e;
		} finally {
			ensureClosed(reader);
		}
	}

	final static class GsonFactory {

		private GsonFactory() {
		}

		/**
		 * Registers type adapters by implicit type. Adds one to read numbers in
		 * a {@code Map<String,
		 * Object>} as Integers.
		 */
		static Gson create(Iterable<TypeAdapter<?>> adapters) {
			GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
			builder.registerTypeAdapter(new TypeToken<Map<String, Object>>() {
			}.getType(), new DoubleToIntMapTypeAdapter());
			for (TypeAdapter<?> adapter : adapters) {
				Type type = resolveLastTypeParameter(adapter.getClass(), TypeAdapter.class);
				builder.registerTypeAdapter(type, adapter);
			}
			return builder.create();
		}
	}

}

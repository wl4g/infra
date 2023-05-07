/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * James Wong <jameswong1376@gmail.com> Technology CO.LTD.
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
 * Reference to website: https://wl4g.github.io
 */
package com.wl4g.infra.integration.springboot.feign.web.method;

import static com.wl4g.infra.common.reflect.TypeUtils2.isSimpleType;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.util.Collection;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.web.method.HandlerMethodCustomizerInterceptor.HandlerProcessor;

/**
 * {@link FeignResponseWrapperProcessor}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2020-12-28
 * @since v2.0
 * @see
 */
public class FeignResponseWrapperProcessor implements HandlerProcessor {

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 200;
	}

	@Override
	public Object postHandle(@NotNull Object bean, @NotNull Method method, Object[] parameters, Object result) {
		if (nonNull(result)) {
			if ((result instanceof Collection) || isSimpleType(result.getClass())) {
				// If the return value is not a normal object type, it is
				// automatically packaged as an object type. (To solve the
				// problem that some serialization frameworks cannot deserialize
				// the collection)
				return new RespBase<>().withData(result);
			}
		}
		return result;
	}

	@Configuration
	static class FeignResponseWrapperAutoConfiguration {
		@Bean
		public FeignResponseWrapperProcessor feignResponseWrapperProcessor() {
			return new FeignResponseWrapperProcessor();
		}
	}

}

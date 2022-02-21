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
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.config.annotation.Reference;
import feign.Feign;
import feign.Target;

import static com.wl4g.component.common.reflect.ReflectionUtils2.findField;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * {@link DubboFeignBuilder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-21
 * @sine v1.0
 * @see
 */
public class DubboFeignBuilder extends Feign.Builder {

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * Default dubbo {@link Reference} instance.
	 */
	public final Reference defaultReference;

	public DubboFeignBuilder() {
		final class DefaultReferenceClass {
			/**
			 * Dubbo starts checking earlier than Eureka when check is set to
			 * false
			 */
			@Reference(check = false)
			String field;
		}
		// Generate @Reference default instance.
		this.defaultReference = findField(DefaultReferenceClass.class, "field").getAnnotation(Reference.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T target(Target<T> target) {
		try {
			ReferenceBeanBuilder builder = ReferenceBeanBuilder
					.create(defaultReference, target.getClass().getClassLoader(), applicationContext)
					.interfaceClass(target.type());
			return (T) builder.build().getObject();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
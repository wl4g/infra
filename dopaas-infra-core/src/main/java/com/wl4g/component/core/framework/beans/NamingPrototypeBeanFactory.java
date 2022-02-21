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
package com.wl4g.component.core.framework.beans;

import static com.wl4g.component.common.lang.Assert2.*;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.annotation.Nullable;

import org.springframework.beans.factory.BeanFactory;

/**
 * Naming prototype bean factory.</br>
 * 
 * @author Wangl.sir <Wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0.0 2019-10-09
 * @since
 */
public class NamingPrototypeBeanFactory {

	/**
	 * Global delegate alias prototype bean class registry.
	 */
	private final Map<String, String> knownPrototypeBeanAlias = synchronizedMap(new HashMap<>(8));

	private final BeanFactory beanFactory;

	public NamingPrototypeBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = notNullOf(beanFactory, "beanFactory");
	}

	/**
	 * Get and create prototype bean instance by alias.
	 * 
	 * @param alias
	 * @param args
	 * @return
	 */
	public <T> T getPrototypeBean(@NotBlank String alias) {
		return getPrototypeBean(alias, new Object[] {});
	}

	/**
	 * Gets and create prototype bean instance by alias.
	 * 
	 * @param alias
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getPrototypeBean(@NotBlank String alias, @Nullable Object... args) {
		hasTextOf(alias, "alias");
		String beanName = knownPrototypeBeanAlias.get(alias);
		notNull(beanName, "No such prototype bean name for 'alias=%s'", alias);
		return (T) beanFactory.getBean(beanName, args);
	}

	/**
	 * 
	 * Register prototype bean name to alias mapping.
	 * 
	 * @param alias
	 * @param beanName
	 * @return
	 */
	boolean registerBeanAlias(String alias, String beanName) {
		return isNull(knownPrototypeBeanAlias.putIfAbsent(alias, beanName));
	}

}
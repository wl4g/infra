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
package com.wl4g.component.integration.feign.core.extension;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmptyArray;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.wl4g.component.core.bean.BaseBean;
import com.wl4g.component.integration.feign.core.context.internal.FeignContextCoprocessor;

import feign.Response;

/**
 * {@link InsertBeanBindingCoprocessor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-05-20
 * @sine v1.0
 * @see
 */
public class InsertBeanBindingCoprocessor implements FeignContextCoprocessor {

	@Override
	public void beforeConsumerExecution(@NotNull Object proxy, @NotNull Method method, @Nullable Object[] args) {
		if (!isEmptyArray(args)) {
			for (Object arg : args) {
				if (nonNull(arg) && BaseBean.class.isAssignableFrom(arg.getClass())) {
					BaseBean.InternalUtil.bind((BaseBean) arg);
					break;
				}
			}
		}
	}

	@Override
	public void afterConsumerExecution(@NotNull Response response, Type type) {
		// Update current inserted bean id.
		BaseBean.InternalUtil.update();
	}

}

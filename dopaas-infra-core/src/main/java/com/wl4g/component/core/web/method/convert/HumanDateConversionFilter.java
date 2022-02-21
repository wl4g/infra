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
package com.wl4g.component.core.web.method.convert;

import static com.wl4g.component.core.constant.CoreConfigConstant.KEY_WEB_HUMAN_DATE_CONVERTER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wl4g.component.common.lang.period.PeriodFormatter;
import com.wl4g.component.common.web.rest.RespBase;
import com.wl4g.component.core.bean.BaseBean;
import com.wl4g.component.core.framework.proxy.InvocationChain;
import com.wl4g.component.core.framework.proxy.SmartProxyFilter;

/**
 * {@link HumanDateConversionFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-21
 * @sine v1.0
 * @see
 */
public class HumanDateConversionFilter implements SmartProxyFilter {

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 100;
	}

	@Override
	public boolean supportTypeProxy(Object bean, Class<?> actualOriginalTargetClass) {
		// support all web mappingHandler type
		return hasAnnotation(actualOriginalTargetClass, Controller.class)
				|| hasAnnotation(actualOriginalTargetClass, RequestMapping.class);
	}

	@Override
	public boolean supportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass, Object... args) {
		// support has @ResponseBody method.
		return hasAnnotation(actualOriginalTargetClass, ResponseBody.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object doInvoke(@NotNull InvocationChain chain, @NotNull Object target, @NotNull Method method, Object[] args)
			throws Exception {
		// Invoke actual method.
		Object result = chain.doInvoke(target, method, args);

		// Update date fields to human formatted.
		if (nonNull(result)) {
			if (result instanceof RespBase) {
				Object data = ((RespBase) result).getData();
				updateHumanDateIfNecessary(data);
			} else {
				updateHumanDateIfNecessary(result);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static void updateHumanDateIfNecessary(Object data) {
		if (data instanceof BaseBean) {
			updateFieldHumanDateIfNecessary(data);
		} else if (data instanceof Collection) {
			Collection<Object> elements = (Collection<Object>) data;
			for (Object element : elements) {
				updateFieldHumanDateIfNecessary(element);
			}
		} else if (data instanceof Map) {
			Map<Object, Object> elements = (Map<Object, Object>) data;
			elements.forEach((key, value) -> {
				updateFieldHumanDateIfNecessary(key);
				updateFieldHumanDateIfNecessary(value);
			});
		}
	}

	/**
	 * Process model date attribute to human format.
	 * 
	 * @param model
	 * @return
	 */
	public static boolean updateFieldHumanDateIfNecessary(Object model) {
		if (model instanceof BaseBean) {
			BaseBean bean = (BaseBean) model;
			if (!isNull(bean) && (isNull(bean.getHumanCreateDate()) || isNull(bean.getHumanCreateDate()))) {
				bean.setHumanCreateDate(
						isNull(bean.getCreateDate()) ? null : defaultFormatter.formatHumanDate(bean.getCreateDate().getTime()));
				bean.setHumanUpdateDate(
						isNull(bean.getUpdateDate()) ? null : defaultFormatter.formatHumanDate(bean.getUpdateDate().getTime()));
			}
			return true;
		}
		return false;
	}

	/*
	 * Human date formatter instance.
	 */
	private static final PeriodFormatter defaultFormatter = PeriodFormatter.getDefault().ignoreLowerDate(true);

	@Configuration
	@ConditionalOnProperty(name = KEY_WEB_HUMAN_DATE_CONVERTER + ".enabled", matchIfMissing = true)
	static class HumanDateConversionAutoConfiguration {
		@Bean
		public HumanDateConversionFilter humanDateConversionProcessor() {
			return new HumanDateConversionFilter();
		}
	}

}

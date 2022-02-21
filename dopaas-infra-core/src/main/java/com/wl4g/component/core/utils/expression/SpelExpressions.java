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

import javax.annotation.Nullable;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeLocator;
import org.springframework.util.ClassUtils;

import com.wl4g.component.common.function.CallbackFunction;

import javax.validation.constraints.NotBlank;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;

/**
 * {@link SpelExpressions}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-15
 * @sine v1.0.0
 * @see
 */
public abstract class SpelExpressions {

	/** Class load of package prefixs. */
	private List<String> knownPackagePrefixes = new ArrayList<>(4);

	private SpelExpressions() {
	}

	/**
	 * New create {@link SpelExpressions}
	 * 
	 * @param classes
	 * @return
	 */
	public static SpelExpressions create(Class<?>... classes) {
		SpelExpressions instance = new SpelExpressions() {
		};
		if (!isEmptyArray(classes)) {
			for (Class<?> cls : classes) {
				/**
				 * @see {@link org.springframework.expression.spel.support.StandardTypeLocator#knownPackagePrefixes}
				 */
				String packagePrefix = cls.getName();
				// inner class for example:
				// com.mycompany.myproject.bean.User$WorkInfo
				packagePrefix = packagePrefix.substring(0, packagePrefix.lastIndexOf("."));
				instance.knownPackagePrefixes.add(packagePrefix);
			}
		}
		return instance;
	}

	/**
	 * New create {@link SpelExpressions}
	 * 
	 * @param packagePrefixs
	 * @return
	 */
	public static SpelExpressions createWithPackages(String... packagePrefixs) {
		SpelExpressions instance = new SpelExpressions() {
		};
		if (!isEmptyArray(packagePrefixs)) {
			for (String prefix : packagePrefixs) {
				/**
				 * @see {@link org.springframework.expression.spel.support.StandardTypeLocator#knownPackagePrefixes}
				 */
				instance.knownPackagePrefixes.add(prefix);
			}
		}
		return instance;
	}

	/**
	 * Resolving spring expression to real value.
	 * 
	 * @param expression
	 * @return
	 */
	public <T> T resolve(@NotBlank String expression) throws EvaluationException {
		hasTextOf(expression, "expression");
		return resolve(expression, null);
	}

	/**
	 * Resolving spring expression to real value.
	 * 
	 * @param expression
	 * @param model
	 * @return
	 */
	public <T> T resolve(@NotBlank String expression, @Nullable Object model) throws EvaluationException {
		return resolve(expression, model, null);
	}

	/**
	 * Resolving spring expression to real value.
	 * 
	 * @param expression
	 * @param model
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public <T> T resolve(@NotBlank String expression, @Nullable Object model,
			@Nullable CallbackFunction<EvaluationContext> customizer) throws EvaluationException {
		hasTextOf(expression, "expression");

		// Create expression parser.
		StandardEvaluationContext context = new StandardEvaluationContext(model);
		StandardTypeLocator locator = new StandardTypeLocator(ClassUtils.getDefaultClassLoader());
		safeList(knownPackagePrefixes).forEach(p -> locator.registerImport(p));
		context.setTypeLocator(locator);
		context.setPropertyAccessors(defaultPropertyAccessors);

		// Customize evaluation context.
		if (nonNull(customizer)) {
			try {
				customizer.process(context);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}

		return (T) defaultParser.parseExpression(expression, ParserContext.TEMPLATE_EXPRESSION).getValue(context);
	}

	/**
	 * Check if it can be a spel template expression. refer:
	 * {@link ParserContext#TEMPLATE_EXPRESSION}
	 * 
	 * @param expectExpr
	 * @return
	 */
	public static boolean hasSpelTemplateExpr(@NotBlank String expectExpr) {
		int startIndex = expectExpr.indexOf("#{");
		int endIndex = expectExpr.lastIndexOf("}");
		return startIndex >= 0 && startIndex < endIndex;
	}

	/**
	 * Wrapper as spel template expression. refer:
	 * {@link ParserContext#TEMPLATE_EXPRESSION}
	 * 
	 * @param maybeExpr
	 * @return
	 */
	public static String wrapExprSpelTemplate(@NotBlank String maybeExpr) {
		return hasSpelTemplateExpr(maybeExpr) ? maybeExpr : "#{".concat(maybeExpr).concat("}");
	}

	/** {@link ExpressionParser} */
	private static final ExpressionParser defaultParser = new SpelExpressionParser();

	/** {@link PropertyAccessor} */
	@SuppressWarnings("serial")
	private static final List<PropertyAccessor> defaultPropertyAccessors = new ArrayList<PropertyAccessor>() {
		{
			// supported for map
			add(new MapAccessor());
			// supported for bean(default)
			add(new ReflectivePropertyAccessor());
		}
	};

}

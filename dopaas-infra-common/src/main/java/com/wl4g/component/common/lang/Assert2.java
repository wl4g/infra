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
package com.wl4g.component.common.lang;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import com.wl4g.component.common.reflect.ObjectInstantiators;

import static com.wl4g.component.common.lang.StringUtils2.*;
import static java.lang.Math.max;
import static java.lang.String.format;

/**
 * Assertion utility class that assists in validating arguments.
 *
 * <p>
 * Useful for identifying programmer errors early and clearly at runtime.
 *
 * <p>
 * For example, if the contract of a public method states it does not allow
 * {@code null} arguments, {@code Assert} can be used to validate that contract.
 * Doing this clearly indicates a contract violation when it occurs and protects
 * the class's invariants.
 *
 * <p>
 * Typically used to validate method arguments rather than configuration
 * properties, to check for cases that are usually programmer errors rather than
 * configuration errors. In contrast to configuration initialization code, there
 * is usually no point in falling back to defaults in such methods.
 *
 * <p>
 * This class is similar to JUnit's assertion library. If an argument value is
 * deemed invalid, an {@link IllegalArgumentException} is thrown (typically).
 * For example:
 *
 * <pre class="code">
 * Assert2.notNull(clazz, "The class must not be null");
 * Assert2.isTrue(i > 0, "The value must be greater than zero");
 * </pre>
 *
 * <p>
 * Mainly for internal use within the framework; consider
 * <a href="http://commons.apache.org/proper/commons-lang/">Apache's Commons
 * Lang</a> for a more comprehensive suite of {@code String} utilities.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Colin Sampaleanu
 * @author Rob Harrop
 * @since 1.1.2
 */
public abstract class Assert2 {

	/**
	 * Assert a boolean expression, throwing an {@code IllegalStateException} if
	 * the expression evaluates to {@code false}.
	 * <p>
	 * Call {@link #isTrue} if you wish to throw an
	 * {@code IllegalArgumentException} on an assertion failure.
	 * 
	 * <pre class="code">
	 * Assert2.state(id == null, "The id property must not already be initialized");
	 * </pre>
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalStateException
	 *             if {@code expression} is {@code false}
	 */
	public static void state(boolean expression, String fmtMessage, Object... args) {
		if (!expression) {
			throw new IllegalStateException(ASSERT_FAILED_PREFIX.concat(doFormat(fmtMessage, args)));
		}
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalStateException} if
	 * the expression evaluates to {@code false}.
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param argName
	 */
	public static void stateOf(boolean expression, String argName) {
		state(expression, argName + " condition miss");
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalArgumentException}
	 * if the expression evaluates to {@code false}.
	 * 
	 * <pre class="code">
	 * Assert2.isTrue(i &gt; 0, "The value must be greater than zero");
	 * </pre>
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if {@code expression} is {@code false}
	 */
	public static void isTrue(boolean expression, String fmtMessage, Object... args) {
		if (!expression) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(doFormat(fmtMessage, args)));
		}
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalArgumentException}
	 * if the expression evaluates to {@code false}.
	 * 
	 * <pre class="code">
	 * Assert2.isTrue(i &gt; 0, "The value must be greater than zero");
	 * </pre>
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @throws IllegalArgumentException
	 *             if {@code expression} is {@code false}
	 */
	public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
		if (!expression) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(nullSafeGet(messageSupplier)));
		}
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalArgumentException}
	 * if the expression evaluates to {@code false}.
	 * 
	 * <pre class="code">
	 * Assert2.isTrue(i &gt; 0, "The value must be greater than zero");
	 * </pre>
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if {@code expression} is {@code false}
	 */
	public static void isTrue(boolean expression, Class<? extends RuntimeException> exceptionClass, String fmtMessage,
			Object... args) {
		if (!expression) {
			doWrapException(exceptionClass, fmtMessage, args);
		}
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalArgumentException}
	 * if the expression evaluates to {@code false}.
	 * 
	 * <pre class="code">
	 * Assert2.isTrue(i &gt; 0, "The value must be greater than zero");
	 * </pre>
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param exceptionClass
	 *            Throwable class
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @throws IllegalArgumentException
	 *             if {@code expression} is {@code false}
	 */
	public static void isTrue(boolean expression, Class<? extends RuntimeException> exceptionClass,
			Supplier<String> messageSupplier) {
		if (!expression) {
			doWrapException(exceptionClass, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalArgumentException}
	 * if the expression evaluates to {@code false}.
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param argName
	 */
	public static void isTrueOf(boolean expression, String argName) {
		isTrue(expression, argName + " condition miss");
	}

	/**
	 * Assert a boolean expression, throwing an {@code IllegalArgumentException}
	 * if the expression evaluates to {@code false}.
	 * 
	 * @param expression
	 *            a boolean expression
	 * @param exceptionClass
	 * @param argName
	 */
	public static void isTrueOf(boolean expression, Class<? extends RuntimeException> exceptionClass, String argName) {
		isTrue(expression, exceptionClass, argName + " condition must be true");
	}

	/**
	 * Assert that an object is {@code null}.
	 * 
	 * <pre class="code">
	 * Assert2.isNull(value, "The value must be null");
	 * </pre>
	 * 
	 * @param object
	 *            the object to check
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object is not {@code null}
	 */
	public static void isNull(Object object, String fmtMessage, Object... args) {
		if (object != null) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(doFormat(fmtMessage, args)));
		}
	}

	/**
	 * Assert that an object is {@code null}.
	 * 
	 * <pre class="code">
	 * Assert2.isNull(value, "The value must be null");
	 * </pre>
	 * 
	 * @param object
	 *            the object to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T isNull(Object object, Class<? extends RuntimeException> exceptionClass, String fmtMessage,
			Object... args) {
		if (object != null) {
			doWrapException(exceptionClass, fmtMessage, args);
		}
		return (T) object;
	}

	/**
	 * Assert that an object is {@code null}.
	 * 
	 * @param object
	 *            the object to check
	 * @param argName
	 */
	public static void isNullOf(Object object, String argName) {
		isNull(object, argName + " must be null");
	}

	/**
	 * Assert that an object is not {@code null}.
	 * 
	 * <pre class="code">
	 * Assert2.notNull(clazz, "The class must not be null");
	 * </pre>
	 * 
	 * @param object
	 *            the object to check
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notNull(Object object, String fmtMessage, Object... args) {
		if (object == null) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX + doFormat(fmtMessage, args));
		}
		return (T) object;
	}

	/**
	 * Assert that an object is not {@code null}.
	 * 
	 * <pre class="code">
	 * Assert2.notNull(clazz, "The class must not be null");
	 * </pre>
	 * 
	 * @param object
	 *            the object to check
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @throws IllegalArgumentException
	 *             if the object is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notNull(Object object, Supplier<String> messageSupplier) {
		if (object == null) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(nullSafeGet(messageSupplier)));
		}
		return (T) object;
	}

	/**
	 * Assert that an object is not {@code null}.
	 * 
	 * <pre class="code">
	 * Assert2.notNull(clazz, "The class must not be null");
	 * </pre>
	 * 
	 * @param object
	 *            the object to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notNull(Object object, Class<? extends RuntimeException> exceptionClass, String fmtMessage,
			Object... args) {
		if (object == null) {
			doWrapException(exceptionClass, fmtMessage, args);
		}
		return (T) object;
	}

	/**
	 * Assert that an object is not {@code null}.
	 * 
	 * <pre class="code">
	 * Assert2.notNull(clazz, "The class must not be null");
	 * </pre>
	 * 
	 * @param object
	 *            the object to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @throws IllegalArgumentException
	 *             if the object is {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notNull(Object object, Class<? extends RuntimeException> exceptionClass,
			Supplier<String> messageSupplier) {
		if (object == null) {
			doWrapException(exceptionClass, nullSafeGet(messageSupplier));
		}
		return (T) object;
	}

	/**
	 * Assert that an object is not {@code null}.
	 * 
	 * @param object
	 *            the object to check
	 * @param argName
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notNullOf(Object object, String argName) {
		notNull(object, argName + " is required");
		return (T) object;
	}

	/**
	 * Assert that an object is not {@code null}.
	 * 
	 * @param object
	 *            the object to check
	 * @param exceptionClass
	 * @param argName
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notNullOf(Object object, Class<? extends RuntimeException> exceptionClass, String argName) {
		notNull(object, exceptionClass, argName + " is required");
		return (T) object;
	}

	/**
	 * Assert that the given String is not empty; that is, it must not be
	 * {@code null} and not the empty String.
	 * 
	 * <pre class="code">
	 * Assert2.hasLength(name, "Name must not be empty");
	 * </pre>
	 * 
	 * @param text
	 *            the String to check
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @see StringUtils#hasLength
	 * @throws IllegalArgumentException
	 *             if the text is empty
	 */
	public static void hasLength(String text, String fmtMessage, Object... args) {
		if (!isNotBlank(text)) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(doFormat(fmtMessage, args)));
		}
	}

	/**
	 * Assert that the given String is not empty; that is, it must not be
	 * {@code null} and not the empty String.
	 * 
	 * @param text
	 *            the String to check
	 * @param argName
	 */
	public static void hasLengthOf(String text, String argName) {
		hasLength(text, argName + " must have length; it must not be null or empty");
	}

	/**
	 * Assert that the given String contains valid text content; that is, it
	 * must not be {@code null} and must contain at least one non-whitespace
	 * character.
	 * 
	 * <pre class="code">
	 * Assert2.hasText(name, "'name' must not be empty");
	 * </pre>
	 * 
	 * @param text
	 *            the String to check
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @see StringUtils#hasText
	 * @throws IllegalArgumentException
	 *             if the text does not contain valid text content
	 */
	@SuppressWarnings("unchecked")
	public static <T> T hasText(String text, String fmtMessage, Object... args) {
		if (!isNotBlank(text)) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(doFormat(fmtMessage, args)));
		}
		return (T) text;
	}

	/**
	 * Assert that the given String contains valid text content; that is, it
	 * must not be {@code null} and must contain at least one non-whitespace
	 * character.
	 * 
	 * <pre class="code">
	 * Assert2.hasText(name, "'name' must not be empty");
	 * </pre>
	 * 
	 * @param text
	 *            the String to check
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @see StringUtils#hasText
	 * @throws IllegalArgumentException
	 *             if the text does not contain valid text content
	 */
	@SuppressWarnings("unchecked")
	public static <T> T hasText(String text, Supplier<String> messageSupplier) {
		if (!isNotBlank(text)) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(nullSafeGet(messageSupplier)));
		}
		return (T) text;
	}

	/**
	 * Assert that the given String contains valid text content; that is, it
	 * must not be {@code null} and must contain at least one non-whitespace
	 * character.
	 * 
	 * <pre class="code">
	 * Assert2.hasText(name, "'name' must not be empty");
	 * </pre>
	 * 
	 * @param text
	 *            the String to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @see StringUtils#hasText
	 * @throws IllegalArgumentException
	 *             if the text does not contain valid text content
	 */
	@SuppressWarnings("unchecked")
	public static <T> T hasText(String text, Class<? extends RuntimeException> exceptionClass, String fmtMessage,
			Object... args) {
		if (!isNotBlank(text)) {
			doWrapException(exceptionClass, fmtMessage, args);
		}
		return (T) text;
	}

	/**
	 * Assert that the given String contains valid text content; that is, it
	 * must not be {@code null} and must contain at least one non-whitespace
	 * character.
	 * 
	 * <pre class="code">
	 * Assert2.hasText(name, "'name' must not be empty");
	 * </pre>
	 * 
	 * @param text
	 *            the String to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @see StringUtils#hasText
	 * @throws IllegalArgumentException
	 *             if the text does not contain valid text content
	 */
	@SuppressWarnings("unchecked")
	public static <T> T hasText(String text, Class<? extends RuntimeException> exceptionClass, Supplier<String> messageSupplier) {
		if (!isNotBlank(text)) {
			doWrapException(exceptionClass, nullSafeGet(messageSupplier));
		}
		return (T) text;
	}

	/**
	 * Assert that the given String contains valid text content; that is, it
	 * must not be {@code null} and must contain at least one non-whitespace
	 * character.
	 * 
	 * @param text
	 *            the String to check
	 * @param argName
	 */
	@SuppressWarnings("unchecked")
	public static <T> T hasTextOf(String text, String argName) {
		hasText(text, argName + " is required");
		return (T) text;
	}

	/**
	 * Assert that the given String contains valid text content; that is, it
	 * must not be {@code null} and must contain at least one non-whitespace
	 * character.
	 * 
	 * @param text
	 *            the String to check
	 * @param exceptionClass
	 * @param argName
	 */
	@SuppressWarnings("unchecked")
	public static <T> T hasTextOf(String text, Class<? extends RuntimeException> exceptionClass, String argName) {
		hasText(text, exceptionClass, argName + " is required");
		return (T) text;
	}

	/**
	 * Assert that the given text does not contain the given substring.
	 * 
	 * <pre class="code">
	 * Assert2.doesNotContain(name, "rod", "Name must not contain 'rod'");
	 * </pre>
	 * 
	 * @param textToSearch
	 *            the text to search
	 * @param substring
	 *            the substring to find within the text
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the text contains the substring
	 */
	public static void doesNotContain(String textToSearch, String substring, String fmtMessage, Object... args) {
		if (isNotBlank(textToSearch) && isNotBlank(substring) && textToSearch.contains(substring)) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX + doFormat(fmtMessage, args));
		}
	}

	/**
	 * Assert that the given text does not contain the given substring.
	 * 
	 * @param textToSearch
	 *            the text to search
	 * @param substring
	 *            the substring to find within the text
	 * @param argName
	 */
	public static void doesNotContainOf(String textToSearch, String substring, String argName) {
		doesNotContain(textToSearch, substring, argName + " must not contain the substring [" + substring + "]");
	}

	/**
	 * Assert that an array contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * 
	 * <pre class="code">
	 * Assert2.notEmpty(array, "The array must contain elements");
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object array is {@code null} or contains no elements
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmpty(Object[] array, String fmtMessage, Object... args) {
		if (array == null || array.length == 0) {
			throw new IllegalArgumentException(doFormat(fmtMessage, args));
		}
		return (T) array;
	}

	/**
	 * Assert that an array contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * 
	 * <pre class="code">
	 * Assert2.notEmpty(array, "The array must contain elements");
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @throws IllegalArgumentException
	 *             if the object array is {@code null} or contains no elements
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmpty(Object[] array, Supplier<String> messageSupplier) {
		if (array == null || array.length == 0) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(nullSafeGet(messageSupplier)));
		}
		return (T) array;
	}

	/**
	 * Assert that an array contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * 
	 * <pre class="code">
	 * Assert2.notEmpty(array, "The array must contain elements");
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object array is {@code null} or contains no elements
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmpty(Object[] array, Class<? extends RuntimeException> exceptionClass, String fmtMessage,
			Object... args) {
		if (array == null || array.length == 0) {
			doWrapException(exceptionClass, fmtMessage, args);
		}
		return (T) array;
	}

	/**
	 * Assert that an array contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * 
	 * <pre class="code">
	 * Assert2.notEmpty(array, "The array must contain elements");
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @throws IllegalArgumentException
	 *             if the object array is {@code null} or contains no elements
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmpty(Object[] array, Class<? extends RuntimeException> exceptionClass,
			Supplier<String> messageSupplier) {
		if (array == null || array.length == 0) {
			doWrapException(exceptionClass, nullSafeGet(messageSupplier));
		}
		return (T) array;
	}

	/**
	 * Assert that an array contains elements; that is, it must not be
	 * {@code null}
	 * 
	 * @param array
	 *            the array to check
	 * @param argName
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmptyOf(Object[] array, String argName) {
		notEmpty(array, argName + " must not be empty: it must contain at least 1 element");
		return (T) array;
	}

	/**
	 * Assert that an array contains elements; that is, it must not be
	 * {@code null}
	 * 
	 * @param array
	 *            the array to check
	 * @param exceptionClass
	 * @param argName
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmptyOf(Object[] array, Class<? extends RuntimeException> exceptionClass, String argName) {
		notEmpty(array, exceptionClass, argName + " must not be empty: it must contain at least 1 element");
		return (T) array;
	}

	/**
	 * Assert that an array contains no {@code null} elements.
	 * <p>
	 * Note: Does not complain if the array is empty!
	 * 
	 * <pre class="code">
	 * Assert2.noNullElements(array, "The array must contain non-null elements");
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object array contains a {@code null} element
	 */
	public static void noNullElements(Object[] array, String fmtMessage, Object... args) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(doFormat(fmtMessage, args)));
				}
			}
		}
	}

	/**
	 * Assert that an array contains no {@code null} elements.
	 * <p>
	 * Note: Does not complain if the array is empty!
	 * 
	 * <pre class="code">
	 * Assert2.noNullElements(array, "The array must contain non-null elements");
	 * </pre>
	 * 
	 * @param array
	 *            the array to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the object array contains a {@code null} element
	 */
	public static void noNullElements(Object[] array, Class<? extends RuntimeException> exceptionClass, String fmtMessage,
			Object... args) {
		if (array != null) {
			for (Object element : array) {
				if (element == null) {
					doWrapException(exceptionClass, fmtMessage, args);
				}
			}
		}
	}

	/**
	 * Assert that an array contains no {@code null} elements.
	 * 
	 * @param array
	 *            the array to check
	 * @param argName
	 */
	public static void noNullElementsOf(Object[] array, String argName) {
		noNullElements(array, argName + " must not contain any null elements");
	}

	/**
	 * Assert that a collection contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * 
	 * <pre class="code">
	 * Assert2.notEmpty(collection, "Collection must contain elements");
	 * </pre>
	 * 
	 * @param collection
	 *            the collection to check
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the collection is {@code null} or contains no elements
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmpty(Collection<?> collection, String fmtMessage, Object... args) {
		if (collection == null || collection.isEmpty()) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(doFormat(fmtMessage, args)));
		}
		return (T) collection;
	}

	/**
	 * Assert that a collection contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * 
	 * <pre class="code">
	 * Assert2.notEmpty(collection, "Collection must contain elements");
	 * </pre>
	 * 
	 * @param collection
	 *            the collection to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the collection is {@code null} or contains no elements
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmpty(Collection<?> collection, Class<? extends RuntimeException> exceptionClass, String fmtMessage,
			Object... args) {
		if (collection == null || collection.isEmpty()) {
			doWrapException(exceptionClass, fmtMessage, args);
		}
		return (T) collection;
	}

	/**
	 * Assert that a collection contains elements; that is, it must not be
	 * {@code null} and must contain at least one element.
	 * 
	 * @param collection
	 *            the collection to check
	 * @param argName
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmptyOf(Collection<?> collection, String argName) {
		notEmpty(collection, argName + " must not be empty: it must contain at least 1 element");
		return (T) collection;
	}

	/**
	 * Assert that a Map contains entries; that is, it must not be {@code null}
	 * and must contain at least one entry.
	 * 
	 * <pre class="code">
	 * Assert2.notEmpty(map, "Map must contain entries");
	 * </pre>
	 * 
	 * @param map
	 *            the map to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the map is {@code null} or contains no entries
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmpty(Map<?, ?> map, Class<? extends RuntimeException> exceptionClass, String fmtMessage,
			Object... args) {
		if (map == null || map.isEmpty()) {
			doWrapException(exceptionClass, fmtMessage, args);
		}
		return (T) map;
	}

	/**
	 * Assert that a Map contains entries; that is, it must not be {@code null}
	 * and must contain at least one entry.
	 * 
	 * <pre class="code">
	 * Assert2.notEmpty(map, "Map must contain entries");
	 * </pre>
	 * 
	 * @param map
	 *            the map to check
	 * @param fmtMessage
	 *            the exception message to use if the assertion fails
	 * @throws IllegalArgumentException
	 *             if the map is {@code null} or contains no entries
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmpty(Map<?, ?> map, String fmtMessage, Object... args) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException(ASSERT_FAILED_PREFIX.concat(doFormat(fmtMessage, args)));
		}
		return (T) map;
	}

	/**
	 * Assert that a Map contains entries; that is, it must not be {@code null}
	 * and must contain at least one entry.
	 * 
	 * @param map
	 *            the map to check
	 * @param argName
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmptyOf(Map<?, ?> map, String argName) {
		notEmpty(map, argName + " must not be empty; it must contain at least one entry");
		return (T) map;
	}

	/**
	 * Assert that a Map contains entries; that is, it must not be {@code null}
	 * and must contain at least one entry.
	 * 
	 * @param map
	 *            the map to check
	 * @param exceptionClass
	 * @param argName
	 */
	@SuppressWarnings("unchecked")
	public static <T> T notEmptyOf(Map<?, ?> map, Class<? extends RuntimeException> exceptionClass, String argName) {
		notEmpty(map, exceptionClass, argName + " must not be empty; it must contain at least one entry");
		return (T) map;
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * 
	 * <pre class="code">
	 * Assert2.mustAssignableFrom(Parent.class, Sub.class);
	 * </pre>
	 * 
	 * @param parentType
	 *            the parent type class
	 * @param type
	 *            the target type class
	 * @param fmtMessage
	 *            a message which will be prepended to provide further context.
	 *            If it is empty or ends in ":" or ";" or "," or ".", a full
	 *            exception message will be appended. If it ends in a space, the
	 *            name of the offending object's type will be appended. In any
	 *            other case, a ":" with a space and the name of the offending
	 *            object's type will be appended.
	 * @throws ClassCastException
	 */
	public static Class<?> mustAssignableFrom(Class<?> parentType, Class<?> type) {
		return mustAssignableFrom(parentType, type, "Type '%s' must be extension compatible from parentType '%s'", parentType,
				type);
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * 
	 * <pre class="code">
	 * Assert2.mustAssignableFrom(Parent.class, Sub.class, "Unexpected compatible type");
	 * </pre>
	 * 
	 * @param parentType
	 *            the parent type class
	 * @param type
	 *            the target type class
	 * @param fmtMessage
	 *            a message which will be prepended to provide further context.
	 *            If it is empty or ends in ":" or ";" or "," or ".", a full
	 *            exception message will be appended. If it ends in a space, the
	 *            name of the offending object's type will be appended. In any
	 *            other case, a ":" with a space and the name of the offending
	 *            object's type will be appended.
	 * @throws ClassCastException
	 */
	public static Class<?> mustAssignableFrom(Class<?> parentType, Class<?> type, String fmtMessage, Object... args) {
		notNull(parentType, "ParentType to check against must not be null");
		notNull(type, "Type to check against must not be null");
		if (!parentType.isAssignableFrom(type)) {
			doWrapException(ClassCastException.class, fmtMessage, args);
		}
		return type;
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * 
	 * <pre class="code">
	 * Assert2.instanceOf(Foo.class, foo, "Foo expected");
	 * </pre>
	 * 
	 * @param type
	 *            the type to check against
	 * @param obj
	 *            the object to check
	 * @param fmtMessage
	 *            a message which will be prepended to provide further context.
	 *            If it is empty or ends in ":" or ";" or "," or ".", a full
	 *            exception message will be appended. If it ends in a space, the
	 *            name of the offending object's type will be appended. In any
	 *            other case, a ":" with a space and the name of the offending
	 *            object's type will be appended.
	 * @throws IllegalArgumentException
	 *             if the object is not an instance of type
	 */
	@SuppressWarnings("unchecked")
	public static <T> T isInstanceOf(Class<?> type, Object obj, String fmtMessage, Object... args) {
		notNull(type, "Type to check against must not be null");
		if (!type.isInstance(obj)) {
			instanceCheckFailed(type, obj, IllegalArgumentException.class, doFormat(fmtMessage, args));
		}
		return (T) obj;
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * 
	 * <pre class="code">
	 * Assert2.instanceOf(Foo.class, foo, "Foo expected");
	 * </pre>
	 * 
	 * @param type
	 *            the type to check against
	 * @param obj
	 *            the object to check
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @throws IllegalArgumentException
	 *             if the object is not an instance of type
	 */
	@SuppressWarnings("unchecked")
	public static <T> T isInstanceOf(Class<?> type, Object obj, Supplier<String> messageSupplier) {
		notNull(type, "Type to check against must not be null");
		if (!type.isInstance(obj)) {
			instanceCheckFailed(type, obj, IllegalArgumentException.class,
					ASSERT_FAILED_PREFIX.concat(nullSafeGet(messageSupplier)));
		}
		return (T) obj;
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * 
	 * <pre class="code">
	 * Assert2.instanceOf(Foo.class, foo, "Foo expected");
	 * </pre>
	 * 
	 * @param type
	 *            the type to check against
	 * @param obj
	 *            the object to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            a message which will be prepended to provide further context.
	 *            If it is empty or ends in ":" or ";" or "," or ".", a full
	 *            exception message will be appended. If it ends in a space, the
	 *            name of the offending object's type will be appended. In any
	 *            other case, a ":" with a space and the name of the offending
	 *            object's type will be appended.
	 * @throws IllegalArgumentException
	 *             if the object is not an instance of type
	 */
	@SuppressWarnings("unchecked")
	public static <T> T isInstanceOf(Class<?> type, Object obj, Class<? extends RuntimeException> exceptionClass,
			String fmtMessage, Object... args) {
		notNull(type, "Type to check against must not be null");
		if (!type.isInstance(obj)) {
			instanceCheckFailed(type, obj, exceptionClass, doFormat(fmtMessage, args));
		}
		return (T) obj;
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * 
	 * <pre class="code">
	 * Assert2.instanceOf(Foo.class, foo, "Foo expected");
	 * </pre>
	 * 
	 * @param type
	 *            the type to check against
	 * @param obj
	 *            the object to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param messageSupplier
	 *            a supplier for the exception message to use if the assertion
	 *            fails
	 * @throws IllegalArgumentException
	 *             if the object is not an instance of type
	 */
	@SuppressWarnings("unchecked")
	public static <T> T isInstanceOf(Class<?> type, Object obj, Class<? extends RuntimeException> exceptionClass,
			Supplier<String> messageSupplier) {
		notNull(type, "Type to check against must not be null");
		if (!type.isInstance(obj)) {
			instanceCheckFailed(type, obj, exceptionClass, nullSafeGet(messageSupplier));
		}
		return (T) obj;
	}

	/**
	 * Assert that the provided object is an instance of the provided class.
	 * 
	 * <pre class="code">
	 * Assert2.instanceOf(Foo.class, foo);
	 * </pre>
	 * 
	 * @param type
	 *            the type to check against
	 * @param obj
	 *            the object to check
	 * @throws IllegalArgumentException
	 *             if the object is not an instance of type
	 */
	@SuppressWarnings("unchecked")
	public static <T> T isInstanceOf(Class<?> type, Object obj) {
		isInstanceOf(type, obj, "");
		return (T) obj;
	}

	/**
	 * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
	 * 
	 * <pre class="code">
	 * Assert2.isAssignable(Number.class, myClass, "Number expected");
	 * </pre>
	 * 
	 * @param superType
	 *            the super type to check against
	 * @param subType
	 *            the sub type to check
	 * @param fmtMessage
	 *            a message which will be prepended to provide further context.
	 *            If it is empty or ends in ":" or ";" or "," or ".", a full
	 *            exception message will be appended. If it ends in a space, the
	 *            name of the offending sub type will be appended. In any other
	 *            case, a ":" with a space and the name of the offending sub
	 *            type will be appended.
	 * @throws IllegalArgumentException
	 *             if the classes are not assignable
	 */
	public static void isAssignable(Class<?> superType, Class<?> subType, String fmtMessage, Object... args) {
		notNull(superType, "Super type to check against must not be null");
		if (subType == null || !superType.isAssignableFrom(subType)) {
			assignableCheckFailed(superType, subType, IllegalArgumentException.class, doFormat(fmtMessage, args));
		}
	}

	/**
	 * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
	 * 
	 * <pre class="code">
	 * Assert2.isAssignable(Number.class, myClass, "Number expected");
	 * </pre>
	 * 
	 * @param superType
	 *            the super type to check against
	 * @param subType
	 *            the sub type to check
	 * @param exceptionClass
	 *            Throwable class
	 * @param fmtMessage
	 *            a message which will be prepended to provide further context.
	 *            If it is empty or ends in ":" or ";" or "," or ".", a full
	 *            exception message will be appended. If it ends in a space, the
	 *            name of the offending sub type will be appended. In any other
	 *            case, a ":" with a space and the name of the offending sub
	 *            type will be appended.
	 * @throws IllegalArgumentException
	 *             if the classes are not assignable
	 */
	public static void isAssignable(Class<?> superType, Class<?> subType, Class<? extends RuntimeException> exceptionClass,
			String fmtMessage, Object... args) {
		notNull(superType, "Super type to check against must not be null");
		if (subType == null || !superType.isAssignableFrom(subType)) {
			assignableCheckFailed(superType, subType, exceptionClass, doFormat(fmtMessage, args));
		}
	}

	/**
	 * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
	 * 
	 * <pre class="code">
	 * Assert2.isAssignable(Number.class, myClass);
	 * </pre>
	 * 
	 * @param superType
	 *            the super type to check
	 * @param subType
	 *            the sub type to check
	 * @throws IllegalArgumentException
	 *             if the classes are not assignable
	 */
	public static void isAssignable(Class<?> superType, Class<?> subType) {
		isAssignable(superType, subType, "");
	}

	/**
	 * Assertion parameters in range
	 * 
	 * @param paramValue
	 * @param lowerValue
	 * @param upperValue
	 */
	public static void assertInRange(long paramValue, long lowerValue, long upperValue) {
		if (!checkParameterInRange(paramValue, lowerValue, true, upperValue, true)) {
			throw new IllegalArgumentException(format("%d not in valid range [%d, %d]", paramValue, lowerValue, upperValue));
		}
	}

	/**
	 * Check parameters in range
	 * 
	 * @param param
	 * @param from
	 * @param leftInclusive
	 * @param to
	 * @param rightInclusive
	 * @return
	 */
	private static boolean checkParameterInRange(long param, long from, boolean leftInclusive, long to, boolean rightInclusive) {
		if (leftInclusive && rightInclusive) { // [from, to]
			if (from <= param && param <= to) {
				return true;
			} else {
				return false;
			}
		} else if (leftInclusive && !rightInclusive) { // [from, to)
			if (from <= param && param < to) {
				return true;
			} else {
				return false;
			}
		} else if (!leftInclusive && !rightInclusive) { // (from, to)
			if (from < param && param < to) {
				return true;
			} else {
				return false;
			}
		} else { // (from, to]
			if (from < param && param <= to) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Check instance failed
	 * 
	 * @param type
	 * @param obj
	 * @param exceptionClass
	 * @param msg
	 */
	private static void instanceCheckFailed(Class<?> type, Object obj, Class<? extends RuntimeException> exceptionClass,
			String msg) {
		String className = (obj != null ? obj.getClass().getName() : "null");
		String result = "";
		boolean defaultMessage = true;
		if (isNotBlank(msg)) {
			if (endsWithSeparator(msg)) {
				result = msg + " ";
			} else {
				result = messageWithTypeName(msg, className);
				defaultMessage = false;
			}
		}
		if (defaultMessage) {
			result = result + ("Object of class [" + className + "] must be an instance of " + type);
		}

		doWrapException(exceptionClass, result);
	}

	/**
	 * Check assignable failed
	 * 
	 * @param superType
	 * @param subType
	 * @param exceptionClass
	 * @param msg
	 */
	private static void assignableCheckFailed(Class<?> superType, Class<?> subType,
			Class<? extends RuntimeException> exceptionClass, String msg) {
		String result = "";
		boolean defaultMessage = true;
		if (isNotBlank(msg)) {
			if (endsWithSeparator(msg)) {
				result = msg + " ";
			} else {
				result = messageWithTypeName(msg, subType);
				defaultMessage = false;
			}
		}
		if (defaultMessage) {
			result = result + (subType + " is not assignable to " + superType);
		}

		doWrapException(exceptionClass, result);
	}

	private static boolean endsWithSeparator(String msg) {
		return (msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith("."));
	}

	private static String messageWithTypeName(String msg, Object typeName) {
		return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
	}

	/**
	 * Do format throwable message.
	 * 
	 * @param format
	 * @param args
	 * @return
	 */
	private static String doFormat(String format, Object... args) {
		if (Objects.nonNull(args) && args.length > 0) {
			return String.format(format, args);
		}
		return format;
	}

	@Nullable
	private static String nullSafeGet(@Nullable Supplier<String> messageSupplier) {
		return (messageSupplier != null ? messageSupplier.get() : null);
	}

	/**
	 * Do wrap assertion exception.
	 * 
	 * @param exceptionClass
	 * @param fmtMessage
	 * @param args
	 */
	private static void doWrapException(Class<? extends RuntimeException> exceptionClass, String fmtMessage, Object... args) {
		RuntimeException th = newRuntimeExceptionInstance(exceptionClass);
		// Init cause message
		try {
			detailMessageField.set(th, ASSERT_FAILED_PREFIX.concat(doFormat(fmtMessage, args)));
		} catch (Exception ex) {
			throw new Error(
					"Unexpected reflection exception - ".concat(ex.getClass().getName()).concat(": ").concat(ex.getMessage()));
		}

		// Remove useless stack elements
		StackTraceElement[] stackEles = th.getStackTrace();
		List<StackTraceElement> availableStackEles = new ArrayList<>(max(stackEles.length - 4, 4));
		for (int i = 0, j = 0; i < stackEles.length; i++) {
			StackTraceElement st = stackEles[i];
			if (j == 0) {
				if (NEW_RUNTIMEEXCEPTION_INSTANCE_METHOD.equals(st.getClassName().concat("#").concat(st.getMethodName())))
					j = i;
			} else
				availableStackEles.add(st);
		}
		th.setStackTrace(availableStackEles.toArray(new StackTraceElement[] {}));

		throw th;
	}

	/**
	 * New create throwable instance.
	 * 
	 * @param exceptionClass
	 * @return
	 */
	private static RuntimeException newRuntimeExceptionInstance(Class<? extends RuntimeException> exceptionClass) {
		return ObjectInstantiators.newInstance(exceptionClass);
	}

	/**
	 * @see new IllegalArgumentException("[Assertion failed] - xxx required");
	 */
	final private static String ASSERT_FAILED_PREFIX = "[AF] - ";
	final private static String NEW_RUNTIMEEXCEPTION_INSTANCE_METHOD = Assert2.class.getName() + "#newRuntimeExceptionInstance";
	final private static Field detailMessageField;
	final private static Field causeField;

	static {
		try {
			detailMessageField = Throwable.class.getDeclaredField("detailMessage");
			causeField = Throwable.class.getDeclaredField("cause");
			detailMessageField.setAccessible(true);
			causeField.setAccessible(true);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

}
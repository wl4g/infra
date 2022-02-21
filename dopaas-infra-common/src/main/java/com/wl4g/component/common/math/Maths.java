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
package com.wl4g.component.common.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Simple math utility {@link Maths} tools.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020年2月4日 v1.0.0
 * @see
 */
public abstract class Maths {

	// --- Basic mathematical function's. ---

	/**
	 * Round the decimal.
	 * 
	 * @param v
	 * @return
	 */
	public static BigDecimal round(double v) {
		return round(v, DEFAULT_DIV_SCALE);
	}

	/**
	 * Round the decimal. {@link BigDecimal#ROUND_HALF_UP}
	 * 
	 * @param v
	 * @param scale
	 * @return
	 */
	public static BigDecimal round(double v, int scale) {
		return new BigDecimal(v).setScale(scale, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * Exact addition
	 * 
	 * @param v1
	 *            Augend
	 * @param v2
	 *            Addition number
	 * @param scale
	 *            The representation needs to be accurate to several decimal
	 *            places
	 * @return The sum of two parameters (BigDecimal)
	 */
	public static BigDecimal add(double v1, double v2) {
		return add(v1, v2, DEFAULT_DIV_SCALE);
	}

	/**
	 * Exact addition
	 * 
	 * @param v1
	 *            Augend
	 * @param v2
	 *            Addition number
	 * @param scale
	 *            The representation needs to be accurate to several decimal
	 *            places
	 * @return The sum of two parameters (BigDecimal)
	 */
	public static BigDecimal add(double v1, double v2, int scale) {
		return add(BigDecimal.valueOf(v1), BigDecimal.valueOf(v2)).setScale(scale, RoundingMode.HALF_EVEN);
	}

	/**
	 * Exact addition
	 * 
	 * @param v1
	 *            Augend
	 * @param v2
	 *            Addition number
	 * @return The sum of two parameters (BigDecimal)
	 */
	public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
		if (null == v1) {
			v1 = BigDecimal.ZERO;
		}
		if (null == v2) {
			v2 = BigDecimal.ZERO;
		}
		return v1.add(v2);
	}

	/**
	 * Exact subtraction
	 * 
	 * @param v1
	 *            Minuend
	 * @param v2
	 *            Reduction
	 * @param scale
	 *            The representation needs to be accurate to several decimal
	 * @return Difference between two parameters (BigDecimal)
	 */
	public static BigDecimal subtract(double v1, double v2) {
		return subtract(v1, v2, DEFAULT_DIV_SCALE);
	}

	/**
	 * Exact subtraction
	 * 
	 * @param v1
	 *            Minuend
	 * @param v2
	 *            Reduction
	 * @param scale
	 *            The representation needs to be accurate to several decimal
	 * @return Difference between two parameters (BigDecimal)
	 */
	public static BigDecimal subtract(double v1, double v2, int scale) {
		return subtract(BigDecimal.valueOf(v1), BigDecimal.valueOf(v2)).setScale(scale, RoundingMode.HALF_EVEN);
	}

	/**
	 * Exact subtraction
	 * 
	 * @param v1
	 *            Minuend
	 * @param v2
	 *            Reduction
	 * @return Difference between two parameters (BigDecimal)
	 */
	public static BigDecimal subtract(BigDecimal v1, BigDecimal v2) {
		if (null == v1) {
			v1 = BigDecimal.ZERO;
		}
		if (null == v2) {
			v2 = BigDecimal.ZERO;
		}
		return v1.subtract(v2);
	}

	/**
	 * Exact multiplication
	 * 
	 * @param v1
	 *            multiplicand
	 * @param v2
	 *            multiplier
	 * @return Product of two parameters (BigDecimal)
	 */
	public static BigDecimal multiply(double v1, double v2) {
		return multiply(BigDecimal.valueOf(v1), BigDecimal.valueOf(v2));
	}

	/**
	 * Exact multiplication
	 * 
	 * @param v1
	 *            multiplicand
	 * @param v2
	 *            multiplier
	 * @return Product of two parameters (BigDecimal)
	 */
	public static BigDecimal multiply(BigDecimal v1, BigDecimal v2) {
		if (null == v1) {
			v1 = BigDecimal.ONE;
		}
		if (null == v2) {
			v2 = BigDecimal.ONE;
		}
		return v1.multiply(v2);
	}

	/**
	 * (relative) accurate division operation. In case of inexhaustible
	 * division, it shall be accurate to 2 digits after the decimal point, and
	 * then the number shall be rounded
	 * 
	 * @param v1
	 *            dividend
	 * @param v2
	 *            divisor
	 * @return Quotient of two parameters (BigDecimal)
	 */
	public static BigDecimal divide(double v1, double v2) {
		return divide(v1, v2, DEFAULT_DIV_SCALE);
	}

	/**
	 * (relative) accurate division operation. In case of inexhaustible
	 * division, it shall be accurate to 2 digits after the decimal point, and
	 * then the number shall be rounded
	 * 
	 * @param v1
	 *            dividend
	 * @param v2
	 *            divisor
	 * @param scale
	 *            The representation needs to be accurate to several decimal
	 * 
	 * @return Quotient of two parameters (BigDecimal)
	 */
	public static BigDecimal divide(double v1, double v2, int scale) {
		return divide(BigDecimal.valueOf(v1), BigDecimal.valueOf(v2), scale);
	}

	/**
	 * (relative) accurate division operation. In case of inexhaustible
	 * division, it shall be accurate to 2 digits after the decimal point, and
	 * then the number shall be rounded
	 * 
	 * @param v1
	 *            dividend
	 * @param v2
	 *            divisor
	 * @return Quotient of two parameters (BigDecimal)
	 */
	public static BigDecimal divide(BigDecimal v1, BigDecimal v2) {
		return divide(v1, v2, DEFAULT_DIV_SCALE);
	}

	/**
	 * 
	 * (relative) precise division operation. In case of incomplete division,
	 * the scale parameter specifies the precision, and then the number is
	 * rounded
	 * 
	 * @param v1
	 *            dividend
	 * @param v2
	 *            divisor
	 * @param scale
	 *            The representation needs to be accurate to several decimal
	 *            places
	 * @return Quotient of two parameters (BigDecimal)
	 */
	public static BigDecimal divide(BigDecimal v1, BigDecimal v2, int scale) {
		if (null == v1) {
			return BigDecimal.ZERO;
		}
		if (null == v2) {
			v2 = BigDecimal.ONE;
		}

		if (v2.compareTo(BigDecimal.ZERO) == 0) {
			throw new IllegalArgumentException("Divisor cannot be 0");
		}

		if (scale < 0) {
			throw new IllegalArgumentException("The accuracy(scale) cannot be less than 0");
		}

		return v1.divide(v2, scale, BigDecimal.ROUND_HALF_UP);
	}

	// --- String mathematical function's ---

	/**
	 * Exact addition
	 * 
	 * @param v1
	 *            Augend
	 * @param v2
	 *            Addition number
	 * @return The sum of two parameters (string)
	 */
	public static String add(String v1, String v2) {
		if (isBlank0(v1)) {
			v1 = "0";
		}
		if (isBlank0(v2)) {
			v2 = "0";
		}
		BigDecimal b1 = new BigDecimal(v1.trim());
		BigDecimal b2 = new BigDecimal(v2.trim());
		return String.valueOf(add(b1, b2));
	}

	/**
	 * Exact subtraction
	 * 
	 * @param v1
	 *            被减数
	 * @param v2
	 *            减数
	 * @return 两个参数的差(String)
	 */
	public static String subtract(String v1, String v2) {
		if (isBlank0(v1)) {
			v1 = "0";
		}
		if (isBlank0(v2)) {
			v2 = "0";
		}
		BigDecimal b1 = new BigDecimal(v1.trim());
		BigDecimal b2 = new BigDecimal(v2.trim());
		return String.valueOf(subtract(b1, b2));
	}

	/**
	 * Exact multiplication
	 * 
	 * @param v1
	 *            Multiplicand
	 * @param v2
	 *            multiplier
	 * @return Product of two parameters (string)
	 */
	public static String multiply(String v1, String v2) {
		if (isBlank0(v1)) {
			v1 = "1";
		}
		if (isBlank0(v2)) {
			v2 = "1";
		}
		BigDecimal b1 = new BigDecimal(v1.trim());
		BigDecimal b2 = new BigDecimal(v2.trim());
		return String.valueOf(multiply(b1, b2));
	}

	/**
	 * (relative) accurate division operation. In case of inexhaustible
	 * division, it shall be accurate to 2 digits after the decimal point, and
	 * then the number shall be rounded
	 * 
	 * @param v1
	 *            Divisor
	 * @param v2
	 *            Divisor
	 * @return Quotient of two parameters (string)
	 */
	public static String divide(String v1, String v2) {
		return divide(v1, v2, DEFAULT_DIV_SCALE);
	}

	/**
	 * (relative) precise division operation. In case of incomplete division,
	 * the scale parameter specifies the precision, and then the number is
	 * rounded
	 * 
	 * @param v1
	 *            Divisor
	 * @param v2
	 *            Divisor
	 * @param scale
	 *            Indicates that it needs to be accurate to several decimal
	 *            places
	 * @return Quotient of two parameters (string)
	 */
	public static String divide(String v1, String v2, int scale) {
		if (null == v1) {
			return "0";
		}
		if (null == v2) {
			v2 = "1";
		}
		BigDecimal b1 = new BigDecimal(v1.trim());
		BigDecimal b2 = new BigDecimal(v2.trim());
		return String.valueOf(divide(b1, b2, scale));
	}

	// --- Array mathematical function's. ---

	/**
	 * Precise addition operation to calculate the sum of multiple values. If
	 * there is a null value, it will be ignored
	 * 
	 * @param values
	 *            Addend set
	 * @return The sum of two parameters (BigDecimal)
	 */
	public static BigDecimal sum(BigDecimal v1, BigDecimal... values) {
		if (null == v1) {
			v1 = BigDecimal.ZERO;
		}
		if (null == values || values.length == 0) {
			return v1;
		}
		for (BigDecimal val : values) {
			if (null != val) {
				v1 = v1.add(val);
			}
		}
		return v1;
	}

	/**
	 * Precise addition operation to calculate the sum of multiple values. If
	 * there is a null value, it will be ignored
	 * 
	 * @param values
	 *            Addend set
	 * @return The sum of two parameters (string)
	 */
	public static String sum(String v1, String... values) {
		if (isBlank0(v1)) {
			v1 = "0";
		}
		if (null == values || values.length == 0) {
			return v1;
		}
		BigDecimal b1 = new BigDecimal(v1.trim());
		for (String val : values) {
			if (!isBlank0(val)) {
				b1 = add(b1, new BigDecimal(val.trim()));
			}
		}
		return String.valueOf(b1);
	}

	/**
	 * Gets average
	 * 
	 * @param values
	 * @return
	 */
	public static BigDecimal avg(BigDecimal... values) {
		if (null != values && values.length != 0) {
			return divide(sum(BigDecimal.ZERO, values), new BigDecimal(values.length));
		}
		return BigDecimal.ZERO;
	}

	/**
	 * Gets average
	 * 
	 * @param values
	 * @return
	 */
	public static String avg(String... values) {
		if (null != values && values.length != 0) {
			return divide(sum("0", values), String.valueOf(values.length));
		}
		return "0";
	}

	/**
	 * Gets maximum
	 * 
	 * @param v1
	 * @param values
	 * @return
	 */
	public static BigDecimal max(BigDecimal v1, BigDecimal... values) {
		BigDecimal max = v1;
		if (null == values || values.length == 0) {
			return max;
		}
		for (BigDecimal val : values) {
			if (null != val && val.compareTo(max) > 0) {
				max = val;
			}
		}
		return max;
	}

	/**
	 * Gets maximum
	 * 
	 * @param values
	 * @return
	 */
	public static BigDecimal maxArr(BigDecimal... values) {
		if (null == values || values.length == 0) {
			return null;
		}

		return max(values[0], values);
	}

	/**
	 * Gets minimum
	 * 
	 * @param v1
	 * @param values
	 * @return
	 */
	public static BigDecimal min(BigDecimal v1, BigDecimal... values) {
		BigDecimal min = v1;
		if (null == values || values.length == 0) {
			return min;
		}
		for (BigDecimal val : values) {
			if (null != val && val.compareTo(min) < 0) {
				min = val;
			}
		}
		return min;
	}

	/**
	 * Gets minimum
	 * 
	 * @param values
	 * @return
	 */
	public static BigDecimal minArr(BigDecimal... values) {
		if (null == values || values.length == 0) {
			return null;
		}
		return min(values[0], values);
	}

	/**
	 * Gets maximum
	 * 
	 * @param v1
	 * @param values
	 * @return
	 */
	public static String max(String v1, String... values) {
		if (isBlank0(v1)) {
			return null;
		}
		if (null == values || values.length == 0) {
			return v1;
		}
		BigDecimal maxBd = new BigDecimal(v1.trim());

		for (String val : values) {
			if (!isBlank0(val) && new BigDecimal(val).compareTo(maxBd) > 0) {
				maxBd = new BigDecimal(val);
			}
		}
		return String.valueOf(maxBd);
	}

	/**
	 * Gets maximum
	 * 
	 * @param values
	 * @return
	 */
	public static String maxArr(String... values) {
		if (null == values || values.length == 0) {
			return null;
		}
		return max(values[0], values);
	}

	/**
	 * Gets minimum
	 * 
	 * @param v1
	 * @param values
	 * @return
	 */
	public static String min(String v1, String... values) {
		if (isBlank0(v1)) {
			return null;
		}
		if (null == values || values.length == 0) {
			return v1;
		}
		BigDecimal minBd = new BigDecimal(v1.trim());

		for (String val : values) {
			if (!isBlank0(val) && new BigDecimal(val).compareTo(minBd) < 0) {
				minBd = new BigDecimal(val);
			}
		}
		return String.valueOf(minBd);
	}

	/**
	 * Gets minimum
	 * 
	 * @param values
	 * @return
	 */
	public static String minArr(String... values) {
		if (null == values || values.length == 0) {
			return null;
		}
		return min(values[0], values);
	}

	/**
	 * Judge whether the string is empty (independent of the third party)
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isBlank0(String str) {
		return null == str || str.trim().length() == 0;
	}

	/** Default division accuracy */
	private static final int DEFAULT_DIV_SCALE = 2;

}
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
package com.wl4g.component.common.codec;

import static java.lang.System.out;

import org.apache.commons.codec.binary.Hex;
import com.wl4g.component.common.codec.Encodes;
import static com.wl4g.component.common.lang.TypeConverts.*;

public class BinaryCodecTests {

	/**
	 * <pre>
	正数的原码、反码、补码都一样；
	负数的反码 = 原码的符号位不变，其他位取反；
	负数的补码 = 反码+1；
	0的原码、反码、补码都是0；
	计算机以补码进行运算；
	取反不同于反码；
	 * </pre>
	 */
	// https://blog.csdn.net/codegeek_jfl/article/details/81979325
	public static void main(String[] args) throws Exception {
		String base64 = Encodes.encodeBase64("123");
		out.println("base64: " + base64);
		String hex = base64ToHexString(base64);
		out.println("hex: " + hex);
		out.println("base64: " + hexToBase64String(hex));
		out.println("---------------");
		out.println("--111--");
		out.println(Hex.encodeHex(new byte[] { 97 }));// 61
		out.println(97);// 97
		out.println((char) 97);// a
		out.println(Integer.bitCount(97));
		out.println(Integer.highestOneBit(97));
		out.println(Integer.lowestOneBit(97));
		out.println(Integer.toBinaryString(97));
		// https://blog.csdn.net/briblue/article/details/70296326
		out.println(Integer.toBinaryString(-20));// 输出为补码形式:11111111-11111111-11111111-11101100
		out.println(Integer.toBinaryString(byte2short(new byte[] { 97, 97 })));// 输出为补码形式:1100001-01100001
		out.println(Integer.toHexString(97));
		out.println("--222--");
		out.println(100);// decimal number => 100
		out.println(0144);// octal number => 100
		out.println("--333--");
		out.println(Integer.parseInt("70", 16));// hex to decimal => 112
		out.println(Integer.parseInt("100", 16));// hex to decimal => 256
		out.println(0x70 + 0x100);// hex number add => 368
		out.println("--444--");
		out.println(Integer.parseInt("070", 8));// hex to octal => 56
		out.println(Integer.parseInt("0100", 8));// hex to octal => 64
		out.println(070 + 0100);// octal number add => 120
		out.println("--555--");
		out.println(70 | 100);
		out.println(100 | 70);
		out.println("--666--");
		/**
		 * <pre>
		 *  <<表示左移移，不分正负数，低位补0；　
		 *  注：以下数据类型默认为byte-8位
		 *  左移时不管正负，低位补0
		 *  正数：r = 20 << 2
		 *  　　20的二进制补码：0001 0100
		 *  　　向左移动两位后：0101 0000
		 *  　　　　   　　结果：r = 80
		 *  负数：r = -20 << 2
		 *  　　-20 的二进制原码: 1001 0100
		 *  　　-20 的二进制反码: 1110 1011
		 *  　　-20 的二进制补码: 1110 1100
		 *  　　左移两位后的补码:  1011 0000
		 *  　　　　　　　　反码:  1010 1111
		 *  　　　　　　　　原码:  1101 0000
		 *  　　　　　　　　结果:  r = -80
		 * </pre>
		 */
		out.println(20 << 2);// => 80
		/**
		 * <pre>
		 * >>>表示无符号右移，也叫逻辑右移，即若该数为正，则高位补0，而若该数为负数，则右移后高位同样补0
		 *  正数：r = 20 >>> 2 的结果与 r = 20 >> 2 相同；
		 *  负数：r = -20 >>> 2
		 *  如：以下数据为-20(int32位)的二进制
		 *  	   源码: 10000000 00000000 00000000 00010100
		 *  　　　　反码: 11111111 11111111 11111111 11101011
		 *  　　　　补码: 11111111 11111111 11111111 11101100
		 *  　　　　右移: 00111111 11111111 11111111 11111011
		 *  　　　　结果: r = 1073741819
		 *  结论: >>操作和>>>操作结果相等，负数：>>操作和>>>操作结果不相等
		 * </pre>
		 */
		out.println(20 >> 2);// 5
		out.println(20 >>> 2);// 5
		out.println(-20 >> 2);// -6
		out.println(-20 >>> 2);// 1073741819
		out.println(Integer.parseInt("00111111 11111111 11111111 11111011".replace(" ", ""), 2));// 1073741819
		out.println("--777--");
		// hex str to binary str => 110010
		out.println(Integer.toBinaryString(Integer.parseInt("32", 16)));
	}

	public static String base64ToHexString(String base64) {
		return Encodes.encodeHex(Encodes.decodeBase64(base64));
	}

	public static String hexToBase64String(String hex) {
		return Encodes.encodeBase64(Encodes.decodeHex(hex));
	}

}
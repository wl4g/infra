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
package com.wl4g.component.common.nlp;

/**
 * {@link Pinyin4jUtilsTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-10-07
 * @sine v1.0.0
 * @see
 */
public class Pinyin4jUtilsTests {

	public static void main(String[] args) {
		System.out.println(PingyUtils.getPingyin("广东省"));
		System.out.println(PingyUtils.getPinyinHeadChar("广东省"));
		System.out.println(PingyUtils.getPinyinHeadChar("北京市"));
	}

}

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
package com.wl4g.component.common.matching;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * {@link RegexMatcher}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-03-17
 * @sine v1.0
 * @see
 */
public class RegexMatcher {
	private transient final Map<String, Pattern> cache = new ConcurrentHashMap<>(16);
	private final boolean sensitive;
	private final String[] regexs;

	public RegexMatcher(String... regexs) {
		this(true, regexs);
	}

	public RegexMatcher(boolean sensitive, String... regexs) {
		this.sensitive = sensitive;
		this.regexs = notNullOf(regexs, "regexs");
	}

	public boolean isSensitive() {
		return sensitive;
	}

	public String[] getRegexs() {
		return regexs;
	}

	public boolean matches(String input) {
		for (String r : regexs) {
			if (getCachePattern(r).matcher(input).matches()) {
				return true;
			}
		}
		return false;
	}

	public boolean matchesAny(String... inputs) {
		for (String input : inputs) {
			if (matches(input)) {
				return true;
			}
		}
		return false;
	}

	public boolean matchesAll(String... inputs) {
		for (String input : inputs) {
			if (!matches(input)) {
				return false;
			}
		}
		return true;
	}

	private final Pattern getCachePattern(String regex) {
		Pattern pattern = cache.get(regex);
		if (isNull(pattern)) {
			synchronized (this) {
				pattern = cache.get(regex);
				if (isNull(pattern)) {
					Pattern p = null;
					if (sensitive) {
						p = Pattern.compile(regex);
					}
					p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
					cache.put(regex, p);
					return p;
				}
			}
		}
		return pattern;
	}

}

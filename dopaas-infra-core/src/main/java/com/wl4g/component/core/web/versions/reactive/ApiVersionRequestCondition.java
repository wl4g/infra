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
package com.wl4g.component.core.web.versions.reactive;

import static com.wl4g.component.common.lang.StringUtils2.eqIgnCase;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.result.condition.RequestCondition;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.component.common.collection.CollectionUtils2;
import com.wl4g.component.common.lang.StringUtils2;
import com.wl4g.component.core.web.versions.VersionConditionSupport;
import com.wl4g.component.core.web.versions.annotation.ApiVersionMappingWrapper;
import com.wl4g.component.core.web.versions.annotation.ApiVersionMappingWrapper.ApiVersionWrapper;

/**
 * Reactive API versions number rules condition.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
public class ApiVersionRequestCondition extends VersionConditionSupport implements RequestCondition<ApiVersionRequestCondition> {

	public ApiVersionRequestCondition(ApiVersionMappingWrapper mapping) {
		super(mapping);
	}

	public ApiVersionRequestCondition(ApiVersionMappingWrapper mapping, List<String> matchedCandidateVersions) {
		super(mapping, matchedCandidateVersions);
	}

	@Override
	public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
		// Use the nearest definition priority principle, that is, the
		// definition on the method covers the definition above the type.
		return new ApiVersionRequestCondition(other.getVersionMapping());
	}

	/**
	 * At this point, it indicates that the URL has been matched, and more
	 * matching candidate versions need to be obtained. </br>
	 * refer:
	 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#lookupHandlerMethod()}
	 * and
	 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#addMatchingMappings()}
	 * 
	 * @see <a href=
	 *      "https://my.oschina.net/zhangxufeng/blog/2177464">https://my.oschina.net/zhangxufeng/blog/2177464</a>
	 */
	@Override
	public ApiVersionRequestCondition getMatchingCondition(ServerWebExchange exchange) {
		String requestVer = findRequestParameter(exchange, getVersionConfig().getVersionParams());
		String requestVerGroup = findRequestParameter(exchange, getVersionConfig().getGroupParams());
		log.debug("Comparing rqeuest version: {}, group: {}", requestVer, requestVerGroup);

		// 获取满足以下条件的所有候选版本号:
		// 1: 首先满足客户端分组(group);
		// 2: 其次要满足小于等于请求版本号(向下兼容);
		List<String> matchingCandidateVersions = new ArrayList<>(getVersionMapping().getApiVersions().size());
		for (ApiVersionWrapper wrap : getVersionMapping().getApiVersions()) {
			for (String group : wrap.getGroups()) {
				// First match API version group.
				if (getEqualer().apply(group, requestVerGroup)) {
					// Downward compatible:
					// If the request version is greater than or equal
					// to the configuration version number, then satisfied.
					if (getVersionConfig().getVersionComparator().compare(requestVer, wrap.getValue()) >= 0) {
						matchingCandidateVersions.add(wrap.getValue());
					}
				}
			}
		}

		if (!CollectionUtils2.isEmpty(matchingCandidateVersions)) {
			return new ApiVersionRequestCondition(getVersionMapping(), matchingCandidateVersions);
		}

		// No matched to must return null
		return null;
	}

	/**
	 * At this point, it indicates that multiple downward compatible candidates
	 * have been matched and a comparison is needed to select the best version.
	 * </br>
	 * refer:
	 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#lookupHandlerMethod()}
	 * and
	 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#addMatchingMappings()}
	 * 
	 * @see {@link org.springframework.web.servlet.mvc.condition.PatternsRequestCondition.compareTo#compareTo()}
	 */
	@Override
	public int compareTo(ApiVersionRequestCondition other, ServerWebExchange exchange) {
		if (CollectionUtils2.isEmpty(other.getMatchingCandidateVersions())
				&& CollectionUtils2.isEmpty(getMatchingCandidateVersions())) {
			return 0;
		}
		if (CollectionUtils2.isEmpty(getMatchingCandidateVersions())) {
			return 1;
		}
		if (CollectionUtils2.isEmpty(other.getMatchingCandidateVersions())) {
			return -1;
		}

		// [Refer]:org.springframework.web.servlet.mvc.condition.PatternsRequestCondition.compareTo#compareTo()
		Iterator<String> it = getMatchingCandidateVersions().iterator();
		Iterator<String> itOther = other.getMatchingCandidateVersions().iterator();
		while (it.hasNext() && itOther.hasNext()) {
			int result = getVersionConfig().getVersionComparator().compare(itOther.next(), it.next());
			if (result != 0) {
				return result;
			}
		}
		if (it.hasNext()) {
			return -1;
		} else if (itOther.hasNext()) {
			return 1;
		}
		return 0;
	}

	/**
	 * Gets reactive request parameter value.
	 * 
	 * @param exchange
	 * @param names
	 * @return
	 */
	protected String findRequestParameter(ServerWebExchange exchange, String[] names) {
		ServerHttpRequest request = exchange.getRequest();
		for (String name : names) {
			String value = request.getQueryParams().getFirst(name);
			value = (isBlank(value) || equalsAnyIgnoreCase(value, "null", "undefined")) ? request.getHeaders().getFirst(name)
					: value;
			// HTTP headers specification, for example: 'X-Version: 2.0.10.1'
			value = isBlank(value) ? request.getHeaders().getFirst("x-".concat(name)) : value;
			if (!isBlank(value)) {
				return value;
			}
		}
		return null;
	}

	private BiFunction<String, String, Boolean> getEqualer() {
		return getVersionConfig().isSensitiveParams() ? sensitiveFunc : unsensitiveFunc;
	}

	private static final BiFunction<String, String, Boolean> sensitiveFunc = (o1, o2) -> StringUtils2.equals(o1, o2);
	private static final BiFunction<String, String, Boolean> unsensitiveFunc = (o1, o2) -> eqIgnCase(o1, o2);

}

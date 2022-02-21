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
package com.wl4g.component.core.web.versions.annotation;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.core.env.Environment;

/**
 * {@link ApiVersionMapping} annotation metadata wrapper.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-03
 * @sine v1.0
 * @see
 */
public class ApiVersionMappingWrapper {

	@NotNull
	private final ApiVersionManagementWrapper versionConfig;
	@NotNull
	private final List<ApiVersionWrapper> apiVersions;

	public ApiVersionMappingWrapper(@NotNull ApiVersionManagementWrapper versionConfig,
			@NotNull List<ApiVersionWrapper> apiVersions) {
		this.versionConfig = versionConfig;
		this.apiVersions = apiVersions;
	}

	public ApiVersionManagementWrapper getVersionConfig() {
		return versionConfig;
	}

	public List<ApiVersionWrapper> getApiVersions() {
		return apiVersions;
	}

	/**
	 * {@link ApiVersion} annotation metadata wrapper.
	 */
	public static class ApiVersionWrapper {

		/**
		 * Resolved placeholders value of {@link ApiVersion#groups()}
		 */
		private final String[] groups;

		/**
		 * Resolved placeholders value of {@link ApiVersion#value()}
		 */
		private final String value;

		public ApiVersionWrapper(String[] groups, String value) {
			this.groups = groups;
			this.value = value;
		}

		public String[] getGroups() {
			return groups;
		}

		public String getValue() {
			return value;
		}

	}

	/**
	 * Building wrap {@link ApiVersionMappingWrapper} instance.
	 * 
	 * @param environment
	 * @param sensitive
	 * @param mapping
	 * @return
	 */
	public static ApiVersionMappingWrapper wrap(Environment environment, ApiVersionManagementWrapper versionConfig,
			ApiVersionMapping mapping) {
		List<ApiVersionWrapper> apiVersions = new ArrayList<>();

		for (ApiVersion ver : safeArrayToList(mapping.value())) {
			if ((nonNull(ver) && !isBlank(ver.value()))) {
				String[] groups = safeArrayToList(ver.groups()).stream().filter(g -> !isBlank(g))
						.map(g -> environment.resolveRequiredPlaceholders(g)).toArray(String[]::new);

				apiVersions.add(new ApiVersionWrapper(groups, environment.resolveRequiredPlaceholders(ver.value())));
			}
		}

		return new ApiVersionMappingWrapper(versionConfig, apiVersions);
	}

}

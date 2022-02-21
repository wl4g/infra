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

import static org.apache.commons.lang3.StringUtils.startsWithAny;

import java.util.Set;
import java.util.function.Predicate;

import com.wl4g.component.common.lang.SimpleVersionComparator;

import static com.wl4g.component.common.lang.ClassUtils2.getPackageName;

/**
 * {@link ApiVersionManagementWrapper}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-04
 * @sine v1.0
 * @see
 */
public class ApiVersionManagementWrapper {

	/**
	 * {@link EnableApiVersionManagement#value()} and
	 * {@link EnableApiVersionManagement#basePackages()} and
	 * {@link EnableApiVersionManagement#basePackageClasses()}
	 */
	private final Predicate<Class<?>> mergedIncludeFilter;

	/**
	 * {@link EnableApiVersionManagement#sensitiveParams()}
	 */
	private final boolean sensitiveParams;

	/**
	 * {@link EnableApiVersionManagement#versionParams()}
	 */
	private final String[] versionParams;

	/**
	 * {@link EnableApiVersionManagement#groupParams()}
	 */
	private final String[] groupParams;

	/**
	 * {@link EnableApiVersionManagement#versionComparator()}
	 */
	private final SimpleVersionComparator versionComparator;

	public ApiVersionManagementWrapper(Set<String> basePackages, boolean sensitiveParams, String[] versionParams,
			String[] groupParams, SimpleVersionComparator versionComparator) {
		final String[] basePackages0 = basePackages.toArray(new String[0]);
		this.mergedIncludeFilter = (beanType -> startsWithAny(getPackageName(beanType), basePackages0));
		this.sensitiveParams = sensitiveParams;
		this.versionParams = versionParams;
		this.groupParams = groupParams;
		this.versionComparator = versionComparator;
	}

	public Predicate<Class<?>> getMergedIncludeFilter() {
		return mergedIncludeFilter;
	}

	public boolean isSensitiveParams() {
		return sensitiveParams;
	}

	public String[] getVersionParams() {
		return versionParams;
	}

	public String[] getGroupParams() {
		return groupParams;
	}

	public SimpleVersionComparator getVersionComparator() {
		return versionComparator;
	}

}

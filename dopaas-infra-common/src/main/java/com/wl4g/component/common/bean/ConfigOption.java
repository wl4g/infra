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
package com.wl4g.component.common.bean;

import javax.annotation.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.wl4g.component.common.lang.Assert2.*;
import static java.util.Arrays.asList;

/**
 * {@link ConfigOption}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-16
 * @since
 */
public class ConfigOption {

	/** Config option name. */
	@NotBlank
	private String name;

	/** Config candidates options values. */
	@NotEmpty
	private List<String> values;

	/** Current selected option value. */
	@Nullable
	private String selectedValue;

	public ConfigOption() {
		super();
	}

	public ConfigOption(@NotBlank String name, @NotEmpty String... values) {
		this(name, asList(notEmptyOf(values, "values")));
	}

	public ConfigOption(@NotBlank String name, @NotEmpty List<String> values) {
		setName(name);
		setValues(values);
	}

	/**
	 * Gets extra option name.
	 * 
	 * @return
	 */
	@NotBlank
	public final String getName() {
		return name;
	}

	/**
	 * Sets extra option name.
	 * 
	 * @param name
	 */
	public void setName(@NotBlank String name) {
		this.name = hasTextOf(name, "name");
	}

	/**
	 * Sets extra option name.
	 * 
	 * @param name
	 */
	public ConfigOption withName(@NotBlank String name) {
		setName(name);
		return this;
	}

	/**
	 * Gets extra option values.
	 * 
	 * @return
	 */
	@NotEmpty
	public final List<String> getValues() {
		return values;
	}

	/**
	 * Sets extra option values.
	 * 
	 * @param values
	 */
	public void setValues(@NotEmpty List<String> values) {
		this.values = notEmptyOf(values, "values");
	}

	/**
	 * Sets extra option values.
	 * 
	 * @param values
	 */
	public ConfigOption withValues(@NotEmpty List<String> values) {
		setValues(values);
		return this;
	}

	/**
	 * Gets selected extra option value.
	 * 
	 * @return
	 */
	@Nullable
	public final String getSelectedValue() {
		return selectedValue;
	}

	/**
	 * Sets selected extra option value.
	 * 
	 * @param values
	 */
	public void setSelectedValue(@Nullable String selectedValue) {
		// this.selectedValue = hasTextOf(selectedValue,
		// "selectedValue");
		this.selectedValue = selectedValue;
	}

	/**
	 * Sets selected extra option value.
	 * 
	 * @param values
	 */
	public ConfigOption withSelectedValue(@Nullable String selectedValue) {
		setSelectedValue(selectedValue);
		return this;
	}

	/**
	 * Validation for itself attributes.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ConfigOption> T validate() {
		hasTextOf(name, "name");
		notEmptyOf(values, "values");
		return (T) this;
	}

	/**
	 * Validation for config option attributes.
	 * 
	 * @param option
	 */
	public static void validate(@NotNull ConfigOption option) {
		notNullOf(option, "option");
		hasTextOf(option.getName(), "name");
		notEmptyOf(option.getValues(), "values");
	}

}
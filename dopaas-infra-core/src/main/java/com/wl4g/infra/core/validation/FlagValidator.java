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
package com.wl4g.infra.core.validation;

import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link FlagValidator}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
public class FlagValidator implements ConstraintValidator<FlagValue, String> {

    private Boolean required;
    private Boolean caseSensitive;

    @Override
    public void initialize(FlagValue constraintAnnotation) {
        this.required = constraintAnnotation.required();
        this.caseSensitive = constraintAnnotation.caseSensitive();
    }

    @Override
    public boolean isValid(String flagValue, ConstraintValidatorContext context) {
        if (required) {
            return doValid(flagValue);
        } else {
            // Can be blank if not required.
            if (isBlank(flagValue)) {
                return true;
            } else {
                return doValid(flagValue);
            }
        }
    }

    private boolean doValid(String flagValue) {
        if (caseSensitive) {
            return StringUtils.equalsAny(flagValue, "Y", "N");
        }
        return equalsAnyIgnoreCase(flagValue, "Y", "N");
    }

}
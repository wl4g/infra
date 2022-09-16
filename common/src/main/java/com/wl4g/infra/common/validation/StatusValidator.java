/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.common.validation;

import static com.wl4g.infra.common.lang.StringUtils2.eqIgnCase;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link StatusValidator}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
public class StatusValidator implements ConstraintValidator<StatusValue, String> {

    private Boolean required;
    private Boolean caseSensitive;
    private List<String> options;

    @Override
    public void initialize(StatusValue constraintAnnotation) {
        this.required = constraintAnnotation.required();
        this.caseSensitive = constraintAnnotation.caseSensitive();
        this.options = asList(constraintAnnotation.options());
    }

    @Override
    public boolean isValid(String statusValue, ConstraintValidatorContext context) {
        if (isBlank(statusValue)) {
            if (required) {
                return false;
            } else {
                return true;
            }
        } else {
            if (caseSensitive) {
                return options.stream().anyMatch(o -> StringUtils.equals(o, statusValue));
            }
            return options.stream().anyMatch(o -> eqIgnCase(o, statusValue));
        }
    }

}
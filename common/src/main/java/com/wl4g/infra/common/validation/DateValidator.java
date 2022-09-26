/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.text.ParseException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.time.DateUtils;

/**
 * {@link DateValidator}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
public class DateValidator implements ConstraintValidator<DateValue, String> {

    private Boolean required;
    private String format;

    @Override
    public void initialize(DateValue constraintAnnotation) {
        this.required = constraintAnnotation.required();
        this.format = constraintAnnotation.format();
    }

    @Override
    public boolean isValid(String dateValue, ConstraintValidatorContext context) {
        if (isBlank(dateValue)) {
            // If it is not a required parameter, it means legal.
            return !required;
        } else {
            try {
                // Check date format.
                DateUtils.parseDate(dateValue, format);
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
    }

}
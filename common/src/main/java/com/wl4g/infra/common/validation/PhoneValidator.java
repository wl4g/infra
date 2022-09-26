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

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@link PhoneValidator}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-04-03 v3.0.0
 * @since v3.0.0
 */
public class PhoneValidator implements ConstraintValidator<PhoneValue, String> {

    private Boolean required;
    private String regex;

    @Override
    public void initialize(PhoneValue constraintAnnotation) {
        this.required = constraintAnnotation.required();
        this.regex = constraintAnnotation.regex();
    }

    @Override
    public boolean isValid(String phoneValue, ConstraintValidatorContext context) {
        if (isBlank(phoneValue)) {
            if (required) {
                return false;
            } else {
                return true;
            }
        } else {
            return patternLocal.get().matcher(phoneValue).matches();
        }
    }

    private final ThreadLocal<Pattern> patternLocal = ThreadLocal.withInitial(() -> Pattern.compile(regex));
}
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

import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.makeAccessible;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.lang.reflect.Field;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@link EnumValidator}
 * 
 * @author James Wong
 * @version 2022-09-16
 * @since v3.0.0
 */
public class EnumValidator implements ConstraintValidator<EnumValue, Object> {

    private Class<?>[] enumClass;
    private String fieldName;
    private boolean caseSensitive;
    private boolean hasText;

    @Override
    public void initialize(EnumValue constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumCls();
        this.fieldName = constraintAnnotation.enumField();
        this.caseSensitive = constraintAnnotation.caseSensitive();
        this.hasText = constraintAnnotation.hasText();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (nonNull(value) && !isBlank(value.toString())) {
            if (enumClass.length > 0) {
                for (Class<?> cl : enumClass) {
                    if (cl.isEnum()) {
                        try {
                            // 匹配枚举常量名
                            Enum[] constants = (Enum[]) cl.getEnumConstants();
                            for (Enum constant : constants) {
                                String constantName = constant.name();
                                if (caseSensitive ? constantName.equals(value.toString())
                                        : equalsIgnoreCase(constantName, value.toString())) {
                                    return true;
                                }
                            }
                            // 匹配枚举常量的属性值
                            if (!isBlank(fieldName)) {
                                Field field = findField(cl, fieldName);
                                if (nonNull(field)) {
                                    makeAccessible(field);
                                    for (Object constant : constants) {
                                        Object fieldValue = getField(field, constant);
                                        if (nonNull(fieldValue)) {
                                            String fieldValueStr = fieldValue.toString();
                                            if (caseSensitive ? fieldValueStr.equals(value.toString())
                                                    : equalsIgnoreCase(fieldValueStr, value.toString())) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }
            } else {
                return true;
            }
        }
        return !hasText;
    }

}

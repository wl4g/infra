/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
package com.wl4g.infra.common.graalvm.substitute;

import static java.util.Objects.isNull;

import java.security.SecureRandom;

/**
 * {@link SecureRandomDefault}
 * 
 * @author James Wong
 * @version 2023-02-07
 * @since v1.0.0
 */
public class SecureRandomDefault {
    private static SecureRandom REPLACED_SECURE_RANDOM;

    public static SecureRandom getDefault() {
        if (isNull(REPLACED_SECURE_RANDOM)) {
            synchronized (SecureRandomDefault.class) {
                if (isNull(REPLACED_SECURE_RANDOM)) {
                    REPLACED_SECURE_RANDOM = new SecureRandom();
                }
            }
        }
        return REPLACED_SECURE_RANDOM;
    }

}

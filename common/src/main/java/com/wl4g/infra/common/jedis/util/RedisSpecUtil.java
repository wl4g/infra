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
package com.wl4g.infra.common.jedis.util;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import lombok.CustomLog;

/**
 * Redis key specifications utils(formatter etc).
 * 
 * @author James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2020年4月10日
 * @since
 */
@CustomLog
public abstract class RedisSpecUtil {

    /**
     * Check is result is successful.
     * 
     * @param res
     * @return
     */
    public static boolean isSuccess(@Nullable final String res) {
        return equalsIgnoreCase(res, "OK") || (!isBlank(res) && isNumeric(res) && Long.parseLong(res) > 0);
    }

    /**
     * Check is result is successful.
     * 
     * @param res
     * @return
     */
    public static boolean isSuccess(@Nullable final Long res) {
        return !isNull(res) && res > 0;
    }

    /**
     * Check input argument names specification.
     * 
     * @param keys
     * @throws ParameterNormativeException
     */
    public static void safeCheckKeys(@NotNull final List<?> keys) throws NoCanonicalParamaterException {
        notNullOf(keys, "jedis operation key");
        for (Object key : keys) {
            char[] _key = null;
            if (key instanceof String) {
                _key = key.toString().toCharArray();
            } else if (char.class.isAssignableFrom(key.getClass())) {
                _key = new char[] { (char) key };
            } else if (key instanceof char[]) {
                _key = (char[]) key;
            }

            if (isNull(_key)) {
                continue;
            }

            // The check exclusion key contains special characters such
            // as '-', '$', ' ' etc and so on.
            for (char c : _key) {
                String warning = format(
                        "The operation redis keys: %s there are unsafe characters: '%s', Because of the binary safety mechanism of redis, it may not be got",
                        keys, c);
                if (!isInvalidCharacter(c)) {
                    if (warnKeyChars.contains(c)) { // Warning key chars
                        log.warn(warning);
                        return;
                    } else {
                        throw new NoCanonicalParamaterException(warning);
                    }
                }
            }
        }
    }

    /**
     * Formating redis arguments unsafe characters, e.g: '-' to '_'
     * 
     * @param key
     * @return
     */
    public static String safeFormat(@Nullable final String key) {
        return safeFormat(key, '_');
    }

    /**
     * Formating redis arguments unsafe characters, e.g: '-' to '_'
     * 
     * @param key
     * @param safeChar
     *            Replace safe character
     * @return
     */
    public static String safeFormat(@Nullable final String key, final char safeChar) {
        if (isBlank(key)) {
            return key;
        }
        safeCheckKeys(singletonList(safeChar));

        // The check exclusion key contains special characters such
        // as '-', '$', ' ' etc and so on.
        StringBuffer _key = new StringBuffer(key.length());
        for (char c : key.toString().toCharArray()) {
            if (isInvalidCharacter(c)) {
                _key.append(c);
            } else {
                _key.append(safeChar);
            }
        }
        return _key.toString();
    }

    /**
     * Check is invalid redis arguments character.
     * 
     * @param c
     * @return
     */
    public static boolean isInvalidCharacter(final char c) {
        return !isNull(c) && isNumeric(valueOf(c)) || isAlpha(valueOf(c)) || safeKeyChars.contains(c);
    }

    /**
     * Redis key-name safe characters.
     */
    private static final List<Character> safeKeyChars = unmodifiableList(new ArrayList<Character>(4) {
        private static final long serialVersionUID = -7144798722787955277L;
        {
            add(':');
            add('_');
            add('.');
            add('@');
            // @see:JedisClusterCRC16#getSlot(byte[]), support user tag
            // slot.
            add('{');
            add('}');
        }
    });

    /**
     * Redis key-name safe characters.
     */
    private static final List<Character> warnKeyChars = unmodifiableList(new ArrayList<Character>(4) {
        private static final long serialVersionUID = -7144798722787955277L;
        {
            add('&');
            add('!');
            add('*');
        }
    });

}
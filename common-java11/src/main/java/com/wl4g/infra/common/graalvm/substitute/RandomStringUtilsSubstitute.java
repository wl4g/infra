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

import java.util.Random;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * {@link RandomStringUtilsSubstitute}
 * 
 * @author James Wong
 * @version 2023-02-07
 * @since v3.1.0
 */
@TargetClass(className = "org.apache.commons.lang3.RandomStringUtils")
final class RandomStringUtilsSubstitute {

    @Delete
    private static Random RANDOM;

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String random(final int count) {
        return random(count, false, false);
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomAscii(final int count) {
        return random(count, 32, 127, false, false);
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomAscii(final int minLengthInclusive, final int maxLengthExclusive) {
        return randomAscii(RandomUtils.nextInt(minLengthInclusive, maxLengthExclusive));
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomAlphabetic(final int count) {
        return random(count, true, false);
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomAlphabetic(final int minLengthInclusive, final int maxLengthExclusive) {
        return randomAlphabetic(RandomUtils.nextInt(minLengthInclusive, maxLengthExclusive));
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomAlphanumeric(final int count) {
        return random(count, true, true);
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomAlphanumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return randomAlphanumeric(RandomUtils.nextInt(minLengthInclusive, maxLengthExclusive));
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomGraph(final int count) {
        return random(count, 33, 126, false, false);
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomGraph(final int minLengthInclusive, final int maxLengthExclusive) {
        return randomGraph(RandomUtils.nextInt(minLengthInclusive, maxLengthExclusive));
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomNumeric(final int count) {
        return random(count, false, true);
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomNumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return randomNumeric(RandomUtils.nextInt(minLengthInclusive, maxLengthExclusive));
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomPrint(final int count) {
        return random(count, 32, 126, false, false);
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String randomPrint(final int minLengthInclusive, final int maxLengthExclusive) {
        return randomPrint(RandomUtils.nextInt(minLengthInclusive, maxLengthExclusive));
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String random(final int count, final boolean letters, final boolean numbers) {
        return random(count, 0, 0, letters, numbers);
    }

    @Substitute
    public static String random(final int count, final int start, final int end, final boolean letters, final boolean numbers) {
        return random(count, start, end, letters, numbers, null, RandomDefault.getDefault());
    }

    @Substitute
    public static String random(
            final int count,
            final int start,
            final int end,
            final boolean letters,
            final boolean numbers,
            final char... chars) {
        return random(count, start, end, letters, numbers, chars, RandomDefault.getDefault());
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static String random(
            int count,
            int start,
            int end,
            final boolean letters,
            final boolean numbers,
            final char[] chars,
            final Random random) {
        if (count == 0) {
            return StringUtils.EMPTY;
        } else if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }
        if (chars != null && chars.length == 0) {
            throw new IllegalArgumentException("The chars array must not be empty");
        }

        if (start == 0 && end == 0) {
            if (chars != null) {
                end = chars.length;
            } else {
                if (!letters && !numbers) {
                    end = Integer.MAX_VALUE;
                } else {
                    end = 'z' + 1;
                    start = ' ';
                }
            }
        } else {
            if (end <= start) {
                throw new IllegalArgumentException("Parameter end (" + end + ") must be greater than start (" + start + ")");
            }
        }

        final char[] buffer = new char[count];
        final int gap = end - start;

        while (count-- != 0) {
            char ch;
            if (chars == null) {
                ch = (char) (random.nextInt(gap) + start);
            } else {
                ch = chars[random.nextInt(gap) + start];
            }
            if (letters && Character.isLetter(ch) || numbers && Character.isDigit(ch) || !letters && !numbers) {
                if (ch >= 56320 && ch <= 57343) {
                    if (count == 0) {
                        count++;
                    } else {
                        // low surrogate, insert high surrogate after putting it
                        // in
                        buffer[count] = ch;
                        count--;
                        buffer[count] = (char) (55296 + random.nextInt(128));
                    }
                } else if (ch >= 55296 && ch <= 56191) {
                    if (count == 0) {
                        count++;
                    } else {
                        // high surrogate, insert low surrogate before putting
                        // it in
                        buffer[count] = (char) (56320 + random.nextInt(128));
                        count--;
                        buffer[count] = ch;
                    }
                } else if (ch >= 56192 && ch <= 56319) {
                    // private high surrogate, no effing clue, so skip it
                    count++;
                } else {
                    buffer[count] = ch;
                }
            } else {
                count++;
            }
        }
        return new String(buffer);
    }

    @Substitute
    public static String random(final int count, final String chars) {
        if (chars == null) {
            return random(count, 0, 0, false, false, null, RandomDefault.getDefault());
        }
        return random(count, chars.toCharArray());
    }

    @Substitute
    public static String random(final int count, final char... chars) {
        if (chars == null) {
            return random(count, 0, 0, false, false, null, RandomDefault.getDefault());
        }
        return random(count, 0, chars.length, false, false, chars, RandomDefault.getDefault());
    }

}

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

import org.apache.commons.lang3.Validate;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * {@link RandomUtilsSubstitute}
 * 
 * @author James Wong
 * @since v3.1.0
 */
@TargetClass(className = "org.apache.commons.lang3.RandomUtils")
final class RandomUtilsSubstitute {

    @Delete
    private static Random RANDOM;

    @Substitute
    public static boolean nextBoolean() {
        return RandomDefault.getDefault().nextBoolean();
    }

    @Substitute
    public static byte[] nextBytes(final int count) {
        Validate.isTrue(count >= 0, "Count cannot be negative.");
        final byte[] result = new byte[count];
        RandomDefault.getDefault().nextBytes(result);
        return result;
    }

    @Substitute
    public static int nextInt(final int startInclusive, final int endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endExclusive) {
            return startInclusive;
        }
        return startInclusive + RandomDefault.getDefault().nextInt(endExclusive - startInclusive);
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static int nextInt() {
        return nextInt(0, Integer.MAX_VALUE);
    }

    @Substitute
    public static long nextLong(final long startInclusive, final long endExclusive) {
        Validate.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endExclusive) {
            return startInclusive;
        }
        return (long) nextDouble(startInclusive, endExclusive);
    }

    @Substitute
    public static long nextLong() {
        return nextLong(0, Long.MAX_VALUE);
    }

    @Substitute
    public static double nextDouble(final double startInclusive, final double endInclusive) {
        Validate.isTrue(endInclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endInclusive) {
            return startInclusive;
        }
        return startInclusive + ((endInclusive - startInclusive) * RandomDefault.getDefault().nextDouble());
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static double nextDouble() {
        return nextDouble(0, Double.MAX_VALUE);
    }

    @Substitute
    public static float nextFloat(final float startInclusive, final float endInclusive) {
        Validate.isTrue(endInclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Validate.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endInclusive) {
            return startInclusive;
        }
        return startInclusive + ((endInclusive - startInclusive) * RandomDefault.getDefault().nextFloat());
    }

    @Alias // 打上标记，让当前(代替类)能够直接访问(原始类)对应的此方法.
    public static float nextFloat() {
        return nextFloat(0, Float.MAX_VALUE);
    }
}

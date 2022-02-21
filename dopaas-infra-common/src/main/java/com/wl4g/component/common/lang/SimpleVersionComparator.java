package com.wl4g.component.common.lang;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isAlphaSpace;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.Comparator;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wl4g.component.common.log.SmartLogger;

/**
 * Simple API version comparator with ASCII.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-30
 * @sine v1.0
 * @see https://www.cnblogs.com/yucongblog/p/5600312.html
 */
public class SimpleVersionComparator implements Comparator<String> {
    protected final SmartLogger log = getLogger(getClass());
    protected final Pattern versionPattern;

    public SimpleVersionComparator() {
        this(DEFAULT_VERSION_REGEX);
    }

    public SimpleVersionComparator(@NotBlank String versionPattern) {
        this.versionPattern = Pattern.compile(hasTextOf(versionPattern, "versionPattern"));
    }

    @Override
    public int compare(@Nullable String version1, @Nullable String version2) {
        if (isNull(version1) || isNull(version2)) {
            return trimToEmpty(version1).compareTo(trimToEmpty(version2));
        }

        // Resolves version numbers
        String[] parts1 = splitVersionParts(version1, false);
        String[] parts2 = splitVersionParts(version2, false);

        // First, direct quick compare
        if (version1.compareTo(version2) == 0) {
            return 0;
        }

        // Cleanup suffix same parts. e.g: 5.7.28-log and 5.7.28.1-log
        int reverseIndex1 = -1, reverseIndex2 = -1;
        for (int i1 = parts1.length - 1, i2 = parts2.length - 1;; i1--, i2--) {
            if (i1 <= 0 || i2 <= 0) {
                break;
            }
            if (isAlphaSpace(parts1[i1]) && isAlphaSpace(parts2[i2]) && parts1[i1].equals(parts2[i2])) {
                reverseIndex1 = i1;
                reverseIndex2 = i2;
            }
        }
        if (reverseIndex1 > 0) {
            parts1 = asList(parts1).subList(0, reverseIndex1).toArray(new String[0]);
        }
        if (reverseIndex2 > 0) {
            parts2 = asList(parts2).subList(0, reverseIndex2).toArray(new String[0]);
        }

        // Check the size of the common parts from left to right with the least
        // number of iterations.
        int iter = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < iter; i++) {
            final int compared = parts1[i].compareTo(parts2[i]);
            if (compared != 0) {
                return compared;
            }
        }

        // At this time, it must be different. Since the public sector can not
        // win, it is a long-term win.
        if (parts1.length > parts2.length) {
            return 1;
        }

        return -1;
    }

    /**
     * Splitting version numbers with pattern.
     * 
     * @param version
     * @param valid
     * @return
     */
    public String[] splitVersionParts(@NotNull String version, boolean valid) {
        String[] parts = versionPattern.split(version);
        if (!isBlank(version) && isEmptyArray(parts)) {
            String errmsg = format(
                    "Invalid version: '%s', Refer to for example: 1.10.0.2a or 1_10_0_2b or 1-10-0-2b etc, The delimiter should satisfy the version regex: '%s'",
                    version, versionPattern);
            if (!valid) {
                log.warn(errmsg);
                return EMPTY_ARRAY;
            }
            throw new IllegalArgumentException(errmsg);
        }
        return parts;
    }

    /**
     * Default version comparator instance.
     */
    public static final SimpleVersionComparator INSTANCE = new SimpleVersionComparator();
    public static final String DEFAULT_VERSION_REGEX = "[-_./;:]";
    public static final String[] EMPTY_ARRAY = {};

}

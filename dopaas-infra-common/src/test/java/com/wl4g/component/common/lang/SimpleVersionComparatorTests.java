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
package com.wl4g.component.common.lang;

import static com.wl4g.component.common.lang.SimpleVersionComparator.INSTANCE;
import static java.lang.System.out;
import static java.util.Arrays.asList;

import org.junit.Test;

import com.wl4g.component.common.lang.SimpleVersionComparator;

/**
 * {@link VersionRequestConditionTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-30
 * @sine v1.0
 * @see
 */
public class SimpleVersionComparatorTests {

    @Test
    public void versionComparateCase1() {
        out.println(INSTANCE.compare("1.10.1.2", "1.10.1.3")); // -1
        out.println(INSTANCE.compare("1_10_1_b", "1_10_1_a")); // 1
        out.println(INSTANCE.compare("1:10:1b", "1:10:1a")); // 1
        out.println(INSTANCE.compare("1/10/1b", "1/10/1a")); // 1
        out.println(INSTANCE.compare("1;10;1;2a", "1;10;1_2b")); // -1
        out.println(INSTANCE.compare("1.0.10.3a", "2.0.1.2b")); // -1
        out.println(INSTANCE.compare("2.0.10.3a", "2.0.1.2b")); // 1
        out.println(INSTANCE.compare("2.0.10.3a", "2.0.10.3a.x")); // -1
        out.println(INSTANCE.compare("5.7.1-log", "5.7.28-log")); // -1
        out.println(INSTANCE.compare("5.7-log", "5.7.28-log")); // -1
        out.println(INSTANCE.compare(null, null)); // 0
        out.println(INSTANCE.compare("a", null)); // 1
    }

    // Negative example:
    @Test /* (expected = IllegalArgumentException.class) */
    public void versionSyntaxOfErrorCase2() {
        SimpleVersionComparator sc = new SimpleVersionComparator("[-_./;:]");
        out.println(asList(sc.splitVersionParts("1.10.1.2b.1", true)));
    }

    // Positive example:
    @Test
    public void versionSyntaxOfSuccessCase2() {
        SimpleVersionComparator sc = new SimpleVersionComparator("[-_./;:]");
        out.println(asList(sc.splitVersionParts("1.10.1.2a", true)));
    }

}

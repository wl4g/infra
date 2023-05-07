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
package com.wl4g.infra.common.lang;

import static com.wl4g.infra.common.lang.DateUtils2.formatDate;
import static com.wl4g.infra.common.lang.DateUtils2.getDate;
import static com.wl4g.infra.common.lang.DateUtils2.parseDate;

import java.util.Date;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.ibm.icu.util.Calendar;

/**
 * {@link DateUtils2Tests}
 * 
 * @author James Wong
 * @version 2020-10-25
 * @since v1.0.0
 */
public class DateUtils2Tests {

    @Test
    public void testFormatAndParse() {
        System.out.println(formatDate(parseDate("2010/3/6")));
        System.out.println(getDate("yyyy年MM月dd日 E"));
        long time = new Date().getTime() - parseDate("2012-11-19").getTime();
        System.out.println(time / (24 * 60 * 60 * 1000));
    }

    @Test
    public void testGetDistanceOf() {
        final Date before = parseDate("2022/10/21 21:14:00");
        final Date after = parseDate("2022/10/22 21:35:00");
        final double distanceOfHours = DateUtils2.getDistanceOf(before, after, "HH");
        System.out.println(distanceOfHours);
        Assertions.assertEquals(distanceOfHours, 24);

        final double distanceOfMinue = DateUtils2.getDistanceOf(before, after, "mm");
        System.out.println(distanceOfMinue);
        Assertions.assertEquals(distanceOfMinue, 1461);
    }

    @Test
    public void testGetDateOf() {
        System.out.println(DateUtils2.getDateOf(Calendar.DAY_OF_MONTH, -1, "yyyy-MM-dd"));
    }

}
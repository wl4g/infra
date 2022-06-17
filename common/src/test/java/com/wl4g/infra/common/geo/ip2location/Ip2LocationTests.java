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
package com.wl4g.infra.common.geo.ip2location;

import static java.lang.String.format;

import org.junit.Test;

import com.wl4g.infra.common.geo.ip2location.v8_8_0.IP2Location;
import com.wl4g.infra.common.lang.SystemUtils2;

/**
 * {@link Ip2LocationTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-06-14 v3.0.0
 * @since v3.0.0
 */
public class Ip2LocationTests {

    @Test
    public void testParseIp2LocationWithV8_8_0() throws Exception {
        String basedir = SystemUtils2.USER_HOME + "/Documents/BACKUP-DATA/BOOK-CLOUD/mnt/disk1/pkg/geodata/ip2location";
        String filename = format("%s/%s", basedir, "IP2LOCATION-LITE-DB11.BIN");

        IP2Location ipl = new IP2Location();
        ipl.Open(filename, true);
        // ipl.Close();

        System.out.println(ipl.IPQuery("8.8.8.8"));
        System.out.println("---------");

        System.out.println(ipl.IPQuery("1.1.1.1"));
        System.out.println("---------");

        System.out.println(ipl.IPQuery("61.141.46.255"));
        System.out.println("---------");

        System.out.println(ipl.IPQuery("176.192.102.130"));
        System.out.println("---------");
    }

}

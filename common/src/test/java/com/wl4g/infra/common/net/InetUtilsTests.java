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
package com.wl4g.infra.common.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

import com.wl4g.infra.common.net.InetUtils.InetUtilsProperties;

/**
 * {@link InetUtilsTests}
 * 
 * @author James Wong
 * @version 2022-10-10
 * @since v3.0.0
 */
public class InetUtilsTests {

    @Test
    public void testFindFirstNonLoopbackHostInfo() throws UnknownHostException {
        try (InetUtils inetUtil = new InetUtils(new InetUtilsProperties());) {
            String address = inetUtil.findFirstNonLoopbackHostInfo().getIpAddress();
            System.out.println(address);
            System.out.println(inetUtil.findFirstNonLoopbackAddress());
        }
        System.out.println(InetAddress.getLocalHost().getHostName());
    }

    @Test
    public void testGetHostName() throws UnknownHostException {
        System.out.println(InetAddress.getLocalHost().getHostName());
    }

}

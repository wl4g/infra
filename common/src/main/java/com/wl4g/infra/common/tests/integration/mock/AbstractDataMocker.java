/**
 * Copyright (C) 2023 ~ 2035 the original authors WL4G (James Wong).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.tests.integration.mock;

import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The {@link AbstractDataMocker}
 *
 * @author James Wong
 * @since v3.1
 **/
public abstract class AbstractDataMocker implements Runnable, Closeable {
    protected final Logger log = getLogger(getClass());

    public abstract void printStatistics();

    @Override
    public void close() throws IOException {
    }

    //public static class MockCustomHostResolver implements HostResolver {
    //    @Override
    //    public InetAddress[] resolve(String host) throws UnknownHostException {
    //        return new InetAddress[] {InetAddress.getLocalHost()};
    //    }
    //}

}

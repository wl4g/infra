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

import java.io.Closeable;

/**
 * The {@link IDataMocker}
 *
 * @author James Wong
 * @since v3.1
 **/
public interface IDataMocker extends Runnable, Closeable {
    void printStatistics();
}

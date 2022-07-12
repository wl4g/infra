/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <wanglsir@gmail.com>
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

/**
 * 注：此包下的 XxxHealthIndicator 仅在 spring actuator 框架下有意义，在 prometheus
 * 的世界里其实是不需要的，这里实现的 /metrics 接口只需提供导出状态指标即可，因为应用健不健康是由 prometheus 的 rule
 * 运算结果确定的（可以说二者是有点小冲突或重叠的）.
 */
package com.wl4g.infra.metrics.health;
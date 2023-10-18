/**
 * Copyright 2017 ~ 2025 the original authors James Wong.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ALL_OR KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.task;

import com.wl4g.infra.common.lang.Assert2;

import javax.validation.constraints.NotBlank;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@link NamedThreadFactory}
 *
 * @author James Wong
 * @since v3.1
 **/
public class NamedThreadFactory implements ThreadFactory {
    private final AtomicInteger threads = new AtomicInteger(1);
    private final String prefix;
    private final ThreadGroup group;

    public NamedThreadFactory(@NotBlank String prefix) {
        this.prefix = Assert2.hasTextOf(prefix, "prefix");
        this.group = Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, prefix + "-" + threads.getAndIncrement(), 0);
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

}

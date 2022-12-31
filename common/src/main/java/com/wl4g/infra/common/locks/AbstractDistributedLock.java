/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.common.locks;

import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.isTrueOf;
import static com.wl4g.infra.common.lang.SystemUtils2.GLOBAL_PROCESS_SERIAL;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;

import com.google.common.annotations.Beta;

import lombok.Getter;
import lombok.ToString;

/**
 * Abstract distributed lock.</br>
 * 
 * @author Wangl.sir James Wong <jameswong1376@gmail.com>>
 * @version v1.0 2019年3月21日
 * @since
 */
@Beta
@Getter
@ToString(callSuper = true)
public abstract class AbstractDistributedLock implements Lock, Serializable {
    private static final long serialVersionUID = -3633610156752730462L;

    /** Current locker name. */
    protected final String lockName;

    /** Current locker request ID. */
    protected final String requestId;

    /** Current locker expired time(MS). */
    protected final long expiredMs;

    public AbstractDistributedLock(String lockName, String requestId, long expiredMs) {
        isTrueOf(expiredMs > 0, "expiredMs > 0");
        this.lockName = hasTextOf(lockName, "lockName");
        this.requestId = hasTextOf(requestId, "requestId");
        this.expiredMs = expiredMs;
    }

    /**
     * Get current thread unique process ID. </br>
     * 
     * <pre>
     * Host serial + local processId + threadId
     * </pre>
     * 
     * @return
     */
    public final static String getThreadCurrentProcessId() {
        return GLOBAL_PROCESS_SERIAL.concat("-") + Thread.currentThread().getId();
    }

}
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
package com.wl4g.infra.common.eventbus;

import org.junit.Test;

import com.google.common.eventbus.Subscribe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link EventBusSupportTests}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2021-12-23 v1.0.0
 * @since v1.0.0
 */
public class EventBusSupportTests {

    private final EventBusSupport bus = EventBusSupport.getDefault();

    public EventBusSupportTests() {
        bus.register(this);
    }

    @Test
    public void testPublish() {
        bus.post(new StartingEvent("Starting server ..."));
        bus.post(new StartedEvent("Started server on 8080."));
    }

    @Subscribe
    public void testSubscribeStartingEvent(StartingEvent event) {
        System.out.println("subscribed starting event message: " + event);
    }

    @Subscribe
    public void testSubscribeStartedEvent(StartedEvent event) {
        System.out.println("subscribed started event message: " + event);
    }

    @Subscribe
    public void testSubscribeAll(Object event) {
        System.out.println("subscribed all event message: " + event);
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class StartingEvent {
        private String message;
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class StartedEvent {
        private String message;
    }

}

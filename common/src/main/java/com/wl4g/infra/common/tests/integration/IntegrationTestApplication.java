/**
 * Copyright 2017 ~ 2025 the original authors James Wong.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ALL_OR KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.tests.integration;

import com.wl4g.infra.common.lang.ClassUtils2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * The {@link IntegrationTestApplication}
 *
 * @author James Wong
 * @since v1.0
 **/
@Slf4j
@Getter
public class IntegrationTestApplication {
    private static volatile IntegrationTestApplication INSTANCE;
    private ConfigurableApplicationContext context;
    private Runnable startedListener;

    public void doMain(String[] args) {
        log.info("Starting for Integration Test Application ...");
        SpringApplication.run(getClass(), args);
    }

    public static IntegrationTestApplication getInstance() {
        if (isNull(INSTANCE)) {
            synchronized (IntegrationTestApplication.class) {
                if (isNull(INSTANCE)) {
                    final String itApplicationClass = System.getenv()
                            .getOrDefault("IT_ASSERTION_APPLICATION_CLASS",
                                    IntegrationTestApplication.class.getName());
                    try {
                        INSTANCE = (IntegrationTestApplication) ClassUtils2.forName(itApplicationClass,
                                        Thread.currentThread().getContextClassLoader())
                                .getConstructor()
                                .newInstance();
                    } catch (Throwable ex) {
                        throw new IllegalStateException("Could not create it application instance", ex);
                    }
                }
            }
        }
        return INSTANCE;
    }

    public void exit() {
        SpringApplication.exit(context);
    }

    public void setStartedListener(@NotNull Runnable startedListener) {
        this.startedListener = requireNonNull(startedListener, "startedListener");
    }

    @Configuration
    static class IntegrationTestApplicationConfiguration {
        @Bean
        public ApplicationRunner applicationRunner(ConfigurableApplicationContext context) {
            getInstance().context = context;
            return args -> {
                if (getInstance().getStartedListener() != null) {
                    getInstance().getStartedListener().run();
                }
            };
        }
    }

}

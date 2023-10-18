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

package com.wl4g.infra.common.tests.integration;

import com.wl4g.infra.common.task.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getenv;
import static java.time.Duration.ofSeconds;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * The {@link AssertionSpringApplicationIT}
 *
 * @author James Wong
 * @since v3.1
 **/
//@SpringJUnitConfig
//@ContextConfiguration(classes = IntegrationTestApplication.class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public abstract class AssertionSpringApplicationIT extends AnnotationOrderedIT {
    // The assertion operation timeout(seconds)
    public static final int IT_ASSERTION_DEPLAY = parseInt(getenv().getOrDefault("IT_ASSERTION_DEPLAY", "60"));
    public static final int IT_ASSERTION_TIMEOUT = parseInt(getenv().getOrDefault("IT_ASSERTION_TIMEOUT", "300"));

    private final IntegrationTestApplication itApplication;

    public AssertionSpringApplicationIT(IntegrationTestApplication itApplication) {
        this.itApplication = requireNonNull(itApplication, "itApplication");
    }

    @Test
    public void startITApplicationAndAssertionTasks() throws Exception {
        log.info("Do integration test application ...");
        //
        // MANUAL: Startup integration test spring application.
        //
        // Notice: That using @RunWith/@SpringBootTest to start the application cannot control the startup
        // sequence (after the kafka container is started), so it can only be controlled by manual startup.
        //
        final Runnable startedListener = () -> {
            ThreadPoolExecutor executor = null;
            try {
                log.info("Waiting for run assertions task of '{}s' ...", IT_ASSERTION_DEPLAY);
                Thread.sleep(ofSeconds(IT_ASSERTION_DEPLAY).toMillis());

                log.info("Register for assertions tasks ...");
                final int assertionTasksTotal = shouldRegisterAssertionTasksTotal();
                final CountDownLatch assertionLatch = new CountDownLatch(assertionTasksTotal);
                final List<Runnable> assertionTasks = registerAssertionTasks(assertionLatch);
                if (assertionTasks.size() != assertionTasksTotal) {
                    throw new IllegalStateException(String.format("Assertion tasks size must be %s", assertionTasksTotal));
                }

                // Startup IT assertion tasks.
                executor = new ThreadPoolExecutor(assertionTasksTotal, assertionTasksTotal, 0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(1),
                        new NamedThreadFactory("it-assertion-task"),
                        new ThreadPoolExecutor.AbortPolicy());

                final List<Future<?>> futures = assertionTasks.stream().map(executor::submit).collect(toList());

                log.info("Waiting for all assertions task completion ...");
                final long start = currentTimeMillis();
                if (!assertionLatch.await(IT_ASSERTION_TIMEOUT + 10, TimeUnit.SECONDS)) {
                    final long costSec = (currentTimeMillis() - start) / 1000;
                    final String errmsg = String.format("Timeout of all assertions IT task waited for %s seconds.", costSec);
                    log.error(errmsg);
                    throw new TimeoutException(errmsg);
                }

                log.info("Checking for all assertions task result ...");
                for (Future<?> future : futures) {
                    future.get();
                }
                log.info("Assertions to result successfully.");
            } catch (Throwable ex) {
                throw new AssertionError(ex);
            } finally {
                // Shutdown assertion tasks executor.
                if (nonNull(executor)) {
                    log.info("Shutting down assertion executor ...");
                    executor.shutdown();
                    log.info("Assertion executor shutdown completed.");
                }

                // Shutdown IT spring application.
                log.info("Exiting integration test spring application server ...");
                itApplication.exit();
                log.info("Exited integration test spring application server.");
            }
        };

        // Startup IT spring application.
        registerApplicationEnvironment();
        //itApplication.setStartedListener(() -> new Thread(startedListener).start());
        itApplication.setStartedListener(startedListener);

        log.info("Starting integration test spring application ...");
        itApplication.doMain(applicationLaunchArgs());

        log.info("Finished integration test spring application.");
    }

    protected abstract void registerApplicationEnvironment();

    protected abstract String[] applicationLaunchArgs();

    protected abstract int shouldRegisterAssertionTasksTotal();

    private List<Runnable> registerAssertionTasks(CountDownLatch assertionLatch) {
        return doRegisterAssertionTasks(assertionLatch);
    }

    protected abstract List<Runnable> doRegisterAssertionTasks(CountDownLatch assertionLatch);

}

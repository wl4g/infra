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

package com.wl4g.infra.common.tests.integration.manager;

import ch.qos.logback.classic.Level;
import com.google.common.annotations.VisibleForTesting;
import com.wl4g.infra.common.cli.ProcessUtils;
import com.wl4g.infra.common.net.InetUtils;
import com.wl4g.infra.common.reflect.ReflectionUtils2;
import com.wl4g.infra.common.tests.integration.mock.AbstractDataMocker;
import lombok.Getter;
import org.apache.commons.lang3.ClassUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.ResourceReaper;
import org.testcontainers.utility.TestcontainersConfiguration;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getIntProperty;
import static com.wl4g.infra.common.lang.EnvironmentUtil.getStringProperty;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findFieldNullable;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static java.lang.String.format;
import static java.lang.System.*;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

/**
 * The {@link AbstractITContainerManager}
 *
 * @author James Wong
 * @since v3.1
 **/
//@Testcontainers
@SuppressWarnings({"rawtypes", "unchecked", "deprecation", "unused"})
public abstract class AbstractITContainerManager implements Closeable {
    // The assertion operation timeout(seconds)
    public static final int IT_START_CONTAINERS_TIMEOUT = getIntProperty("IT_START_CONTAINERS_TIMEOUT", 600);
    public static final int IT_DATA_MOCKERS_TIMEOUT = getIntProperty("IT_DATA_MOCKERS_TIMEOUT", 300);

    // IT docker daemon properties definitions.
    private static final int DOCKER_DAEMON_PORT = getIntProperty("IT_DOCKER_DAEMON_PORT", 2375);
    private static @Getter String dockerDaemonVmIp;
    private static @Getter String localHostIp;

    // IT runtime properties definitions.
    // see:https://java.testcontainers.org/modules/kafka/#example
    private final Map<String, ITGenericContainer> mwContainers = new ConcurrentHashMap<>();
    private CountDownLatch mwContainersStartedLatch;
    private final Map<String, AbstractDataMocker> dataMockers = new ConcurrentHashMap<>();
    private CountDownLatch dataMockersFinishedLatch;

    //
    // Notice: That using @RunWith/@SpringBootTest to start the application cannot control the startup
    // sequence (e.g after the kafka container is started), so it can only be controlled by manual startup.
    //
    //protected static final Map<String, Supplier<Object>> extraEnvSupplier = synchronizedMap(new HashMap<>());

    // ----- Integration Test Global Initialization Methods. -----

    static {
        ((ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(Logger.ROOT_LOGGER_NAME))
                .setLevel(Level.INFO);

        ((ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger("org.testcontainers"))
                .setLevel(Level.INFO);

        ((ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger("com.github.dockerjava"))
                .setLevel(Level.INFO);

        ((ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger("com.github.dockerjava.api.command.PullImageResultCallback"))
                .setLevel(Level.DEBUG);

        setupITLocalHostIp();
        setupITDockerHost();
        setupRyukContainerIfNeed();
    }

    @VisibleForTesting
    static void setupITLocalHostIp() {
        try (InetUtils helper = new InetUtils(new InetUtils.InetUtilsProperties())) {
            localHostIp = helper.findFirstNonLoopbackHostInfo().getIpAddress();
        }
    }

    /**
     * Set Up to IT docker host. (for compatibility with local multipass VM in docker)
     */
    @VisibleForTesting
    static void setupITDockerHost() {
        String itDockerHost = getenv("IT_DOCKER_HOST");
        if (isBlank(itDockerHost)) {
            // Detect for docker daemon VM IP in multipass(MacOS).
            if (IS_OS_MAC) {
                if (new File("/var/run/docker.sock").exists()) {
                    itDockerHost = "unix:///var/run/docker.sock";
                } else {
                    try {
                        final String dockerDaemonVmIP = ProcessUtils
                                .execSimpleString("[ $(command -v multipass) ] && multipass info docker | grep -i IPv4 | awk '{print $2}' || echo ''");
                        out.printf(">>> [MacOS] Found local multipass(macos) VM for docker IP: %s%n", dockerDaemonVmIP);
                        if (!isBlank(dockerDaemonVmIP)) {
                            itDockerHost = dockerDaemonVmIP;
                        }
                    } catch (Throwable ex) {
                        err.printf(">>> [MacOS] Unable to detect local multipass VM for docker. reason: %s%n", ex.getMessage());
                    }
                }
            }
            // Detect for docker daemon VM IP in multipass(Windows).
            else if (IS_OS_WINDOWS) {
                if (new File("\\\\.\\pipe\\docker_engine").exists()) {
                    itDockerHost = "npipe:////./pipe/docker_engine";
                } else {
                    try {
                        // call to windows multipass command find str info docker
                        final String dockerDaemonVmIP = ProcessUtils
                                .execSimpleString("cmd /c \"(if exist %SystemRoot%\\System32\\multipass.exe (multipass info docker | findstr /i IPv4 | awk \"{print $2}\") else (echo.))\"");
                        out.printf(">>> [Windows] Found local multipass(windows) VM for docker IP: %s%n", dockerDaemonVmIP);
                        if (!isBlank(dockerDaemonVmIP)) {
                            itDockerHost = dockerDaemonVmIP;
                        }
                    } catch (Throwable ex) {
                        err.printf(">>> [Windows] Unable to detect local multipass VM for docker. reason: %s%n", ex.getMessage());
                    }
                }
            }
        }
        if (isNotBlank(itDockerHost)) {
            // CleanUp the line feed.
            Pattern regex = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
            Matcher matcher = regex.matcher(itDockerHost);
            if (matcher.find()) {
                itDockerHost = matcher.group();
            }
            dockerDaemonVmIp = itDockerHost;

            // see:org.testcontainers.utility.TestcontainersConfiguration#getDockerClientStrategyClassName()
            TestcontainersConfiguration.getInstance().updateGlobalConfig("docker.client.strategy",
                    EnvironmentAndSystemPropertyClientProviderStrategy.class.getName());
            TestcontainersConfiguration.getInstance()
                    .updateGlobalConfig("docker.host", format("tcp://%s:%s", itDockerHost, DOCKER_DAEMON_PORT));
        }
    }

    /**
     * {@link org.testcontainers.DockerClientFactory#client()}
     * {@link org.testcontainers.utility.ResourceReaper#instance()}
     * {@link org.testcontainers.utility.ResourceReaper#init()}
     * {@link org.testcontainers.utility.RyukResourceReaper#ryukContainer}
     */
    @VisibleForTesting
    @SuppressWarnings("all")
    static void setupRyukContainerIfNeed() {
        try {
            final ResourceReaper reaper = ResourceReaper.instance();
            final Class<?> reaperResourceCls = ClassUtils.getClass("org.testcontainers.utility.RyukResourceReaper");
            final Class<?> ryukContainerCls = ClassUtils.getClass("org.testcontainers.utility.RyukContainer");
            if (reaperResourceCls.isAssignableFrom(reaper.getClass())) {
                final Field ryukContainerField = findFieldNullable(reaperResourceCls, "ryukContainer", ryukContainerCls);
                if (nonNull(ryukContainerField)) {
                    final GenericContainer ryukContainer = getField(ryukContainerField, reaper, true);
                    final String ryukImageName = getStringProperty("IT_RYUK_CONTAINER_IMAGE",
                            "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/testcontainers_ryuk:0.5.1");
                    ryukContainer.setDockerImageName(ryukImageName);
                }
            }
        } catch (Exception e) {
            err.printf("Unable to setup ryuk container, cause by: %s%n", getRootCauseMessage(e));
        }
    }

    //
    // Notice: That using @RunWith/@SpringBootTest to start the application cannot control the startup
    // sequence (e.g after the kafka container is started), so it can only be controlled by manual startup.
    //
    //@DynamicPropertySource
    //static void registerExtraEnvironment(DynamicPropertyRegistry registry) {
    //    extraEnvSupplier.forEach(registry::add);
    //}

    // ----- Integration Test Assertion Basic Methods. -----

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicInteger closePending;

    public AbstractITContainerManager(@NotNull Class<?> testClass) {
        requireNonNull(testClass, "testClass must not be null");

        // Find all methods total in target test class.
        this.closePending = new AtomicInteger(ReflectionUtils
                .findMethods(testClass, m -> m.isAnnotationPresent(Test.class) || m.isAnnotationPresent(org.junit.Test.class)).size());

        // Check for use is must a static field in test class, so it will be shared by all test cases.
        safeArrayToList(ReflectionUtils2.getDeclaredFields(testClass))
                .stream()
                .filter(f -> AbstractITContainerManager.class.isAssignableFrom(f.getType()))
                .forEach(f -> {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        throw new IllegalStateException(String.format("Field %s must be static, so it will be shared by all test cases.", f));
                    }
                });
    }

    @Override
    public void close() throws IOException {
        close(false);
    }

    @SuppressWarnings("all")
    private void close(boolean force) {
        // If there are still test methods pending, skip closing.
        if (!force && closePending.decrementAndGet() > 0) {
            return;
        }
        if (started.compareAndSet(true, false)) {
            log.info("Shutting down to IT middleware containers ...");
            mwContainers.values().forEach(ITGenericContainer::close);

            log.info("Shutting down to IT data mockers ...");
            dataMockers.values().forEach(dataMocker -> {
                try {
                    dataMocker.close();
                } catch (Throwable ex) {
                    throw new IllegalStateException(String.format("Could not shutting down data mocker %s", dataMocker), ex);
                }
            });

            log.info(">>>>>>>>>> Shutdown for IT containers manager. <<<<<<<<<<");
        }
    }

    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            log.info(">>>>>>>>>> Initializing for IT containers manager ... <<<<<<<<<<");
            startForMwContainers();
            startForDataMocks();
        }
    }

    /**
     * Startup middleware Containers(e.g: zookeeper/kafka/mongodb)
     */
    private void startForMwContainers() throws Exception {
        log.info("Initializing for IT middleware containers ...");

        final Supplier<CountDownLatch> mwContainersStartedLatchSupplier = () -> this.mwContainersStartedLatch;
        initMwContainers(mwContainersStartedLatchSupplier, mwContainers);
        this.mwContainersStartedLatch = new CountDownLatch(mwContainers.size());

        // Run for middleware containers.
        log.info("Starting IT middleware containers ...");
        mwContainers.forEach((name, container) -> new Thread(() -> {
            log.info("Starting for IT middleware container: {}", name);
            container.getContainer().start();
        }).start());

        if (!mwContainersStartedLatch.await(IT_START_CONTAINERS_TIMEOUT, TimeUnit.SECONDS)) {
            throw new TimeoutException("Failed to start IT middleware containers. timeout: " + IT_START_CONTAINERS_TIMEOUT + "s");
        }
        log.info("Started for IT middleware containers: " + mwContainers.keySet());
    }

    /**
     * Startup Data Mockers.
     */
    private void startForDataMocks() throws Exception {
        log.info("Initializing for IT data mockers ...");
        final Supplier<CountDownLatch> dataMockersFinishedLatchSupplier = () -> dataMockersFinishedLatch;
        initDataMockers(dataMockersFinishedLatchSupplier, dataMockers);
        this.dataMockersFinishedLatch = new CountDownLatch(dataMockers.size());

        log.info("Starting for IT data mockers ...");
        this.dataMockers.forEach((name, mocker) -> new Thread(() -> {
            log.info("Starting IT data mocker: {}", name);
            mocker.run();
            mocker.printStatistics();
        }).start());
        if (!dataMockersFinishedLatch.await(IT_DATA_MOCKERS_TIMEOUT, TimeUnit.SECONDS)) {
            throw new TimeoutException("Failed to start IT data mockers. timeout: " + IT_DATA_MOCKERS_TIMEOUT + "s");
        }
    }

    protected abstract void initMwContainers(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                             @NotNull Map<String, ITGenericContainer> mwContainers);

    protected abstract void initDataMockers(@NotNull Supplier<CountDownLatch> finishedLatchSupplier,
                                            @NotNull Map<String, AbstractDataMocker> dataMockers);

    // ------ Getting Running Containers Configuration  ------

    public Map<String, ITGenericContainer> getMwContainers() {
        return unmodifiableMap(mwContainers);
    }

    @SuppressWarnings("unchecked")
    public <T extends ITGenericContainer> T getRequiredContainer(String name) {
        return (T) requireNonNull(mwContainers.get(name), String.format("Could not get first container for %s", name));
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDataMocker> T getRequiredDataMocker(String name) {
        return (T) requireNonNull(dataMockers.get(name), String.format("Could not get data mocker for %s", name));
    }

    @SuppressWarnings("all")
    public String getServersConnectString(String protocol, String clusterName) {
        final ITGenericContainer container = getRequiredContainer(clusterName);
        //
        //final int primaryMappedPort = container.getExposedPorts().stream().findFirst().orElseThrow(() ->
        //        new IllegalStateException("Could not get first mapped port for container server port."));
        final int primaryMappedPort = container.getPrimaryMappedPort();
        return getServersConnectString(protocol, primaryMappedPort);
    }

    public String getServersConnectString(String protocol, int mappedPort) {
        String availableContainerHost = isBlank(dockerDaemonVmIp) ? localHostIp : dockerDaemonVmIp;
        return String.format("%s%s:%s", protocol, availableContainerHost, mappedPort);
    }

    @Getter
    public static class ITGenericContainer implements Closeable {
        private final int primaryMappedPort;
        private final GenericContainer<?> container;

        @SuppressWarnings("all")
        public ITGenericContainer(@Min(1024) int primaryMappedPort,
                                  @NotNull GenericContainer<?> container,
                                  @Nullable ITGenericContainer... dependsOn) {
            Assertions.assertTrue(primaryMappedPort >= 1024, "primaryMappedPort must be greater than or equal to 1024");
            this.primaryMappedPort = primaryMappedPort;
            this.container = requireNonNull(container, "container must not be null");
            withDependsOn(dependsOn);
        }

        @Override
        public void close() {
            container.close();
        }

        public void start() {
            container.start();
        }

        @SuppressWarnings("all")
        public ITGenericContainer withDependsOn(@Nullable ITGenericContainer... dependsOn) {
            this.container.dependsOn(safeArrayToList(dependsOn)
                    .stream()
                    .map(ITGenericContainer::getContainer)
                    .toArray(Startable[]::new));
            return this;
        }
    }

}

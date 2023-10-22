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

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.wl4g.infra.common.cli.ProcessUtils;
import com.wl4g.infra.common.net.InetUtils;
import com.wl4g.infra.common.reflect.ReflectionUtils2;
import com.wl4g.infra.common.tests.integration.mock.AbstractDataMocker;
import com.wl4g.infra.common.tests.integration.testcontainers.RocketMQContainer;
import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.TestcontainersConfiguration;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseToNode;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.*;
import static java.util.Collections.singletonList;
import static java.util.Objects.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

/**
 * The {@link GenericITContainerManager}
 *
 * @author James Wong
 * @since v3.1
 **/
//@Testcontainers
@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public abstract class GenericITContainerManager implements Closeable {
    public static final String KAFKA_UI_01 = "kafka-ui-01";
    // The assertion operation timeout(seconds)
    public static final int IT_START_MW_CONTAINERS_TIMEOUT = parseInt(getenv().getOrDefault("IT_START_HW_CONTAINERS_TIMEOUT", "600"));
    public static final boolean IT_START_MGMT_CONTAINERS_ENABLE = parseBoolean(getenv().getOrDefault("IT_START_MGMT_CONTAINERS_ENABLE", "true"));
    public static final int IT_START_MGMT_CONTAINERS_TIMEOUT = parseInt(getenv().getOrDefault("IT_START_MGMT_CONTAINERS_TIMEOUT", "600"));
    public static final int IT_DATA_MOCKERS_TIMEOUT = parseInt(getenv().getOrDefault("IT_DATA_MOCKERS_TIMEOUT", "300"));

    // IT docker daemon properties definitions.
    private static final int DOCKER_DAEMON_PORT = parseInt(getenv().getOrDefault("IT_DOCKER_DAEMON_PORT", "2375"));
    private static String dockerDaemonVmIp;
    private static String localHostIp;

    // IT runtime properties definitions.
    // see:https://java.testcontainers.org/modules/kafka/#example
    private final Map<String, ITGenericContainerWrapper> mwContainers = new ConcurrentHashMap<>();
    private CountDownLatch mwContainersStartedLatch;
    private final Map<String, ITGenericContainerWrapper> mgmtContainers = new ConcurrentHashMap<>();
    private CountDownLatch mgmtContainersStartedLatch;
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
    }

    private static void setupITLocalHostIp() {
        try (InetUtils helper = new InetUtils(new InetUtils.InetUtilsProperties())) {
            localHostIp = helper.findFirstNonLoopbackHostInfo().getIpAddress();
        }
    }

    /**
     * Set Up to IT docker host. (for compatibility with local multipass VM in docker)
     */
    private static void setupITDockerHost() {
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
                        // call to windows multipass command findstr info docker
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

    //
    // Notice: That using @RunWith/@SpringBootTest to start the application cannot control the startup
    // sequence (e.g after the kafka container is started), so it can only be controlled by manual startup.
    //
    //@DynamicPropertySource
    //static void registerExtraEnvironment(DynamicPropertyRegistry registry) {
    //    extraEnvSupplier.forEach(registry::add);
    //}

    //
    // ----- Integration Test Assertion Basic Methods. -----
    //

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicInteger closePending;

    public GenericITContainerManager(@NotNull Class<?> testClass) {
        requireNonNull(testClass, "testClass must not be null");

        // Find all methods total in target test class.
        this.closePending = new AtomicInteger(ReflectionUtils
                .findMethods(testClass, m -> m.isAnnotationPresent(Test.class) || m.isAnnotationPresent(org.junit.Test.class)).size());

        // Check for use is must a static field in test class, so it will be shared by all test cases.
        safeArrayToList(ReflectionUtils2.getDeclaredFields(testClass))
                .stream()
                .filter(f -> GenericITContainerManager.class.isAssignableFrom(f.getType()))
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

    private void close(boolean force) {
        // If there are still test methods pending, skip closing.
        if (!force && closePending.decrementAndGet() > 0) {
            return;
        }
        if (started.compareAndSet(true, false)) {
            log.info("Shutting down to IT middleware containers ...");
            mwContainers.values().forEach(ITGenericContainerWrapper::close);

            log.info("Shutting down to IT mgmt containers ...");
            mgmtContainers.values().forEach(ITGenericContainerWrapper::close);

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
            startForMiddlewareContainers();
            startForMgmtContainers();
            startForDataMocks();
        }
    }

    private void startForMiddlewareContainers() throws Exception {
        // ---------------- Startup MW Containers(e.g: kafka/mongodb) -------------------

        log.info("Initializing for IT middleware containers ...");
        final Supplier<CountDownLatch> mwContainersStartedLatchSupplier = () -> mwContainersStartedLatch;
        initMiddlewareContainers(mwContainersStartedLatchSupplier, mwContainers);
        mwContainersStartedLatch = new CountDownLatch(mwContainers.size());

        // Run for middleware containers.
        log.info("Starting for IT middleware containers ...");
        mwContainers.forEach((name, c) -> {
            new Thread(() -> {
                log.info("Starting IT middleware container: {}", name);
                c.start();
            }).start();
        });
        if (!mwContainersStartedLatch.await(IT_START_MW_CONTAINERS_TIMEOUT, TimeUnit.SECONDS)) {
            throw new TimeoutException("Failed to start IT middleware containers. timeout: " + IT_START_MW_CONTAINERS_TIMEOUT + "s");
        }
        log.info("Started for IT middleware containers: " + mwContainers.keySet());
    }

    private void startForMgmtContainers() throws Exception {
        // ---------------- Startup MGMT Containers(e.g: kafka-ui/mongodb-express) -----------------

        if (mwContainers.values().stream().noneMatch(c ->
                c.getContainer().getDockerImageName().toUpperCase().contains("KAFKA"))) {
            log.warn("Skip start for mgmt containers because not found IT kafka containers.");
            return;
        }

        // Try to wait until the middleware container is ready to start.
        Thread.sleep(5000L);

        log.info("Initializing for IT mgmt containers ...");
        final Supplier<CountDownLatch> mgmtContainersStartedLatchSupplier = () -> mgmtContainersStartedLatch;
        initManagementContainers(mgmtContainersStartedLatchSupplier, mgmtContainers);
        mgmtContainersStartedLatch = new CountDownLatch(mgmtContainers.size());

        // Run for mgmt containers.
        log.info("Starting for IT mgmt containers ...");
        mgmtContainers.forEach((name, c) -> {
            new Thread(() -> {
                log.info("Starting IT mgmt container: {}", name);
                c.start();
            }).start();
        });
        if (!mgmtContainersStartedLatch.await(IT_START_MGMT_CONTAINERS_TIMEOUT, TimeUnit.SECONDS)) {
            throw new TimeoutException("Failed to start IT mgmt containers. timeout: " + IT_START_MGMT_CONTAINERS_TIMEOUT + "s");
        }
        log.info("Started for IT mgmt containers: " + mgmtContainers.keySet());
    }

    private void startForDataMocks() throws Exception {
        // ---------------- Startup Data Mockers --------------------

        // Run for data mockers.
        log.info("Initializing for IT data mockers ...");
        final Supplier<CountDownLatch> dataMockersFinishedLatchSupplier = () -> dataMockersFinishedLatch;
        initDataMockers(dataMockersFinishedLatchSupplier, dataMockers);
        dataMockersFinishedLatch = new CountDownLatch(dataMockers.size());

        log.info("Starting for IT data mockers ...");
        dataMockers.forEach((name, mocker) -> {
            new Thread(() -> {
                log.info("Starting IT data mocker: {}", name);
                mocker.run();
                mocker.printStatistics();
            }).start();
        });
        if (!dataMockersFinishedLatch.await(IT_DATA_MOCKERS_TIMEOUT, TimeUnit.SECONDS)) {
            throw new TimeoutException("Failed to start IT data mockers. timeout: " + IT_DATA_MOCKERS_TIMEOUT + "s");
        }
    }

    protected abstract void initMiddlewareContainers(@NotNull Supplier<CountDownLatch> startedLatch,
                                                     @NotNull Map<String, ITGenericContainerWrapper> mwContainers);

    protected void initManagementContainers(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                            @NotNull Map<String, ITGenericContainerWrapper> mgmtContainers) {
        if (IT_START_MGMT_CONTAINERS_ENABLE) {
            final List<String> kafkaClusters = mwContainers
                    .entrySet()
                    .stream()
                    .filter(e -> StringUtils.contains(e.getValue().getContainer().getDockerImageName(), "kafka"))
                    .map(Map.Entry::getKey)
                    .map(this::getKafkaClusterServers)
                    .collect(toList());
            if (!kafkaClusters.isEmpty()) {
                mgmtContainers.put(KAFKA_UI_01, buildProvectuslabsKafkaUIContainer(startedLatchSupplier, kafkaClusters));
            }
        } else {
            log.warn("Skip init for mgmt containers because disabled.");
        }
    }

    protected abstract void initDataMockers(@NotNull Supplier<CountDownLatch> finishedLatchSupplier,
                                            @NotNull Map<String, AbstractDataMocker> dataMockers);

    //
    // --------------------- Getting Run Containers Configuration  -----------------------
    //

    @SuppressWarnings("unchecked")
    public <T extends ITGenericContainerWrapper> T getRequiredContainer(String name) {
        return (T) requireNonNull(mwContainers.get(name), String.format("Could not get middleware container for %s", name));
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDataMocker> T getRequiredDataMocker(String name) {
        return (T) requireNonNull(dataMockers.get(name), String.format("Could not get data mocker for %s", name));
    }

    public String getKafkaClusterServers(String clusterName) {
        return getServersConnectionString("PLAINTEXT://", clusterName);
    }

    public String getRocketMQClusterServers(String clusterName) {
        // return ((RocketMQContainer) getRequiredContainer(clusterName)).getNamesrvAddr();
        return getServersConnectionString("", clusterName);
    }

    public String getServersConnectionString(String protocol, String clusterName) {
        final ITGenericContainerWrapper container = getRequiredContainer(clusterName);
        //final int primaryMappedPort = container.getExposedPorts().stream().findFirst().orElseThrow(() ->
        //        new IllegalStateException("Could not get first mapped port for container server port."));
        final int primaryMappedPort = container.getPrimaryMappedPort();
        return getServersConnectionString(protocol, primaryMappedPort);
    }

    public String getServersConnectionString(String protocol, int mappedPort) {
        String availableContainerHost = isBlank(dockerDaemonVmIp) ? localHostIp : dockerDaemonVmIp;
        return String.format("%s%s:%s", protocol, availableContainerHost, mappedPort);
    }

    //
    // --------------------- Build MW Containers  -----------------------
    //

    public ITGenericContainerWrapper buildBitnamiKafka35Container(@NotNull Supplier<CountDownLatch> startedLatch,
                                                                  @Min(1024) int mappedPort,
                                                                  @Min(1024) int containerPort,
                                                                  @Null Map<String, String> overrideEnv) {
        // Generate controller port with retry.
        int controllerPort;
        do {
            controllerPort = RandomUtils.nextInt(55535, 65535);
        } while (controllerPort == mappedPort || controllerPort == containerPort);

        final String serverListen = getServersConnectionString("PLAINTEXT://", mappedPort);

        final Map<String, String> env = new HashMap<>();
        env.put("ALLOW_PLAINTEXT_LISTENER", "yes");
        // see:https://github.com/bitnami/containers/blob/main/bitnami/kafka/3.5/debian-11/docker-compose.yml
        // KRaft settings
        env.put("KAFKA_CFG_NODE_ID", "0");
        env.put("KAFKA_CFG_PROCESS_ROLES", "controller,broker");
        env.put("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", format("0@localhost:%s", controllerPort));
        //env.put("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", format("0@%s:%s", localHostIp, controllerPort));
        // Listeners
        env.put("KAFKA_CFG_LISTENERS", format("PLAINTEXT://:%s,CONTROLLER://:%s", containerPort, controllerPort));
        //env.put("KAFKA_CFG_LISTENERS", serverListen + "," + format("CONTROLLER://%s:%s", localHostIp, controllerPort));
        env.put("KAFKA_CFG_ADVERTISED_LISTENERS", serverListen);
        env.put("KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT");
        env.put("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER");
        env.put("KAFKA_CFG_INTER_BROKER_LISTENER_NAME", "PLAINTEXT");
        // Other
        // see:https://github.com/bitnami/containers/blob/main/bitnami/kafka/3.5/debian-11/rootfs/opt/bitnami/scripts/libkafka.sh#L937
        env.put("KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE", "true");

        // Merge of override env.
        if (nonNull(overrideEnv)) {
            env.putAll(overrideEnv);
        }

        return buildBitnamiKafkaContainer(startedLatch, "3.5", mappedPort, containerPort, null, env);
    }

    /**
     * Manual tests for examples:
     *
     * <pre>
     *  export imageName='registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_kafka:3.5'
     *  export kafkaPort='19092'
     *  export kafkaTopic='my-test'
     *  export localHostIp=$(ip a | grep -E '^[a-zA-Z0-9]+: (em|eno|enp|ens|eth|wlp|en)+[0-9]' -A2 | grep inet | awk -F ' ' '{print $2}' | cut -f 1 -d / | tail -n 1)
     *  export localHostIp=$([ -z "${localHostIp}" ] && echo $([ $(command -v multipass) ] && multipass info docker | grep -i IPv4 | awk '{print $2}') || echo ${localHostIp})
     *
     *  docker run --rm --name kafka_test --network host --entrypoint /bin/bash ${imageName} -c \
     *  "echo 'key1:value1' | /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server ${localHostIp}:${kafkaPort} --create --topic ${kafkaTopic}"
     *
     *  docker run --rm --name kafka_test --network host --entrypoint /bin/bash ${imageName} -c \
     *  "echo 'key1:value1' | /opt/bitnami/kafka/bin/kafka-topics.sh --bootstrap-server ${localHostIp}:${kafkaPort} --list"
     *
     *  docker run --rm --name kafka_test --network host --entrypoint /bin/bash ${imageName} -c \
     *  "echo 'key1:value1-of-${kafkaPort}' | /opt/bitnami/kafka/bin/kafka-console-producer.sh --bootstrap-server ${localHostIp}:${kafkaPort} \
     *  --topic ${kafkaTopic} --property parse.key=true --property key.separator=:"
     *
     *  docker run --rm --name kafka_test --network host --entrypoint /bin/bash ${imageName} -c \
     *  "/opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server ${localHostIp}:${kafkaPort} --topic ${kafkaTopic} --from-beginning"
     * </pre>
     */
    public ITGenericContainerWrapper buildBitnamiKafkaContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                @NotBlank String kafkaVersion,
                                                                @Min(1024) int mappedPort,
                                                                @Min(1024) int containerPort,
                                                                @Null Duration startupTimeout,
                                                                @NotNull Map<String, String> env) {
        Assertions.assertNotNull(startedLatchSupplier, "startedLatchSupplier must not be null");
        Assertions.assertTrue(isNotBlank(kafkaVersion), "kafkaVersion must be like 2.8.1");
        Assertions.assertTrue(mappedPort > 1024, "mappedPort must be greater than 1024");
        Assertions.assertTrue(containerPort > 1024, "containerPort must be greater than 1024");
        Assertions.assertNotNull(env, "env must not be null");

        //final GenericContainer<?> kafka01 = new KafkaContainer(DockerImageName.parse("bitnami/kafka:2.8.1")
        //  .asCompatibleSubstituteFor("confluentinc/cp-kafka"))
        //  .withEmbeddedZookeeper();

        // see:https://java.testcontainers.org/modules/kafka/#example
        //new KafkaContainer().withEmbeddedZookeeper();

        @SuppressWarnings("rawtypes") final GenericContainer<?> kafkaContainer = new GenericContainer("registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_kafka:".concat(kafkaVersion)) {
            @Override
            protected void containerIsStarted(InspectContainerResponse containerInfo) {
                log.info("Kafka container is started: " + containerInfo.getId());
                startedLatchSupplier.get().countDown();
            }
        };

        startupTimeout = isNull(startupTimeout) ? Duration.ofSeconds(IT_START_MW_CONTAINERS_TIMEOUT) : startupTimeout;
        kafkaContainer
                //.withNetworkMode("host")
                .withEnv("ALLOW_PLAINTEXT_LISTENER", "yes")
                // see:https://github.com/bitnami/containers/blob/main/bitnami/kafka/3.5/debian-11/rootfs/opt/bitnami/scripts/libkafka.sh#L937
                .withEnv(env)
                .withReuse(false)
                .withExposedPorts(mappedPort)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.kafka")))
                //.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd ->
                //     cmd.withHostConfig(new HostConfig()
                //             .withPortBindings(new PortBinding(Ports.Binding.bindPort(containerPort),
                //                     new ExposedPort(mappedPort)))
                //     ))
                //.withCommand("run", "-e", "{\"kafka_advertised_hostname\":" + kafkaAdvertisedHostname + "}")
                .waitingFor(Wait.forLogMessage("(.*)Kafka Server started (.*)", 1)
                        .withStartupTimeout(startupTimeout));

        kafkaContainer.setPortBindings(singletonList(mappedPort + ":" + containerPort));
        return new ITGenericContainerWrapper(mappedPort, kafkaContainer);
    }

    public ITGenericContainerWrapper buildApacheRocketMQContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                  @Min(1024) int mappedPort,
                                                                  @Min(1024) int containerPort) {
        return buildApacheRocketMQContainer(startedLatchSupplier, "4.9.7", mappedPort, containerPort);
    }

    public ITGenericContainerWrapper buildApacheRocketMQContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                  @NotBlank String rocketMQVersion,
                                                                  @Min(1024) int mappedPort,
                                                                  @Min(1024) int containerPort) {
        Assertions.assertTrue(isNotBlank(rocketMQVersion), "rocketMQVersion must be like 4.9.1");

        final RocketMQContainer rocketmqContainer = new RocketMQContainer(DockerImageName
                .parse("registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/apache_rocketmq:".concat(rocketMQVersion))
                .asCompatibleSubstituteFor("apache/rocketmq")) {
            @Override
            protected void containerIsStarted(InspectContainerResponse containerInfo) {
                log.info("RocketMQ container is started: " + containerInfo.getId());
                startedLatchSupplier.get().countDown();
                super.containerIsStarted(containerInfo);
            }
        };

        rocketmqContainer.setPortBindings(singletonList(String.format("%s:%s", mappedPort, containerPort)));
        return new ITGenericContainerWrapper(mappedPort, rocketmqContainer);
    }

    //
    // --------------------- Build MGMT Containers  -----------------------
    //


    public ITGenericContainerWrapper buildProvectuslabsKafkaUIContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                        @NotNull List<String> kafkaClusters) {
        return buildProvectuslabsKafkaUIContainer(startedLatchSupplier, "v0.7.1", 58888, 8080, null, kafkaClusters);
    }

    public ITGenericContainerWrapper buildProvectuslabsKafkaUIContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                        @NotBlank String kafkaUiVersion,
                                                                        @Min(1024) int mappedPort,
                                                                        @Min(1024) int containerPort,
                                                                        @Null Duration startupTimeout,
                                                                        @NotNull List<String> kafkaClusters) {
        Assertions.assertNotNull(startedLatchSupplier, "startedLatchSupplier must not be null");
        Assertions.assertTrue(isNotBlank(kafkaUiVersion), "kafkaUiVersion must be like 2.8.1");
        Assertions.assertTrue(mappedPort > 1024, "mappedPort must be greater than 1024");
        Assertions.assertTrue(containerPort > 1024, "containerPort must be greater than 1024");

        final GenericContainer<?> kafkaUiContainer = new GenericContainer("registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/provectuslabs_kafka-ui:".concat(kafkaUiVersion)) {
            @Override
            protected void containerIsStarted(InspectContainerResponse containerInfo) {
                log.info("Kafka ui container is started: " + containerInfo.getId());
                startedLatchSupplier.get().countDown();
            }
        };

        startupTimeout = isNull(startupTimeout) ? Duration.ofSeconds(IT_START_MGMT_CONTAINERS_TIMEOUT) : startupTimeout;
        kafkaUiContainer
                .withEnv("JAVA_OPTS", "-Djava.net.preferIPv4Stack=true -Xmx1G")
                .withReuse(false)
                .withExposedPorts(mappedPort)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.kafka")))
                .waitingFor(Wait.forLogMessage("(.*)Started KafkaUiApplication (.*)", 1)
                        .withStartupTimeout(startupTimeout));

        for (int i = 0; i < kafkaClusters.size(); i++) {
            final String kafkaServers = kafkaClusters.get(i);
            kafkaUiContainer.withEnv("KAFKA_CLUSTERS_" + i + "_NAME", "it-cluster-" + i);
            kafkaUiContainer.withEnv("KAFKA_CLUSTERS_" + i + "_BOOTSTRAPSERVERS", kafkaServers);
        }

        kafkaUiContainer.setPortBindings(singletonList(mappedPort + ":" + containerPort));
        return new ITGenericContainerWrapper(mappedPort, kafkaUiContainer);
    }

    //
    // --------------------- MQ consuming Assertion Runners  --------------
    //

    public Runnable buildKafkaConsumingAssertionRunner(CountDownLatch latch,
                                                       String kafkaServers,
                                                       String topic,
                                                       String groupId,
                                                       int timeoutSeconds,
                                                       Function<JsonNode, Boolean> collector,
                                                       int exitOfCount,
                                                       Consumer<Map<String, JsonNode>> assertion) {
        return () -> {
            final Map<String, JsonNode> result = new ConcurrentHashMap<>();
            final Properties props = new Properties();
            //props.put(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG, BasedDataMocker.MockCustomHostResolver.class.getName());
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props)) {
                consumer.subscribe(singletonList(topic));
                final long start = currentTimeMillis();
                while (currentTimeMillis() - start < (timeoutSeconds * 1000L)) {
                    final ConsumerRecords<String, Object> records = consumer.poll(200);
                    if (records.isEmpty()) {
                        continue;
                    }
                    log.info("Received sink records(Kafka): " + records.count());
                    for (ConsumerRecord<String, Object> record : records) {
                        if (nonNull(record.value())) {
                            if (record.value() instanceof String) {
                                final JsonNode node = parseToNode((String) record.value());
                                if (collector.apply(node)) {
                                    result.put(record.key(), node);
                                }
                            }
                        }
                    }
                    if (result.size() >= exitOfCount) {
                        latch.countDown();
                        log.info("Assertion for Kafka consumer is valid. Total: " + result.size());
                        break;
                    }
                }
                assertion.accept(result);
            } catch (Throwable ex) {
                latch.countDown();
                String errmsg = String.format("Failed to assertion for consuming kafka on %s, reason: %s",
                        kafkaServers, ExceptionUtils.getStackTrace(ex));
                err.println(errmsg);
                throw new IllegalStateException(errmsg);
            }
        };
    }

    public Runnable buildRocketMQConsumingAssertionRunner(CountDownLatch latch,
                                                          String namesrvAddr,
                                                          String topic,
                                                          String groupId,
                                                          int timeoutSeconds,
                                                          Function<JsonNode, Boolean> collector,
                                                          int exitOfCount,
                                                          Consumer<Map<String, JsonNode>> assertion) {
        return () -> {
            final Map<String, JsonNode> result = new ConcurrentHashMap<>();
            final DefaultLitePullConsumer consumer = new DefaultLitePullConsumer(groupId);
            consumer.setNamesrvAddr(namesrvAddr);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            try {
                consumer.subscribe(topic, "*");
                consumer.start();
                final long start = currentTimeMillis();
                while (currentTimeMillis() - start < (timeoutSeconds * 1000L)) {
                    final List<MessageExt> records = consumer.poll(200);
                    if (records.isEmpty()) {
                        continue;
                    }
                    log.info("Received sink records(RocketMQ): " + records.size());
                    for (MessageExt record : records) {
                        if (nonNull(record.getBody())) {
                            final JsonNode node = parseToNode(new String(record.getBody(), StandardCharsets.UTF_8));
                            if (collector.apply(node)) {
                                result.put(record.getMsgId(), node);
                            }
                        }
                    }
                    if (result.size() >= exitOfCount) {
                        latch.countDown();
                        log.info("Assertion for RocketMQ consumer is valid. Total: " + result.size());
                        break;
                    }
                }
                assertion.accept(result);
            } catch (Throwable ex) {
                latch.countDown();
                String errmsg = String.format("Failed to assertion for consuming RocketMQ on %s", namesrvAddr);
                err.println(errmsg);
                throw new IllegalStateException(errmsg, ex);
            } finally {
                consumer.shutdown();
            }
        };
    }

    @Getter
    public static class ITGenericContainerWrapper implements Closeable {
        private final int primaryMappedPort;
        private final GenericContainer<?> container;

        public ITGenericContainerWrapper(int primaryMappedPort,
                                         GenericContainer<?> container) {
            this.primaryMappedPort = primaryMappedPort;
            this.container = container;
        }

        @Override
        public void close() {
            container.close();
        }

        public void start() {
            container.start();
        }
    }

}

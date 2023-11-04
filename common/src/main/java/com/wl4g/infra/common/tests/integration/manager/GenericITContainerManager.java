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

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.wl4g.infra.common.lang.Assert2;
import com.wl4g.infra.common.tests.integration.testcontainers.RocketMQContainer;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.testcontainers.shaded.org.apache.commons.lang3.StringUtils.isBlank;

/**
 * The {@link GenericITContainerManager}
 *
 * @author James Wong
 * @since v3.1
 **/
//@Testcontainers
@SuppressWarnings({"rawtypes", "unused"})
public abstract class GenericITContainerManager extends AbstractITContainerManager {

    public GenericITContainerManager(@NotNull Class<?> testClass) {
        super(testClass);
    }

    // --------------------- Getting Run Containers Configuration  -----------------------

    public String getKafkaClusterServers(String clusterName) {
        return getServersConnectString("PLAINTEXT://", clusterName, 0);
    }

    public String getRocketMQClusterServers(String clusterName) {
        // return ((RocketMQContainer) getRequiredContainer(clusterName)).getNamesrvAddr();
        return getServersConnectString("", clusterName, 0);
    }


    // --------------------- ZOOKEEPER build container ---------------------

    public ITGenericContainer buildBitnamiZookeeper34xContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                @Min(1024) int serverPort,
                                                                @Nullable Map<String, String> env) {
        return buildBitnamiZookeeperContainer(startedLatchSupplier, "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_zookeeper",
                "3.4.13", serverPort, "(.*)binding to port (.*)", null, env, null, null);
    }

    public ITGenericContainer buildBitnamiZookeeper35xContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                @Min(1024) int serverPort,
                                                                @Nullable Map<String, String> env) {
        return buildBitnamiZookeeperContainer(startedLatchSupplier, "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_zookeeper",
                "3.5.9", serverPort, "(.*)binding to port (.*)", null, env, null, null);
    }

    public ITGenericContainer buildBitnamiZookeeper36xContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                @Min(1024) int serverPort,
                                                                @Nullable Map<String, String> env) {
        return buildBitnamiZookeeperContainer(startedLatchSupplier,
                "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_zookeeper",
                "3.6.2", serverPort, "(.*)binding to port (.*)", null, env, null, null);
    }

    public ITGenericContainer buildBitnamiZookeeperContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                             @NotBlank String imageRepo,
                                                             @NotBlank String imageTag,
                                                             @Min(1024) int serverPort,
                                                             @NotBlank String startedLogRegex,
                                                             @Nullable Duration startupTimeout,
                                                             @Nullable Map<String, String> env,
                                                             @Nullable Consumer<String> createdListener,
                                                             @Nullable Consumer<InspectContainerResponse> startedListener) {
        Assertions.assertTrue(serverPort > 1024, "serverPort must be greater than 1024");

        final Map<String, String> mergeEnv = new HashMap<>(safeMap(env));
        //mergeEnv.putIfAbsent("ZOO_ENABLE_AUTH", "true");
        mergeEnv.putIfAbsent("ALLOW_ANONYMOUS_LOGIN", "yes");
        mergeEnv.putIfAbsent("ZOO_PORT_NUMBER", String.valueOf(serverPort));
        return buildBitnamiContainer(startedLatchSupplier, imageRepo, imageTag, asList(serverPort, serverPort),
                "zookeeper", startedLogRegex, null, startupTimeout, null,
                mergeEnv, createdListener, startedListener);
    }

    // --------------------- KAFKA build container ---------------------------

    public ITGenericContainer buildBitnamiKafka22xContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                            @Min(1024) int serverPort,
                                                            @Min(1024) int jmxPort,
                                                            @Nullable Map<String, String> env,
                                                            @NotBlank String zookeeperServers) {
        Assertions.assertTrue(serverPort > 1024, "serverPort must be greater than 1024");
        Assertions.assertTrue(jmxPort > 1024, "jmxPort must be greater than 1024");
        Assertions.assertTrue(isNotBlank(zookeeperServers), "zookeeperServers must not be blank");

        final String serverListen = getServersConnectString("PLAINTEXT://", serverPort);
        final Map<String, String> mergeEnv = new HashMap<>(safeMap(env));
        mergeEnv.putIfAbsent("ALLOW_PLAINTEXT_LISTENER", "yes");
        mergeEnv.putIfAbsent("JMX_PORT", valueOf(jmxPort));
        mergeEnv.putIfAbsent("KAFKA_ADVERTISED_LISTENERS", serverListen);
        mergeEnv.putIfAbsent("KAFKA_LISTENERS", String.format("PLAINTEXT://0.0.0.0:%s", serverPort));
        mergeEnv.putIfAbsent("KAFKA_ZOOKEEPER_CONNECT", zookeeperServers);

        // Merge of override mergeEnv.
        if (nonNull(env)) {
            mergeEnv.putAll(env);
        }

        return buildBitnamiKafkaContainer(startedLatchSupplier, "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_kafka",
                "2.2.0", asList(serverPort, serverPort, jmxPort, jmxPort), "(.*)started \\(kafka\\.server\\.KafkaServer\\)(.*)",
                null, mergeEnv, null, null);
    }

    public ITGenericContainer buildBitnamiKafka35xContainer(@NotNull Supplier<CountDownLatch> startedLatch,
                                                            @Min(1024) int serverPort,
                                                            @Min(1024) int jmxPort,
                                                            @Nullable Map<String, String> env) {
        Assertions.assertTrue(serverPort > 1024, "serverPort must be greater than 1024");
        Assertions.assertTrue(jmxPort > 1024, "jmxPort must be greater than 1024");

        // Generate controller port with retry.
        int controllerPort;
        do {
            controllerPort = RandomUtils.nextInt(55535, 65535);
        } while (controllerPort == serverPort);

        final String serverListen = getServersConnectString("PLAINTEXT://", serverPort);

        final Map<String, String> mergeEnv = new HashMap<>();
        mergeEnv.putIfAbsent("ALLOW_PLAINTEXT_LISTENER", "yes");
        mergeEnv.putIfAbsent("JMX_PORT", valueOf(jmxPort));
        // see:https://github.com/bitnami/containers/blob/main/bitnami/kafka/3.5/debian-11/docker-compose.yml
        // KRaft settings
        mergeEnv.putIfAbsent("KAFKA_CFG_NODE_ID", "0");
        mergeEnv.putIfAbsent("KAFKA_CFG_PROCESS_ROLES", "controller,broker");
        mergeEnv.putIfAbsent("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", format("0@localhost:%s", controllerPort));
        //mergeEnv.put("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", format("0@%s:%s", localHostIp, controllerPort));
        // Listeners
        mergeEnv.putIfAbsent("KAFKA_CFG_LISTENERS", format("PLAINTEXT://:%s,CONTROLLER://:%s", serverPort, controllerPort));
        //mergeEnv.put("KAFKA_CFG_LISTENERS", serverListen + "," + format("CONTROLLER://%s:%s", localHostIp, controllerPort));
        mergeEnv.putIfAbsent("KAFKA_CFG_ADVERTISED_LISTENERS", serverListen);
        mergeEnv.putIfAbsent("KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP", "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT");
        mergeEnv.putIfAbsent("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER");
        mergeEnv.putIfAbsent("KAFKA_CFG_INTER_BROKER_LISTENER_NAME", "PLAINTEXT");
        // Other
        // see:https://github.com/bitnami/containers/blob/main/bitnami/kafka/3.5/debian-11/rootfs/opt/bitnami/scripts/libkafka.sh#L937
        mergeEnv.putIfAbsent("KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE", "true");

        // Merge of override mergeEnv.
        if (nonNull(env)) {
            mergeEnv.putAll(env);
        }

        return buildBitnamiKafkaContainer(startedLatch, "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_kafka",
                "3.5", asList(serverPort, serverPort, jmxPort, jmxPort), "(.*)Kafka Server started (.*)",
                null, mergeEnv, null, null);
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
    @SuppressWarnings("all")
    public ITGenericContainer buildBitnamiKafkaContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                         @NotBlank String imageRepo,
                                                         @NotBlank String imageTag,
                                                         @NotEmpty List<Integer> portBindings,
                                                         @NotBlank String startedLogRegex,
                                                         @Nullable Duration startupTimeout,
                                                         @NotNull Map<String, String> env,
                                                         @Nullable Consumer<String> createdListener,
                                                         @Nullable Consumer<InspectContainerResponse> startedListener) {
        //final GenericContainer<?> kafka01 = new KafkaContainer(DockerImageName.parse("bitnami/kafka:2.8.1")
        //  .asCompatibleSubstituteFor("confluentinc/cp-kafka"))
        //  .withEmbeddedZookeeper();

        // see:https://java.testcontainers.org/modules/kafka/#example
        //new KafkaContainer().withEmbeddedZookeeper();

        //startupTimeout = isNull(startupTimeout) ? Duration.ofSeconds(IT_START_MW_CONTAINERS_TIMEOUT) : startupTimeout;
        //kafkaContainer
        //        //.withNetworkMode("host")
        //        .withEnv("ALLOW_PLAINTEXT_LISTENER", "yes")
        //        // see:https://github.com/bitnami/containers/blob/main/bitnami/kafka/3.5/debian-11/rootfs/opt/bitnami/scripts/libkafka.sh#L937
        //        .withEnv(env)
        //        .withReuse(false)
        //        .withExposedPorts(serverPort)
        //        .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.kafka")))
        //        //.withCreateContainerCmdModifier((Consumer<CreateContainerCmd>) cmd ->
        //        //     cmd.withHostConfig(new HostConfig()
        //        //             .withPortBindings(new PortBinding(Ports.Binding.bindPort(containerPort),
        //        //                     new ExposedPort(serverPort)))
        //        //     ))
        //        //.withCommand("run", "-e", "{\"kafka_advertised_hostname\":" + kafkaAdvertisedHostname + "}")
        //        .waitingFor(Wait.forLogMessage("(.*)Kafka Server started (.*)", 1)
        //                .withStartupTimeout(startupTimeout));
        //kafkaContainer.setPortBindings(singletonList(serverPort + ":" + containerPort));

        return buildBitnamiContainer(startedLatchSupplier, imageRepo, imageTag, portBindings,
                "kafka", startedLogRegex, null, startupTimeout, emptyMap(),
                env, createdListener, startedListener);
    }

    // --------------------- RocketMQ build container ------------------------

    public ITGenericContainer buildApacheRocketMQ49xContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                              @Min(1024) int serverPort) {
        return buildApacheRocketMQContainer(startedLatchSupplier,
                "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/apache_rocketmq",
                "4.9.7", serverPort, null, null);
    }

    public ITGenericContainer buildApacheRocketMQContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                           @NotBlank String imageRepo,
                                                           @NotBlank String imageTag,
                                                           @Min(1024) int serverPort,
                                                           @Nullable Consumer<String> createdListener,
                                                           @Nullable Consumer<InspectContainerResponse> startedListener) {
        Assertions.assertTrue(serverPort > 1024, "serverPort must be greater than 1024");

        final DockerImageName image = DockerImageName.parse(imageRepo.concat(":").concat(imageTag))
                .asCompatibleSubstituteFor("apache/rocketmq");
        final RocketMQContainer container = new RocketMQContainer(image) {
            @Override
            protected void containerIsCreated(String containerId) {
                log.info("RocketMQ container is created: " + containerId);
                super.containerIsCreated(containerId);

                if (nonNull(createdListener)) {
                    createdListener.accept(containerId);
                }
            }

            @Override
            protected void containerIsStarted(InspectContainerResponse containerInfo) {
                log.info("RocketMQ container is started: " + containerInfo.getId());
                startedLatchSupplier.get().countDown();
                super.containerIsStarted(containerInfo);

                if (nonNull(startedListener)) {
                    startedListener.accept(containerInfo);
                }
            }
        };
        container.setPortBindings(singletonList(String.format("%s:%s", serverPort, serverPort)));
        return new ITGenericContainer(singletonList(serverPort + ":" + serverPort), container);
    }

    // --------------------- Kafka UI build Containers  ----------------------

    public ITGenericContainer buildProvectuslabsKafkaUI07xContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                    @NotNull List<String> kafkaClusters,
                                                                    @NotEmpty List<Integer> kafkaMetricsPorts) {
        return buildProvectuslabsKafkaUIContainer(startedLatchSupplier, "v0.7.1", 58888,
                null, null, kafkaClusters, kafkaMetricsPorts, null, null);
    }

    @SuppressWarnings("all")
    public ITGenericContainer buildProvectuslabsKafkaUIContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                                 @NotBlank String imageTag,
                                                                 @Min(1024) int serverPort,
                                                                 @NotNull Map<String, String> env,
                                                                 @Nullable String auditLogTopic,
                                                                 @NotNull List<String> kafkaClusters,
                                                                 @NotEmpty List<Integer> kafkaMetricsPorts,
                                                                 @Nullable Consumer<String> createdListener,
                                                                 @Nullable Consumer<InspectContainerResponse> startedListener) {
        Assertions.assertNotNull(startedLatchSupplier, "startedLatchSupplier must not be null");
        Assertions.assertTrue(serverPort > 1024, "serverPort must be greater than 1024");
        Assertions.assertTrue(nonNull(kafkaClusters) && !kafkaClusters.isEmpty(), "kafkaClusters must not be empty");
        long kafkaClusterCount = kafkaClusters.stream().filter(d -> !isBlank(d)).count();
        long kafkaMetricsPortsCount = safeList(kafkaMetricsPorts).stream().filter(Objects::nonNull).count();
        Assertions.assertEquals(kafkaMetricsPortsCount, kafkaClusterCount, format("kafkaMetricsPorts size(%s) must be equal to kafkaClusters size(%s)",
                kafkaMetricsPortsCount, kafkaClusterCount));

        final Map<String, String> mergeEnv = new HashMap<>(safeMap(env));
        mergeEnv.putIfAbsent("JAVA_OPTS", "-Djava.net.preferIPv4Stack=true -Xmx1G");
        mergeEnv.putIfAbsent("SERVER_PORT", valueOf(serverPort));
        for (int i = 0; i < kafkaClusters.size(); i++) {
            final String kafkaServers = kafkaClusters.get(i);
            mergeEnv.putIfAbsent("KAFKA_CLUSTERS_" + i + "_NAME", "it-cluster-" + i);
            mergeEnv.putIfAbsent("KAFKA_CLUSTERS_" + i + "_BOOTSTRAPSERVERS", kafkaServers);
            mergeEnv.putIfAbsent("KAFKA_CLUSTERS_" + i + "_METRICS_PORT", valueOf(kafkaMetricsPorts.get(i)));
            mergeEnv.putIfAbsent("KAFKA_CLUSTERS_" + i + "_AUDIT_TOPIC_AUDIT_ENABLED", "true");
            mergeEnv.putIfAbsent("KAFKA_CLUSTERS_" + i + "_AUDIT_TOPIC", isBlank(auditLogTopic) ? "__kui-audit-log" : auditLogTopic);
            mergeEnv.putIfAbsent("KAFKA_CLUSTERS_" + i + "_AUDIT_TOPIC_PROPERTIES_RETENTION_MS", "43200000");
            mergeEnv.putIfAbsent("KAFKA_CLUSTERS_" + i + "_AUDIT_TOPICS_PARTITIONS", "3");
            mergeEnv.putIfAbsent("KAFKA_CLUSTERS_" + i + "_AUDIT_LEVEL", "ALTER_ONLY");
        }

        return buildBitnamiContainer(startedLatchSupplier, "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/provectuslabs_kafka-ui",
                imageTag, asList(serverPort, serverPort), "kafka-ui", "(.*)Started KafkaUiApplication (.*)",
                null, null, emptyMap(), mergeEnv, createdListener, startedListener);
    }

    // --------------------- Prometheus build Containers  --------------------

    public ITGenericContainer buildBitnamiPrometheus24xContainer(Supplier<CountDownLatch> startedLatchSupplier,
                                                                 @Min(1024) int serverPort,
                                                                 @Min(1) int scrapeIntervalSeconds,
                                                                 List<String> scrapeUrls,
                                                                 @Nullable Map<String, String> env) {
        return buildBitnamiPrometheusContainer(startedLatchSupplier, "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_prometheus",
                "2.47.2", serverPort, scrapeIntervalSeconds, scrapeUrls, env, null, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "unused", "all"})
    public ITGenericContainer buildBitnamiPrometheusContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                              @NotBlank String imageName,
                                                              @NotBlank String imageTag,
                                                              @Min(1024) int serverPort,
                                                              @Min(1) int scrapeIntervalSeconds,
                                                              @NotEmpty List<String> scrapeUrls,
                                                              @Nullable Map<String, String> env,
                                                              @Nullable Consumer<String> createdListener,
                                                              @Nullable Consumer<InspectContainerResponse> startedListener) {
        Assert2.isTrue(scrapeIntervalSeconds > 0, "scrapeIntervalSeconds must be greater than 0");
        Assert2.notEmptyOf(scrapeUrls, "scrapeUrls");

        // @formatter:off
        String prometheusConfig =
                "global:\n" +
                "  scrape_interval: %ss\n" +
                "  evaluation_interval: %ss\n" +
                "scrape_configs:\n" +
                "  - job_name: it-application-job\n" +
                "    static_configs:\n";
        prometheusConfig = String.format(prometheusConfig, scrapeIntervalSeconds, scrapeIntervalSeconds);
        for (String scrapeUrl : scrapeUrls) {
            prometheusConfig +=
                    "    - targets: ['%s']\n" +
                    "      labels:\n" +
                    "        instance: '%s'";
            prometheusConfig = String.format(prometheusConfig, scrapeUrl, scrapeUrl);
        }
        // @formatter:on

        return buildBitnamiContainer(startedLatchSupplier, "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_prometheus",
                imageTag, asList(serverPort, 9090), "prometheus", "(.*)Server is ready to receive web requests(.*)",
                null, null, singletonMap("/opt/bitnami/prometheus/conf/prometheus.yml", prometheusConfig),
                env, createdListener, startedListener);
    }

    // --------------------- Grafana build Containers  -----------------------

    public ITGenericContainer buildBitnamiGrafana101xContainer(Supplier<CountDownLatch> startedLatchSupplier,
                                                               int mappedPort,
                                                               @Nullable Map<String, String> env) {
        return buildBitnamiGrafanaContainer(startedLatchSupplier, mappedPort, "10.1.5", env, null, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "unused", "all"})
    public ITGenericContainer buildBitnamiGrafanaContainer(Supplier<CountDownLatch> startedLatchSupplier,
                                                           @Min(1024) int mappedPort,
                                                           @NotBlank String imageTag,
                                                           @Nullable Map<String, String> env,
                                                           @Nullable Consumer<String> createdListener,
                                                           @Nullable Consumer<InspectContainerResponse> startedListener) {
        return buildBitnamiContainer(startedLatchSupplier, "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_grafana",
                imageTag, asList(mappedPort, 3000), "grafana", "(.*)HTTP Server Listen(.*)",
                null, null, null, env, createdListener, startedListener);
    }

    // --------------------- Generic build container ---------------------

    public ITGenericContainer buildBitnamiContainer(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                                    @NotBlank String imageRepo,
                                                    @NotBlank String imageTag,
                                                    @NotEmpty List<Integer> portBindings,
                                                    @NotBlank String loggerName,
                                                    @NotBlank String startedLogRegex,
                                                    @Nullable String networkMode,
                                                    @Nullable Duration startupTimeout,
                                                    @Nullable Map<String, String> configs,
                                                    @Nullable Map<String, String> env,
                                                    @Nullable Consumer<String> createdListener,
                                                    @Nullable Consumer<InspectContainerResponse> startedListener) {
        Assertions.assertNotNull(startedLatchSupplier, "startedLatchSupplier must not be null");
        Assertions.assertTrue(isNotBlank(imageRepo), "imageRepo must be like e.g: 'bitnami/zookeeper' or 'registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_zookeeper'");
        Assertions.assertTrue(isNotBlank(imageTag), "imageTag must be like 3.4.13");
        Assertions.assertTrue(nonNull(portBindings) && portBindings.size() % 2 == 0, "portBindings must be in pairs, e.g: [58080,8080,59090,9090]");
        Assertions.assertTrue(isNotBlank(loggerName), "loggerName must be empty");
        Assertions.assertTrue(isNotBlank(startedLogRegex), "loggerName must be empty");

        startupTimeout = isNull(startupTimeout) ? Duration.ofSeconds(IT_START_CONTAINERS_TIMEOUT) : startupTimeout;

        final List<String> portBindings0 = new ArrayList<>();
        for (int i = 0; i < portBindings.size(); i += 2) {
            Integer mappedPort = portBindings.get(i);
            Integer containerPort = portBindings.get(i + 1);
            Assertions.assertTrue(nonNull(mappedPort) && mappedPort > 1024, "mappedPort must be greater than 1024");
            Assertions.assertTrue(nonNull(containerPort) && containerPort > 1024, "containerPort must be greater than 1024");
            portBindings0.add(mappedPort + ":" + containerPort);
        }

        @SuppressWarnings("rawtypes") final GenericContainer<?> container = new GenericContainer(imageRepo.concat(":").concat(imageTag)) {
            @SuppressWarnings("all")
            @Override
            protected void containerIsCreated(String containerId) {
                log.info("{}:{} container is created: {}", imageRepo, imageTag, containerId);

                //final String originalCmd = StringUtils.join(containerInfo.getConfig().getCmd());
                //withCommand("sh", "-c", config + "; " + originalCmd);

                safeMap(configs).forEach((configPath, configContent) ->
                        copyFileToContainer(Transferable.of(configContent, 0777), configPath));

                if (nonNull(createdListener)) {
                    createdListener.accept(containerId);
                }
            }

            @Override
            protected void containerIsStarted(InspectContainerResponse containerInfo) {
                log.info("{}:{} container is started: {}", imageRepo, imageTag, containerInfo.toString());
                startedLatchSupplier.get().countDown();

                if (nonNull(startedListener)) {
                    startedListener.accept(containerInfo);
                }
            }
        };

        container.withEnv(env)
                .withReuse(false)
                //.withExposedPorts(portBindings) // no need
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.".concat(loggerName))))
                .waitingFor(Wait.forLogMessage(startedLogRegex, 1).withStartupTimeout(startupTimeout));
        container.setPortBindings(portBindings0);
        if (!isBlank(networkMode)) {
            container.withNetworkMode(networkMode);
        }
        container.setStartupAttempts(3);

        return new ITGenericContainer(portBindings0, container);
    }

}

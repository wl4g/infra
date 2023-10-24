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

import com.fasterxml.jackson.databind.JsonNode;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.wl4g.infra.common.lang.Assert2;
import com.wl4g.infra.common.tests.integration.testcontainers.RocketMQContainer;
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
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import javax.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.wl4g.infra.common.serialize.JacksonUtils.parseToNode;
import static java.lang.String.format;
import static java.lang.System.*;
import static java.util.Collections.singletonList;
import static java.util.Objects.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * The {@link GenericITContainerManager}
 *
 * @author James Wong
 * @since v3.1
 **/
//@Testcontainers
@SuppressWarnings({"rawtypes", "deprecation", "unused"})
public abstract class GenericITContainerManager extends ITContainerManagerSupport {
    public static final String KAFKA_UI_01 = "kafka-ui-01";

    public GenericITContainerManager(@NotNull Class<?> testClass) {
        super(testClass);
    }

    protected void initManagementContainers(@NotNull Supplier<CountDownLatch> startedLatchSupplier,
                                            @NotNull Map<String, ITGenericContainerWrapper> mgmtContainers) {
        if (IT_START_MGMT_CONTAINERS_ENABLE) {
            final List<String> kafkaClusters = getMwContainers()
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

    //
    // --------------------- Getting Run Containers Configuration  -----------------------
    //

    public String getKafkaClusterServers(String clusterName) {
        return getServersConnectionString("PLAINTEXT://", clusterName);
    }

    public String getRocketMQClusterServers(String clusterName) {
        // return ((RocketMQContainer) getRequiredContainer(clusterName)).getNamesrvAddr();
        return getServersConnectionString("", clusterName);
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

    private ITGenericContainerWrapper buildBitnami24PrometheusContainer(Supplier<CountDownLatch> startedLatchSupplier,
                                                                        int mappedPort,
                                                                        int scrapeIntervalSeconds,
                                                                        List<String> scrapeUrls,
                                                                        Map<String, String> env) {
        return buildBitnamiPrometheusContainer(startedLatchSupplier, mappedPort,
                "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_prometheus",
                "2.47.2",
                scrapeIntervalSeconds,
                scrapeUrls,
                env);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "unused", "all"})
    private ITGenericContainerWrapper buildBitnamiPrometheusContainer(Supplier<CountDownLatch> startedLatchSupplier,
                                                                      @Min(1024) int mappedPort,
                                                                      @NotBlank String imageName,
                                                                      @NotBlank String imageVersion,
                                                                      @Min(1) int scrapeIntervalSeconds,
                                                                      @NotEmpty List<String> scrapeUrls,
                                                                      Map<String, String> env) {
        requireNonNull(startedLatchSupplier, "startedLatchSupplier");
        Assert2.isTrue(mappedPort > 1024, "mappedPort must be greater than 1024");
        Assert2.hasTextOf(imageName, "imageName");
        Assert2.hasTextOf(imageVersion, "imageVersion");
        Assert2.isTrue(scrapeIntervalSeconds > 0, "scrapeIntervalSeconds must be greater than 0");
        Assert2.notEmptyOf(scrapeUrls, "scrapeUrls");

        // "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_prometheus"
        imageName = isBlank(imageName) ? "bitnami/prometheus" : imageName;
        final GenericContainer prometheusContainer = new GenericContainer(imageName.concat(":").concat(imageVersion)) {

            @Override
            protected void containerIsCreated(String containerId) {
                // @formatter:off
                String config =
                        "global:\n" +
                                "  scrape_interval: %ss\n" +
                                "  evaluation_interval: %ss\n" +
                                "scrape_configs:\n" +
                                "  - job_name: it-application-job\n" +
                                "    static_configs:\n";
                config = String.format(config, scrapeIntervalSeconds, scrapeIntervalSeconds);
                for (String scrapeUrl : scrapeUrls) {
                    config += "    - targets: ['%s']\n" +
                              "      labels:\n" +
                              "        instance: '%s'";
                    config = String.format(config, scrapeUrl, scrapeUrl);
                }
                // @formatter:on
                copyFileToContainer(Transferable.of(config, 0777), "/opt/bitnami/prometheus/conf/prometheus.yml");

                //final String originalCmd = StringUtils.join(containerInfo.getConfig().getCmd());
                //withCommand("sh", "-c", config + "; " + originalCmd);
            }

            @Override
            protected void containerIsStarted(InspectContainerResponse containerInfo) {
                log.info("Prometheus container is started: " + containerInfo.getId());
                startedLatchSupplier.get().countDown();
            }
        };
        prometheusContainer
                //.withNetworkMode("host")
                .withEnv(env)
                .withReuse(false)
                .withExposedPorts(mappedPort)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.prometheus")))
                .waitingFor(Wait.forLogMessage("(.*)Server is ready to receive web requests(.*)", 1)
                        .withStartupTimeout(Duration.ofMinutes(1)));
        prometheusContainer.setPortBindings(singletonList(mappedPort + ":9090"));
        return new ITGenericContainerWrapper(mappedPort, prometheusContainer);
    }

    private ITGenericContainerWrapper buildBitnami101GrafanaContainer(Supplier<CountDownLatch> startedLatchSupplier,
                                                                      int mappedPort,
                                                                      Map<String, String> env) {
        return buildBitnamiGrafanaContainer(startedLatchSupplier, mappedPort,
                "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_grafana",
                "10.1.5",env);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "unused", "all"})
    private ITGenericContainerWrapper buildBitnamiGrafanaContainer(Supplier<CountDownLatch> startedLatchSupplier,
                                                                   int mappedPort,
                                                                   String imageName,
                                                                   String imageVersion,
                                                                   Map<String, String> env) {
        // "registry.cn-shenzhen.aliyuncs.com/wl4g-k8s/bitnami_grafana"
        imageName = isBlank(imageName) ? "bitnami/grafana" : imageName;
        final GenericContainer grafanaContainer = new GenericContainer(imageName.concat(":").concat(imageVersion)) {
            @Override
            protected void containerIsStarted(InspectContainerResponse containerInfo) {
                log.info("Grafana container is started: " + containerInfo.getId());
                startedLatchSupplier.get().countDown();
            }
        };
        grafanaContainer
                //.withNetworkMode("host")
                .withEnv(env)
                .withReuse(false)
                .withExposedPorts(mappedPort)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("testcontainers.grafana")))
                .waitingFor(Wait.forLogMessage("(.*)HTTP Server Listen(.*)", 1)
                        .withStartupTimeout(Duration.ofMinutes(1)));
        grafanaContainer.setPortBindings(singletonList(mappedPort + ":3000"));
        return new ITGenericContainerWrapper(mappedPort, grafanaContainer);
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

}

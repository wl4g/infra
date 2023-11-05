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

package com.wl4g.infra.common.tests.integration.mock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static com.wl4g.infra.common.crypto.digest.DigestUtils2.md5Hex;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

/**
 * The {@link SimpleKafkaDataMocker}
 *
 * @author James Wong
 * @since v1.0
 **/
@Slf4j
@Getter
public class SimpleKafkaDataMocker extends AbstractDataMocker {
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final Supplier<CountDownLatch> finishedLatchSupplier;
    private final String kafkaServers;
    private final String kafkaTopic;
    private final int partition;
    private final int replicationFactor;
    private final int limitTotal;
    private final AtomicInteger progress = new AtomicInteger(0);
    private final AtomicLong recordsBytesTotal = new AtomicLong(0);
    private long beginTime = 0L;
    private long endTime = 0L;

    public SimpleKafkaDataMocker(Supplier<CountDownLatch> finishedLatchSupplier,
                                 String kafkaServers,
                                 String kafkaTopic,
                                 int partition,
                                 int replicationFactor,
                                 int limitTotal) {
        this.finishedLatchSupplier = requireNonNull(finishedLatchSupplier);
        this.kafkaServers = requireNonNull(kafkaServers);
        this.kafkaTopic = requireNonNull(kafkaTopic);
        this.partition = partition;
        this.replicationFactor = replicationFactor;
        this.limitTotal = limitTotal;
        if (limitTotal <= 0) {
            throw new IllegalArgumentException("Total must be greater than 0");
        }
    }

    @Override
    public void run() {
        initKafkaSourceTopics();
        beforeExecution();
        doRun();
        afterExecution();
    }

    @Override
    public void close() throws IOException {
        isRunning.set(false);
    }

    @Override
    public void printStatistics() {
    }

    protected void initKafkaSourceTopics() {
        log.info("Recreating to Kafka source topic: {} on {}", kafkaTopic, kafkaServers);
        final Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        try (AdminClient adminClient = AdminClient.create(props)) {
            try {
                DeleteTopicsResult result = adminClient.deleteTopics(singleton(kafkaTopic));
                result.all().get(2L, TimeUnit.MINUTES);
                log.info("Deleted Kafka source topic: " + kafkaTopic);
            } catch (Throwable ex) {
                Throwable reason = ExceptionUtils.getRootCause(ex);
                if (reason instanceof UnknownTopicOrPartitionException) {
                    log.warn("Not exists Kafka source topic: " + kafkaTopic);
                } else {
                    throw new IllegalStateException(String.format("Could not delete Kafka source topic: %s",
                            kafkaTopic), ex);
                }
            }
            try {
                log.info("Creating Kafka input topic: " + kafkaTopic);
                CreateTopicsResult result = adminClient.createTopics(singleton(new NewTopic(kafkaTopic,
                        partition, (short) replicationFactor)));
                result.all().get(2L, TimeUnit.MINUTES);
                log.info("Created Kafka source topic: " + kafkaTopic);
            } catch (Throwable ex) {
                Throwable reason = ExceptionUtils.getRootCause(ex);
                if (reason instanceof TopicExistsException) {
                    log.warn("Already exists Kafka source topic: " + kafkaTopic);
                } else {
                    throw new IllegalStateException(String.format("Could not create Kafka source topic: %s, " +
                            "please check sets to auto.create.topics.enable=true ?", kafkaTopic), ex);
                }
            }
        }
    }

    protected void beforeExecution() {
    }

    protected void afterExecution() {
        finishedLatchSupplier.get().countDown(); // setup to mocked finished.
    }

    @SuppressWarnings("all")
    protected void doRun() {
        final Map<String, Object> props = new HashMap<>();
        //props.put(ProducerConfig.CLIENT_DNS_LOOKUP_CONFIG, MockCustomHostResolver.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "32");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        Producer<String, String> producer = null;
        try {
            producer = new KafkaProducer<>(props);
            this.beginTime = System.currentTimeMillis();
            while (isRunning.get() && progress.get() < this.limitTotal) {
                try {
                    final ProducerRecord<String, String> pr = generateNextRecord();
                    log.info("Sending to Kafka source topic: {}, key: {}, value: {}", kafkaTopic, pr.key(), pr.value());

                    recordsBytesTotal.addAndGet(pr.value().getBytes(UTF_8).length + pr.key().getBytes(UTF_8).length);

                    final Future<RecordMetadata> future = producer.send(pr);
                    if (progress.get() % 100 == 0) {
                        producer.flush();
                    }

                    final RecordMetadata rm = future.get();
                    progress.incrementAndGet();

                    log.info(">>> Produce to kafka progressed: {}, topic: {}, key: {} of result: {}",
                            progress.get(), kafkaTopic, pr.key(), rm);
                    doSleep();
                } catch (Exception ex) {
                    log.error("Produce to kafka error", ex);
                    try {
                        producer.flush();
                        producer.close();
                        producer = new KafkaProducer<>(props);
                    } catch (Throwable ex2) {
                        log.error("Recreate producer error", ex2);
                    }
                }
            }
        } finally {
            log.info(">>> Closing producer to kafka: {}", kafkaServers);
            if (nonNull(producer)) {
                producer.flush();
                producer.close();
            }
            log.info(">>> Closed producer to kafka: {}", kafkaServers);
            this.endTime = System.currentTimeMillis();
        }
    }

    protected ProducerRecord<String, String> generateNextRecord() {
        final String key = String.format("mock_%s", progress.get());
        final Properties value = new Properties();
        value.put("msgId", key);
        value.put("ts", System.currentTimeMillis());
        value.put("state", RandomUtils.nextBoolean());
        value.put("deviceId", RandomUtils.nextInt(10000000, 99999999));
        value.put("signature", md5Hex(randomAlphabetic(16)));

        log.info("Sending to Kafka source topic: {}, key: {}, value: {}", kafkaTopic, key, value);
        return new ProducerRecord<>(kafkaTopic, key, toJSONString(value));
    }

    protected void doSleep() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1L);
    }

}


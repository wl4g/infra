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

import com.fasterxml.jackson.databind.JsonNode;
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

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.wl4g.infra.common.serialize.JacksonUtils.parseToNode;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.err;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;

/**
 * The {@link AssertionITContainerManager}
 *
 * @author James Wong
 * @since v3.1
 **/
@SuppressWarnings({"unused"})
public abstract class AssertionITContainerManager extends GenericITContainerManager {

    public AssertionITContainerManager(Class<?> testClass) {
        super(testClass);
    }

    // --------------------- MQ consuming Assertion Runners  --------------

    @SuppressWarnings("all")
    public Runnable buildKafkaConsumingAssertionRunner(@NotNull CountDownLatch latch,
                                                       @NotBlank String kafkaServers,
                                                       @NotBlank String topic,
                                                       @NotBlank String groupId,
                                                       @Nullable Properties overrideProps,
                                                       @Min(1) int timeoutSeconds,
                                                       @NotNull Function<JsonNode, Boolean> collector,
                                                       @Min(1) int exitConsumedThreshold,
                                                       @NotNull Consumer<Map<String, JsonNode>> assertion) {
        Assertions.assertNotNull(latch, "latch must not be null");
        Assertions.assertNotNull(kafkaServers, "kafkaServers must not be null");
        Assertions.assertNotNull(topic, "topic must not be null");
        Assertions.assertNotNull(groupId, "groupId must not be null");
        Assertions.assertTrue(timeoutSeconds > 0, "timeoutSeconds must be greater than 0");
        Assertions.assertNotNull(collector, "collector must not be null");
        Assertions.assertTrue(exitConsumedThreshold > 0, "exitConsumedThreshold must be greater than 0");
        Assertions.assertNotNull(assertion, "assertion must not be null");
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
            if (nonNull(overrideProps)) {
                props.putAll(overrideProps);
            }
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
                    if (result.size() >= exitConsumedThreshold) {
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
                                                          int exitConsumedThreshold,
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
                    if (result.size() >= exitConsumedThreshold) {
                        latch.countDown();
                        log.info("Assertion for RocketMQ consumer is valid. Total: " + result.size());
                        break;
                    }
                }
                assertion.accept(result);
            } catch (Throwable ex) {
                latch.countDown();
                final String errmsg = String.format("Failed to assertion for consuming RocketMQ on %s",
                        namesrvAddr);
                err.println(errmsg);
                throw new IllegalStateException(errmsg, ex);
            } finally {
                consumer.shutdown();
            }
        };
    }
}

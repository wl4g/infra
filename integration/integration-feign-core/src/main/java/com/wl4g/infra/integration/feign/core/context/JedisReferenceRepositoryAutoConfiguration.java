/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * James Wong <jameswong1376@gmail.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: https://wl4g.github.io
 */
package com.wl4g.infra.integration.feign.core.context;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wl4g.infra.common.jedis.JedisClient;
import com.wl4g.infra.integration.feign.core.context.RpcContextHolder.ReferenceRepository;
import com.wl4g.infra.support.cache.jedis.JedisClientFactoryBean;

/**
 * Notes: There is no need to check the automatic configuration only in the
 * springboot + feign environment, because it is required in both springboot +
 * feign and springcloud + feign environments.
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2021-01-25
 * @since v2.0
 * @see
 */
@Configuration
@ConditionalOnClass(JedisClientFactoryBean.class)
public class JedisReferenceRepositoryAutoConfiguration {

    @Bean
    // @ConditionalOnBean(JedisClientBuilder.class) // invalid??
    public ReferenceRepository jedisReferenceRepository(@Autowired(required = false) JedisClient jedisClient) {
        return isNull(jedisClient) ? null : new JedisReferenceRepository(jedisClient);
    }

    /**
     * Jedis implemention reference attachments repository.
     */
    class JedisReferenceRepository implements ReferenceRepository {
        private final JedisClient jedisClient;

        public JedisReferenceRepository(JedisClient jedisClient) {
            this.jedisClient = notNullOf(jedisClient, "jedisClient");
        }

        @Override
        public <T> T doGetRefValue(@NotBlank String refKey, @NotNull Class<T> valueType) {
            return parseJSON(jedisClient.get(refKey), valueType);
        }

        @Override
        public boolean doSetRefValue(@NotBlank String refKey, Object value) {
            return nonNull(jedisClient.set(refKey, toJSONString(value)));
        }

        @Override
        public boolean doRemoveRefValue(@NotBlank String refKey) {
            Long result = jedisClient.del(refKey);
            return nonNull(result) && result > 0;
        }
    }

}

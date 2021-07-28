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
package com.wl4g.component.integration.sharding.failover.config;

import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import com.wl4g.component.integration.sharding.failover.exception.InvalidConfigurationFailoverException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link FailoverConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-26 v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
@ToString
public class FailoverConfiguration {
    private boolean enable = true;
    private long inspectInitialDelayMs = 3_000L;
    private long inspectMinDelayMs = 10_000L;
    private long inspectMaxDelayMs = 30_000L;

    private Map<String, FailoverAdminUser> adminUsers = synchronizedMap(new HashMap<>());

    public void merge(Properties props) {
        try {
            Map<Object, Object> failoverProps = safeMap(props).entrySet().stream()
                    .filter(e -> valueOf(e.getKey()).startsWith(KEY_PREFIX))
                    .collect(toMap(e -> valueOf(e.getKey()).substring(KEY_PREFIX.length()), e -> e.getValue()));
            BeanUtilsBean2.getInstance().copyProperties(this, failoverProps);

            safeMap(failoverProps).entrySet().stream().filter(e -> valueOf(e.getKey()).startsWith(KEY_DB_PREFIX)).forEach(e -> {
                String fullKeyName = valueOf(e.getKey());
                String value = valueOf(e.getValue());
                String schemaName = fullKeyName.substring(KEY_DB_PREFIX.length());

                // Check schemaName.
                if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
                    throw new InvalidConfigurationFailoverException(
                            format("Invalid failover configuration. unknown schemaName: %s", schemaName), null);
                }

                adminUsers.put(schemaName, parseJSON(value, FailoverAdminUser.class));
            });

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public final static class FailoverAdminUser {
        private String username;
        private String password;
    }

    private static final String KEY_PREFIX = "failover-";
    private static final String KEY_DB_PREFIX = "admin-datasource-props-json-for-"; // admin-dataSource
}

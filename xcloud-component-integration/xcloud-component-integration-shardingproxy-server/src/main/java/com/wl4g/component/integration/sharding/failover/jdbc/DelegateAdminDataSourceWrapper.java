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
package com.wl4g.component.integration.sharding.failover.jdbc;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.sharding.failover.exception.InvalidStateFailoverException;
import com.wl4g.component.integration.sharding.failover.exception.NoNextAdminDataSourceFailoverException;
import com.zaxxer.hikari.HikariDataSource;

/**
 * {@link DelegateAdminDataSourceWrapper}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-29 v1.0.0
 * @since v1.0.0
 */
public final class DelegateAdminDataSourceWrapper implements Iterator<HikariDataSource>, Closeable {
    protected final SmartLogger log = getLogger(getClass());

    private final TreeMap<String, HikariDataSource> dataSources = new TreeMap<>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    });

    private AtomicInteger selectionPos = new AtomicInteger(-1);
    private volatile HikariDataSource selection;

    @Override
    public boolean hasNext() {
        return selectionPos.incrementAndGet() <= (dataSources.size() - 1);
    }

    @Override
    public synchronized HikariDataSource next() {
        if (!hasNext()) {
            throw new NoNextAdminDataSourceFailoverException(
                    format("Currently attempted: %s, all dataSources: %s", selectionPos.get(), dataSources));
        }
        int index = 0;
        for (String dsname : dataSources.keySet()) {
            if (index++ == selectionPos.get()) {
                return selection = dataSources.get(dsname);
            }
        }
        throw new NoNextAdminDataSourceFailoverException(
                format("Currently attempted: %s, all dataSources: %s", selectionPos.get(), dataSources));
    }

    public boolean hasAvailable() {
        return !dataSources.isEmpty();
    }

    public HikariDataSource get() {
        if (isNull(selection)) {
            next();
        }
        if (isNull(selection)) {
            throw new InvalidStateFailoverException("There are currently no available data sources selected.");
        }
        return selection;
    }

    public synchronized DelegateAdminDataSourceWrapper addDataSource(String dataSourceName, HikariDataSource dataSource) {
        this.dataSources.put(dataSourceName, dataSource);
        return this;
    }

    @Override
    public void close() throws IOException {
        dataSources.forEach((dsname, ds) -> {
            try {
                ds.close();
            } catch (Exception e) {
                log.error("", e);
            }
        });
    }

}

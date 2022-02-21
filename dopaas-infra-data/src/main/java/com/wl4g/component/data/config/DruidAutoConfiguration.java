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
package com.wl4g.component.data.config;

import static com.wl4g.component.common.web.WebUtils2.isTrue;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.getenv;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.wl4g.component.common.codec.CodecSource;
import com.wl4g.component.common.crypto.symmetric.AES128ECBPKCS5;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;
import lombok.Setter;

/**
 * Druid DataSource auto configuration.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月13日
 * @since
 */
@ConditionalOnClass(DruidDataSource.class)
@ConditionalOnExpression("'com.alibaba.druid.pool.DruidDataSource'.equalsIgnoreCase('${spring.datasource.type:}')")
@ConditionalOnMissingBean(HikariDataSource.class)
public class DruidAutoConfiguration extends BasedMybatisDataSourceConfigurer {

    // @RefreshScope
    // @ConditionalOnMissingBean
    @Bean
    public DruidDataSource druidDataSource(DruidProperties config) {
        DruidDataSource druid = new DruidDataSource();
        druid.setUrl(config.getUrl());
        druid.setUsername(config.getUsername());
        String plain = config.getPassword();
        String skip = getenv("SKIP_CIPHER_DB_PASSWD");
        if (valueOf(environment.getProperty("spring.profiles.active")).startsWith("pro") && !isTrue(skip, false)) {
            try {
                // TODO using dynamic cipherKey??
                byte[] cipherKey = AES128ECBPKCS5.getEnvCipherKey("DB_CIPHER_KEY");
                plain = new AES128ECBPKCS5().decrypt(cipherKey, CodecSource.fromHex(config.getPassword())).toString();
            } catch (Throwable th) {
                throw new IllegalStateException(format("Unable to decryption database password for '%s'", config.getPassword()),
                        th);
            }
        }
        druid.setPassword(plain);
        druid.setDriverClassName(config.getDriverClassName());
        druid.setInitialSize(config.getInitialSize());
        druid.setMinIdle(config.getMinIdle());
        druid.setMaxActive(config.getMaxActive());
        druid.setMaxWait(config.getMaxWait());
        druid.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
        druid.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
        druid.setValidationQuery(config.getValidationQuery());
        druid.setTestWhileIdle(config.isTestWhileIdle());
        druid.setTestOnBorrow(config.isTestOnBorrow());
        druid.setTestOnReturn(config.isTestOnReturn());
        try {
            druid.setFilters(config.getFilters());
        } catch (SQLException e) {
            log.error("Cannot initialization druid filter", e);
        }
        return druid;
    }

    @Bean
    public SqlSessionFactoryBean druidSmartSqlSessionFactoryBean(MybatisProperties config, DataSource dataSource,
            List<Interceptor> interceptors) throws Exception {
        return createSmartSqlSessionFactoryBean(config, dataSource, interceptors);
    }

    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet(DruidProperties druidConfig) {
        ServletRegistrationBean<StatViewServlet> registrar = new ServletRegistrationBean<>();
        registrar.setServlet(new StatViewServlet());
        registrar.addUrlMappings("/druid/*");
        registrar.addInitParameter("loginUsername", druidConfig.getWebLoginUsername());
        registrar.addInitParameter("loginPassword", druidConfig.getWebLoginPassword());
        registrar.addInitParameter("logSlowSql", druidConfig.getLogSlowSql());
        return registrar;
    }

    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
        FilterRegistrationBean<WebStatFilter> registrar = new FilterRegistrationBean<>();
        registrar.setFilter(new WebStatFilter());
        registrar.addUrlPatterns("/druid/*");
        registrar.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        registrar.addInitParameter("profileEnable", "true");
        return registrar;
    }

    @Bean
    public DruidProperties druidProperties() {
        return new DruidProperties();
    }

    @Getter
    @Setter
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    static class DruidProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private int initialSize;
        private int minIdle;
        private int maxActive;
        private int maxWait;
        private int timeBetweenEvictionRunsMillis;
        private int minEvictableIdleTimeMillis;
        private String validationQuery;
        private boolean testWhileIdle;
        private boolean testOnBorrow;
        private boolean testOnReturn;
        private String filters;
        private String logSlowSql;
        private String webLoginUsername = "druid";
        private String webLoginPassword = "druid";
    }

    static {
        // com.alibaba.druid.support.logging.LogFactory.static{}
        System.setProperty("druid.logType", "slf4j");
    }

}
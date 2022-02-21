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

import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import com.wl4g.component.common.codec.CodecSource;
import com.wl4g.component.common.crypto.symmetric.AES128ECBPKCS5;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Hikari DataSource auto configuration.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-04-30
 * @sine v1.0
 * @see
 */
@ConditionalOnClass(HikariDataSource.class)
@ConditionalOnExpression("'com.zaxxer.hikari.HikariDataSource'.equalsIgnoreCase('${spring.datasource.type:}')")
public class HikariAutoConfiguration extends BasedMybatisDataSourceConfigurer {

    // @RefreshScope
    // @ConditionalOnMissingBean
    @Bean
    public HikariDataSource hikariDataSource(HikariProperties config) {
        HikariDataSource hikari = new HikariDataSource(config);

        // TODO use config center.
        // Update database password.
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
        hikari.setPassword(plain);

        return hikari;
    }

    @Bean
    public SqlSessionFactoryBean hikariSmartSqlSessionFactoryBean(MybatisProperties config, DataSource dataSource,
            List<Interceptor> interceptors) throws Exception {
        return createSmartSqlSessionFactoryBean(config, dataSource, interceptors);
    }

    @Bean
    public HikariProperties hikariProperties() {
        return new HikariProperties();
    }

    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    static class HikariProperties extends HikariConfig {
    }

}
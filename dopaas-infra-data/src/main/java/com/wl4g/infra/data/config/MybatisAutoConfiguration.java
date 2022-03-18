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
package com.wl4g.infra.data.config;

import static com.wl4g.infra.data.constant.DataInfraConstants.CONF_PREFIX_INFRA_DATA_HOTSPOT_LOADER;
import static com.wl4g.infra.data.constant.DataInfraConstants.CONF_PREFIX_INFRA_DATA_MYBATIS;

import java.util.Properties;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.github.pagehelper.PageHelper;
import com.wl4g.infra.core.annotation.condition.ConditionalOnJdwpDebug;
import com.wl4g.infra.data.mybatis.loader.SqlSessionMapperHotspotLoader;
import com.wl4g.infra.data.mybatis.loader.SqlSessionMapperHotspotLoader.HotspotLoaderProperties;
import com.wl4g.infra.data.mybatis.mapper.IdGenerator;
import com.wl4g.infra.data.mybatis.mapper.PreparedBeanMapperInterceptor;

/**
 * {@link SqlSessionMapperHotspotLoader} auto configuration.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年11月14日
 * @since
 */
@ConditionalOnClass(SqlSessionFactory.class)
public class MybatisAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_DATA_MYBATIS)
    public MybatisProperties mybatisProperties() {
        return new MybatisProperties();
    }

    // --- Hotspot loader. ---

    @Bean
    @ConditionalOnJdwpDebug(enableProperty = CONF_PREFIX_INFRA_DATA_HOTSPOT_LOADER + ".enabled")
    @ConfigurationProperties(prefix = CONF_PREFIX_INFRA_DATA_HOTSPOT_LOADER)
    // @ConditionalOnBean(SmartSqlSessionFactoryBean.class)
    public HotspotLoaderProperties hotspotLoaderProperties() {
        return new HotspotLoaderProperties();
    }

    @Bean
    @ConditionalOnBean(value = { HotspotLoaderProperties.class })
    public SqlSessionMapperHotspotLoader sqlSessionMapperHotspotLoader(SqlSessionFactoryBean sessionFactory,
            HotspotLoaderProperties config) {
        return new SqlSessionMapperHotspotLoader(sessionFactory, config);
    }

    // --- Mapper ID generators. ---

    /**
     * A better recommendation is to use a dedicated ID generation
     * services.</br>
     * <p>
     * for example: <a href='https://github.com/wl4g/xcloud-dguid'>Dguid -
     * https://github.com/wl4g/xcloud-dguid</a>
     * </p>
     * 
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public IdGenerator defaultIdGenerator() {
        return new IdGenerator() {
        };
    }

    // --- Mapper interceptors. ---

    @Bean
    @ConditionalOnClass(PageHelper.class)
    @Order(ORDER_PAGEHELPER)
    public PageHelper githubPageHelper() {
        Properties props = new Properties();
        props.setProperty("dialect", "mysql");
        props.setProperty("reasonable", "true");
        props.setProperty("supportMethodsArguments", "true");
        props.setProperty("returnPageInfo", "check");
        props.setProperty("params", "count=countSql");
        PageHelper page = new PageHelper();
        page.setProperties(props); // 添加插件
        return page;
    }

    @Bean
    @Order(ORDER_PREPARED_BEAN_MAPPER)
    public PreparedBeanMapperInterceptor preparedBeanMapperInterceptor() {
        return new PreparedBeanMapperInterceptor();
    }

    /**
     * Make sure that the prepared bean mapper interceptor is executed the
     * earliest. </br>
     * </br>
     * Note: mybatis {@link Interceptor} is reverse, that is, the last execution
     * that is first added to {@link InterceptorChain}
     */
    public static final int ORDER_PREPARED_BEAN_MAPPER = Ordered.LOWEST_PRECEDENCE;
    public static final int ORDER_PAGEHELPER = ORDER_PREPARED_BEAN_MAPPER - 1;

}
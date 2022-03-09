package com.wl4g.infra.integration.feign.core.config;

import static com.wl4g.infra.integration.feign.core.config.FeignConsumerAutoConfiguration.BEAN_DEFAULT_FEIGN_CLIENT;
import static com.wl4g.infra.integration.feign.core.constant.FeignConsumerConstant.KEY_CONFIG_PREFIX;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;

import feign.Client;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

@ConditionalOnExpression("'okhttp3'.equalsIgnoreCase('${" + KEY_CONFIG_PREFIX + ".client-provider:okhttp3}')")
@ConditionalOnClass(OkHttpClient.class)
public class OkhttpFeignConsumerAutoConfiguration {

    @Bean(BEAN_OKHTTP3_POOL)
    public ConnectionPool okHttp3ConnectionPool(FeignConsumerProperties config) {
        return new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), MINUTES);
    }

    @Bean(BEAN_DEFAULT_FEIGN_CLIENT)
    public Client feignOkHttp3Client(FeignConsumerProperties config, @Qualifier(BEAN_OKHTTP3_POOL) ConnectionPool pool) {
        OkHttpClient delegate = new OkHttpClient().newBuilder()
                .connectionPool(pool)
                .connectTimeout(config.getConnectTimeout(), MILLISECONDS)
                .readTimeout(config.getReadTimeout(), MILLISECONDS)
                .writeTimeout(config.getWriteTimeout(), MILLISECONDS)
                .build();
        return new feign.okhttp.OkHttpClient(delegate);
    }

    private static final String BEAN_OKHTTP3_POOL = "infraSpringBootFeign.okhttp3Pool";

}
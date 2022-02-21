package com.wl4g.component.integration.feign.core.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import org.springframework.context.annotation.Bean;

import static com.wl4g.component.integration.feign.core.config.FeignConsumerAutoConfiguration.*;
import static com.wl4g.component.integration.feign.core.constant.FeignConsumerConstant.KEY_CONFIG_PREFIX;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import feign.Client;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

@ConditionalOnExpression("'okhttp3'.equalsIgnoreCase('${" + KEY_CONFIG_PREFIX + ".client-provider:okhttp3}')")
@ConditionalOnClass(OkHttpClient.class)
public class OkhttpFeignConsumerAutoConfiguration {

	@Bean
	public ConnectionPool okHttp3ConnectionPool(FeignConsumerProperties config) {
		return new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), MINUTES);
	}

	@Bean(BEAN_FEIGN_CLIENT)
	public Client okHttpFeignClient(FeignConsumerProperties config, ConnectionPool pool) {
		OkHttpClient delegate = new OkHttpClient().newBuilder().connectionPool(pool)
				.connectTimeout(config.getConnectTimeout(), MILLISECONDS).readTimeout(config.getReadTimeout(), MILLISECONDS)
				.writeTimeout(config.getWriteTimeout(), MILLISECONDS).build();
		return new feign.okhttp.OkHttpClient(delegate);
	}

}
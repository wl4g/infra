package com.wl4g.infra.integration.feign.core.config;

import static com.wl4g.infra.integration.feign.core.constant.FeignConsumerConstant.KEY_CONFIG_PREFIX;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

//import static com.wl4g.infra.integration.feign.core.config.FeignConsumerAutoConfiguration.BEAN_DEFAULT_FEIGN_CLIENT;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
//import org.springframework.boot.system.JavaVersion;
//
//import feign.http2client.Http2Client;

@ConditionalOnExpression("'http2'.equalsIgnoreCase('${" + KEY_CONFIG_PREFIX + ".client-provider:http2}')")
// @ConditionalOnClass(HttpClient.class)
public class Http2FeignConsumerAutoConfiguration {

    // @Bean(BEAN_DEFAULT_FEIGN_CLIENT)
    // @ConditionalOnJava(JavaVersion.ELEVEN)
    // @ConditionalOnExpression(KEY_CLIENT_EXPRESSION)
    // @ConditionalOnClass(HttpClient.class)
    // public Client http2FeignClient() {
    // HttpClient httpClient =
    // HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS)
    // .version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofMillis(config.getConnectTimeout())).build();
    // return new Http2Client(httpClient);
    // }

}
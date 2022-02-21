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
package com.wl4g.component.core.remoting;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;

import static com.wl4g.component.common.lang.TypeConverts.safeLongToInt;
import static io.netty.channel.ChannelOption.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

/**
 * {@link WebClientAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-24
 * @sine v1.0
 * @see
 */
@ConditionalOnClass({ ClientHttpConnector.class, HttpClient.class })
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class WebClientAutoConfiguration {

	@Bean
	@ConfigurationProperties(prefix = "spring.web.remote")
	public ClientHttpProperties remoteProperties() {
		return new ClientHttpProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public ClientHttpConnector clientHttpConnector(ClientHttpProperties config, ReactorResourceFactory reactorFactory) {
		TcpClient client = TcpClient.create(reactorFactory.getConnectionProvider()).runOn(reactorFactory.getLoopResources())
				.option(CONNECT_TIMEOUT_MILLIS, safeLongToInt(SECONDS.toMillis(config.getConnectTimeout())))
				.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(config.getReadTimeout())));
		return new ReactorClientHttpConnector(HttpClient.from(client));
	}

	/**
	 * Remote rest template properties
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年11月20日
	 * @since
	 */
	public static class ClientHttpProperties {

		private int readTimeout = 60_000;
		private int connectTimeout = 10_000;
		private int maxResponseSize = 1024 * 1024 * 10;
		private SslProperties sslProperties = new SslProperties();

		public Integer getReadTimeout() {
			return readTimeout;
		}

		public void setReadTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
		}

		public int getConnectTimeout() {
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public int getMaxResponseSize() {
			return maxResponseSize;
		}

		public void setMaxResponseSize(int maxResponseSize) {
			this.maxResponseSize = maxResponseSize;
		}

		public SslProperties getSslProperties() {
			return sslProperties;
		}

		public void setSslProperties(SslProperties sslProperties) {
			this.sslProperties = sslProperties;
		}

	}

	/**
	 * Remote SSL context properties.
	 * 
	 * @author Wangl.sir <983708408@qq.com>
	 * @version v1.0
	 * @date 2018年11月21日
	 * @since
	 */
	public static class SslProperties {
		/*
		 * Make sure to sync this list with JdkSslEngineFactory.
		 */
		final public static List<String> DEFAULT_CIPHERS = Collections.unmodifiableList(Arrays.asList(
				new String[] { "ECDHE-RSA-AES128-SHA", "ECDHE-RSA-AES256-SHA", "AES128-SHA", "AES256-SHA", "DES-CBC3-SHA" }));

		private String keyCertChainFile;
		private String keyFile;
		/**
		 * Clearly specify OpenSSL, because jdk8 may have performance problems,
		 * See: https://www.cnblogs.com/wade-luffy/p/6019743.html#_label1
		 * {@link io.netty.handler.ssl.ReferenceCountedOpenSslContext
		 * ReferenceCountedOpenSslContext}
		 */
		private List<String> ciphers;

		public String getKeyCertChainFile() {
			return keyCertChainFile;
		}

		public void setKeyCertChainFile(String keyCertChainFile) {
			this.keyCertChainFile = keyCertChainFile;
		}

		public String getKeyFile() {
			return keyFile;
		}

		public void setKeyFile(String keyFile) {
			this.keyFile = keyFile;
		}

		public List<String> getCiphers() {
			return this.ciphers;
		}

		public void setCiphers(List<String> ciphers) {
			this.ciphers = ciphers;
		}
	}

}
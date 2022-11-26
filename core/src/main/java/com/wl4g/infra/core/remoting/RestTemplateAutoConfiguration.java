/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.core.remoting;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.wl4g.infra.core.constant.CoreInfraConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import okhttp3.CipherSuite;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

/**
 * {@link RestTemplateAutoConfiguration}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2019-12-24
 * @sine v1.0
 * @see
 */
@ConditionalOnClass({ RestTemplate.class })
@ConditionalOnWebApplication(type = Type.SERVLET)
public class RestTemplateAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = CoreInfraConstants.CONF_PREFIX_INFRA_CORE_HTTP_REMOTE)
    public ClientHttpProperties clientHttpProperties() {
        return new ClientHttpProperties();
    }

    @Bean(DEFAULT_REST_TEMPLATE)
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean(DEFAULT_REST_FACTORY)
    @ConditionalOnMissingBean
    @ConditionalOnClass({ OkHttpClient.class })
    public ClientHttpRequestFactory okhttp3ClientHttpRequestFactory(ClientHttpProperties config) {
        // https://square.github.io/okhttp/features/https/
        ConnectionPool pool = new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), MINUTES);

        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
                .cipherSuites(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                .build();

        // TODO
        // see:com.wl4g.iam.gateway.server.SecureSslServerCustomizer.SecureX509TrustManager
        //
        // X509TrustManager trustManager = trustManagerForCertificates(null);
        // SSLContext sslContext = SSLContext.getInstance("TLS");
        // sslContext.init(null, new TrustManager[] { trustManager }, null);
        // SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        OkHttpClient okhttpClient = new OkHttpClient().newBuilder()
                .connectionPool(pool)
                .connectionSpecs(singletonList(spec))
                .connectTimeout(config.getConnectTimeout(), MILLISECONDS)
                .readTimeout(config.getReadTimeout(), MILLISECONDS)
                .writeTimeout(config.getWriteTimeout(), MILLISECONDS)
                // TODO
                // .sslSocketFactory(sslSocketFactory, trustManager)
                .build();

        return new OkHttp3ClientHttpRequestFactory(okhttpClient);
    }

    /**
     * Clearly specify OpenSSL, because jdk8 may have performance problems, See:
     * https://www.cnblogs.com/wade-luffy/p/6019743.html#_label1
     * 
     * @return
     * @throws SSLException
     * @see {@link io.netty.handler.ssl.ReferenceCountedOpenSslContext}
     */
    // @Bean
    // @ConditionalOnMissingBean
    // public SslContext sslContext(ClientHttpProperties config) throws
    // SSLException {
    // SslProperties ssl = config.getSslProperties();
    // List<String> ciphers = ssl.getCiphers() == null ?
    // SslProperties.DEFAULT_CIPHERS : ssl.getCiphers();
    // return SslContextBuilder.forServer(new File(ssl.getKeyCertChainFile()),
    // new File(ssl.getKeyFile()))
    // .sslProvider(SslProvider.OPENSSL)
    // .ciphers(ciphers)
    // .clientAuth(ClientAuth.REQUIRE)
    // .trustManager(InsecureTrustManagerFactory.INSTANCE)
    // .build();
    // }

    /**
     * Verifies a SSL peer host name based on an explicit whitelist of allowed
     * hosts.
     * 
     * @author James Wong <jameswong1376@gmail.com>
     * @version v1.0
     * @date 2018年11月20日
     * @since
     */
    public final static class WhitelistHostnameVerifier implements HostnameVerifier {

        /** Allowed hosts */
        private String[] allowedHosts;

        /**
         * Creates a new instance using the given array of allowed hosts.
         * 
         * @param allowed
         *            Array of allowed hosts.
         */
        public WhitelistHostnameVerifier(final String[] allowed) {
            this.allowedHosts = allowed;
        }

        /**
         * Creates a new instance using the given list of allowed hosts.
         * 
         * @param allowedList
         *            Comma-separated list of allowed hosts.
         */
        public WhitelistHostnameVerifier(final String allowedList) {
            this.allowedHosts = allowedList.split(",\\s*");
        }

        /** {@inheritDoc} */
        public boolean verify(final String hostname, final SSLSession session) {
            for (final String allowedHost : this.allowedHosts) {
                if (hostname.equalsIgnoreCase(allowedHost)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Validates an SSL peer's hostname using a regular expression that a
     * candidate host must match in order to be verified.
     * 
     * @author James Wong <jameswong1376@gmail.com>
     * @version v1.0
     * @date 2018年11月20日
     * @since
     */
    public final static class RegexHostnameVerifier implements HostnameVerifier {

        /** Allowed hostname pattern */
        private Pattern pattern;

        /**
         * Creates a new instance using the given regular expression.
         * 
         * @param regex
         *            Regular expression describing allowed hosts.
         */
        public RegexHostnameVerifier(final String regex) {
            this.pattern = Pattern.compile(regex);
        }

        /** {@inheritDoc} */
        public boolean verify(final String hostname, final SSLSession session) {
            return pattern.matcher(hostname).matches();
        }
    }

    /**
     * Hostname verifier that performs no host name verification for an SSL peer
     * such that all hosts are allowed.
     * 
     * @author James Wong <jameswong1376@gmail.com>
     * @version v1.0
     * @date 2018年11月20日
     * @since
     */
    public final static class AnyHostnameVerifier implements HostnameVerifier {

        /** {@inheritDoc} */
        public boolean verify(final String hostname, final SSLSession session) {
            return true;
        }

    }

    /**
     * Remote rest template properties
     * 
     * @author James Wong <jameswong1376@gmail.com>
     * @version v1.0
     * @date 2018年11月20日
     * @since
     */
    @Getter
    @Setter
    @ToString
    public static class ClientHttpProperties {

        /** Max Idle time of connections */
        private int maxIdleConnections = 200;

        /** The keep alive default is 5 minutes. */
        private long keepAliveDuration = 5;

        /** The connect timeout default is 10 seconds. */
        private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;

        /** The read timeout default is 10 seconds. */
        private long readTimeout = DEFAULT_READ_TIMEOUT;

        /** The write timeout default is 10 seconds. */
        private long writeTimeout = DEFAULT_WRITE_TIMEOUT;

        // private int maxResponseSize = 1024 * 1024 * 10;

        private SslProperties sslProperties = new SslProperties();
    }

    /**
     * Remote SSL context properties.
     * 
     * @author James Wong <jameswong1376@gmail.com>
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

    public static final long DEFAULT_CONNECT_TIMEOUT = 3 * 1000L;
    public static final long DEFAULT_READ_TIMEOUT = 6 * 1000L;
    public static final long DEFAULT_WRITE_TIMEOUT = 6 * 1000L;

    public static final String DEFAULT_REST_TEMPLATE = "infraDefaultRestTeamplte";
    public static final String DEFAULT_REST_FACTORY = "infraDefaultOkhttp3Factory";

}
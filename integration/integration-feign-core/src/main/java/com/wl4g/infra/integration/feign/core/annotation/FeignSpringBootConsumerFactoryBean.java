/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.integration.feign.core.annotation;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static com.wl4g.infra.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static com.wl4g.infra.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findMethodNullable;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.infra.integration.feign.core.config.FeignSpringBootAutoConfiguration.BEAN_DEFAULT_FEIGN_CLIENT;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.wl4g.infra.common.annotation.Reserved;
import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.integration.feign.core.config.FeignSpringBootAutoConfiguration;
import com.wl4g.infra.integration.feign.core.config.FeignSpringBootProperties;
import com.wl4g.infra.integration.feign.core.context.internal.ConsumerFeignContextFilter.FeignContextDecoder;
import com.wl4g.infra.metrics.MetricsFacade;
import com.wl4g.infra.integration.feign.core.context.internal.FeignContextBuilder;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.Logger.Level;
import feign.MethodMetadata;
import feign.Request;
import feign.Request.Options;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.Retryer;
import feign.Target;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link FeignSpringBootConsumerFactoryBean}
 * 
 * @param <T>
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@Setter
@ToString
class FeignSpringBootConsumerFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    /**
     * To be consistent with the {@link feign.slf4j.Slf4jLogger} log prefix.
     */
    private final SmartLogger log = getLogger(feign.Logger.class);

    private ApplicationContext applicationContext;
    private MetricsFacade metricsFacade;
    private FeignSpringBootProperties config;
    private Contract defaultContract;
    private Client client;
    private List<RequestInterceptor> requestInterceptors;
    private FeignSpringBootTargetFactory feignTargetFactory;

    @Nullable
    private Class<T> targetClass;
    @Nullable
    private String name;
    @Nullable
    private String url;
    @Nullable
    private String path;
    @Nullable
    private Boolean decode404;
    @Nullable
    private Logger.Level logLevel;
    @Nullable
    private Class<?>[] configuration;
    @Nullable
    private Long connectTimeout;
    @Nullable
    private Long readTimeout;
    @Deprecated
    @Nullable
    private Long writeTimeout;
    @Nullable
    private Boolean followRedirects;
    @Nullable
    private String namespace;

    // Fall-back default configuration.
    private Class<?>[] defaultConfiguration;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Class<?> getObjectType() {
        return targetClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public T getObject() throws Exception {
        // Notice the invoke order:
        // can't get the bean in InitializingBean#afterPropertiesSet()?
        this.metricsFacade = obtainMetricsFacade();
        this.config = obtainFeignConfigProperties();
        this.defaultContract = obtainDefaultSpringMvcContract();
        this.client = obtainFeignHttpClientInstance();
        this.requestInterceptors = obtainFeignRequestInterceptors();
        this.feignTargetFactory = obtainFeignTargetFactory();

        // Builder feign
        Feign.Builder builder = new FeignContextBuilder(metricsFacade).client(client);

        // Sets request interceptor's.
        if (!requestInterceptors.isEmpty()) {
            builder.requestInterceptors(requestInterceptors);
        }

        // Sets decode404
        if (nonNull(decode404) && decode404) {
            builder.decode404();
        }

        // Sets request option
        mergeRequestOptionSet(builder);

        // Sets logger level.
        builder.logLevel(getLogLevel());

        // Sets configuration with merge.
        mergeConfigurationSet(builder);

        return builder.target(feignTargetFactory.create(config, targetClass, name, namespace, url, path));
    }

    private MetricsFacade obtainMetricsFacade() {
        if (nonNull(metricsFacade)) {
            return metricsFacade;
        }
        return (metricsFacade = applicationContext.getBean(MetricsFacade.class));
    }

    private FeignSpringBootProperties obtainFeignConfigProperties() {
        if (nonNull(config)) {
            return config;
        }
        return (config = applicationContext.getBean(FeignSpringBootProperties.class));
    }

    private Contract obtainDefaultSpringMvcContract() {
        return (defaultContract = (Contract) applicationContext
                .getBean(FeignSpringBootAutoConfiguration.BEAN_SPRINGMVC_CONTRACT));
    }

    private Client obtainFeignHttpClientInstance() {
        if (nonNull(client)) {
            return client;
        }
        try {
            client = applicationContext.getBean(BEAN_DEFAULT_FEIGN_CLIENT, Client.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("Without one of [okhttp3, Http2Client] client.");
        }
        return client;
    }

    private List<RequestInterceptor> obtainFeignRequestInterceptors() {
        if (nonNull(requestInterceptors)) {
            return requestInterceptors;
        }
        try {
            requestInterceptors = applicationContext.getBeansOfType(RequestInterceptor.class).values().stream().collect(toList());
            AnnotationAwareOrderComparator.sort(requestInterceptors);
            return requestInterceptors;
        } catch (BeansException e) {
            return emptyList();
        }
    }

    private FeignSpringBootTargetFactory obtainFeignTargetFactory() {
        List<FeignSpringBootTargetFactory> candidates = safeMap(
                applicationContext.getBeansOfType(FeignSpringBootTargetFactory.class)).values().stream().collect(toList());
        AnnotationAwareOrderComparator.sort(candidates);
        // Check target must have only one valid.
        if (candidates.isEmpty()) {
            throw new Error(format("Error, shouldn't be here, No found %s feign target factory.", Target.class.getSimpleName()));
        }
        return candidates.get(0);
    }

    private Logger.Level getLogLevel() {
        return (nonNull(logLevel) && logLevel != Logger.Level.NONE) ? logLevel : (logLevel = config.getDefaultLogLevel());
    }

    private void mergeConfigurationSet(Feign.Builder builder) throws Exception {
        List<Class<?>> mergedConfiguration = new ArrayList<>(safeArrayToList(configuration));
        mergedConfiguration.addAll(safeArrayToList(defaultConfiguration));
        Encoder encoder = null;
        Decoder decoder = null;
        Contract contract = null;
        Retryer retryer = null;
        Logger logger = null;
        for (Class<?> clazz : mergedConfiguration) {
            // If there are multiple configuration classes of the same type, the
            // first one takes effect.
            if (isNull(encoder) && Encoder.class.isAssignableFrom(clazz)) {
                encoder = (Encoder) clazz.newInstance();
            } else if (isNull(decoder) && Decoder.class.isAssignableFrom(clazz)) {
                decoder = (Decoder) clazz.newInstance();
            } else if (isNull(contract) && Contract.class.isAssignableFrom(clazz)) {
                contract = (Contract) clazz.newInstance();
            } else if (isNull(retryer) && Retryer.class.isAssignableFrom(clazz)) {
                retryer = (Retryer) clazz.newInstance();
            } else if (isNull(logger) && Logger.class.isAssignableFrom(clazz)) {
                logger = (Logger) clazz.newInstance();
            } else {
                throw new IllegalArgumentException(
                        format("Unsupported spring boot feign configuration type: %s, The supported lists are: %s, %s, %s, %s",
                                clazz, Encoder.class, Decoder.class, Contract.class, Retryer.class, Logger.class));
            }
        }
        // new GsonEncoder()
        builder.encoder(new DecorateFeignEncoder(isNull(encoder) ? defaultEncoder : encoder));
        // new GsonDecoder()
        // new ParameterizedGsonDecoder()
        builder.decoder(new DecorateFeignDecoder(isNull(decoder) ? defaultDecoder : decoder));
        // new Contract.Default()
        builder.contract(isNull(contract) ? defaultContract : contract);
        // builder.contract(new DelegateContract(isNull(contract)?new
        // SpringMvcContract():contract));
        builder.retryer(isNull(retryer) ? defaultRetryer : retryer);
        builder.logger(isNull(logger) ? defaultLogger : logger);
    }

    private void mergeRequestOptionSet(Feign.Builder builder) {
        long connectTimeout0 = (nonNull(connectTimeout) && connectTimeout > 0) ? connectTimeout : config.getConnectTimeout();
        long readTimeout0 = (nonNull(readTimeout) && readTimeout > 0) ? readTimeout : config.getReadTimeout();
        builder.options(new Options(connectTimeout0, MILLISECONDS, readTimeout0, MILLISECONDS,
                (nonNull(followRedirects) ? followRedirects : config.isFollowRedirects())));
    }

    class DecorateFeignEncoder implements Encoder {
        private final Encoder encoder;

        public DecorateFeignEncoder(Encoder encoder) {
            this.encoder = notNullOf(encoder, "encoder");
        }

        // Do nothing, Retention extension
        @Override
        public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
            encoder.encode(object, bodyType, template);
        }
    }

    /**
     * {@link feign.SynchronousMethodHandler#executeAndDecode()}
     */
    class DecorateFeignDecoder implements Decoder {
        private final Decoder decoder;
        private String feignHttpProtocol;

        public DecorateFeignDecoder(Decoder decoder) {
            this.decoder = new FeignContextDecoder(notNullOf(decoder, "decoder"));
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
            Response resp = wrapRepeatableResponse(response);
            try {
                // see:feign.jackson.JacksonDecoder#decode()
                return decoder.decode(resp, type);
            } catch (Throwable e) {
                String errmsg = format("Failed to decode feign RPC called. - ReturnType: %s\n--->>\n%s\n<<---\n%s", type,
                        printRequestAsString(response.request()), resp);
                // High concurrency performance optimizing throw exception.
                boolean dumpStackTrace = (getLogLevel() != Level.NONE);
                throw new FeignRpcException(errmsg, (log.isDebugEnabled() ? e : null), dumpStackTrace);
            } finally {
                // Actual close response.
                if (nonNull(resp.body())) {
                    ((RepeatableResponseBody) resp.body()).actualClose();
                }
            }
        }

        private Response wrapRepeatableResponse(Response response) throws IOException {
            Response.Body body = null;
            if (nonNull(response.body())) {
                body = new RepeatableResponseBody(response.body());
            }
            return Response.builder()
                    .status(response.status())
                    .reason(response.reason())
                    .request(response.request())
                    .headers(response.headers())
                    .body(body)
                    .build();
        }

        private String printRequestAsString(Request request) {
            if (request.isBinary()) {
                if (isNull(feignHttpProtocol) && nonNull(JAVA11_HTTPCLIENT_VERSION_METHOD)) {
                    Object java11HttpClientVersion = invokeMethod(JAVA11_HTTPCLIENT_VERSION_METHOD, client);
                    if (equalsIgnoreCase(valueOf(java11HttpClientVersion), "HTTP_2")) {
                        feignHttpProtocol = "HTTP/2";
                    }
                } else {
                    feignHttpProtocol = "HTTP/1.1";
                }
                return request.httpMethod()
                        .toString()
                        .concat(" ")
                        .concat(request.url())
                        .concat(" ")
                        .concat(feignHttpProtocol)
                        .concat(lineSeparator())
                        .concat("--- Binary Data ---");
            }
            return request.toString();
        }
    }

    /**
     * Delegate wrapper {@link Contract}.
     */
    @Reserved
    @Deprecated
    class DelegateFeignContract implements Contract {
        private final Contract delegate;

        public DelegateFeignContract(Contract delegate) {
            this.delegate = notNullOf(delegate, "delegate");
        }

        @Override
        public List<MethodMetadata> parseAndValidateMetadata(Class<?> targetType) {
            List<MethodMetadata> mds = delegate.parseAndValidateMetadata(targetType);
            for (MethodMetadata md : safeList(mds)) {
                md.returnType(transformParameterizedType(md.returnType()));
            }
            return mds;
        }

        /**
         * Wrap transform parameterized raw type to {@link RespBase}
         */
        private ParameterizedType transformParameterizedType(Type returnType) {
            Type[] actualTypes = { returnType };
            if (returnType instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) returnType;
                actualTypes = new Type[] { new ParameterizedType() {
                    @Override
                    public Type getRawType() {
                        return pType.getRawType();
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }

                    @Override
                    public Type[] getActualTypeArguments() {
                        return pType.getActualTypeArguments();
                    }
                } };
            }
            final Type rawType0 = RespBase.class;
            final Type[] actualTypes0 = actualTypes;
            return new ParameterizedType() {

                @Override
                public Type getRawType() {
                    return rawType0;
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }

                @Override
                public Type[] getActualTypeArguments() {
                    return actualTypes0;
                }
            };
        }
    }

    /**
     * Delegate repeatable {@link InputStream} of {@link feign.Response.Body}
     */
    class RepeatableResponseBody implements feign.Response.Body {
        private final feign.Response.Body orig;
        private final Reader reader;

        RepeatableResponseBody(@NotNull feign.Response.Body orig) throws IOException {
            this.orig = notNullOf(orig, "origBody");
            Reader r = this.orig.asReader(Util.UTF_8);
            if (!r.markSupported()) {
                // Make sure it can be read again.
                r = new BufferedReader(r, 1) {
                    @Override
                    public void close() throws IOException {
                        // Ignore defer close, see:
                        // RepeatableResponseBody#actualClose()
                    }
                };
            }
            this.reader = r;
        }

        final void actualClose() throws IOException {
            orig.close();
        }

        @Override
        public void close() throws IOException {
            // Ignore defer close, see: RepeatableResponseBody#actualClose()
        }

        @Override
        public Integer length() {
            return orig.length();
        }

        @Override
        public boolean isRepeatable() {
            return true;
        }

        @Override
        public InputStream asInputStream() throws IOException {
            return orig.asInputStream();
        }

        @Override
        public Reader asReader(Charset charset) throws IOException {
            return reader;
        }

        @Override
        public String toString() {
            if (log.isDebugEnabled()) {
                try {
                    reader.reset();
                    return CharStreams.toString(reader);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
            return super.toString();
        }
    }

    public static class FeignRpcException extends RuntimeException {
        static final long serialVersionUID = -7034833390745116939L;

        /**
         * Has been message to specify whether to log exceptions.
         * 
         * @param message
         * @param cause
         * @param dumpStackTrace
         */
        public FeignRpcException(String message, Throwable cause, boolean dumpStackTrace) {
            super(message, cause, false, dumpStackTrace);
        }
    }

    private static final Encoder defaultEncoder = new JacksonEncoder(
            new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL));
    private static final Decoder defaultDecoder = new JacksonDecoder();
    private static final Retryer defaultRetryer = new Retryer.Default();
    private static final Logger defaultLogger = new Slf4jLogger();

    private static final Method JAVA11_HTTPCLIENT_VERSION_METHOD = findMethodNullable(
            resolveClassNameNullable("java.net.http.HttpClient"), "version");

}

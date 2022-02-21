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
package com.wl4g.component.integration.springboot.feign.annotation;

import feign.Body;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.Logger.Level;
import feign.MethodMetadata;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Request.Options;
import feign.Response;
import feign.Retryer;
import feign.Target;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.StringUtils;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.CharStreams;
import com.wl4g.component.common.annotation.Reserved;
import com.wl4g.component.common.collection.CollectionUtils2;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.web.rest.RespBase;
import com.wl4g.component.integration.springboot.feign.annotation.mvc.SpringMvcContract;
import com.wl4g.component.integration.springboot.feign.config.SpringBootFeignConfigurer;
import com.wl4g.component.integration.springboot.feign.config.SpringBootFeignProperties;
import com.wl4g.component.integration.springboot.feign.context.FeignContextBinders;
import com.wl4g.component.integration.springboot.feign.context.RpcContextHolder;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmpty;
import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.lang.Assert2.hasText;
import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethod;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethodNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.component.integration.springboot.feign.config.SpringBootFeignConfigurer.BEAN_FEIGN_CLIENT;
import static com.wl4g.component.integration.springboot.feign.constant.SpringBootFeignConstant.KEY_CONFIG_PREFIX;
import static feign.Util.UTF_8;
import static feign.Util.toByteArray;
import static feign.Util.valuesOrEmpty;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

/**
 * {@link SpringBootFeignFactoryBean}
 * 
 * @param <T>
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@SuppressWarnings("unused")
class SpringBootFeignFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

	/**
	 * To be consistent with the {@link feign.slf4j.Slf4jLogger} log prefix.
	 */
	private final SmartLogger log = getLogger(feign.Logger.class);

	private ApplicationContext applicationContext;
	private SpringBootFeignProperties config;
	private Contract defaultContract;
	private Client client;
	private List<RequestInterceptor> requestInterceptors;

	@Nullable
	private Class<T> targetClass;
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

	// Fallback default configuration.
	private Class<?>[] defaultConfiguration;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setTargetClass(Class<T> targetClass) {
		this.targetClass = targetClass;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setDecode404(Boolean decode404) {
		this.decode404 = decode404;
	}

	public void setLogLevel(Logger.Level logLevel) {
		this.logLevel = logLevel;
	}

	public void setConfiguration(Class<?>[] configuration) {
		this.configuration = configuration;
	}

	public void setConnectTimeout(Long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(Long readTimeout) {
		this.readTimeout = readTimeout;
	}

	public void setWriteTimeout(Long writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	public void setFollowRedirects(Boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public void setDefaultConfiguration(Class<?>[] defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
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
		// If there is a local call service implementation instance, priority is
		// given to injection.
		T localServiceImpl = obtainLocalServiceImplIfLocal();
		if (nonNull(localServiceImpl)) {
			return localServiceImpl;
		}

		// Build feign client remote call instnace.
		return buildFeignClientServiceImpl();
	}

	/**
	 * Obtain the local call existing service instance, if there is one, it will
	 * be injected first.
	 * 
	 * @return
	 */
	@Nullable
	private T obtainLocalServiceImplIfLocal() {
		try {
			List<T> candidates = applicationContext.getBeansOfType(targetClass).values().stream()
					.filter(b -> hasAnnotation(b.getClass(), Primary.class)).filter(b -> !(b instanceof Target))
					.collect(toList());
			if (candidates.size() > 0) {
				isTrue(candidates.size() == 1, NoUniqueBeanDefinitionException.class,
						"Failed to priority inject of local serviceimpl instance, There are several primary bean candidates: ",
						candidates);
				return candidates.get(0);
			}
			return null;
		} catch (BeansException e) {
			log.warn("Failed to priority inject of local serviceimpl instance.", e.getMessage());
			return null;
		}
	}

	/**
	 * Building feign client remote call instance
	 * 
	 * @return
	 * @throws Exception
	 */
	private T buildFeignClientServiceImpl() throws Exception {
		// 注意执行顺序，不能使用InitializingBean#afterPropertiesSet()
		this.config = obtainFeignConfigProperties();
		this.defaultContract = obtainDefaultSpringMvcContract();
		this.client = obtainFeignHttpClientInstance();
		this.requestInterceptors = obtainFeignRequestInterceptors();

		// Builder feign
		Feign.Builder builder = Feign.builder().client(client);

		// Sets request interceptors.
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

		return builder.target(targetClass, buildRequestUrl());
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

	private Contract obtainDefaultSpringMvcContract() {
		return (defaultContract = (Contract) applicationContext.getBean(SpringBootFeignConfigurer.BEAN_SPRINGMVC_CONTRACT));
	}

	private SpringBootFeignProperties obtainFeignConfigProperties() {
		if (nonNull(config)) {
			return config;
		}
		return (config = applicationContext.getBean(SpringBootFeignProperties.class));
	}

	private Client obtainFeignHttpClientInstance() {
		if (nonNull(client)) {
			return client;
		}
		try {
			client = applicationContext.getBean(BEAN_FEIGN_CLIENT, Client.class);
		} catch (NoSuchBeanDefinitionException e) {
			throw new IllegalStateException("Without one of [okhttp3, Http2Client] client.");
		}
		return client;
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
		builder.encoder(new DelegateFeignEncoder(isNull(encoder) ? defaultEncoder : encoder));
		// new GsonDecoder()
		// new ParameterizedGsonDecoder()
		builder.decoder(new DelegateFeignDecoder(isNull(decoder) ? defaultDecoder : decoder));
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

	private String buildRequestUrl() {
		String url = trimToEmpty(isBlank(this.url) ? config.getDefaultUrl() : this.url);
		hasText(url, "Feign base url is required, please check configuration: %s.defaultUrl or use @%s#url()", KEY_CONFIG_PREFIX,
				SpringBootFeignClient.class.getSimpleName());
		return url + cleanPath();
	}

	private String cleanPath() {
		String path = trimToEmpty(this.path);
		if (StringUtils.hasLength(path)) {
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
		}
		return path;
	}

	class DelegateFeignEncoder implements Encoder {
		private final Encoder encoder;

		public DelegateFeignEncoder(Encoder encoder) {
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
	class DelegateFeignDecoder implements Decoder {
		private final Decoder decoder;
		private String feignHttpProtocol;

		public DelegateFeignDecoder(Decoder decoder) {
			this.decoder = notNullOf(decoder, "decoder");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
			Response wrapResponse = wrapRepeatableResponseIfNecessary(response);
			try {
				return decoder.decode(wrapResponse, type);
			} catch (Exception e) {
				String errmsg = format("Failed to decode feign RPC called. - ReturnType: %s\n----->>>\n%s\n<<<-----\n%s", type,
						printRequestAsString(response.request()), wrapResponse);
				// High concurrency performance optimizing throw exception.
				boolean dumpStackTrace = (getLogLevel() != Level.NONE);
				throw new FeignRpcException(errmsg, (log.isDebugEnabled() ? e : null), true);
			} finally {
				// The RPC call has responded and the attachment info should be
				// extracted from it.
				try {
					FeignContextBinders.bindFeignResposneAttachmentsToContext(wrapResponse);
				} catch (Exception e2) {
					log.warn("Cannot bind feign response attachments to current RpcContext", e2);
				}
				// Actual close response.
				((RepeatableResponseBody) wrapResponse.body()).actualClose();
			}
		}

		private Response wrapRepeatableResponseIfNecessary(Response response) throws IOException {
			// Wrap response.
			return Response.builder().status(response.status()).reason(response.reason()).request(response.request())
					.headers(response.headers()).body(new RepeatableResponseBody(response.body())).build();
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
				return request.httpMethod().toString().concat(" ").concat(request.url()).concat(" ").concat(feignHttpProtocol)
						.concat(lineSeparator()).concat("--- Binary Data ---");
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

		RepeatableResponseBody(feign.Response.Body orig) throws IOException {
			this.orig = notNullOf(orig, "originalBody");
			Reader reader0 = orig.asReader(Util.UTF_8);
			if (!reader0.markSupported()) {
				reader0 = new BufferedReader(reader0, 1) {
					@Override
					public void close() throws IOException {
						// Ignore defer close, see:
						// RepeatableResponseBody#actualClose()
					}
				};
			}
			this.reader = reader0;
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

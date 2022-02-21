/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.component.integration.feign.springcloud.context;

import static feign.form.ContentType.MULTIPART;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
import org.springframework.cloud.openfeign.DefaultFeignLoggerFactory;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.cloud.openfeign.FeignLoggerFactory;
import org.springframework.cloud.openfeign.clientconfig.FeignClientConfigurer;
import org.springframework.cloud.openfeign.support.AbstractFormWriter;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.PageableSpringEncoder;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.fasterxml.jackson.databind.Module;
import com.netflix.hystrix.HystrixCommand;
import com.wl4g.component.integration.feign.core.context.internal.ConsumerFeignContextFilter.FeignContextDecoder;
import com.wl4g.component.integration.feign.core.context.internal.FeignContextBuilder;
import com.wl4g.component.integration.feign.springcloud.config.EnhanceSpringCloudFeignAutoConfiguration;

import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.MultipartFormContentProcessor;
import feign.form.spring.SpringFormEncoder;
import feign.hystrix.HystrixFeign;
import feign.optionals.OptionalDecoder;

/**
 * {@link FeignContextClientsAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-28
 * @sine v1.0
 * @see
 */
@AutoConfigureAfter(EnhanceSpringCloudFeignAutoConfiguration.class)
@AutoConfigureBefore(FeignClientsConfiguration.class)
public class FeignContextClientsAutoConfiguration {

	@Autowired
	private ObjectFactory<HttpMessageConverters> messageConverters;

	@Autowired(required = false)
	private List<AnnotatedParameterProcessor> parameterProcessors = new ArrayList<>();

	@Autowired(required = false)
	private List<FeignFormatterRegistrar> feignFormatterRegistrars = new ArrayList<>();

	@Autowired(required = false)
	private Logger logger;

	@Autowired(required = false)
	private SpringDataWebProperties springDataWebProperties;

	//
	// Override springcloud default SpringDecoder
	//

	// @Bean
	// @ConditionalOnMissingBean
	// public Decoder feignDecoder() {
	// return new OptionalDecoder(new ResponseEntityDecoder(new
	// SpringDecoder(this.messageConverters)));
	// }

	@Bean
	@Primary
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
		return new FeignContextDecoder(new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(messageConverters))));
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnMissingClass("org.springframework.data.domain.Pageable")
	public Encoder feignEncoder(ObjectProvider<AbstractFormWriter> formWriterProvider) {
		return springEncoder(formWriterProvider);
	}

	@Bean
	@ConditionalOnClass(name = "org.springframework.data.domain.Pageable")
	@ConditionalOnMissingBean
	public Encoder feignEncoderPageable(ObjectProvider<AbstractFormWriter> formWriterProvider) {
		PageableSpringEncoder encoder = new PageableSpringEncoder(springEncoder(formWriterProvider));

		if (springDataWebProperties != null) {
			encoder.setPageParameter(springDataWebProperties.getPageable().getPageParameter());
			encoder.setSizeParameter(springDataWebProperties.getPageable().getSizeParameter());
			encoder.setSortParameter(springDataWebProperties.getSort().getSortParameter());
		}
		return encoder;
	}

	@Bean
	@ConditionalOnMissingBean
	public Contract feignContract(ConversionService feignConversionService) {
		return new SpringMvcContract(this.parameterProcessors, feignConversionService);
	}

	@Bean
	public FormattingConversionService feignConversionService() {
		FormattingConversionService conversionService = new DefaultFormattingConversionService();
		for (FeignFormatterRegistrar feignFormatterRegistrar : this.feignFormatterRegistrars) {
			feignFormatterRegistrar.registerFormatters(conversionService);
		}
		return conversionService;
	}

	@Bean
	@ConditionalOnMissingBean
	public Retryer feignRetryer() {
		return Retryer.NEVER_RETRY;
	}

	//
	// Override springcloud default FeignBuilder
	//

	// @Bean
	// @Scope("prototype")
	// @ConditionalOnMissingBean
	// public Feign.Builder feignBuilder(Retryer retryer) {
	// return Feign.builder().retryer(retryer);
	// }

	@Bean
	@Scope("prototype")
	@ConditionalOnMissingBean
	public Feign.Builder feignBuilder(Retryer retryer) {
		return new FeignContextBuilder().retryer(retryer);
	}

	@Bean
	@ConditionalOnMissingBean(FeignLoggerFactory.class)
	public FeignLoggerFactory feignLoggerFactory() {
		return new DefaultFeignLoggerFactory(this.logger);
	}

	@Bean
	@ConditionalOnClass(name = "org.springframework.data.domain.Page")
	public Module pageJacksonModule() {
		return new PageJacksonModule();
	}

	@Bean
	@ConditionalOnClass(name = "org.springframework.data.domain.Page")
	public Module sortModule() {
		return new SortJacksonModule();
	}

	@Bean
	@ConditionalOnMissingBean(FeignClientConfigurer.class)
	public FeignClientConfigurer feignClientConfigurer() {
		return new FeignClientConfigurer() {
		};
	}

	private Encoder springEncoder(ObjectProvider<AbstractFormWriter> formWriterProvider) {
		AbstractFormWriter formWriter = formWriterProvider.getIfAvailable();
		if (formWriter != null) {
			return new SpringEncoder(new SpringPojoFormEncoder(formWriter), this.messageConverters);
		} else {
			return new SpringEncoder(new SpringFormEncoder(), this.messageConverters);
		}
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ HystrixCommand.class, HystrixFeign.class })
	protected static class HystrixFeignConfiguration {

		@Bean
		@Scope("prototype")
		@ConditionalOnMissingBean
		@ConditionalOnProperty(name = "feign.hystrix.enabled")
		public Feign.Builder feignHystrixBuilder() {
			return HystrixFeign.builder();
		}

	}

	private class SpringPojoFormEncoder extends SpringFormEncoder {

		SpringPojoFormEncoder(AbstractFormWriter formWriter) {
			super();
			MultipartFormContentProcessor processor = (MultipartFormContentProcessor) getContentProcessor(MULTIPART);
			processor.addFirstWriter(formWriter);
		}

	}

}

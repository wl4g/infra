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
package com.wl4g.component.core.web.error;

import com.wl4g.component.core.web.mapping.PrefixHandlerMappingSupport;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.serialize.JacksonUtils.convertBean;
import static com.wl4g.component.core.constant.CoreConfigConstant.KEY_WEB_GLOBAL_ERROR;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

/**
 * Global error controller handler auto configuration.
 * 
 * @author wangl.sir
 * @version v1.0 2019年1月10日
 * @since
 */
public abstract class AbstractErrorAutoConfiguration extends PrefixHandlerMappingSupport {

	@Bean
	@ConfigurationProperties(prefix = KEY_WEB_GLOBAL_ERROR)
	public ErrorHandlerProperties errorHandlerProperties() {
		return new ErrorHandlerProperties();
	}

	@Bean
	public DefaultErrorConfigurer defaultErrorConfigurer(ErrorHandlerProperties config) {
		return new DefaultErrorConfigurer(config);
	}

	@Bean
	public CompositeErrorConfigurer compositeErrorConfigurer(ErrorHandlerProperties config, List<ErrorConfigurer> configures) {
		return new CompositeErrorConfigurer(config, configures);
	}

	@Bean
	public Object errorHandlerPrefixHandlerMapping() {
		return super.newPrefixHandlerMapping("/", ErrorController.class);
	}

	/**
	 * Error handler controller properties.</br>
	 * <font color=red>Note: When {@link ConfigurationProperties} is used, the
	 * field name cannot contain numbers, otherwise</font>
	 * 
	 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0.0 2019-11-02
	 * @since
	 */
	public static class ErrorHandlerProperties implements InitializingBean {

		/**
		 * Default error view configuration base path.
		 */
		private String basePath = DEFAULT_ERROR_VIEW_PATH;

		/**
		 * Error rendering mapping.
		 */
		private Map<Integer, String> renderingMapping = new HashMap<Integer, String>() {
			private static final long serialVersionUID = 551980985208402881L;
			{
				put(403, DEFAULT_TPL_403_NAME);
				put(404, DEFAULT_TPL_404_NAME);
				put(500, DEFAULT_TPL_50X_NAME);
				put(501, DEFAULT_TPL_50X_NAME);
				put(502, DEFAULT_TPL_50X_NAME);
				put(503, DEFAULT_TPL_50X_NAME);
			}
		};

		/**
		 * Error return previous page URI.</br>
		 * Default for browser location origin.
		 */
		private String homeUri = "javascript:location.href=location.origin";

		// --- Temporary. ---

		/**
		 * This serialized to map.
		 */
		private transient Map<String, Object> asMap;

		public String getBasePath() {
			return basePath;
		}

		public void setBasePath(String basePath) {
			this.basePath = hasTextOf(basePath, "basePath");
		}

		public Map<Integer, String> getRenderingMapping() {
			return renderingMapping;
		}

		public void setRenderingMapping(Map<Integer, String> renderingMapping) {
			this.renderingMapping = renderingMapping;
		}

		public String getHomeUri() {
			return homeUri;
		}

		public void setHomeUri(String homeUri) {
			this.homeUri = hasTextOf(homeUri, "homeUri");
		}

		// --- Function's. ---

		@SuppressWarnings("unchecked")
		@Override
		public void afterPropertiesSet() throws Exception {
			this.asMap = convertBean(this, HashMap.class);
		}

		/**
		 * Convert bean to {@link Map} properties.
		 * 
		 * @return
		 */
		public Map<String, Object> asMap() {
			return this.asMap;
		}

		public static final String DEFAULT_ERROR_VIEW_PATH = "/default-error-view/";

		/**
		 * {@link HttpStatus#NOT_FOUND}
		 */
		public static final String DEFAULT_TPL_404_NAME = "404.tpl.html";

		/**
		 * {@link HttpStatus#FORBIDDEN}
		 */
		public static final String DEFAULT_TPL_403_NAME = "403.tpl.html";

		/**
		 * {@link HttpStatus#SERVICE_UNAVAILABLE}
		 */
		public static final String DEFAULT_TPL_50X_NAME = "50x.tpl.html";
	}

	/**
	 * {@link ErrorController}
	 *
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2020-09-09
	 * @since
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@Documented
	public static @interface ErrorController {
	}

}
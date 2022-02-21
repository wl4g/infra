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
package com.wl4g.component.core.web.embed;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wl4g.component.core.web.mapping.PrefixHandlerMappingSupport;

import static com.wl4g.component.core.constant.CoreConfigConstant.KEY_WEB_EMBEDDED_WEBAPP;

import java.util.Properties;

/**
 * Embedded webapps site configuration.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年2月20日
 * @since
 */
@Configuration
@ConditionalOnProperty(value = KEY_WEB_EMBEDDED_WEBAPP + ".enabled", matchIfMissing = false)
public class EmbedWebappAutoConfiguration extends PrefixHandlerMappingSupport {

	@Bean(BEAN_DEFAULT_PROPERTIES)
	@ConfigurationProperties(prefix = KEY_WEB_EMBEDDED_WEBAPP)
	public SimpleEmbedWebappProperties defaultEmbedWebappEndpointProperties() {
		return new SimpleEmbedWebappProperties();
	}

	@Bean(BEAN_DEFAULT_ENDPOINT)
	@ConditionalOnBean(name = BEAN_DEFAULT_PROPERTIES)
	public SimpleEmbedWebappEndpoint simpleEmbedWebappsEndpoint(
			@Qualifier(BEAN_DEFAULT_PROPERTIES) SimpleEmbedWebappProperties config) {
		return new SimpleEmbedWebappEndpoint(config);
	}

	@Bean
	@ConditionalOnBean(name = BEAN_DEFAULT_PROPERTIES)
	public Object simpleEmbedWebappEndpointPrefixHandlerMapping(
			@Qualifier(BEAN_DEFAULT_PROPERTIES) SimpleEmbedWebappProperties config,
			@Qualifier(BEAN_DEFAULT_ENDPOINT) SimpleEmbedWebappEndpoint endpoint) {
		return super.newPrefixHandlerMapping(config.getBaseUri(), endpoint);
	}

	/**
	 * {@link SimpleEmbedWebappProperties}
	 * 
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2020年2月20日
	 * @since
	 */
	public static class SimpleEmbedWebappProperties {

		/**
		 * Basic controller mapping access URI of default web application
		 */
		private String baseUri = "/default-view";

		/**
		 * The static file publishing directory of the default web application,
		 * such as: classpath*:/default-webapps
		 */
		private String webappLocation = "classpath*:/default-webapps";

		/**
		 * Media mapping
		 */
		private Properties mimeMapping = new Properties() {
			private static final long serialVersionUID = 6601944358361144649L;
			{
				put("html", "text/html");
				put("shtml", "text/html");
				put("htm", "text/html");
				put("css", "text/css");
				put("js", "application/javascript");

				put("icon", "image/icon");
				put("ico", "image/icon");
				put("gif", "image/gif");
				put("jpg", "image/jpeg");
				put("jpeg", "image/jpeg");
				put("png", "image/png");
				put("bmp", "image/jpeg");
				put("svg", "image/svg");

				put("json", "application/json");
				put("xml", "application/xml");

				put("doc", "application/msword");
				put("dot", "application/msword");
				put("docx", "  application/vnd.openxmlformats-officedocument.wordprocessingml.document");
				put("dotx", "  application/vnd.openxmlformats-officedocument.wordprocessingml.template");
				put("docm", "  application/vnd.ms-word.document.macroEnabled.12");
				put("dotm", "  application/vnd.ms-word.template.macroEnabled.12");

				put("xls", "application/vnd.ms-excel");
				put("xlt", "application/vnd.ms-excel");
				put("xla", "application/vnd.ms-excel");

				put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
				put("xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
				put("xltm", "application/vnd.ms-excel.template.macroEnabled.12");
				put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
				put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");

				put("ppt", "application/vnd.ms-powerpoint");
				put("pot", "application/vnd.ms-powerpoint");
				put("pps", "application/vnd.ms-powerpoint");
				put("ppa", "application/vnd.ms-powerpoint");

				put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
				put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
				put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
				put("ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
				put("pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
				put("potm", "application/vnd.ms-powerpoint.template.macroEnabled.12");
				put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");

				put("mdb", "application/vnd.ms-access");
			}
		};

		public SimpleEmbedWebappProperties() {
			super();
		}

		public SimpleEmbedWebappProperties(String baseUri, String webappLocation) {
			setBaseUri(baseUri);
			setWebappLocation(webappLocation);
		}

		public String getBaseUri() {
			return baseUri;
		}

		public void setBaseUri(String baseUri) {
			this.baseUri = baseUri;
		}

		public String getWebappLocation() {
			return webappLocation;
		}

		public void setWebappLocation(String webappLocation) {
			this.webappLocation = webappLocation;
		}

		public Properties getMimeMapping() {
			return mimeMapping;
		}

		public void setMimeMapping(Properties mimeMapping) {
			this.mimeMapping = mimeMapping;
		}

	}

	public static final String BEAN_DEFAULT_PROPERTIES = "defaultSimpleEmbeddedWebappsProperties";
	public static final String BEAN_DEFAULT_ENDPOINT = "defaultSimpleEmbeddedWebappsEndpoint";

}
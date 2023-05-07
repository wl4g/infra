/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
package com.wl4g.infra.core.web.versions;

import static java.lang.String.format;
import static java.lang.System.out;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.wl4g.infra.context.boot.BootstrappingAutoConfiguration;
import com.wl4g.infra.core.web.versions.annotation.ApiVersion;
import com.wl4g.infra.core.web.versions.annotation.ApiVersionMapping;
import com.wl4g.infra.core.web.versions.annotation.EnableApiVersionManagement;

/**
 * {@link ApiVersionControllerTests}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2020-12-01
 * @since v2.0
 * @see https://spring.io/guides/gs/testing-web/
 */
@EnableApiVersionManagement("com.wl4g.infra.core.web.versions")
@SpringBootApplication(exclude = { BootstrappingAutoConfiguration.class })
public class ApiVersionControllerTests {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ApiVersionControllerTests.class, args);

		checkApiVersionsMatchsCase1();

		System.exit(0);
	}

	/**
	 * <pre>
	 * Version info parameter alias name includes: eg: '_v' or 'version' or 'apiVersion';
	 * 
	 * Client info parameter alias name includes, eg: 'platform' or 'clientType'
	 * 
	 * refer to {@link EnableApiVersionManagement}
	 * </pre>
	 */
	private static void checkApiVersionsMatchsCase1() {
		System.out.println("----------- Testing ------------");

		RestTemplate rest = new RestTemplate();
		String url = "http://localhost:8080/api-test/userinfo?_v=%s&platform=%s&%s=%s";

		// Positive example:
		out.println(">> " + rest.getForObject(format(url, "1.0.10.3a", "wechatmp", "token", "abcd333"), String.class));// V1
		out.println(">> " + rest.getForObject(format(url, "1.0.10.2a", "iOS", "token", "abcd111"), String.class));// V1
		out.println(">> " + rest.getForObject(format(url, "1.0.10.2b", "Android", "token", "abcd555"), String.class));// V2
		out.println(">> " + rest.getForObject(format(url, "1.0", "", "token", "abcd666"), String.class));// default
		out.println(">> " + rest.getForObject(format(url, "", "", "token", "abcd666"), String.class));// default

		// Negative example:
		out.println(">> " + rest.getForObject(format(url, "1.0.10.3a", "iOS", "token", "abcd222"), String.class));// V1
		out.println(">> " + rest.getForObject(format(url, "1.0.10.2b", "iOS", "token", "abcd555"), String.class));// V1

	}

	@RestController
	@RequestMapping("/api-test/")
	public static class TestApiVersionController {

		@ApiVersionMapping({ @ApiVersion(groups = { "iOS", "wechatmp" }, value = "1.0.10.2a"),
				@ApiVersion(groups = { "WebPC", "wechatmp" }, value = "1.0.10.3a") })
		@RequestMapping("userinfo")
		public String getUserInfo20200901(String token) {
			return "I'am API, version is V1 - token=" + token;
		}

		@ApiVersionMapping({ @ApiVersion(groups = { "NativePC", "Android" }, value = "1.0.10.2b"),
				@ApiVersion(groups = { "iOS" }, value = "1.0.10.1a") })
		@RequestMapping("userinfo")
		public String getUserInfo202010905(String token) {
			return "I'am API, version is V2 - token=" + token;
		}

		// Default mapping (Low priority)
		@RequestMapping("userinfo")
		public String getUserInfo(String token) {
			return "I'am API, version is default - token=" + token;
		}

	}

}

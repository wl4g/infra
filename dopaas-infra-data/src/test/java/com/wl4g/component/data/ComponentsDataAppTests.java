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
package com.wl4g.component.data;

import static java.lang.System.out;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.wl4g.component.data.annotation.EnableComponentDataConfiguration;
import com.wl4g.component.data.sample.dao.SampleDao;

/**
 * {@link ComponentsDataAppTests}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-07
 * @since
 */
@EnableComponentDataConfiguration
@MapperScan("com.wl4g.component.data.sample.dao.*")
@SpringBootApplication(scanBasePackages = { "com" })
public class ComponentsDataAppTests implements InitializingBean {

	public static void main(String[] args) {
		SpringApplication.run(ComponentsDataAppTests.class, args);
	}

	@Autowired
	private SampleDao sampleDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		out.println("Query sample bean from database ....");
		out.println(sampleDao.getSample());
	}

}

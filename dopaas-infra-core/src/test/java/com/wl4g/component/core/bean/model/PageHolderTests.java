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
package com.wl4g.component.core.bean.model;

import static java.lang.System.out;

import org.junit.Test;

import com.github.pagehelper.Page;
import com.wl4g.component.core.page.PageHolder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link PageHolderTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-16
 * @sine v1.0
 * @see
 */
public class PageHolderTests {

	@Test
	public void copyPaginationCase() {
		PageHolder<MyUser> pm = new PageHolder<>();
		Page<MyUser> page = new Page<>();
		page.add(new MyUser("zs", 18));
		page.setPageSize(15); // current page row count
		page.setPageNum(3); // current page index
		page.setPages(9); // total page count
		page.setTotal(123); // total row count

		out.println(" -> pm.pageSize: " + pm.getPageSize());
		out.println(" -> pm.pageNum: " + pm.getPageNum());
		out.println(" -> pm.pageTotal: " + pm.getTotal());
		out.println(" -> pm.pages: " + pm.getPages());
		out.println(" -> pm.records: " + pm.getRecords());
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MyUser {
		private String name;
		private int age;
	}

}

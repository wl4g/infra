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
package com.wl4g.component.common.excel;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.SystemUtils.USER_DIR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Font;
import org.junit.Test;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.CellExtra;
import com.wl4g.component.common.io.FileIOUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link EasyExcelServicesTests}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-22
 * @since
 */
public class EasyExcelServicesTests {

	@Test
	public void simpleReadCase1() {
		EasyExcelServices.createDefault().read(new File(READ_EXCEL_FILE), new ReadExcelListener());
	}

	@Test
	public void simpleWriteCase1() throws IOException {
		List<Student> dataset = new ArrayList<Student>();
		dataset.add(new Student("张三丰", 18, "古国1"));
		dataset.add(new Student("张二丰", 11, "远古之城x"));
		dataset.add(new Student("张一丰", 22, "USA"));
		File file = File.createTempFile("simpleWriteEasyExcel", ".xlsx");
		FileIOUtils.ensureFile(file);
		EasyExcelServices.createDefault().write(file, dataset,
				new CellColorSheetWriteHandler(singletonList(1), singletonList(1), Font.COLOR_RED));

		System.out.println("Writed for " + file);
	}

	@Getter
	@Setter
	@ToString
	@ColumnWidth(40)
	public static class Student {

		@ExcelProperty(value = { "姓名" })
		private String name;

		@ExcelProperty(value = { "年齡" })
		private Integer age;

		@ExcelProperty(value = { "住址" })
		private String address;

		public Student() {
			super();
		}

		public Student(String name, Integer age, String address) {
			super();
			this.name = name;
			this.age = age;
			this.address = address;
		}

	}

	public static class ReadExcelListener extends AnalysisEventListener<Student> {

		// The invoke method is called every time a row is parsed
		@Override
		public void invoke(Student data, AnalysisContext context) {
			System.out.println("invoke: " + data + ", context: " + context);
		}

		@SuppressWarnings({ "rawtypes" })
		@Override
		public void invokeHead(Map<Integer, CellData> headMap, AnalysisContext context) {
			System.out.println("invokeHead: " + headMap + ", context: " + context);
		}

		@Override
		public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
			System.out.println("headMap: " + headMap + ", context: " + context);
		}

		@Override
		public void extra(CellExtra extra, AnalysisContext context) {
			System.out.println("extra: " + extra + ", context: " + context);
		}

		@Override
		public void onException(Exception exception, AnalysisContext context) throws Exception {
			System.out.println("onException context: " + context);
			exception.printStackTrace();
		}

		@Override
		public boolean hasNext(AnalysisContext context) {
			System.out.println("hasNext context: " + context);
			return super.hasNext(context);
		}

		// Call automatically after parsing
		@Override
		public void doAfterAllAnalysed(AnalysisContext context) {
			System.out.println("read finshed. context: " + context);

		}

	}

	static String READ_EXCEL_FILE = USER_DIR + File.separator + "src" + File.separator
			+ "test" + File.separator + "java" + File.separator + EasyExcelServicesTests.class.getName()
					.replace(".", File.separator).replace(EasyExcelServicesTests.class.getSimpleName(), "")
			+ File.separator + "easyexcel_example.xlsx";

}

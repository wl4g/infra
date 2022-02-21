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

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.reflect.ResolvableType;

/**
 * {@link EasyExcelServices}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-09-22
 * @since
 */
public abstract class EasyExcelServices {

	protected final SmartLogger log = getLogger(getClass());

	private EasyExcelServices() {
	}

	/**
	 * Create {@link EasyExcelServices} instance with default settings.
	 * 
	 * @return
	 */
	public static EasyExcelServices createDefault() {
		EasyExcelServices instance = new EasyExcelServices() {
		};
		return instance;
	}

	/**
	 * Read dataset from excel file with default configuration.
	 * 
	 * @param readfile
	 * @param listener
	 * @return
	 */
	public <T> ExcelReader read(File readfile, ReadListener<T> listener) {
		try (InputStream in = new FileInputStream(readfile);) {
			ExcelReader reader = EasyExcelFactory.read(in, listener).build();

			ReadSheet sheet = new ReadSheet(1, "Sheet1");
			Class<?> beanClass = ResolvableType.forClass(listener.getClass()).getSuperType().getGeneric(0).resolve();
			sheet.setClazz(beanClass);
			sheet.setAutoTrim(true);
			sheet.setLocale(Locale.getDefault());
			sheet.setHeadRowNumber(1); // Default
			// sheet.setCustomConverterList(null);

			return reader.read(sheet);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Writing dataset to excel file with default configuration.
	 * 
	 * @param writefile
	 * @param dataset
	 */
	public <T> void write(File writefile, List<T> dataset, CellWriteHandler handler) {
		try (OutputStream out = new FileOutputStream(writefile);) {

			Class<?> beanClass = dataset.get(0).getClass();
			ExcelWriter writer = EasyExcelFactory.write(out).autoTrim(true).registerWriteHandler(handler).head(beanClass).build();

			// 2代表sheetNo,不可以重复,如果两个sheet的sheetNo相同则输出时只会有一个sheet
			WriteSheet sheet1 = new WriteSheet();
			sheet1.setSheetName("Sheet1");

			writer.write(asList(dataset), sheet1);
			writer.finish();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}

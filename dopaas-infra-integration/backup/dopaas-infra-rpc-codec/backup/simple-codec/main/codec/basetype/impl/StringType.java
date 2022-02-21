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
package com.wl4g.component.integration.codec.basetype.impl;

import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Decoder;
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.basetype.BaseType;
import com.wl4g.component.integration.codec.helper.ByteHelper;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;

/**
 * 
 * StringType.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
public class StringType extends BaseType {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void encode(Encoder encoder, BytesOutputStream out, Object obj, CodecParameter param) throws Exception {
		String s = (String) obj;
		byte[] bytes = ByteHelper.convertString(s, param.getEncoding());
		writeAutoLength(out, bytes.length, param);
		out.write(bytes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object decode(Decoder decoder, BytesInputStream in, Object obj, CodecParameter param) throws Exception {
		int len = in.available();
		if (len <= 0) {
			return null;
		}
		len = readAutoLength(in, param);
		int ioLen = in.available();
		len = (len < 0 || ioLen < len) ? ioLen : len;
		byte[] bytes = ByteHelper.readBytes(in, len, false);
		if (bytes == null) {
			return null;
		}
		return ByteHelper.convertToString(bytes, param.getEncoding());
	}

}
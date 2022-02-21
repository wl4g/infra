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
package com.wl4g.component.integration.codec.type;

import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Decoder;
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.annotations.CodecType;
import com.wl4g.component.integration.codec.helper.ByteHelper;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;

/**
 * Just like {@linkplain java.lang.String String}.
 * 
 * OCString.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
@CodecType
public class OCString extends OCBaseType<String> {

	public OCString() {
		super();
	}

	public OCString(String value, int length) {
		super(value, length);
	}

	public OCString(String value) {
		super(value);
	}

	public OCString(OCInteger lenType) {
		super(lenType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeObject(Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		writeAutoLength(encoder, out, param);
		byte[] bytes = ByteHelper.convertString(getValue(), param.getEncoding());
		super.writeBytes(encoder, out, bytes, param);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readObject(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		readAutoLength(decoder, in, param);
		byte[] bytes = null;
		if (!isDynamicLength()) {
			int len = in.available();
			setLength(len);
			bytes = ByteHelper.readBytes(in, len, false);
		} else {
			int len = getLenType().getValue();
			setLength(len);
			bytes = ByteHelper.readBytes(in, len, false);
		}
		setValue(ByteHelper.convertToString(bytes, param.getEncoding()));
	}
}
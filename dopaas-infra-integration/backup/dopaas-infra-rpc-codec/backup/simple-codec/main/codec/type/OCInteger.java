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
import com.wl4g.component.integration.codec.exception.EncodingException;
import com.wl4g.component.integration.codec.helper.ByteHelper;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;

/**
 * Integer base type.
 * 
 * OCInteger.java
 * 
 * @see OCBaseType
 * @version 1.0.0
 * @author Wanglsir
 */
@CodecType
public class OCInteger extends OCBaseType<Integer> {

	public static final int BIT8_LEN = 1;

	public static final int BIT16_LEN = 2;

	public static final int BIT32_LEN = 4;

	public OCInteger() {

	}

	/**
	 * Construct integer by initialize value.
	 * 
	 * @param val
	 *            Integer value.
	 */
	public OCInteger(int val) {
		super(val);
	}

	/**
	 * Construct integer by initialize value.
	 * 
	 * @param val
	 *            Integer value.
	 * @param len
	 *            bits length..
	 */
	public OCInteger(int val, int len) {
		super(val, len);
	}

	/**
	 * Construct integer object by length type object.
	 * 
	 * @param lenType
	 *            Length type
	 */
	public OCInteger(OCInteger lenType) {
		super(lenType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeObject(Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		byte[] bytes = getBytes(param.isLittleEndian());
		if (bytes == null) {
			throw new EncodingException("Fail to encode " + getClass());
		}
		super.writeBytes(encoder, out, bytes, param);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readObject(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		byte[] bytes = ByteHelper.readBytes(in, getLength(), param.isLittleEndian());

		int len = getLength();
		switch (len) {
		case BIT8_LEN:
			setValue(ByteHelper.convertToInt8(bytes));
			break;
		case BIT16_LEN:
			setValue(ByteHelper.convertToInt16(bytes));
			break;
		case BIT32_LEN:
			setValue(ByteHelper.convertToInt32(bytes));
			break;
		}
	}

	public byte[] getBytes(boolean littleEndian) {
		byte[] bytes = null;
		int len = getLength();
		switch (len) {
		case BIT8_LEN:
			bytes = ByteHelper.convertInt8(getValue(0));
			break;
		case BIT16_LEN:
			bytes = ByteHelper.convertInt16(getValue(0), littleEndian);
			break;
		case BIT32_LEN:
			bytes = ByteHelper.convertInt32(getValue(0), littleEndian);
			break;
		}
		return bytes;
	}

}
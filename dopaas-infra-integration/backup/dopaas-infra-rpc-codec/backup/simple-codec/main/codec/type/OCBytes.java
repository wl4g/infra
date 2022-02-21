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

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Decoder;
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.annotations.CodecType;
import com.wl4g.component.integration.codec.exception.DecodingException;
import com.wl4g.component.integration.codec.helper.ByteHelper;
import com.wl4g.component.integration.codec.helper.StringHelper;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;

import java.io.UnsupportedEncodingException;

/**
 * Instead of Java bytes arrays.
 * 
 * OCBytes.java
 * 
 * @see CodecType
 * @version 1.0.0
 * @author Wanglsir
 */
@CodecType
public class OCBytes extends OCBaseType<byte[]> {

	private static final SmartLogger log = getLogger(OCBytes.class);

	public OCBytes() {

	}

	/**
	 * Construct bytes object by length type object.
	 * 
	 * @param lenType
	 *            Length type.
	 */
	public OCBytes(OCInteger lenType) {
		super(lenType);
	}

	/**
	 * Construct bytes object by bytes arrays.
	 * 
	 * @param bytes
	 *            Bytes arrays.
	 */
	public OCBytes(byte[] bytes) {
		super(bytes);
		setLength(bytes.length);
	}

	/**
	 * Construct bytes object by bytes arrays and length type object.
	 * 
	 * @param bytes
	 *            Bytes arrays.
	 * @param lenType
	 *            Length type object.
	 */
	public OCBytes(byte[] bytes, OCInteger lenType) {
		super(bytes, lenType);
	}

	/**
	 * Construct bytes object by bytes arrays.
	 * 
	 * @param bytes
	 *            Bytes arrays.
	 * @param len
	 *            Bytes arrays length.
	 */
	public OCBytes(byte[] bytes, int len) {
		super(bytes, len);
	}

	/**
	 * Construct bytes object by bytes arrays length. Developer can use it in
	 * decoding.
	 * 
	 * @param len
	 *            Bytes arrays length.
	 */
	public OCBytes(int len) {
		super(null, len);
	}

	public static OCBytes valueOf(byte v) {
		return new OCBytes(ByteHelper.convertInt8(v));
	}

	public static OCBytes valueOf(short v, boolean littleEndian) {
		return new OCBytes(ByteHelper.convertInt16(v, littleEndian));
	}

	public static OCBytes valueOf(int v, boolean littleEndian) {
		return new OCBytes(ByteHelper.convertInt32(v, littleEndian));
	}

	public static OCBytes valueOf(String s) {
		return new OCBytes(s.getBytes());
	}

	public static OCBytes valueOf(String s, String encoding) {
		try {
			return new OCBytes(s.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static OCBytes valueOf(byte[] bytes) {
		return new OCBytes(bytes);
	}

	public void bytes(byte[] bytes) {
		setValue(bytes);
		setLength(bytes.length);
	}

	public void int8(int v) {
		bytes(ByteHelper.convertInt8(v));
	}

	public void int16(int v, boolean littleEndian) {
		bytes(ByteHelper.convertInt16(v, littleEndian));
	}

	public void int32(int v, boolean littleEndian) {
		bytes(ByteHelper.convertInt32(v, littleEndian));
	}

	public void string(String s) {
		bytes(s.getBytes());
	}

	public void string(String s, String encoding) {
		try {
			bytes(s.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
	}

	public String getString() {
		return new String(getValue());
	}

	public String getString(String encoding) {
		try {
			return new String(getValue(), encoding);
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public int getInt8() {
		return ByteHelper.convertToInt8(getValue());
	}

	public int getInt16(boolean littleEndian) {
		return ByteHelper.convertToInt16(getValue(), littleEndian);
	}

	public int getInt32(boolean littleEndian) {
		return ByteHelper.convertToInt32(getValue(), littleEndian);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeObject(Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		if (getValue() == null) {
			return;
		}
		writeAutoLength(encoder, out, param);
		byte[] bytes = getValue(new byte[0]);
		super.writeBytes(encoder, out, bytes, param);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void readObject(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		readAutoLength(decoder, in, param);
		if (isDynamicLength()) {
			readDynamicLengthObject(decoder, in, param);
		} else if (getLength() >= 0) {
			readSpecifyLengthObject(decoder, in, param);
		} else {
			readAvailableLengthObject(decoder, in, param);
		}
	}

	private void readDynamicLengthObject(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		int length = getLenType().getValue();
		setLength(length);
		readSpecifyLengthObject(decoder, in, param);
	}

	private void readAvailableLengthObject(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		int len = in.available();
		setLength(len);
		readSpecifyLengthObject(decoder, in, param);
	}

	private void readSpecifyLengthObject(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		int length = getLength();
		byte[] buf = getValue();
		if (buf == null || buf.length != length) {
			buf = new byte[length];
		}
		int currLen = in.read(buf, 0, length);
		if (currLen != length) {
			throw new DecodingException("Fail to read bytes.Require length " + length + ", which is " + currLen);
		}
		setValue(buf);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return StringHelper.buffer("OCBytes [getValue()=", ByteHelper.toHexString(getValue()), ", getLength()=", getLength(),
				']');
	}

}
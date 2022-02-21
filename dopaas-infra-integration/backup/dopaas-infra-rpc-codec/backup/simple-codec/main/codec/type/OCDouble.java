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
 * Just like java double type.
 * 
 * OCDouble.java
 * 
 * @see OCBaseType
 * @version 1.0.0
 * @author Wanglsir
 */
@CodecType
public class OCDouble extends OCBaseType<Double> {

	public OCDouble() {
		setLength(8);
	}

	/**
	 * Construct object by initialize value.
	 * 
	 * @param val
	 *            Double Value.
	 */
	public OCDouble(double val) {
		super(val, 8);
	}

	/**
	 * Construct double object by length type object.
	 * 
	 * @param lenType
	 *            Length type
	 */
	public OCDouble(OCInteger lenType) {
		super(lenType);
		setLength(8);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeObject(Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		double v = getValue(0.);
		byte[] bytes = ByteHelper.convertLong(Double.doubleToLongBits(v), param.isLittleEndian());
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
		long v = ByteHelper.convertToLong(bytes);
		setValue(Double.longBitsToDouble(v));
	}

}
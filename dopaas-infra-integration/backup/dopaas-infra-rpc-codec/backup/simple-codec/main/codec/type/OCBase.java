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
import com.wl4g.component.integration.codec.basetype.BaseType;
import com.wl4g.component.integration.codec.basetype.BaseTypeFactory;
import com.wl4g.component.integration.codec.helper.ReflectHelper;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;

/**
 * 
 * OCBase.java
 * 
 * @see CodecType
 * @version 1.0.0
 * @author Wanglsir
 */
@CodecType
public abstract class OCBase implements OCType {

	private OCInteger lenType;

	private int bytePos;

	public OCBase() {
	}

	/**
	 * Construct object by length type. Length type object will be set when
	 * encoding. Object will read specify length through length type when
	 * decoding.
	 * 
	 * @param lenType
	 *            Length type
	 */
	public OCBase(OCInteger lenType) {
		this.lenType = lenType;
	}

	protected void writeBytes(Encoder encoder, BytesOutputStream out, byte[] bytes, CodecParameter param) throws Exception {
		if (bytes == null) {
			return;
		}
		writeDynamicLength(bytes.length, encoder, out, param);
		setBytePos(out.size());
		out.write(bytes);
	}

	protected void writeDynamicLength(int length, Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		if (lenType != null) {
			lenType.setValue(length);
			int pos = lenType.getBytePos();
			out.setCursor(pos);
			lenType.writeObject(encoder, out, param);
			out.moveLast();
		}
	}

	@SuppressWarnings("unchecked")
	protected <E> E getGenerticValue(Decoder decoder, BytesInputStream in, Class<E> genericType, CodecParameter param)
			throws Exception {
		E obj = null;
		BaseType baseType = BaseTypeFactory.getCodec(genericType);
		if (baseType != null) {
			obj = (E) baseType.decode(decoder, in, null, param);
		} else {
			obj = ReflectHelper.newInstance(genericType);
			decoder.decodeObject(in, obj, param);
		}
		return obj;
	}

	protected void writeAutoLength(Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		if (param.isAutoLength() && lenType == null) {
			lenType = new OCInt32();
			lenType.writeObject(encoder, out, param);
		}
	}

	protected void readAutoLength(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		if (param.isAutoLength() && lenType == null) {
			lenType = new OCInt32();
			lenType.readObject(decoder, in, param);
		}
	}

	public boolean isDynamicLength() {
		return lenType != null;
	}

	public OCInteger getLenType() {
		return lenType;
	}

	public void setLenType(OCInteger lenType) {
		this.lenType = lenType;
	}

	public int getBytePos() {
		return bytePos;
	}

	public void setBytePos(int bytePos) {
		this.bytePos = bytePos;
	}

}
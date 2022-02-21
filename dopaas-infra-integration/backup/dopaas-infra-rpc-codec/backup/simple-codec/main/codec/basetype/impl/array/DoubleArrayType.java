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
package com.wl4g.component.integration.codec.basetype.impl.array;

import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Decoder;
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.basetype.BaseType;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;

/**
 * 
 * DoubleArrayType.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
public class DoubleArrayType extends BaseType {

	BaseTypeBox typeBox;

	public DoubleArrayType(BaseTypeBox typeBox) {
		this.typeBox = typeBox;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void encode(Encoder encoder, BytesOutputStream out, Object obj, CodecParameter param) throws Exception {
		if (obj != null) {
			if (typeBox == BaseTypeBox.BOX) {
				Double[] vs = (Double[]) obj;
				out.writeInt(vs.length);
				for (Double v : vs) {
					if (v == null) {
						v = 0.;
					}
					out.writeDouble(v);
				}
			} else {
				double[] vs = (double[]) obj;
				out.writeInt(vs.length);
				for (double v : vs) {
					out.writeDouble(v);
				}
			}
		} else {
			out.writeInt(0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object decode(Decoder decoder, BytesInputStream in, Object obj, CodecParameter param) throws Exception {
		int len = in.readInt();
		if (len <= 0) {
			return null;
		}

		if (typeBox == BaseTypeBox.BOX) {
			Double[] vs = new Double[len];
			for (int i = 0; i < len; i++) {
				double v = in.readDouble();
				vs[i] = v;
			}
			return vs;
		} else {
			double[] vs = new double[len];
			for (int i = 0; i < len; i++) {
				double v = in.readDouble();
				vs[i] = v;
			}
			return vs;
		}
	}

}
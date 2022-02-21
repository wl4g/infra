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
 * LongArrayType.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
public class LongArrayType extends BaseType {

	BaseTypeBox typeBox;

	public LongArrayType(BaseTypeBox typeBox) {
		this.typeBox = typeBox;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void encode(Encoder encoder, BytesOutputStream out, Object obj, CodecParameter param) throws Exception {
		if (obj != null) {
			if (typeBox == BaseTypeBox.BOX) {
				Long[] vs = (Long[]) obj;
				out.writeInt(vs.length);
				for (Long v : vs) {
					if (v == null) {
						v = 0l;
					}
					out.writeLong(v);
				}
			} else {
				long[] vs = (long[]) obj;
				out.writeInt(vs.length);
				for (long v : vs) {
					out.writeLong(v);
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
			Long[] vs = new Long[len];
			for (int i = 0; i < len; i++) {
				long v = in.readLong();
				vs[i] = v;
			}
			return vs;
		} else {
			long[] vs = new long[len];
			for (int i = 0; i < len; i++) {
				long v = in.readLong();
				vs[i] = v;
			}
			return vs;
		}
	}

}
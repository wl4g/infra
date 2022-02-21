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
package com.wl4g.component.integration.codec.internal;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Decoder;
import com.wl4g.component.integration.codec.basetype.BaseType;
import com.wl4g.component.integration.codec.basetype.BaseTypeFactory;
import com.wl4g.component.integration.codec.exception.OCException;
import com.wl4g.component.integration.codec.helper.ReflectHelper;
import com.wl4g.component.integration.codec.helper.StringHelper;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.type.OCInt32;
import com.wl4g.component.integration.codec.type.OCInteger;
import com.wl4g.component.integration.codec.type.OCObject;
import com.wl4g.component.integration.codec.type.OCType;

/**
 * Default decoder in
 * {@linkplain com.wl4g.component.integration.codec.internal.DefaultCodec DefaultCodec}.
 * 
 * DefaultDecoder.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
public class DefaultDecoder extends Decoder {

	private static final SmartLogger log = getLogger(DefaultDecoder.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object decodeObject(BytesInputStream in, Object obj, CodecParameter param) throws Exception {
		BaseType baseType = BaseTypeFactory.getCodec(obj, param);
		if (baseType != null) {
			if (log.isDebugEnabled()) {
				log.debug(StringHelper.buffer("Decode base type:[", ReflectHelper.getClass(obj, param), "] ", obj));
			}
			return baseType.decode(this, in, obj, param);
		} else if (ReflectHelper.isDefaultType(obj)) {
			decodeDefault(in, (OCType) obj, param);
		} else {
			return decodeOther(in, obj, param);
		}
		return null;
	}

	/**
	 * Decoding default type which inherit {@inheritDoc darks.codec.type.OCType
	 * OCType}.
	 * 
	 * @param in
	 *            Decoding IO stream.
	 * @param type
	 *            Default type object.
	 * @param param
	 *            Codec parameter.
	 */
	private void decodeDefault(BytesInputStream in, OCType type, CodecParameter param) throws Exception {
		try {
			if (log.isDebugEnabled()) {
				log.debug(StringHelper.buffer("Decode default:[", ReflectHelper.getClass(type, param), "] ", type));
			}
			if (type == null && param.getCurrentfield() != null) {
				type = (OCType) ReflectHelper.newInstance(param.getCurrentfield().getType());
			}
			if (type != null) {
				type.readObject(this, in, param);
			}
		} catch (Exception e) {
			throw new OCException("Fail to decode default object. Cause " + e.getMessage(), e);
		}
	}

	/**
	 * Decoding java object.
	 * 
	 * @param out
	 *            Decoding IO stream.
	 * @param object
	 *            Java object.
	 * @param param
	 *            Codec parameter
	 * @throws Exception
	 *             exception
	 * @return If object is null, return new Object;
	 */
	private Object decodeOther(BytesInputStream in, Object object, CodecParameter param) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug(StringHelper.buffer("Decode default:[", ReflectHelper.getClass(object, param), "] ", object));
		}
		OCInteger lenType = null;
		if (object == null && param.isAutoLength() && !param.isIgnoreObjectAutoLength()) {
			lenType = new OCInt32();
			lenType.readObject(this, in, param);
			int lenVal = lenType.getValue(0);
			if (lenVal != 0) {
				object = ReflectHelper.newInstance(param.getCurrentfield().getType());
				new OCObject(object, lenType).readObject(this, in, param);
				return object;
			}
		} else {
			new OCObject(object).readObject(this, in, param);
		}
		return null;
	}
}
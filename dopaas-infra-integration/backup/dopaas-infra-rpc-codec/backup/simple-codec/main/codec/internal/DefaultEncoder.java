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
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.basetype.BaseType;
import com.wl4g.component.integration.codec.basetype.BaseTypeFactory;
import com.wl4g.component.integration.codec.exception.OCException;
import com.wl4g.component.integration.codec.helper.ReflectHelper;
import com.wl4g.component.integration.codec.helper.StringHelper;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;
import com.wl4g.component.integration.codec.type.OCInt32;
import com.wl4g.component.integration.codec.type.OCInteger;
import com.wl4g.component.integration.codec.type.OCObject;
import com.wl4g.component.integration.codec.type.OCType;

/**
 * Default encoder in
 * {@linkplain com.wl4g.component.integration.codec.internal.DefaultCodec DefaultCodec}.
 * 
 * DefaultEncoder.java
 * 
 * @see Encoder
 * @version 1.0.0
 * @author Wanglsir
 */
public class DefaultEncoder extends Encoder {

	private static final SmartLogger log = getLogger(DefaultEncoder.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void encodeObject(BytesOutputStream out, Object obj, CodecParameter param) throws Exception {
		BaseType baseType = BaseTypeFactory.getCodec(obj, param);
		if (baseType != null) {
			if (log.isDebugEnabled()) {
				log.debug(StringHelper.buffer("Encode base type:[", ReflectHelper.getClass(obj, param), "] ", obj));
			}
			baseType.encode(this, out, obj, param);
		} else if (ReflectHelper.isDefaultType(obj)) {
			encodeDefault(out, (OCType) obj, param);
		} else {
			encodeOther(out, obj, param);
		}
	}

	/**
	 * Encoding default type which inherit {@inheritDoc darks.codec.type.OCType
	 * OCType}.
	 * 
	 * @param out
	 *            Encoding IO stream.
	 * @param type
	 *            Default type object.
	 * @param param
	 *            Codec parameter.
	 */
	private void encodeDefault(BytesOutputStream out, OCType type, CodecParameter param) throws Exception {
		try {
			if (log.isDebugEnabled()) {
				log.debug(StringHelper.buffer("Encode default object:[", ReflectHelper.getClass(type, param), "] ", type));
			}
			if (type == null && param.getCurrentfield() != null) {
				type = (OCType) ReflectHelper.newInstance(param.getCurrentfield().getType());
			}
			if (type != null) {
				type.writeObject(this, out, param);
			}
		} catch (Exception e) {
			throw new OCException("Fail to encode default object. Cause " + e.getMessage(), e);
		}
	}

	/**
	 * Encoding java object.
	 * 
	 * @param out
	 *            Encoding IO stream.
	 * @param object
	 *            Java object.
	 * @param param
	 *            Codec parameter
	 * @throws Exception
	 *             exception
	 */
	private void encodeOther(BytesOutputStream out, Object object, CodecParameter param) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug(StringHelper.buffer("Encode other object:[", ReflectHelper.getClass(object, param), "] ", object));
		}
		if (object == null && param.isAutoLength() && !param.isIgnoreObjectAutoLength()) {
			OCInteger lenType = new OCInt32(0);
			lenType.writeObject(this, out, param);
		} else {
			new OCObject(object).writeObject(this, out, param);
		}
	}

}
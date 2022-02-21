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
import com.wl4g.component.integration.codec.Codec;
import com.wl4g.component.integration.codec.CodecConfig;
import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Decoder;
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.CodecConfig.TotalLengthType;
import com.wl4g.component.integration.codec.internal.cache.Cache;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;
import com.wl4g.component.integration.codec.type.OCInt32;
import com.wl4g.component.integration.codec.type.OCObject;
import com.wl4g.component.integration.codec.wrap.TotalLengthWrapper;
import com.wl4g.component.integration.codec.wrap.WrapChain;

/**
 * Default codec to encode or decode object.
 * 
 * DefaultCodec.java
 * 
 * @see Codec
 * @version 1.0.0
 * @author Wanglsir
 */
public class DefaultCodec extends Codec {

	private static final SmartLogger log = getLogger(DefaultCodec.class);

	private static final int INIT_BYTES_SIZE = 128;

	private Encoder encoder;

	private Decoder decoder;

	private Cache cache;

	private WrapChain wrapChain;

	private TotalLengthWrapper totalLenWrap = new TotalLengthWrapper();

	public DefaultCodec(CodecConfig codecConfig) {
		super(codecConfig);
		encoder = new DefaultEncoder();
		decoder = new DefaultDecoder();
		wrapChain = codecConfig.getWrapChain();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void activated() {
		cache = Cache.getCache(codecConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] encode(OCObject msg) throws Exception {
		CodecParameter param = new CodecParameter(codecConfig, cache);
		BytesOutputStream out = new BytesOutputStream(INIT_BYTES_SIZE, codecConfig);
		FinalEncodeQueue queue = new FinalEncodeQueue();
		param.setFinalQueue(queue);
		wrapChain.beforeEncode(encoder, out, param);
		encodeTotalLength(msg, out, param);
		encoder.encodeObject(out, msg, param);
		wrapChain.afterEncode(encoder, out, param);
		queue.doFinal(encoder, out, param);
		byte[] bytes = out.toByteArray();
		if (log.isDebugEnabled()) {
			log.debug(out.toString());
		}
		return bytes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OCObject decode(byte[] bytes, OCObject msg) throws Exception {
		CodecParameter param = new CodecParameter(codecConfig, cache);
		BytesInputStream in = new BytesInputStream(bytes, codecConfig);
		wrapChain.beforeDecode(decoder, in, param);
		decodeTotalLength(msg, in, param);
		decoder.decodeObject(in, msg, param);
		wrapChain.afterDecode(decoder, in, param);
		return msg;
	}

	private void encodeTotalLength(OCObject msg, BytesOutputStream out, CodecParameter param) throws Exception {
		if (codecConfig.getTotalLengthType() != TotalLengthType.AUTO) {
			OCInt32 totalLength = new OCInt32();
			totalLength.writeObject(encoder, out, param);
			msg.setLenType(totalLength);
			param.getFinalQueue().addWrap(totalLenWrap, null);
		}
	}

	private void decodeTotalLength(OCObject msg, BytesInputStream in, CodecParameter param) throws Exception {
		if (codecConfig.getTotalLengthType() != TotalLengthType.AUTO) {
			OCInt32 totalLength = new OCInt32();
			totalLength.readObject(decoder, in, param);
			msg.setLenType(totalLength);
		}
	}
}
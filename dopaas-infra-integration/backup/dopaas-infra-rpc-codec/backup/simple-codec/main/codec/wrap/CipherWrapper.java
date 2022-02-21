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
package com.wl4g.component.integration.codec.wrap;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.codec.CodecConfig;
import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Decoder;
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;
import com.wl4g.component.integration.codec.wrap.cipher.AESCipher;
import com.wl4g.component.integration.codec.wrap.cipher.OCCipher;

/**
 * CipherWrapper can encrypt bytes after encoding and decrypt before decoding
 * bytes. It support AES, RSA cipher algorithm.
 * <p>
 * Example:
 * 
 * <pre>
 *  ObjectCoder coder = new ObjectCoder();
 *      ...
 *  coder.getCodecConfig().addWrap(CipherWrapper.AES("darks"));
 *      ...
 * </pre>
 * 
 * Or
 * 
 * <pre>
 *  ObjectCoder coder = new ObjectCoder();
 *      ...
 *  coder.getCodecConfig().addWrap(new CipherWrapper(new CustomCipher()));
 *      ...
 * </pre>
 * 
 * CipherWrapper.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
public class CipherWrapper extends Wrapper {

	private static final SmartLogger log = getLogger(CipherWrapper.class);

	private OCCipher cipher;

	public static CipherWrapper AES(String key) {
		return new CipherWrapper(new AESCipher(key));
	}

	public static CipherWrapper AES(String key, int keysize) {
		return new CipherWrapper(new AESCipher(key, keysize));
	}

	public CipherWrapper(OCCipher cipher) {
		this.cipher = cipher;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterEncode(Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Before cipher encrypt bytes:" + out);
		}
		int outSize = out.size();
		CodecConfig cfg = param.getCodecConfig();
		byte[] data = cipher.encrypt(out.getDirectBytes(), out.getOffset(), outSize, param);
		out.reset();
		int start = 0;
		boolean hasTotalLen = cfg.isHasTotalLength();
		if (hasTotalLen) {
			start = TOTAL_LEN_BITS;
			out.setCursor(start);
		}
		out.write(data);
		if (hasTotalLen) {
			int count = out.size() - start;
			out.setCursor(0);
			out.writeInt(count);
			out.moveLast();
			if (log.isDebugEnabled()) {
				log.debug("Cipher wrapper encrypt final count:" + count);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("After cipher encrypt bytes:" + out);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeDecode(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Before cipher decrypt bytes:" + in);
		}
		CodecConfig cfg = param.getCodecConfig();
		in.moveHead();
		if (cfg.isHasTotalLength()) {
			int count = in.readInt();
			if (log.isDebugEnabled()) {
				log.debug("Cipher wrapper decrypt count:" + count);
			}
		}
		byte[] data = cipher.decrypt(in.getDirectBytes(), in.position(), in.available(), param);
		in.reset(data);
		if (log.isDebugEnabled()) {
			log.debug("After cipher wrapper decrypt bytes:" + in);
		}
	}
}
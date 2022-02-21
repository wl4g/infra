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
package com.wl4g.component.integration.codec.wrap.verify;

import java.util.zip.Adler32;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.codec.helper.ByteHelper;
import com.wl4g.component.integration.codec.wrap.VerifyWrapper;

/**
 * Adler32 verify bytes arrays.
 * 
 * Adler32Verifier.java
 * 
 * @see VerifyWrapper
 * @version 1.0.0
 * @author Wanglsir
 */
public class Adler32Verifier extends Verifier {

	private static final SmartLogger log = getLogger(Adler32Verifier.class);

	public Adler32Verifier() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getVerifyCode(Object code, boolean littleEndian) {
		Adler32 adler = (Adler32) code;
		if (adler != null) {
			return ByteHelper.convertInt32((int) adler.getValue(), littleEndian);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object update(Object initData, byte[] data, int offset, int length) {
		Adler32 adler = (Adler32) initData;
		if (adler == null) {
			adler = new Adler32();
		}
		adler.update(data, offset, length);
		if (log.isDebugEnabled()) {
			log.debug("Adler32 update " + ByteHelper.toHexString(data, offset, length));
		}
		return adler;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int verifyLength() {
		return 4;
	}

}
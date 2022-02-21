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

import java.nio.ByteBuffer;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.codec.CodecConfig;
import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.CodecConfig.TotalLengthType;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;

/**
 * Calculate bytes total length in final encoding handle.
 * 
 * TotalLengthWrapper.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
public class TotalLengthWrapper extends Wrapper {
	private static final SmartLogger log = getLogger(TotalLengthWrapper.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finalEncode(Encoder encoder, BytesOutputStream out, CodecParameter param, Object extern) throws Exception {
		CodecConfig cfg = param.getCodecConfig();
		int totalSize = 0;
		if (out.getHead() != null && cfg.getTotalLengthType() == TotalLengthType.HEAD_BODY) {
			for (ByteBuffer buf : out.getHead()) {
				totalSize += buf.position();
			}
		}
		if (out.getTail() != null) {
			for (ByteBuffer buf : out.getTail()) {
				totalSize += buf.position();
			}
		}
		totalSize += out.size();
		if (cfg.getTotalLengthType() == TotalLengthType.BODY) {
			totalSize -= 4;
		}
		if (log.isDebugEnabled()) {
			log.debug("Final encode total length:" + totalSize);
		}
		out.setCursor(0);
		out.writeInt(totalSize);
		out.moveLast();
	}

}
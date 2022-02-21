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

import java.util.LinkedList;

import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;
import com.wl4g.component.integration.codec.wrap.Wrapper;

/**
 * Final encode queue will be called in final encode handle.
 * 
 * FinalEncodeQueue.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
public class FinalEncodeQueue {

	LinkedList<FinalPair> pairs = new LinkedList<FinalEncodeQueue.FinalPair>();

	/**
	 * Add final called wrapper.
	 * 
	 * @param wrap
	 *            {@linkplain com.wl4g.component.integration.codec.wrap.Wrapper Wrapper}
	 *            object.
	 * @param extern
	 *            Additional data.
	 */
	public void addWrap(Wrapper wrap, Object extern) {
		pairs.add(new FinalPair(wrap, extern));
	}

	/**
	 * Call final wrapper to encoding final bytes.
	 * 
	 * @param encoder
	 *            {@linkplain com.wl4g.component.integration.codec.Encoder Encoder}
	 *            object.
	 * @param out
	 *            Encoding IO stream
	 * @param param
	 *            parameters
	 * @throws Exception
	 *             exception.
	 */
	public void doFinal(Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		FinalPair pair = null;
		while ((pair = pairs.poll()) != null) {
			pair.wrap.finalEncode(encoder, out, param, pair.extern);
		}
	}

	static class FinalPair {
		Wrapper wrap;

		Object extern;

		public FinalPair(Wrapper wrap, Object extern) {
			super();
			this.wrap = wrap;
			this.extern = extern;
		}

	}
}
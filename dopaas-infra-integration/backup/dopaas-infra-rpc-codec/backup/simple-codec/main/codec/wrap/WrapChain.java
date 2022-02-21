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

import com.wl4g.component.integration.codec.CodecParameter;
import com.wl4g.component.integration.codec.Decoder;
import com.wl4g.component.integration.codec.Encoder;
import com.wl4g.component.integration.codec.stream.BytesInputStream;
import com.wl4g.component.integration.codec.stream.BytesOutputStream;

/**
 * 
 * WrapChain.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
public class WrapChain {
	Wrapper head;

	Wrapper tail;

	/**
	 * Add wrapper to wrap chain
	 * 
	 * @param wrap
	 *            {@linkplain com.wl4g.component.integration.codec.wrap.Wrapper Wrapper}
	 *            object.
	 */
	public void add(Wrapper wrap) {
		if (head == null) {
			head = wrap;
			tail = wrap;
		} else {
			wrap.next = head;
			head.prev = wrap;
			head = wrap;
		}
	}

	public void beforeEncode(Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		Wrapper wrap = head;
		while (wrap != null) {
			wrap.beforeEncode(encoder, out, param);
			wrap = wrap.next;
		}
	}

	public void afterEncode(Encoder encoder, BytesOutputStream out, CodecParameter param) throws Exception {
		Wrapper wrap = head;
		while (wrap != null) {
			wrap.afterEncode(encoder, out, param);
			wrap = wrap.next;
		}
	}

	public void beforeDecode(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		Wrapper wrap = tail;
		while (wrap != null) {
			wrap.beforeDecode(decoder, in, param);
			wrap = wrap.prev;
		}
	}

	public void afterDecode(Decoder decoder, BytesInputStream in, CodecParameter param) throws Exception {
		Wrapper wrap = tail;
		while (wrap != null) {
			wrap.afterDecode(decoder, in, param);
			wrap = wrap.prev;
		}
	}
}
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
package com.wl4g.component.integration.codec.netty;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.codec.protocol.ProtocolWriter;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * {@link GenericMessageEncoder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-15
 * @sine v1.0
 * @see
 */
public class GenericMessageEncoder extends MessageToByteEncoder<ProtocolWriter> implements BytesCodec {

	protected final SmartLogger log = getLogger(getClass());

	@SuppressWarnings("unchecked")
	@Override
	protected void encode(ChannelHandlerContext ctx, ProtocolWriter msg, ByteBuf out) throws Exception {
		log.debug("Begin encoding output message... - {}", () -> toJSONString(msg));

		try {
			// Output bytebuf to channel
			msg.writeByteBufEncode(out);
			log.debug("Code complete, write to network buffer");

			// http://www.oschina.net/question/1247524_250701
			out.retain();

			ctx.writeAndFlush(out);
		} catch (Exception e) {
			log.error("Encode error", e);
		}

		// Invoke other encodes
		// ctx.fireChannelRead(out);
	}

}

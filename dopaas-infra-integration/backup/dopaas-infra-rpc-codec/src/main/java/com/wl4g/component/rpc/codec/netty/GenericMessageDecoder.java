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
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.codec.protocol.ActiveReader;
import com.wl4g.component.integration.codec.protocol.ConnectReader;
import com.wl4g.component.integration.codec.protocol.ProtocolReader;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * {@link GenericMessageDecoder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-15
 * @sine v1.0
 * @see
 */
public class GenericMessageDecoder extends ByteToMessageDecoder implements BytesCodec {

	protected final SmartLogger log = getLogger(getClass());

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try {
			ProtocolReader decoded = null;
			int len = in.readableBytes();
			log.debug("Decoded total bytes: {}", len);

			// Mark the current readindex location
			in.markReaderIndex();

			// Read message header
			int totalLength = in.readInt(); // 应答消息总长度字段
			int commandId = in.readInt(); // 应答命令类型
			int sequenceId = in.readInt(); // 应答消息流水
			log.debug("Decoded headers. - TotalLength: {}, CommandId: {}, SequenceId: {}", totalLength, commandId, sequenceId);

			if (len < totalLength) {
				/**
				 * If the length of the read message body is less than the
				 * length of the message we sent, we should reset the
				 * readerindex. This should be used in conjunction with the
				 * {@link ByteBuf#markReaderIndex()}. Reset readindex to mark.
				 */
				in.resetReaderIndex();
				log.debug("Wait until there is load data...");
				return;
			}

			// Matchs and decodes protocol message.
			decoded = dispatchMatchingProtocol(commandId);

			// Sets headers.
			decoded.getHeader().setTotalLength(totalLength);
			decoded.getHeader().setCommandId(commandId);
			decoded.getHeader().setSequenceId(sequenceId);

			// Decode body
			decoded.readByteBufDecode(in);
			log.debug("Decoded the message as object: {}", decoded);

			out.add(decoded);
		} catch (Exception e) {
			log.error("解码处理异常.", e);
		}
	}

	/**
	 * Match and obtain the corresponding response message class instance
	 * according to ActionID
	 * 
	 * @param commandId
	 * @return
	 */
	private ProtocolReader dispatchMatchingProtocol(int commandId) {
		ProtocolReader readed = null;
		switch (commandId) {
		case 0x80000001: // CMPP_CONNECT_RESP
			readed = new ConnectReader();
			break;
		case 0x80000007: // CMPP_CANCEL_RESP
			break;
		case 0x80000008: // CMPP_ACTIVE_TEST_RESP
			readed = new ActiveReader();
			break;
		default:
			log.warn("Unknown message, commandId: {}", commandId);
			break;
		}
		return readed;
	}

}

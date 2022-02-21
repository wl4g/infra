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
package com.wl4g.component.integration.codec.protocol;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import com.wl4g.component.common.log.SmartLogger;

import java.io.Serializable;

/**
 * Read remote output byte stream message to local object.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @time 2016年9月21日
 * @since
 */
@Getter
@Setter
public abstract class ProtocolReader implements Serializable {
	private static final long serialVersionUID = 6391365835978781552L;

	protected final SmartLogger log = getLogger(getClass());

	private ProtocolHeader header = new ProtocolHeader();
	private long msgId;
	private byte result;

	/**
	 * Read {@link ByteBuf} stream decoding to object.
	 * 
	 * @param in
	 * @throws Exception
	 */
	public abstract void readByteBufDecode(ByteBuf in) throws Exception;

}

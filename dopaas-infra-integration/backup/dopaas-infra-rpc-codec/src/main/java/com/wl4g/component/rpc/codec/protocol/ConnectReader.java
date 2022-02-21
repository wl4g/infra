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

import static com.google.common.base.Charsets.UTF_8;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@link ConnectReader}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-15
 * @sine v1.0
 * @see
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectReader extends ProtocolReader {
	private static final long serialVersionUID = 3762587348690287871L;

	/*
	 * 状态 0：正确 1：消息结构错 2：非法源地址 3：认证错 4：版本太高 5~ ：其他错误
	 */
	private byte status;

	/*
	 * ISMG认证码，用于鉴别ISMG。 其值通过单向MD5 hash计算得出，表示如下： AuthenticatorISMG
	 * =MD5（Status+AuthenticatorSource+shared secret），Shared secret
	 * 由中国移动与源地址实体事先商定，AuthenticatorSource为源地址实体发送给ISMG的对应消息CMPP_Connect中的值。
	 * 认证出错时，此项为空
	 */
	private String authenticateISMG;

	/*
	 * 服务器支持的最高版本号，对于3.0的版本，高4bit为3，低4位为0
	 */
	private byte version;

	@Override
	public void readByteBufDecode(ByteBuf in) throws Exception {
		setStatus(in.readByte());
		setAuthenticateISMG(in.readBytes(16).toString(UTF_8));
		setVersion(in.readByte());
	}

}

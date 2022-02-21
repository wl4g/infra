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
package com.wl4g.component.integration.feign.dubbo.context;

import com.alibaba.dubbo.rpc.RpcContext;
import com.wl4g.component.integration.feign.context.RpcContextRegistry;

/**
 * {@link DubboRpcContextHolder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-17
 * @sine v1.0
 * @see
 */
class DubboRpcContextHolder extends RpcContextRegistry {

	@Override
	public String getAttachment(String key) {
		return RpcContext.getContext().getAttachment(key);
	}

	@Override
	public void setAttachment(String key, String value) {
		RpcContext.getContext().setAttachment(key, value);
	}

	@Override
	public void removeAttachment(String key) {
		RpcContext.getContext().removeAttachment(key);
	}

	@Override
	public void clearAttachments() {
		RpcContext.getContext().clearAttachments();
	}

	@Override
	public int getRemotePort() {
		return RpcContext.getContext().getRemotePort();
	}

	@Override
	public String getRemoteHost() {
		return RpcContext.getContext().getRemoteHost();
	}

	@Override
	public int getLocalPort() {
		return RpcContext.getContext().getLocalPort();
	}

	@Override
	public String getLocalHost() {
		return RpcContext.getContext().getLocalHost();
	}

}

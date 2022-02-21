///*
// * Copyright (C) 2017 ~ 2025 the original author or authors.
// * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
// * All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * 
// * Reference to website: http://wl4g.com
// */
//package com.wl4g.component.integration.feign.context;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * {@link FeignRpcContextHolder}
// * 
// * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
// * @version v1.0 2020-12-17
// * @sine v1.0
// * @see
// */
//public class FeignRpcContextHolder extends RpcContextHolder {
//
//	private final Map<String, String> attachments = new HashMap<String, String>();
//
//	@Override
//	public String getAttachment(String key) {
//		return attachments.get(key);
//	}
//
//	@Override
//	public void setAttachment(String key, String value) {
//		attachments.put(key, value);
//	}
//
//	@Override
//	public void removeAttachment(String key) {
//		attachments.remove(key);
//	}
//
//	@Override
//	public void clearAttachments() {
//		attachments.clear();
//	}
//
//	@Override
//	public int getRemotePort() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public String getRemoteHost() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public int getLocalPort() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public String getLocalHost() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}

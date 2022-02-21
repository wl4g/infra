/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
 */
package com.wl4g.component.integration.feign.springcloud.dubbo.context.filter;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.feign.core.context.RpcContextHolder;

/**
 * {@link ProviderContextFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see https://blog.csdn.net/qq_38377774/article/details/108204661
 * @see https://www.jianshu.com/p/089778701921
 * @see https://github.com/apache/dubbo/issues/1533
 */
@Activate(group = Constants.PROVIDER, order = -2000)
@Deprecated // 原因见：#invoke()方法注释
public class ProviderContextFilter implements Filter {
	protected final SmartLogger log = getLogger(getClass());

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Result result = invoker.invoke(invocation);
		Map<String, String> attachments = RpcContextHolder.getServerContext().getAttachments();
		result.getAttachments().putAll(attachments);
		invocation.getAttachments().putAll(attachments);
		log.debug("Sets response context attachments: {}", attachments);

		// TODO 未通过测试!!! 在ConsumerContextFilter无法获取到这里返回的附加参数???
		return result;
	}

}

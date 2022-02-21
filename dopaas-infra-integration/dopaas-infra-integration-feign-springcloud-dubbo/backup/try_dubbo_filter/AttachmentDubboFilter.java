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
package com.wl4g.component.integration.feign.dubbo.context;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmpty;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethod;
import static com.wl4g.component.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.component.common.reflect.ReflectionUtils2.makeAccessible;
import static com.wl4g.component.common.web.WebUtils2.getFirstParameters;
import static com.wl4g.component.core.utils.web.WebUtils3.currentServletRequest;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.feign.context.RpcContextRegistry;

/**
 * {@link AttachmentDubboFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see https://blog.csdn.net/qq_38377774/article/details/108204661
 * @see https://www.jianshu.com/p/089778701921
 * @see https://github.com/apache/dubbo/issues/1533
 */
@Activate(group = { Constants.PROVIDER, Constants.CONSUMER }, order = -2000)
public class AttachmentDubboFilter implements Filter {
	private static final SmartLogger log = getLogger(AttachmentDubboFilter.class);

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		// Sets current request parameters.
		/*
		 * Note: Here it is possible to retry the execution several times, the
		 * second execution of currentServletRequest will return null.
		 */
		Map<String, String> params = getFirstParameters(currentServletRequest());
		if (!isEmpty(params)) {
			RpcContext.getContext().getAttachments().putAll(params);
			// TODO
			// RpcContextHolder.setContext(new DubboRpcContextHolder());
		}

		// Sets current IAM principal.(if necessary)
		if (nonNull(GET_PRINCIPALINFO_METHOD)) {
			// Optimize performance by lazy invoke with lambde function.
			RpcContextRegistry.getContext().setLambdaAttachment("IAM_PRINCIPAL",
					() -> invokeMethod(GET_PRINCIPALINFO_METHOD, null));
		}

		return invoker.invoke(invocation);
	}

	public static final transient Method GET_PRINCIPALINFO_METHOD;

	static {
		Method getPrincipalMethod = null;
		try {
			getPrincipalMethod = findMethod(Class.forName("com.wl4g.iam.core.utils.IamSecurityHolder"), "getPrincipalInfo");
			makeAccessible(getPrincipalMethod);
		} catch (ClassNotFoundException e) { // Ignore
			log.warn("Cannot load IamSecurityHolder.getPrincipalInfo() ", e.getMessage());
		}
		GET_PRINCIPALINFO_METHOD = getPrincipalMethod;
	}

}

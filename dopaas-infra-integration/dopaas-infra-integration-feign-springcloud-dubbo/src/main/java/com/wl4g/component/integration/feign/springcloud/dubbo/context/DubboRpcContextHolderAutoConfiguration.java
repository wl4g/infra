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
package com.wl4g.component.integration.feign.springcloud.dubbo.context;

import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethodNullable;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.Nullable;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import com.alibaba.boot.dubbo.autoconfigure.DubboAutoConfiguration;
import com.alibaba.dubbo.rpc.RpcContext;
import com.wl4g.component.integration.feign.core.context.RpcContextHolder;

/**
 * {@link DubboRpcContextHolderAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-28
 * @sine v1.0
 * @see
 */
@ConditionalOnClass(DubboAutoConfiguration.class)
public class DubboRpcContextHolderAutoConfiguration {

	@Bean
	public RpcContextHolder springCloudDubboRpcContextHolder() {
		return new SpringCloudDubboRpcContextHolder();
	}

	/**
	 * {@link SpringCloudDubboRpcContextHolder}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2020-12-17
	 * @sine v1.0
	 * @see
	 */
	static class SpringCloudDubboRpcContextHolder extends RpcContextHolder {

		@Override
		public String getAttachment(String key) {
			throw new IllegalStateException();
		}

		@Override
		public Map<String, String> getAttachments() {
			throw new IllegalStateException();
		}

		@Override
		public void setAttachment(String key, String value) {
			throw new IllegalStateException();
		}

		@Override
		public void removeAttachment(String key) {
			throw new IllegalStateException();
		}

		@Override
		public void clearAttachments() {
			throw new IllegalStateException();
		}

		@Override
		protected RpcContextHolder getContext0() {
			return defaultDubboRpcContext;
		}

		@Override
		protected RpcContextHolder getServerContext0() {
			return defaultDubboServerRpcContext;
		}

		@Override
		protected void removeContext0() {
			RpcContext.removeContext();
		}

		@Override
		protected void removeServerContext0() {
			// For compatibility dubbo-2.6/dubbo-2.7
			if (nonNull(apacheDubbo27RemoveServerContextMethod)) {
				try {
					apacheDubbo27RemoveServerContextMethod.invoke(null);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
			// Fallback
			else {
				RpcContext.removeContext();
			}
		}

		// Compatibility FIXED: apache-dubbo-2.7.x => getContext() and
		// getServerContext()
		private static final @Nullable Class<?> apacheDubbo27RpcContextClass = resolveClassNameNullable(
				"org.apache.dubbo.rpc.RpcContext"); // dubbo-2.7.x:org.apache.dubbo.rpc.RpcContext

		private static final @Nullable Method apacheDubbo27GetServerContextMethod = findMethodNullable(
				apacheDubbo27RpcContextClass, "getServerContext"); // dubbo-2.6.x:getContext(),dubbo-2.7.x:getServerContext()

		private static final @Nullable Method apacheDubbo27RemoveServerContextMethod = findMethodNullable(
				apacheDubbo27RpcContextClass, "removeServerContext"); // dubbo-2.6.x:getContext(),dubbo-2.7.x:getServerContext()

		private static final RpcContextHolder defaultDubboRpcContext = new RpcContextHolder() {
			@Override
			public String getAttachment(String key) {
				return RpcContext.getContext().getAttachment(key);
			}

			@Override
			public Map<String, String> getAttachments() {
				return RpcContext.getContext().getAttachments();
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
			protected RpcContextHolder getContext0() {
				return this;
			}

			@Override
			protected RpcContextHolder getServerContext0() {
				return defaultDubboServerRpcContext;
			}

			@Override
			protected void removeContext0() {
				throw new IllegalStateException();
			}

			@Override
			protected void removeServerContext0() {
				throw new IllegalStateException();
			}
		};

		private static final RpcContextHolder defaultDubboServerRpcContext = new RpcContextHolder() {
			@Override
			public String getAttachment(String key) {
				if (nonNull(apacheDubbo27GetServerContextMethod)) {
					try {
						RpcContext rpcContext = (RpcContext) apacheDubbo27GetServerContextMethod.invoke(null);
						return rpcContext.getAttachment(key);
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
				// Fallback
				return RpcContext.getContext().getAttachment(key);
			}

			@Override
			public Map<String, String> getAttachments() {
				if (nonNull(apacheDubbo27GetServerContextMethod)) {
					try {
						RpcContext rpcContext = (RpcContext) apacheDubbo27GetServerContextMethod.invoke(null);
						return rpcContext.getAttachments();
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
				// Fallback
				return RpcContext.getContext().getAttachments();
			}

			@Override
			public void setAttachment(String key, String value) {
				if (nonNull(apacheDubbo27GetServerContextMethod)) {
					try {
						RpcContext rpcContext = (RpcContext) apacheDubbo27GetServerContextMethod.invoke(null);
						rpcContext.setAttachment(key, value);
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
			}

			@Override
			public void removeAttachment(String key) {
				if (nonNull(apacheDubbo27GetServerContextMethod)) {
					try {
						RpcContext rpcContext = (RpcContext) apacheDubbo27GetServerContextMethod.invoke(null);
						rpcContext.removeAttachment(key);
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
				// Fallback
				else {
					RpcContext.getContext().removeAttachment(key);
				}
			}

			@Override
			public void clearAttachments() {
				if (nonNull(apacheDubbo27GetServerContextMethod)) {
					try {
						RpcContext rpcContext = (RpcContext) apacheDubbo27GetServerContextMethod.invoke(null);
						rpcContext.clearAttachments();
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
				// Fallback
				else {
					RpcContext.getContext().clearAttachments();
				}
			}

			@Override
			protected RpcContextHolder getContext0() {
				return defaultDubboRpcContext;
			}

			@Override
			protected RpcContextHolder getServerContext0() {
				return this;
			}

			@Override
			protected void removeContext0() {
				throw new IllegalStateException();
			}

			@Override
			protected void removeServerContext0() {
				throw new IllegalStateException();
			}
		};

	}

}

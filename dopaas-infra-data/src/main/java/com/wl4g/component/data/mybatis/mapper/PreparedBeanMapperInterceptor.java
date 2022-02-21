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
package com.wl4g.component.data.mybatis.mapper;

import static com.wl4g.component.common.lang.TypeConverts.parseLongOrNull;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.beans.BeanUtils.copyProperties;

import java.util.Iterator;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.SqlUtil;
import com.wl4g.component.common.bridge.RpcContextHolderBridges;
import com.wl4g.component.common.bridge.RpcContextIamSecurityBridges;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.bean.BaseBean;
import com.wl4g.component.core.page.PageHolder;

/**
 * General {@link BaseBean} property functions handle interceptors in a unified
 * way, for example, generating IDs for insertion methods (supports local or
 * remote generators)</br>
 * </br>
 * 
 * <b>In mybatis, there are four types that can be intercepted (in the order of
 * interception type):</b></br>
 * </br>
 * Executor：Method of intercepting actuator</br>
 * ParameterHandler：Processing of interception parameters</br>
 * ResultHandler：Processing of interception result set</br>
 * StatementHandler：Processing of intercepting SQL syntax construction</br>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-17
 * @sine v1.0
 * @see https://www.jianshu.com/p/0a72bb1f6a21
 * @see https://cloud.tencent.com/developer/article/1170370
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class }) })
public class PreparedBeanMapperInterceptor implements Interceptor {

	protected final SmartLogger log = getLogger(getClass());

	@Autowired
	private IdGenerator idGenerator;

	/**
	 * Intercepting executor only
	 *
	 * @param target
	 * @return
	 */
	@Override
	public Object plugin(Object target) {
		if (target instanceof Executor) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties properties) {
	}

	@Override
	public Object intercept(Invocation invoc) throws Throwable {
		if (isNull(invoc.getArgs())) {
			return invoc.proceed();
		}
		MappedStatement statement = (MappedStatement) invoc.getArgs()[0];
		SqlCommandType command = statement.getSqlCommandType();

		// Prepared properties.
		preProcess(invoc, command);

		// Invoke mapper process
		final Object result = invoc.proceed();

		// Post properties.
		return postProcess(invoc, command, result);
	}

	/**
	 * Prepared process.
	 * 
	 * @param invoc
	 * @param command
	 */
	private void preProcess(Invocation invoc, SqlCommandType command) {
		switch (command) {
		case SELECT:
			preQuery(invoc);
			break;
		case UPDATE:
			preUpdate(invoc);
			break;
		case INSERT:
			preInsert(invoc);
			break;
		default: // Ignore
		}
	}

	/**
	 * Post process.
	 * 
	 * @param invoc
	 * @param command
	 * @param result
	 */
	private Object postProcess(Invocation invoc, SqlCommandType command, Object result) {
		switch (command) {
		case SELECT:
			return postQuery(invoc, result);
		case UPDATE:
			return postUpdate(invoc, result);
		case INSERT:
			return postInsert(invoc, result);
		default: // Ignore
			return result;
		}
	}

	// --- Prepare SQL processing. ---

	/**
	 * Pre query properties set. (if necessary)
	 * 
	 * @param invoc
	 * @param command
	 */
	protected void preQuery(Invocation invoc) {
		/*
		 * Note: In order to be compatible with the distributed microservice
		 * architecture, it is not the best solution to convert the bean time
		 * returned by Dao layer here. Because Dao layer may not be able to
		 * obtain the I18N language of the current user, it is finally decided
		 * to migrate to the web layer.
		 */
		if (isNull(SqlUtil.getLocalPage())) { // Not paging
			// Obtain page from rpc context.
			com.wl4g.component.core.page.PageHolder.Page<?> page = PageHolder.InternalUtil.current(false);
			if (nonNull(page)) {
				log.debug("Begin current paging of: {}", page);
				PageHelper.startPage(page.getPageNum(), page.getPageSize(), page.isCount());
			}
		}
	}

	/**
	 * Pre updation properties set.(if necessary)
	 * 
	 * @param invoc
	 * @param command
	 */
	protected void preUpdate(Invocation invoc) {
		for (int i = 1; i < invoc.getArgs().length; i++) {
			Object arg = invoc.getArgs()[i];
			if (BaseBean.class.isAssignableFrom(arg.getClass())) {
				BaseBean bean = (BaseBean) arg;
				if (isUpdateSettable(bean)) {
					bean.preUpdate();
					bean.setUpdateBy(getCurrentPrincipalId());
				}
				break;
			}
		}
	}

	/**
	 * Pre insertion properties set.(if necessary)
	 * 
	 * @param invoc
	 */
	@SuppressWarnings("rawtypes")
	protected void preInsert(Invocation invoc) {
		// Sets insert properties
		for (int i = 1; i < invoc.getArgs().length; i++) {
			Object arg = invoc.getArgs()[i];
			if (BaseBean.class.isAssignableFrom(arg.getClass())) {
				doPreInsert(invoc, (BaseBean) arg);
			}
			// e.g: UserDao#insertBatch(List<User>)
			else if (Iterable.class.isAssignableFrom(arg.getClass())) {
				Iterator it = ((Iterable) arg).iterator();
				while (it.hasNext()) {
					Object value = it.next();
					if (BaseBean.class.isAssignableFrom(value.getClass())) {
						doPreInsert(invoc, (BaseBean) arg);
					}
				}
			}
		}
	}

	protected void doPreInsert(Invocation invoc, BaseBean bean) {
		// Assign sets primary key ID.
		if (!isNull(bean) && isNull(bean.getId())) {
			bean.setId(idGenerator.nextId(bean));
			log.debug("Dynamic assigned primary key ID for: {}, method: {}", bean.getId(), invoc.getMethod());
		}
		if (isInsertSettable(bean)) {
			bean.preInsert();
			Long currentPrincipalId = getCurrentPrincipalId();
			bean.setCreateBy(isNull(currentPrincipalId) ? BaseBean.UNKNOWN_USER_ID : currentPrincipalId);
			bean.setUpdateBy(bean.getCreateBy());
		}
	}

	// --- Post SQL processing. ---

	/**
	 * Post query properties set. (if necessary)
	 * 
	 * @param invoc
	 * @param result
	 * @return
	 */
	protected Object postQuery(Invocation invoc, Object result) {
		if (isNull(result)) {
			return null;
		}
		// Update executed paging info to rpc server context.
		if (result instanceof Page) {
			com.github.pagehelper.Page<?> helperPage = (com.github.pagehelper.Page<?>) result;
			com.wl4g.component.core.page.PageHolder.Page<?> currentPage = PageHolder.InternalUtil.current(false);
			if (nonNull(currentPage)) {
				copyProperties(helperPage, currentPage);
				// Rebind executed page information to rpc server context,
				// Ensure that the information that has been paged can be
				// obtained on the consumer service side.
				PageHolder.InternalUtil.bind(true, currentPage);

				// Update current executed paging info.
				PageHolder.InternalUtil.update();
				log.debug("End current paging of: {}", currentPage);
			}
		}
		return result;
	}

	/**
	 * Post updation properties set.(if necessary)
	 * 
	 * @param invoc
	 * @param result
	 * @return
	 */
	protected Object postUpdate(Invocation invoc, Object result) {
		return result;
	}

	/**
	 * Post insertion properties set.(if necessary)
	 * 
	 * @param invoc
	 * @param result
	 */
	protected Object postInsert(Invocation invoc, Object result) {
		if (isNull(result)) {
			return null;
		}
		// Sets the primary key value generated by inserted SQL to return to the
		// caller.
		for (int i = 1; i < invoc.getArgs().length; i++) {
			Object arg = invoc.getArgs()[i];
			if (BaseBean.class.isAssignableFrom(arg.getClass())) {
				BaseBean bean = (BaseBean) arg;
				if (RpcContextHolderBridges.hasRpcContextHolderClass()) { // Distributed(cluster)?
					RpcContextHolderBridges.invokeSet(true, BaseBean.InternalUtil.CURRENT_BEANID_KEY, bean.getId());
				}
			}
		}
		return result;
	}

	/**
	 * Check whether parameter properties need to be set when insertion.
	 * 
	 * @param bean
	 * @return
	 */
	protected boolean isInsertSettable(BaseBean bean) {
		return isNull(bean.getCreateDate()) || isNull(bean.getCreateBy());
	}

	/**
	 * Check whether parameter properties need to be set when updating.
	 * 
	 * @param bean
	 * @return
	 */
	protected boolean isUpdateSettable(BaseBean bean) {
		return isNull(bean.getUpdateDate()) || isNull(bean.getUpdateBy());
	}

	/**
	 * Gets current login principalId.
	 * 
	 * @return
	 */
	protected Long getCurrentPrincipalId() {
		if (RpcContextIamSecurityBridges.hasRpcContextIamSecurityUtilsClass()) {
			try {
				// TODO remove parse, direct use string principalId.
				return parseLongOrNull(RpcContextIamSecurityBridges.currentIamPrincipalId());
			} catch (Exception e) {
				log.warn("Cannot get currentIamPrincipalId. - {}", e.getMessage());
			}
		} else {
			log.warn("Saving unable to set currentIamPrincipalId! Please check if 'xcloud-iam-common' dependency exists!");
		}
		return BaseBean.UNKNOWN_USER_ID;
	}

}

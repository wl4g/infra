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
package com.wl4g.component.core.framework;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.DefaultParameterNameDiscoverer;

/**
 * The main purpose of this class is to obtain the name of method formal
 * parameter as much as possible. Since these information will be lost before
 * and after compilation, this kind of enhanced acquisition is needed.
 * 
 * Note: after verification, it can obtain the parameter name of the parent
 * class corresponding to the cglib proxy class, but it cannot be obtained when
 * the parent class is an interface.
 * 
 * <pre>
 * import java.lang.reflect.Method;
 * import javassist.ClassPool;
 * import javassist.CtClass;
 * import javassist.CtMethod;
 * import javassist.Modifier;
 * import javassist.bytecode.CodeAttribute;
 * import javassist.bytecode.LocalVariableAttribute;
 * import javassist.bytecode.MethodInfo;
 * 
 * public String[] getParameterNames(Method method) {
 *     try {
 *     	ClassPool pool = ClassPool.getDefault();
 *     	CtClass cc = pool.get(method.getDeclaringClass().getName());
 *     	CtMethod cm = cc.getDeclaredMethod(method.getName());
 *     	MethodInfo methodInfo = cm.getMethodInfo();
 *     	CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
 *     	LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
 *     	if (attr != null) {
 *     		String[] paramNames = new String[cm.getParameterTypes().length];
 *     		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
 *     		for (int i = 0; i < paramNames.length; i++) {
 *     			paramNames[i] = attr.variableName(i + pos);
 *     			return paramNames;
 *     		}
 *     	}
 *     } catch (Exception e) {
 *     	e.printStackTrace();
 *     }
 * }
 * </pre>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-24
 * @sine v1.0
 * @see
 */
public class HierarchyParameterNameDiscoverer extends DefaultParameterNameDiscoverer {

	public static final HierarchyParameterNameDiscoverer DEFAULT = new HierarchyParameterNameDiscoverer();

	@Override
	public String[] getParameterNames(Method method) {
		String[] parameterNames = super.getParameterNames(method);
		if (!isEmptyArray(parameterNames)) {
			return parameterNames;
		}

		// Fallback, try to get the parameter name through the super classes and
		// interfaces as much as possible.
		List<Class<?>> classes = new ArrayList<>(safeArrayToList(method.getDeclaringClass().getInterfaces()));
		Class<?> clazz = method.getDeclaringClass();
		do {
			classes.add(clazz);
			classes.addAll(safeArrayToList(clazz.getInterfaces()));
			clazz = clazz.getSuperclass();
		} while (nonNull(clazz) && clazz != Object.class);

		// find parametered names.
		for (Class<?> cls : classes) {
			try {
				Method superMethod = cls.getMethod(method.getName(), method.getParameterTypes());
				parameterNames = super.getParameterNames(superMethod);
				if (!isEmptyArray(parameterNames)) {
					return parameterNames;
				}
			} catch (NoSuchMethodException e) {
				continue;
			}
		}

		return null;
	}

}
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
package com.wl4g.component.core.framework.beans;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Scope;

/**
 * Configure aliases for prototype beans to obtain prototype beans at
 * {@link NamingPrototypeBeanFactory} </br>
 * 
 * <p>
 * <b>for example bean alias definition: </b>
 * 
 * <pre>
 * import static com.wl4g.component.common.reflect.ReflectionUtils2.getFieldValues;
 * 
 * public interface MyProviderType {
 * 	public static final String BEAN_ALIAS1 = "myBeanAlias1";
 * 	public static final String BEAN_ALIAS2 = "myBeanAlias2";
 *
 * 	// List of field values of class {&#64;link MyProviderType}.
 * 	public static final String[] VALUES = getFieldValues(MyProviderType.class, "VALUES").toArray(new String[] {});
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * <b>for example1: </b>
 * 
 * <pre>
 * &#64;Configuration
 * public class MyAutoConfiguration {
 *
 * 	&#64;Bean
 * 	&#64;NamingPrototype({ MyProviderType.BEAN_ALIAS1, MyProviderType.BEAN_ALIAS2 })
 * 	public MyProvider myProvider() {
 * 		return new MyProvider();
 * 	}
 *
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * <b>for example2: </b>
 * 
 * <pre>
 * &#64;Component
 * &#64;NamingPrototype({ MyProviderType.BEAN_ALIAS1, MyProviderType.BEAN_ALIAS2 })
 * public class MyProvider {
 * 
 * }
 * </pre>
 * </p>
 * 
 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2019-10-09
 * @since
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope(SCOPE_PROTOTYPE)
public @interface NamingPrototype {

	/**
	 * Naming prototype bean aliases.
	 * 
	 * @return
	 */
	String[] value();

}
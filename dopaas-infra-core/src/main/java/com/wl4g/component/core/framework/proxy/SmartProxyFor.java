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
package com.wl4g.component.core.framework.proxy;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * This annotation can be used to specify the interface used by the smart proxy.
 * </br>
 * This is particularly useful when beans are final types, Because classes of
 * final type cannot be directly enhenced.</br>
 * <b>For Example:</b>
 * 
 * <pre>
 * public interface XxxService {
 * }
 * 
 * {@code @Service}
 * {@code @SmartProxyFor} // Equivalent to: {@code @SmartProxyFor}({ XxxService.class })
 * public <b><font color=
red>final</font></b> class XxxServiceImpl implements XxxService {
 * }
 * 
 * public class XxxController {
 *   {@code @Autowired}
 *   private XxxService xxxService;
 * }
 * </pre>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-03-24
 * @sine v1.0
 * @see
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface SmartProxyFor {

	/**
	 * Displays the list of interfaces used by the specified enhancement. When
	 * it is empty, the bean actual interface list is used as the default value.
	 * </br>
	 * 
	 * @return
	 */
	@AliasFor("interfaces")
	Class<?>[] value() default {};

	/**
	 * Reference to: {@link #value()}
	 * 
	 * @return
	 */
	@AliasFor("value")
	Class<?>[] interfaces() default {};

}

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
package com.wl4g.component.data.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import com.wl4g.component.data.config.DruidAutoConfiguration;
import com.wl4g.component.data.config.HikariAutoConfiguration;
import com.wl4g.component.data.config.MybatisAutoConfiguration;

/**
 * Enable xcloud components datasource auto configuration.
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-08-05
 * @see {@link MapperScan}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@MapperScan
@Import({ HikariAutoConfiguration.class, DruidAutoConfiguration.class, MybatisAutoConfiguration.class })
public @interface EnableComponentDataConfiguration {

	// --- Mapper scan attributes. ---

	/**
	 * Alias for the {@link #basePackages()} attribute. Allows for more concise
	 * annotation declarations e.g.: {@code @MapperScan("org.my.pkg")} instead
	 * of {@code @MapperScan(basePackages = "org.my.pkg"})}.
	 *
	 * @return base package names
	 */
	@AliasFor(annotation = MapperScan.class, attribute = "value")
	String[] value() default {};

	/**
	 * Base packages to scan for MyBatis interfaces. Note that only interfaces
	 * with at least one method will be registered; concrete classes will be
	 * ignored.
	 *
	 * @return base package names for scanning mapper interface
	 */
	@AliasFor(annotation = MapperScan.class, attribute = "basePackages")
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the
	 * packages to scan for annotated components. The package of each class
	 * specified will be scanned.
	 * <p>
	 * Consider creating a special no-op marker class or interface in each
	 * package that serves no purpose other than being referenced by this
	 * attribute.
	 *
	 * @return classes that indicate base package for scanning mapper interface
	 */
	@AliasFor(annotation = MapperScan.class, attribute = "basePackageClasses")
	Class<?>[] basePackageClasses() default {};

	/**
	 * The {@link BeanNameGenerator} class to be used for naming detected
	 * components within the Spring container.
	 *
	 * @return the class of {@link BeanNameGenerator}
	 */
	@AliasFor(annotation = MapperScan.class, attribute = "nameGenerator")
	Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

	/**
	 * This property specifies the annotation that the scanner will search for.
	 * <p>
	 * The scanner will register all interfaces in the base package that also
	 * have the specified annotation.
	 * <p>
	 * Note this can be combined with markerInterface.
	 *
	 * @return the annotation that the scanner will search for
	 */
	@AliasFor(annotation = MapperScan.class, attribute = "annotationClass")
	Class<? extends Annotation> annotationClass() default Annotation.class;

	/**
	 * This property specifies the parent that the scanner will search for.
	 * <p>
	 * The scanner will register all interfaces in the base package that also
	 * have the specified interface class as a parent.
	 * <p>
	 * Note this can be combined with annotationClass.
	 *
	 * @return the parent that the scanner will search for
	 */
	@AliasFor(annotation = MapperScan.class, attribute = "markerInterface")
	Class<?> markerInterface() default Class.class;

	/**
	 * Specifies which {@code SqlSessionTemplate} to use in the case that there
	 * is more than one in the spring context. Usually this is only needed when
	 * you have more than one datasource.
	 *
	 * @return the bean name of {@code SqlSessionTemplate}
	 */
	@AliasFor(annotation = MapperScan.class, attribute = "sqlSessionTemplateRef")
	String sqlSessionTemplateRef() default "";

	/**
	 * Specifies which {@code SqlSessionFactory} to use in the case that there
	 * is more than one in the spring context. Usually this is only needed when
	 * you have more than one datasource.
	 *
	 * @return the bean name of {@code SqlSessionFactory}
	 */
	@AliasFor(annotation = MapperScan.class, attribute = "sqlSessionFactoryRef")
	String sqlSessionFactoryRef() default "";

	/**
	 * Specifies a custom MapperFactoryBean to return a mybatis proxy as spring
	 * bean.
	 *
	 * @return the class of {@code MapperFactoryBean}
	 */
	@SuppressWarnings("rawtypes")
	@AliasFor(annotation = MapperScan.class, attribute = "factoryBean")
	Class<? extends MapperFactoryBean> factoryBean() default MapperFactoryBean.class;

	/**
	 * Whether enable lazy initialization of mapper bean.
	 *
	 * <p>
	 * Default is {@code false}.
	 * </p>
	 * 
	 * @return set {@code true} to enable lazy initialization
	 * @since 2.0.2
	 */
	@AliasFor(annotation = MapperScan.class, attribute = "lazyInitialization")
	String lazyInitialization() default "";

}
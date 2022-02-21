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
package com.wl4g.component.integration.feign.core.annotation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.MultiValueMap;

import com.wl4g.component.common.log.SmartLogger;

import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethodNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.invokeMethod;
import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * {@link BridgeSpringCloudFeignClientsRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
class BridgeSpringCloudFeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
	protected final SmartLogger log = getLogger(getClass());

	private ResourceLoader resourceLoader;
	private Environment environment;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		// Check enabled configuration
		if (!FeignConsumersRegistrar.isEnableConfiguration(environment)) {
			log.warn("No enabled SpringBoot and SpringCloud feign auto configurer!");
			return;
		}

		if (FeignConsumersRegistrar.hasSpringCloudFeignClass()) {
			try {
				Constructor<?> constructor = FEIGNCLIENTS_REGISTRAR_CLASS.getDeclaredConstructor();
				constructor.setAccessible(true);

				Object feignClientsRegistrar = constructor.newInstance();

				FEIGNCLIENTS_REGISTRAR_SETRESOURCELOADER.setAccessible(true);
				invokeMethod(FEIGNCLIENTS_REGISTRAR_SETRESOURCELOADER, feignClientsRegistrar, resourceLoader);

				FEIGNCLIENTS_REGISTRAR_SETENVIRONMENT.setAccessible(true);
				invokeMethod(FEIGNCLIENTS_REGISTRAR_SETENVIRONMENT, feignClientsRegistrar, environment);

				FEIGNCLIENTS_REGISTRAR_REGISTERBEANDEFINITIOINS.setAccessible(true);
				invokeMethod(FEIGNCLIENTS_REGISTRAR_REGISTERBEANDEFINITIOINS, feignClientsRegistrar,
						new Object[] { new AnnotationMetadata() {

							@Override
							public boolean isAnnotated(String annotationName) {
								return metadata.isAnnotated(annotationName);
							}

							@Override
							public Map<String, Object> getAnnotationAttributes(String annotationName) {
								// IMPORTANTS: transform springboot-feign to
								// springcloud-feign
								if (annotationName.equals("org.springframework.cloud.openfeign.EnableFeignClients")) {
									return metadata
											.getAnnotationAttributes(EnableFeignConsumers.class.getCanonicalName());
								}
								return metadata.getAnnotationAttributes(annotationName);
							}

							@Override
							public Map<String, Object> getAnnotationAttributes(String annotationName,
									boolean classValuesAsString) {
								return metadata.getAnnotationAttributes(annotationName, classValuesAsString);
							}

							@Override
							public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName) {
								return metadata.getAllAnnotationAttributes(annotationName);
							}

							@Override
							public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName,
									boolean classValuesAsString) {
								return metadata.getAllAnnotationAttributes(annotationName, classValuesAsString);
							}

							@Override
							public boolean isConcrete() {
								return metadata.isConcrete();
							}

							@Override
							public boolean hasEnclosingClass() {
								return metadata.hasEnclosingClass();
							}

							@Override
							public boolean hasSuperClass() {
								return metadata.hasSuperClass();
							}

							@Override
							public Set<String> getAnnotationTypes() {
								return metadata.getAnnotationTypes();
							}

							@Override
							public Set<String> getMetaAnnotationTypes(String annotationName) {
								return metadata.getMetaAnnotationTypes(annotationName);
							}

							@Override
							public boolean hasAnnotation(String annotationName) {
								return metadata.hasAnnotation(annotationName);
							}

							@Override
							public boolean hasMetaAnnotation(String metaAnnotationName) {
								return metadata.hasMetaAnnotation(metaAnnotationName);
							}

							@Override
							public boolean hasAnnotatedMethods(String annotationName) {
								return metadata.hasAnnotatedMethods(annotationName);
							}

							@Override
							public MergedAnnotations getAnnotations() {
								return metadata.getAnnotations();
							}

							@Override
							public boolean isInterface() {
								return metadata.isInterface();
							}

							@Override
							public boolean isIndependent() {
								return metadata.isIndependent();
							}

							@Override
							public boolean isFinal() {
								return metadata.isFinal();
							}

							@Override
							public boolean isAnnotation() {
								return metadata.isAnnotation();
							}

							@Override
							public boolean isAbstract() {
								return metadata.isAbstract();
							}

							@Override
							public String getSuperClassName() {
								return metadata.getSuperClassName();
							}

							@Override
							public String[] getMemberClassNames() {
								return metadata.getMemberClassNames();
							}

							@Override
							public String[] getInterfaceNames() {
								return metadata.getInterfaceNames();
							}

							@Override
							public String getEnclosingClassName() {
								return metadata.getEnclosingClassName();
							}

							@Override
							public String getClassName() {
								return metadata.getClassName();
							}

							@Override
							public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
								return metadata.getAnnotatedMethods(annotationName);
							}
						}, registry });

			} catch (Exception e) {
				throw new IllegalStateException(format("Cannot to bridge invoke to SpringCloud '%s#registerBeanDefinitions'",
						FEIGNCLIENTS_REGISTRAR_CLASS), e);
			}
		}
	}

	private static final Class<?> FEIGNCLIENTS_REGISTRAR_CLASS = resolveClassNameNullable(
			"org.springframework.cloud.openfeign.FeignClientsRegistrar");
	private static final Method FEIGNCLIENTS_REGISTRAR_SETRESOURCELOADER = findMethodNullable(FEIGNCLIENTS_REGISTRAR_CLASS,
			"setResourceLoader", ResourceLoader.class);
	private static final Method FEIGNCLIENTS_REGISTRAR_SETENVIRONMENT = findMethodNullable(FEIGNCLIENTS_REGISTRAR_CLASS,
			"setEnvironment", Environment.class);
	private static final Method FEIGNCLIENTS_REGISTRAR_REGISTERBEANDEFINITIOINS = findMethodNullable(FEIGNCLIENTS_REGISTRAR_CLASS,
			"registerBeanDefinitions", AnnotationMetadata.class, BeanDefinitionRegistry.class);

}

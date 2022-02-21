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
package com.wl4g.component.core.web.versions.reactive;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.lang.Assert2.state;
import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.result.condition.RequestCondition;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import com.wl4g.component.common.collection.CollectionUtils2;
import com.wl4g.component.core.web.mapping.annotation.WebFluxSmartHandlerMappingConfigurer.ReactiveHandlerMappingSupport;
import com.wl4g.component.core.web.versions.AmbiguousApiVersionMappingException;
import com.wl4g.component.core.web.versions.annotation.ApiVersionManagementWrapper;
import com.wl4g.component.core.web.versions.annotation.ApiVersionMapping;
import com.wl4g.component.core.web.versions.annotation.ApiVersionMappingWrapper;

/**
 * API versions {@link RequestMapping} handler mapping. </br>
 * </br>
 * Notes: Only valid for all {@link RequestMapping} annotated beans (it will
 * override the spring default {@link RequestMappingHandlerMapping}(servlet)
 * instance).
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-26
 * @sine v1.0
 * @see <a href=
 *      "https://blog.csdn.net/sinat_29508581/article/details/89392831">Case2</a>
 * @see <a href=
 *      "http://www.kailaisii.com/archives/%E8%87%AA%E5%AE%9A%E4%B9%89RequestMappingHandlerMapping,%E5%86%8D%E4%B9%9F%E4%B8%8D%E7%94%A8%E5%86%99url%E4%BA%86~">Case1</a>
 * @see <a href=
 *      "https://blog.csdn.net/chuantian3080/article/details/100873706">Case3</a>
 */
public class ApiVersionRequestHandlerMapping extends ReactiveHandlerMappingSupport {

    private ApiVersionManagementWrapper versionConfig;

    public ApiVersionRequestHandlerMapping() {
        setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
    }

    public ApiVersionManagementWrapper getVersionConfig() {
        return versionConfig;
    }

    public void setVersionConfig(ApiVersionManagementWrapper versionConfig) {
        this.versionConfig = versionConfig;
    }

    // --- Request mapping condition. ---

    @Override
    protected boolean supportsHandlerMethod(Object handler, Class<?> handlerType, Method method) {
        return versionConfig.getMergedIncludeFilter().test(handlerType) && hasAnnotation(method, ApiVersionMapping.class);
    }

    @Override
    protected RequestCondition<ApiVersionRequestCondition> getCustomTypeCondition(Class<?> handlerType) {
        return createCondition(handlerType);
    }

    @Override
    protected RequestCondition<ApiVersionRequestCondition> getCustomMethodCondition(Method method) {
        return createCondition(method);
    }

    private final RequestCondition<ApiVersionRequestCondition> createCondition(AnnotatedElement element) {
        ApiVersionMapping versionMapping = findAnnotation(element, ApiVersionMapping.class);

        // Check version properties valid.
        checkVersionValid(element, versionMapping);

        return isNull(versionMapping) ? null
                : new ApiVersionRequestCondition(
                        ApiVersionMappingWrapper.wrap(getApplicationContext().getEnvironment(), versionConfig, versionMapping));
    }

    /**
     * Check API version mapping uniqueness.
     * 
     * @param element
     * @param versionMapping
     */
    protected void checkVersionValid(AnnotatedElement element, ApiVersionMapping versionMapping) {
        RequestMapping requestMapping = findMergedAnnotation(element, RequestMapping.class);
        state(!isNull(requestMapping), "Shouldn't be here");

        // Since it will be executed twice, it is necessary to judge.
        // refer:org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping.getMappingForMethod()
        if (element instanceof Method) {
            CheckMappingWrapper cm = new CheckMappingWrapper(requestMapping, versionMapping);
            isTrue(!checkingMappings.contains(cm), AmbiguousApiVersionMappingException.class,
                    "Ambiguous version API mapping, please ensure that the combind of version and requestPath and requestMethod is unique. - %s",
                    cm);
            this.checkingMappings.add(cm);

            // Check version syntax.
            for (String ver : cm.versions) {
                versionConfig.getVersionComparator().splitVersionParts(ver, true);
            }
        }
    }

    /**
     * Used to check if the mapping is unique wrapper.
     */
    class CheckMappingWrapper {

        private final List<RequestMethod> methods;
        private final List<String> paths;
        private final List<String> versions;

        public CheckMappingWrapper(RequestMapping requestMapping, ApiVersionMapping apiVersionGroup) {
            this.methods = safeArrayToList(requestMapping.method());
            List<String> paths = safeArrayToList(requestMapping.path());
            this.paths = CollectionUtils2.isEmpty(paths) ? safeArrayToList(requestMapping.value()) : paths;
            this.versions = isNull(apiVersionGroup) ? emptyList()
                    : safeArrayToList(apiVersionGroup.value()).stream().map(v -> v.value()).collect(toList());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((methods == null) ? 0 : methods.hashCode());
            result = prime * result + ((paths == null) ? 0 : paths.hashCode());
            result = prime * result + ((versions == null) ? 0 : versions.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CheckMappingWrapper other = (CheckMappingWrapper) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (methods == null) {
                if (other.methods != null)
                    return false;
            } else if (!methods.equals(other.methods))
                return false;
            if (paths == null) {
                if (other.paths != null)
                    return false;
            } else if (!paths.equals(other.paths))
                return false;
            if (versions == null) {
                if (other.versions != null)
                    return false;
            } else if (!versions.equals(other.versions))
                return false;
            return true;
        }

        private ApiVersionRequestHandlerMapping getOuterType() {
            return ApiVersionRequestHandlerMapping.this;
        }

        @Override
        public String toString() {
            return "[methods=" + methods + ", path=" + paths + ", version=" + versions + "]";
        }

    }

    private final List<CheckMappingWrapper> checkingMappings = synchronizedList(new LinkedList<>());

}

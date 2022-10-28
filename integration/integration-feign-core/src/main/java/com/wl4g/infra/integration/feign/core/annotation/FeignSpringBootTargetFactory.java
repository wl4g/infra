/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.integration.feign.core.annotation;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

import com.wl4g.infra.integration.feign.core.config.FeignSpringBootProperties;

import feign.Request;
import feign.RequestTemplate;

/**
 * {@link FeignSpringBootTargetFactory}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version 2022-03-10 v1.0.0
 * @since v1.0.0
 */
public interface FeignSpringBootTargetFactory {

    default <T> feign.Target<T> create(
            FeignSpringBootProperties config,
            Class<T> type,
            String name,
            String namespace,
            String url,
            String path) {
        return new FeignSpringBootUrlTarget<T>(config, type, name, namespace, url, path);
    }

    /**
     * Cached discoverable URL feign target.
     */
    public static class FeignSpringBootUrlTarget<T> implements feign.Target<T> {

        private final FeignSpringBootProperties config;
        private final Class<T> type;
        private final String name;
        private final String namespace;
        private final String url;
        private final String path;

        public FeignSpringBootUrlTarget(@NotNull FeignSpringBootProperties config, @NotNull Class<T> type, @Nullable String name,
                @Nullable String namespace, @Nullable String url, @Nullable String path) {
            this.config = notNullOf(config, "config");
            this.type = notNullOf(type, "type");
            this.name = name;
            this.namespace = namespace;
            this.url = url;
            this.path = path;
        }

        @Override
        public Class<T> type() {
            return this.type;
        }

        @Override
        public String name() {
            return this.name;
        }

        public String namespace() {
            return this.namespace;
        }

        @Override
        public String url() {
            return buildRequestUrl();
        }

        /* no authentication or other special activity. just insert the url. */
        @Override
        public Request apply(RequestTemplate input) {
            if (input.url().indexOf("http") != 0) {
                input.target(url());
            }
            return input.request();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FeignSpringBootUrlTarget) {
                FeignSpringBootUrlTarget<T> other = (FeignSpringBootUrlTarget<T>) obj;
                return type.equals(other.type) && name.equals(other.name) && url.equals(other.url);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + type.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + url.hashCode();
            return result;
        }

        @Override
        public String toString() {
            if (name.equals(url)) {
                return getClass() + " (type=" + type.getSimpleName() + ", url=" + url + ")";
            }
            return getClass() + " (type=" + type.getSimpleName() + ", name=" + name + ", url=" + url + ")";
        }

        protected String buildRequestUrl() {
            // priority1: by codes annotation absolute URL
            if (!isBlank(this.url)) {
                return buildByUrl();
            }
            // priority2: by codes annotation serviceId(name)
            else if (!isBlank(name)) {
                return buildByName();
            }
            // priority3: by configuration default URL.
            return buildByDefaultUrl();
        }

        protected String cleanPath() {
            String path = trimToEmpty(this.path);
            if (StringUtils.hasLength(path)) {
                if (!path.startsWith("/")) {
                    path = "/".concat(path);
                }
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
            }
            return EMPTY;
        }

        protected String buildByUrl() {
            String url = trimToEmpty(isBlank(this.url) ? config.getDefaultUrl() : this.url);
            return url.concat(cleanPath());

        }

        protected String buildByName() {
            return "http://".concat(name).concat(cleanPath());
        }

        protected String buildByDefaultUrl() {
            return config.getDefaultUrl().concat(cleanPath());
        }

    }

}

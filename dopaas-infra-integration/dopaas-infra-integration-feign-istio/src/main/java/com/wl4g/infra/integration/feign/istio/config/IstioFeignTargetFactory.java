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
package com.wl4g.infra.integration.feign.istio.config;

import javax.validation.constraints.NotNull;

import com.wl4g.infra.integration.feign.core.annotation.FeignTargetFactory;
import com.wl4g.infra.integration.feign.core.config.FeignConsumerProperties;

import feign.Target;

/**
 * {@link IstioFeignTargetFactory}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-10 v1.0.0
 * @since v1.0.0
 */
public class IstioFeignTargetFactory implements FeignTargetFactory {

    @Override
    public <T> Target<T> create(FeignConsumerProperties config, Class<T> type, String name, String url, String path) {
        return new IstioFeignUrlTarget<T>(config, type, name, url, path);
    }

    public static class IstioFeignUrlTarget<T> extends DefaultFeignUrlTarget<T> {

        public IstioFeignUrlTarget(@NotNull FeignConsumerProperties config, @NotNull Class<T> type, String name, String url,
                String path) {
            super(config, type, name, url, path);
        }

        @Override
        protected String buildByName() {
            // TODO
            return "http://" + name().concat(".default.svc.cluster.local");
        }

    }

}

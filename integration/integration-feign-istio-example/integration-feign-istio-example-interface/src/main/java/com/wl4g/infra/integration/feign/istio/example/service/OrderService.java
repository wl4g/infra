/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <James Wong@gmail.com, 983708408@qq.com> Technology CO.LTD.
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
package com.wl4g.infra.integration.feign.istio.example.service;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wl4g.infra.core.page.PageHolder;
import com.wl4g.infra.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.infra.integration.feign.istio.example.bean.OrderInfo;

/**
 * {@link OrderService} </br>
 * 
 * <p>
 * The priorities for building the calling URL are: </br>
 * 
 * Priority-as-1: </br>
 * See the source code:
 * {@link com.wl4g.infra.integration.feign.core.annotation.FeignSpringBootTargetFactory.FeignSpringBootUrlTarget#buildByUrl}
 * </br>
 * 
 * Priority-as-2: </br>
 * See the source code:
 * {@link com.wl4g.infra.integration.feign.core.annotation.FeignSpringBootTargetFactory.FeignSpringBootUrlTarget#buildByName}
 * </br>
 * 
 * Priority-as-3: </br>
 * See the source code:
 * {@link com.wl4g.infra.integration.feign.core.annotation.FeignSpringBootTargetFactory.FeignSpringBootUrlTarget#buildByDefaultUrl}
 * </br>
 *
 * </p>
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version v1.0 2021-03-18
 * @sine v1.0
 * @see
 */
//
// Priority-as-1: (for example)
// @FeignConsumer(url="http://order-service.default.svc.cluster.local:27002")
// @FeignConsumer(url="order-service.default.svc.cluster.local:27002")
//
// Priority-as-2: (for example)
// @FeignConsumer(name = "${provider.serviceId}")
@FeignConsumer(name = "${provider.serviceId}")
@RequestMapping("/order")
public interface OrderService {

    // Notes: Cannot be used @GetMapping, because feign convention does not
    // allow it.
    @RequestMapping(value = "/list", method = POST)
    List<OrderInfo> list(@RequestBody PageHolder<OrderInfo> page, @RequestParam("orderName") String orderName);

    @RequestMapping(value = "/create", method = POST)
    int create(@RequestBody OrderInfo order, @RequestParam("goodsId") Long goodsId);

}

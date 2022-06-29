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
package com.wl4g.infra.integration.feign.istio.example.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wl4g.infra.common.web.rest.RespBase;
import com.wl4g.infra.core.page.PageHolder;
import com.wl4g.infra.core.web.BaseController;
import com.wl4g.infra.integration.feign.istio.example.bean.OrderInfo;
import com.wl4g.infra.integration.feign.istio.example.service.OrderService;

/**
 * {@link OrderController}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version v1.0 2021-03-18
 * @sine v1.0
 * @see
 */
@RestController
@RequestMapping("/order")
public class OrderController extends BaseController {

    private @Autowired OrderService orderService;

    @RequestMapping(value = "/list", method = GET)
    public RespBase<?> list(
            PageHolder<OrderInfo> page,
            @RequestParam(name = "orderName", defaultValue = "", required = false) String orderName) {
        RespBase<Object> resp = RespBase.create();

         resp.setData(orderService.list(page, orderName));
//        resp.setData("[{\"id\":10001,\"enable\":1,\"organizationCode\":\"abcd111111\",\"remark\":null,\"createBy\":1,\"createDate\":\"2021-05-20 15:25:07\",\"updateBy\":1,\"updateDate\":\"2021-05-20 15:24:15\",\"delFlag\":0,\"humanCreateDate\":\"a year ago\",\"humanUpdateDate\":\"a year ago\",\"orderNo\":202100121,\"name\":\"Sniper rifle\",\"deliveryAddress\":\"1458 Bee Street1\",\"attributes\":null},{\"id\":10002,\"enable\":1,\"organizationCode\":\"abcd122222\",\"remark\":null,\"createBy\":1,\"createDate\":\"2021-05-20 15:25:07\",\"updateBy\":1,\"updateDate\":\"2021-05-20 15:24:15\",\"delFlag\":0,\"humanCreateDate\":\"a year ago\",\"humanUpdateDate\":\"a year ago\",\"orderNo\":202100122,\"name\":\"Over limit combat check\",\"deliveryAddress\":\"95 Oxford Rd\",\"attributes\":null},{\"id\":10003,\"enable\":1,\"organizationCode\":\"abcd133333\",\"remark\":null,\"createBy\":1,\"createDate\":\"2021-05-20 15:25:07\",\"updateBy\":1,\"updateDate\":\"2021-05-20 15:24:15\",\"delFlag\":0,\"humanCreateDate\":\"a year ago\",\"humanUpdateDate\":\"a year ago\",\"orderNo\":202100123,\"name\":\"fake vote\",\"deliveryAddress\":\"394 Patterson Fork Road\",\"attributes\":null},{");

        log.info("find orders resp: {}", resp);
        return resp;
    }

    @RequestMapping(value = "/create", method = POST)
    public RespBase<?> createOrder(
            @RequestBody OrderInfo order,
            @RequestParam(name = "goodsId", defaultValue = "20001", required = false) Long goodsId) {
        log.info("Saving order, orderId: {}", order.getId());

        RespBase<Object> resp = RespBase.create();

        resp.setData(orderService.create(order, goodsId));

        log.info("Created orders resp: {}", resp);
        return resp;
    }

}

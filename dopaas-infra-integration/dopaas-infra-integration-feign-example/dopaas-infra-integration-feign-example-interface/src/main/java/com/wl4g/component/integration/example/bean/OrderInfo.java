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
package com.wl4g.component.integration.example.bean;

import java.util.Properties;

import com.wl4g.component.core.bean.BaseBean;

/**
 * {@link OrderInfo}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-03-18
 * @sine v1.0
 * @see
 */
public class OrderInfo extends BaseBean {
	private static final long serialVersionUID = -5164915069497907966L;

	private Long orderNo;
	private String name;
	private String deliveryAddress;
	private Properties attributes;

	public OrderInfo() {
		super();
	}

	public OrderInfo(Long orderNo, String name, String deliveryAddress, Properties attributes) {
		super();
		this.orderNo = orderNo;
		this.name = name;
		this.deliveryAddress = deliveryAddress;
		this.attributes = attributes;
	}

	public Long getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Long orderNo) {
		this.orderNo = orderNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Properties getAttributes() {
		return attributes;
	}

	public void setAttributes(Properties attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return "OrderInfo [orderNo=" + orderNo + ", name=" + name + ", deliveryAddress=" + deliveryAddress + ", attributes="
				+ attributes + "]";
	}

}

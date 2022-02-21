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
package com.wl4g.component.common.web;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import javax.annotation.Nullable;

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.BrowserType;
import nl.bitwalker.useragentutils.DeviceType;
import nl.bitwalker.useragentutils.UserAgent;

/**
 * User Agent recognition tools.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2018-12-26
 * @sine v1.0.0
 * @see
 */
public abstract class UserAgentUtils {

	/**
	 * Gets the user agent object
	 * 
	 * @param request
	 * @return
	 */
	public static UserAgent getUserAgent(@NotNull HttpServletRequest request) {
		notNullOf(request, "request");
		return parseUserAgent(request.getHeader("User-Agent"));
	}

	/**
	 * Gets the user agent object
	 * 
	 * @param request
	 * @return
	 */
	public static UserAgent parseUserAgent(@Nullable String uaString) {
		return isNull(uaString) ? null : UserAgent.parseUserAgentString(uaString);
	}

	/**
	 * Get device type
	 * 
	 * @param request
	 * @return
	 */
	public static DeviceType getDeviceType(@NotNull HttpServletRequest request) {
		notNullOf(request, "request");
		UserAgent ua = getUserAgent(request);
		return (isNull(ua) ? null : ((isNull(ua.getOperatingSystem()) ? null : ua.getOperatingSystem()).getDeviceType()));
	}

	/**
	 * Is it a PC?
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isComputer(@NotNull HttpServletRequest request) {
		DeviceType dt = getDeviceType(request);
		return dt != null && DeviceType.COMPUTER.equals(dt);
	}

	/**
	 * Is it a cell phone?
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isMobile(@NotNull HttpServletRequest request) {
		DeviceType dt = getDeviceType(request);
		return dt != null && DeviceType.MOBILE.equals(dt);
	}

	/**
	 * Is it a flat panel?
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isTablet(@NotNull HttpServletRequest request) {
		DeviceType dt = getDeviceType(request);
		return dt != null && DeviceType.TABLET.equals(dt);
	}

	/**
	 * Are they mobile phones and tablets?
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isMobileOrTablet(@NotNull HttpServletRequest request) {
		DeviceType dt = getDeviceType(request);
		return (dt != null && (DeviceType.MOBILE.equals(dt) || DeviceType.TABLET.equals(dt)));
	}

	/**
	 * Is it a browser?
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isBrowser(@NotNull HttpServletRequest request) {
		Browser br = getBrowser(request);
		return (nonNull(br) && nonNull(br.getBrowserType()) && br.getBrowserType() != BrowserType.UNKNOWN);
	}

	/**
	 * Is it a browser?
	 * 
	 * @param uaString
	 * @return
	 */
	public static boolean isBrowser(@Nullable String uaString) {
		Browser br = getBrowser(uaString);
		return (nonNull(br) && nonNull(br.getBrowserType()) && br.getBrowserType() != BrowserType.UNKNOWN);
	}

	/**
	 * Gets the browsing type
	 * 
	 * @param uaString
	 * @return
	 */
	public static Browser getBrowser(@Nullable String uaString) {
		UserAgent ua = parseUserAgent(uaString);
		return (nonNull(ua) && nonNull(ua.getBrowser()) && ua.getBrowser() != Browser.UNKNOWN) ? ua.getBrowser() : null;
	}

	/**
	 * Gets the browsing type
	 * 
	 * @param request
	 * @return
	 */
	public static Browser getBrowser(@NotNull HttpServletRequest request) {
		UserAgent ua = getUserAgent(request);
		return (nonNull(ua) && nonNull(ua.getBrowser()) && ua.getBrowser() != Browser.UNKNOWN) ? ua.getBrowser() : null;
	}

	/**
	 * Gets the browsing type name
	 * 
	 * @param request
	 * @return
	 */
	public static String getBrowserName(@NotNull HttpServletRequest request) {
		Browser browser = getBrowser(request);
		return isNull(browser) ? null : browser.getName();
	}

	/**
	 * Whether the IE version is less than or equal to IE8
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isLteIE8(@NotNull HttpServletRequest request) {
		Browser br = getBrowser(request);
		return (nonNull(br)
				&& (Browser.IE5.equals(br) || Browser.IE6.equals(br) || Browser.IE7.equals(br) || Browser.IE8.equals(br)));
	}

}
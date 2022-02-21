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
package com.wl4g.component.common.lang.period;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.util.Collections.synchronizedMap;
import static java.util.Locale.US;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.wl4g.component.common.bridge.IamSecurityHolderBridges;
import com.wl4g.component.common.log.SmartLogger;

/**
 * {@link PeriodFormatter}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年6月16日
 * @since
 */
public abstract class PeriodFormatter {

	protected final SmartLogger log = getLogger(getClass());

	/**
	 * Current configuration for locale.
	 */
	private Locale locale = Locale.getDefault();

	/**
	 * The lower time unit section will be ignored.
	 */
	private boolean ignoreLowerDate = false;

	/**
	 * Gets instance by default impl class.
	 * 
	 * @return
	 */
	public static PeriodFormatter getDefault() {
		return getInstance(SamplePeriodFormatter.class);
	}

	/**
	 * Gets instance by impl class.
	 * 
	 * @param implClass
	 * @return
	 */
	public static PeriodFormatter getInstance(Class<? extends PeriodFormatter> implClass) {
		return registers.get(implClass);
	}

	/**
	 * Gets current locale.
	 * 
	 * @return
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets current thread locale.
	 * 
	 * @param locale
	 */
	public PeriodFormatter locale(Locale locale) {
		this.locale = locale;
		return this;
	}

	/**
	 * Gets current is ignore lower date.
	 * 
	 * @return
	 */
	public boolean isIngoreLowerDate() {
		return ignoreLowerDate;
	}

	/**
	 * Sets current thread locale.
	 * 
	 * @param l
	 */
	public PeriodFormatter ignoreLowerDate(boolean ignoreLowerDate) {
		this.ignoreLowerDate = ignoreLowerDate;
		return this;
	}

	/**
	 * Formatting to human Date time formatted as human friendly
	 * 
	 * @param startTime
	 *            current timestamp
	 * @param endTime
	 *            Target timestamp to format
	 * @return
	 */
	public abstract String formatHumanDate(long startTime, long endTime);

	/**
	 * Formatting to human Date time formatted as human friendly
	 * 
	 * @param targetTime
	 *            Target timestamp to format
	 * @return
	 */
	public String formatHumanDate(long targetTime) {
		return formatHumanDate(currentTimeMillis(), targetTime);
	}

	/**
	 * Cleanup date empty string.
	 * 
	 * @param dateString
	 * @return
	 */
	protected String cleanupDateEmptyString(String dateString) {
		int safeThreshold = 10;
		String cleared = dateString.toString().trim(), lastCleared = EMPTY;
		for (int i = 0; i < safeThreshold && !lastCleared.equals(cleared); i++, lastCleared = cleared) {
			cleared = cleared.replace("  ", " ");
		}
		return cleared;
	}

	/**
	 * Gets localized message by key.
	 * 
	 * @param localizedKey
	 * @return
	 * @see {@link com.wl4g.iam.core.utils.IamSecurityHolder#getBindValue(String)}
	 * @see {@link com.wl4g.iam.common.i18n.SessionResourceMessageBundler#getSessionLocale()}
	 * @see {@link com.wl4g.component.core.constants.IAMDevOpsConstants#KEY_LANG_NAME}
	 */
	protected String getLocalizedMessage(String localizedKey) {
		String loc = null;
		try {
			if (IamSecurityHolderBridges.hasIamSecurityHolderClass()) {
				loc = (String) IamSecurityHolderBridges.invokeGetBindValue("langAttrName");
			}
		} catch (Exception e) {
			log.warn(format("Cannot get IAM session locale, fallback use '%s'", locale), e);
		}

		try {
			return getResourceBundle(isNull(loc) ? locale : new Locale(loc)).getString(localizedKey);
		} catch (MissingResourceException e) {
			return localizedKey;
		}
	}

	/**
	 * Gets resources bundle.
	 * 
	 * @param loc
	 * @return
	 */
	private static ResourceBundle getResourceBundle(Locale loc) {
		try {
			return ResourceBundle.getBundle(defaultI18nResourcesBaseName, loc);
		} catch (MissingResourceException e) {
			return ResourceBundle.getBundle(defaultI18nResourcesBaseName, US);
		}
	}

	/**
	 * Gets default i18n resources base-name.
	 * 
	 * @return
	 */
	private static String getDefaultI18nResourcesBaseName0() {
		String className = PeriodFormatter.class.getName();
		return className.substring(0, className.lastIndexOf(".")).replace(".", "/").concat("/messages");
	}

	/**
	 * Default i18n resources base-name.
	 */
	private static final String defaultI18nResourcesBaseName = getDefaultI18nResourcesBaseName0();

	/**
	 * {@link PeriodFormatter} register instances
	 */
	private static final Map<Class<? extends PeriodFormatter>, PeriodFormatter> registers = synchronizedMap(new HashMap<>());

	static {
		registers.put(JodaPeriodFormatter.class, new JodaPeriodFormatter());
		registers.put(SamplePeriodFormatter.class, new SamplePeriodFormatter());
	}

}
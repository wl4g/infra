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
package com.wl4g.component.common.typesafe;

import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;

import org.junit.Test;

/**
 * {@link HoconConfigUtilsTests}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-08-13
 * @since
 */
public class HoconConfigUtilsTests {

	@Test
	public void loadAndParseTest1() {
		UserHoconConfig user = HoconConfigUtils.loadConfig("hocon/application-sample.conf", UserHoconConfig.class);
		System.out.println(user);
	}

	/**
	 * Exmpple hocon configuration bean.
	 *
	 * @since
	 */
	public static class UserHoconConfig {

		private String firstName;
		private String lastName;
		private int age;
		private int sex;
		private double height;
		// private java.util.Date birthDate;
		// private java.util.List<UserHoconConfig> children;
		// private UserHoconConfig parent;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public int getSex() {
			return sex;
		}

		public void setSex(int sex) {
			this.sex = sex;
		}

		public double getHeight() {
			return height;
		}

		public void setHeight(double height) {
			this.height = height;
		}

		// public java.util.Date getBirthDate() {
		// return birthDate;
		// }
		//
		// public void setBirthDate(java.util.Date birthDate) {
		// this.birthDate = birthDate;
		// }
		//
		// public java.util.List<UserHoconConfig> getChildren() {
		// return children;
		// }
		//
		// public void setChildren(java.util.List<UserHoconConfig> children) {
		// this.children = children;
		// }
		//
		// public UserHoconConfig getParent() {
		// return parent;
		// }
		//
		// public void setParent(UserHoconConfig parent) {
		// this.parent = parent;
		// }

		@Override
		public String toString() {
			return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
		}

	}

}

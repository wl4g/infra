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
package com.wl4g.component.common.id;

import static com.wl4g.component.common.lang.Assert2.isTrueOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.lang.String.format;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Snowflake algorithms Id generator.
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2018年6月10日
 * @since
 * @see <a href=
 *      "https://github.com/twitter/snowflake">https://github.com/twitter/snowflake</a>
 */
public final class SnowflakeIdGenerator {

	/**
	 * {@link BitsDefine}
	 */
	private final BitsDefine def;
	// Worker node ID.
	private final long workerId;
	// Data center ID.
	private final long dataCenterId;
	// Sequence ID.
	private final AtomicLong sequence = new AtomicLong(0L);
	// Last generate timestamp.
	private final AtomicLong lastTime = new AtomicLong(-1L);

	public SnowflakeIdGenerator() {
		this(BitsDefine.StandardSafeJs, 0L, 0L, 0L);
	}

	public SnowflakeIdGenerator(BitsDefine def, long workerId, long dataCenterId, long sequence) {
		validate(def, workerId, dataCenterId, sequence);
		this.def = def;
		this.workerId = workerId;
		this.dataCenterId = dataCenterId;
		this.sequence.set(sequence);
	}

	/**
	 * Gets next global UID. </br>
	 * 
	 * @return
	 */
	public synchronized long nextId() {
		long now = timeGen();
		// 如果服务器时间有问题(时钟后退) 报错。
		if (now < lastTime.get()) {
			throw new IllegalStateException(
					format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTime.get() - now));
		}
		// 如果上次生成时间和当前时间相同,在同一毫秒内
		if (lastTime.get() == now) {
			// sequence自增，因为sequence只有12bit，所以和sequenceMask相与一下，去掉高位
			sequence.set(sequence.incrementAndGet() & def.sequenceMask);
			// 判断是否溢出,也就是每毫秒内超过4095，当为4096时，与sequenceMask相与，sequence就等于0
			if (sequence.get() == 0) {
				// 自旋等待到下一毫秒
				now = tilNextMillis(lastTime.get());
			}
		} else {
			// 如果和上次生成时间不同,重置sequence，就是下一毫秒开始，sequence计数重新从0开始累加
			sequence.set(0L);
		}
		lastTime.set(now);

		// Finally, the ID is calculated according to the rules.
		// 000000000000000000000000000000000000000000 00000 00000 000000000000
		// time dataCenterId workerId sequence
		return ((now - def.twepoch) << def.timestampLeftShift) | (dataCenterId << def.dataCenterIdShift)
				| (workerId << def.workerIdShift) | sequence.get();
	}

	/**
	 * Spin wait until next MS
	 * 
	 * @param lastTimestamp
	 * @return
	 */
	protected long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	/**
	 * Gets now timestamp.
	 * 
	 * @return
	 */
	protected long timeGen() {
		return System.currentTimeMillis();
	}

	/**
	 * Validation bits definition options.
	 * 
	 * @param def
	 * @param workerId
	 * @param dataCenterId
	 * @param sequence
	 */
	private void validate(BitsDefine def, long workerId, long dataCenterId, long sequence) {
		notNullOf(def, "bitsDefine");
		isTrueOf(workerId >= 0, "workerId>=0");
		isTrueOf(dataCenterId >= 0, "dataCenterId>=0");
		isTrueOf(sequence >= 0, "sequence>=0");
		isTrueOf(def.sequenceBits >= 0, "sequenceBits>=0");
		if (workerId > def.maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(format("worker Id can't be greater than %d or less than 0", def.maxWorkerId));
		}
		if (dataCenterId > def.maxDatacenterId || dataCenterId < 0) {
			throw new IllegalArgumentException(
					format("datacenter Id can't be greater than %d or less than 0", def.maxDatacenterId));
		}
	}

	/**
	 * Gets {@link SnowflakeIdGenerator} default instance.
	 * 
	 * @return
	 */
	public final static SnowflakeIdGenerator getDefault() {
		return SingletionHolder.instance;
	}

	/**
	 * {@link SingletionHolder} singletion holder
	 *
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2020年6月10日
	 * @since
	 */
	private static final class SingletionHolder {
		private static final SnowflakeIdGenerator instance = new SnowflakeIdGenerator();
	}

	/**
	 * {@link BitsDefine}
	 *
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2020-10-09
	 * @since
	 */
	public static final class BitsDefine {
		// Epoch (e.g: 1288834974657L => Thu, 04 Nov 2010 01:42:54 GMT)
		final long twepoch;
		// Node ID length bits.
		final long workerIdBits;
		// Data Center ID length bits.
		final long dataCenterIdBits;
		// The maximum number of machine nodes supported is 0-31, a total of 32
		final long maxWorkerId;
		// The maximum number of data center nodes supported is 0-31, a total of
		// 32
		final long maxDatacenterId;
		// Serial number length bits.
		final long sequenceBits;
		// Machine node left shift length bits.
		final long workerIdShift;
		// Data center node moves length bits.
		final long dataCenterIdShift;
		// Time milliseconds shift length bits.
		final long timestampLeftShift;
		// Sequence mask bits.
		final long sequenceMask;

		public BitsDefine(long twepoch, long workerIdBits, long dataCenterIdBits, long sequenceBits) {
			this.twepoch = twepoch;
			this.workerIdBits = workerIdBits;
			this.dataCenterIdBits = dataCenterIdBits;
			// The maximum number of machine nodes supported is 0-31, a total of
			// 32
			this.maxWorkerId = -1L ^ (-1L << workerIdBits);
			// The maximum number of data center nodes supported is 0-31, a
			// total of 32
			this.maxDatacenterId = -1L ^ (-1L << dataCenterIdBits);
			// Serial number. (e.g: 12 digits)
			this.sequenceBits = sequenceBits;
			// Machine node left shift bits. (e.g: 12 bits)
			this.workerIdShift = sequenceBits;
			// Data center node moves left bits. (e.g: 17 bits)
			this.dataCenterIdShift = sequenceBits + workerIdBits;
			// Time milliseconds shift left bits. (e.g: 22 bits)
			this.timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
			// Sequence mask bits. (e.g: 4095 bits).
			this.sequenceMask = -1L ^ (-1L << sequenceBits);
			// Validation
			isTrueOf(twepoch >= 0, "twepoch>=0");
			isTrueOf(workerIdBits >= 0, "workerIdBits>=0");
			isTrueOf(dataCenterIdBits >= 0, "dataCenterIdBits>=0");
			isTrueOf(sequenceBits >= 0, "sequenceBits>=0");
		}

		/**
		 * Generates a decimal integer with a maximum length of 16, which is
		 * JavaScript compatible (i.e., IEEE 754 specification).
		 * 
		 * <pre>
		 * twepoch=1288834974657L => Thu, 04 Nov 2010 01:42:54 GMT
		 * </pre>
		 */
		public static final BitsDefine StandardSafeJs = new BitsDefine(1288834974657L, 2L, 2L, 10L);

		/**
		 * Generates a decimal integer with a maximum length of 19. <b>Note:</b>
		 * that it is not compatible with JavaScript (does not follow the IEEE
		 * 754 specification), and is suitable for high concurrency and non web
		 * browser systems. The system of web browser recommends using the safe
		 * option: {@link #StandardSafeJs}.
		 * 
		 * <pre>
		 * twepoch=1288834974657L => Thu, 04 Nov 2010 01:42:54 GMT
		 * </pre>
		 */
		public static final BitsDefine LargeUnsafeJs = new BitsDefine(1288834974657L, 5L, 5L, 12L);

	}

}
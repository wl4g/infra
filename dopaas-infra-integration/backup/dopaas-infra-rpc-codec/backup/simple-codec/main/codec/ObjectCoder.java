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
package com.wl4g.component.integration.codec;

import com.wl4g.component.integration.codec.internal.DefaultCodec;
import com.wl4g.component.integration.codec.type.OCObject;

/**
 * ObjectCoder is the main API class for developer. It uses
 * {@linkplain com.wl4g.component.integration.codec.internal.DefaultCodec DefaultCodec} as
 * default codec. Developer can customize own codec for the coder. Developer can
 * use {@linkplain com.wl4g.component.integration.codec.CodecConfig CodecConfig} to
 * configure codec parameters.<br>
 * 
 * Example:
 * 
 * <pre>
 *  ObjectCoder coder = new ObjectCoder();
 *  coder.getCodecConfig().setEndianType(EndianType.LITTLE);
 *  coder.getCodecConfig().setTotalLengthType(TotalLengthType.HEAD_BODY);
 *  coder.getCodecConfig().setAutoLength(false);
 *  coder.getCodecConfig().setCacheType(CacheType.LOCAL);
 *  coder.getCodecConfig().addWrap(new IdentifyWrapper(new OCInt16(0xfafb)));
 *  coder.getCodecConfig().addWrap(VerifyWrapper.CRC16());
 *      ...
 *  SendMessage sm = ...
 *  byte[] bytes = oc.encode(sm);
 *      ...
 *  RecvMessage rm = ...
 *  oc.decode(bytes, rm);
 *      ...
 * </pre>
 * 
 * ObjectCoder.java
 * 
 * @version 1.0.0
 * @author Wanglsir
 */
public class ObjectCoder {

	private Codec codec;

	private CodecConfig codecConfig;

	private volatile boolean actived = false;

	private Object mutex = new Object();

	/**
	 * Construct ObjectCoder by
	 * {@linkplain com.wl4g.component.integration.codec.internal.DefaultCodec
	 * DefaultCodec}.
	 */
	public ObjectCoder() {
		initialize();
		codec = new DefaultCodec(codecConfig);
	}

	/**
	 * Construct ObjectCoder by
	 * {@linkplain com.wl4g.component.integration.codec.internal.DefaultCodec
	 * DefaultCodec}.
	 * 
	 * @param codecConfig
	 *            Codec configuration
	 *            {@linkplain com.wl4g.component.integration.codec.CodecConfig
	 *            CodecConfig}.
	 */
	public ObjectCoder(CodecConfig codecConfig) {
		this.codecConfig = codecConfig;
		initialize();
		codec = new DefaultCodec(codecConfig);
	}

	/**
	 * Construct ObjectCoder by custom codec.
	 * 
	 * @param codec
	 *            Custom codec.
	 */
	public ObjectCoder(Codec codec) {
		initialize();
		this.codec = codec;
		if (codec.getCodecConfig() == null) {
			codec.setCodecConfig(codecConfig);
		}
	}

	private void initialize() {
		if (codecConfig == null) {
			codecConfig = new CodecConfig();
		}
	}

	/**
	 * Encode message java object. Java object will construct the
	 * {@linkplain com.wl4g.component.integration.codec.type.OCObject OCObject} to
	 * encode data really.
	 * 
	 * @param msg
	 *            Message java object.
	 * @return If successful encoding, return bytes array. Otherwise return
	 *         null. If runtime exception occurred when encoding, it will throw
	 *         EncodingException, ReflectException and other wrapper's
	 *         OCException.
	 * @throws Exception
	 *             exception
	 */
	public byte[] encode(Object msg) throws Exception {
		if (codec == null) {
			return null;
		}
		checkActivated();
		return codec.encode(new OCObject(msg));
	}

	/**
	 * Encode message {@linkplain com.wl4g.component.integration.codec.type.OCObject
	 * OCObject}.
	 * 
	 * @param msg
	 *            Message {@linkplain com.wl4g.component.integration.codec.type.OCObject
	 *            OCObject}.
	 * @return If successful encoding, return bytes array. Otherwise return
	 *         null. If runtime exception occurred when encoding, it will throw
	 *         EncodingException, ReflectException and other wrapper's
	 *         OCException.
	 * @throws Exception
	 *             exception
	 */
	public byte[] encode(OCObject msg) throws Exception {
		if (codec == null) {
			return null;
		}
		checkActivated();
		return codec.encode(msg);
	}

	/**
	 * Decode message java object. Java object will construct the
	 * {@linkplain com.wl4g.component.integration.codec.type.OCObject OCObject} to
	 * decode data really.
	 * 
	 * @param bytes
	 *            Message bytes array.
	 * @param source
	 *            Decode target java object.
	 * @return If successful decoding, return
	 *         {@linkplain com.wl4g.component.integration.codec.type.OCObject OCObject}.
	 *         Otherwise return null. If runtime exception occurred when
	 *         encoding, it will throw DecodingException, ReflectException and
	 *         other wrapper's OCException.
	 * @throws Exception
	 */
	public OCObject decode(byte[] bytes, Object source) throws Exception {
		if (codec == null) {
			return null;
		}
		checkActivated();
		return codec.decode(bytes, new OCObject(source));
	}

	/**
	 * Decode message {@linkplain com.wl4g.component.integration.codec.type.OCObject
	 * OCObject}.
	 * 
	 * @param bytes
	 *            Message bytes array.
	 * @param source
	 *            Decode target
	 *            {@linkplain com.wl4g.component.integration.codec.type.OCObject
	 *            OCObject}.
	 * @return If successful decoding, return
	 *         {@linkplain com.wl4g.component.integration.codec.type.OCObject OCObject}.
	 *         Otherwise return null. If runtime exception occurred when
	 *         encoding, it will throw DecodingException, ReflectException and
	 *         other wrapper's OCException.
	 * @throws Exception
	 *             exception
	 */
	public OCObject decode(byte[] bytes, OCObject source) throws Exception {
		if (codec == null) {
			return null;
		}
		checkActivated();
		return codec.decode(bytes, source);
	}

	private void checkActivated() {
		if (!actived) {
			synchronized (mutex) {
				if (!actived) {
					codec.activated();
					actived = true;
				}
			}
		}
	}

	public Codec getCodec() {
		return codec;
	}

	public void setCodec(Codec codec) {
		this.codec = codec;
	}

	public CodecConfig getCodecConfig() {
		return codecConfig;
	}

}
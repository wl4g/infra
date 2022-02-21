package com.wl4g.component.integration.codec.simple;

import com.wl4g.component.integration.codec.ObjectCoder;
import com.wl4g.component.integration.codec.CodecConfig.EndianType;
import com.wl4g.component.integration.codec.CodecConfig.TotalLengthType;
import com.wl4g.component.integration.codec.helper.ByteHelper;
import com.wl4g.component.integration.codec.type.OCInt16;
import com.wl4g.component.integration.codec.type.OCInt8;
import com.wl4g.component.integration.codec.wrap.IdentifyWrapper;

public class SimpleCoder {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		simpleCode();
		simpleCodeTotalHeadBody();
		simpleCodeAutoLengtth();
	}

	private static void simpleCode() throws Exception {
		ObjectCoder coder = new ObjectCoder();
		coder.getCodecConfig().setEndianType(EndianType.LITTLE);
		coder.getCodecConfig().addWrap(new IdentifyWrapper(new OCInt16(0xfafb), new OCInt8(0xFF)));
		SimpleMsg msg = new SimpleMsg();
		msg.id = 32;
		msg.version = 1;
		msg.command = "running";
		byte[] bytes = coder.encode(msg);
		System.out.println(ByteHelper.toHexString(bytes));

		SimpleMsg result = new SimpleMsg();
		coder.decode(bytes, result);
		System.out.println("ID:" + result.id);
		System.out.println("VERSION:" + result.version);
		System.out.println("COMMAND:" + result.command);
	}

	private static void simpleCodeTotalHeadBody() throws Exception {
		ObjectCoder coder = new ObjectCoder();
		coder.getCodecConfig().setEndianType(EndianType.LITTLE);
		coder.getCodecConfig().setTotalLengthType(TotalLengthType.HEAD_BODY);
		coder.getCodecConfig().addWrap(new IdentifyWrapper(new OCInt16(0xFAFB)));
		SimpleMsg msg = new SimpleMsg();
		msg.id = 32;
		msg.version = 1;
		msg.command = "running";
		byte[] bytes = coder.encode(msg);
		System.out.println(ByteHelper.toHexString(bytes));
	}

	private static void simpleCodeAutoLengtth() throws Exception {
		ObjectCoder coder = new ObjectCoder();
		coder.getCodecConfig().setEndianType(EndianType.LITTLE);
		coder.getCodecConfig().setTotalLengthType(TotalLengthType.HEAD_BODY);
		coder.getCodecConfig().setAutoLength(true);
		coder.getCodecConfig().addWrap(new IdentifyWrapper(new OCInt16(0xFAFB)));
		SimpleMsg msg = new SimpleMsg();
		msg.id = 32;
		msg.version = 1;
		msg.command = "running";
		byte[] bytes = coder.encode(msg);
		System.out.println(ByteHelper.toHexString(bytes));
	}
}

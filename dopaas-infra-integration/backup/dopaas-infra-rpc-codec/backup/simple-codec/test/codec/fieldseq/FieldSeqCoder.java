package com.wl4g.component.integration.codec.fieldseq;

import com.wl4g.component.integration.codec.ObjectCoder;
import com.wl4g.component.integration.codec.CodecConfig.EndianType;
import com.wl4g.component.integration.codec.helper.ByteHelper;
import com.wl4g.component.integration.codec.type.OCInt16;
import com.wl4g.component.integration.codec.type.OCInt8;
import com.wl4g.component.integration.codec.wrap.IdentifyWrapper;

public class FieldSeqCoder {

	public static void main(String[] args) throws Exception {
		codeFieldMsg();
		codeFieldCodecMsg();
	}

	private static void codeFieldMsg() throws Exception {
		ObjectCoder coder = new ObjectCoder();
		coder.getCodecConfig().setEndianType(EndianType.LITTLE);
		coder.getCodecConfig().addWrap(new IdentifyWrapper(new OCInt16(0xfafb), new OCInt8(0xFF)));
		FieldMsg msg = new FieldMsg();
		msg.id = 32;
		msg.version = 1;
		msg.command = "running";
		byte[] bytes = coder.encode(msg);
		System.out.println(ByteHelper.toHexString(bytes));

		FieldMsg result = new FieldMsg();
		coder.decode(bytes, result);
		System.out.println("ID:" + result.id);
		System.out.println("VERSION:" + result.version);
		System.out.println("COMMAND:" + result.command);
	}

	private static void codeFieldCodecMsg() throws Exception {
		ObjectCoder coder = new ObjectCoder();
		coder.getCodecConfig().setEndianType(EndianType.LITTLE);
		coder.getCodecConfig().addWrap(new IdentifyWrapper(new OCInt16(0xfafb), new OCInt8(0xFF)));
		FieldCodecMsg msg = new FieldCodecMsg();
		msg.id = 32;
		msg.version = 1;
		msg.command.setValue("running");
		byte[] bytes = coder.encode(msg);
		System.out.println(ByteHelper.toHexString(bytes));

		FieldCodecMsg result = new FieldCodecMsg();
		coder.decode(bytes, result);
		System.out.println("ID:" + result.id);
		System.out.println("VERSION:" + result.version);
		System.out.println("COMMAND:" + result.command.getValue());
	}

}

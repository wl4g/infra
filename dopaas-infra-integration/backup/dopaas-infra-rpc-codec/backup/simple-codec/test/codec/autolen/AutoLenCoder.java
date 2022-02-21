package com.wl4g.component.integration.codec.autolen;

import com.wl4g.component.integration.codec.ObjectCoder;
import com.wl4g.component.integration.codec.CodecConfig.EndianType;
import com.wl4g.component.integration.codec.CodecConfig.TotalLengthType;
import com.wl4g.component.integration.codec.helper.ByteHelper;
import com.wl4g.component.integration.codec.type.OCInt16;
import com.wl4g.component.integration.codec.wrap.IdentifyWrapper;

public class AutoLenCoder {

	public static void main(String[] args) throws Exception {
		encodeAutoLength();
	}

	private static void encodeAutoLength() throws Exception {
		ObjectCoder coder = new ObjectCoder();
		coder.getCodecConfig().setEndianType(EndianType.LITTLE);
		coder.getCodecConfig().setTotalLengthType(TotalLengthType.HEAD_BODY);
		coder.getCodecConfig().setAutoLength(true);
		coder.getCodecConfig().addWrap(new IdentifyWrapper(new OCInt16(0xFAFB)));
		MultiCmdMsg msg = new MultiCmdMsg();
		msg.id = 32;
		msg.version = 1;
		msg.command1 = "ready";
		msg.command2 = "running";
		byte[] bytes = coder.encode(msg);
		System.out.println(ByteHelper.toHexString(bytes));
	}
}

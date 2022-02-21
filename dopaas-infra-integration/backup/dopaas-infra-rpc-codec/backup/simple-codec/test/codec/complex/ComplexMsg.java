package com.wl4g.component.integration.codec.complex;

import com.wl4g.component.integration.codec.type.OCBytes;
import com.wl4g.component.integration.codec.type.OCInt16;
import com.wl4g.component.integration.codec.type.OCInt32;
import com.wl4g.component.integration.codec.type.OCInt8;
import com.wl4g.component.integration.codec.type.OCInteger;
import com.wl4g.component.integration.codec.type.OCMap;
import com.wl4g.component.integration.codec.type.OCObject;
import com.wl4g.component.integration.codec.type.OCString;

public class ComplexMsg {
	int id;

	byte version;

	OCInt16 externLength = new OCInt16();

	OCBytes extern = new OCBytes(externLength);

	OCInt32 subMsgLength = new OCInt32();

	ComplexSubMsg subMsg = new ComplexSubMsg(subMsgLength);
}

class ComplexSubMsg extends OCObject {

	OCInt8 codeLen = new OCInt8();

	OCString equipCode = new OCString(codeLen);

	byte modular;

	byte subModular;

	OCMap<Byte, OrderMsg> commands = new OCMap<Byte, OrderMsg>();

	public ComplexSubMsg(OCInteger lenType) {
		super(lenType);
	}

}

class OrderMsg {
	OCInt16 orderLength = new OCInt16();

	OCBytes order = new OCBytes(orderLength);

	public OrderMsg() {
	}

	public OrderMsg(OCBytes order) {
		this.order = order;
		order.setLenType(orderLength);
	}

}

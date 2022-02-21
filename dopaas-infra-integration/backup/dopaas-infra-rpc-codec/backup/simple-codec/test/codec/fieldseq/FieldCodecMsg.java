package com.wl4g.component.integration.codec.fieldseq;

import com.wl4g.component.integration.codec.type.OCObject;
import com.wl4g.component.integration.codec.type.OCString;

public class FieldCodecMsg extends OCObject {
	public FieldCodecMsg() {
		setFieldSequence(new String[] { "id", "version", "command" });
	}

	OCString command = new OCString();
	int id;
	byte version;
}

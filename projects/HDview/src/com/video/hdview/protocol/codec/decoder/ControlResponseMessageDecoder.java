package com.video.hdview.protocol.codec.decoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.video.hdview.protocol.message.Constants;
import com.video.hdview.protocol.message.ControlResponse;

public class ControlResponseMessageDecoder extends
		AbstractMessageDecoder<ControlResponse> {

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.CONTROL_ANSWER;
	}

	@Override
	protected ControlResponse decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		ControlResponse c = new ControlResponse();
		
		c.setResult(body.getUnsignedShort());
		c.setReserve(body.getUnsignedShort());
		
		return c;
	}

}

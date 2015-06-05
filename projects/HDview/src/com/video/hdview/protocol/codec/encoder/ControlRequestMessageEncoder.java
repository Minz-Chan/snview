package com.video.hdview.protocol.codec.encoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.video.hdview.protocol.message.Constants;
import com.video.hdview.protocol.message.ControlRequest;

public class ControlRequestMessageEncoder extends
		AbstractMessageEncoder<ControlRequest> {

	@Override
	protected int getMessageLength() {
		return Constants.MSG_LEN.CONTROL_REQUEST;
	}

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.CONTROL_REQUEST;
	}

	@Override
	protected void encodeBody(IoSession session, ControlRequest message,
			IoBuffer out) {
		out.putUnsignedInt(message.getDeviceId());
		out.putUnsigned((byte) (message.getChannel() - 1));
		out.putUnsigned((byte) message.getCmdCode());
		out.putUnsignedShort(message.getSize());
	}

}

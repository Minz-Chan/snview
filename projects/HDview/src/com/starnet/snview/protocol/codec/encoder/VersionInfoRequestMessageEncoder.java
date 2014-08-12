package com.starnet.snview.protocol.codec.encoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.protocol.message.VersionInfoRequest;

public class VersionInfoRequestMessageEncoder extends AbstractMessageEncoder<VersionInfoRequest>{

	@Override
	protected void encodeBody(IoSession session, VersionInfoRequest message,
			IoBuffer out) {
		out.putUnsignedShort(message.getVersionMajor());
		out.putUnsignedShort(message.getVersionMinor());		
	}

	@Override
	protected int getMessageLength() {
		return Constants.MSG_LEN.VERSION_INFO_REQUEST;
	}

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.VERSION_INFO_REQUEST;
	}
}

package com.video.hdview.protocol.codec.decoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.video.hdview.protocol.message.Constants;
import com.video.hdview.protocol.message.VersionInfoRequest;

public class VersionInfoRequestMessageDecoder extends AbstractMessageDecoder<VersionInfoRequest> {
	
	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.VERSION_INFO_REQUEST;
	}

	@Override
	protected VersionInfoRequest decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		VersionInfoRequest v = new VersionInfoRequest();
		v.setVersionMajor(body.getUnsignedShort());
		v.setVersionMinor(body.getUnsignedShort());

		return v;
	}

	
}

package com.video.hdview.protocol.codec.decoder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.video.hdview.protocol.message.Constants;
import com.video.hdview.protocol.message.LoginResponse;

public class LoginResponseMessageDecoder extends
		AbstractMessageDecoder<LoginResponse> {

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.LOGIN_ANSWER;
	}

	@Override
	protected LoginResponse decodeBody(IoSession session, IoBuffer body)
			throws Exception {
		LoginResponse lr = new LoginResponse();
		
		lr.setResult(body.getUnsignedShort());
		lr.setReserve(body.getUnsignedShort());
		
		return lr;
	}

}

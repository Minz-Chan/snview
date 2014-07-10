package com.starnet.snview.protocol.codec.encoder;

import java.nio.ByteOrder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.protocol.message.LoginRequest;

public class LoginRequestMessageEncoder extends AbstractMessageEncoder<LoginRequest> {

	@Override
	protected void encodeBody(IoSession session, LoginRequest message,
			IoBuffer out) {
		IoBuffer tmp = IoBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
		
		String userName = message.getUserName().trim();
		if (userName == null || userName.length() > 32) {
			throw new IllegalArgumentException(
					"Error userName of LoginRequest, be null or it's length is greater than 32.");
		}
		tmp.put(userName.getBytes());
		tmp.rewind();
		out.put(tmp);  // put userName
		
		tmp.clear();
		tmp.limit(16);
		
		String password = message.getPassword().trim();
		if (password == null || password.length() > 16) {
			throw new IllegalArgumentException(
					"Error password of LoginRequest, be null or it's length is greater than 16.");
		}
		tmp.put(password.getBytes());
		tmp.rewind();
		out.put(tmp);  // put password
		
		out.putInt(message.getDeviceId());  // put deviceId
		out.put((byte)1);  // should be set to 1 to be compatible with the previous version
		out.put((byte)(message.getChannel() - 1) );  // put channel, start from 0
		out.put((byte)message.getReserve()[0]);  // reserve[0]
		out.put((byte)message.getReserve()[1]);  // reserve[1]
		
	}

	@Override
	protected int getMessageLength() {
		return Constants.MSG_LEN.LOGIN_REQUEST;
	}

	@Override
	protected int getMessageType() {
		return Constants.MSG_TYPE.LOGIN_REQUEST;
	}

}

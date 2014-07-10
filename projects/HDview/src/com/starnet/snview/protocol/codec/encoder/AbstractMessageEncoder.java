package com.starnet.snview.protocol.codec.encoder;

import java.nio.ByteOrder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

public abstract class AbstractMessageEncoder<T> implements MessageEncoder<T> {

	@Override
	public void encode(IoSession session, T message, ProtocolEncoderOutput out)
			throws Exception {
		int msgLen = getMessageLength();
		int msgType = getMessageType();
		
		if (msgLen == 0 || msgType == 0) {
			throw new IllegalArgumentException("Invalid message length or type.");
		}
		
		IoBuffer obj = IoBuffer.allocate(msgLen + 4).order(ByteOrder.LITTLE_ENDIAN);
		
		obj.putShort((short)msgType);
		obj.putShort((short)msgLen);
		
		IoBuffer objBody =  IoBuffer.allocate(msgLen).order(ByteOrder.LITTLE_ENDIAN);
		encodeBody(session, message, objBody);
		objBody.rewind();  // set the position to zero
		
		if (objBody.remaining() != msgLen) {
			throw new IllegalStateException("Real message length "
					+ objBody.remaining()
					+ " can't match the specified message length "
					+ msgLen);
		}
		
		obj.put(objBody); // merge the encoded data of body into TLV object
							// buffer
		obj.flip();
		out.write(obj.array());
		
	}

	protected abstract int getMessageLength();
	protected abstract int getMessageType();
	protected abstract void encodeBody(IoSession session, T message, IoBuffer out);
}

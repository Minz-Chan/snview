package com.video.hdview.protocol.codec.decoder;

import java.nio.ByteOrder;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

public abstract class AbstractMessageDecoder<T> implements MessageDecoder {

	@Override
	public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
		in.order(ByteOrder.LITTLE_ENDIAN);
		
		// If buffer length is greater than TLV header's 		
		if ( in.remaining() < 4 ) {
			return MessageDecoderResult.NEED_DATA;
		}
		
		int type = in.getUnsignedShort();
		
		// Check if buffer contains a complete TLV
		int length = in.getUnsignedShort();
		if ( in.remaining() < length) {
			return MessageDecoderResult.NEED_DATA;
		}
		
		// Check if type is expected
		if ( type != getMessageType() ) {
			return MessageDecoderResult.NOT_OK;
		}
		
		return MessageDecoderResult.OK;
	}

	@Override
	public MessageDecoderResult decode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		in.order(ByteOrder.LITTLE_ENDIAN);
		in.getUnsignedShort();  // skip TLV type

		byte[] bodyData = new byte[in.getUnsignedShort()];
		in.get(bodyData);  // get body data of TLV structure
		
		T obj = decodeBody(session, IoBuffer.wrap(bodyData).order(ByteOrder.LITTLE_ENDIAN));
		
		out.write(obj);
//		out.write(bodyData); // transfer body data to next processor
		
		return MessageDecoderResult.OK;
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out)
			throws Exception {
		// TODO Auto-generated method stub

	}
	
	protected abstract int getMessageType();
	protected abstract T decodeBody(IoSession session, IoBuffer body) throws Exception;
}

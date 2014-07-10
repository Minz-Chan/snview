package com.starnet.snview.protocol.codec.encoder;

import java.nio.ByteBuffer;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;


public class TlvMessageEncoder implements MessageEncoder<ByteBuffer> {
	@Override
	public void encode(IoSession session, ByteBuffer message,
			ProtocolEncoderOutput out) throws Exception {
		out.write(IoBuffer.wrap(message));				
	}

}


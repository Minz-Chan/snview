package com.video.hdview.protocol.codec.encoder;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import com.video.hdview.protocol.message.OwspEnd;

public class OwspEndMessageEncoder implements MessageEncoder<OwspEnd> {

	@Override
	public void encode(IoSession session, OwspEnd message,
			ProtocolEncoderOutput out) throws Exception {
		out.write(message);
	}

}

package com.starnet.snview.protocol.codec.encoder;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import com.starnet.snview.protocol.message.OwspBegin;

public class OwspBeginMessageEncoder implements MessageEncoder<OwspBegin> {

	@Override
	public void encode(IoSession session, OwspBegin message,
			ProtocolEncoderOutput out) throws Exception {
		out.write(message);		
	}

}

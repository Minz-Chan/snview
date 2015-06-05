package com.video.hdview.protocol.codec.factory;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.video.hdview.protocol.codec.decoder.OwspDecoder;
import com.video.hdview.protocol.codec.encoder.OwspEncoder;

public class OwspFactory implements ProtocolCodecFactory {

	private ProtocolEncoder encoder;
    private ProtocolDecoder decoder;
	
	public OwspFactory() {
		this.encoder = new OwspEncoder();
		this.decoder = new OwspDecoder();
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		return encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		return decoder;
	}

}

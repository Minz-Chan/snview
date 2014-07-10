package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

public class SimpleBufferMessageHandler implements MessageHandler<SimpleBufferAllocator> {

	@Override
	public void handleMessage(IoSession session, SimpleBufferAllocator message)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}

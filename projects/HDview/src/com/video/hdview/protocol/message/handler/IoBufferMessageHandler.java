package com.video.hdview.protocol.message.handler;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

public class IoBufferMessageHandler implements MessageHandler<IoBuffer> {

	@Override
	public void handleMessage(IoSession session, IoBuffer message)
			throws Exception {
		System.out.println("IoBffer is arrived...");
		
	}

}

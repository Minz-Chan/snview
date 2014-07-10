package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.protocol.message.DVSInfoRequest;

public class DVSInfoRequestMessageHandler implements
		MessageHandler<DVSInfoRequest> {

	@Override
	public void handleMessage(IoSession session, DVSInfoRequest message)
			throws Exception {
		System.out.println("DVSInfoRequest is arrived...");	
	}

}

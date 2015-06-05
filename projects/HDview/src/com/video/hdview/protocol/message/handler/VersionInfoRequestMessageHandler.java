package com.video.hdview.protocol.message.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.video.hdview.protocol.message.VersionInfoRequest;

public class VersionInfoRequestMessageHandler implements MessageHandler<VersionInfoRequest> {

	@Override
	public void handleMessage(IoSession session, VersionInfoRequest message)
			throws Exception {
		System.out.println("VersionInfoRequest is arrived...");
		
	}
}

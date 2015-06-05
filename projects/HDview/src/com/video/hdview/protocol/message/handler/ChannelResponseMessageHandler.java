package com.video.hdview.protocol.message.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.video.hdview.protocol.message.ChannelResponse;

public class ChannelResponseMessageHandler implements
		MessageHandler<ChannelResponse> {

	@Override
	public void handleMessage(IoSession session, ChannelResponse message)
			throws Exception {
		System.out.println("ChannelReponse is arrived");
		
	}

}

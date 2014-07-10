package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.protocol.message.VideoPFrameData;

public class VideoPFrameDataMessageHandler implements
		MessageHandler<VideoPFrameData> {

	@Override
	public void handleMessage(IoSession session, VideoPFrameData message)
			throws Exception {
		System.out.println("VideoPFrameData is arrived...");
		
	}

}

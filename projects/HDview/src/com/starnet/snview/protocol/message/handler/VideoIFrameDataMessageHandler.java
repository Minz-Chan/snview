package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.protocol.message.VideoIFrameData;

public class VideoIFrameDataMessageHandler implements
		MessageHandler<VideoIFrameData> {

	@Override
	public void handleMessage(IoSession session, VideoIFrameData message)
			throws Exception {
		System.out.println("VideoIFrameData is arrived...");
		
	}

}

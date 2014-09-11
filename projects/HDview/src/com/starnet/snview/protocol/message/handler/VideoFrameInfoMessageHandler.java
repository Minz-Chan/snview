package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.protocol.message.VideoFrameInfo;

public class VideoFrameInfoMessageHandler implements
		MessageHandler<VideoFrameInfo> {

	@Override
	public void handleMessage(IoSession session, VideoFrameInfo message)
			throws Exception {
		//System.out.println("VideoFrameInfo is arrived...");
		
	}

}

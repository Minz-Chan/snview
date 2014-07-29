package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.protocol.message.VideoFrameInfoEx;

public class VideoFrameInfoExMessageHandler implements
		MessageHandler<VideoFrameInfoEx> {

	@Override
	public void handleMessage(IoSession arg0, VideoFrameInfoEx arg1)
			throws Exception {
		System.out.println("VideoFrameInfoEx is arrived...");
		
	}

}

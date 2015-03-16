package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import android.util.Log;

import com.starnet.snview.protocol.message.VideoFrameInfo;

public class VideoFrameInfoMessageHandler implements
		MessageHandler<VideoFrameInfo> {
	private static final String TAG = "VideoFrameInfoMessageHandler";
	
	private AttributeKey DATA_EXCEED_64KB = new AttributeKey(VideoFrameInfoExMessageHandler.class, "dataExceed64Kb");

	@Override
	public void handleMessage(IoSession session, VideoFrameInfo message)
			throws Exception {
		Log.d(TAG, "VideoFrameInfo is arrived...");
		Boolean dataExceed64Kb = (Boolean) session.getAttribute(DATA_EXCEED_64KB);
		if (dataExceed64Kb != null) {
			session.setAttribute(DATA_EXCEED_64KB, null);
		}			
	}
}

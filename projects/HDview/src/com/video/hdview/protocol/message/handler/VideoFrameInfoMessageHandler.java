package com.video.hdview.protocol.message.handler;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import android.util.Log;

import com.video.hdview.protocol.message.VideoFrameInfo;

public class VideoFrameInfoMessageHandler implements
		MessageHandler<VideoFrameInfo> {
	private static final String TAG = "VideoFrameInfoMessageHandler";
	
	private AttributeKey DATA_EXCEED_64KB = new AttributeKey(VideoFrameInfoExMessageHandler.class, "dataExceed64Kb");
	private AttributeKey CURR_VIDEO_TIMESTAMP = new AttributeKey(VideoFrameInfoExMessageHandler.class, "currentVideoTimestamp");

	@Override
	public void handleMessage(IoSession session, VideoFrameInfo message)
			throws Exception {
		Log.d(TAG, "VideoFrameInfo is arrived...");
		Boolean dataExceed64Kb = (Boolean) session.getAttribute(DATA_EXCEED_64KB);
		if (dataExceed64Kb != null) {
			session.setAttribute(DATA_EXCEED_64KB, null);
		}			
		Long currVideoTimestamp = (Long) session.getAttribute(CURR_VIDEO_TIMESTAMP);
		if (currVideoTimestamp != null) {
			session.setAttribute(CURR_VIDEO_TIMESTAMP, Long.valueOf(message.getTime()));
		}
	}
}

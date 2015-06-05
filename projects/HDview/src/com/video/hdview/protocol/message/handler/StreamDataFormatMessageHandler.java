package com.video.hdview.protocol.message.handler;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import android.util.Log;

import com.video.hdview.R;
import com.video.hdview.component.h264.H264DecodeUtil;
import com.video.hdview.component.liveview.LiveViewItemContainer;
import com.video.hdview.protocol.Connection;
import com.video.hdview.protocol.message.Constants;
import com.video.hdview.protocol.message.StreamDataFormat;

public class StreamDataFormatMessageHandler implements
		MessageHandler<StreamDataFormat> {
	private static final String TAG = "StreamDataFormatMessageHandler";

	private AttributeKey CONNECTION = new AttributeKey(Connection.class, "connection");
	private AttributeKey CURR_VIDEO_TIMESTAMP = new AttributeKey(VideoFrameInfoExMessageHandler.class, "currentVideoTimestamp");
	private AttributeKey PRE_VIDEO_TIMESTAMP = new AttributeKey(VideoFrameInfoExMessageHandler.class, "pretVideoTimestamp");
	private H264DecodeUtil h264;
	
	private Connection connection;
	private LiveViewItemContainer lvContainer;	
	
	@Override
	public void handleMessage(IoSession session, StreamDataFormat message)
			throws Exception {
		Log.d(TAG, "StreamDataFormat is arrived...");
		if (connection == null) {
			connection = (Connection) session.getAttribute(CONNECTION);
		}
		
		if (h264 == null) {
			h264 = connection.getH264decoder();
		}
		
		if (lvContainer == null || connection.isShowComponentChanged()) {
			lvContainer = connection.getLiveViewItemContainer();
		}
		
		
		session.setAttribute(CURR_VIDEO_TIMESTAMP, Long.valueOf(0));
		session.setAttribute(PRE_VIDEO_TIMESTAMP, Long.valueOf(0));
		
		int width = message.getVideoDataFormat().getWidth();
		int height = message.getVideoDataFormat().getHeight();
		int framerate = message.getVideoDataFormat().getFramerate();
		
		Log.d(TAG, "width:" + width + ", height:" + height + ", fps:" + framerate);
		
		lvContainer.getVideoConfig().setFramerate(framerate);
		
		long codecId = message.getVideoDataFormat().getCodecId();
		if (codecId != 0 && codecId != Constants.CODEC_H264) {
			lvContainer.setWindowInfoContent(R.string.connection_response_unsupported_codec);
			lvContainer.setIsResponseError(true);
			session.close();
		}
		
		if (width > 0 && height > 0) {
			lvContainer.getVideoConfig().setWidth(width);
			lvContainer.getVideoConfig().setHeight(height);
			lvContainer.getSurfaceView().init(width, height);
			
			h264.init(width, height);
			
			// 判断分辨率种类
			if (connection.isValid()) {
				lvContainer.setWindowInfoContent(checkResolution(width, height));
			}
		}
		
	}
	
	private String checkResolution(int width, int height) {

		// 待实现
		
		return width + "x" + height;
	}
	
	private String getString(int resid) {
		if (lvContainer == null) {
			return null;
		}
		
		return lvContainer.getResources().getString(resid);
	}

}

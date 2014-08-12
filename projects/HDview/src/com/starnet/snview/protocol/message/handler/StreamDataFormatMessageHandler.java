package com.starnet.snview.protocol.message.handler;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.message.StreamDataFormat;

public class StreamDataFormatMessageHandler implements
		MessageHandler<StreamDataFormat> {
	private final AttributeKey LIVEVIEW_ITEM = new AttributeKey(Connection.class, "liveview_item");
	private final AttributeKey H264DECODER = new AttributeKey(Connection.class, "h264decoder");
	private H264DecodeUtil h264;
	
	private LiveViewItemContainer lvContainer;
	
	@Override
	public void handleMessage(IoSession session, StreamDataFormat message)
			throws Exception {
		if (h264 == null) {
			h264 = (H264DecodeUtil) session.getAttribute(H264DECODER);
		}
		
		if (lvContainer == null) {
			lvContainer = (LiveViewItemContainer) session.getAttribute(LIVEVIEW_ITEM);
		}
		
		
		
		
		
		System.out.println("StreamDataFormat is arrived...");
		int width = message.getVideoDataFormat().getWidth();
		int height = message.getVideoDataFormat().getHeight();
		
		if (width > 0 && height > 0) {
			h264.init(width, height);
			
			// 判断分辨率种类
			lvContainer.setWindowInfoContent(checkResolution(width, height));
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

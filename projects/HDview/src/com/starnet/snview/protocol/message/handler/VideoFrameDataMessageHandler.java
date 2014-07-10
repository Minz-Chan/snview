package com.starnet.snview.protocol.message.handler;


import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.liveview.LiveView;
import com.starnet.snview.component.liveview.OnLiveViewChangedListener;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.message.VideoFrameData;
import com.starnet.snview.protocol.message.VideoIFrameData;

public class VideoFrameDataMessageHandler implements MessageHandler<VideoFrameData> {
	private final AttributeKey LIVEVIEW_LISTENER = new AttributeKey(Connection.class, "liveview_listener");
	private final AttributeKey H264DECODER = new AttributeKey(Connection.class, "h264decoder");
	
	private H264DecodeUtil h264;
	private OnLiveViewChangedListener liveViewChangedListener;
	
	@Override
	public void handleMessage(IoSession session, VideoFrameData message) throws Exception {
		
		if (h264 == null) {
			h264 = (H264DecodeUtil) session.getAttribute(H264DECODER);
		}
		
		if (liveViewChangedListener == null) {
			liveViewChangedListener = (OnLiveViewChangedListener) session
					.getAttribute(LIVEVIEW_LISTENER);
			
		}
		
		// Decode the video data
		if (message instanceof VideoIFrameData) {
			System.out.println("$$$VideoIFrameData is arrvied...");
			h264.setbFirst(true);
			h264.setbFindPPS(true);
		} else {
			System.out.println("$$$VideoPFrameData is arrvied...");
		}
		h264.decodePacket(message.getData(), message.getData().length,
				((LiveView) liveViewChangedListener).retrievetDisplayBuffer());		
		
		// Update the video view
		if (liveViewChangedListener != null) {
			liveViewChangedListener.onDisplayContentUpdated();
		}
		
//		File f = new File("e:/test.h264");
//		FileOutputStream out =new FileOutputStream(f, true);
//		
//		out.write(message.getData());
//		
//		out.close();
		
		
	}

}

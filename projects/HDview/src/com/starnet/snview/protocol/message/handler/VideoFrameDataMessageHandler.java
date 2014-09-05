package com.starnet.snview.protocol.message.handler;


import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.liveview.LiveView;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.OnLiveViewChangedListener;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.protocol.message.VideoFrameData;
import com.starnet.snview.protocol.message.VideoIFrameData;

public class VideoFrameDataMessageHandler implements MessageHandler<VideoFrameData> {
//	private final AttributeKey LIVEVIEW_ITEM = new AttributeKey(Connection.class, "liveview_item");
//	private final AttributeKey LIVEVIEW_LISTENER = new AttributeKey(Connection.class, "liveview_listener");
//	private final AttributeKey CONNECTION_LISTENER = new AttributeKey(Connection.class, "connection_listener");
//	private final AttributeKey H264DECODER = new AttributeKey(Connection.class, "h264decoder");
	private AttributeKey CONNECTION = new AttributeKey(Connection.class, "connection");
	
	private H264DecodeUtil h264;
	private OnLiveViewChangedListener liveViewChangedListener;
	private boolean isDataArrived = false;
	
	private Connection connection;
	
	@Override
	public void handleMessage(IoSession session, VideoFrameData message) throws Exception {
		
		if (connection == null) {
			connection = (Connection) session.getAttribute(CONNECTION);
		}
		
		
		if (h264 == null) {
			h264 = connection.getH264decoder();
		}
		
		if (liveViewChangedListener == null) {
			liveViewChangedListener = connection.getLiveViewChangedListener();
			
		}
		
		
		if (!connection.isValid()) {
			session.close(true);
		}
		
		if (!isDataArrived) {
			isDataArrived = true;
			
			LiveViewItemContainer liveViewItemConatainer = connection.getLiveViewItemContainer();
			StatusListener connectionStatusListener = connection.getConnectionListener();
			if (liveViewItemConatainer != null && connectionStatusListener != null) {
				connectionStatusListener.OnConnectionBusy(liveViewItemConatainer);
			}
		}
		
		
		
		// 视频数据解码
		if (message instanceof VideoIFrameData) {
			System.out.println("$$$VideoIFrameData is arrvied...");
			h264.setbFirst(true);
			h264.setbFindPPS(true);
		} else {
			System.out.println("$$$VideoPFrameData is arrvied...");
		}
		h264.decodePacket(message.getData(), message.getData().length,
				((LiveView) liveViewChangedListener).retrievetDisplayBuffer());		
		
		// 更新视频显示
		if (liveViewChangedListener != null) {
			System.out.println(liveViewChangedListener + "@before onDisplayContentUpdated" );
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

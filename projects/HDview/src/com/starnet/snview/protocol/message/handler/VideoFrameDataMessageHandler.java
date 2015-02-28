package com.starnet.snview.protocol.message.handler;


import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import android.util.Log;

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.h264.H264Decoder;
import com.starnet.snview.component.h264.MP4Recorder;
import com.starnet.snview.component.liveview.LiveView;
import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.component.liveview.OnLiveViewChangedListener;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.protocol.Connection.StatusListener;
import com.starnet.snview.protocol.message.VideoFrameData;
import com.starnet.snview.protocol.message.VideoIFrameData;

public class VideoFrameDataMessageHandler implements MessageHandler<VideoFrameData> {
private static final String TAG = null;

	//	private final AttributeKey LIVEVIEW_ITEM = new AttributeKey(Connection.class, "liveview_item");
//	private final AttributeKey LIVEVIEW_LISTENER = new AttributeKey(Connection.class, "liveview_listener");
//	private final AttributeKey CONNECTION_LISTENER = new AttributeKey(Connection.class, "connection_listener");
//	private final AttributeKey H264DECODER = new AttributeKey(Connection.class, "h264decoder");
	private AttributeKey CONNECTION = new AttributeKey(Connection.class, "connection");
	
	private H264DecodeUtil h264;
	private OnLiveViewChangedListener liveViewChangedListener;
	private LiveViewItemContainer lvContainer;	
	private Connection connection;
	
	private boolean isDataArrived = false;
	
	
	@Override
	public void handleMessage(IoSession session, VideoFrameData message) throws Exception {
		Log.i(TAG, "decode VideoFrameDataMessageHandler");
		if (connection == null) {
			connection = (Connection) session.getAttribute(CONNECTION);
		}
		
		if (h264 == null) {
			h264 = connection.getH264decoder();
		}
		
		if (lvContainer == null || connection.isShowComponentChanged()) {
			lvContainer = connection.getLiveViewItemContainer();
		}
		
		if (liveViewChangedListener == null || connection.isShowComponentChanged()) {
			liveViewChangedListener = connection.getLiveViewChangedListener();
		}
		
		
		
		if (!connection.isValid()) {
			session.close(true);
		}
		
		if (!isDataArrived || connection.isJustAfterConnected()) {
			isDataArrived = true;
			connection.setIsJustAfterConnected(false);
			
			LiveViewItemContainer liveViewItemConatainer = connection.getLiveViewItemContainer();
			StatusListener connectionStatusListener = connection.getConnectionListener();
			if (liveViewItemConatainer != null && connectionStatusListener != null) {
				connectionStatusListener.OnConnectionBusy(liveViewItemConatainer);
			}
		}
		
		
		
		// 视频数据解码
		if (message instanceof VideoIFrameData) {
			//System.out.println("$$$VideoIFrameData is arrvied...");
			h264.setbFirst(true);
			h264.setbFindPPS(true);
			
			byte[] _sps = H264Decoder.extractSps(message.getData(), message.getData().length);
			byte[] sps = lvContainer.getVideoConfig().getSps();
			System.arraycopy(_sps, 4, sps, 0, _sps.length-4);
			lvContainer.getVideoConfig().setSpsLen(_sps.length-4);
			
			if (lvContainer.isInRecording()) {
				lvContainer.setCanStartRecord(true);
			}
		} else {
			//System.out.println("$$$VideoPFrameData is arrvied...");
		}
		
		long t1 = System.currentTimeMillis();
		
		h264.decodePacket(message.getData(), message.getData().length,
				((LiveView) liveViewChangedListener).retrievetDisplayBuffer());		
		
		Log.i(TAG, "decode consume: " + (System.currentTimeMillis()-t1));
		
		if (lvContainer.isInRecording() && lvContainer.canStartRecord()) {
			MP4Recorder.packVideo(lvContainer.getRecordFileHandler(), message.getData(), message.getData().length);
		}
		
		// 更新视频显示
		if (liveViewChangedListener != null && !connection.getLiveViewItemContainer().isManualStop()) {
			//System.out.println(liveViewChangedListener + "@before onDisplayContentUpdated" );
			liveViewChangedListener.onContentUpdated();
		}
	}

	
}
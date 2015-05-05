package com.starnet.snview.protocol.message.handler;


import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.demux.MessageHandler;

import android.util.Log;

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.h264.H264DecodeUtil.OnResolutionChangeListener;
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

	private AttributeKey CONNECTION = new AttributeKey(Connection.class, "connection");
	private AttributeKey ONE_FRAME_BUFFER = new AttributeKey(VideoFrameInfoExMessageHandler.class, "oneFrameBuffer");
	private AttributeKey ONE_FRAME_BUFFER_SIZE = new AttributeKey(VideoFrameInfoExMessageHandler.class, "oneFrameBufferSize");
	private AttributeKey DATA_EXCEED_64KB = new AttributeKey(VideoFrameInfoExMessageHandler.class, "dataExceed64Kb");
	
	
	private H264DecodeUtil h264;
	private OnLiveViewChangedListener liveViewChangedListener;
	private LiveViewItemContainer lvContainer;	
	private Connection connection;
	
	private boolean isDataArrived = false;
	
	
	@Override
	public void handleMessage(IoSession session, VideoFrameData message) throws Exception {
		Log.d(TAG, "decode VideoFrameDataMessageHandler");
		
		byte[] data = message.getData();
		int length = data.length;
		Boolean dataExceed64kb = (Boolean) session.getAttribute(DATA_EXCEED_64KB);
		if (dataExceed64kb != null && dataExceed64kb) {
			IoBuffer oneFrameBuffer = (IoBuffer) session.getAttribute(ONE_FRAME_BUFFER);
			Integer oneFrameDataSize = (Integer) session.getAttribute(ONE_FRAME_BUFFER_SIZE);
			
			oneFrameBuffer.put(message.getData());
			
			if (oneFrameBuffer.position() >= oneFrameDataSize) {
				data = oneFrameBuffer.flip().array();
				length = oneFrameDataSize;
			} else {
				return;
			}
		}
		
		if (connection == null) {
			connection = (Connection) session.getAttribute(CONNECTION);
		}
		
		if (connection.isDisposed()) {
			session.close(true);
		}
		
		if (h264 == null) {
			h264 = connection.getH264decoder();
			h264.setOnResolutionChangeListener(new OnResolutionChangeListener() {
				@Override
				public void onResolutionChanged(int oldWidth, int oldHeight, int newWidth,
						int newHeight) {
					if (lvContainer != null) {
						Log.d(TAG, "onResolutionChanged, oldWidth:" + oldWidth
								+ ", oldHeihgt:" + oldHeight + ", newWidth:"
								+ newWidth + ", newHeight:" + newHeight);
						int framerate = lvContainer.getVideoConfig().getFramerate();
						if (framerate <= 0 || framerate > 120){
							lvContainer.getVideoConfig().setFramerate(25);
						}
						lvContainer.getVideoConfig().setWidth(newWidth);
						lvContainer.getVideoConfig().setHeight(newHeight);
						lvContainer.setWindowInfoContent(newWidth + "x" + newHeight);
						lvContainer.getSurfaceView().init(newWidth, newHeight);
					}					
				}
			});
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
			
			byte[] _sps = H264Decoder.extractSps(data, length);
			byte[] sps = lvContainer.getVideoConfig().getSps();
			if (_sps != null && sps != null) {
				System.arraycopy(_sps, 4, sps, 0, _sps.length-4);
				lvContainer.getVideoConfig().setSpsLen(_sps.length-4);
			} else {
				Log.d(TAG, "_sps:" + _sps + ", sps:" + sps);
			}
			
			
			if (lvContainer.isInRecording()) {
				lvContainer.setCanStartRecord(true);
			}
		} else {
			//System.out.println("$$$VideoPFrameData is arrvied...");
		}
		
		long t1 = System.currentTimeMillis();
		
		int decodeResult = h264.decodePacket(data, length,
				((LiveView) liveViewChangedListener).retrievetDisplayBuffer());
		
		Log.i(TAG, "decode consume: " + (System.currentTimeMillis()-t1));
		
		if (lvContainer.isInRecording() && lvContainer.canStartRecord()) {
			MP4Recorder.packVideo(lvContainer.getRecordFileHandler(), data, length);
		}
		
		// 更新视频显示
		if (decodeResult == 1 && liveViewChangedListener != null
				&& !connection.getLiveViewItemContainer().isManualStop()) {
			liveViewChangedListener.onContentUpdated();
		}
	}

	
}
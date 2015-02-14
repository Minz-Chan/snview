package com.starnet.snview.component.video;

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.liveview.PlaybackLiveView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class VideoHandler extends Handler {
	private static final String TAG = "VideoPlayHandler";
	public static final int MSG_BUFFER_PROCESS = 0x11110001;
	public static final int MSG_VIDEOPLAYER_INIT = 0x11110002;
	
	private H264DecodeUtil h264decoder;
	private VideoBufferQueue bufferQueue;
	private PlaybackLiveView videoView;
	
	public VideoHandler(Looper looper, PlaybackLiveView playbackLiveView) {
		super(looper);
		this.videoView = playbackLiveView;
		h264decoder = new H264DecodeUtil(this.toString());
		bufferQueue = new VideoBufferQueue(this);
	}


	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
		case MSG_BUFFER_PROCESS:			
			processVideoData();
			break;
		case MSG_VIDEOPLAYER_INIT:
			h264decoder.init(msg.arg1, msg.arg2);
			videoView.init(msg.arg1, msg.arg2);
			break;
		}
	}
	
	private void processVideoData() {
		long t1 = System.currentTimeMillis();
		byte[] vData = bufferQueue.read();
		int result = 0;
		try {
			
			result = h264decoder.decodePacket(vData, vData.length,
					videoView.retrievetDisplayBuffer());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (result == 1) {
			Log.i(TAG, "Video data size:" + vData.length);
			Log.i(TAG, "*********************** update video: X Frame  *************************");
			videoView.onContentUpdated();					
		}
		Log.i(TAG, "$$$XFramedecode consume: " + (System.currentTimeMillis()-t1));
	}
	
	public VideoBufferQueue getBufferQueue() {
		return bufferQueue;
	}
	
}

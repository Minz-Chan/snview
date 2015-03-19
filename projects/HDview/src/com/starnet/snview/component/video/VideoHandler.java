package com.starnet.snview.component.video;

import com.starnet.snview.component.h264.H264DecodeUtil;
import com.starnet.snview.component.h264.MP4Recorder;
import com.starnet.snview.component.liveview.PlaybackLiveView;
import com.starnet.snview.component.liveview.PlaybackLiveViewItemContainer;
import com.starnet.snview.playback.PlaybackActivity;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class VideoHandler extends Handler {
	private static final String TAG = "VideoPlayHandler";
	public static final int MSG_BUFFER_PROCESS = 0x11110001;
	public static final int MSG_VIDEOPLAYER_INIT = 0x11110002;
	
	private Context context;
	
	private H264DecodeUtil h264decoder;
	private VideoBufferQueue bufferQueue;
	private PlaybackLiveView videoView;
	
	private boolean isAlive;
	
	public VideoHandler(Context context, Looper looper, PlaybackLiveView playbackLiveView) {
		super(looper);
		this.context = context;
		this.videoView = playbackLiveView;
		h264decoder = new H264DecodeUtil(this.toString());
		bufferQueue = new VideoBufferQueue(this);
		
		isAlive = true;
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
		
		if (vData == null) {
			Log.d(TAG, "processVideoData, data null");
			return;
		}
		
		int result = 0;
		try {
			Log.d(TAG, "$$$Process video data: " + vData.length);
			result = h264decoder.decodePacket(vData, vData.length,
					videoView.retrievetDisplayBuffer());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.d(TAG, "decode result: " + result);
		Log.d(TAG, "Video data size:" + vData.length);
		if (result == 1) {			
			Log.d(TAG, "*********************** update video: X Frame  *************************");
			videoView.onContentUpdated();					
		}
		Log.d(TAG, "$$$XFramedecode consume: " + (System.currentTimeMillis()-t1));
		
		if (getPlaybackContainer().isInRecording() && getPlaybackContainer().canStartRecord()) {
			Log.d(TAG, "MP4Recorder.packVideo, data size:" + vData.length);
			int r = MP4Recorder.packVideo(getPlaybackContainer().getRecordFileHandler(), vData, vData.length);
			Log.d(TAG, "MP4Recorder.packVideo result:" + r);
		} 
	}
	
	public void onResolutionChanged(int newWidth, int newHeight) {
		//h264decoder.init(newWidth, newHeight);
		videoView.init(newWidth, newHeight);
	}
	
	public VideoBufferQueue getBufferQueue() {
		return bufferQueue;
	}

	private PlaybackLiveViewItemContainer getPlaybackContainer() {
		return getPlaybackActivity().getVideoContainer();
	}

	private PlaybackActivity getPlaybackActivity() {
		return (PlaybackActivity) context;
	}

	public H264DecodeUtil getH264Decoder() {
		return h264decoder;
	}
	
	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}	
	
}

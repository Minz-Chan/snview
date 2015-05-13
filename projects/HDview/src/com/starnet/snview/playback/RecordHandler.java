package com.starnet.snview.playback;

import com.starnet.snview.component.h264.MP4Recorder;
import com.starnet.snview.component.liveview.PlaybackLiveViewItemContainer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class RecordHandler extends Handler {
	private static final String TAG = "RecordHandler";
	
	private Context context;
	
	public static final int MSG_VIDEO_RECORD = 0x11110003;
	public static final int MSG_AUDIO_RECORD = 0x11110004;
	
	
	public RecordHandler(Context context, Looper looper) {
		super(looper);
		this.context = context;
	}
	
	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
		case MSG_VIDEO_RECORD:
			Log.d(TAG, "MSG_VIDEO_RECORD");
			performVideoRecord(msg);
			break;
		case MSG_AUDIO_RECORD:
			Log.d(TAG, "MSG_AUDIO_RECORD");
			performAudioRecord(msg);
			break;
		}
	}
	
	private void performAudioRecord(Message msg) {
		Bundle data = msg.getData();
		if (data == null) {
			return;
		}

		byte[] alawData = data.getByteArray("AUDIO_DATA");
		int len = data.getInt("FRAME_LENGTH");
		int duration = data.getInt("DURATION");
		if (alawData != null) {
			if (getPlaybackContainer().isInRecording()
					&& getPlaybackContainer().canStartRecord()) {
				Log.d(TAG, "MP4Recorder.packAudio, data size:"
						+ alawData.length);
				MP4Recorder.packAudio(getPlaybackContainer()
						.getRecordFileHandler(), alawData, len, duration);
			}
		}
	}
	
	private void performVideoRecord(Message msg) {
		Bundle data = msg.getData();
		if (data == null) {
			return;
		}

		byte[] videoData = data.getByteArray("VIDEO_DATA");
		int len = data.getInt("FRAME_LENGTH");
		int duration = data.getInt("DURATION");
		if (videoData != null) {
			if (getPlaybackContainer().isInRecording()
					&& getPlaybackContainer().canStartRecord()) {
				Log.d(TAG, "MP4Recorder.packVideo, data size:"
						+ videoData.length);
				int r = MP4Recorder.packVideo(getPlaybackContainer()
						.getRecordFileHandler(), videoData, len, duration);
				Log.d(TAG, "MP4Recorder.packVideo result:" + r);
			}
		}
	}
	
	private PlaybackLiveViewItemContainer getPlaybackContainer() {
		return getPlaybackActivity().getVideoContainer();
	}

	private PlaybackActivity getPlaybackActivity() {
		return (PlaybackActivity) context;
	}
}

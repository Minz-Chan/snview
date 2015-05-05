package com.starnet.snview.component.audio;

import com.starnet.snview.component.h264.MP4Recorder;
import com.starnet.snview.component.liveview.PlaybackLiveViewItemContainer;
import com.starnet.snview.playback.PlaybackActivity;

import android.content.Context;
import android.media.AudioFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AudioHandler extends Handler {
	private static final String TAG = "AudioHandler";
	public static final int MSG_BUFFER_FULL = 0x12340001;
	public static final int MSG_AUDIOPLAYER_INIT = 0x12340002;
	
	private Context context;
	
	private AudioPlayer audioPlayer;
	private AudioBufferQueue bufferQueue;
	
	private byte[] alawData = new byte[AudioBufferQueue.BUFFER_SIZE];
	private byte[] pcmData = new byte[AudioBufferQueue.BUFFER_SIZE*2];
	
	public AudioHandler(Context context, Looper looper) {
		super(looper);			
		this.context = context;
		bufferQueue = new AudioBufferQueue(this);
	}

	@Override
	public void handleMessage(Message msg) {
		switch(msg.what) {
		case MSG_BUFFER_FULL:
			
			processAudioData();
			
			break;
		case MSG_AUDIOPLAYER_INIT:
			initAudioPlayer(msg.arg1); // take SampleRate as parameter
			break;
		}
	}
	
	private void processAudioData() {
		int readByte = bufferQueue.read(alawData);
		if (readByte == AudioBufferQueue.BUFFER_SIZE
				&& audioPlayer != null) {
			long t1 = System.currentTimeMillis();
			AudioCodec.g711aDecode(alawData, alawData.length, pcmData, pcmData.length);
			Log.i(TAG, "$$$audio g711decode consume:" + (System.currentTimeMillis()-t1));
			t1 = System.currentTimeMillis();
			audioPlayer.playAudioTrack(pcmData, 0, pcmData.length);
			Log.i(TAG, "$$$audio play consume:" + (System.currentTimeMillis()-t1));
		} else {
			Log.i(TAG, "Read " + readByte + " byte(s) from audio buffer queue");
		}
	}
	
	private void initAudioPlayer(int sampleRate) {
		audioPlayer = new AudioPlayer(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, 
				AudioFormat.ENCODING_PCM_16BIT);
		audioPlayer.init();
	}
	
	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
	
	public AudioBufferQueue getBufferQueue() {
		return bufferQueue;
	}
	
	private PlaybackLiveViewItemContainer getPlaybackContainer() {
		return getPlaybackActivity().getVideoContainer();
	}

	private PlaybackActivity getPlaybackActivity() {
		return (PlaybackActivity) context;
	}
	
	
}
package com.video.hdview.images.utils;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoPlayerUtils implements OnPreparedListener,OnCompletionListener {

	@SuppressWarnings("unused")
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private MediaPlayer mMediaPlayer;
	@SuppressWarnings("unused")
	private String videoPath;//视频路径
	private Activity mActivity;

	public VideoPlayerUtils(SurfaceView mSurfaceView) {
		this.mSurfaceView = mSurfaceView;
	}
	
	@SuppressWarnings("deprecation")
	public VideoPlayerUtils(MediaPlayer mMediaPlayer,SurfaceView mSurfaceView,String videoPath) {
		this.mSurfaceView = mSurfaceView;
		this.videoPath = videoPath;
		this.mMediaPlayer = mMediaPlayer;
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {// 用于监听准备事情
		mp.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {// 用于监听播放完毕事情,返回到原来的界面...
		if (mp != null) {
			mp.stop();
			mp.release();
			mp = null;
		}
		mActivity.finish();
	}

	public void playUrl(String videoPath)  
    {  
        try {  
        	mMediaPlayer.reset();  
        	mMediaPlayer.setDataSource(videoPath);  
        	mMediaPlayer.prepare();//prepare之后自动播放  
        } catch (IllegalArgumentException e) {
            e.printStackTrace();  
        } catch (IllegalStateException e) {
            e.printStackTrace();  
        } catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }  
	
	public void play() {//开始播放事件...
		mMediaPlayer.start();
	}

	public void stop() {// 停止播放...
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
	
	public void setActivity(Activity mActivity){
		this.mActivity = mActivity;
	}

}
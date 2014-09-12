package com.starnet.snview.global;

import com.starnet.snview.R;
import com.starnet.snview.images.ImageLoader;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.ActivityUtility;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

public class GlobalApplication extends Application {
	private static GlobalApplication singleton = new GlobalApplication();
	
	private String appName;
	
	private RealplayActivity mRealplayActivity;
	
	private int mScreenWidth;
	private int mScreenHeight;
	
	private int mLandscapeControlWidth;
	private int mLandscapeControlHeight;
	
	private int mPTZPopFrameWidth;
	private int mPTZPopFrameHeight;
	
	private boolean mIsFullscreenMode;
	
	private Handler handler;

	public static GlobalApplication getInstance() {
		return singleton;
	}
	
	public void init(Activity activity) {
		mRealplayActivity = (RealplayActivity) activity;
		
		mScreenWidth = ActivityUtility.getScreenSize(activity).x;
		mScreenHeight = ActivityUtility.getScreenSize(activity).y;
		
		mLandscapeControlWidth = activity.getResources().getDrawable(R.drawable.fullscreen_bar).getIntrinsicWidth();
		mLandscapeControlHeight = activity.getResources().getDrawable(R.drawable.fullscreen_bar).getIntrinsicHeight();
		
		mPTZPopFrameWidth = activity.getResources().getDrawable(R.drawable.ptz_pop_frame_bg).getIntrinsicWidth();
		mPTZPopFrameHeight = activity.getResources().getDrawable(R.drawable.ptz_pop_frame_bg).getIntrinsicHeight();
		
		mIsFullscreenMode = false;
		
		ImageLoader.getInstance().setImageMaxSize(mScreenWidth / 3);
	}

	public int getScreenWidth() {
		return mScreenWidth;
	}

	public void setScreenWidth(int width) {
		this.mScreenWidth = width;
	}
	
	

	public int getLandscapeControlWidth() {
		return mLandscapeControlWidth;
	}

	public void setLandscapeControlWidth(int landscapeControlWidth) {
		this.mLandscapeControlWidth = landscapeControlWidth;
	}

	public int getLandscapeControlHeight() {
		return mLandscapeControlHeight;
	}

	public void setLandscapeControlHeight(int landscapeControlHeight) {
		this.mLandscapeControlHeight = landscapeControlHeight;
	}
	
	public int getVideoRegionWidth() {
		return mScreenWidth;
	}
	
	public int getVideoRegionHeight() {
		if (mIsFullscreenMode) {
			return mScreenHeight;
		} else {
			return mScreenWidth;
		}		
	}

	public int getPTZPopFrameWidth() {
		return mPTZPopFrameWidth;
	}

	public void setPTZPopFrameWidth(int width) {
		this.mPTZPopFrameWidth = width;
	}

	public int getPTZPopFrameHeight() {
		return mPTZPopFrameHeight;
	}

	public void setPTZPopFrameHeight(int height) {
		this.mPTZPopFrameHeight = height;
	}

	public boolean isIsFullMode() {
		return mIsFullscreenMode;
	}

	public void setFullscreenMode(boolean isFullscreenMode) {
		this.mIsFullscreenMode = isFullscreenMode;
	}

	public int getScreenHeight() {
		return mScreenHeight;
	}

	public void setScreenHeight(int height) {
		this.mScreenHeight = height;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	public void setRealplayActivity(RealplayActivity r) {
		this.mRealplayActivity = r;
	}
	
	public RealplayActivity getRealplayActivity() {
		return mRealplayActivity;
	}

}

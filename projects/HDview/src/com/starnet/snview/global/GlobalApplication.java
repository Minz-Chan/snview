package com.starnet.snview.global;

import com.starnet.snview.R;
import com.starnet.snview.util.ActivityUtility;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

public class GlobalApplication extends Application {
	private static GlobalApplication singleton = new GlobalApplication();
	
	private String appName;
	
	private int mScreenWidth;
	private int mScreenHeight;
	
	private int mLandscapeControlWidth;
	private int mLandscapeControlHeight;
	
	private boolean mIsFullscreenMode;
	
	private Handler handler;

	public static GlobalApplication getInstance() {
		return singleton;
	}
	
	public void init(Activity activity) {
		mScreenWidth = ActivityUtility.getScreenSize(activity).x;
		mScreenHeight = ActivityUtility.getScreenSize(activity).y;
		
		mLandscapeControlWidth = activity.getResources().getDrawable(R.drawable.fullscreen_bar).getIntrinsicWidth();
		mLandscapeControlHeight = activity.getResources().getDrawable(R.drawable.fullscreen_bar).getIntrinsicHeight();
		
		mIsFullscreenMode = false;
		
		
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
	
	

}

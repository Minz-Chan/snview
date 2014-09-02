package com.starnet.snview.global;

import android.app.Application;
import android.os.Handler;

public class GlobalApplication extends Application {
	private static GlobalApplication singleton = new GlobalApplication();
	
	private String appName;
	
	private int mScreenWidth;
	private int mScreenHeight;
	
	private boolean mIsFullscreenMode;
	
	private Handler handler;

	public static GlobalApplication getInstance() {
		return singleton;
	}

	public int getScreenWidth() {
		return mScreenWidth;
	}

	public void setScreenWidth(int width) {
		this.mScreenWidth = width;
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

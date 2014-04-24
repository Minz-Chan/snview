package com.starnet.hdview.global;

import android.app.Application;

public class GlobalApplication extends Application {
	private static GlobalApplication singleton = new GlobalApplication();
	
	private String appName;
	
	private int mScreenWidth;

	public static GlobalApplication getInstance() {
		return singleton;
	}

	public int getScreenWidth() {
		return mScreenWidth;
	}

	public void setScreenWidth(int mScreenWidth) {
		this.mScreenWidth = mScreenWidth;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	

}

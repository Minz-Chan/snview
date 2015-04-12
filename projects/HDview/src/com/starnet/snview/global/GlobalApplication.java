package com.starnet.snview.global;

import java.util.List;

import com.baidu.frontia.FrontiaApplication;
import com.starnet.snview.R;
import com.starnet.snview.images.ImageLoader;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.ActivityUtility;

import android.app.Activity;
import android.os.Handler;

//public class GlobalApplication extends Application {
public class GlobalApplication extends FrontiaApplication {
	private static GlobalApplication singleton = new GlobalApplication();
	
	private String appName;
	
	private Activity mCurrentActivity;
	private RealplayActivity mRealplayActivity;
	
	private int mScreenWidth;
	private int mScreenHeight;
	
	private int mRealplayContainerWidth;
	private int mRealplayContainerHeight;
	
	private int mLandscapeControlWidth;
	private int mLandscapeControlHeight;
	
	private int mPTZPopFrameWidth;
	private int mPTZPopFrameHeight;
	
	private boolean mIsFullscreenMode;
	
	private Handler mHandler;
	private Handler mPlaybackHandler;
	
	private List<PreviewDeviceItem> lastPreviewItems;

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
	
	public int getRealplayContainerWidth() {
		return mRealplayContainerWidth;
	}

	public void setRealplayContainerWidth(int realplayContainerWidth) {
		this.mRealplayContainerWidth = realplayContainerWidth;
	}

	public int getRealplayContainerHeight() {
		return mRealplayContainerHeight;
	}

	public void setRealplayContainerHeight(int realplayContainerHeight) {
		this.mRealplayContainerHeight = realplayContainerHeight;
	}

	public Handler getHandler() {
		return mHandler;
	}

	public void setHandler(Handler handler) {
		this.mHandler = handler;
	}
	
	public Handler getPlaybackHandler() {
		return mPlaybackHandler;
	}

	public void setPlaybackHandler(Handler playbackHandler) {
		this.mPlaybackHandler = playbackHandler;
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

	public Activity getCurrentActivity() {
		return mCurrentActivity;
	}

	public void setCurrentActivity(Activity currentActivity) {
		this.mCurrentActivity = currentActivity;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public List<PreviewDeviceItem> getLastPreviewItems() {
		return lastPreviewItems;
	}

	public void setLastPreviewItems(List<PreviewDeviceItem> lastPreviewItems) {
		this.lastPreviewItems = lastPreviewItems;
	}
	
	public void exit(){
		System.exit(0);
	}
}

package com.starnet.snview.component.liveview;

import com.starnet.snview.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class LiveViewItemContainer extends RelativeLayout {
	
	
	private WindowLinearLayout mWindowLayout;
	private FrameLayout mPlaywindowFrame;
	private LiveView mSurfaceView;
	private ProgressBar mProgressBar;
	private ImageView mRefresh;
	private TextView mWindowInfoText;
	
	
	
	private OnLiveViewContainerClickListener mLvContainerClickListener;
	private OnRefreshButtonClickListener mRefreshButtonClickListener;
	
	
	public LiveViewItemContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public LiveViewItemContainer(Context context) {
		super(context);
	}
	
	

	public void findSubViews() {
		mWindowLayout = (WindowLinearLayout) findViewById(R.id.liveview_surface_infotext_layout);
		mPlaywindowFrame = (FrameLayout) findViewById(R.id.liveview_playwindow_frame);
		mSurfaceView = (LiveView) findViewById(R.id.liveview_surfaceview);
		mProgressBar = (ProgressBar) findViewById(R.id.liveview_progressbar);
		mRefresh = (ImageView) findViewById(R.id.liveview_refresh_imageview);
		mWindowInfoText = (TextView) findViewById(R.id.liveview_liveinfo_textview);
	}
	
	public void init() {
		if (mLvContainerClickListener != null) {
			this.setOnClickListener(mLvContainerClickListener);
		}
		
		if (mRefreshButtonClickListener != null) {
			mRefresh.setOnClickListener(mRefreshButtonClickListener);
		}
		
		mWindowInfoText.setText("test...");
		
	}
	
	
	
	public void setLiveViewContainerClickListener(
			OnLiveViewContainerClickListener lvContainerClickListener) {
		this.mLvContainerClickListener = lvContainerClickListener;
	}
	
	
	public void setRefreshButtonClickListener(
			OnRefreshButtonClickListener RefreshButtonClickListener) {
		this.mRefreshButtonClickListener = RefreshButtonClickListener;
	}
	public WindowLinearLayout getWindowLayout() {
		return mWindowLayout;
	}
	
	public FrameLayout getPlaywindowFrame() {
		return mPlaywindowFrame;
	}
	
	public LiveView getSurfaceView() {
		return mSurfaceView;
	}
	
	public ProgressBar getProgressBar() {
		return mProgressBar;
	}
	
	public ImageView getRefreshImageView() {
		return mRefresh;
	}
	
	public TextView getWindowInfoText() {
		return mWindowInfoText;
	}
	
	
	public static interface OnLiveViewContainerClickListener extends View.OnClickListener {}
	public static interface OnRefreshButtonClickListener extends View.OnClickListener {}
}

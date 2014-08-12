package com.starnet.snview.component.liveview;

import com.starnet.snview.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LiveViewItemContainer extends RelativeLayout {
	
	
	private WindowLinearLayout mWindowLayout;
	private ViewGroup mPlaywindowFrame;
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
		mSurfaceView = (LiveView) findViewById(R.id.liveview_surfaceview);
		mProgressBar = (ProgressBar) findViewById(R.id.liveview_progressbar);
		mRefresh = (ImageView) findViewById(R.id.liveview_refresh_imageview);
		mWindowInfoText = (TextView) findViewById(R.id.liveview_liveinfo_textview);
	}
	
	public void initListener() {
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
	
	public ViewGroup getPlaywindowFrame() {
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

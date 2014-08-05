package com.starnet.snview.component.liveview;

import com.starnet.snview.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LiveViewItemContainer extends RelativeLayout {
	
	
	private WindowLinearLayout mWindowLayout;
	private ViewGroup mPlaywindowFrame;
	private LiveView mSurfaceView;
	private TextView mWindowInfoText;
	
	
	
	private OnLiveViewContainerClickListener lvContainerClickListener;
	
	
	public LiveViewItemContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public LiveViewItemContainer(Context context) {
		super(context);
	}
	
	

	public void findSubViews() {
		mWindowLayout = (WindowLinearLayout) findViewById(R.id.liveview_surface_infotext_layout);
		mSurfaceView = (LiveView) findViewById(R.id.liveview_surfaceview);
		mWindowInfoText = (TextView) findViewById(R.id.liveview_liveinfo_textview);
	}
	
	public void initListener() {
		if (lvContainerClickListener != null) {
			this.setOnClickListener(lvContainerClickListener);
		}
		
		mWindowInfoText.setText("test...");
		
	}
	
	
	
	public void setLiveViewContainerClickListener(
			OnLiveViewContainerClickListener lvContainerClickListener) {
		this.lvContainerClickListener = lvContainerClickListener;
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
	public TextView getWindowInfoText() {
		return mWindowInfoText;
	}
	
	
	public static interface OnLiveViewContainerClickListener extends View.OnClickListener{
		
	}
}

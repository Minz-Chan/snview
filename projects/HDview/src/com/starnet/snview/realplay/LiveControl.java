package com.starnet.snview.realplay;

import com.starnet.snview.R;
import com.starnet.snview.component.LandscapeToolbar;
import com.starnet.snview.global.GlobalApplication;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class LiveControl {

	private RealplayActivity mLiveActivity;
	
	private RelativeLayout mLandscapePopFrame;
	private LandscapeToolbar mLandscapeControlbar;
	
	
	public LiveControl(RealplayActivity activity) {
		this.mLiveActivity = activity;
		
		findViews();
	}
	
	private void findViews() {
		initLandscapePopFrame();
		
	}
	
	private void initLandscapePopFrame() {
		mLandscapePopFrame = ((RelativeLayout)mLiveActivity.findViewById(R.id.landscape_liveview_pop_frame));
	    mLandscapeControlbar = ((LandscapeToolbar)mLiveActivity.findViewById(R.id.landscape_liveview_control_frame));
	    mLandscapeControlbar.findViews();
	}
	
	private void initLandscapeControlbarPosition() {
		int sWidth = GlobalApplication.getInstance().getScreenWidth();
		int sHeight = GlobalApplication.getInstance().getScreenHeight();
		int lWidth = GlobalApplication.getInstance().getLandscapeControlWidth();
		int lHeight = GlobalApplication.getInstance()
				.getLandscapeControlHeight();

		Log.i("LiveManager", "screenHeight1: " + sHeight
				+ " controlbarHeight: " + lHeight + "  screenWidth: " + sWidth
				+ " controlbarWidth: " + lWidth);

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mLandscapeControlbar
				.getLayoutParams();
		lp.leftMargin = (sWidth - lWidth) / 2;
		lp.topMargin = (sHeight - lHeight) - 20;
		mLandscapeControlbar.setLayoutParams(lp);
	}
	
	public void showLandscapeToolbarFrame() {
		mLandscapePopFrame.setVisibility(View.VISIBLE);
		initLandscapeControlbarPosition();
	}
	
	public void hideLandscapeToolbarFrame() {
		mLandscapePopFrame.setVisibility(View.GONE);
	}
	
}

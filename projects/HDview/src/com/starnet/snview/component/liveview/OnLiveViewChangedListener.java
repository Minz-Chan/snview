package com.starnet.snview.component.liveview;

import android.view.SurfaceHolder;

public interface OnLiveViewChangedListener extends SurfaceHolder.Callback {
	
	public void onDisplayResulotionChanged(int width, int height);
	
	public void onDisplayContentUpdated();
	
	public void onDisplayContentReset();
}

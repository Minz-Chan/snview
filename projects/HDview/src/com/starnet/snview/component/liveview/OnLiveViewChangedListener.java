package com.starnet.snview.component.liveview;

import android.view.SurfaceHolder;

public interface OnLiveViewChangedListener extends SurfaceHolder.Callback {
	public void onResulotionChanged(int width, int height);
	public void onContentUpdated();
	public void onContentReset();
}

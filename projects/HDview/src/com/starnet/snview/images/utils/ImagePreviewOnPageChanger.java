package com.starnet.snview.images.utils;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.TextView;

public class ImagePreviewOnPageChanger implements OnPageChangeListener {
	
	private final String TAG = "ImagePreviewOnPageChanger";
	
	private TextView imagepreview_title_image_num;// 显示设备的数量，以及显示画面的序号...
	
	public ImagePreviewOnPageChanger(TextView imagepreview_title_image_num) {
		this.imagepreview_title_image_num = imagepreview_title_image_num;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		Log.v(TAG, "arg0:"+arg0);

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		Log.v(TAG, "arg0:"+arg0);
		Log.v(TAG, "arg1:"+arg1);
		Log.v(TAG, "arg2:"+arg2);

	}

	@Override
	public void onPageSelected(int arg0) {
		Log.v(TAG, "arg0:"+arg0);
	}
}
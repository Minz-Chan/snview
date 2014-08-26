package com.starnet.snview.images.myutils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MyImageview extends ImageView {

	public MyImageview(Context context) {
		super(context);
	}

	public MyImageview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private MyImageViewAttacher mAttacher;

	private ScaleType mPendingScaleType;

}

package com.video.hdview.component;

import com.video.hdview.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

public class ToastTextView extends TextView {

	public ToastTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ToastTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ToastTextView(Context context) {
		super(context);
		init();
	}

	private void init() {
		this.setTextColor(Color.WHITE);
		this.setGravity(Gravity.CENTER);
		this.setBackgroundColor(getContext().getResources().getColor(R.color.realplay_toast_bg));
		this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}
	
}

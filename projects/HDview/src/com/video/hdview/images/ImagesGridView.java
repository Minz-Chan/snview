package com.video.hdview.images;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

public class ImagesGridView extends GridView {

	public ImagesGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(
				536870911, MeasureSpec.AT_MOST));
	}
}

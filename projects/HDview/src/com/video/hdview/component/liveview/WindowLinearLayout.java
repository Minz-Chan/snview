package com.video.hdview.component.liveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

public class WindowLinearLayout extends LinearLayout {
	public static final String TAG = "WindowLinearLayout";
	private static final boolean debug = true;
	
	private static final int BORDER_COLOR_SELECTED = -13312;		 // 淡黄
	private static final int BORDER_COLOR_UNSELECTED = Color.BLACK;  // 黑色
	
	private boolean mIsSelected = false;
	private int mLayoutHeight;
	private int mLayoutWidth;
	
	private Paint mPaint;
	private RectF mBorder;

	public WindowLinearLayout(Context context) {
		super(context);
		init();
	}
	
	public WindowLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		mPaint = new Paint();
		mBorder =  new RectF();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setAntiAlias(true);
	}

	public boolean isWindowSelected() {
		return mIsSelected;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mLayoutWidth = getMeasuredWidth();
		mLayoutHeight = getMeasuredHeight();
		
		if (debug) {
			Log.d(TAG, "onDraw(), measuredWidth:" + mLayoutWidth
					+ ", measuredHeight:" + mLayoutHeight 
					+ ", selected:" + mIsSelected);
		}
		
		if ((mLayoutWidth > 0) && (mLayoutHeight > 0)) {
			mPaint.setColor(mIsSelected ? 
					BORDER_COLOR_SELECTED : BORDER_COLOR_UNSELECTED);
			
			// 1 pixel border
			mBorder.set(0.0F, 0.0F, mLayoutWidth, mLayoutHeight);
			canvas.drawRoundRect(mBorder, 0.0F, 0.0F, mPaint);
			// another 1 pixel border
			mBorder.set(1.0F, 1.0F, mLayoutWidth - 1, mLayoutHeight - 1);
			canvas.drawRoundRect(mBorder, 0.0F, 0.0F, mPaint);
		}
	}

	public void setWindowSelected(boolean selected) {
		this.mIsSelected = selected;
		//invalidate();
		postInvalidate();
	}
}
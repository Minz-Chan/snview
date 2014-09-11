package com.starnet.snview.component.liveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class WindowLinearLayout extends LinearLayout {
	public static final String TAG = "WindowLinearLayout";
	
	private boolean mIsSelected = false;
	private int mLayoutHeight;
	private int mLayoutWidth;
	
	private Paint paint = new Paint();
	private RectF border =  new RectF();

	public WindowLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WindowLinearLayout(Context context) {
		super(context);

	}

	public boolean isWindowSelected() {
		return mIsSelected;
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		mLayoutWidth = getMeasuredWidth();
		mLayoutHeight = getMeasuredHeight();
		
		if ((mLayoutWidth > 0) && (mLayoutHeight > 0)) {
			paint.setStyle(Paint.Style.STROKE);

			if (!mIsSelected) {
				paint.setColor(Color.BLACK);// 黑色
			} else {
				paint.setColor(-13312); // 暗绿色
			}
			
			paint.setAntiAlias(true);
			
			border.set(0.0F, 0.0F, mLayoutWidth, mLayoutHeight);
			canvas.drawRoundRect(border, 0.0F, 0.0F, paint);
			
			border.set(1.0F, 1.0F, mLayoutWidth - 1, mLayoutHeight - 1);
			canvas.drawRoundRect(border, 0.0F, 0.0F, paint);
		}

	}

	public void setWindowSelected(boolean paramBoolean) {
		this.mIsSelected = paramBoolean;
		invalidate();
	}
}
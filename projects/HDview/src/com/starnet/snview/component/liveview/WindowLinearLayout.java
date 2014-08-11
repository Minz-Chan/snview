package com.starnet.snview.component.liveview;

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
	
	private boolean mIsSelected = false;
	private int mLayoutHeight;
	private int mLayoutWidth;

	public WindowLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public WindowLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public boolean isWindowSelected() {
		return this.mIsSelected;
	}

	protected void onDraw(Canvas canvas) {
		long t1 = System.currentTimeMillis();
		
		super.onDraw(canvas);
		this.mLayoutWidth = getMeasuredWidth();
		this.mLayoutHeight = getMeasuredHeight();
		Paint paint = new Paint();

		if ((this.mLayoutWidth > 0) && (this.mLayoutHeight > 0)) {
			paint.setStyle(Paint.Style.STROKE);

			if (!this.mIsSelected) {
				//paint.setColor(-3355444); // 搂黑色
				paint.setColor(Color.BLACK);
				
			} else {
				paint.setColor(-13312); // 暗绿色
			}

			
			
			paint.setAntiAlias(true);
			canvas.drawRoundRect(new RectF(0.0F, 0.0F, this.mLayoutWidth,
					this.mLayoutHeight), 0.0F, 0.0F, paint);
			canvas.drawRoundRect(new RectF(1.0F, 1.0F, -1
					+ this.mLayoutWidth, -1 + this.mLayoutHeight), 0.0F,
					0.0F, paint);
		}

		long t2 = System.currentTimeMillis();
		
		Log.i(TAG, "t1: " + t1 + ", t2: " + t2);
		Log.i(TAG, "Time consumed during OnDraw: " + (t2 - t1) + "ms");
		//
		//
		//
		//
		// if ((this.mLayoutWidth > 0) && (this.mLayoutHeight > 0))
		// {
		// paint.setStyle(Paint.Style.STROKE);
		// if (!this.mIsSelected) {
		// break label128;
		// }
		// paint.setColor(-13312);
		// }
		// for (;;)
		// {
		// paint.setAntiAlias(true);
		// canvas.drawRoundRect(new RectF(0.0F, 0.0F, this.mLayoutWidth,
		// this.mLayoutHeight), 0.0F, 0.0F, paint);
		// canvas.drawRoundRect(new RectF(1.0F, 1.0F, -1 + this.mLayoutWidth, -1
		// + this.mLayoutHeight), 0.0F, 0.0F, paint);
		// return;
		// label128:
		// paint.setColor(-3355444);
		// }
	}

	public void setWindowSelected(boolean paramBoolean) {
		this.mIsSelected = paramBoolean;
		invalidate();
	}
}

/*
 * Location:
 * D:\kuaipan\我的资料\研究生阶段\项目\星网安防\star-security\iVMS-4500\classes_dex2jar.jar
 * Qualified Name: com.mcu.iVMS.component.WindowLinearLayout JD-Core Version:
 * 0.7.0-SNAPSHOT-20130630
 */
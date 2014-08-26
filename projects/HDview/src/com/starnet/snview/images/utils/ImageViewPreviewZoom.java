package com.starnet.snview.images.utils;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * 实现图片缩放的类...
 * 
 * @author zhongxu
 * 
 */
public class ImageViewPreviewZoom implements OnTouchListener {

	private final float MINSCALE = 0.5f;//最小缩放比例...
	private final float MAXSCALE = 2.5f;//最大缩放比例...
	
	private Matrix imageMatrix;// 图片矩阵...
	

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:// 第一根手指也按下去的时候...

			break;
		case MotionEvent.ACTION_POINTER_DOWN:// 第二根手指也按下去的时候...

			break;
		case MotionEvent.ACTION_UP:// 第一根手指拿开的时候...

			break;
		case MotionEvent.ACTION_POINTER_UP:// 第二根手指拿开的时候...
			
			break;
		}
		return true;
	}
}
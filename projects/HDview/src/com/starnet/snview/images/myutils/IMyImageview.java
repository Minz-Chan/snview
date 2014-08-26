package com.starnet.snview.images.myutils;

import android.graphics.RectF;
import android.widget.ImageView.ScaleType;

public interface IMyImageview {//主要是针对imageView的放大缩小操作
		
	void setMinScale(float minScale);//设置最小的缩放比例
	
	void setMaxScale(float maxScale);//设置最大的缩放比例
	
	float getMinScale();//最小的缩放比例
	
	float getMaxScale();//最大缩放比例
	
	RectF getDisplayRect();//获取展示区域...

	void setScaleType(ScaleType scaleType);//设置图片的缩放类型
	
	ScaleType getScaleType();//获取图片的缩放类型
	
	void setZoomable(boolean zoomable);//设置图片是否可缩放...

    void zoomTo(float scale, float focalX, float focalY);//图片缩放的最终情形...
    
    boolean canZoom();//获取图片是否可以缩放...
}
package com.video.hdview.playback;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.Calendar;

/**
 * 表示视频所占时间范围的矩形区域
 * @author minz
 *
 */
class FileRect
{
  

  private int mType;

  private float mX;				// 矩形区域左上角顶点X坐标
  private float mY;				// 矩形区域左上角顶点Y坐标
  
  private float mWidth;			// 矩形区域宽度
  private float mHeight;		// 矩形区域高度
  
  private Calendar mStartTime;
  private Calendar mStopTime;
  
  FileRect(float x, float y, int width, int height, int type, Calendar startTime, Calendar stopTime)
  {
    this.mX = x;
    this.mY = y;
    this.mWidth = width;
    this.mHeight = height;
    this.mType = type;
    this.mStartTime = startTime;
    this.mStopTime = stopTime;
  }
  
  public void draw(Canvas canvas, Paint paint, int minWidth, int maxWidth)
  {
	  if (isInRange(minWidth, maxWidth)) {
			if ((this.mX >= minWidth) || (this.mX + this.mWidth <= minWidth)) {
				if ((this.mX < maxWidth)
						&& (this.mX + this.mWidth > maxWidth)) {
					canvas.drawRect(this.mX, this.mY, maxWidth + 1,
							this.mY + this.mHeight, paint);
				} else {
					canvas.drawRect(this.mX, this.mY,
							1.0F + (this.mX + this.mWidth), this.mY
									+ this.mHeight, paint);
				}
			} else {
				canvas.drawRect(minWidth, this.mY,
						1.0F + (this.mX + this.mWidth), this.mY + this.mHeight,
						paint);
			}
		}
  }
  
  public long getStartTimeInMillis()
  {
    return this.mStartTime.getTimeInMillis();
  }
  
  public long getStopTimeInMillis()
  {
    return this.mStopTime.getTimeInMillis();
  }
  
	public boolean isInRange(float x1, float x2) {
		if ((this.mX + this.mWidth < x1) || (this.mX > x2)) {
			return false;
		} else {
			return true;
		}
	}
  
  public void setPos(float x, float y, int width, int height)
  {
    this.mX = x;
    this.mY = y;
    this.mWidth = width;
    this.mHeight = height;
  }
}
package com.starnet.hdview.playback;

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
//    if (isInRange(paramInt1, paramInt2))
//    {
//      if ((this.mX >= paramInt1) || (this.mX + this.mWidth <= paramInt1)) {
//        break label69;
//      }
//      paramCanvas.drawRect(paramInt1, this.mY, 1.0F + (this.mX + this.mWidth), this.mY + this.mHeight, paramPaint);
//    }
//    for (;;)
//    {
//      return;
//      label69:
//      if ((this.mX < paramInt2) && (this.mX + this.mWidth > paramInt2)) {
//        paramCanvas.drawRect(this.mX, this.mY, paramInt2 + 1, this.mY + this.mHeight, paramPaint);
//      } else {
//        paramCanvas.drawRect(this.mX, this.mY, 1.0F + (this.mX + this.mWidth), this.mY + this.mHeight, paramPaint);
//      }
//    }
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
  
  public boolean isInRange(float x1, float x2)
  {
//    if ((this.mX + this.mWidth < paramFloat1) || (this.mX > paramFloat2)) {}
//    for (boolean bool = false;; bool = true) {
//      return bool;
//    }
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


/* Location:           D:\kuaipan\鎴戠殑璧勬枡\鐮旂┒鐢熼樁娈礬椤圭洰\鏄熺綉瀹夐槻\star-security\iVMS-4500\classes_dex2jar.jar
 * Qualified Name:     com.mcu.iVMS.component.FileRect
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */
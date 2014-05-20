package com.starnet.snview.playback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TimeBar
  extends View
{
  private static final String TAG = "TimeBar";

  private Calendar mPickedTime = new GregorianCalendar();
  private TimePickedCallBack mTimePickCallback;
  private boolean mIsFrozen = false;
  private float mLastTouchX = 0.0F;
  private boolean mTouchDownFlag = false;
  private boolean mTouchMoved = false;
  private float mMoveSensitive = 0.2F;
  
  
  private int mWidth = 0;						// 控件宽度
  private int mHeight = 0;						// 控件 高度
  private float mMilliSecondsPerPixel = 0.0F;	// 每个像素代表的毫秒数

  private float mCellWidth = 0.0F;				// 刻度单元格宽度，在iVMS中为宽度的1/4
  private static final long mCellMilliSeconds = 3600000L;
  private static final int mShowCellNumber = 5;
  private static final int mTotoalCellNumber = 24;
  
  
  /**
   * 中线/中线时间文本 （middleLine/middleTime）相关
   */
  private GregorianCalendar mMiddleLineTime = new GregorianCalendar();
  private static final SimpleDateFormat mTimeFormat 
  					= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	// 中线时间格式
  private static final int mMiddleTimeColor = Color.RED;				// 中线时间文字颜色
  private static final Typeface mMiddleTimefont 
  					= Typeface.create(Typeface.SANS_SERIF, 1);	// 中线时间文字字体
  private static int mMiddleTimeFontSize = 18;					// 中线时间文字字体大小
  private static final float mMiddleTimeFontSizeInSp = 12.0F;	// 中线时间字体大小（单位sp）
  private float mMiddleLineX = 0.0F;			// 中线X轴坐标
  private float mMiddleLineTimeY = 15.0F;		// 中线Y轴坐标（单位px）
  private float mMiddleLineTimeYInSp = 10.0F;	// 中线Y轴坐标（单位sp）
  
  
  /**
   * 刻度(Scale)相关
   */
  private List<ScaleInfo> mScaleInfoList = new LinkedList();	// 刻度信息列表
  private static final int mScaleColor = Color.BLACK;				// 刻度线颜色
  private static int mScaleTimeFontSize = 12;					// 刻度时间字体大小（单位px）
  private static final float mScaleTimeFontSizeInSp = 9.0F;		// 刻度时间字体大小（单位sp）
  
  private float mScaleLineY = 45.0F;			// 刻度线Y轴坐标
  private float mScaleTimeY = 57.0F;			// 时间Y轴坐标
  private float mScaleLineHeight = 6.0F;		// 刻度线高度
  
  
  /**
   * 矩形条（FileRect）相关
   */
  private List<FileRect> mFileRectList = new LinkedList();
  private static final int mFileInfoColor = -10256404;			// 视频文件信息矩形条颜色
  private int mFileRectHeight = 12;
  
  
  
  
  public TimeBar(Context context)
  {
    super(context);
    initView();
  }
  
  public TimeBar(Context context, AttributeSet attributes)
  {
    super(context, attributes);
    initView();
  }
  
  private void OnActionDown(MotionEvent paramMotionEvent)
  {
    this.mLastTouchX = paramMotionEvent.getX();
    this.mTouchDownFlag = true;
  }
  
  private void OnActionMove(MotionEvent paramMotionEvent)
  {
    float moveOffsetOnX = paramMotionEvent.getX() - this.mLastTouchX;
    
    if (Math.abs(moveOffsetOnX) >= this.mMoveSensitive) {
    	this.mLastTouchX = paramMotionEvent.getX();
        if (!this.mIsFrozen)
        {
          this.mTouchMoved = true;
          UpdateDataPos(moveOffsetOnX);
          invalidate();
        }
    }
    
//    if (Math.abs(f) < this.mMoveSensitive) {}
//    for (;;)
//    {
//      return;
//      this.mLastTouchX = paramMotionEvent.getX();
//      if (!this.mIsFrozen)
//      {
//        this.mTouchMoved = true;
//        UpdateDataPos(f);
//        invalidate();
//      }
//    }
  }
  
  private void OnActionUp(MotionEvent paramMotionEvent)
  {
    if ((this.mTouchMoved) && (this.mTimePickCallback != null))
    {
      this.mPickedTime.setTime(this.mMiddleLineTime.getTime());
      this.mTimePickCallback.onTimePickedCallback(this.mPickedTime);
    }
    this.mTouchMoved = false;
    this.mTouchDownFlag = false;
  }
  
  private void UpdateDataPos(float xOffset)
  {
    updateMiddleLineTime(xOffset);
    updateScalePos();
    updateFileListPos();
  }
  
  private FileRect createFileRectInfo(int type, Calendar startTime, Calendar stopTime)
  {
    long l1 = startTime.getTimeInMillis();
    long l2 = stopTime.getTimeInMillis();
    long l3 = this.mMiddleLineTime.getTimeInMillis();
    float x = this.mMiddleLineX + (float)((l1 - l3) / (1.0D * mCellMilliSeconds) * this.mCellWidth);
    int frWidth = (int)((l2 - l1) / (1.0D * mCellMilliSeconds) * this.mCellWidth);
    return new FileRect(x, this.mHeight - this.mFileRectHeight, frWidth, this.mFileRectHeight, type, startTime, stopTime);
  }
  
  private void initVectorScale()
  {
//    for (int i = 0;; i++)
//    {
//      if (i >= 24) {
//        return;
//      }
//      ScaleInfo localScaleInfo = new ScaleInfo(-1, (int)(3600L * i));
//      localScaleInfo.setPosRange(0.0F, 24.0F * this.mCellWidth);
//      this.mScaleInfoList.add(localScaleInfo);
//    }
    
    for (int i = 0; i < mTotoalCellNumber; i++) {
    	ScaleInfo scaleinfo = new ScaleInfo(-1, (int)(3600L * i));
        scaleinfo.setPosRange(0.0F, 1.0F * mTotoalCellNumber * this.mCellWidth);
        this.mScaleInfoList.add(scaleinfo);
    }
  }
  
  private void initView()
  {
    initVectorScale();
    mMiddleTimeFontSize = sp2px(getContext(), mMiddleTimeFontSizeInSp);
    Log.i("TimeBar", "mMiddleTimeFontSize: " + mMiddleTimeFontSize);
    mScaleTimeFontSize = sp2px(getContext(), mScaleTimeFontSizeInSp);
    Log.i("TimeBar", "mScaleTimeFontSize: " + mScaleTimeFontSize);
    this.mMiddleLineTimeY = sp2px(getContext(), this.mMiddleLineTimeYInSp);
  }
  
  private int sp2px(Context paramContext, float paramFloat)
  {
    float f = paramContext.getResources().getDisplayMetrics().scaledDensity;
    Log.i("TimeBar", "scaledDensity: " + f);
    return (int)(0.5F + paramFloat * f);
  }
  
  /* 更新视频时间段 */
  private void updateFileListPos()
  {
    Iterator frIt = this.mFileRectList.iterator();
    
    while (frIt.hasNext()) {
    	FileRect fr = (FileRect)frIt.next();
        long startTimeMillis = fr.getStartTimeInMillis();
        long stopTimeMillis = fr.getStopTimeInMillis();
        long offset = startTimeMillis - this.mMiddleLineTime.getTimeInMillis();
        float left = this.mMiddleLineX + (float)(offset / (1.0D * mCellMilliSeconds) * this.mCellWidth);
        //float top = this.mHeight - this.mFileRectHeight;
        float top = this.mMiddleTimeFontSize + this.mFileRectHeight;
        int frWidth = (int)((stopTimeMillis - startTimeMillis) / (1.0D * mCellMilliSeconds) * this.mCellWidth);
        if (frWidth == 0) {
          frWidth = 1;
        }
        fr.setPos(left, top, frWidth, this.mFileRectHeight);
    }
//    for (;;)
//    {
//      if (!frIt.hasNext()) {
//        return;
//      }
//      FileRect localFileRect = (FileRect)frIt.next();
//      long l1 = localFileRect.getStartTimeInMillis();
//      long l2 = localFileRect.getStopTimeInMillis();
//      long l3 = l1 - this.mMiddleLineTime.getTimeInMillis();
//      float f1 = this.mMiddleLineX + (float)(l3 / 3600000.0D * this.mCellWidth);
//      float f2 = this.mHeight - this.mFileRectHeight;
//      int i = (int)((l2 - l1) / 3600000.0D * this.mCellWidth);
//      if (i == 0) {
//        i = 1;
//      }
//      localFileRect.setPos(f1, f2, i, this.mFileRectHeight);
//    }
    
  }
  
  /* 更新中间线时间 */
  private void updateMiddleLineTime(float offset)
  {
    long l1 = (long) (offset * this.mMilliSecondsPerPixel);
    long l2 = this.mMiddleLineTime.getTimeInMillis() - l1;
    this.mMiddleLineTime.setTimeInMillis(l2);
  }
  
  /* 更新刻度位置 */
  private void updateScalePos()
  {
    Iterator scaleinfoIt = this.mScaleInfoList.iterator();
    
    while (scaleinfoIt.hasNext()) {
    	ScaleInfo scaleinfo = (ScaleInfo)scaleinfoIt.next();
        long deltaTInSecond = 3600 * (scaleinfo.getHour() - this.mMiddleLineTime.get(Calendar.HOUR_OF_DAY)) 
        		+ 60 * (scaleinfo.getMinute() - this.mMiddleLineTime.get(Calendar.MINUTE)) 
        		+ (scaleinfo.getSecond() - this.mMiddleLineTime.get(Calendar.SECOND));
        int i = (int)this.mMiddleLineX + (int)(1000L * deltaTInSecond / (1.0D * mCellMilliSeconds) * this.mCellWidth);
        scaleinfo.setPosRange(0.0F, 1.0F * mTotoalCellNumber * this.mCellWidth);
        scaleinfo.setPos(i);
    }
    
//    for (;;)
//    {
//      if (!scaleinfoIt.hasNext()) {
//        return;
//      }
//      ScaleInfo localScaleInfo = (ScaleInfo)scaleinfoIt.next();
//      long l = 3600 * (localScaleInfo.getHour() - this.mMiddleLineTime.get(11)) + 60 * (localScaleInfo.getMinute() - this.mMiddleLineTime.get(12)) + (localScaleInfo.getSecond() - this.mMiddleLineTime.get(13));
//      int i = (int)this.mMiddleLineX + (int)(1000L * l / 3600000.0D * this.mCellWidth);
//      localScaleInfo.setPosRange(0.0F, 24.0F * this.mCellWidth);
//      localScaleInfo.setPos(i);
//    }
  }
  
  public void addFileInfo(int type, Calendar startTime, Calendar stopTime)
  {
	  
	if (startTime != null && stopTime != null) {
		this.mFileRectList.add(createFileRectInfo(type, startTime, stopTime));
	    invalidate();
	}
	  
	
//    if ((startTime == null) || (stopTime == null)) {}
//    for (;;)
//    {
//      return;
//      this.mFileRectList.add(createFileRectInfo(type, startTime, stopTime));
//      invalidate();
//    }
  }
  
  public void addFileInfoList(List<FileInfo> paramList)
  {
    if (paramList == null) {
      return;
    }
    Iterator fiIt = paramList.iterator();
    
    while (fiIt.hasNext()) {
    	FileInfo fi = (FileInfo)fiIt.next();
        if (fi != null) {
          this.mFileRectList.add(createFileRectInfo(fi.getType(), fi.getStartTime(), fi.getStopTime()));
        }
    }
    
    invalidate();
    
//    for (;;)
//    {
//      if (!fiIt.hasNext())
//      {
//        invalidate();
//        break;
//      }
//      FileInfo localFileInfo = (FileInfo)fiIt.next();
//      if (localFileInfo != null) {
//        this.mFileRectList.add(createFileRectInfo(localFileInfo.getType(), localFileInfo.getStartTime(), localFileInfo.getStopTime()));
//      }
//    }
  }
  
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);
    Paint paint = new Paint();
    paint.setColor(mScaleColor);
    paint.setTextSize(mScaleTimeFontSize);
    Iterator scaleInfoIt = this.mScaleInfoList.iterator();
    Iterator fileRectIt = null;
    
    
    // 绘制横线
    canvas.drawLine(2, this.mScaleLineY + this.mScaleLineHeight + 1, this.mWidth - 2, 
    		this.mScaleLineY + this.mScaleLineHeight + 1, paint);
    
    /* 绘制刻度信息 */
    while (scaleInfoIt.hasNext()) {
    	ScaleInfo si = (ScaleInfo)scaleInfoIt.next();
//        if (!si.isInRange(mMiddleLineTime.getTimeInMillis() / mMilliSecondsPerPixel - mMiddleLineX , 
//        		mMiddleLineTime.getTimeInMillis() / mMilliSecondsPerPixel + mMiddleLineX)) {
//          break;
//        }
        
    	
    	// 绘制刻度竖线
        canvas.drawLine(si.getX(), this.mScaleLineY, si.getX(), this.mScaleLineY + this.mScaleLineHeight, paint);

        // 绘制刻度上的文本
        //canvas.drawText(si.getTime(), si.getX() - paint.measureText("00:00") / 2.0F, this.mScaleTimeY, paint);
        canvas.drawText(si.getTime(), si.getX(), this.mScaleTimeY, paint);
    }
    
    /* 绘制视频矩形条 */
    paint.setColor(mFileInfoColor);
    fileRectIt = this.mFileRectList.iterator();
    
    while (fileRectIt.hasNext()) {
    	((FileRect)fileRectIt.next()).draw(canvas, paint, 0, this.mWidth);
    }
    
    /* 绘制中线、中线时间文字 */
    paint.setColor(Color.BLUE);
    canvas.drawLine(this.mMiddleLineX, 0.0F, this.mMiddleLineX, this.mHeight, paint);
    String str = mTimeFormat.format(this.mMiddleLineTime.getTime());
    paint.setColor(mMiddleTimeColor);
    paint.setTextSize(mMiddleTimeFontSize);
    paint.setTypeface(mMiddleTimefont);
    float f = 2.0F + (paint.measureText(str) / 2.0F + paint.measureText("0"));
    canvas.drawText(str, this.mMiddleLineX - f, this.mMiddleLineTimeY, paint);
    
//    if (!scaleInfoIt.hasNext())
//    {
//      paint.setColor(-mFileInfoColor);
//      fileRectIt = this.mFileRectList.iterator();
//    }
//    for (;;)
//    {
//      if (!fileRectIt.hasNext())
//      {
//        paint.setColor(-256);
//        canvas.drawLine(this.mMiddleLineX, 0.0F, this.mMiddleLineX, this.mHeight, paint);
//        String str = mTimeFormat.format(this.mMiddleLineTime.getTime());
//        paint.setColor(-1);
//        paint.setTextSize(mMiddleTimeFontSize);
//        paint.setTypeface(mMiddleTimefont);
//        float f = 2.0F + (paint.measureText(str) / 2.0F + paint.measureText("0"));
//        canvas.drawText(str, this.mMiddleLineX - f, this.mMiddleLineTimeY, paint);
//        return;
//        ScaleInfo localScaleInfo = (ScaleInfo)scaleInfoIt.next();
//        if (!localScaleInfo.isInRange(0.0F, this.mWidth)) {
//          break;
//        }
//        canvas.drawText(localScaleInfo.getTime(), localScaleInfo.getX() - paint.measureText("00:00") / 2.0F, this.mScaleTimeY, paint);
//        canvas.drawLine(localScaleInfo.getX(), this.mScaleLineY, localScaleInfo.getX(), this.mScaleLineY + this.mScaleLineHeight, paint);
//        break;
//      }
//      
//    }
    
    
    
  }
  
  protected void onSizeChanged(int w, int h, int oldw, int oldh)
  {
    super.onSizeChanged(w, h, oldw, oldh);
    this.mWidth = w;
    this.mHeight = h;
    this.mMiddleLineX = (w / 2);
    this.mCellWidth = (w / (1.0F * mShowCellNumber));
    this.mMilliSecondsPerPixel = ((1.0F * mCellMilliSeconds) / this.mCellWidth);
    //this.mScaleTimeY = ((int)(0.5D * h));
    //this.mScaleLineY = (3.0F + this.mScaleTimeY);
    //this.mScaleLineY = ((int)(0.5D * h));
    //this.mScaleTimeY = (18.0F + mScaleLineHeight + this.mScaleLineY);
    this.mScaleTimeY = h;
    this.mScaleLineY = this.mScaleTimeY - mScaleLineHeight - 16.0F;
    updateScalePos();
    updateFileListPos();
  }
  
  public boolean onTouchEvent(MotionEvent event)
  {
//    switch (event.getAction())
//    {
//    }
//    for (;;)
//    {
//      return true;
//      OnActionDown(event);
//      continue;
//      OnActionUp(event);
//      continue;
//      OnActionMove(event);
//    }
    
    switch (event.getAction()) {
    case MotionEvent.ACTION_DOWN:
    	OnActionDown(event);
    	break;
    case MotionEvent.ACTION_UP:
    	OnActionUp(event);
    	break;
    case MotionEvent.ACTION_MOVE:
    	OnActionMove(event);
    	break;
    }
    
    return true;
  }
  
  public void reset()
  {
    this.mFileRectList.clear();
    setCurrentTime(Calendar.getInstance());
    invalidate();
  }
  
  public void setCurrentTime(Calendar calendar)
  {
	  
	if (calendar != null) {
		if (!this.mTouchDownFlag)
	      {
	        this.mMiddleLineTime.setTime(calendar.getTime());
	        updateScalePos();
	        updateFileListPos();
	        invalidate();
	      }
	}
//    if (calendar == null) {}
//    for (;;)
//    {
//      return;
//      if (!this.mTouchDownFlag)
//      {
//        this.mMiddleLineTime.setTime(calendar.getTime());
//        updateScalePos();
//        updateFileListPos();
//        invalidate();
//      }
//    }
  }
  
  public void setFrozen(boolean paramBoolean)
  {
    this.mIsFrozen = paramBoolean;
  }
  
  public void setTimeBarCallback(TimePickedCallBack paramTimePickedCallBack)
  {
    this.mTimePickCallback = paramTimePickedCallBack;
  }
  
  public static abstract interface TimePickedCallBack
  {
    public abstract void onTimePickedCallback(Calendar paramCalendar);
  }
}


/* Location:           D:\kuaipan\鎴戠殑璧勬枡\鐮旂┒鐢熼樁娈礬椤圭洰\鏄熺綉瀹夐槻\star-security\iVMS-4500\classes_dex2jar.jar
 * Qualified Name:     com.mcu.iVMS.component.TimeBar
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */
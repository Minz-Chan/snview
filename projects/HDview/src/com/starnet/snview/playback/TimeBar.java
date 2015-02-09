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

public class TimeBar extends View {
	private static final String TAG = "TimeBar";

	private Calendar mPickedTime = new GregorianCalendar();
	private TimePickedCallBack mTimePickCallback;
	private boolean mIsFrozen = false;
	private float mLastTouchX = 0.0F;
	private boolean mTouchDownFlag = false;
	private boolean mTouchMoved = false;
	private float mMoveSensitive = 0.2F;

	private int mWidth = 0; // 控件宽度
	private int mHeight = 0; // 控件 高度
	private float mMilliSecondsPerPixel = 0.0F; // 每个像素代表的毫秒数

	private float mCellWidth = 0.0F; // 刻度单元格宽度，在iVMS中为宽度的1/4
	private static final long mCellMilliSeconds = 3600000L;
	private static final int mShowCellNumber = 5;
	private static final int mTotoalCellNumber = 24;

	/**
	 * 中线/中线时间文本 （middleLine/middleTime）相关
	 */
	private GregorianCalendar mMiddleLineTime = new GregorianCalendar();
	private static final SimpleDateFormat mTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 中线时间格式
	private static final int mMiddleTimeColor = 0xfff15a24; // 中线时间文字颜色
	private static final Typeface mMiddleTimefont = Typeface.create(
			Typeface.SANS_SERIF, 1); // 中线时间文字字体
	private static int mMiddleTimeFontSize = 18; // 中线时间文字字体大小
	private static final float mMiddleTimeFontSizeInSp = 12.0F; // 中线时间字体大小（单位sp）
	private float mMiddleLineX = 0.0F; // 中线X轴坐标
	private float mMiddleLineTimeY = 15.0F; // 中线Y轴坐标（单位px）
	private float mMiddleLineTimeYInSp = 10.0F; // 中线Y轴坐标（单位sp）

	/**
	 * 刻度(Scale)相关
	 */
	private List<ScaleInfo> mScaleInfoList = new LinkedList<ScaleInfo>(); // 刻度信息列表
	private static final int mScaleColor = Color.BLACK; // 刻度线颜色
	private static int mScaleTimeFontSize = 12; // 刻度时间字体大小（单位px）
	private static final float mScaleTimeFontSizeInSp = 9.0F; // 刻度时间字体大小（单位sp）

	private float mScaleLineY = 45.0F; // 刻度线Y轴坐标
	private float mScaleTimeY = 57.0F; // 时间Y轴坐标
	private float mScaleLineHeight = 6.0F; // 刻度线高度

	/**
	 * 矩形条（FileRect）相关
	 */
	private List<FileRect> mFileRectList = new LinkedList<FileRect>();
	private static final int mFileInfoColor = -10256404; // 视频文件信息矩形条颜色
	private int mFileRectHeight = 12;

	public TimeBar(Context context) {
		super(context);
		initView();
	}

	public TimeBar(Context context, AttributeSet attributes) {
		super(context, attributes);
		initView();
	}

	private void OnActionDown(MotionEvent e) {
		mLastTouchX = e.getX();
		mTouchDownFlag = true;
	}

	private void OnActionMove(MotionEvent e) {
		float moveOffsetOnX = e.getX() - mLastTouchX;

		if (Math.abs(moveOffsetOnX) >= mMoveSensitive) {
			mLastTouchX = e.getX();
			if (!mIsFrozen) {
				mTouchMoved = true;
				UpdateDataPos(moveOffsetOnX); // 根据偏移更新中线时间、刻度位置、矩形位置
				invalidate(); // 强制onDraw重绘
			}
		}
	}

	private void OnActionUp(MotionEvent e) {
		if ((mTouchMoved) && (mTimePickCallback != null)) {
			mPickedTime.setTime(mMiddleLineTime.getTime());
			mTimePickCallback.onTimePickedCallback(mPickedTime);
		}
		mTouchMoved = false;
		mTouchDownFlag = false;
	}

	private void UpdateDataPos(float xOffset) {
		updateMiddleLineTime(xOffset);
		updateScalePos();
		updateFileListPos();
	}

	private FileRect createFileRectInfo(int type, Calendar startTime,
			Calendar stopTime) {
		long l1 = startTime.getTimeInMillis();
		long l2 = stopTime.getTimeInMillis();
		long l3 = mMiddleLineTime.getTimeInMillis();
		float x = mMiddleLineX
				+ (float) ((l1 - l3) / (1.0D * mCellMilliSeconds) * mCellWidth);
		int frWidth = (int) ((l2 - l1) / (1.0D * mCellMilliSeconds) * mCellWidth);
		return new FileRect(x, mHeight - mFileRectHeight, frWidth,
				mFileRectHeight, type, startTime, stopTime);
	}

	private void initVectorScale() {
		// 初始化刻度信息
		for (int i = 0; i < mTotoalCellNumber; i++) {
			ScaleInfo scaleinfo = new ScaleInfo(-1, (int) (3600L * i));
			scaleinfo.setPosRange(0.0F, 1.0F * mTotoalCellNumber * mCellWidth);
			mScaleInfoList.add(scaleinfo);
		}
	}

	private void initView() {
		initVectorScale();
		mMiddleTimeFontSize = sp2px(getContext(), mMiddleTimeFontSizeInSp);
		Log.i("TimeBar", "mMiddleTimeFontSize: " + mMiddleTimeFontSize);
		mScaleTimeFontSize = sp2px(getContext(), mScaleTimeFontSizeInSp);
		Log.i("TimeBar", "mScaleTimeFontSize: " + mScaleTimeFontSize);
		mMiddleLineTimeY = sp2px(getContext(), mMiddleLineTimeYInSp);
	}

	private int sp2px(Context context, float sp) {
		float density = context.getResources().getDisplayMetrics().scaledDensity;
		Log.i("TimeBar", "scaledDensity: " + density);
		return (int) (0.5F + sp * density);
	}

	/* 更新视频时间段 */
	private void updateFileListPos() {
		Iterator<FileRect> frIt = mFileRectList.iterator();

		while (frIt.hasNext()) {
			FileRect fr = (FileRect) frIt.next();
			long startTimeMillis = fr.getStartTimeInMillis();
			long stopTimeMillis = fr.getStopTimeInMillis();
			long timeDelta = startTimeMillis
					- mMiddleLineTime.getTimeInMillis();
			float left = mMiddleLineX
					+ (float) (timeDelta / (1.0D * mCellMilliSeconds) * mCellWidth);
			float top = mMiddleTimeFontSize + mFileRectHeight;
			int frWidth = (int) ((stopTimeMillis - startTimeMillis)
					/ (1.0D * mCellMilliSeconds) * mCellWidth);
			if (frWidth == 0) {
				frWidth = 1;
			}
			fr.setPos(left, top, frWidth, mFileRectHeight);
		}
	}

	/* 更新中间线时间 */
	private void updateMiddleLineTime(float offset) {
		long timeOffset = (long) (offset * mMilliSecondsPerPixel);
		long timeNew = mMiddleLineTime.getTimeInMillis() - timeOffset;
		mMiddleLineTime.setTimeInMillis(timeNew);
	}

	/* 更新刻度位置 */
	private void updateScalePos() {
		Iterator<ScaleInfo> scaleinfoIt = mScaleInfoList.iterator();
		while (scaleinfoIt.hasNext()) {
			ScaleInfo scaleinfo = (ScaleInfo) scaleinfoIt.next();
			long deltaTInSecond = 3600 * (scaleinfo.getHour() - mMiddleLineTime.get(Calendar.HOUR_OF_DAY)) + 60 * (scaleinfo.getMinute() - mMiddleLineTime.get(Calendar.MINUTE)) + (scaleinfo.getSecond() - mMiddleLineTime.get(Calendar.SECOND));
			int i = (int) mMiddleLineX + (int) (1000L * deltaTInSecond / (1.0D * mCellMilliSeconds) * mCellWidth);
			scaleinfo.setPosRange(0.0F, 1.0F * mTotoalCellNumber * mCellWidth);
			scaleinfo.setPos(i);
		}
	}

	public void addFileInfo(int type, Calendar startTime, Calendar stopTime) {
		if (startTime != null && stopTime != null) {
			mFileRectList.add(createFileRectInfo(type, startTime, stopTime));
			invalidate();
		}
	}

	public void addFileInfoList(List<FileInfo> fileinfos) {
		if (fileinfos == null) {
			return;
		}
		Iterator<FileInfo> fiIt = fileinfos.iterator();

		while (fiIt.hasNext()) {
			FileInfo fi = (FileInfo) fiIt.next();
			if (fi != null) {
				mFileRectList.add(createFileRectInfo(fi.getType(),
						fi.getStartTime(), fi.getStopTime()));
			}
		}

		invalidate();
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setColor(mScaleColor);
		paint.setTextSize(mScaleTimeFontSize);
		Iterator<ScaleInfo> scaleInfoIt = mScaleInfoList.iterator();
		Iterator<FileRect> fileRectIt = null;

		// 绘制横线
		canvas.drawLine(2, mScaleLineY + mScaleLineHeight + 1, mWidth - 2,mScaleLineY + mScaleLineHeight + 1, paint);

		/* 绘制刻度信息 */
		while (scaleInfoIt.hasNext()) {
			ScaleInfo si = (ScaleInfo) scaleInfoIt.next();
			// if (!si.isInRange(mMiddleLineTime.getTimeInMillis() /
			// mMilliSecondsPerPixel - mMiddleLineX ,
			// mMiddleLineTime.getTimeInMillis() / mMilliSecondsPerPixel +
			// mMiddleLineX)) {
			// break;
			// }

			// 绘制刻度竖线
			canvas.drawLine(si.getX(), mScaleLineY, si.getX(), mScaleLineY
					+ mScaleLineHeight, paint);

			// 绘制刻度上的文本
			// canvas.drawText(si.getTime(), si.getX() -
			// paint.measureText("00:00") / 2.0F, this.mScaleTimeY, paint);
			
			canvas.drawText(si.getTime(), si.getX(), mScaleTimeY, paint);
		}

		/* 绘制视频矩形条 */
		paint.setColor(mFileInfoColor);
		fileRectIt = mFileRectList.iterator();

		while (fileRectIt.hasNext()) {
			((FileRect) fileRectIt.next()).draw(canvas, paint, 0, mWidth);
		}

		/* 绘制中线、中线时间文字 */
		paint.setColor(Color.BLUE);
		canvas.drawLine(mMiddleLineX, 0.0F, mMiddleLineX, mHeight, paint);
		String str = mTimeFormat.format(mMiddleLineTime.getTime());
		paint.setColor(mMiddleTimeColor);
		paint.setTextSize(mMiddleTimeFontSize);
		paint.setTypeface(mMiddleTimefont);
		float f = 2.0F + (paint.measureText(str) / 2.0F + paint
				.measureText("0"));
		Log.i(TAG, "time:"+str);
		currrentTime = str;
		canvas.drawText(str, mMiddleLineX - f, mMiddleLineTimeY, paint);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		mMiddleLineX = (w / 2);
		mCellWidth = (w / (1.0F * mShowCellNumber));
		mMilliSecondsPerPixel = ((1.0F * mCellMilliSeconds) / mCellWidth);
		// this.mScaleTimeY = ((int)(0.5D * h));
		// this.mScaleLineY = (3.0F + this.mScaleTimeY);
		// this.mScaleLineY = ((int)(0.5D * h));
		// this.mScaleTimeY = (18.0F + mScaleLineHeight + this.mScaleLineY);
		mScaleTimeY = h;
		mScaleLineY = mScaleTimeY - mScaleLineHeight - 16.0F;
		updateScalePos();
		updateFileListPos();
	}

	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			OnActionDown(e);
			break;
		case MotionEvent.ACTION_UP:
			OnActionUp(e);
			break;
		case MotionEvent.ACTION_MOVE:
			OnActionMove(e);
			break;
		}

		return true;
	}

	public void reset() {
		mFileRectList.clear();
		setCurrentTime(Calendar.getInstance());
		invalidate();
	}

	public void setCurrentTime(Calendar calendar) {
		if (calendar != null) {
			if (!mTouchDownFlag) {
				mMiddleLineTime.setTime(calendar.getTime());
				updateScalePos();
				updateFileListPos();
				invalidate();
			}
		}
	}

	public void setFrozen(boolean isFrozen) {
		this.mIsFrozen = isFrozen;
	}

	public void setTimeBarCallback(TimePickedCallBack timePickedCallBack) {
		this.mTimePickCallback = timePickedCallBack;
	}
	
	private String currrentTime;
	/**获取当前的显示时间***/
	public String getCurrentTime(){
		return currrentTime;
	}
	/**设置当前的显示时间***/
	public void setCurrentTime(String currrentTime){
		this.currrentTime = currrentTime;
	}

	public static abstract interface TimePickedCallBack {
		public abstract void onTimePickedCallback(Calendar c);
	}
}
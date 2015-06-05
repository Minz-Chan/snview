package com.video.hdview.playback;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class ScaleInfo {
	/* 三者可确定一刻度所在位置 */
	private int mHour;
	private int mMinute;
	private int mSecond;

	/* 刻度所在X最大值、最小值 */
	private float mMaxX;
	private float mMinX;

	private String mTime;

	/* 刻度起始基点X */
	private float mX;

	ScaleInfo(int x, int totalSeconds) {
		this.mX = x;
		this.mHour = (totalSeconds / 3600);
		this.mMinute = (totalSeconds % 3600 / 60);
		this.mSecond = (totalSeconds % 3600 % 60);
		Object[] arrayOfObject = new Object[2];
		arrayOfObject[0] = Integer.valueOf(this.mHour);
		arrayOfObject[1] = Integer.valueOf(this.mMinute);
		this.mTime = String.format("%02d:%02d", arrayOfObject);
	}

	@SuppressLint("SimpleDateFormat")
	private int getPassedSeconds(String timeString) {
		int i = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date localDate = sdf.parse(timeString);
			GregorianCalendar gCalendar = new GregorianCalendar();
			gCalendar.setTime(localDate);
			int j = this.mHour - gCalendar.get(Calendar.HOUR_OF_DAY);
			int k = this.mMinute - gCalendar.get(Calendar.MINUTE);
			i = this.mSecond - gCalendar.get(Calendar.SECOND)
					+ (j * 3600 + k * 60);
		} catch (Exception localException) {
			// for (;;)
			// {
			System.out.println(localException);
			i = 0;
			// }
		}

		return i;
	}

	public int getHour() {
		return this.mHour;
	}

	public int getMinute() {
		return this.mMinute;
	}

	public int getSecond() {
		return this.mSecond;
	}

	public String getTime() {
		return this.mTime;
	}

	public float getX() {
		return this.mX;
	}

	public boolean isInRange(float x1, float x2) {
		if ((this.mX >= x1) && (this.mX <= x2)) {
			return true;
		} else {
			return false;
		}

	}

	/* 设置刻度所在位置 */
	public void setPos(float pos) {
		if (pos < this.mMinX) {
			pos = this.mMaxX - (this.mMinX - pos);
		} else {
			if (pos > this.mMaxX) {
				pos = this.mMinX + (pos - this.mMaxX);
			}
		}

		this.mX = pos;
	}

	public void setPosByTime(String time, int paramInt1, float paramFloat,
			int paramInt2) {
		this.mX = (paramInt1 + (int) (getPassedSeconds(time) / paramInt2 * paramFloat));
		if (this.mX < this.mMinX) {
			this.mX = (this.mMaxX - (this.mMinX - this.mX));
		} else {
			this.mX = (this.mMinX + (this.mX - this.mMaxX));
		}

	}

	public void setPosRange(float minX, float maxX) {
		this.mMinX = minX;
		this.mMaxX = maxX;
	}
}

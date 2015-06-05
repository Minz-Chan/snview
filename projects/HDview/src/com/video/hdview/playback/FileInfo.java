package com.video.hdview.playback;

import java.util.Calendar;

public class FileInfo implements Comparable<FileInfo> {
	private Calendar mStartTime = null;
	private Calendar mStopTime = null;
	private int mType = 0;

	public FileInfo(int type, Calendar startTime, Calendar stopTime) {
		this.mType = type;
		this.mStartTime = startTime;
		this.mStopTime = stopTime;
	}

	public int compareTo(FileInfo fi) {
		return mStartTime.compareTo(fi.mStartTime);
	}

	public Calendar getStartTime() {
		return mStartTime;
	}

	public long getStartTimeInMillis() {
		return mStartTime.getTimeInMillis();
	}

	public Calendar getStopTime() {
		return mStopTime;
	}

	public long getStopTimeInMillis() {
		return mStopTime.getTimeInMillis();
	}

	public int getType() {
		return mType;
	}
}

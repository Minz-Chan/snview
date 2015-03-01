package com.starnet.snview.protocol.message;

import android.os.Parcel;
import android.os.Parcelable;

public class OWSPDateTime implements Parcelable {/**
	 * 
	 */
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;

	public int getYear() {
		return year;
	}
	
	public OWSPDateTime(){
		
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}
	
	private OWSPDateTime(Parcel in){
		this.year = in.readInt();
		this.month = in.readInt();
		this.day = in.readInt();
		this.hour = in.readInt();
		this.minute = in.readInt();
		this.second = in.readInt();
	}
	
	public static final Parcelable.Creator<OWSPDateTime> CREATOR = new Parcelable.Creator<OWSPDateTime>() {

		@Override
		public OWSPDateTime createFromParcel(Parcel in) {
			return new OWSPDateTime(in);
		}

		@Override
		public OWSPDateTime[] newArray(int size) {
			return new OWSPDateTime[size];
		}
	};

	
	public int describeContents() {
		return 0;
	}

	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(year);
		dest.writeInt(month);
		dest.writeInt(day);
		dest.writeInt(hour);
		dest.writeInt(minute);
		dest.writeInt(second);
	}

	@Override
	public String toString() {
		StringBuffer t = new StringBuffer();
		t.append(year).append("-").append(month).append("-").append(day)
				.append(" ").append(hour).append(":").append(minute)
				.append(":").append(second);
		
		return t.toString();
	}
	
	
}
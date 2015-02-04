package com.starnet.snview.playback.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.starnet.snview.protocol.message.OWSPDateTime;

public class TLV_V_RecordInfo implements Parcelable{

	private int deviceid;
	private OWSPDateTime startTime;
	private OWSPDateTime endTime;
	private int channel;
	private int recordTypeMask;
	private int reserve[] = new int[2];

	public TLV_V_RecordInfo(){
		
	}
	
	private TLV_V_RecordInfo(Parcel source){
		this.deviceid = source.readInt();
		
		int startYear = source.readInt();
		int startMonth = source.readInt();
		int startDay = source.readInt();
		int startHour = source.readInt();
		int startMinute = source.readInt();
		int startSecond = source.readInt();
		startTime.setYear(startYear);
		startTime.setMonth(startMonth);
		startTime.setDay(startDay);
		startTime.setHour(startHour);
		startTime.setMinute(startMinute);
		startTime.setSecond(startSecond);
		
		int endYear = source.readInt();
		int endMonth = source.readInt();
		int endDay = source.readInt();
		int endHour = source.readInt();
		int endMinute = source.readInt();
		int endSecond = source.readInt();
		endTime.setYear(endYear);
		endTime.setMonth(endMonth);
		endTime.setDay(endDay);
		endTime.setHour(endHour);
		endTime.setMinute(endMinute);
		endTime.setSecond(endSecond);
		
//		this.startTime = source.readParcelable(OWSPDateTime.class.getClassLoader());
//		this.endTime = source.readParcelable(OWSPDateTime.class.getClassLoader());
//		Object []os = source.readArray(OWSPDateTime.class.getClassLoader());
//		if (os!=null) {
//			this.startTime = (OWSPDateTime) os[0];
//			this.endTime = (OWSPDateTime) os[1];
//		}
		this.channel = source.readInt();
		this.recordTypeMask = source.readInt();
		source.readIntArray(this.reserve);
		
	}
	
	public int getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(int deviceid) {
		this.deviceid = deviceid;
	}

	public OWSPDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(OWSPDateTime startTime) {
		this.startTime = startTime;
	}

	public OWSPDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(OWSPDateTime endTime) {
		this.endTime = endTime;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getRecordTypeMask() {
		return recordTypeMask;
	}

	public void setRecordTypeMask(int recordTypeMask) {
		this.recordTypeMask = recordTypeMask;
	}

	public int[] getReserve() {
		return reserve;
	}

	public void setReserve(int[] reserve) {
		this.reserve = reserve;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(deviceid);
//		dest.writeParcelable(startTime, Parcelable.CONTENTS_FILE_DESCRIPTOR);
//		dest.writeParcelable(endTime, Parcelable.CONTENTS_FILE_DESCRIPTOR);
		
		dest.writeInt(startTime.getYear());
		dest.writeInt(startTime.getMonth());
		dest.writeInt(startTime.getDay());
		dest.writeInt(startTime.getHour());
		dest.writeInt(startTime.getMinute());
		dest.writeInt(startTime.getSecond());
		
		dest.writeInt(endTime.getYear());
		dest.writeInt(endTime.getMonth());
		dest.writeInt(endTime.getDay());
		dest.writeInt(endTime.getHour());
		dest.writeInt(endTime.getMinute());
		dest.writeInt(endTime.getSecond());
//		dest.writeParcelable(startTime, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
//		dest.writeParcelable(endTime, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
//		dest.writeArray(new Object[]{startTime,endTime});
		dest.writeInt(channel);
		dest.writeInt(recordTypeMask);
		dest.writeIntArray(reserve);
	}
	
	public static Parcelable.Creator<TLV_V_RecordInfo> CREATOR = new Parcelable.Creator<TLV_V_RecordInfo>(){
		@Override
		public TLV_V_RecordInfo createFromParcel(Parcel source) {
			return new TLV_V_RecordInfo(source);
		}

		@Override
		public TLV_V_RecordInfo[] newArray(int size) {
			return new TLV_V_RecordInfo[size];
		}
	};
}
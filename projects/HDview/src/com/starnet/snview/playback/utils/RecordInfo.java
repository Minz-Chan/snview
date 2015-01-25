package com.starnet.snview.playback.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.starnet.snview.protocol.message.OWSPDateTime;

public class RecordInfo implements Parcelable{

	private int deviceid;
	private OWSPDateTime startTime;
	private OWSPDateTime endTime;
	private int channel;
	private int recordTypeMask;
	private int reserve[] = new int[2];

	public RecordInfo(){
		
	}
	
	private RecordInfo(Parcel source){
		this.deviceid = source.readInt();
		Object []os = source.readArray(OWSPDateTime.class.getClassLoader());
		if (os!=null) {
			this.startTime = (OWSPDateTime) os[0];
			this.endTime = (OWSPDateTime) os[1];
		}
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
		dest.writeArray(new Object[]{startTime,endTime});
		dest.writeInt(channel);
		dest.writeInt(recordTypeMask);
		dest.writeIntArray(reserve);
	}
	
	public static Parcelable.Creator<RecordInfo> creator = new Parcelable.Creator<RecordInfo>(){
		@Override
		public RecordInfo createFromParcel(Parcel source) {
			return new RecordInfo(source);
		}

		@Override
		public RecordInfo[] newArray(int size) {
			return new RecordInfo[size];
		}
	};
}
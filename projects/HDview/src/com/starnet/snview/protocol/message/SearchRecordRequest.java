package com.starnet.snview.protocol.message;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchRecordRequest implements Parcelable{
	
	private int deviceId;//设备编号
	private int channel;//通道号
	private int recordType;//记录类型
	private int count;//记录总数
	private OWSPDateTime startTime;
	private OWSPDateTime endTime;
	private int []reserve = new int[3];
	
	public SearchRecordRequest(){
		
	}
	
	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public int getRecordType() {
		return recordType;
	}
	public void setRecordType(int recordType) {
		this.recordType = recordType;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
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
		dest.writeInt(deviceId);
		dest.writeInt(channel);
		dest.writeInt(recordType);
		dest.writeInt(count);
//		dest.writeValue(startTime);
//		dest.writeValue(endTime);
		dest.writeParcelable(startTime, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeParcelable(endTime, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
		dest.writeIntArray(reserve);
	}
	
	private SearchRecordRequest(Parcel source){
		this.deviceId = source.readInt();
		this.channel = source.readInt();
		this.recordType = source.readInt();
		this.count = source.readInt();
		this.startTime = source.readParcelable(OWSPDateTime.class.getClassLoader());
		this.endTime = source.readParcelable(OWSPDateTime.class.getClassLoader());
		
	}
	
	public static final Parcelable.Creator<SearchRecordRequest> CREATOR = new Parcelable.Creator<SearchRecordRequest>(){
		@Override
		public SearchRecordRequest createFromParcel(Parcel source) {
			return new SearchRecordRequest(source);
		}

		@Override
		public SearchRecordRequest[] newArray(int size) {
			return new SearchRecordRequest[size];
		}
	};
}
package com.video.hdview.playback.utils;

import com.video.hdview.protocol.message.OWSPDateTime;
import android.os.Parcel;
import android.os.Parcelable;

public class TLV_V_SearchRecordRequest implements Parcelable {

	private int deviceId;// 设备编号
	private int channel;// 通道号
	private int recordType;// 远程录像回放类型
	private int count;// 记录总数
	private OWSPDateTime startTime;
	private OWSPDateTime endTime;
	private int[] reserve = new int[3];

	public TLV_V_SearchRecordRequest() {

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

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(deviceId);
		dest.writeInt(channel);
		dest.writeInt(recordType);
		dest.writeInt(count);

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

		dest.writeIntArray(reserve);
	}

	private TLV_V_SearchRecordRequest(Parcel source) {
		this.deviceId = source.readInt();
		this.channel = source.readInt();
		this.recordType = source.readInt();
		this.count = source.readInt();

		OWSPDateTime sTime = new OWSPDateTime();
		int sYear = source.readInt();
		int sMonth = source.readInt();
		int sDay = source.readInt();
		int sHour = source.readInt();
		int sMinute = source.readInt();
		int sSecond = source.readInt();
		sTime.setYear(sYear);
		sTime.setMonth(sMonth);
		sTime.setDay(sDay);
		sTime.setHour(sHour);
		sTime.setMinute(sMinute);
		sTime.setSecond(sSecond);

		OWSPDateTime eTime = new OWSPDateTime();
		int eYear = source.readInt();
		int eMonth = source.readInt();
		int eDay = source.readInt();
		int eHour = source.readInt();
		int eMinute = source.readInt();
		int eSecond = source.readInt();
		eTime.setYear(eYear);
		eTime.setMonth(eMonth);
		eTime.setDay(eDay);
		eTime.setHour(eHour);
		eTime.setMinute(eMinute);
		eTime.setSecond(eSecond);

		this.startTime = sTime;
		this.endTime = eTime;
		source.readIntArray(reserve);

	}

	public static final Parcelable.Creator<TLV_V_SearchRecordRequest> CREATOR = new Parcelable.Creator<TLV_V_SearchRecordRequest>() {
		@Override
		public TLV_V_SearchRecordRequest createFromParcel(Parcel source) {
			return new TLV_V_SearchRecordRequest(source);
		}

		@Override
		public TLV_V_SearchRecordRequest[] newArray(int size) {
			return new TLV_V_SearchRecordRequest[size];
		}
	};
}
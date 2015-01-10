package com.starnet.snview.playback.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**请求远程回放时使用**/
public class PlayBackDeviceItem implements Parcelable{
	
	private String deviceName;
	private String startPlayTime;
	private String endPlayTime;
	private String channelNo;
	private String recordType;
	private String playIp;
	private String playPort;
	private String userName;
	private String password;
	
	private PlayBackDeviceItem(Parcel in) {
		this.deviceName = in.readString();
		this.startPlayTime = in.readString();
		this.endPlayTime = in.readString();
		this.channelNo = in.readString();
		this.recordType = in.readString();
		this.playIp = in.readString();
		this.playPort = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(deviceName);
		dest.writeString(startPlayTime);
		dest.writeString(endPlayTime);
		dest.writeString(channelNo);
		dest.writeString(recordType);
		dest.writeString(playIp);
		dest.writeString(playPort);
		
	}
	
	public static final Parcelable.Creator<PlayBackDeviceItem> CREATOR = new Parcelable.Creator<PlayBackDeviceItem>() {

		@Override
		public PlayBackDeviceItem createFromParcel(Parcel in) {
			return new PlayBackDeviceItem(in);
		}

		@Override
		public PlayBackDeviceItem[] newArray(int size) {
			return new PlayBackDeviceItem[size];
		}
	};

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getStartPlayTime() {
		return startPlayTime;
	}

	public void setStartPlayTime(String startPlayTime) {
		this.startPlayTime = startPlayTime;
	}

	public String getEndPlayTime() {
		return endPlayTime;
	}

	public void setEndPlayTime(String endPlayTime) {
		this.endPlayTime = endPlayTime;
	}

	public String getChannelNo() {
		return channelNo;
	}

	public void setChannelNo(String channelNo) {
		this.channelNo = channelNo;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getPlayIp() {
		return playIp;
	}

	public void setPlayIp(String playIp) {
		this.playIp = playIp;
	}

	public String getPlayPort() {
		return playPort;
	}

	public void setPlayPort(String playPort) {
		this.playPort = playPort;
	}
}

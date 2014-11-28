package com.starnet.snview.alarmmanager;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmDevice implements Parcelable{
	private String deviceName;
	private String alarmTime; // YYYY-MM-DD HH:MM:SS
	private String alarmType;
	private String alarmContent;
	private String imageUrl;
	private String ip;
	private int port;
	private int channel;
	private String userName;
	private String password;
	private String pusherUserName;
	private String pusherPassword;
	private String pusherDomain;  // domain or ip
	
	
	public AlarmDevice() {
		
	}
	
	private AlarmDevice(Parcel in) {
		this.deviceName = in.readString();
		this.alarmTime = in.readString();
		this.alarmType = in.readString();
		this.alarmContent = in.readString();
		this.imageUrl = in.readString();
		this.ip = in.readString();
		this.port = in.readInt();
		this.channel = in.readInt();
		this.userName = in.readString();
		this.password = in.readString();
		this.pusherUserName = in.readString();
		this.pusherPassword = in.readString();
		this.pusherDomain = in.readString();
	}
	
	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getAlarmTime() {
		return alarmTime;
	}

	public void setAlarmTime(String alarmTime) {
		this.alarmTime = alarmTime;
	}

	public String getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}

	public String getAlarmContent() {
		return alarmContent;
	}

	public void setAlarmContent(String alarmContent) {
		this.alarmContent = alarmContent;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPusherUserName() {
		return pusherUserName;
	}

	public void setPusherUserName(String pusherUserName) {
		this.pusherUserName = pusherUserName;
	}

	public String getPusherPassword() {
		return pusherPassword;
	}

	public void setPusherPassword(String pusherPassword) {
		this.pusherPassword = pusherPassword;
	}

	public String getPusherDomain() {
		return pusherDomain;
	}

	public void setPusherDomain(String pusherDomain) {
		this.pusherDomain = pusherDomain;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(deviceName);
		dest.writeString(alarmTime);
		dest.writeString(alarmType);
		dest.writeString(alarmContent);
		dest.writeString(imageUrl);
		dest.writeString(ip);
		dest.writeInt(port);
		dest.writeInt(channel);
		dest.writeString(userName);
		dest.writeString(password);
		dest.writeString(pusherUserName);
		dest.writeString(pusherPassword);
		dest.writeString(pusherDomain);;
	}
	
	public static final Parcelable.Creator<AlarmDevice> CREATOR = new Parcelable.Creator<AlarmDevice>() {

		@Override
		public AlarmDevice createFromParcel(Parcel in) {
			return new AlarmDevice(in);
		}

		@Override
		public AlarmDevice[] newArray(int size) {
			return new AlarmDevice[size];
		}
	};

}
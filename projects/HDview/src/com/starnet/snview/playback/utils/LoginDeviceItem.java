package com.starnet.snview.playback.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginDeviceItem implements Parcelable {

	private String loginUser;
	private String loginPass;
	private String svrPort;
	private String[] svrIP;
	
	private String platUserName;//星云平台用户名
	private String deviceName;//星云平台设备名称
	
	public LoginDeviceItem(){
		
	}

	private LoginDeviceItem(Parcel source) {
		this.loginUser = source.readString();
		this.loginPass = source.readString();
		this.svrPort = source.readString();
		svrIP = new String[4];
		source.readStringArray(svrIP);
		this.platUserName = source.readString();
		this.deviceName = source.readString();
//		source.readStringList(svrIP);
	}

	public String getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(String loginUser) {
		this.loginUser = loginUser;
	}

	public String getLoginPass() {
		return loginPass;
	}

	public void setLoginPass(String loginPass) {
		this.loginPass = loginPass;
	}

	public String getSvrPort() {
		return svrPort;
	}

	public void setSvrPort(String svrPort) {
		this.svrPort = svrPort;
	}

//	public List<String> getSvrIP() {
//		return svrIP;
//	}
//
//	public void setSvrIP(List<String> svrIP) {
//		this.svrIP = svrIP;
//	}
	public String[] getSvrIP() {
		return svrIP;
	}

	public void setSvrIP(String[]svrIP) {
		this.svrIP = svrIP;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeString(loginUser);
		dest.writeString(loginPass);
		dest.writeString(svrPort);
//		dest.writeStringList(svrIP);
		dest.writeStringArray(svrIP);
		dest.writeString(platUserName);
		dest.writeString(deviceName);
		
	}
	
	public static Parcelable.Creator<LoginDeviceItem> CREATOR = new Parcelable.Creator<LoginDeviceItem>(){

		@Override
		public LoginDeviceItem createFromParcel(Parcel source) {
			return new LoginDeviceItem(source);
		}

		@Override
		public LoginDeviceItem[] newArray(int size) {
			return new LoginDeviceItem[size];
		}
		
	};

	public String getPlatUserName() {
		return platUserName;
	}

	public void setPlatUserName(String platUserName) {
		this.platUserName = platUserName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

}

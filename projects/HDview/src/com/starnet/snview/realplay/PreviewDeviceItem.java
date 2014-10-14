package com.starnet.snview.realplay;

import android.os.Parcel;
import android.os.Parcelable;

public class PreviewDeviceItem implements Parcelable {
	
	
	private String deviceRecordName;   // 设备记录名
	private String svrIp;              // 服务器IP
	private String svrPort;            // 服务器端口
	private String loginUserName;      // 登录用户名
	private String loginPassword;      // 登录密码
	private int channelNo; 	           // 当前预览通道
	
	private String platformUsername;   //平台用户名
	
//	public int isRemove = 0;         //1 表示可以移除;0表示不移除
	public PreviewDeviceItem() {
		
	}
	
	private PreviewDeviceItem(Parcel in) {
		this.deviceRecordName = in.readString();
		this.svrIp = in.readString();
		this.svrPort = in.readString();
		this.loginUserName = in.readString();
		this.loginPassword = in.readString();
		this.channelNo = in.readInt();
		this.platformUsername = in.readString();
		
	}
	
	
	
	public String getDeviceRecordName() {
		return deviceRecordName;
	}

	public void setDeviceRecordName(String deviceRecordName) {
		this.deviceRecordName = deviceRecordName;
	}

	public String getSvrIp() {
		return svrIp;
	}
	public void setSvrIp(String svrIp) {
		this.svrIp = svrIp;
	}
	public String getSvrPort() {
		return svrPort;
	}
	public void setSvrPort(String svrPort) {
		this.svrPort = svrPort;
	}
	public String getLoginUser() {
		return loginUserName;
	}
	public void setLoginUser(String loginUser) {
		this.loginUserName = loginUser;
	}
	public String getLoginPass() {
		return loginPassword;
	}
	public void setLoginPass(String loginPass) {
		this.loginPassword = loginPass;
	}
	public int getChannel() {
		return channelNo;
	}
	public void setChannel(int channel) {
		this.channelNo = channel;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(deviceRecordName);
		out.writeString(svrIp);
		out.writeString(svrPort);
		out.writeString(loginUserName);
		out.writeString(loginPassword);
		out.writeInt(channelNo);
		out.writeString(platformUsername);
	}
	
	public static final Parcelable.Creator<PreviewDeviceItem> CREATOR = new Parcelable.Creator<PreviewDeviceItem>() {

		@Override
		public PreviewDeviceItem createFromParcel(Parcel in) {
			return new PreviewDeviceItem(in);
		}

		@Override
		public PreviewDeviceItem[] newArray(int size) {
			return new PreviewDeviceItem[size];
		}
	};


	public String getPlatformUsername() {
		return platformUsername;
	}

	public void setPlatformUsername(String platformUsername) {
		this.platformUsername = platformUsername;
	}
	
}

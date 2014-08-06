package com.starnet.snview.realplay;

import android.os.Parcel;
import android.os.Parcelable;

public class PreviewDeviceItem implements Parcelable {
	
	private String svrIp;              // 服务器IP
	private String svrPort;            // 服务器端口
	private String loginUserName;      // 登录用户名
	private String loginPassword;      // 登录密码
	private int channelNo; 	           // 当前预览通道
	
	
	public PreviewDeviceItem() {
		
	}
	
	private PreviewDeviceItem(Parcel in) {
		this.svrIp = in.readString();
		this.svrPort = in.readString();
		this.loginUserName = in.readString();
		this.loginPassword = in.readString();
		this.channelNo = in.readInt();
		
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
		out.writeString(svrIp);
		out.writeString(svrPort);
		out.writeString(loginUserName);
		out.writeString(loginPassword);
		out.writeInt(channelNo);
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
	
}

package com.video.hdview.syssetting;

import java.io.Serializable;
import java.util.List;

import com.video.hdview.devicemanager.DeviceItem;

public class CloudAccount implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5137007817929994645L;
	private String domain;    // 域名服务器
	private String port;      // 端口
	private String username;  // 用户名
	private String password;  // 密码
	private boolean isEnabled;// 是否启用
	private List<DeviceItem> deviceList; // 设备列表
	private boolean isExpanded;//设置用户是否展开
	private boolean isRotate;//设置圈圈是否转动
	
	public boolean isRotate() {
		return isRotate;
	}
	public void setRotate(boolean isRotate) {
		this.isRotate = isRotate;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public List<DeviceItem> getDeviceList() {
		return deviceList;
	}
	public void setDeviceList(List<DeviceItem> deviceList) {
		this.deviceList = deviceList;
	}
	public boolean isExpanded() {
		return isExpanded;
	}
	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}
	
//	//进行序列化；
//	@Override
//	public int describeContents() {
//		return 0;
//	}
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(domain);
//		dest.writeString(port);
//		dest.writeString(username);
//		dest.writeString(password);
//		
//		dest.writeSparse;
//		
//	}
//	
//	 public static final Parcelable.Creator<CloudAccount> CREATOR = new Creator<CloudAccount>() {
//
//		@Override
//		public CloudAccount createFromParcel(Parcel source) {
//			return null;
//		}
//
//		@Override
//		public CloudAccount[] newArray(int size) {
//			return null;
//		}
//	 };
	
}

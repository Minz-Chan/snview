package com.starnet.snview.syssetting;

import java.util.List;

import com.starnet.snview.devicemanager.DeviceItem;

public class CloudAccount {
	private String domain;    // 域名服务器
	private String port;      // 端口
	private String username;  // 用户名
	private String password;  // 密码
	private boolean isEnabled;// 是否启用
	private List<DeviceItem> deviceList; // 设备列表
	
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
	
	
}

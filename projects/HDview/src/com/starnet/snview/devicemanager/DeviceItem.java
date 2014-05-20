package com.starnet.snview.devicemanager;

import java.util.List;

import com.starnet.snview.channelmanager.Channel;

public class DeviceItem {
	private String deviceName;
	private int deviceType;
	private boolean isSecurityProtectionOpen = false;
	private boolean isExpanded = false;
	private List<Channel> channelList;
	
	
	public List<Channel> getChannelList() {
		return channelList;
	}
	public void setChannelList(List<Channel> channelList) {
		this.channelList = channelList;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public int getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	public boolean isSecurityProtectionOpen() {
		return isSecurityProtectionOpen;
	}
	public void setSecurityProtectionOpen(boolean isSecurityProtectionOpen) {
		this.isSecurityProtectionOpen = isSecurityProtectionOpen;
	}
	public boolean isExpanded() {
		return isExpanded;
	}
	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}
	
	
}

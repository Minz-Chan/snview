package com.starnet.hdview.devicemanager;

public class DeviceItem {
	private String deviceName;
	private int deviceType;
	private boolean isSecurityProtectionOpen = false;
	
	
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
	
}

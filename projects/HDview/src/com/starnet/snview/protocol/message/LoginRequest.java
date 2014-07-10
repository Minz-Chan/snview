package com.starnet.snview.protocol.message;

public class LoginRequest {
	private String userName;
	private String password;
	private int deviceId;
	private int flag;
	private int channel;
	private int[] reserve = new int[2];
	
	
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
	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public int[] getReserve() {
		return reserve;
	}
	public void setReserve(int[] reserve) {
		this.reserve = reserve;
	}
}

package com.starnet.snview.realplay;

public class PreviewDeviceItem {
	private String svrIp;              // 服务器IP
	private String svrPort;            // 服务器端口
	private String loginUserName;      // 登录用户名
	private String loginPassword;      // 登录密码
	private int channelNo; 	           // 当前预览通道
	
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
	
	
}

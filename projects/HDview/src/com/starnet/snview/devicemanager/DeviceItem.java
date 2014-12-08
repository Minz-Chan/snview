package com.starnet.snview.devicemanager;

import java.io.Serializable;
import java.util.List;

import com.starnet.snview.channelmanager.Channel;

public class DeviceItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 28304239979711868L;
	private String deviceName;     // 记录名
	private String svrIp;          // 服务器IP
	private String svrPort;        // 服务器端口
	private String loginUser;      // 登录用户名
	private String loginPass;      // 登录密码
	private int defaultChannel; // 默认通道
	private String channelSum;     // 通道总数	
	private int deviceType;
	private boolean isSecurityProtectionOpen = false;
	private boolean isExpanded = false; // 是否展开
	private List<Channel> channelList;  // 通道列表，包含通道相关信息
	
	private String platformUsername;	// 所属的星云账户用户名
	private boolean isIdentify;//是否进行验证
	
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
	public int getDefaultChannel() {
		return defaultChannel;
	}
	public void setDefaultChannel(int defaultChannel) {
		this.defaultChannel = defaultChannel;
	}
	public String getChannelSum() {
		return channelSum;
	}
	public void setChannelSum(String channelSum) {
		this.channelSum = channelSum;
	}
	public String getPlatformUsername() {
		return platformUsername;
	}
	public void setPlatformUsername(String platformUsername) {
		this.platformUsername = platformUsername;
	}
	public boolean isIdentify() {
		return isIdentify;
	}
	public void setIdentify(boolean isIdentify) {
		this.isIdentify = isIdentify;
	}
	
	
}

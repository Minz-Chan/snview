package com.starnet.snview.channelmanager.xml;

import java.io.Serializable;

/***
 * 
 * 平台设备
 * @author  	创建人                 肖远东
 * @date        创建日期           2013-03-18
 * @author      修改人                 肖远东
 * @date        修改日期           2013-03-18
 * @description 修改说明	             首次增加
 *
 */
public class DVRDevice implements Serializable{
	/***
	 * 
	 */
	private static final long serialVersionUID = -394689937589506763L;
	
	private String loginUsername;	// <du>登陆设备用户名</du>
	private String loginPassword;	// <dp>登陆设备密码</dp>
	private String loginMode;		// <dm>登陆设备模式(0.IP  1.域名)</dm>
	private String loginIP;			// <dip>设备IP[登陆设备模式 为IP的时候有效]</dip>
	private String loginDomain;		// <dd>设备域名[登陆设备模式 为域名的时候有效]</dd>
	private String loginPort;		// <dpn>登陆设备端口号</dpn>
	private String starChannel;		// <scn>开始通道号</scn>
	private String channelNumber;	//  <cn>通道个数</cn>
	private String warningInputNumber;		// <ain>报警输入个数</ain>
	private String warningOutputNumber;		// <aon>报警输出个数</aon>
	private String audioChannelNumber;		// <acn>音频通道个数</acn>
	private String IntercomNumber;			// <vn>语音对讲个数</vn>
	private String HDNumber;		// <dn>硬盘个数</dn>
	private String maxMobileDetectionNumber;	// <mdn>最大支持的移动侦测区域个数</mdn>
	private String maxOverlayAreaNumber;		// <can>最大支持的视频遮盖区域个数</can>
	private String productModel;		// <tp>产品型号</tp>
	private String manufacturer;		// <ma>厂家类型: 星网锐捷[枚举类型]</ma>
	private String serialNumber;		// <sn>设备序列号</sn>
	private String ethernetaddress;		// <dmc>设备网卡地址</dmc>
	private String versionNumber;		// <dv>设备版本号</dv>
	private String platformUsername;	// <wu>平台用户名</wu>
	private String platformPassword;	// <wp>密码（密文）</wp>
	private String WEBPort;			// <dwp>设备WEB端口</dwp>
	private String deviceName;		// <dna>(在线、离线)设备名称</dna>
	private String mobliePhonePort;		// <mp>手机端口号</mp>
	
	public String getLoginUsername() {
		return loginUsername;
	}
	public void setLoginUsername(String loginUsername) {
		this.loginUsername = loginUsername;
	}
	public String getLoginPassword() {
		return loginPassword;
	}
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	public String getLoginMode() {
		return loginMode;
	}
	public void setLoginMode(String loginMode) {
		this.loginMode = loginMode;
	}
	public String getLoginIP() {
		return loginIP;
	}
	public void setLoginIP(String loginIP) {
		this.loginIP = loginIP;
	}
	public String getLoginDomain() {
		return loginDomain;
	}
	public void setLoginDomain(String loginDomain) {
		this.loginDomain = loginDomain;
	}
	public String getLoginPort() {
		return loginPort;
	}
	public void setLoginPort(String loginPort) {
		this.loginPort = loginPort;
	}
	public String getStarChannel() {
		return starChannel;
	}
	public void setStarChannel(String starChannel) {
		this.starChannel = starChannel;
	}
	public String getChannelNumber() {
		return channelNumber;
	}
	public void setChannelNumber(String channelNumber) {
		this.channelNumber = channelNumber;
	}
	public String getWarningInputNumber() {
		return warningInputNumber;
	}
	public void setWarningInputNumber(String warningInputNumber) {
		this.warningInputNumber = warningInputNumber;
	}
	public String getWarningOutputNumber() {
		return warningOutputNumber;
	}
	public void setWarningOutputNumber(String warningOutputNumber) {
		this.warningOutputNumber = warningOutputNumber;
	}
	public String getAudioChannelNumber() {
		return audioChannelNumber;
	}
	public void setAudioChannelNumber(String audioChannelNumber) {
		this.audioChannelNumber = audioChannelNumber;
	}
	public String getIntercomNumber() {
		return IntercomNumber;
	}
	public void setIntercomNumber(String intercomNumber) {
		IntercomNumber = intercomNumber;
	}
	public String getHDNumber() {
		return HDNumber;
	}
	public void setHDNumber(String hDNumber) {
		HDNumber = hDNumber;
	}
	public String getMaxMobileDetectionNumber() {
		return maxMobileDetectionNumber;
	}
	public void setMaxMobileDetectionNumber(String maxMobileDetectionNumber) {
		this.maxMobileDetectionNumber = maxMobileDetectionNumber;
	}
	public String getMaxOverlayAreaNumber() {
		return maxOverlayAreaNumber;
	}
	public void setMaxOverlayAreaNumber(String maxOverlayAreaNumber) {
		this.maxOverlayAreaNumber = maxOverlayAreaNumber;
	}
	public String getProductModel() {
		return productModel;
	}
	public void setProductModel(String productModel) {
		this.productModel = productModel;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public String getEthernetaddress() {
		return ethernetaddress;
	}
	public void setEthernetaddress(String ethernetaddress) {
		this.ethernetaddress = ethernetaddress;
	}
	public String getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}
	public String getPlatformUsername() {
		return platformUsername;
	}
	public void setPlatformUsername(String platformUsername) {
		this.platformUsername = platformUsername;
	}
	public String getPlatformPassword() {
		return platformPassword;
	}
	public void setPlatformPassword(String platformPassword) {
		this.platformPassword = platformPassword;
	}
	public String getWEBPort() {
		return WEBPort;
	}
	public void setWEBPort(String wEBPort) {
		WEBPort = wEBPort;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getMobliePhonePort() {
		return mobliePhonePort;
	}
	public void setMobliePhonePort(String mobliePhonePort) {
		this.mobliePhonePort = mobliePhonePort;
	}
	public String getIsUPNP() {
		return isUPNP;
	}
	public void setIsUPNP(String isUPNP) {
		this.isUPNP = isUPNP;
	}

	@Override
	public String toString() {
		return deviceName;
	}

	/***
	 * <up>是否通过upnp上网(0.否，1.是)</up>
	 */
	private String isUPNP;
	
}

package com.starnet.snview.channelmanager.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

/**
 * 
 * @author zhaohongxu
 * @Date Jul 12, 2014
 * @ClassName CloudAccountUtil.java
 * @Description TODO
 * @Modifier zhaohongxu
 * @Modify date Jul 12, 2014
 * @Modify description 封装有关获取星云平台用户信息
 */
public class CloudAccountUtil {
	
	private CloudService cloudService ;
    private List<String>cloudAccountInfo ;
	
	//请求星云账号中设备平台的信息
	private String domain;//域名设置
	private String port;//端口号
	private String username;//用户名称
	private String password;//登陆密码
	private String deviceName;//设备名称

	public CloudAccountUtil(CloudService cloudService, String domain,String port, String username, String password, String deviceName) {
		super();
		this.cloudService = cloudService;
		this.domain = domain;
		this.port = port;
		this.username = username;
		this.password = password;
		this.deviceName = deviceName;
	}
	
	public CloudAccountUtil(CloudService cloudService,List<String> cloudAccountInfo) {
		super();
		this.cloudService = cloudService;
		this.cloudAccountInfo = cloudAccountInfo;
		this.domain = cloudAccountInfo.get(0);
		this.port = cloudAccountInfo.get(1);
		this.username = cloudAccountInfo.get(2);
		this.password = cloudAccountInfo.get(3);
		this.deviceName = cloudAccountInfo.get(4);
	}
	public CloudAccount getCloudAccountFromURL() throws IOException, DocumentException{
		CloudAccount cloudAccount = new CloudAccount();
		Document document = cloudService.SendURLPost(domain, port, username, password, deviceName);
		String requestState = cloudService.readXmlStatus(document);//判断是否请求成功
		if (requestState == null) {//请求成功
			List<DVRDevice> dvrDevices = cloudService.readXmlDVRDevices(document);//获取得到DVRDevice的信息	
			cloudAccount  = getCloudAccountFromDVRDevice(dvrDevices);	
		}else{//返回给用户请求失败的信息，不能进行接下来的操作			
			System.out.println("请求不成功！！！！");			
		}
		return cloudAccount;
	}

	private CloudAccount getCloudAccountFromDVRDevice(List<DVRDevice> dvrDevices) {
		CloudAccount cloudAccount = new CloudAccount();
		cloudAccount.setDomain(domain);
		cloudAccount.setPassword(password);
		cloudAccount.setPort(port);
		cloudAccount.setUsername(username);
		cloudAccount.setExpanded(false);//暂时设置
		cloudAccount.setEnabled(true);//暂时设置
		
		int dvrDeviceSize = dvrDevices.size();
		List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		for (int i = 0; i < dvrDeviceSize; i++) {
			DeviceItem deviceItem = new DeviceItem();
					
			DVRDevice dvrDevice =	dvrDevices.get(i);
			int deviceType=5;//====？？？？？？？？？对应着哪一个	
			
			String deviceName = dvrDevice.getDeviceName();
			String svrIp = dvrDevice.getLoginIP();// 服务器IP
			String svrPort = dvrDevice.getLoginPort();// 服务器端口
			String loginUser = dvrDevice.getLoginUsername();// 登录用户名
			String loginPass = dvrDevice.getLoginPassword();// 登录密码
			String defaultChannel = dvrDevice.getStarChannel();			
			//设置设备信息
			deviceItem.setDefaultChannel(defaultChannel);
			deviceItem.setDeviceName(deviceName);
			deviceItem.setSvrIp(svrIp);
			deviceItem.setSvrPort(svrPort);
			deviceItem.setLoginPass(loginPass);
			deviceItem.setLoginUser(loginUser);
			deviceItem.setSecurityProtectionOpen(false);
			deviceItem.setExpanded(false);
			deviceItem.setDeviceType(deviceType);			
			String channelSum = dvrDevice.getChannelNumber();//用于为设备添加通道列表而准备
			deviceItem.setChannelSum(channelSum);
			
			List<Channel> channelList = new ArrayList<Channel>();
			int channeNumber = Integer.valueOf(channelSum);
			for (int j = 0; j < channeNumber; j++) {
				Channel channel = new Channel();
				channel.setChannelName("通道"+(j+1));
				channel.setSelected(false);
				channel.setChannelNo((j+1));
				channelList.add(channel);
			}
			deviceItem.setChannelList(channelList);
			deviceList.add(deviceItem);
		}
		cloudAccount.setDeviceList(deviceList);
		return cloudAccount;
	}
}
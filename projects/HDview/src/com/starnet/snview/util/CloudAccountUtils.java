package com.starnet.snview.util;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

public class CloudAccountUtils {
	
	//根据用户信息从网络获取用户数据，构建用户的设备列表
	public CloudAccount getCloudAccountFromDVRDevice(List<DVRDevice> dvrDevices,String domain,String port,String username,String password) {
		
		CloudAccount cloudAccount = new CloudAccount();
		cloudAccount.setDomain(domain);
		cloudAccount.setPort(port);
		cloudAccount.setUsername(username);
		cloudAccount.setPassword(password);
		
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
			String svrPort = dvrDevice.getMobliePhonePort();// 服务器端口
			String loginUser = dvrDevice.getLoginUsername();// 登录用户名
			String loginPass = dvrDevice.getLoginPassword();// 登录密码
			String defaultChannel = dvrDevice.getStarChannel();			
			//设置设备信息
			deviceItem.setDefaultChannel(Integer.valueOf(defaultChannel));
			deviceItem.setDeviceName(deviceName);
			deviceItem.setSvrIp(svrIp);
			deviceItem.setSvrPort(svrPort);
			deviceItem.setLoginPass(loginPass);
			deviceItem.setLoginUser(loginUser);
			deviceItem.setSecurityProtectionOpen(true);
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
	
	//根据用户信息从网络获取用户数据，构建用户的设备列表
		public CloudAccount getCloudAccountFromDVRDevice(List<DVRDevice> dvrDevices) {
			
			CloudAccount cloudAccount = new CloudAccount();			
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
				String svrPort = dvrDevice.getMobliePhonePort();// 服务器端口
				String loginUser = dvrDevice.getLoginUsername();// 登录用户名
				String loginPass = dvrDevice.getLoginPassword();// 登录密码
				String defaultChannel = dvrDevice.getStarChannel();			
				//设置设备信息
				deviceItem.setDefaultChannel(Integer.valueOf(defaultChannel));
				deviceItem.setDeviceName(deviceName);
				deviceItem.setSvrIp(svrIp);
				deviceItem.setSvrPort(svrPort);
				deviceItem.setLoginPass(loginPass);
				deviceItem.setLoginUser(loginUser);
				deviceItem.setSecurityProtectionOpen(true);
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

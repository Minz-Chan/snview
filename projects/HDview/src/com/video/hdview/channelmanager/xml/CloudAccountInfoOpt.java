package com.video.hdview.channelmanager.xml;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.video.hdview.R;
import com.video.hdview.syssetting.CloudAccount;
import com.video.hdview.util.ReadWriteXmlUtils;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import com.video.hdview.channelmanager.Channel;
import com.video.hdview.devicemanager.DeviceItem;

/**
 *封装有关获取星云平台用户信息
 */
@SuppressLint("SdCardPath")
public class CloudAccountInfoOpt {
	
	public static final String filePathOfCloudAccount = "/data/data/com.video.hdview/star_cloudAccount.xml";
	
	//请求星云账号中设备平台的信息
	private String domain;
	private String port;
	private String username;
	private String password;
	private String deviceName;
	private Context context;

	public CloudAccountInfoOpt(Context context,String domain,String port, String username, String password, String deviceName) {
		super();
		this.domain = domain;
		this.port = port;
		this.username = username;
		this.password = password;
		this.deviceName = deviceName;
		this.context = context;
	}
	
	public CloudAccountInfoOpt(Context context) {
		this.context = context;
	}

	public CloudAccount getCloudAccountFromURL() throws IOException, DocumentException{
		CloudAccount cloudAccount = new CloudAccount();
		Document document = ReadWriteXmlUtils.SendURLPost(domain, port, username, password, deviceName);
		String requestState = ReadWriteXmlUtils.readXmlStatus(document);//判断是否请求成功
		if (requestState == null) {//请求成功
			List<DVRDevice> dvrDevices = ReadWriteXmlUtils.readXmlDVRDevices(document);//获取得到DVRDevice的信息	
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
			deviceItem.setDefaultChannel(Integer.valueOf(defaultChannel));
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
				channel.setChannelName(context.getString(R.string.device_manager_collect_device)+(j+1));
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

	
	/**
	 * 从XML文档中获取星云平台用户数据，数据内容主要包括：domain、port、username、 password、deviceName，其中星云账户的DeviceList
	 * 为空；
	 */
	public List<CloudAccount> getCloudAccountInfoFromUI(String username) {
		List<CloudAccount> accoutInfo = new ArrayList<CloudAccount>();
		try{
			CloudAccount collectAccount = new CloudAccount();
			//获取收藏设备，应该对收藏设备进行判断
			List<CloudAccount> cloudAccountList = ReadWriteXmlUtils.getCloudAccountList(filePathOfCloudAccount);
			collectAccount.setDeviceList(null);
			collectAccount.setEnabled(true);
			collectAccount.setExpanded(false);
			collectAccount.setRotate(true);
			collectAccount.setUsername(username);
			collectAccount.setDomain("bo.com");
			collectAccount.setPort("8080");
			collectAccount.setPassword("4a5");
			accoutInfo.add(collectAccount);
			
			int size = cloudAccountList.size();
			for(int i =0 ;i<size;i++){
				CloudAccount cloudAccount = cloudAccountList.get(i);
				if (cloudAccount.isEnabled()) {
					accoutInfo.add(cloudAccount);
				}				
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			Log.i("CloudAccountInfoOpt","Read Over!");
		}
		return accoutInfo;
	}
}

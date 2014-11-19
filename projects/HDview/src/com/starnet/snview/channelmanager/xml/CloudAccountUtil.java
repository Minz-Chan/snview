package com.starnet.snview.channelmanager.xml;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;

import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.CollectDeviceItemParams;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.devicemanager.DeviceItem;

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
@SuppressLint("SdCardPath")
public class CloudAccountUtil {
	
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";//收藏设备的存放地址；获取得打的数据放在ExpandableListView的第一个位置
	private CloudService cloudService ;
	private final String filePathOfCloudAccount = "/data/data/com.starnet.snview/star_cloudAccount.xml";
	
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
	
	public CloudAccountUtil() {
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
				channel.setChannelName(CollectDeviceItemParams.param+(j+1));
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
	 * 
	 * @author zhongxu
	 * @Date 2014年7月23日
	 * @Description 从用户保存界面获取拥护数据
	 * @return
	 */
	public List<CloudAccount> getCloudAccountInfoFromUI() {
		List<CloudAccount> accoutInfo = new ArrayList<CloudAccount>();
		CloudAccountXML caXML = new CloudAccountXML();
		try{
			CloudAccount collectDevice = new CloudAccount();
			List<DeviceItem> deviceItemList = caXML.getCollectDeviceListFromXML(filePath);//获取收藏设备，应该对收藏设备进行判断
			int devicelist_size = deviceItemList.size();
			
			List<CloudAccount> cloudAccountList = caXML.getCloudAccountList(filePathOfCloudAccount);
			
//			for (int i = 0; i < devicelist_size; i++) {
//				DeviceItem deviceItem = deviceItemList.get(i);
//				String accountUsername = deviceItem.getPlatformUsername();
//				int caSize = cloudAccountList.size();
				
//				for (int j = 0; j < caSize; j++) {//星云账户存在并禁用时，收藏设备列表中则不显示该设备
//					if (accountUsername.equals(cloudAccountList.get(j)
//							.getUsername())
//							&& !cloudAccountList.get(j).isEnabled()) {
//						deviceItemList.remove(deviceItem);
//						break;
//					}
//				}
				
//				boolean isExist = false;
//				for (int j = 0; j < caSize; j++) {//星云账户不存在时，收藏设备列表中则不显示该设备
//					if (accountUsername.equals(cloudAccountList.get(j).getUsername())) {
//						isExist = true;
//						break;
//					}
//					if (!isExist) {
//						deviceItemList.remove(deviceItem);
//					}
//				}
//			}			
			
			collectDevice.setDeviceList(deviceItemList);
			collectDevice.setEnabled(true);
			collectDevice.setExpanded(false);
			collectDevice.setRotate(true);
			collectDevice.setUsername(CollectDeviceItemParams.param);
			collectDevice.setDomain("com");
			collectDevice.setPort("808");
			collectDevice.setPassword("0208");
			accoutInfo.add(collectDevice);
			
			int size = cloudAccountList.size();
			for(int i =0 ;i<size;i++){
				CloudAccount cloudAccount = cloudAccountList.get(i);
				accoutInfo.add(cloudAccount);					
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			System.out.println("Read Over!");
		}
		return accoutInfo;
	}
}

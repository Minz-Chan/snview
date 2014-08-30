package com.starnet.snview.channelmanager.xml;

import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.syssetting.CloudAccount;

/**
 * 
 * @author zhongxu
 * @Date 2014年7月23日
 * @ClassName NetCloudAccountThread.java
 * @Description 该线程主要用于网络用户数据的访问，获取到数据之后，通知ExpandableListView中加载圈的消失与否
 * @Modifier zhongxu
 * @Modify date 2014年7月23日
 * @Modify description TODO
 */
@SuppressLint("SdCardPath")
public class NetCloudAccountThread extends Thread {
	
	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";
	
	private CloudAccount cAccount;//包含访问网络的信息
	private CloudService cloudService;//用于用户访问网络的接口
	
	private Handler netHandler;//主线程的handler
	private int postition;//代表组的号码
	
	private CloudAccountXML caXml ;
	@Override
	public void run() {//线程的执行方法
		super.run();	
		try {
			Message msg = new Message();
			caXml = new CloudAccountXML();
			//如果网络访问成功msg.what = 1；否则，msg.what = 0；
			String domain = cAccount.getDomain();
			String port = cAccount.getPort();
			String username = cAccount.getUsername();
			String password = cAccount.getPassword();
			String deviceName = "conn1";
			Document document = cloudService.SendURLPost(domain, port, username, password, deviceName);
			String requestStatus = cloudService.readXmlStatus(document);
			if (requestStatus == null) {//网络访问成功
				List<DVRDevice> dvrDevices = cloudService.readXmlDVRDevices(document);//获取到设备
				CloudAccount netCloudAccount = getCloudAccountFromDVRDevice(dvrDevices);//将获取的内容封装成CloudAccount
				//写操作的同步,是否有必要向文档中写入保存呢。。。？？？？
				caXml.writeNewCloudAccountToXML(netCloudAccount, CLOUD_ACCOUNT_PATH);//将数据写入xml文档中,将访问成功得到的数据，写入文档中，使得ExpandableListview在进行界面加载时，可以直接从文档中读取；
				Bundle data = new Bundle();
				data = encopeNetCloudAccountSuccess(data,netCloudAccount);//封装数据:将网络访问获取得到的数据打包
				data.putSerializable("netCloudAccount", netCloudAccount);
				msg.setData(data);//置为1，表示获取成功
				netHandler.sendMessage(msg);
			}else {//网络访问失败
				Bundle data = new Bundle();
				data = encopeNetCloudAccountFail(data,cAccount);//封装数据:将网络访问获取得到的数据打包			
				data.putSerializable("netCloudAccount", cAccount);
				msg.setData(data);
				netHandler.sendMessage(msg);
               }
           } catch (Exception e) {
			e.printStackTrace();
		} 	
	}

	private Bundle encopeNetCloudAccountFail(Bundle data, CloudAccount cAccount2) {
		data.putString("position", String.valueOf(postition));
		data.putString("success", "No");//获取失败,success置为No
		return data;
	}

	private Bundle encopeNetCloudAccountSuccess(Bundle data,CloudAccount netCloudAccount) {
		data.putString("position", String.valueOf(postition));
		data.putString("success", "Yes");//获取成功,success置为Yes
		return data;
	}

	private CloudAccount getCloudAccountFromDVRDevice(List<DVRDevice> dvrDevices) {
		CloudAccount cloudAccount = new CloudAccount();
		String domain = cAccount.getDomain();
		String port = cAccount.getPort();
		String username = cAccount.getUsername();
		String password = cAccount.getPassword();
		cloudAccount.setDomain(domain);
		cloudAccount.setPort(port);
		cloudAccount.setUsername(username);
		cloudAccount.setPassword(password);
		
		cloudAccount.setExpanded(false);//暂时设置
//		cloudAccount.setEnabled(true);//暂时设置
		
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
			if(channeNumber != 0){
				for (int j = 0; j < channeNumber; j++) {
					Channel channel = new Channel();
					channel.setChannelName("通道"+(j+1));
					channel.setSelected(false);
					channel.setChannelNo((j+1));
					channelList.add(channel);
				}
			}else {//通道为空的情况；人为的添加一个通道...
				
			}
			deviceItem.setChannelList(channelList);
			deviceList.add(deviceItem);
		}
		cloudAccount.setDeviceList(deviceList);
		return cloudAccount;
	}

	public NetCloudAccountThread(CloudAccount cAccount,CloudService cloudService,Handler netHandler,int postition) {
		super();
		this.cAccount = cAccount;
		this.cloudService = cloudService;
		this.netHandler = netHandler;
		this.postition = postition;
	}
}
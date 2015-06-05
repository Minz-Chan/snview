package com.video.hdview.channelmanager.xml;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.video.hdview.R;
import com.video.hdview.channelmanager.Channel;
import com.video.hdview.devicemanager.DeviceItem;
import com.video.hdview.syssetting.CloudAccount;
import com.video.hdview.util.ReadWriteXmlUtils;

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
	
	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.video.hdview/cloudAccount_list.xml";
	private int postition;
	private Handler netHandler;
	private CloudAccount cAccount;
	private final int STARCLOUNDDOWNLOADSUC = 10;
	private Context context;
	
	@Override
	public void run() {//线程的执行方法
		super.run();	
		try {
			Message msg = new Message();
			//如果网络访问成功msg.what = 1；否则，msg.what = 0；
			String domain = cAccount.getDomain();
			String port = cAccount.getPort();
			String username = cAccount.getUsername();
			String password = cAccount.getPassword();
			String deviceName = "conn1";
			Document document = ReadWriteXmlUtils.SendURLPost(domain, port, username, password, deviceName);
			String requestStatus = ReadWriteXmlUtils.readXmlStatus(document);
			if (requestStatus == null) {//网络访问成功
				List<DVRDevice> dvrDevices = ReadWriteXmlUtils.readXmlDVRDevices(document);//获取到设备
				CloudAccount netCloudAccount = getCloudAccountFromDVRDevice(dvrDevices);//将获取的内容封装成CloudAccount
				//写操作的同步,是否有必要向文档中写入保存呢
				ReadWriteXmlUtils.writeNewCloudAccountToXML(netCloudAccount, CLOUD_ACCOUNT_PATH);//将数据写入xml文档中,将访问成功得到的数据，写入文档中，使得ExpandableListview在进行界面加载时，可以直接从文档中读取；
				Bundle data = new Bundle();
				data = encopeNetCloudAccountSuccess(data,netCloudAccount);//封装数据:将网络访问获取得到的数据打包
				data.putSerializable("netCloudAccount", netCloudAccount);
				msg.setData(data);//置为1，表示获取成功
				msg.what = STARCLOUNDDOWNLOADSUC;
				netHandler.sendMessage(msg);
			}else {//网络访问失败
				Bundle data = new Bundle();
				data = encopeNetCloudAccountFail(data,cAccount);//封装数据:将网络访问获取得到的数据打包			
				data.putSerializable("netCloudAccount", cAccount);
				data.putString("visit_flag", "nosuc");
				msg.setData(data);
				msg.what = STARCLOUNDDOWNLOADSUC;
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
		int dvrDeviceSize = dvrDevices.size();
		List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		for (int i = 0; i < dvrDeviceSize; i++) {
			DeviceItem deviceItem = new DeviceItem();
					
			DVRDevice dvrDevice =	dvrDevices.get(i);
			int deviceType=5;
			
			String deviceName = dvrDevice.getDeviceName();
			String svrIp = dvrDevice.getLoginIP();
			String svrPort = dvrDevice.getMobliePhonePort();
			String loginUser = dvrDevice.getLoginUsername();
			String loginPass = dvrDevice.getLoginPassword();
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
			deviceItem.setPlatformUsername(username);
			deviceItem.setIdentify(true);
			List<Channel> channelList = new ArrayList<Channel>();
			int channeNumber = Integer.valueOf(channelSum);
			if(channeNumber != 0){
				for (int j = 0; j < channeNumber; j++) {
					Channel channel = new Channel();
					channel.setChannelName(context.getString(R.string.device_manager_collect_device)+(j+1));
					channel.setSelected(false);
					channel.setChannelNo((j+1));
					channelList.add(channel);
				}
			}
			deviceItem.setChannelList(channelList);
			deviceList.add(deviceItem);
		}
		cloudAccount.setDeviceList(deviceList);
		return cloudAccount;
	}

	public NetCloudAccountThread(Context context ,CloudAccount cAccount,Handler netHandler,int postition) {
		super();
		this.cAccount = cAccount;
		this.netHandler = netHandler;
		this.postition = postition;
		this.context = context;
	}
}
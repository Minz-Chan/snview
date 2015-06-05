package com.video.hdview.playback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.video.hdview.R;
import com.video.hdview.channelmanager.Channel;
import com.video.hdview.channelmanager.ChannelListActivity;
import com.video.hdview.channelmanager.xml.DVRDevice;
import com.video.hdview.devicemanager.DeviceItem;
import com.video.hdview.syssetting.CloudAccount;
import com.video.hdview.util.ReadWriteXmlUtils;


public class DeviceItemRequestTask {
	private int pos;
	protected Context ctx;
	private Handler mHandler;
	private Thread timeThread;
	private Thread workThread;
	private CloudAccount reqCA;
	private final int TIME = 77;
	private boolean threadOver;
	private boolean isCanceled;
	private boolean isDocumentOpt;
	private boolean isRequestTimeOut;
	private boolean isTimeThreadOver;
	private boolean isStartWorkRequest;
//	private final int TIMEOUT = 0x0002;
//	private final int LOADSUC = 0x0003;
//	private final int LOADFAI = 0x0004;

	public DeviceItemRequestTask(Context ctx,CloudAccount reqCA, Handler mHandler, final int pos) {
		this.pos = pos;
		this.ctx = ctx;
		this.reqCA = reqCA;
		this.mHandler = mHandler;
		workThread = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					if (pos == 0) {//第一个为收藏设备，直接发送到UI界面进行更新即可...
						try {
							sendMessageToUiFresh();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else {
						onStartWorkRequest();
					}
				} catch (IOException e) {
					onRequestTimeOut();
				} catch (DocumentException e) {
					onDocumentOpt();
				}
			}
		};
		timeThread = new Thread() {
			@Override
			public void run() {
				super.run();
				boolean isRun = false;
				int timeCount = 0;
				while (!isRun && !isTimeThreadOver && !isCanceled && !threadOver) {
					timeCount++;
					try {
						Thread.sleep(1000);
						if (timeCount == TIME) {
							isRun = true;
							onTimeOut();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}
	
	public void setThreadOver(boolean threadOver){
		this.threadOver = threadOver;
	}

	protected void sendMessageToUiFresh() throws Exception {
		List<DeviceItem> its = new ArrayList<DeviceItem>();
		List<DeviceItem> items = ReadWriteXmlUtils.getCollectDeviceListFromXML(ChannelListActivity.filePath);
		for (DeviceItem item : items) {
			if (item.isUsable()) {
				its.add(item);
			}
		}
		isDocumentOpt = true;
		isTimeThreadOver = true;
		isRequestTimeOut = true;
		isTimeThreadOver = true;
		isStartWorkRequest = true;
		reqCA.setDeviceList(its);
		Message msg = new Message();
		Bundle data = new Bundle();
		msg.what = TimeSettingActivity.LOAD_COLLECT_DATA_LOADSUC;
		data.putInt("position", pos);
		data.putString("success", "Yes");
		data.putSerializable("netCA", reqCA);
		msg.setData(data);
		mHandler.sendMessage(msg);
	}

	private void onDocumentOpt() {

		isTimeThreadOver = true;
		isRequestTimeOut = true;
		isTimeThreadOver = true;
		isStartWorkRequest = true;

		if (!isCanceled && !isDocumentOpt) {
			isDocumentOpt = true;
			Message msg = new Message();
			msg.what = TimeSettingActivity.LOAD_COLLECT_DATA_LOADFAI;
			Bundle data = new Bundle();
			data.putInt("position", pos);
			data.putSerializable("netCA", reqCA);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}

	private void onRequestTimeOut() {
		isDocumentOpt = true;
		isTimeThreadOver = true;
		isStartWorkRequest = true;
		if (!isCanceled && !isRequestTimeOut) {
			isDocumentOpt = true;
			Message msg = new Message();
			msg.what = TimeSettingActivity.LOAD_COLLECT_DATA_LOADFAI;
			Bundle data = new Bundle();
			data.putInt("position", pos);
			data.putSerializable("netCA", reqCA);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}

	private void onStartWorkRequest() throws IOException, DocumentException {
		String domain = reqCA.getDomain();
		String port = reqCA.getPort();
		String username = reqCA.getUsername();
		String password = reqCA.getPassword();
		Document doc = ReadWriteXmlUtils.SendURLPost(domain, port, username,password, "");
		String result = ReadWriteXmlUtils.readXmlStatus(doc);
		if (result == null) {
			ArrayList<DVRDevice> dList = (ArrayList<DVRDevice>) ReadWriteXmlUtils.readXmlDVRDevices(doc);
			if (!isCanceled && !isStartWorkRequest) {
				Message msg = new Message();
				Bundle data = new Bundle();
				CloudAccount netAct = getCloudAccountFromDVRDevice(dList);
				isDocumentOpt = true;
				isTimeThreadOver = true;
				isRequestTimeOut = true;
				isTimeThreadOver = true;
				isStartWorkRequest = true;
				msg.what = TimeSettingActivity.LOAD_COLLECT_DATA_LOADSUC;
				data.putInt("position", pos);
				data.putString("success", "Yes");
				data.putSerializable("netCA", netAct);
				msg.setData(data);
				if (!threadOver) {
					mHandler.sendMessage(msg);
				}
			}
		} else {
			if (!isCanceled && !isStartWorkRequest) {
				isDocumentOpt = true;
				isTimeThreadOver = true;
				isRequestTimeOut = true;
				isTimeThreadOver = true;
				isStartWorkRequest = true;
				Message msg = new Message();
				msg.what = TimeSettingActivity.LOAD_COLLECT_DATA_LOADFAI;
				Bundle data = new Bundle();
				data.putInt("position", pos);
				data.putSerializable("netCA", reqCA);
				msg.setData(data);
				if (!threadOver) {
					mHandler.sendMessage(msg);
				}
			}
		}
	}

	private void onTimeOut() {
		isDocumentOpt = true;
		isTimeThreadOver = true;
		isRequestTimeOut = true;
		isTimeThreadOver = true;
		isStartWorkRequest = true;
		if (!isCanceled && !threadOver) {
			Message msg = new Message();
			msg.what = TimeSettingActivity.LOAD_COLLECT_DATA_TIMEOUT;
			Bundle data = new Bundle();
			data.putInt("position", pos);
			data.putSerializable("netCA", reqCA);
			msg.setData(data);
			if (!threadOver) {
				mHandler.sendMessage(msg);
			}
		}
	}

	public void start() {
		threadOver = false;
		isCanceled = false;
		isDocumentOpt = false;
		isRequestTimeOut = false;
		isTimeThreadOver = false;
		isStartWorkRequest = false;
		timeThread.start();
		workThread.start();
	}

	public void setCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public CloudAccount getCloudAccountFromDVRDevice(List<DVRDevice> dvrDevices) {
		CloudAccount cloudAccount = new CloudAccount();
		String domain = reqCA.getDomain();
		String port = reqCA.getPort();
		String username = reqCA.getUsername();
		String password = reqCA.getPassword();
		cloudAccount.setDomain(domain);
		cloudAccount.setPort(port);
		cloudAccount.setUsername(username);
		cloudAccount.setPassword(password);
		cloudAccount.setExpanded(false);// 暂时设置
		cloudAccount.setEnabled(true);
		int dvrDeviceSize = dvrDevices.size();
		List<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		for (int i = 0; i < dvrDeviceSize; i++) {
			DeviceItem deviceItem = new DeviceItem();

			DVRDevice dvrDevice = dvrDevices.get(i);
			int deviceType = 5;

			String deviceName = dvrDevice.getDeviceName();
			String svrIp = dvrDevice.getLoginIP();
			String svrPort = dvrDevice.getMobliePhonePort();
			String loginUser = dvrDevice.getLoginUsername();
			String loginPass = dvrDevice.getLoginPassword();
			String defaultChannel = dvrDevice.getStarChannel();
			// 设置设备信息
			deviceItem.setDefaultChannel(Integer.valueOf(defaultChannel));
			deviceItem.setDeviceName(deviceName);
			deviceItem.setSvrIp(svrIp);
			deviceItem.setSvrPort(svrPort);
			deviceItem.setLoginPass(loginPass);
			deviceItem.setLoginUser(loginUser);
			deviceItem.setSecurityProtectionOpen(true);
			deviceItem.setExpanded(false);
			deviceItem.setDeviceType(deviceType);
			String channelSum = dvrDevice.getChannelNumber();// 用于为设备添加通道列表而准备
			deviceItem.setChannelSum(channelSum);
			deviceItem.setPlatformUsername(username);
			deviceItem.setIdentify(true);
			List<Channel> channelList = new ArrayList<Channel>();
			int channeNumber = Integer.valueOf(channelSum);
			if (channeNumber != 0) {
				for (int j = 0; j < channeNumber; j++) {
					Channel channel = new Channel();
					channel.setChannelName(ctx.getString(R.string.device_manager_collect_device) + (j + 1));
					channel.setSelected(false);
					channel.setChannelNo((j + 1));
					channelList.add(channel);
				}
			}
			deviceItem.setChannelList(channelList);
			deviceList.add(deviceItem);
		}
		cloudAccount.setDeviceList(deviceList);
		return cloudAccount;
	}
}

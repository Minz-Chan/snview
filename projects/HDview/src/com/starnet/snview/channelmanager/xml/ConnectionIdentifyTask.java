package com.starnet.snview.channelmanager.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.component.BufferSendManager;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.protocol.message.LoginRequest;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.PhoneInfoRequest;
import com.starnet.snview.protocol.message.VersionInfoRequest;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.ReadWriteXmlUtils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ConnectionIdentifyTask {
	// private final String TAG = "ConnectionIdentifyTask";
	private int childPos;
	private int parentPos;
	private Socket client;
	private Context context;
	private Handler mHandler;
	private boolean isCanceled;
	private Thread timeOutThread;
	private final int timeOut = 7;
	private DeviceItem mDeviceItem;
	private Thread connectionThread;
	private BufferSendManager sender;
	private boolean isOnConnectionWrong;
	private CloudAccount clickCloudAccount;
	private final int defaultChannelNum = 1;
	private boolean isConnectedOver = false;
	private boolean shouldTimeOutOver = false;
	private final int CONNECTIFYIDENTIFY_WRONG = 0x0012;
	private final int CONNECTIFYIDENTIFY_SUCCESS = 0x0011;
	private final int CONNECTIFYIDENTIFY_TIMEOUT = 0x0013;
	private boolean selectAll = false;
	
//	private boolean clickOk;
	private List<DeviceItem> dItems ;

	public ConnectionIdentifyTask(Handler handler,CloudAccount clickCloudAccount, DeviceItem dItem, int parentPos,
			int childPos,boolean selectAll) {
		this.mHandler = handler;
		this.mDeviceItem = dItem;
		this.childPos = childPos;
		this.parentPos = parentPos;
		this.selectAll = selectAll;
		this.clickCloudAccount = clickCloudAccount;
		initialThread();
	}

	private void initialThread() {
//		clickOk = false;
		isOnWorkdIOErr = false;
		isConnectedOver = false;
		shouldTimeOutOver = false;
		isOnWorkdUnknwnHost = false;
		isOnConnectionWrong = false;

		timeOutThread = new Thread() {
			@Override
			public void run() {
				
				int timeCount = 0;
				boolean canRun = true;
				while (canRun && !shouldTimeOutOver && !isCanceled) {
					try {
						Thread.sleep(1000);
						timeCount++;
						if (timeCount == timeOut) {
							canRun = false;
							if (!isCanceled) {// && !shouldTimeOutOver
								exit();
								onTimeOut();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		connectionThread = new Thread() {// Preview channel connection thread.
			@Override
			public void run() {
				try {
					dItems = ReadWriteXmlUtils.getCollectDeviceListFromXML(ChannelListActivity.filePath);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!isCanceled) {// user did not click back.
					boolean isConnected = initialClientSocket();
					if (isConnected) {
						startConnectionIdentify();
					} else {// 连接不正常的处理，有时候比较耗时
						isOnWorkdUnknwnHost = true;
						isConnectedOver = true;
						isOnWorkdIOErr = true;
						if (!isCanceled && !shouldTimeOutOver && !isConnectedOver && !isOnConnectionWrong) {
							exit();
							onConnectionWrong();
						}
						shouldTimeOutOver = true;
					}
				}
			}
		};
	}

	/*** 网络链接不成功的处理操作 ***/
	private void onConnectionWrong() {
		if (!shouldTimeOutOver && !isCanceled && !isOnConnectionWrong) {
			isOnWorkdUnknwnHost = true;
			shouldTimeOutOver = true;
			isConnectedOver = true;
			isOnWorkdIOErr = true;
			Message msg = new Message();
			Bundle data = new Bundle();
			setWrongDevicetItem(1);
			setBundleData(data);
			msg.setData(data);
			msg.what = CONNECTIFYIDENTIFY_WRONG;
			if (!isCanceled && !isOnConnectionWrong && shouldTimeOutOver) {
				mHandler.sendMessage(msg);
			}
		}
	}

	/*** 初始化客户端Socket ***/
	private boolean initialClientSocket() {
		boolean isConnected = false;
		try {
			String host = mDeviceItem.getSvrIp();
			int port = Integer.valueOf(mDeviceItem.getSvrPort());
			client = new Socket(host, port);
			isConnected = client.isConnected();
		} catch (UnknownHostException e) {
			isConnected = false;
			if (!isCanceled) {
				onWorkdUnknwnHost();
			}
		} catch (IOException e) {
			isConnected = false;
			if (!isCanceled) {
				onWorkdIOErr();
			}
		}
		return isConnected;
	}

	/** 端口错误的操作 ***/
	protected void onWorkdUnknwnHost() {
		if (!shouldTimeOutOver && !isCanceled && !isOnWorkdUnknwnHost) {
			shouldTimeOutOver = true;
			isConnectedOver = true;
			Message msg = new Message();
			Bundle data = new Bundle();
			setWrongDevicetItem(1);
			setBundleData(data);
			msg.setData(data);
			msg.what = CONNECTIFYIDENTIFY_WRONG;
			if (!isCanceled && !isOnWorkdUnknwnHost) {// && !clickOk
				mHandler.sendMessage(msg);
			}
		}
	}

	private boolean isOnWorkdIOErr;
	private boolean isOnWorkdUnknwnHost;

	/** ??错误的操作 ***/
	protected void onWorkdIOErr() {
		if (!shouldTimeOutOver && !isCanceled && !isOnWorkdIOErr) {
			
			shouldTimeOutOver = true;
			isConnectedOver = true;
			Message msg = new Message();
			Bundle data = new Bundle();
			setWrongDevicetItem(1);
			setBundleData(data);
			msg.setData(data);
			msg.what = CONNECTIFYIDENTIFY_WRONG;
			if (!isCanceled && !isOnWorkdIOErr) {// && !clickOk 
				mHandler.sendMessage(msg);
			}
		}
	}

	/** 开始网络连接验证 **/
	protected void startConnectionIdentify() {
		try {
			if (!isCanceled) {
				sendConnectionIdentifyRequest();
			}
		} catch (IOException e) {
			return;
		}
		try {
			if (!isCanceled) {
				getConnectionIdentifyInfo();
			}
		} catch (IOException e) {
			return;
		}
	}

	/** 获取网络连接验证的信息 */
	@SuppressWarnings("static-access")
	private void getConnectionIdentifyInfo() throws IOException {

		Bundle data = new Bundle();
		Message msg = new Message();
		InputStream in = client.getInputStream();
		byte[] head = new byte[8];
		in.read(head);
		ByteBuffer headBuffer = ByteBuffer.allocate(8).wrap(head);
		headBuffer.order(ByteOrder.BIG_ENDIAN);
		int len = headBuffer.getInt();
		if (len == 140) {// 连接成功
			if (!isCanceled) {
				shouldTimeOutOver = true;
				msg.what = CONNECTIFYIDENTIFY_SUCCESS;
				byte[] recvData = new byte[len - 4];
				in.read(recvData);
				int channelNumber = recvData[80];
				setDevicetItem(channelNumber);
				setBundleData(data);
				msg.setData(data);
				mHandler.sendMessage(msg);
				exit();
			}
		} else if (len == 20) {
			if (!isCanceled) {
				shouldTimeOutOver = true;
				msg.what = CONNECTIFYIDENTIFY_WRONG;
				setDevicetItem(defaultChannelNum);
				setBundleData(data);
				msg.setData(data);
				mHandler.sendMessage(msg);
				try {
					exit();
					int index = getIndexFromDeviceItem();
					if (needChange(index)) {
						ReadWriteXmlUtils.replaceSpecifyDeviceItem(ChannelListActivity.filePath, index, mDeviceItem);	
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			if (!isCanceled) {
				shouldTimeOutOver = true;
				msg.what = CONNECTIFYIDENTIFY_WRONG;
				setDevicetItem(defaultChannelNum);
				setBundleData(data);
				msg.setData(data);
				mHandler.sendMessage(msg);
				try {
					exit();
					int index = getIndexFromDeviceItem();
					if (needChange(index)) {
						ReadWriteXmlUtils.replaceSpecifyDeviceItem(ChannelListActivity.filePath, index, mDeviceItem);	
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private int getIndexFromDeviceItem() {
		int index = 0 ;
		if (dItems != null && dItems.size() > 0) {
			for (int i = 0; i < dItems.size(); i++) {
				if (mDeviceItem.getDeviceName().equals(dItems.get(i).getDeviceName())) {
					index = i;
					break;
				}
			}
		}		
		return index;
	}

	/** 设置验证后的设备 ***/
	private void setWrongDevicetItem(int channelNumber) {
		String chanelName = context.getString(R.string.device_manager_channel);
		List<Channel> channelList = new ArrayList<Channel>();
		mDeviceItem.setChannelSum(String.valueOf(channelNumber));
		for (int i = 0; i < channelNumber; i++) {
			Channel channel = new Channel();
			channel.setChannelName(chanelName + "" + (i + 1));
			channel.setChannelNo((i + 1));
			channel.setSelected(false);
			channelList.add(channel);
		}
		mDeviceItem.setChannelList(channelList);
		mDeviceItem.setIdentify(true);
		mDeviceItem.setConnPass(false);
		try {
			int index = getIndexFromDeviceItem();
			if (needChange(index)) {
				ReadWriteXmlUtils.replaceSpecifyDeviceItem(ChannelListActivity.filePath, index, mDeviceItem);	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 设置验证后的设备 ***/
	private void setDevicetItem(int channelNumber) {
		String chanelName = context.getString(R.string.device_manager_channel);
		List<Channel> channelList = new ArrayList<Channel>();
		mDeviceItem.setChannelSum(String.valueOf(channelNumber));
		for (int i = 0; i < channelNumber; i++) {
			Channel channel = new Channel();
			channel.setChannelName(chanelName + (i + 1));
			channel.setChannelNo((i+1));
			channel.setSelected(false);
			channelList.add(channel);
		}
		mDeviceItem.setChannelList(channelList);
		mDeviceItem.setIdentify(true);
		mDeviceItem.setConnPass(true);
		try {
			int index = getIndexFromDeviceItem();
			if (needChange(index)) {
				ReadWriteXmlUtils.replaceSpecifyDeviceItem(ChannelListActivity.filePath, index, mDeviceItem);	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 发送网络连接验证请求 **/
	private void sendConnectionIdentifyRequest() throws IOException {
		sender = BufferSendManager.getInstance();
		sender.setOutStream(client.getOutputStream());
		// 发送请求
		VersionInfoRequest v = new VersionInfoRequest();
		v.setVersionMajor(3);
		v.setVersionMinor(8);

		PhoneInfoRequest p = new PhoneInfoRequest();
		p.setEquipmentIdentity("");
		p.setEquipmentOS("Android");

		LoginRequest l = new LoginRequest();
		l.setUserName(mDeviceItem.getLoginUser());
		l.setPassword(mDeviceItem.getLoginPass());
		l.setDeviceId(1);
		l.setFlag(1);
		l.setChannel(1);
		l.setReserve(new int[] { 0, 0 });

		sender.write(new OwspBegin());
		sender.write(v);
		sender.write(p);
		sender.write(l);
		sender.write(new OwspEnd());
	}

	public void start() {
		if (!isCanceled) {
			timeOutThread.start();
			connectionThread.start();
		}
	}

	private void onTimeOut() {
		isOnWorkdIOErr = true;
		isConnectedOver = true;
		isOnConnectionWrong = true;
		isOnWorkdUnknwnHost = true;
		if (client != null) {
			client = null;
		}
		if (!isCanceled && !shouldTimeOutOver) {
			Message msg = new Message();
			Bundle data = new Bundle();
			mDeviceItem.setIdentify(true);
			mDeviceItem.setConnPass(false);
			setBundleData(data);
			msg.setData(data);
			msg.what = CONNECTIFYIDENTIFY_TIMEOUT;
			if (!shouldTimeOutOver) {
				mHandler.sendMessage(msg);
			}
			try {
				int index = getIndexFromDeviceItem();
				if (needChange(index)) {
					ReadWriteXmlUtils.replaceSpecifyDeviceItem(ChannelListActivity.filePath, index, mDeviceItem);	
				}				
				exit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		shouldTimeOutOver = true;
	}
	
	private boolean needChange(int index){
		boolean result = false;
		if(mDeviceItem.isConnPass() == dItems.get(index).isConnPass()){
			result = true;
		}
		return result;
	}

	private void setBundleData(Bundle data) {
		data.putSerializable("identifyDeviceItem", mDeviceItem);
		data.putBoolean("selectAll", selectAll);
		data.putInt("childPos", childPos);
		data.putInt("parentPos", parentPos);
		data.putString("deviceName", mDeviceItem.getDeviceName());
		data.putSerializable("clickCloudAccount", clickCloudAccount);
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public void setCancel(boolean isCanceled){
		this.isCanceled = isCanceled;
	}

	public void setCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
		isOnWorkdUnknwnHost = true;
		shouldTimeOutOver = true;
		isConnectedOver = true;
		isOnWorkdIOErr = true;
		exit();
	}
	
//	public void setClickOk(boolean clickOk){
//		this.clickOk = clickOk;
//	}
	
	private void exit(){
		if ( (client != null) && client.isConnected()) {
			try {
				client.close();
				client = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
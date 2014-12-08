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
import com.starnet.snview.component.BufferSendManager;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.protocol.message.LoginRequest;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.PhoneInfoRequest;
import com.starnet.snview.protocol.message.VersionInfoRequest;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ConnectionIdentifyTask {
//	private final String TAG = "ConnectionIdentifyTask";
	private Socket client;
	private Context context;
	private Handler mHandler;
	private boolean isCanceled;
	private Thread timeOutThread;
	private final int timeOut = 7;
	private DeviceItem mDeviceItem;
	private Thread connectionThread;
	private BufferSendManager sender;
	private final int defaultChannelNum = 1;
	private boolean shouldTimeOutOver = false;
	private final int CONNECTIFYIDENTIFY_WRONG = 0x0012;
	private final int CONNECTIFYIDENTIFY_SUCCESS = 0x0011;
	private final int CONNECTIFYIDENTIFY_TIMEOUT = 0x0013;

	public ConnectionIdentifyTask(Handler handler, DeviceItem deviceItem) {

		isCanceled = false;
		this.mHandler = handler;
		shouldTimeOutOver = false;
		this.mDeviceItem = deviceItem;
		
		timeOutThread = new Thread() {
			@Override
			public void run() {
				super.run();
				int timeCount = 0;
				boolean canRun = true;
				while (canRun && !shouldTimeOutOver && !isCanceled) {
					try {
						Thread.sleep(1000);
						timeCount++;
						if (timeCount == timeOut) {
							canRun = false;
							onTimeOut();// time out thread event.
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
				super.run();
				if (!isCanceled) {// user did not click back.
					boolean isConnected = initialClientSocket();
					if (isConnected) {
						startConnectionIdentify();
					} else {
						onConnectionWrong();
					}
				}
			}
		};
	}

	/*** 网络链接不成功的处理操作 ***/
	private void onConnectionWrong() {
		if (!shouldTimeOutOver) {
			shouldTimeOutOver = true;
			Message msg = new Message();
			msg.what = CONNECTIFYIDENTIFY_WRONG;
			mHandler.sendMessage(msg);
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

	/*** 端口错误的操作 ***/
	protected void onWorkdUnknwnHost() {
		if (!shouldTimeOutOver&&!isCanceled) {
			shouldTimeOutOver = true;
			Message msg = new Message();
			msg.what = CONNECTIFYIDENTIFY_WRONG;
			mHandler.sendMessage(msg);
		}
	}

	/*** ??错误的操作 ***/
	protected void onWorkdIOErr() {
		if (!shouldTimeOutOver&&!isCanceled) {
			shouldTimeOutOver = true;
			Message msg = new Message();
			msg.what = CONNECTIFYIDENTIFY_WRONG;
			mHandler.sendMessage(msg);
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

	/**
	 * 获取网络连接验证的信息
	 * 
	 * @throws IOException
	 **/
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
				data.putSerializable("identifyDeviceItem", mDeviceItem);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}
		} else if (len == 20) {
			if (!isCanceled) {
				shouldTimeOutOver = true;
				msg.what = CONNECTIFYIDENTIFY_WRONG;
				setDevicetItem(defaultChannelNum);
				data.putSerializable("identifyDeviceItem", mDeviceItem);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}
		} else {
			if (!isCanceled) {
				shouldTimeOutOver = true;
				msg.what = CONNECTIFYIDENTIFY_WRONG;
				setDevicetItem(defaultChannelNum);
				data.putSerializable("identifyDeviceItem", mDeviceItem);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}
		}
	}
	
	/**设置验证后的设备***/
	private void setDevicetItem(int channelNumber){
		String chanelName = context.getString(R.string.device_manager_channel);
		List<Channel>channelList = new ArrayList<Channel>();
		mDeviceItem.setChannelSum(String.valueOf(channelNumber));
		for (int i = 0; i < channelNumber; i++) {
			Channel channel = new Channel();
			channel.setChannelName(chanelName+""+(i+1));
			channel.setChannelNo((i+1));
			channel.setSelected(false);
			channelList.add(channel);
		}
		mDeviceItem.setChannelList(channelList);
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
		l.setUserName(mDeviceItem.getLoginUser());// "admin"
		l.setPassword(mDeviceItem.getLoginPass());// "25676166"
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
		shouldTimeOutOver = true;
		if (client != null) {
			client = null;
		}
		if (!isCanceled) {
			Message msg = new Message();
			msg.what = CONNECTIFYIDENTIFY_TIMEOUT;
			mHandler.sendMessage(msg);
		}
	}
	
	public void setContext(Context context){
		this.context = context;
	}
	
	public void setCanceled(boolean isCanceled){
		this.isCanceled = isCanceled;
	}
}
package com.starnet.snview.channelmanager.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

/**
 * 用于全选时的通道验证
 * 
 */
public class MultiConnIdentifyTask {
	
	private final String TAG = "MultiConnIdentifyTask";

	private int pos;
	private Socket client;
	private Context context;
	private DeviceItem mItem;
	private Handler mHandler;
	private CloudAccount account;
	private BufferSendManager sender;

	private Thread timeThread;
	private Thread workThread;

	/* 收藏设备列表* */
	private List<DeviceItem> mItems;

	private final int defaultChannelNum = 1;
	private final int TIMECOUNT = 7;

	private boolean connectionLongOver;
	private boolean timeThreadOver;
	private boolean isCanceled;

	public static final int MULTICONNIDENTIFYSUCCESS = 0x0021; // 验证成功
	public static final int MULTICONNIDENTIFYFAIL = 0x0022; // 验证失败
	public static final int MULTICONNIDENTIFYTIMEOUT = 0x0023; // 验证超时
	public static final int MULTICONNIDENTIFYHOSTORPORT = 0x0024; // 验证的域名或者端口错误
	public static final int MULTICONNIDENTIFYLONGTIMEOUT = 0x0025; // 建立socket连接超时
	public static final int MULTICONNIDENTIFYSENDERROR = 0x0026; // 登陆信息发送异常

	public MultiConnIdentifyTask(Context ctx, Handler handler,
			CloudAccount account, DeviceItem item, int pos) {
		this.mHandler = handler;
		this.account = account;
		this.context = ctx;
		this.mItem = item;
		this.pos = pos;
		initThreads();
	}

	private void initThreads() {
		timeThread = new Thread() {
			@Override
			public void run() {
				boolean canRun = true;
				int count = 0;
				while (canRun && !timeThreadOver && !isCanceled) {
					try {
						Thread.sleep(1000);
						count++;
						if (count == TIMECOUNT) {
							canRun = false;
							onTimeOut();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
						canRun = false;
					}
				}
			}
		};
		workThread = new Thread() {
			@Override
			public void run() {
				try {
					mItems = ReadWriteXmlUtils
							.getCollectDeviceListFromXML(ChannelListActivity.filePath);
					startConnIdentifyWork();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	protected void startConnIdentifyWork() {
		boolean isConnected = initialClientSocket();
		if (isConnected) {
			try {
				if (!isCanceled) {
					sendConnectionIdentifyRequest();
				}
			} catch (IOException e) {
				e.printStackTrace();
				if (!isCanceled) {
					onSendErrorWork();
				}
			}
			try {
				if (!isCanceled) {
					getConnectionIdentifyInfo();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void getConnectionIdentifyInfo() throws IOException {
		Message msg = new Message();
		Bundle data = new Bundle();
		InputStream in = client.getInputStream();
		byte[] head = new byte[8];
		in.read(head);
		@SuppressWarnings("static-access")
		ByteBuffer headBuffer = ByteBuffer.allocate(8).wrap(head);
		headBuffer.order(ByteOrder.BIG_ENDIAN);
		int len = headBuffer.getInt();
		if (len == 140) {// 连接成功
			timeThreadOver = true;
			Log.i(TAG, "len == 140");
			if (!isCanceled) {
				msg.what = MULTICONNIDENTIFYSUCCESS;
				byte[] recvData = new byte[len - 4];
				in.read(recvData);
				int channelNumber = recvData[80];
				setDevicetItem(channelNumber, true);
				setBundleData(data);
				msg.setData(data);
				mHandler.sendMessage(msg);
				closeSocket();
				replaceItem();
			}
		} else if (len == 20) {
			Log.i(TAG, "len == 20");
			timeThreadOver = true;
			if (!isCanceled) {
				connIdentifyFail(msg, data);
				closeSocket();
				replaceItem();
			}
		} else {
			timeThreadOver = true;
			Log.i(TAG, "other....len:"+len);
			if (!isCanceled) {
				connIdentifyFail(msg, data);
				closeSocket();
				replaceItem();
			}
		}
	}

	private void connIdentifyFail(Message msg, Bundle data) {
		Log.i(TAG, "connIdentifyFail");
		msg.what = MULTICONNIDENTIFYFAIL;
		setDevicetItem(defaultChannelNum, false);
		setBundleData(data);
		msg.setData(data);
		mHandler.sendMessage(msg);
		closeSocket();
	}

	private void setBundleData(Bundle data) {
		data.putSerializable("deviceItem", mItem);
		data.putSerializable("account", account);
		data.putInt("position", pos);
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
		l.setUserName(mItem.getLoginUser());
		l.setPassword(mItem.getLoginPass());
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

	/*** 初始化客户端Socket ***/
	private boolean initialClientSocket() {
		boolean isConnected = false;
		String host = mItem.getSvrIp();
		int port = Integer.valueOf(mItem.getSvrPort());
		try {
			client = new Socket(host, port);
			isConnected = client.isConnected();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			if (!isCanceled) {
				onUnknownHostWork();
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (!connectionLongOver && !isCanceled) {
				onLongTimeConnctWork();
			}
		}
		return isConnected;
	}

	/** 设置验证后的设备 ***/
	private void setDevicetItem(int channelNumber, boolean connPass) {
		String chanelName = context.getString(R.string.device_manager_channel);
		List<Channel> channelList = new ArrayList<Channel>();
		mItem.setChannelSum(String.valueOf(channelNumber));
		for (int i = 0; i < channelNumber; i++) {
			Channel channel = new Channel();
			channel.setChannelName(chanelName + (i + 1));
			channel.setChannelNo((i + 1));
			channel.setSelected(false);
			channelList.add(channel);
		}
		mItem.setChannelList(channelList);
		mItem.setIdentify(true);
		mItem.setConnPass(connPass);
	}

	private void replaceItem() {
		try {
			int index = getIndexFromDeviceItem();
			if (needChange(index)) {
				ReadWriteXmlUtils.replaceSpecifyDeviceItem(
						ChannelListActivity.filePath, index, mItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean needChange(int index) {
		boolean result = false;
		if (mItems == null) {
			return result;
		}
		if (mItem.isConnPass() != mItems.get(index).isConnPass()) {
			result = true;
		}
		return result;
	}

	private int getIndexFromDeviceItem() {
		int index = 0;
		if (mItems != null && mItems.size() > 0) {
			for (int i = 0; i < mItems.size(); i++) {
				if (mItem.getDeviceName().equals(mItems.get(i).getDeviceName())) {
					index = i;
					break;
				}
			}
		}
		return index;
	}

	private void onSendErrorWork() {
		Log.i(TAG, "onSendErrorWork");
		timeThreadOver = true;
		Message msg = new Message();
		Bundle data = new Bundle();
		msg.what = MULTICONNIDENTIFYSENDERROR;
		setDevicetItem(defaultChannelNum, false);
		setBundleData(data);
		msg.setData(data);
		mHandler.sendMessage(msg);
		closeSocket();
	}

	private void onLongTimeConnctWork() {// 尚未建立起连接
		Log.i(TAG, "onLongTimeConnctWork");
		timeThreadOver = true;
		Message msg = new Message();
		Bundle data = new Bundle();
		msg.what = MULTICONNIDENTIFYLONGTIMEOUT;
		setDevicetItem(defaultChannelNum, false);
		setBundleData(data);
		msg.setData(data);
		mHandler.sendMessage(msg);
	}

	private void onUnknownHostWork() {// 尚未建立起连接
		Log.i(TAG, "onUnknownHostWork");
		timeThreadOver = true;
		Message msg = new Message();
		Bundle data = new Bundle();
		msg.what = MULTICONNIDENTIFYHOSTORPORT;
		setDevicetItem(defaultChannelNum, false);
		setBundleData(data);
		msg.setData(data);
		mHandler.sendMessage(msg);
	}

	// 超时验证
	protected void onTimeOut() {
		Log.i(TAG, "onTimeOut");
		connectionLongOver = true;
		Message msg = new Message();
		Bundle data = new Bundle();
		msg.what = MULTICONNIDENTIFYTIMEOUT;
		setDevicetItem(defaultChannelNum, false);
		setBundleData(data);
		msg.setData(data);
		mHandler.sendMessage(msg);
		closeSocket();
	}

	private void closeSocket() {
		if ((client != null) && !client.isClosed()) {
			try {
				client.close();
				client = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setCancel(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public void start() {
		timeThread.start();
		workThread.start();
	}
}
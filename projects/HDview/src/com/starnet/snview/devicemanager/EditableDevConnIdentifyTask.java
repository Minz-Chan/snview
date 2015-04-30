package com.starnet.snview.devicemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.starnet.snview.component.BufferSendManager;
import com.starnet.snview.protocol.message.LoginRequest;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.PhoneInfoRequest;
import com.starnet.snview.protocol.message.VersionInfoRequest;

public class EditableDevConnIdentifyTask {
	private static final String TAG = "EditableDevConnIdentifyTask";
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
	private boolean isConnectedOver = false;
	private boolean shouldTimeOutOver = false;
	private boolean isOnWorkdIOErr;
	private boolean isOnWorkdUnknwnHost;

	public EditableDevConnIdentifyTask(Handler handler, DeviceItem deviceItem) {
		this.mHandler = handler;
		this.mDeviceItem = deviceItem;
		initialThread();
	}

	private void initialThread() {
		isCanceled = false;
		isOnWorkdIOErr = false;
		isConnectedOver = false;
		shouldTimeOutOver = false;
		isOnWorkdUnknwnHost = false;
		isOnConnectionWrong = false;
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
							if (!isCanceled) {// && !shouldTimeOutOver
								closeConnection();
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
				super.run();
				if (!isCanceled) {// user did not click back.
					boolean isConnected = initialClientSocket();
					if (isConnected) {
						startConnectionIdentify();
					} else {// 连接不正常的处理，有时候比较耗时
						isOnWorkdUnknwnHost = true;
						isConnectedOver = true;
						isOnWorkdIOErr = true;
						if (!isCanceled && !shouldTimeOutOver
								&& !isConnectedOver && !isOnConnectionWrong) {
							closeConnection();
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
			msg.what = DeviceEditableActivity.CONNECTIFYIDENTIFY_WRONG;
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
			msg.what = DeviceEditableActivity.CONNECTIFYIDENTIFY_HOST_ERROR;
			if (!isCanceled && !isOnWorkdUnknwnHost) {
				mHandler.sendMessage(msg);
			}
		}
	}

	/** ??错误的操作 ***/
	protected void onWorkdIOErr() {
		if (!shouldTimeOutOver && !isCanceled && !isOnWorkdIOErr) {
			shouldTimeOutOver = true;
			isConnectedOver = true;
			Message msg = new Message();
			msg.what = DeviceEditableActivity.CONNECTIFYIDENTIFY_WRONG;
			if (!isCanceled && !isOnWorkdIOErr) {
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

	
	 
	/* The length within OWSP header encoded using BIG_ENDIAN */
	private final int RESPONSE_VALIDATE_SUCC = 140;	// len = sizeof(SEQ) + TLV(VersionInfoReq) + TLV(DVSInfoReq)
													// 	  + TLV(ChannelRsp) + TLV(StreamDataFormat) = 140
	private final int RESPONSE_VALIDATE_FAIL = 20;	// len = sizeof(SEQ) + TLV(VersionInfoReq) + TLV(LoginRsp) = 20
	
	/** 获取网络连接验证的信息 */
	private void getConnectionIdentifyInfo() throws IOException {
		InputStream in = client.getInputStream();
		int packetLength = parsePacketHeader(in);
		
		if (!isCanceled) {
			Message msg = Message.obtain();
			shouldTimeOutOver = true;
			if (packetLength == RESPONSE_VALIDATE_SUCC) {
				Log.d(TAG, "Validattion result: success !!!");
				msg.what = DeviceEditableActivity.CONNECTIFYIDENTIFY_SUCCESS;
			} else if (packetLength == RESPONSE_VALIDATE_FAIL) {
				Log.d(TAG, "Validattion result: fail !!!");
				msg.what = DeviceEditableActivity.CONNECTIFYIDENTIFY_USERPSWD_ERROR;
				msg.arg1 = parseLoginResponse(in); // error code of login	
				Log.d(TAG, "Login error code: " + msg.arg1);
			} else { // 0, connection may be closed rapidly by remote host;
					 // Otherwise, client connect to the port of other service
					 // or unexpected data is returned
				Log.d(TAG, "Validattion result: unknown error !!!");
				msg.what = DeviceEditableActivity.CONNECTIFYIDENTIFY_WRONG;
			}
			mHandler.sendMessage(msg);
		}
		
		closeConnection();
	}
	
	
	private final int OWSP_HEADER_LEN = 8;
	/**
	 * Parse the packet header
	 * @param in The stream provide the read data
	 * @return Packet length that is defined in OWSP protocol
	 */
	private int parsePacketHeader(InputStream in) {
		byte[] packetHeader = null;
		ByteBuffer header = null;
		int packetLength = 0;
		try {
			packetHeader = new byte[OWSP_HEADER_LEN];
			in.read(packetHeader);
			
			header = ByteBuffer.wrap(packetHeader);
			header.order(ByteOrder.BIG_ENDIAN);
			packetLength = header.getInt(); // Actually, getUnsingedInt() should be called
		} catch (IOException e) {
			packetLength = -1;
			e.printStackTrace();
		}
		
		return packetLength;
	}
	
	/**
	 * Parse the login response.
	 * @param in The stream provide the read data
	 * @return -1, error occurs while parsing; otherwise, return 
	 * 	error code responded by server.  
	 */
	private int parseLoginResponse(InputStream in) {
		byte[] packetBody = null;
		ByteBuffer body = null;
		int result = -1;
		try {
			packetBody = new byte[RESPONSE_VALIDATE_FAIL-4];
			in.read(packetBody);
			
			body = ByteBuffer.wrap(packetBody);
			body.order(ByteOrder.LITTLE_ENDIAN);
			body.position((4+4)+4);  // skip TLV(VersionInfoReq) + TL
			result = body.getShort();  // Actually, getUnsighedShort() should be called
		} catch (IOException e) {
			result = -1;
			e.printStackTrace();
		}
		
		return result;
	}
	
	

	/** 设置验证后的设备 ***/
//	private void setWrongDevicetItem(int channelNumber) {
//		String chanelName = context.getString(R.string.device_manager_channel);
//		List<Channel> channelList = new ArrayList<Channel>();
//		mDeviceItem.setChannelSum(String.valueOf(channelNumber));
//		for (int i = 0; i < channelNumber; i++) {
//			Channel channel = new Channel();
//			channel.setChannelName(chanelName + "" + (i + 1));
//			channel.setChannelNo((i + 1));
//			channel.setSelected(false);
//			channelList.add(channel);
//		}
//		mDeviceItem.setChannelList(channelList);
//		mDeviceItem.setIdentify(true);
//		mDeviceItem.setConnPass(false);
//	}

	/** 设置验证后的设备 ***/
//	private void setDevicetItem(int channelNumber) {
//		String chanelName = context.getString(R.string.device_manager_channel);
//		List<Channel> channelList = new ArrayList<Channel>();
//		mDeviceItem.setChannelSum(String.valueOf(channelNumber));
//		for (int i = 0; i < channelNumber; i++) {
//			Channel channel = new Channel();
//			channel.setChannelName(chanelName + (i + 1));
//			channel.setChannelNo((i + 1));
//			channel.setSelected(false);
//			channelList.add(channel);
//		}
//		mDeviceItem.setChannelList(channelList);
//		mDeviceItem.setIdentify(true);
//		mDeviceItem.setConnPass(true);
//
//	}

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
			msg.what = DeviceEditableActivity.CONNECTIFYIDENTIFY_TIMEOUT;
			if (!shouldTimeOutOver) {
				mHandler.sendMessage(msg);
			}
		}
		shouldTimeOutOver = true;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void setCancel(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public void setCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
		isOnWorkdUnknwnHost = true;
		shouldTimeOutOver = true;
		isConnectedOver = true;
		isOnWorkdIOErr = true;
		closeConnection();
	}

	private void closeConnection() {
		if ((client != null) && client.isConnected()) {
			try {
				client.close();
				client = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

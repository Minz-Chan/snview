package com.starnet.snview.playback.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.starnet.snview.component.BufferSendManagerPlayBack;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.protocol.message.OWSPDateTime;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PlayBackSearchRecordTask {

	private Socket client;
	private Handler mHandler;
	private Thread timeThread;
	private Thread loadThread;
	boolean isConnected = false;
	private final int TIMECOUNT = 4;
	private TLV_V_SearchRecordRequest srr;
	private DeviceItem visitDevItem;
	private BufferSendManagerPlayBack back;
	private final int NOTIFYREMOTEUIFRESH_SUC = 0x0008;
	private final int NOTIFYREMOTEUIFRESH_FAIL = 0x0009;
	private final int NOTIFYREMOTEUIFRESH_TMOUT = 0x0006;

	private boolean isCancel;
	private boolean isTimeOut;
	private boolean isInitWrong;
	private boolean isOnSocketWork;

	public PlayBackSearchRecordTask(Handler mHandler, DeviceItem dItem,
			TLV_V_SearchRecordRequest srr) {
		this.srr = srr;
		this.mHandler = mHandler;
		this.visitDevItem = dItem;
		loadThread = new Thread() {
			@Override
			public void run() {
				super.run();
				initClient();
				if (isConnected) {
					searchRecordRequestWork();// 如果请求录像回放记录成功，则允许其进行登录认证，否则，不允许
				}
			}
		};

		timeThread = new Thread() {
			@Override
			public void run() {
				super.run();
				int timeCount = 0;
				boolean isCanRun = true;
				while (isCanRun && !isTimeOut && !isCancel) {
					try {
						Thread.sleep(1000);
						timeCount++;
						if (timeCount == TIMECOUNT) {
							isCanRun = false;
							isInitWrong = true;
							isOnSocketWork = true;
							
							onWorkTimeOut();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	protected void onWorkTimeOut() {
		Message msg = new Message();
		msg.what = NOTIFYREMOTEUIFRESH_TMOUT;
		mHandler.sendMessage(msg);
	}

	private void initClient() {
		try {
			String host = visitDevItem.getSvrIp();
			int port = Integer.valueOf(visitDevItem.getSvrPort());
			client = new Socket(host, port);
			isConnected = client.isConnected();
		} catch (Exception e) {
			if (!isCancel && !isInitWrong) {
				isTimeOut = true;
				isOnSocketWork = true;
				Bundle data = new Bundle();
				Message msg = new Message();
				msg.what = NOTIFYREMOTEUIFRESH_FAIL;
				data.putParcelableArrayList("srres", null);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}
		}
	}

	private void searchRecordRequestWork() {
		try {
			back = BufferSendManagerPlayBack.getInstance();
			back.setOutStream(client.getOutputStream());
			if (isConnected) {
				back.write(new OwspBegin());
				back.write(srr);
				back.write(new OwspEnd());
				setResponseForSearchRecord();
			}
		} catch (Exception e) {
			if (!isCancel) {
				isTimeOut = true;
				isInitWrong = true;
				Bundle data = new Bundle();
				Message msg = new Message();
				msg.what = NOTIFYREMOTEUIFRESH_FAIL;
				data.putParcelableArrayList("srres", null);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}
		}
	}

	/** 获取记录设备的返回列表，并且通知远程回放界面渲染时间轴 **/
	// 首先判断是否返回成功，如果不成功，则不需要渲染时间轴
	private void setResponseForSearchRecord() throws IOException {
		InputStream in = client.getInputStream();
		parseResponsePacketFromSocket(in);// 解析数据返回包，首先需要解包头，其次，需要解析包的TLV部分；
	}

	// ceshi ??????
	public void testParseResponsePacketFromSocket() {
		byte[] bytes = { 0, 0, 0, 89, 0, 0, 0, 0, 1, 1, 3, 0, 1, -126, 0, 8, 8,
				22, 0, 0, 0, 0, 0, -33, 07, 1, 1, 2, 2, 2, -33, 07, 1, 1, 2, 2,
				2, 3, 2, 0, 0, 8, 8, 22, 0, 0, 0, 0, 0, -33, 07, 2, 2, 3, 3, 3,
				-33, 07, 2, 2, 3, 3, 3, 3, 2, 0, 0, 8, 8, 22, 0, 0, 0, 0, 0,
				-33, 07, 3, 3, 4, 4, 4, -33, 07, 3, 3, 4, 4, 4, 3, 2, 0, 0 };
		InputStream testInputStream = new ByteArrayInputStream(bytes);
		parseResponsePacketFromSocket(testInputStream);
	}

	/*** 从包中读取SearchRecordResponse的信息 */
	@SuppressWarnings("static-access")
	private void parseResponsePacketFromSocket(InputStream in) {
		TLV_V_SearchRecordResponse srr = new TLV_V_SearchRecordResponse();
		byte[] head = new byte[8];
		try {
			in.read(head);
			ByteBuffer headBuffer = ByteBuffer.allocate(8).wrap(head);
			headBuffer = headBuffer.order(ByteOrder.BIG_ENDIAN);
			int packetLen = headBuffer.getInt() - 4;// TLV的总长度
			Log.i("packetLen", "packetLen:" + packetLen);
			byte[] tBuf = new byte[7];
			in.read(tBuf);
			ByteBuffer t = ByteBuffer.wrap(tBuf).order(ByteOrder.LITTLE_ENDIAN);
			int type = t.getShort();
			int leng = t.getShort();
			byte tempresult = t.get(4);
			byte tempcount = t.get(5);
			byte tempreserve = t.get(6);

			int reserve;
			if (tempreserve < 0) {
				reserve = getIntFromByte(tempreserve);
			} else {
				reserve = tempreserve;
			}

			int count;
			if (tempcount < 0) {
				count = getIntFromByte(tempcount);
			} else {
				count = tempcount;
			}

			int result;
			if (tempcount < 0) {
				result = getIntFromByte(tempresult);
			} else {
				result = tempresult;
			}

			if (result == 1) {// 表示成功
				ArrayList<TLV_V_RecordInfo> infoList = new ArrayList<TLV_V_RecordInfo>();
				if (count > 0) {
					for (int i = 0; i < count; i++) {
						TLV_V_RecordInfo recordInfo = new TLV_V_RecordInfo();
						byte[] temp = new byte[26];
						in.read(temp);
						ByteBuffer tempBuffer = ByteBuffer.wrap(temp).order(
								ByteOrder.LITTLE_ENDIAN);
						tempBuffer.getShort();
						tempBuffer.getShort();
						int deviceID = tempBuffer.getInt();
						// 解析时间
						int startyear = tempBuffer.getShort();
						int startMonth = tempBuffer.get();
						int startDay = tempBuffer.get();
						int starthour = tempBuffer.get();
						int startminute = tempBuffer.get();
						int startsecond = tempBuffer.get();

						OWSPDateTime startTime = new OWSPDateTime();
						startTime.setYear(startyear);
						startTime.setMonth(startMonth);
						startTime.setDay(startDay);
						startTime.setHour(starthour);
						startTime.setMinute(startminute);
						startTime.setSecond(startsecond);

						int endyear = tempBuffer.getShort();
						int endMonth = tempBuffer.get();
						int endDay = tempBuffer.get();
						int endhour = tempBuffer.get();
						int endminute = tempBuffer.get();
						int endsecond = tempBuffer.get();

						OWSPDateTime endTime = new OWSPDateTime();
						endTime.setYear(endyear);
						endTime.setMonth(endMonth);
						endTime.setDay(endDay);
						endTime.setHour(endhour);
						endTime.setMinute(endminute);
						endTime.setSecond(endsecond);

						int channel = tempBuffer.get();
						int recordTypeMask = tempBuffer.get();
						int reserve1 = tempBuffer.get();
						int reserve2 = tempBuffer.get();
						int reserv[] = { reserve1, reserve2 };

						recordInfo.setChannel(channel);
						recordInfo.setDeviceid(deviceID);
						recordInfo.setStartTime(startTime);
						recordInfo.setEndTime(endTime);
						recordInfo.setRecordTypeMask(recordTypeMask);
						recordInfo.setReserve(reserv);

						infoList.add(recordInfo);
					}
					if (!isOnSocketWork && !isCancel) {
						isTimeOut = true;
						isInitWrong = true;
						Bundle data = new Bundle();
						Message msg = new Message();
						msg.what = NOTIFYREMOTEUIFRESH_SUC;
						data.putParcelableArrayList("srres", infoList);
						msg.setData(data);
						mHandler.sendMessage(msg);
					}
				} else {// 没有数据
					if (!isOnSocketWork && !isCancel) {
						isTimeOut = true;
						isInitWrong = true;
						Bundle data = new Bundle();
						Message msg = new Message();
						msg.what = NOTIFYREMOTEUIFRESH_FAIL;
						data.putParcelableArrayList("srres", null);
						msg.setData(data);
						mHandler.sendMessage(msg);
					}
				}
			} else {// 表示失败
				if (!isOnSocketWork && !isCancel) {
					isTimeOut = true;
					isInitWrong = true;
					Bundle data = new Bundle();
					Message msg = new Message();
					msg.what = NOTIFYREMOTEUIFRESH_FAIL;
					data.putParcelableArrayList("srres", null);
					msg.setData(data);
					mHandler.sendMessage(msg);
				}
			}
			srr.setReserve(reserve);
			srr.setCount(count);
			srr.setResult(result);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int getIntFromByte(byte b) {
		String result = byte2HexString(b);
		return HexString2Ten(result);
	}

	/** 单个字节转化为字符串 **/
	@SuppressLint("DefaultLocale")
	public String byte2HexString(byte b) {
		String ret = "";
		String hex = Integer.toHexString(b & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		ret += hex.toUpperCase();
		return ret;
	}

	public int HexString2Ten(String result) {
		int re = Integer.parseInt(result, 16);
		return re;
	}

	/** 字节数组转化为字符串 **/
	@SuppressLint("DefaultLocale")
	public String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			String temp = hex.toUpperCase();
			ret += temp;
		}
		return ret;
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}

	public void start() {

		isCancel = false;
		isTimeOut = false;
		isInitWrong = false;
		isOnSocketWork = false;

		loadThread.start();
		timeThread.start();

	}
}
package com.starnet.snview.playback.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.starnet.snview.component.BufferSendManagerPlayBack;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.protocol.message.OWSPDateTime;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.VersionInfoRequest;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class PlaybackControllTask {

	protected final String TAG = "PlaybackControllTask";

	private final int PlayCommandStart = 1; // 播放
	private final int PlayCommandPause = 2; // 暂停
	private final int PlayCommandResume = 3; // 继续
	private final int PlayCommandStop = 4; // 停止

	private boolean isCancel = false;
	private boolean isTimeOut = false;
//	private boolean isInitWrong = false;
	private boolean isOnSocketWork = false;

	private final int NOTIFYREMOTEUIFRESH_SUC = 0x0008;
	private final int NOTIFYREMOTEUIFRESH_EXCEPTION = 0x0009;
	private final int NOTIFYREMOTEUIFRESH_TMOUT = 0x0006;

	protected PlaybackControllTask() {

	}

	public PlaybackControllTask(Handler mHandler) {
		this.mHandler = mHandler;
	}

	private static PlaybackControllTask instance;

	public static PlaybackControllTask getInstance(Context ctx,
			Handler mHandler, TLV_V_SearchRecordRequest srr, DeviceItem dItem) {
		if (instance == null) {
			return new PlaybackControllTask(ctx, mHandler, srr, dItem);
		}
		return instance;
	}

	private Context ctx;
	private Socket client;
	private Handler mHandler;
	private boolean isConnected;
	private Thread timeThread;
	private Thread firstPlayThread;
	private DeviceItem visitDevItem;
	private TLV_V_SearchRecordRequest srr;
	private final int TIMEOUT = 8;

	private boolean isCanLogin;
	private boolean isCanPlay;

	public PlaybackControllTask(Context ctx, Handler mHandler,
			TLV_V_SearchRecordRequest srr, DeviceItem dItem) {
		this.ctx = ctx;
		this.srr = srr;
		this.mHandler = mHandler;
		this.visitDevItem = dItem;
		firstPlayThread = new Thread() {
			@Override
			public void run() {
				super.run();
				if (!isCancel) {
					initClient();
					if (isConnected && !isCancel) {
						searchRecordFile();
						if (isCanLogin && !isCancel) {
							loginRequestWork();
							if (isCanPlay && !isCancel) {
								playRecordRequesWork();
							}
						}
					}
				}
			}
		};
		timeThread = new Thread() {
			@Override
			public void run() {
				super.run();
				boolean isCanRun = false;
				int timeCount = 0;
				while (!isTimeOut && !isCanRun && !isCancel) {
					try {
						Thread.sleep(1000);
						timeCount++;
						if (timeCount == TIMEOUT) {
							isCanRun = true;
							onTimeOutWork();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}

	protected void onTimeOutWork() {// 超时处理
		isTimeOut = true;
//		isInitWrong = true;
		isOnSocketWork = true;
		sendMessageToActivity(NOTIFYREMOTEUIFRESH_TMOUT);
	}
	
	
	protected void onClientWrongWork() {// 无法连接网络
		isTimeOut = true;
//		isInitWrong = true;
		isOnSocketWork = true;
		sendMessageToActivity(NOTIFYREMOTEUIFRESH_EXCEPTION);
	}
	
	private void sendMessageToActivity(int notifyID) {
		Message msg = new Message();
		msg.what = notifyID;
		mHandler.sendMessage(msg);
	}


	protected void searchRecordFile() {
		try {
			if (isConnected) {
				sender.write(new OwspBegin());
				sender.write(srr);
				sender.write(new OwspEnd());
				parseSearchRecordResponse();
			}
		} catch (Exception e) {
			e.printStackTrace();
			isCanLogin = false;
		}
	}

	/** 获取记录设备的返回列表，并且通知远程回放界面渲染时间轴 **/
	// 首先判断是否返回成功，如果不成功，则不需要渲染时间轴
	private void parseSearchRecordResponse() throws IOException {
		ArrayList<TLV_V_RecordInfo> infoList = new ArrayList<TLV_V_RecordInfo>();
		infoList = PlaybackControllTaskUtils
				.parseResponsePacketFromSocket(receiver);// 解析数据返回包，首先需要解包头，其次，需要解析包的TLV部分；
		isCanLogin = PlaybackControllTaskUtils.isCanPlay;

		if (!isOnSocketWork && !isCancel) {
			isTimeOut = true;
//			isInitWrong = true;
			Bundle data = new Bundle();
			Message msg = new Message();
			msg.what = NOTIFYREMOTEUIFRESH_SUC;
			data.putParcelableArrayList("srres", infoList);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}

	private InputStream receiver = null;
	private BufferSendManagerPlayBack sender = null;

	private void initClient() {
		try {
			String host = visitDevItem.getSvrIp();
			int port = Integer.valueOf(visitDevItem.getSvrPort());
			client = new Socket(host, port);
			isConnected = client.isConnected();
		} catch (Exception e) {
			isCanLogin = false;
			isCanPlay = false;
			isConnected = false;
			onClientWrongWork();
		} finally {
			try {
				if (isConnected) {
					receiver = client.getInputStream();
					BufferSendManagerPlayBack.getInstance().setOutStream(
							client.getOutputStream());
					sender = BufferSendManagerPlayBack.getInstance();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void playRecordRequesWork() {
		try {

			OWSPDateTime time = new OWSPDateTime();
			time.setYear(6);
			time.setMonth(1);
			time.setDay(29);
			time.setHour(18);
			time.setMinute(17);
			time.setSecond(40);

			TLV_V_PlayRecordRequest prr = new TLV_V_PlayRecordRequest();

			prr.setDeviceId(0);
			// prr.setStartTime(srr.getStartTime());
			prr.setStartTime(time);
			prr.setCommand(PlayCommandStart);
			prr.setReserve(0);
			prr.setChannel(srr.getChannel());
			// prr.setChannel(1);

			sender.write(new OwspBegin());
			sender.write(prr);
			sender.write(new OwspEnd());

			// PlaybackControllTaskUtils.parseVideoAndAudioRsp(receiver);
			DataProcessService serv = new DataProcessServiceImpl("conn");
			PlaybackControllTaskUtils.setService(serv);
			PlaybackControllTaskUtils.newParseVideoAndAudioRsp(receiver);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loginRequestWork() {
		try {

			VersionInfoRequest v = new VersionInfoRequest();
			v.setVersionMajor(0x534E);
			v.setVersionMinor(0x0000);

			TLV_V_LoginInfoRequest l = new TLV_V_LoginInfoRequest();
			l.setUserName(visitDevItem.getLoginUser());
			l.setPassword(visitDevItem.getLoginPass());
			l.setDeviceId(0);
			l.setFlag(1);
			l.setChannel(srr.getChannel());
			// l.setChannel(1);
			l.setDataType(2);
			// l.setDataType(2);// 0:只包含视频；1只包含音频；2 音频和视频都包含
			l.setStreamMode(3);// 录像类型

			sender.write(new OwspBegin());
			sender.write(v);
			sender.write(l);
			sender.write(new OwspEnd());

			isCanPlay = PlaybackControllTaskUtils.parseLoginRsp(receiver);

		} catch (Exception e) {
			e.printStackTrace();
			isCanPlay = false;
		}
	}

	public void setDeviceItem(DeviceItem visitDevItem) {
		this.visitDevItem = visitDevItem;
	}

	public void setSearchRecord(TLV_V_SearchRecordRequest srr) {
		this.srr = srr;
	}

	public void start() {

		timeThread.start();
		firstPlayThread.start();

	}

	/** 停止播放 **/
	public void stop() {

		try {
			TLV_V_PlayRecordRequest prr = new TLV_V_PlayRecordRequest();
			prr.setDeviceId(0);
			prr.setStartTime(srr.getStartTime());
			prr.setCommand(PlayCommandStop);
			prr.setReserve(0);
			prr.setChannel(srr.getChannel());

			sender.write(new OwspBegin());
			sender.write(prr);
			sender.write(new OwspEnd());

		} catch (Exception e) {

		}

	}

	/** 继续播放 **/
	public void resume() {
		try {

			TLV_V_PlayRecordRequest prr = new TLV_V_PlayRecordRequest();
			prr.setDeviceId(0);
			prr.setStartTime(srr.getStartTime());
			prr.setCommand(PlayCommandResume);
			prr.setReserve(0);
			prr.setChannel(srr.getChannel());

			sender.write(new OwspBegin());
			sender.write(prr);
			sender.write(new OwspEnd());

			PlaybackControllTaskUtils.newParseVideoAndAudioRsp(receiver);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/** 暂停播放 **/
	public void pause() {
		try {

			TLV_V_PlayRecordRequest prr = new TLV_V_PlayRecordRequest();
			prr.setDeviceId(0);
			prr.setStartTime(srr.getStartTime());
			prr.setCommand(PlayCommandPause);
			prr.setReserve(0);
			prr.setChannel(srr.getChannel());
			sender.write(new OwspBegin());
			sender.write(prr);
			sender.write(new OwspEnd());

			PlaybackControllTaskUtils.newParseVideoAndAudioRsp(receiver);
		} catch (Exception e) {

		}
	}

	/** 开始播放 **/
	public void startPlay() {

	}

	public void exit() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}

}
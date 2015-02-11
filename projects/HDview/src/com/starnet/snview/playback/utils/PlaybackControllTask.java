package com.starnet.snview.playback.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.starnet.snview.component.BufferSendManagerPlayBack;
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
	private boolean isOnSocketWork = false;
	
	private final int ONLY_AUDIO = 1;
	private final int ONLY_VIDEO = 0;
	private final int AUDIAO_VIDEO = 2;
	
	protected Context ctx;
	private Socket client;
	private Handler mHandler;
	private Thread timeThread;
	private boolean isCanPlay;
	private boolean isCanLogin;
	private boolean isConnected;
	private final int TIMEOUT = 80;
	private Thread firstPlayThread;
	private Thread socketPauseThread;
	private Thread socketResumeThread;
	private OWSPDateTime playStartTime;
	private LoginDeviceItem visitDevItem;
	private TLV_V_SearchRecordRequest srr;
	private static PlaybackControllTask instance;
	private final int NOTIFYREMOTEUIFRESH_SUC = 0x0008;
	private final int NOTIFYREMOTEUIFRESH_TMOUT = 0x0006;
	private final int NOTIFYREMOTEUIFRESH_EXCEPTION = 0x0009;
	
	protected PlaybackControllTask() {

	}

	public PlaybackControllTask(Handler mHandler) {
		this.mHandler = mHandler;
	}

	

	public static PlaybackControllTask getInstance(Context ctx,
			Handler mHandler, TLV_V_SearchRecordRequest srr, LoginDeviceItem dItem) {
		if (instance == null) {
			return new PlaybackControllTask(ctx, mHandler, srr, dItem);
		}
		return instance;
	}



	public PlaybackControllTask(Context ctx, Handler mHandler,TLV_V_SearchRecordRequest srr, LoginDeviceItem dItem) {
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
		
		socketPauseThread = new Thread(){
			@Override
			public void run() {
				super.run();
				while (!isPauseSuc&&!isCancel) {
					//执行暂停操作
				}
			}
		};
		
		socketResumeThread = new Thread(){
			@Override
			public void run() {
				super.run();
				while (!isResumeSuc&&!isCancel) {
					//执行继续操作
				}
			}
		};
	}

	protected void onTimeOutWork() {// 超时处理
		isTimeOut = true;
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
		PlaybackControllTaskUtils.newParseVideoAndAudioRsp(receiver);
		infoList = PlaybackControllTaskUtils.getRecordInfos();
		if (infoList != null) {
			playStartTime = infoList.get(0).getStartTime();
			isCanLogin = true;
		}else {
			isCanLogin = false;
		}

		if (!isOnSocketWork && !isCancel) {
			isTimeOut = true;
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
			DataProcessService serv = new DataProcessServiceImpl(ctx, "conn");
			PlaybackControllTaskUtils.setService(serv);
			String[] ips = visitDevItem.getSvrIP();
			String host = PlaybackControllTaskUtils.getIP(ips);
			int port = Integer.valueOf(visitDevItem.getSvrPort());
			client = new Socket(host, port);
//			client.setSoTimeout(TIMEOUT * 1000);
			isConnected = client.isConnected();
		} catch (Exception e) {
			e.printStackTrace();
			isCanLogin = false;
			isCanPlay = false;
			isConnected = false;
			onClientWrongWork();
		} finally {
			try {
				if (isConnected) {
					receiver = client.getInputStream();
					BufferSendManagerPlayBack.getInstance().setOutStream(client.getOutputStream());
					sender = BufferSendManagerPlayBack.getInstance();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void playRecordRequesWork() {
		try {
			
			TLV_V_PlayRecordRequest prr = new TLV_V_PlayRecordRequest();

			prr.setDeviceId(0);
			prr.setStartTime(playStartTime);
			prr.setCommand(PlayCommandStart);
			prr.setReserve(0);
			prr.setChannel(srr.getChannel());

			sender.write(new OwspBegin());
			sender.write(prr);
			sender.write(new OwspEnd());

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
//			l.setDataType(ONLY_AUDIO);
//			l.setDataType(ONLY_VIDEO);
			l.setDataType(AUDIAO_VIDEO);// 0:只包含视频；1只包含音频；2 音频和视频都包含
			l.setStreamMode(3);// 录像类型

			sender.write(new OwspBegin());
			sender.write(v);
			sender.write(l);
			sender.write(new OwspEnd());

			PlaybackControllTaskUtils.newParseVideoAndAudioRsp(receiver);
			isCanPlay = PlaybackControllTaskUtils.isCanPlay;

		} catch (Exception e) {
			e.printStackTrace();
			isCanPlay = false;
		}
	}

	public void setDeviceItem(LoginDeviceItem visitDevItem) {
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
	public void resume(OWSPDateTime startTime) {//需要开启时间线程
		try {
			while (!isResumeSuc&&!isCancel) {
				TLV_V_PlayRecordRequest prr = new TLV_V_PlayRecordRequest();
				prr.setDeviceId(0);
				prr.setStartTime(startTime);//重新设置继续播放时间？？？？
				prr.setCommand(PlayCommandResume);
				prr.setReserve(0);
				prr.setChannel(srr.getChannel());

				sender.write(new OwspBegin());
				sender.write(prr);
				sender.write(new OwspEnd());

				PlaybackControllTaskUtils.newParseVideoAndAudioRsp(receiver);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 暂停播放 **/
	public void pause(OWSPDateTime startTime) {
		try {
			while (!isPauseSuc&&!isCancel) {
				TLV_V_PlayRecordRequest prr = new TLV_V_PlayRecordRequest();
				prr.setDeviceId(0);
				prr.setStartTime(startTime);
				prr.setCommand(PlayCommandPause);
				prr.setReserve(0);
				prr.setChannel(srr.getChannel());
				sender.write(new OwspBegin());
				sender.write(prr);
				sender.write(new OwspEnd());
				PlaybackControllTaskUtils.newParseVideoAndAudioRsp(receiver);
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exit() {
		try {
			if (client != null && !client.isClosed() && client.isConnected()) {
				client.close();
				client = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
	/**新的暂停接口**/
	public void pauseWork(){
		timeThread.start();
		socketPauseThread.start();
	}
	/**新的继续播放接口**/
	public void resumeWork(){
		timeThread.start();
		socketResumeThread.start();
	}
	
	private boolean isPauseSuc;
	private boolean isResumeSuc;
	public void setPause(boolean isPauseSuc){
		this.isPauseSuc = isPauseSuc;
	}
	
	public void setReume(boolean isResumeSuc){
		this.isResumeSuc = isResumeSuc;
	}

}

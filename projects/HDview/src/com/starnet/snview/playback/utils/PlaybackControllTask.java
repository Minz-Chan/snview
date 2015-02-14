package com.starnet.snview.playback.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.starnet.snview.component.BufferSendManagerPlayBack;
import com.starnet.snview.component.audio.AudioBufferQueue;
import com.starnet.snview.component.audio.AudioCodec;
import com.starnet.snview.component.audio.AudioHandler;
import com.starnet.snview.component.audio.AudioPlayer;
import com.starnet.snview.component.liveview.PlaybackLiveViewItemContainer;
import com.starnet.snview.component.video.VideoHandler;
import com.starnet.snview.playback.PlaybackActivity;
import com.starnet.snview.protocol.message.OWSPDateTime;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.VersionInfoRequest;

import android.content.Context;
import android.media.AudioFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PlaybackControllTask {

	protected final String TAG = "PlaybackControllTask";

	private PlaybackController controller;

	private boolean isCancel = false;
	private boolean isTimeOut = false;
	private boolean isOnSocketWork = false;
	
	private final int ONLY_AUDIO = 1;
	private final int ONLY_VIDEO = 0;
	private final int AUDIAO_VIDEO = 2;
	private static final int COMMANDTIMEOUT = 15;
	
	protected Context ctx;
	private Socket client;
	private Handler mHandler;
	private Thread timeThread;
	private boolean isCanPlay;
	private boolean isCanLogin;
	private boolean isConnected;
	private final int TIMEOUT = 8;//超时时间设置
	private Thread firstPlayThread;
	private OWSPDateTime playStartTime;
	private LoginDeviceItem visitDevItem;
	private TLV_V_SearchRecordRequest srr;
	private static PlaybackControllTask instance;

	private final int PAUSE_RESUME_TIMEOUT = 0x0002;
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
		PlaybackControllTaskUtils.mHandler = mHandler;
//		serv = new DataProcessServiceImpl(ctx, "conn");

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
		
		// Audio play thread
		HandlerThread audioPlayThread = new HandlerThread("audioPlayThread");
		audioPlayThread.start();
		AudioHandler audioPlayHandler = new AudioHandler(audioPlayThread.getLooper());
		
		// Video play thread
		HandlerThread videoPlayThread = new HandlerThread("videoPlayThread");
		videoPlayThread.start();
		VideoHandler videoPlayHandler = new VideoHandler(videoPlayThread.getLooper(), 
				((PlaybackActivity)ctx).getVideoContainer().getSurfaceView());
		
		serv = new DataProcessServiceImpl(ctx, audioPlayHandler, videoPlayHandler);
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

	private DataProcessService serv;
	private InputStream receiver = null;
	private BufferSendManagerPlayBack sender = null;

	private void initClient() {
		try {
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
			prr.setCommand(PlaybackCommand.START);
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
		serv.setPause(false);
		serv.setResume(false);
		PlaybackControllTaskUtils.setPause(false);
		timeThread.start();
		firstPlayThread.start();
	}

	/** 停止播放 **/
	public void stop(OWSPDateTime time) {
		try {
			controller = new PlaybackController();
			controller.setChannel(srr.getChannel());
			controller.requestStop(time);
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
	public void pauseWork(OWSPDateTime startTime){
//		if (!isCancel && !isPause) {
			serv.setPause(true);
			serv.setResume(false);
			final OWSPDateTime sTime = startTime;
			controller = new PlaybackController();
			controller.setChannel(srr.getChannel());
			controller.requestPause(sTime);
//		}
	}
	/**新的继续播放接口**/
	public void resumeWork(OWSPDateTime startTime){
//		if (!isCancel && !isResume) {
			serv.setPause(false);
			serv.setResume(true);
			PlaybackControllTaskUtils.setPause(false);
			final OWSPDateTime sTime = startTime;
			controller = new PlaybackController();
			controller.setChannel(srr.getChannel());
			controller.requestResume(sTime);
//		}
	}
	
	private class PlaybackCommand {
		public static final int START = 1; 
		public static final int PAUSE = 2; 
		public static final int RESUME = 3; 
		public static final int STOP = 4; 
	}
	
	private class PlaybackController {
		private static final int INVALID_CHANNEL = -1;
		private int channel = INVALID_CHANNEL;
		
		public PlaybackController() {
			
		}
		
		public void setChannel(int channel) {
			this.channel = channel;
		}
		
		public void requestStart(final OWSPDateTime startTime) {
			sendCommand(PlaybackCommand.START, startTime);
		}
		
		public void requestResume(final OWSPDateTime startTime) {
			sendCommand(PlaybackCommand.RESUME, startTime);
		}
		
		public void requestPause(final OWSPDateTime startTime) {
			sendCommand(PlaybackCommand.PAUSE, startTime);
		}
		
		public void requestStop(final OWSPDateTime startTime) {
			sendCommand(PlaybackCommand.STOP, startTime);
		}
		
		private void sendCommand(final int cmdCode, final OWSPDateTime startTime) {
			(new Thread() {
				@Override
				public void run() {
					Log.i(TAG, "========Thread start========");
					if (sender != null && channel != -1) {
						TLV_V_PlayRecordRequest prr = new TLV_V_PlayRecordRequest();
						prr.setDeviceId(0);
						prr.setStartTime(startTime);
						prr.setCommand(cmdCode);
						prr.setReserve(0);
						prr.setChannel(channel);
						sender.write(new OwspBegin());
						sender.write(prr);
						sender.write(new OwspEnd());
					}
				}
			}).start();
			
			Thread timePickerThread = new Thread(){
				@Override
				public void run() {
					boolean canRun  = true;
					int timeCount = 0 ;
					while(canRun&&!timePickerThreadOver){
						try {
							Thread.sleep(1000);
							timeCount++;
							if (timeCount == COMMANDTIMEOUT) {
								canRun = false;
								if (!timePickerThreadOver) {
									Message msg = new Message();
									msg.what = PAUSE_RESUME_TIMEOUT;
									mHandler.sendMessage(msg);
								}
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			timePickerThread.start();
			Log.i(TAG, "========Thread end========");
		}
	}
	
	private boolean timePickerThreadOver = false;
	
	private boolean isResume = false;
	private boolean isPause = false;
	
	public void setResume(boolean isResume){
		this.isResume = isResume;
	}
	public void setPause(boolean isPause){
		this.isPause = isPause;
	}
	public void setTimePickerThreadOver(boolean isOver){
		this.timePickerThreadOver = isOver;
	}
}

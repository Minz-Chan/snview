package com.starnet.snview.playback.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.starnet.snview.component.BufferSendManagerPlayBack;
import com.starnet.snview.component.audio.AudioHandler;
import com.starnet.snview.component.video.VideoHandler;
import com.starnet.snview.playback.PlaybackActivity;
import com.starnet.snview.protocol.message.OWSPDateTime;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.VersionInfoRequest;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class PlaybackControllTask {
	
	private final static String TAG = "PlaybackControllTask";
	
	private static final int VERSION_MAJOR = 0x534E;
	private static final int VERSION_MINOR = 0x0000;

	public static final int RECORD_EOF = -1;
	public static final int LOGIN_SUC = 41;
	public static final int LOGIN_FAIL = 42;
	public static final int RECORDINFORS = 32;
	public static final int SEARCH_RECORD_FILE_NULL = 48;
	
	private final int TIMEOUT = 8; //超时时间设置

	private PlaybackController controller;

	private boolean isCancel = false;
	private boolean isTimeOut = false;
	private boolean isOnSocketWork = false;	
	
	protected Context context;
	private Socket client;
	private Handler mHandler;
	
	private boolean isCanPlay;
	private boolean isCanLogin;
	private boolean isConnected;
	
	private OWSPDateTime playStartTime;
	private PlaybackRequest playbackRequest;	
	
	private Thread recvThread;
	private Thread timeThread;
	private HandlerThread videoPlayThread;
	private HandlerThread audioPlayThread;

	private DataProcessService service;
	private InputStream receiver = null;
	private BufferSendManagerPlayBack sender = null;
	
	public PlaybackControllTask() { }

	public PlaybackControllTask(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public PlaybackControllTask(Context context, Handler mHandler, PlaybackRequest playbackRequest) {
		this.context = context;
		this.mHandler = mHandler;
		this.playbackRequest = playbackRequest;

		recvThread = new Thread("recvThread") {
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
		
		timeThread = new Thread("timeoutThread") {
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
		audioPlayThread = new HandlerThread("audioPlayThread");
		audioPlayThread.start();
		AudioHandler audioPlayHandler = new AudioHandler(audioPlayThread.getLooper());
		
		// Video play thread
		videoPlayThread = new HandlerThread("videoPlayThread");
		videoPlayThread.start();
		VideoHandler videoPlayHandler = new VideoHandler(videoPlayThread.getLooper(), 
				((PlaybackActivity)context).getVideoContainer().getSurfaceView());
		
		service = new DataProcessServiceImpl(context, audioPlayHandler, videoPlayHandler);
		controller = new PlaybackController();
	}

	protected void onTimeOutWork() {// 超时处理
		isTimeOut = true;
		isOnSocketWork = true;
		sendMessageToActivity(PlaybackActivity.NOTIFYREMOTEUIFRESH_TMOUT);
	}
	
	protected void onClientWrongWork() {// 无法连接网络
		isTimeOut = true;
//		isInitWrong = true;
		isOnSocketWork = true;
		sendMessageToActivity(PlaybackActivity.NOTIFYREMOTEUIFRESH_EXCEPTION);
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
				sender.write(playbackRequest.getSearchRecordRequestInfo());
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
		recvAndProcessData(receiver);
		ArrayList<TLV_V_RecordInfo> infoList = getRecordInfos();
		if (infoList != null&&infoList.size()>0) {
			playStartTime = infoList.get(0).getStartTime();
			isCanLogin = true;
		}else {
			isCanLogin = false;
		}
		if (!isOnSocketWork && !isCancel) {
			isTimeOut = true;
			Bundle data = new Bundle();
			Message msg = new Message();
			msg.what = PlaybackActivity.NOTIFYREMOTEUIFRESH_SUC;
			data.putParcelableArrayList("srres", infoList);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}

	private void initClient() {
		try {
			String host = playbackRequest.getDeviceInfo().getSvrIp();
			int port = Integer.valueOf(playbackRequest.getDeviceInfo().getSvrPort());
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
			prr.setChannel(getPlaybackChannel());
			sender.write(new OwspBegin());
			sender.write(prr);
			sender.write(new OwspEnd());
			
			breakDataProcess = false;
			resumePlay = true;
			recvAndProcessData(receiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loginRequestWork() {
		try {
			VersionInfoRequest v = new VersionInfoRequest();
			v.setVersionMajor(VERSION_MAJOR);
			v.setVersionMinor(VERSION_MINOR);
			TLV_V_LoginInfoRequest l = new TLV_V_LoginInfoRequest();
			l.setUserName(playbackRequest.getDeviceInfo().getLoginUser());
			l.setPassword(playbackRequest.getDeviceInfo().getLoginPass());
			l.setDeviceId(0);
			l.setFlag(1);
			l.setChannel(getPlaybackChannel());
			l.setDataType(STREAM_DATA_TYPE.MIXED);
			l.setStreamMode(STREAM_TYPE.STREAM_VOD);// 录像类型
			sender.write(new OwspBegin());
			sender.write(v);
			sender.write(l);
			sender.write(new OwspEnd());
			
			breakDataProcess = false;
			resumePlay = true;
			recvAndProcessData(receiver);
		} catch (Exception e) {
			e.printStackTrace();
			isCanPlay = false;
		}
	}
	
	private int getPlaybackChannel() {
		return playbackRequest.getSearchRecordRequestInfo().getChannel();
	}
	
	public void exit() {
		try {
			if ((audioPlayThread != null) && audioPlayThread.isAlive()) {
				audioPlayThread.quit();
			}
			if ((videoPlayThread != null) && videoPlayThread.isAlive()) {
				videoPlayThread.quit();
			}
			
			if (client != null && !client.isClosed() && client.isConnected()) {
				client.close();
				client = null;
			}
			
			breakDataProcess = true;  // quit data receive and process thread
			resumePlay = false;
			
			isTimeOut = true; // quit timeout thread
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}
		
	private boolean resumePlay = true;
	private boolean breakDataProcess = false;
	
	/**
	 * 接收并处理服务器返回的数据
	 * @param receiver
	 */
	public void recvAndProcessData(InputStream receiver) {// 需要一直从Socket中接收数据，直到接收完毕
		SocketInputStream sockIn = new SocketInputStream(receiver);
		byte[] packetHeaderBuf = new byte[8];
		byte[] tlvContent = new byte[655350];
		long packetLength;
		
		try {
WAIT_TO_RESUME:
			while(!breakDataProcess) {
				Thread.sleep(10);
				if (resumePlay) {
					packetLength = recvOnePacket(sockIn, packetHeaderBuf, tlvContent);
					while (!tlvContent.equals("")) {
						int result = service.process(tlvContent, (int)packetLength);
						if (decideWhether2RecvData(result)) {
							resumePlay = false;
							continue WAIT_TO_RESUME;
						}
						packetLength = recvOnePacket(sockIn, packetHeaderBuf, tlvContent);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resumePlay() {
		resumePlay = true;
	}
	
	/**
	 * 读取完整的一个数据包(COMMOND_HEADER + N个TLV)
	 * @param in 输入流
	 * @param headerBuf 公共包头数据接收缓冲区
	 * @param bodyBuf 包体数据接收缓冲区
	 * @return 包数据长度(sizeof(uint)+N个TLV长度)
	 * @throws IOException
	 */
	private long recvOnePacket(InputStream in, byte[] headerBuf, byte[] bodyBuf) throws IOException {
		long packetLength = 0;
		
		resetArray(headerBuf);
		in.read(headerBuf, 0, OWSP_LEN.OwspPacketHeader);
		
		checkHeader(headerBuf);
		
		packetLength = getPacketHeader(headerBuf).getPacket_length();
		bodyBuf = makesureBufferEnough(bodyBuf, (int)packetLength - 4);
		in.read(bodyBuf, 0, (int)packetLength - 4);
		
		return packetLength;
	}
	
	private void checkHeader(byte[] headerBuf) {
		TLV_V_PacketHeader header = (TLV_V_PacketHeader) ByteArray2Object
				.convert2Object(TLV_V_PacketHeader.class, headerBuf, 0,
						OWSP_LEN.OwspPacketHeader);
		
		if (!(header.getPacket_length() >= 4 && header.getPacket_seq() > 0)) {
			throw new IllegalStateException(
					"Invalid common header. [packetLen:"
							+ header.getPacket_length() + ", packetSeq:"
							+ header.getPacket_seq() + "]");
		}
		Log.i(TAG,
				"Packet seq:" + header.getPacket_seq() + ", len:"
						+ (header.getPacket_length() - 4));
	}
	
	private TLV_V_PacketHeader getPacketHeader(byte[] headerBuf) {
		return (TLV_V_PacketHeader) ByteArray2Object
				.convert2Object(TLV_V_PacketHeader.class, headerBuf, 0, OWSP_LEN.OwspPacketHeader);
		
	}
	
	/**
	 * 根据解析结果决定是否停止接收数据
	 * @param parsedResult
	 * @return
	 */
	private boolean decideWhether2RecvData(int parsedResult) {
		boolean result = false;
		switch (parsedResult) {
		case RECORD_EOF:
		case RECORDINFORS: // 接收录像文件数据之后，数据用于渲染时间轴，暂时退出接收过程
			breakDataProcess = true;
			result = true;
			break;
		case PlaybackActivity.ACTION_PAUSE_SUCC:
			result = true;
			break;	
		case PlaybackActivity.ACTION_RANDOM_SUCC:
			resumePlay = true;
			break;
		case LOGIN_SUC:
			breakDataProcess = true;
			isCanPlay = true;
			result = true;
			break;
		case LOGIN_FAIL:
			isCanPlay = false;
			result = true;
			break;
		case SEARCH_RECORD_FILE_NULL:
			breakDataProcess = true;
			result = true;
			break;
		case PlaybackActivity.ACTION_RESUME_FAIL:
		case PlaybackActivity.ACTION_PAUSE_FAIL:
		case PlaybackActivity.ACTION_RESUME_SUCC:
		default:
				break;
		}
		return result;
	}

	private ArrayList<TLV_V_RecordInfo> getRecordInfos(){
		return service.getRecordInfos();
	}

	private void resetArray(byte[] b) {
		int i = 0;
		for (i = 0; i < b.length; i++) {
			b[i] = 0;
		}
	}

	private byte[] makesureBufferEnough(byte[] buffer, int realSize) {
		byte[] result = buffer;
		int size = buffer.length;
		if (size < realSize) {
			buffer = null;
			buffer = new byte[(int) (realSize * 1.2)];
			result = buffer;
		}
		return result;
	}
	
	public void start() {
		timeThread.start();
		recvThread.start();
		resumePlay = true;
		breakDataProcess = false;
	}
	
	public void start(OWSPDateTime startTime) {
		controller.setChannel(getPlaybackChannel());
		controller.requestStart(startTime);
	}

	public void pause(){
		controller.setChannel(getPlaybackChannel());
		controller.requestPause();
	}
	
	public void resume(){
		controller.setChannel(getPlaybackChannel());
		controller.requestResume();
		resumePlay = true;
	}
	
	public void stop() {
		controller.setChannel(getPlaybackChannel());
		controller.requestStop();
	}
	
	public void random(OWSPDateTime startTime) {
		controller.setChannel(getPlaybackChannel());
		controller.requestRandom(startTime);
	}
	
	private interface STREAM_DATA_TYPE {
		public static final int VIDEO = 0;
		public static final int AUDIO = 1;
		public static final int MIXED = 2; // both video and audio
	}
	
	private interface STREAM_TYPE {
		public static final int STREAM_MAIN = 0;  // Main stream
		public static final int STREAM_SUB1 = 1;  // Sub stream 1
		public static final int STREAM_SUB2 = 2;  // Sub stream 2
		public static final int STREAM_VOD = 3;   // Record file stream
		public static final int MODE_SETTING = 4; // Remote set mode
	}
	
	public static class PlaybackRequest {
		private TLV_V_SearchRecordRequest searchRecordRequestInfo;
		private PlaybackDeviceItem deviceInfo;
		
		public TLV_V_SearchRecordRequest getSearchRecordRequestInfo() {
			return searchRecordRequestInfo;
		}
		public void setSearchRecordRequestInfo(TLV_V_SearchRecordRequest searchRecordRequestInfo) {
			this.searchRecordRequestInfo = searchRecordRequestInfo;
		}
		public PlaybackDeviceItem getDeviceInfo() {
			return deviceInfo;
		}
		public void setDeviceInfo(PlaybackDeviceItem deviceInfo) {
			this.deviceInfo = deviceInfo;
		}
	}
	
	
	private interface PlaybackCommand {
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
		
		public void requestResume() {
			OWSPDateTime startTime = new OWSPDateTime();
			sendCommand(PlaybackCommand.RESUME, startTime);
//			recvAndProcessData(receiver);
		}
		
		public void requestPause() {
			OWSPDateTime startTime = new OWSPDateTime();
			sendCommand(PlaybackCommand.PAUSE, startTime);
		}
		
		public void requestStop() {
			OWSPDateTime startTime = new OWSPDateTime();
			sendCommand(PlaybackCommand.STOP, startTime);
		}
		
		public void requestRandom(final OWSPDateTime startTime) {
			requestStart(startTime);
		}
		
		private void sendCommand(final int cmdCode, final OWSPDateTime startTime) {
			(new Thread() {
				@Override
				public void run() {
					Log.i(TAG, "========Thread start========");
					if (sender != null && channel != -1) {
						TLV_V_PlayRecordRequest prr = new TLV_V_PlayRecordRequest();
						prr.setDeviceId(0);
						startTime.setYear(startTime.getYear()-2009);
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
			Log.i(TAG, "========Thread end========");
		}
	}
	
//	private boolean isResume = false;
//	private boolean isPause = false;
//	
//	public void setResume(boolean isResume){
//		this.isResume = isResume;
//	}
//	public void setPause(boolean isPause){
//		this.isPause = isPause;
//	}
}

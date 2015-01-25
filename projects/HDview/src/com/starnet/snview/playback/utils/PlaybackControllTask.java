package com.starnet.snview.playback.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.starnet.snview.component.BufferSendManagerPlayBack;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.protocol.message.LoginResponse;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;

import android.content.Context;

public class PlaybackControllTask {
	
	private PlaybackControllTask(){
		
	}
	
	private static PlaybackControllTask instance;
	
	public static PlaybackControllTask getInstance(){
		if (instance==null) {
			return new PlaybackControllTask();
		}
		return instance;
	}

	private Context ctx;
	private Socket client;
	private boolean isConnected;
	private Thread controllThread;
	private DeviceItem visitDevItem;
	private SearchRecordRequest srr;
	private BufferSendManagerPlayBack back;

	public PlaybackControllTask(Context ctx, DeviceItem dItem) {
		this.ctx = ctx;
		this.visitDevItem = dItem;
		controllThread = new Thread() {
			@Override
			public void run() {
				super.run();
				initClient();
				if (isConnected) {
					playRecordRequesWork();
				}
			}
		};
	}

	private void initClient() {
		try {
			String host = visitDevItem.getSvrIp();
			int port = Integer.valueOf(visitDevItem.getSvrPort());
			client = new Socket(host, port);
			isConnected = client.isConnected();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void playRecordRequesWork() {
		try {
			if (isConnected) {
				PlayRecordRequest prr = new PlayRecordRequest();
				prr.setDeviceId(0);
				prr.setStartTime(srr.getStartTime());
				prr.setCommand(1);
				prr.setReserve(0);
				prr.setChannel(1);
				back.write(new OwspBegin());
				back.write(prr);
				back.write(new OwspEnd());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loginRequestWork() {
		try {
			if (isConnected) {
				
				back = BufferSendManagerPlayBack.getInstance();
				back.setOutStream(client.getOutputStream());
				LoginInfoRequest l = new LoginInfoRequest();
				l.setUserName(visitDevItem.getLoginUser());
				l.setPassword(visitDevItem.getLoginPass());
				l.setDeviceId(0);
				l.setFlag(1);
				l.setChannel(srr.getChannel());
				l.setDataType(2);// 音频和视频都包含
				l.setStreamMode(3);// 录像类型
				back.write(new OwspBegin());
				back.write(l);
				back.write(new OwspEnd());

				LoginResponse lr = parseLoginResponse(client.getInputStream());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private LoginResponse parseLoginResponse(InputStream inputStream) {
		LoginResponse lr = new LoginResponse();
		byte[] head = new byte[8];
		try {

			int index = inputStream.read(head);
			ByteBuffer headBuffer = ByteBuffer.allocate(8).wrap(head);
			headBuffer = headBuffer.order(ByteOrder.BIG_ENDIAN);
			int packetLen = headBuffer.getInt();// TLV的总长度

			byte[] tempRecordRsp = new byte[8];
			inputStream.read(tempRecordRsp);
			ByteBuffer tByteBuffer = ByteBuffer.wrap(tempRecordRsp).order(
					ByteOrder.LITTLE_ENDIAN);
			tByteBuffer.getShort();
			tByteBuffer.getShort();

			short reserve = tByteBuffer.getShort();
			short result = tByteBuffer.getShort();

			lr.setReserve(reserve);
			lr.setResult(result);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return lr;
	}

	public void start() {
		controllThread.start();
	}

}

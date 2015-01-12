package com.starnet.snview.playback.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import com.starnet.snview.component.BufferSendManager;
import com.starnet.snview.devicemanager.DeviceItem;
import com.starnet.snview.protocol.message.LoginRequest;
import com.starnet.snview.protocol.message.OwspBegin;
import com.starnet.snview.protocol.message.OwspEnd;
import com.starnet.snview.protocol.message.PhoneInfoRequest;
import com.starnet.snview.protocol.message.SearchRecordRequest;
import com.starnet.snview.protocol.message.VersionInfoRequest;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class PlayBackTask {

	private Context ctx;
	private Socket client;
	private Handler mHandler;
	private Thread loadThread;
	boolean isConnected = false;
	private SearchRecordRequest srr;
	private DeviceItem visitDevItem;
	private BufferSendManager sender;

	private LoginRequest loginRequest;

	public PlayBackTask(DeviceItem dItem, SearchRecordRequest srr) {
		this.srr = srr;
		this.visitDevItem = dItem;
		loadThread = new Thread() {
			@Override
			public void run() {
				super.run();
				initClient();

				if (isConnected) {
					 startWork();
//					testRequest();
				}

				if (isConnected) {
//					loginRequest();
				}
			}
		};
	}

	private void testRequest() {
		try {
			sender = BufferSendManager.getInstance();
			sender.setOutStream(client.getOutputStream());
			VersionInfoRequest v = new VersionInfoRequest();
			v.setVersionMajor(3);
			v.setVersionMinor(8);

			PhoneInfoRequest p = new PhoneInfoRequest();
			p.setEquipmentIdentity("");
			p.setEquipmentOS("Android");

			LoginRequest l = new LoginRequest();
			l.setUserName(visitDevItem.getLoginUser());
			l.setPassword(visitDevItem.getLoginPass());
			l.setDeviceId(1);
			l.setFlag(1);
			l.setChannel(1);
			l.setReserve(new int[] { 0, 0 });

			sender.write(new OwspBegin());
			sender.write(v);
			sender.write(p);
			sender.write(l);
			sender.write(new OwspEnd());
			
			InputStream in = client.getInputStream();
			String result = inputStreamToString(in);
			Log.v("result", result);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loginRequest() {
		try {
			sender = BufferSendManager.getInstance();
			sender.setOutStream(client.getOutputStream());
			if (isConnected) {
				sender.write(new OwspBegin());
				loginRequest.setFlag(1);
				loginRequest.setChannel(1);
				loginRequest.setReserve(new int[] { 3, 2 });
				loginRequest.setDeviceId(1);
				loginRequest.setPassword(visitDevItem.getLoginPass());
				loginRequest.setUserName(visitDevItem.getLoginUser());
				sender.write(loginRequest);
				sender.write(new OwspEnd());
				InputStream in = client.getInputStream();
				String result = inputStreamToString(in);
				Log.v("result", result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

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

	private void startWork() {
		try {
			sender = BufferSendManager.getInstance();
			sender.setOutStream(client.getOutputStream());
			if (isConnected) {
				sender.write(new OwspBegin());
				sender.write(srr);
				sender.write(new OwspEnd());
				InputStream in = client.getInputStream();
				String result = inputStreamToString(in);
				Log.v("result", result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String inputStreamToString(InputStream is) throws IOException {
		String s = "";
		String line = "";
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		// String lString = rd.readLine();
		while ((line = rd.readLine()) != null) {
			s += line;
		}
		return s;
	}

	public void setLoginReq(LoginRequest lr) {
		this.loginRequest = lr;
	}

	public void start() {
		loadThread.start();
	}
}

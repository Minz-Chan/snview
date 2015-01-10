package com.starnet.snview.playback.utils;

import java.io.IOException;
import java.net.Socket;

import com.starnet.snview.component.BufferSendManager;
import com.starnet.snview.protocol.message.SearchRecordRequest;

import android.content.Context;
import android.os.Handler;

public class PlayBackTask {
	
	private Context ctx;
	private Handler mHandler;
	private SearchRecordRequest srr;
	private BufferSendManager sender;
	private Thread loadThread;
	private Socket client;
	
	
	public PlayBackTask(SearchRecordRequest srr){
		this.srr = srr;
		loadThread = new Thread(){
			@Override
			public void run() {
				super.run();
				try {
					initClient();
					startWork();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	private void initClient(){
		client = new Socket();
		
	}
	
	private void startWork() throws IOException{
		sender = BufferSendManager.getInstance();
		sender.setOutStream(client.getOutputStream());
	}

	public void start(){
		loadThread.start();
	}
}

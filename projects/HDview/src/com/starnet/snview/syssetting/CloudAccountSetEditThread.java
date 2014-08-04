package com.starnet.snview.syssetting;

import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import com.starnet.snview.channelmanager.xml.CloudService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class CloudAccountSetEditThread extends Thread {

	private Handler handler;// 要处理的Handler
	private Message msg;
	private CloudService cloudService;

	private String domain;
	private String port;
	private String username;
	private String password;

	@Override
	public void run() {
		super.run();
		try {
			Document document = cloudService.SendURLPost(domain, port,username, password, "conn1");
			String status = cloudService.readXmlStatus(document);
			if (status == null) {
				msg.what = 1;
				handler.sendMessage(msg);
			} else {
				msg.what = 2;
				Bundle bundle = new Bundle();
				bundle.putString("status", status);
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		} catch (IOException e) {
			msg.what = 3;
			handler.sendMessage(msg);
		} catch (DocumentException e) {
			msg.what = 4;
			handler.sendMessage(msg);
		}
	}
	public CloudAccountSetEditThread(Handler handler, CloudService cloudService) {
		super();
		this.handler = handler;
		this.cloudService = cloudService;
	}
}
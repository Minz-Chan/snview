package com.video.hdview.playback.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.video.hdview.channelmanager.xml.MD5Util;
import com.video.hdview.devicemanager.DeviceItem;
import com.video.hdview.syssetting.CloudAccount;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

public class DeviceItemsTask extends AsyncTask<Void, Void, CloudAccount> {
	
	private final int TIMEOUT = 10;
	private CloudAccount cloudAccount;
	public void setCloudAccount(CloudAccount cloudAccount){
		this.cloudAccount = cloudAccount;
	}

	@Override
	protected CloudAccount doInBackground(Void... params) {
		HttpURLConnection httpURLConnection = sendDataToServer();
		CloudAccount cloudAccount = parseReturnXMLDataFromServer(httpURLConnection);
		return cloudAccount;
	}

	private CloudAccount parseReturnXMLDataFromServer(HttpURLConnection httpURLConnection) {
		CloudAccount cloudAccount = new CloudAccount();
		try {
			InputStream inStream = httpURLConnection.getInputStream();
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(inStream, "UTF-8");
				int eventType = parser.getEventType();// 产生第一个事件
				while (eventType != XmlPullParser.END_DOCUMENT) {
					DeviceItem deviceItem = null;
					switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						Log.i("DeviceItemsTask", "start tag...");
						deviceItem = new DeviceItem();
						break;
					case XmlPullParser.START_TAG:
						Log.i("DeviceItemsTask", "media tag...");
						String name = parser.getName();
						Log.i("DeviceItemsTask", "name:" + name);
						
						
						
						
						break;
					case XmlPullParser.END_TAG:
						Log.i("DeviceItemsTask", "end tag...");
						break;
					}
					eventType = parser.next();
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cloudAccount;
	}

	private HttpURLConnection sendDataToServer() {
		HttpURLConnection httpURLConnection = null;
		String domain = cloudAccount.getDomain();
		String port = cloudAccount.getPort();
		String password = cloudAccount.getPassword();
		String username = cloudAccount.getUsername();
		String ip = "http://"+domain+":"+port+"/xml_device-list";
		try {
			URL url = new URL(ip);
			try {
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setRequestMethod("POST");
				httpURLConnection.setDoOutput(true);
				httpURLConnection.setConnectTimeout(TIMEOUT*1000);
				httpURLConnection.setUseCaches(false);
				String encoded = MD5Util.md5Encode(password);// 为密码进行加密；
				StringBuffer param = new StringBuffer("wu=" + username + "&wp=" + encoded + "&pn="); // 请求URL的查询参数
				OutputStream os = httpURLConnection.getOutputStream();
				os.write(param.toString().getBytes()); // 将数据转化成byte数组，即字节数组；
				os.flush();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			//抛出网络不可达的情形
		}
		return httpURLConnection;
	}
}

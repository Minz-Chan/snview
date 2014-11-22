package com.starnet.snview.alarmmanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AlarmImageDownLoadTask {

	final String TAG = "AlarmImageDownLoadTask";

	private String imageUrl;
	private Context mContext;
	private Handler handler;

	private Thread imgeLoadThread;
	private Thread timeoutThread;
	private final int TIMEOUT = 7;
	private boolean isTimeOut;
	private boolean isCanceled;

	public AlarmImageDownLoadTask(String imageUrl, Context context) {
		this.imageUrl = imageUrl;
		this.mContext = context;

		timeoutThread = new Thread() {
			@Override
			public void run() {
				super.run();
				boolean canRun = true;
				int timeCount = 0;
				while (canRun) {
					try {
						Thread.sleep(1000);
						timeCount++;
						if (timeCount == TIMEOUT) {
							isTimeOut = true;
							canRun = false;
							if (!isCanceled) {
								onTimeOut();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		imgeLoadThread = new Thread() {
			@Override
			public void run() {
				super.run();
				try {

					byte[] data = startDownloadImage();// 比较耗时的操作
					Log.i(TAG, "data.length:" + data.length);
					if (!isCanceled && !isTimeOut) {
						if (getAlarmActivity().imgprogress != null) {
							getAlarmActivity().imgprogress.dismiss();
//							getAlarmActivity().imgprogress = null;
						}
						Message msg = new Message();
						Bundle bundle = new Bundle();
						bundle.putByteArray("image", data);
						msg.setData(bundle);
						handler.sendMessage(msg);
					}
				} catch (Exception e) {// 考虑异常的处理
					e.printStackTrace();
				}
			}
		};
	}

	protected byte[] startDownloadImage() throws Exception {
		byte[] data = null;
		URL url = new URL(imageUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(TIMEOUT * 1000);
		conn.setRequestMethod("GET");
		InputStream inStream = conn.getInputStream();
		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			data = readStream(inStream);
		}
		return data;
	}

	private byte[] readStream(InputStream inStream) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		inStream.close();
		return outStream.toByteArray();
	};

	public void start() {
		isTimeOut = false;
		isCanceled = false;
		imgeLoadThread.start();
		timeoutThread.start();
	}

	/**
	 * 超时处理函数
	 */
	protected void onTimeOut() {
		if (getAlarmActivity().imgprogress != null && !isCanceled) {
			getAlarmActivity().imgprogress.dismiss();
//			getAlarmActivity().imgprogress = null;
		}

	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}
	public void setCancel(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public AlarmActivity getAlarmActivity() {
		return (AlarmActivity) mContext;
	}
}
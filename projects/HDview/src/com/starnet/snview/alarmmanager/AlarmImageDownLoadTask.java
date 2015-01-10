package com.starnet.snview.alarmmanager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.starnet.snview.util.BitmapUtils;
import com.starnet.snview.util.SDCardUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@SuppressLint("SdCardPath")
public class AlarmImageDownLoadTask {

	private String imageUrl;
	private Handler handler;
	private Context mContext;
	private String deviceName;
	private boolean isTimeOut;
	private boolean isCanceled;
	private boolean timeoutover;
	private Thread timeoutThread;
	private Thread imgeLoadThread;
	private final int TIMEOUT = 7;
	// private boolean isDownloadSuc = true;
	private final int TIMOUTCODE = 0x0002;// 超时发送标志
	private final int LOADSUCCESS = 0x0004;
	private final int REQUESTCODE = 0x0023;
	private final int DOWNLOADFAILED = 0x0003;
	private boolean isDownloadFailOver = false;
	private boolean isStartDownloadOver = false;
	protected final String TAG = "AlarmImageDownLoadTask";

	// private String imagePath = "/mnt/sdcard/SNview/alarmImg";
	// private String imagePath;

	public AlarmImageDownLoadTask(String imageUrl, Context context,
			Handler handler) {

		this.handler = handler;
		this.mContext = context;
		this.imageUrl = imageUrl;
		timeoutThread = new Thread() {
			@Override
			public void run() {
				super.run();
				boolean canRun = true;
				int timeCount = 0;
				while (canRun && !timeoutover) {
					try {
						Thread.sleep(1000);
						timeCount++;
						if (timeCount == TIMEOUT) {
							canRun = false;
							if (!isCanceled && isTimeOut) {
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
			byte[] imgData = null;

			@Override
			public void run() {
				super.run();
				try {
					imgData = startDownloadImage();// 耗时操作
					sendDataToImgActivity(imgData);
				} catch (IOException e) {
					onDownloadFailed();// 下载失败的处理
				}
			}
		};
	}

	private void sendDataToImgActivity(byte[] imgData) {
		if (!isStartDownloadOver && !isCanceled) {
			if (imgData == null) {
				return;
			}
			Message msg = new Message();
			if (SDCardUtils.IS_MOUNTED) {// 保存下载的图像文件
				String[] urls = imageUrl.split("/");
				String imagename = urls[urls.length - 1];
				String appName = AlarmImageFileCache.getApplicationName2();
				String fImgPath = SDCardUtils.getSDCardPath() + appName;
				createMkdir(fImgPath);
				fImgPath = fImgPath + "/"+imagename;
				BitmapUtils.saveBmpFile(Utils.bytes2Bimap(imgData), fImgPath);
				Bundle data = new Bundle();
				data.putByteArray("image", imgData);
				msg.setData(data);
				msg.what = LOADSUCCESS;
				handler.sendMessage(msg);
			} else {
				Intent intent = new Intent();
				intent.putExtra("image", imgData);
				intent.setClass(mContext, AlarmImageActivity.class);
				getAlarmActivity().startActivityForResult(intent, REQUESTCODE);
			}
		}
		isStartDownloadOver = true;
		isDownloadFailOver = true;
		// isDownloadSuc = true;
		timeoutover = true;
	}

	/*** 下载失败的处理 **/
	protected void onDownloadFailed() {
		if (!isDownloadFailOver && !isCanceled) {
			Message msg = new Message();
			msg.what = DOWNLOADFAILED;
			handler.sendMessage(msg);
		}
		isStartDownloadOver = true;
		isDownloadFailOver = true;
		// isDownloadSuc = false;
		timeoutover = true;
	}
	// /*** 创建文件夹 ***/
	private void createMkdir(String imagePath) {
		File file = new File(imagePath);
		if (!file.exists() && !file.isDirectory()) {
			file.mkdir();
		}
	}

	/*** 开始从网络上下载数据 **/
	protected byte[] startDownloadImage() throws IOException {
		byte[] data = null;
		URL url = new URL(imageUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(TIMEOUT * 1000);
		InputStream inStream = conn.getInputStream();
		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			data = readStream(inStream);
		}
		return data;
	}

	/*** 从输入流中读取数据，并包装成byte数组 **/
	private byte[] readStream(InputStream inPutStream) throws IOException {
		int len = 0;
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		while ((len = inPutStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		inPutStream.close();
		return outStream.toByteArray();
	};

	public void start() {
		isTimeOut = false;
		isCanceled = false;
		timeoutover = false;
		isDownloadFailOver = false;
		isStartDownloadOver = false;
		imgeLoadThread.start();
		timeoutThread.start();
	}

	/**
	 * 超时处理函数
	 */
	protected void onTimeOut() {
		if (!isTimeOut && !isCanceled) {
			Message msg = new Message();
			msg.what = TIMOUTCODE;
			handler.sendMessage(msg);
		}
		isTimeOut = true;
		isDownloadFailOver = true;
		isStartDownloadOver = true;
	}

	public void setCancel(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public void setTitle(String deviceName) {
		this.deviceName = deviceName;
	}

	private AlarmContentActivity getAlarmActivity() {
		return (AlarmContentActivity) mContext;
	}

}
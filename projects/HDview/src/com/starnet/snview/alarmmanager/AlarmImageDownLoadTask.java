package com.starnet.snview.alarmmanager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.starnet.snview.util.BitmapUtils;
import com.starnet.snview.util.SDCardUtils;

import android.content.Context;
import android.content.Intent;

public class AlarmImageDownLoadTask {

	private String imageUrl;
	private Context mContext;
	private String deviceName;
	private boolean isTimeOut;
	private boolean isCanceled;
	private Thread timeoutThread;
	private Thread imgeLoadThread;
	private final int TIMEOUT = 7;
	private boolean isDownloadSuc = true;
	private final int REQUESTCODE = 0x0023;
	protected final String TAG = "AlarmImageDownLoadTask";

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
			byte[] imgData = null;

			@Override
			public void run() {
				super.run();
				try {
					imgData = startDownloadImage();// 耗时操作
				} catch (IOException e) {
					onDownloadFailed();// 下载失败的处理
				} finally {
					if (isDownloadSuc&&!isCanceled && !isTimeOut) {
						onStartFinished(imgData);
					}
				}
			}
		};
	}

	/***下载失败的处理**/
	protected void onDownloadFailed() {
		isDownloadSuc = false;
	}

	/***下载图像文件成功的处理**/
	protected void onStartFinished(byte[] imgData) {
		if (getAlarmActivity().imgprogress != null) {
			getAlarmActivity().imgprogress.dismiss();
		}
		if (imgData == null) {
			return;
		}		
		if (SDCardUtils.IS_MOUNTED) {// 保存下载的图像文件
			String[] urls = imageUrl.split("/");
			String imagename = urls[urls.length - 1];
			String fullImgPath = SDCardUtils.getSDCardPath() + imagename;
			BitmapUtils.saveBmpFile(Utils.bytes2Bimap(imgData), fullImgPath);
		}
		Intent intent = new Intent();
		intent.putExtra("isExist", false);
		intent.putExtra("image", imgData);
		intent.putExtra("title", deviceName);
		intent.putExtra("cancel", isCanceled);
		intent.putExtra("imageUrl", imageUrl);
		intent.setClass(getAlarmActivity(), AlarmImageActivity.class);
		getAlarmActivity().startActivityForResult(intent, REQUESTCODE);
	}

	/***开始从网络上下载数据**/
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
	/***从输入流中读取数据，并包装成byte数组**/
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
		imgeLoadThread.start();
		timeoutThread.start();
	}

	/**
	 * 超时处理函数
	 */
	protected void onTimeOut() {
		if (getAlarmActivity().imgprogress != null && !isCanceled) {
			getAlarmActivity().imgprogress.dismiss();
		}

	}
	
	public void setCancel(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public void setTitle(String deviceName) {
		this.deviceName = deviceName;
	}

	public AlarmActivity getAlarmActivity() {
		return (AlarmActivity) mContext;
	}
	
}
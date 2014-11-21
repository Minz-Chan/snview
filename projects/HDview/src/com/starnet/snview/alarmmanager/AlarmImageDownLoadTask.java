package com.starnet.snview.alarmmanager;

import android.content.Context;

public abstract class AlarmImageDownLoadTask {

	private String imageUrl;
	private Context mContext;

	private Thread imgeLoadThread;
	private Thread timeoutThread;
	private final int TIMEOUT = 7;
	private boolean isTimeOut;
	private boolean isCanceled = false;// 用户单击back键

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
	}

	/**
	 * 超时处理函数
	 */
	protected void onTimeOut() {

	}

}

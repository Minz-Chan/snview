package com.starnet.snview.alarmmanager;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.util.NetWorkUtils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

//需要考虑网络异常的情况
@SuppressLint("HandlerLeak")
public class NetDetectionReceiver extends BroadcastReceiver {

	public Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				serviceStatus = PUSH_SERVICE_STATUS.WORKING;
				break;
			case -1:
				serviceStatus = PUSH_SERVICE_STATUS.STOP;
				mStartPushServiceTask.cancel(false);
				break;
			}
		}
	};
	

	@Override
	public void onReceive(Context context, Intent intent) {

		if (context == null) {
			return;
		}
				
		SharedPreferences sp = context.getSharedPreferences("ALARM_PUSHSET_FILE", Context.MODE_PRIVATE);
		boolean hasPushServiceAccept = sp.getBoolean("isAllAccept", false);
		if(!hasPushServiceAccept){
			return;
		}
		
		boolean isOpen = NetWorkUtils.checkNetConnection(context);
		if (!isOpen) {
			PushManager.stopWork(context);
			return;
		}
		
		boolean isPushEnabled = PushManager.isPushEnabled(context);
		if (isPushEnabled) {
			return;
		}else {
			AlarmReceiver.mNetDetectionReceiver = this;
			mStartPushServiceTask = new StartPushServiceTask(context);
			mStartPushServiceTask.execute(new Object());
		}
	}
	
	private StartPushServiceTask mStartPushServiceTask;
	
	private enum ACTION {
		START_SERVICE,
		STOP_SERVICE
	}
	
	private ACTION currentAction;
	
	private enum PUSH_SERVICE_STATUS {
		INIT,	 // 初始
		WORKING, // 工作中
		STOP	 // 已停止
	}
	
	private PUSH_SERVICE_STATUS serviceStatus = PUSH_SERVICE_STATUS.INIT;
	
	public class StartPushServiceTask extends AsyncTask<Object, Object, Boolean> {
		
		private Context context;
		public StartPushServiceTask(Context context){
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			// 启动推送服务，同时显示加载框
			currentAction = ACTION.START_SERVICE;
			serviceStatus = PUSH_SERVICE_STATUS.INIT;
			PushManager.startWork(context, PushConstants.LOGIN_TYPE_API_KEY,Utils.getMetaValue(context.getApplicationContext(), "api_key"));
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			// 等待服务启动完成
			while (serviceStatus != PUSH_SERVICE_STATUS.WORKING && !isCancelled());
			Boolean startSuccess = true;
			if (isCancelled()) {  // 被取消说明启动失败
				startSuccess = false; 
			}
			return startSuccess;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// 服务启动成功，onPostExecute正常调用 
			if (!result) {
				mStartPushServiceTask = new StartPushServiceTask(context);
				mStartPushServiceTask.execute(new Object());
			}
		}

		@Override
		protected void onCancelled(Boolean result) {
			// 服务启动失败，onPostExecute不被调用，onCancelled被调用 
			mStartPushServiceTask = new StartPushServiceTask(context);
			mStartPushServiceTask.execute(new Object());
		}
	}
}
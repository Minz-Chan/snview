package com.starnet.snview.alarmmanager;

import java.util.ArrayList;
import java.util.List;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.util.NetWorkUtils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

//需要考虑网络异常的情况
@SuppressLint("HandlerLeak")
public class NetDetectionReceiver extends BroadcastReceiver {
	
	public static final int STARTWORKFLAG = 1;
	public static final int SETTAGFLAG = 2;
	public static final int DELTAGFLAG = 3;

	//需要辨识出是使用的setTag/delTag的返回值,还是startWork的返回值
	public Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			Bundle data = msg.getData();
			int flag = data.getInt("flag", 0);
			switch (flag) {
			case STARTWORKFLAG:
				int erroCode = data.getInt("errorCode", -1);
				if (erroCode == 0) {//startWork成功
					serviceStatus = PUSH_SERVICE_STATUS.WORKING;
					break;
				}else {//startWork失败，继续尝试开启服务
					serviceStatus = PUSH_SERVICE_STATUS.STOP;
					mStartPushServiceTask.cancel(false);
				}
				break;
			case SETTAGFLAG:
				erroCode = data.getInt("errorCode", -1);
				if (erroCode == 0) {//注册标签成功
					
					break;
				}else {//失败，继续注册标签服务
					
				}
				break;
			case DELTAGFLAG:
				erroCode = data.getInt("errorCode", -1);
				if (erroCode == 0) {//注销标签成功
					
					break;
				}else {//失败，继续注销标签服务
					
				}
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
			SharedPreferences sps = context.getSharedPreferences("alarmAccounts", Context.MODE_PRIVATE);
			String tag = sps.getString("tags", "");
			List<String>setTags = new ArrayList<String>();
			List<String>delTags = new ArrayList<String>();
			if (tag==null || tag.equals("") || tag.length() == 0) {
				
			}else {
				String tags[] = tag.split(",");
				for (int i = 0; i < tags.length; i++) {
					if (tags[i].contains("setTags")&&tags[i].contains("false")) {
						tags[i] = tags[i].replace("|setTags", "");
						tags[i] = tags[i].replace("|false", "");
						setTags.add(tags[i]);
					}else if (tags[i].contains("delTags")&&tags[i].contains("false")){
						tags[i] = tags[i].replace("|delTags", "");
						tags[i] = tags[i].replace("|delTags", "");
						delTags.add(tags[i]);
					}
				}
				//启动注册setTag的服务
				if (setTags.size() > 0) {
					
				}
				//启动注销delTag的服务
				if (delTags.size() > 0) {
					
				}
			}
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
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
import android.util.Log;

//需要考虑网络异常的情况
@SuppressLint("HandlerLeak")
public class NetDetectionReceiver extends BroadcastReceiver {
	
	private static final String TAG = "NetDetectionReceiver";
	//如果是true的话，应该删除掉，在保存的时候不需要再次保存了？？？？？
	
	public static final int STARTWORKFLAG = 1;
	public static final int SETTAGFLAG = 2;
	public static final int DELTAGFLAG = 3;
	
	private boolean isSetTagsWorking = true;//用于标记SetTagsServiceTask是否正在进行
	private SharedPreferences acceptInfoSp;
	private List<String>originTagsList = new ArrayList<String>();
	private boolean setTagsSuccess = false;
	private boolean delTagsSuccess = false;
	
	private List<String>setTags = new ArrayList<String>();
	private List<String>delTags = new ArrayList<String>();

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
					serviceStatus = PUSH_SERVICE_STATUS.STOP;
					break;
				}else {//startWork失败，继续尝试开启服务
					serviceStatus = PUSH_SERVICE_STATUS.WORKING;
					mStartPushServiceTask.cancel(false);
				}
				break;
			case SETTAGFLAG:
				setTagsSuccess = true;
				delTagsSuccess = false;
				erroCode = data.getInt("errorCode", -1);
				if (erroCode == 0) {//注册标签成功,则修改false为true，删除该账户的标记语句，以避免xml文档增长的过大
					serviceStatus = PUSH_SERVICE_STATUS.STOP;
					break;
				}else {//失败，继续注册标签服务
					serviceStatus = PUSH_SERVICE_STATUS.WORKING;
					mSetTagsServiceTask.cancel(false);
				}
				break;
			case DELTAGFLAG:
				setTagsSuccess = false;
				delTagsSuccess = true;
				erroCode = data.getInt("errorCode", -1);
				if (erroCode == 0) {//注销标签成功,则修改标记语句中的false为true，删除该账户的标记语句，以避免xml文档增长的过大
					serviceStatus = PUSH_SERVICE_STATUS.STOP;
					break;
				}else {//失败，继续注销标签服务
					serviceStatus = PUSH_SERVICE_STATUS.WORKING;
					mDelTagsServiceTask.cancel(false);
				}
				break;
			}
		}
	};
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.i(TAG,"===NetDetectionReceiver,,,,onReceive=====");

		if (context == null) {
			return;
		}				
		
		boolean isOpen = NetWorkUtils.checkNetConnection(context);
		if (!isOpen) {
			PushManager.stopWork(context);
			return;
		}
		
		if(!context.getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE).getBoolean(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, false)){
			return;
		}
		
		boolean isPushEnabled = PushManager.isPushEnabled(context);
		if (isPushEnabled) {
			
			AlarmReceiver.mNetDetectionReceiver = this;
			
			acceptInfoSp = context.getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, Context.MODE_PRIVATE);
			boolean isAllAcc = acceptInfoSp.getBoolean(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, true);
			boolean isAcc = acceptInfoSp.getBoolean(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, true);
			if (!isAllAcc||!isAcc) {
				return;
			}
			
			originTagsList = new ArrayList<String>();
			SharedPreferences sps = context.getSharedPreferences("alarmAccounts", Context.MODE_PRIVATE);
			String tag = sps.getString("tags", "");
			if (tag==null || tag.equals("") || tag.length() == 0) {}
			else {
				
				String tags[] = tag.split(",");
				if (tags == null || tags.length == 0) {
					return;
				}
				for (int i = 0; i < tags.length; i++) {
					if (tags[i].contains("setTags")&&tags[i].contains("false")) {
						tags[i] = tags[i].replace("|setTags", "");
						tags[i] = tags[i].replace("|false", "");
						setTags.add(tags[i]);
						originTagsList.add(tags[i]);
					}else if (tags[i].contains("delTags")&&tags[i].contains("false")){
						tags[i] = tags[i].replace("|delTags", "");
						tags[i] = tags[i].replace("|false", "");
						delTags.add(tags[i]);
						originTagsList.add(tags[i]);
					}
				}
				//启动注册setTag的服务,启动服务之后必须挂起等待返回结果，若是不挂起的话，极有可能会导致返回结果错乱的情况；
				//在获取保存信息的时候，修改
				if (setTags.size() > 0) {
					mSetTagsServiceTask = new SetTagsServiceTask(context, setTags);
					mSetTagsServiceTask.execute(new Object());
					Log.i(TAG,"===NetDetectionReceiver,,,,setTags=====");
				}
				//??????????应该卡住，以免同时修改文档造成文档错误
				//启动注销delTag的服
				if (delTags.size() > 0) {
					mDelTagsServiceTask = new DelTagsServiceTask(context, delTags);
					mDelTagsServiceTask.execute(new Object());
					Log.i(TAG,"===NetDetectionReceiver,,,,delTags=====");
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
	private SetTagsServiceTask mSetTagsServiceTask;
	private DelTagsServiceTask mDelTagsServiceTask;
	
	private enum ACTION {
		START_SERVICE,
		ADD_TAG,
		DEL_TAG
	}
	
	private ACTION currentAction;
	
	private enum PUSH_SERVICE_STATUS {
		INIT,	 // 初始
		WORKING, // 工作中
		STOP	 // 已停止
	}
	
	private PUSH_SERVICE_STATUS serviceStatus = PUSH_SERVICE_STATUS.INIT;
	
	
	private final class SetTagsServiceTask extends AsyncTask<Object, Object, Boolean>{
		
		private Context context;
		private List<String> setTags ;
		public SetTagsServiceTask(Context context,List<String> tags){
			this.context = context;
			this.setTags = tags;
		}
		
		@Override
		protected void onPreExecute() {
			// 启动推送服务，同时显示加载框
			currentAction = ACTION.ADD_TAG;
			serviceStatus = PUSH_SERVICE_STATUS.INIT;
			PushManager.setTags(context, setTags);
			isSetTagsWorking = true;
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			while (serviceStatus != PUSH_SERVICE_STATUS.WORKING && !isCancelled());
			Boolean startSuccess = true;
			if (isCancelled()) {  // 被取消说明启动失败
				startSuccess = false; 
			}
			return startSuccess;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// 注册服务不成功的情形，不修改xml文档中的内容，否则，修改为true
			while (!setTagsSuccess) { }
			if (!result) {//注册不成功，将原来的信息保存在xml文档中；
				//先清除
				saveOrignalInfo(originTagsList);
			}else{//成功时，去除setTags
				saveOrignalInfo(delTags,false);
			}
			isSetTagsWorking = false;//置为false，开启注销服务
		}

		@Override
		protected void onCancelled(Boolean result) {
			// 取消注册服务，不修改xml文档中的内容
			while (!setTagsSuccess) {}
			saveOrignalInfo(originTagsList);
			isSetTagsWorking = false;//置为false，开启注销服务
		}
	}
	
	private void saveOrignalInfo(List<String>tagList) {
		acceptInfoSp.edit().clear().commit();
		//重新组织tags
		String dTags = "";
		if((tagList==null)||(tagList.size()==0)){
			return;
		}
		int size = tagList.size();
		for (int i = 0; i < size - 1; i++) {
			String delTag = tagList.get(i);
			dTags += delTag + ",";
		}
		dTags = dTags + tagList.get(size - 1);
		acceptInfoSp.edit().putString("tags", dTags).commit();
	}
	
	private void saveOrignalInfo(List<String>tagList,boolean setTagFlag) {
		acceptInfoSp.edit().clear().commit();
		//重新组织tags
		String dTags = "";
		String tempTag = "";
		if((tagList==null)||(tagList.size()==0)){
			return;
		}
		String preFix = "";
		if (setTagFlag) {
			preFix = "|false|setTags"+",";
		}else{
			preFix = "|false|delTags"+",";
		}
		
		int size = tagList.size();
		for (int i = 0; i < size - 1; i++) {
			String delTag = tagList.get(i);
			if (i==0) {
				tempTag = delTag +preFix;
				dTags = tempTag;
			}else {
				dTags = dTags + preFix;
			}
		}
		dTags = dTags + tagList.get(size - 1);
		acceptInfoSp.edit().putString("tags", dTags).commit();
	}
	
	private final class DelTagsServiceTask extends AsyncTask<Object, Object, Boolean>{
		
		private Context context;
		private List<String> delTagList ;
		public DelTagsServiceTask(Context context,List<String> tags){
			this.context = context;
			this.delTagList = tags;
		}
		
		@Override
		protected void onPreExecute() {
			// 启动推送服务，同时显示加载框
			currentAction = ACTION.DEL_TAG;
			serviceStatus = PUSH_SERVICE_STATUS.INIT;
			PushManager.delTags(context, delTagList);
		}
		
		@Override
		protected Boolean doInBackground(Object... params) {
			while (serviceStatus != PUSH_SERVICE_STATUS.WORKING && !isCancelled());
			Boolean startSuccess = true;
			if (isCancelled()) {  // 被取消说明启动失败
				startSuccess = false; 
			}
			return startSuccess;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// 注册服务不成功的情形，不修改xml文档中的内容，否则，修改为true
			while(!delTagsSuccess) {}
			if (!result) {//注销不成功，将原来的信息保存在xml文档中；
				saveOrignalInfo(originTagsList);
			}else{
				saveOrignalInfo(setTags,true);
			}
			isSetTagsWorking = true;
		}

		@Override
		protected void onCancelled(Boolean result) {
			// 取消注册服务，不修改xml文档中的内容
			while(!setTagsSuccess) {}
			saveOrignalInfo(originTagsList);
			isSetTagsWorking = true;
		}
	}
	
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
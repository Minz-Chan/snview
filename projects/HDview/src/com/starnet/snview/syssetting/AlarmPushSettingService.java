package com.starnet.snview.syssetting;

import java.util.ArrayList;
import java.util.List;

import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.alarmmanager.AlarmSettingUtils;
import com.starnet.snview.util.MD5Utils;
import com.starnet.snview.util.ReadWriteXmlUtils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

@SuppressLint("SdCardPath")
public class AlarmPushSettingService extends Service {

	private boolean isAllAcc;
	private boolean isAccept;
	private static Context ctx;
	private List<CloudAccount> ps;
	private List<CloudAccount> ca;
	private SharedPreferences settingsSP;
	private final String TAG = "AlarmPushSettingService";
	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// getSettingsSP();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.v(TAG, "AlarmPushSettingService onStart......");
//		getSettingsSP();
//		if (!isAllAcc) {
//			if (ctx!=null) {
//				PushManager.stopWork(ctx.getApplicationContext());// 关闭百度推送服务
//			}
//		} else {
//			new Thread() {
//				@Override
//				public void run() {
//					while (isAccept && AlarmReceiver.relFlag == -1) {
//						getSettingsSP();
//						Log.v(TAG, "AlarmPushSettingService onStart relFlag ......");
//						try {
//							if (ctx!=null) {
//								saveAllAccounts();
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//					while (!isAccept && AlarmReceiver.delFlag == -1) {
//						getSettingsSP();
//						Log.v(TAG, "AlarmPushSettingService onStart delFlag......");
//						try {
//							if (ctx!=null) {
//								delPushUserAndSaveStarnetUser();
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}.start();
//		}
		// }
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "AlarmPushSettingService onStartCommand......");
		return super.onStartCommand(intent, flags, startId);
	}

	/** 删除推送账户，注册星云平台账户 **/
	private void delPushUserAndSaveStarnetUser() throws Exception {
		List<String> dTags = new ArrayList<String>();
		List<String> rTags = new ArrayList<String>();

		ps = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		ca = ReadWriteXmlUtils.getCloudAccountList(filePath);// 获取星云平台账户

		if (ps != null) {
			for (int i = 0; i < ps.size(); i++) {
				CloudAccount pA = ps.get(i);
				String uName = pA.getUsername();
				String pswd = MD5Utils.createMD5(pA.getPassword());
				String tag = uName + "" + pswd;
				dTags.add(tag);
			}
			PushManager.delTags(ctx.getApplicationContext(), dTags);// 删除推送账户
		}

		if (ca != null) {
			for (int i = 0; i < ca.size(); i++) {
				CloudAccount cA = ca.get(i);
				String uName = cA.getUsername();
				String pswd = MD5Utils.createMD5(cA.getPassword());
				String tag = uName + "" + pswd;
				rTags.add(tag);
			}
			PushManager.setTags(ctx.getApplicationContext(), rTags);// 注册星云平台账户
		}
	}

	private void saveAllAccounts() throws Exception {
		List<String> rTags = new ArrayList<String>();

		ps = ReadWriteXmlUtils.getAlarmPushUsersFromXML();
		ca = ReadWriteXmlUtils.getCloudAccountList(filePath);// 获取星云平台账户

		if (ps != null) {
			for (int i = 0; i < ps.size(); i++) {
				CloudAccount pA = ps.get(i);
				String uName = pA.getUsername();
				String pswd = MD5Utils.createMD5(pA.getPassword());
				String tag = uName + "" + pswd;
				rTags.add(tag);
			}
		}

		if (ca != null) {
			for (int i = 0; i < ca.size(); i++) {
				CloudAccount cA = ca.get(i);
				String uName = cA.getUsername();
				String pswd = MD5Utils.createMD5(cA.getPassword());
				String tag = uName + "" + pswd;
				rTags.add(tag);
			}
		}
		PushManager.setTags(ctx.getApplicationContext(), rTags);// 注册星云平台账户
	}

	/** 获取用户需要保存的设置选项 **/
	private void getSettingsSP() {
		// regOrDelSP = ctx.getSharedPreferences("PSXMLFILE", 0);
		// isAccept = regOrDelSP.getBoolean("regOrDelSP", true);
		if (ctx!=null) {
			settingsSP = ctx.getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, 0);
			isAccept = settingsSP.getBoolean(AlarmSettingUtils.ALARM_CONFIG_USER_ALARM, true);
			isAllAcc = settingsSP.getBoolean(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, true);
		}
	}

	private final IBinder binder = new MyBinder();

	public class MyBinder extends Binder {
		AlarmPushSettingService getService() {
			return AlarmPushSettingService.this;
		}
	}

	public static Context getCtx() {
		return ctx;
	}

	public static void setCtx(Context ctx) {
		AlarmPushSettingService.ctx = ctx;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "AlarmPushSettingService OVER......");
	}

}
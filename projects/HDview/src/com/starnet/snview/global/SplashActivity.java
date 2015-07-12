package com.starnet.snview.global;

import java.io.File;
import java.io.IOException;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.alarmmanager.AlarmReceiver;
import com.starnet.snview.alarmmanager.AlarmSettingUtils;
import com.starnet.snview.alarmmanager.Utils;
import com.starnet.snview.images.LocalFileUtils;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.syssetting.AlarmPushSettingService;
import com.starnet.snview.util.AssetsUtil;
import com.starnet.snview.util.FileUtility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {
	private final int DELAY_START_TIME = 2000; // 延时, 单位毫秒
	private SharedPreferences preferences;

	private boolean isShake;
	private boolean isSound;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.start_activity);

		GlobalApplication.getInstance()
				.setAppName(getString(R.string.app_name));		
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		new DelayStarTask().execute(new Object());
	}

	private void toGuideOrRealplayActivity() {		
		SharedPreferences sp = getSharedPreferences(AlarmSettingUtils.ALARM_CONFIG, 0);
		boolean isAccept = sp.getBoolean(AlarmSettingUtils.ALARM_CONFIG_GLOBAL_ALARM, true);
		isShake = sp.getBoolean(AlarmSettingUtils.ALARM_CONFIG_SHAKE, true);
		isSound = sp.getBoolean(AlarmSettingUtils.ALARM_CONFIG_SOUND, true);
		if (isAccept) {
			startBaiduPushService();
		}
		
		if (checkWhetherFirstStart()) { // 首次启动
			Intent intent = new Intent(this, GuideActivity.class);
			startActivity(intent);
			finish();
		} else {			
			Intent intent = new Intent(this, RealplayActivity.class);
			startActivity(intent);
			finish();
		}
	}
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings({ "deprecation" })
	private boolean checkWhetherFirstStart() {
		boolean isFirstStart = false;
		int count;
		
		preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
		count = preferences.getInt("count", 0);
		if (count == 0) {
			isFirstStart = true;
		}
		
		Editor editor = preferences.edit();
		editor.putInt("count", ++count);
		editor.commit();

		return isFirstStart;
	}

	private void startBaiduPushService() {
		AlarmSettingUtils.getInstance().init(this);
		AlarmPushSettingService.setCtx(SplashActivity.this);
		PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY,
				Utils.getMetaValue(SplashActivity.this, "api_key"));
		Resources resource = this.getResources();
	    String pkgName = this.getPackageName();
		CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
                resource.getIdentifier(
                        "notification_custom_builder", "layout", pkgName),
                resource.getIdentifier("notification_icon", "id", pkgName),
                resource.getIdentifier("notification_title", "id", pkgName),
                resource.getIdentifier("notification_text", "id", pkgName));
        cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
        if(isShake&&isSound){
        	cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND
                    | Notification.DEFAULT_VIBRATE);
        }else if (isSound) {
        	cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND);
		}else if (isShake) {
			cBuilder.setNotificationDefaults(Notification.DEFAULT_VIBRATE);
		}
        cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
        cBuilder.setLayoutDrawable(resource.getIdentifier(
                "simple_notification_icon", "drawable", pkgName));
        PushManager.setNotificationBuilder(this, 1, cBuilder);
	}
	
	class DelayStarTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			try {
				Thread.sleep(DELAY_START_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			toGuideOrRealplayActivity();
		}
	}
	
	class LoadDemoDataTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			File f = new File(LocalFileUtils.getLocalFileRootPath());

			if (!f.exists()) {
				f.mkdir();
			} else {
				try {
					FileUtility.deleteDirectory(f.getPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
				f.mkdir();
			}

			AssetsUtil.copyAssetsDir(SplashActivity.this, "thumbnails",
					LocalFileUtils.getLocalFileRootPath());
			AssetsUtil.copyAssetsDir(SplashActivity.this, "capture",
					LocalFileUtils.getLocalFileRootPath());
			AssetsUtil.copyAssetsDir(SplashActivity.this, "Picture",
					LocalFileUtils.getLocalFileRootPath());
			AssetsUtil.copyAssetsDir(SplashActivity.this, "record",
					LocalFileUtils.getLocalFileRootPath());
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
//			SplashActivity.this.dismissDialog(PROGRESS_DIALOG);
//			showMainActivity();
			super.onPostExecute(result);
		}
	}

	@Override
	public void onBackPressed() {
	}
}

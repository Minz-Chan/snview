package com.starnet.snview.global;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.alarmmanager.AlarmPersistenceUtils;
import com.starnet.snview.alarmmanager.Utils;
import com.starnet.snview.images.LocalFileUtils;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.realplay.RefreshDeviceConnectionInfo;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.AssetsUtil;
import com.starnet.snview.util.FileUtility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {
	private final int SPLASH_DISPLAY_LENGTH = 2000; // 延时5秒

	private SharedPreferences preferences;
	private final int mHandler_close = 11;
	Intent mainIntent;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case mHandler_close:
				preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
				int count = preferences.getInt("count", 0);
				// Intent mainIntent;
				if (count == 1) {
					mainIntent = new Intent(SplashActivity.this,GuideActivity.class);
					mainIntent.putExtra("guide", "guide");
				} else {
					mainIntent = new Intent(SplashActivity.this,RealplayActivity.class);
					Bundle bundle = msg.getData();
					if (bundle!=null) {
						ArrayList<PreviewDeviceItem> devices = bundle.getParcelableArrayList("previewItems");
						mainIntent.putParcelableArrayListExtra("previewItems", devices);
					}
				}
				SplashActivity.this.startActivity(mainIntent);
				SplashActivity.this.finish();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// 隐去标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 隐去状态栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.start_activity);

		GlobalApplication.getInstance()
				.setAppName(getString(R.string.app_name));

		if (!CheckWhetherFirstStart()) {
//			showMainActivity();
		}
		preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
		int count = preferences.getInt("count", 0);
		// Intent mainIntent;
		if (count == 1) {
			mainIntent = new Intent(SplashActivity.this,GuideActivity.class);
			SplashActivity.this.startActivity(mainIntent);
			SplashActivity.this.finish();
		}else {
			new RefreshDeviceConnectionInfo(this,mHandler).start();//更新预览通道数据
		}
		AlarmPersistenceUtils.flag_start = true;
		startBaiduPushService();
	}

	private void startBaiduPushService() {
		initWithApiKey();//// 以apikey的方式绑定
		PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY,Utils.getMetaValue(SplashActivity.this, "api_key"));
		Resources resource = this.getResources();
	    String pkgName = this.getPackageName();
		CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
                getApplicationContext(), resource.getIdentifier(
                        "notification_custom_builder", "layout", pkgName),
                resource.getIdentifier("notification_icon", "id", pkgName),
                resource.getIdentifier("notification_title", "id", pkgName),
                resource.getIdentifier("notification_text", "id", pkgName));
        cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
        cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE);
        cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
        cBuilder.setLayoutDrawable(resource.getIdentifier(
                "simple_notification_icon", "drawable", pkgName));
        PushManager.setNotificationBuilder(this, 1, cBuilder);
	}

	protected void showMainActivity() {
		mHandler.postDelayed(new Runnable() {
			public void run() {
//				// 判断是否为第一次进入，如果是第一次进入时，则显示滑动页面；否则显示RealplayActivity界面
//				preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
//				int count = preferences.getInt("count", 0);
//				// Intent mainIntent;
//				if (count == 1) {
//					mainIntent = new Intent(SplashActivity.this,GuideActivity.class);
//				} else {
//					mainIntent = new Intent(SplashActivity.this,RealplayActivity.class);
//				}
////				SplashActivity.this.startActivity(mainIntent);
////				SplashActivity.this.finish();
			}
		}, SPLASH_DISPLAY_LENGTH);
	}
	
	protected boolean isAllCloudAccountEnabled(List<CloudAccount> cloudAccounts){
		boolean result = true;
		if(cloudAccounts == null){
			return false;
		}
		if((cloudAccounts != null)&&(cloudAccounts.size()>1)){
			for(int i =1;i<cloudAccounts.size();i++){
				if(!cloudAccounts.get(i).isEnabled()){
					result = false;
					break;
				}
			}
		}
		return result;
	}

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings({ "deprecation", "unchecked" })
	private boolean CheckWhetherFirstStart() {
		boolean isFirstStart = false;

		// 读取SharedPreferences中需要的数据
		preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
		int count = preferences.getInt("count", 0);
		// 判断程序与第几次运行,如果是第一次运行则跳转到引导页面
		if (count == 0) {
			LoadDemoDataAsync loadtask = new LoadDemoDataAsync();
			loadtask.execute();
			
			showDialog(RefreshDeviceConnectionInfo.REFRESH_CLOUDACCOUT_PROCESS_DIALOG);

			isFirstStart = true;
		}
		
		Editor editor = preferences.edit();
		editor.putInt("count", ++count);
		editor.commit();

		return isFirstStart;
	}

	private static final int PROGRESS_DIALOG = 1;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case RefreshDeviceConnectionInfo.REFRESH_CLOUDACCOUT_PROCESS_DIALOG:
			ProgressDialog progress = ProgressDialog.show(this, "",
					getString(R.string.realplay_updating_devicedata_wait),
					true, true);
			progress.setOnCancelListener(new OnCancelListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(RefreshDeviceConnectionInfo.REFRESH_CLOUDACCOUT_PROCESS_DIALOG);
				}
			});
			return progress;
			
		default:
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	private class LoadDemoDataAsync extends AsyncTask {

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

		@SuppressWarnings({ "deprecation", "unchecked" })
		@Override
		protected void onPostExecute(Object result) {
//			SplashActivity.this.dismissDialog(PROGRESS_DIALOG);
//			showMainActivity();
			super.onPostExecute(result);
		}
	}
	// 以apikey的方式绑定
    private void initWithApiKey() {
        // Push: 无账号初始化，用api key绑定
    	int api_key = PushConstants.LOGIN_TYPE_API_KEY;
    	String api_keys = Utils.getMetaValue(SplashActivity.this, "api_key");
        PushManager.startWork(getApplicationContext(),api_key,api_keys);
    }
}

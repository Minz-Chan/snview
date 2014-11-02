package com.starnet.snview.global;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.dom4j.Document;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountUtil;
import com.starnet.snview.channelmanager.xml.CloudService;
import com.starnet.snview.channelmanager.xml.CloudServiceImpl;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.images.LocalFileUtils;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.realplay.RealplayActivityUtils;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.AssetsUtil;
import com.starnet.snview.util.CloudAccountUtils;
import com.starnet.snview.util.FileUtility;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.PreviewItemXMLUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {
	private final int SPLASH_DISPLAY_LENGTH = 2000; // 延时5秒

	private SharedPreferences preferences;

	private List<CloudAccount> groupList;
	private final int mHandler_close = 11;
	private final int mHandler_initial = 12;
	Intent mainIntent;
	private final int exception_num = 2;
	
	private boolean timer_flag = true;
	private long star_time;
	private Timer timer = new Timer(true);
	private final int timeOut = 5000;
	TimerTask timeTask = new TimerTask(){
		@Override
		public void run() {
			while(timer_flag){
				long end_time = System.currentTimeMillis();
				if(end_time - star_time >=  timeOut){
					mHandler.sendEmptyMessage(timeOut);
				}
			}
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case mHandler_close:
				SplashActivity.this.startActivity(mainIntent); // 启动MainActivity
				SplashActivity.this.finish(); // 结束SplashActivity
				break;
			case mHandler_initial:
				List<PreviewDeviceItem> devices = PreviewItemXMLUtils.getPreviewItemListInfoFromXML(getString(R.string.common_last_devicelist_path));
				RealplayActivityUtils.setSelectedAccDevices(devices, groupList);
				SplashActivity.this.startActivity(mainIntent); // 启动MainActivity
				SplashActivity.this.finish(); // 结束SplashActivity
				break;
			case timeOut:
				SplashActivity.this.startActivity(mainIntent); // 启动MainActivity
				SplashActivity.this.finish(); // 结束SplashActivity
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
			showMainActivity();
		}

		// Animation animatinoGone =
		// AnimationUtils.loadAnimation(this,R.anim.alpha_gone);

	}

	private void showMainActivity() {
		// new Handler().postDelayed(new Runnable() {
		mHandler.postDelayed(new Runnable() {
			public void run() {
				// 判断是否为第一次进入，如果是第一次进入时，则显示滑动页面；否则显示RealplayActivity界面
				preferences = getSharedPreferences("count", MODE_WORLD_READABLE);
				int count = preferences.getInt("count", 0);
				// Intent mainIntent;
				if (count == 1) {
					mainIntent = new Intent(SplashActivity.this,
							GuideActivity.class);
				} else {
					mainIntent = new Intent(SplashActivity.this,
							RealplayActivity.class);
				}

				Thread thread = new Thread() {
					@Override
					public void run() {
						super.run();
						boolean isNetOpen = NetWorkUtils.checkNetConnection(getApplicationContext());
						if (isNetOpen) {
							CloudAccountUtil caUtil = new CloudAccountUtil();
							groupList = caUtil.getCloudAccountInfoFromUI();

							long start_time = System.currentTimeMillis();
							if (groupList != null && groupList.size() > 1) {
								star_time = System.currentTimeMillis();
//								timer.schedule(timeTask, 1);//开启计时器
								// List<PreviewDeviceItem> devices =
								// PreviewItemXMLUtils.getPreviewItemListInfoFromXML(getString(R.string.common_last_devicelist_path));
								for (int i = 1; i < groupList.size(); i++) {
									CloudAccount iCloudAccount = groupList.get(i);
									// 利用用户信息访问网络
									try {
										if (iCloudAccount.isEnabled()) {
											CloudService cloudService = new CloudServiceImpl("conn");

											String domain = iCloudAccount.getDomain();
											String port = iCloudAccount.getPort();
											String username = iCloudAccount.getUsername();
											String password = iCloudAccount.getPassword();
											String deviceName = "deviceName";

											Document doc = cloudService.SendURLPost(domain, port,username, password,deviceName);
											String requestStatus = cloudService.readXmlStatus(doc);
											if (requestStatus == null) {//连接获取数据成功
												List<DVRDevice> dvrDevices = cloudService.readXmlDVRDevices(doc);// 获取到设备
												CloudAccountUtils utils = new CloudAccountUtils();
												iCloudAccount = utils.getCloudAccountFromDVRDevice(SplashActivity.this,dvrDevices);

												iCloudAccount.setDomain(domain);
												iCloudAccount.setPassword(password);
												iCloudAccount.setPort(port);
												iCloudAccount.setUsername(username);

												groupList.set(i, iCloudAccount);
											}else{
												if(i == groupList.size() - 1){
													SplashActivity.this.startActivity(mainIntent); // 启动MainActivity
													SplashActivity.this.finish(); // 结束SplashActivity
													return;
												}
											}
										}
									} catch (Exception e) {// 网络访问异常的情况,使用未经修改的数据...
										e.printStackTrace();
										mainIntent.putExtra("exception_num",exception_num);
										SplashActivity.this.startActivity(mainIntent); // 启动MainActivity
										SplashActivity.this.finish(); // 结束SplashActivity
										break;
									} finally {
										long end_time = System.currentTimeMillis();
										long time = (end_time - start_time) / 1000;
										if (time >= 5) {// 超过5秒没有或得数据的话也要结束...
											SplashActivity.this.startActivity(mainIntent); // 启动MainActivity
											SplashActivity.this.finish(); // 结束SplashActivity
											return;
										}
										if (i == groupList.size() - 1) {
											mHandler.sendEmptyMessage(mHandler_initial);
											break;
										}
									}
								}
							} else {//没有星云账户的情况
								SplashActivity.this.startActivity(mainIntent); // 启动MainActivity
								SplashActivity.this.finish(); // 结束SplashActivity
							}
						} else {// 网络断开的情况
							mHandler.sendEmptyMessage(mHandler_close);
						}
					}
				};
				thread.start();
			}
		}, SPLASH_DISPLAY_LENGTH);
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

			showDialog(PROGRESS_DIALOG);

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
		case PROGRESS_DIALOG:
			ProgressDialog progressDialog = ProgressDialog.show(this, "",
					"演示数据加载中...", true, true);
			progressDialog.setCancelable(false);
			// progressDialog.setOnCancelListener(new OnCancelListener(){
			// @Override
			// public void onCancel(DialogInterface dialog) {
			// //dismissDialog(PROGRESS_DIALOG);
			//
			// }
			// });
			return progressDialog;
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
					// TODO Auto-generated catch block
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
			SplashActivity.this.dismissDialog(PROGRESS_DIALOG);
			showMainActivity();

			super.onPostExecute(result);
		}

	}
	
	protected void canceTimer(){
//		if(timer != null){
//			timer.cancel();
//			timer = null;
//		}
	}
}

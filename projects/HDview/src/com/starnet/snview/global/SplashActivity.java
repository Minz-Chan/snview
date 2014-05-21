package com.starnet.snview.global;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.starnet.snview.R;
import com.starnet.snview.images.LocalFileUtils;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.AssetsUtil;
import com.starnet.snview.util.FileUtility;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SplashActivity extends Activity{
	private final int SPLASH_DISPLAY_LENGTH = 2000;  //延时5秒
	
	private SharedPreferences preferences;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//隐去标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//隐去状态栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.start_activity);
		
		
		GlobalApplication.getInstance().setAppName(getString(R.string.app_name));
		
		
		if (!CheckWhetherFirstStart()) {
			showMainActivity();
		}
		
		//Animation animatinoGone = AnimationUtils.loadAnimation(this,R.anim.alpha_gone);
		
		
	}
	
	private void showMainActivity() {
		new Handler().postDelayed(new Runnable(){
			public void run(){
				Intent  mainIntent = new Intent(SplashActivity.this, RealplayActivity.class);
				
				SplashActivity.this.startActivity(mainIntent);	// 启动MainActivity
				SplashActivity.this.finish();					// 结束SplashActivity
			}
		}, SPLASH_DISPLAY_LENGTH);
	}
	
	private boolean CheckWhetherFirstStart() {
		boolean isFirstStart = false;
		
		//读取SharedPreferences中需要的数据
		preferences = getSharedPreferences("count",MODE_WORLD_READABLE);
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
		switch(id) { 
		case PROGRESS_DIALOG:			
			ProgressDialog progressDialog = ProgressDialog.show(this, "", "演示数据加载中...", true,true);
			progressDialog.setCancelable(false);
//			progressDialog.setOnCancelListener(new OnCancelListener(){
//				@Override
//				public void onCancel(DialogInterface dialog) {
//					//dismissDialog(PROGRESS_DIALOG);
//					
//				}
//			});
			return progressDialog;
		default: return null; 
		} 
	} 
	
	
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
			
			AssetsUtil.copyAssetsDir(SplashActivity.this, "thumbnails", LocalFileUtils.getLocalFileRootPath());
			AssetsUtil.copyAssetsDir(SplashActivity.this, "capture", LocalFileUtils.getLocalFileRootPath());
			AssetsUtil.copyAssetsDir(SplashActivity.this, "Picture", LocalFileUtils.getLocalFileRootPath());
			AssetsUtil.copyAssetsDir(SplashActivity.this, "record", LocalFileUtils.getLocalFileRootPath());
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			SplashActivity.this.dismissDialog(PROGRESS_DIALOG);
			showMainActivity();
			
			super.onPostExecute(result);
		}

	}
}

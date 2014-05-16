package com.starnet.hdview.global;

import com.starnet.hdview.R;
import com.starnet.hdview.realplay.RealplayActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SplashActivity extends Activity{
	private final int SPLASH_DISPLAY_LENGTH = 5000;  //延时5秒
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//隐去标题栏
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//隐去状态栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.start_activity);
		
		//Animation animatinoGone = AnimationUtils.loadAnimation(this,R.anim.alpha_gone);
		
		new Handler().postDelayed(new Runnable(){
			public void run(){
				Intent  mainIntent = new Intent(SplashActivity.this, RealplayActivity.class);
				
				SplashActivity.this.startActivity(mainIntent);  //启动MainActivity
				SplashActivity.this.finish();   //结束SplashActivity
			}
		}, SPLASH_DISPLAY_LENGTH);
	}
}

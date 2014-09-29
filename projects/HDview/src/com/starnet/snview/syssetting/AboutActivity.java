package com.starnet.snview.syssetting;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class AboutActivity extends BaseActivity {
	private static final String TAG = "AboutActivity";
	
	private String version ;
	private TextView about_version_txt;
	private ImageView about_version_img;
	
	private String mIntroductionContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_about_activity);
		
		initView();
		setListeners();
		setContentsForWagets();
	}
	
	protected void setContentsForWagets(){
		mIntroductionContent = "";
	}
	
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_about));
		super.hideExtendButton();
		super.hideRightButton();
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		
		about_version_img = (ImageView) findViewById(R.id.introduction_img);
		about_version_txt = (TextView) findViewById(R.id.introduction_txt);
		
		try {
			PackageInfo mPackageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = mPackageInfo.versionName;
			about_version_txt.setText("Version "+version+" "+getString(R.string.system_setting_about_build_time));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AboutActivity.this.finish();
			}
		});
	}
	
}

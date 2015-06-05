package com.video.hdview.syssetting;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
//import android.widget.ImageView;
import android.widget.TextView;

import com.video.hdview.R;
import com.video.hdview.component.BaseActivity;

public class AboutActivity extends BaseActivity {
	
	private TextView about_version_txt;
//	private ImageView about_version_img;
	
//	String mIntroductionContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_about_activity);
		
		initView();
		setListeners();
//		setContentsForWagets();
	}
	
//	private void setContentsForWagets(){
//		mIntroductionContent = "";
//	}
	
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_about));
		super.hideExtendButton();
		super.hideRightButton();
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		
//		about_version_img = (ImageView) findViewById(R.id.introduction_img);
		about_version_txt = (TextView) findViewById(R.id.introduction_txt);
		about_version_txt.setText(getString(R.string.system_setting_about_build));
		
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

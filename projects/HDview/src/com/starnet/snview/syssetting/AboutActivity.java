package com.starnet.snview.syssetting;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class AboutActivity extends BaseActivity {
	private static final String TAG = "AboutActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_about_activity);
		
		initView();
		setListeners();
	}
	
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_about));
		super.hideExtendButton();
		super.hideRightButton();
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
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

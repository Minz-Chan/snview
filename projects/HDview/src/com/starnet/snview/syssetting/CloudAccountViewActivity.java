package com.starnet.snview.syssetting;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class CloudAccountViewActivity extends BaseActivity {
	private static final String TAG = "CloudAccountViewActivity";

	private ListView mNetWorkSettingList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_activity);
		
		initView();
		setListeners();
	}
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_network_setting));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		
		mNetWorkSettingList = (ListView) findViewById(R.id.cloudaccount_listview);
		ArrayList<CloudAccount> cloudaccountList = new ArrayList<CloudAccount>();
		
		CloudAccount c1 = new CloudAccount();
		c1.setUsername("xwrj");
		c1.setEnabled(true);
		
		CloudAccount c2 = new CloudAccount();
		c2.setUsername("jtpt");
		c2.setEnabled(false);
		
		CloudAccount c3 = new CloudAccount();
		c3.setUsername("why");
		c3.setEnabled(true);
		
		cloudaccountList.add(c1);
		cloudaccountList.add(c2);
		cloudaccountList.add(c3);
		
		mNetWorkSettingList.setAdapter(new CloudAccountAdapter(this, cloudaccountList));
	}
	
	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloudAccountViewActivity.this.finish();
			}
		});
		
		super.getRightButton().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gotoCloudAccountSetting();
			}
		});
	}
	
	protected void gotoCloudAccountSetting() {
		Intent intent = new Intent();
        intent.setClass(CloudAccountViewActivity.this, CloudAccountSettingActivity.class); 
        startActivity(intent);
	}
}

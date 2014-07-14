package com.starnet.snview.devicemanager;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class DeviceViewActivity extends BaseActivity {
	private static final String TAG = "DeviceViewActivity";
	
	private ListView mDeviceList;
	private Button navigation_bar_add_btn;//zk
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.device_manager_activity);
		
		initView();
	}
	
	
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_device_management));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		
		mDeviceList = (ListView) findViewById(R.id.device_listview);
		navigation_bar_add_btn = (Button) findViewById(R.id.base_navigationbar_right_btn);//zk
		
		ArrayList<DeviceItem> deviceList = new ArrayList<DeviceItem>();
		
		DeviceItem d1 = new DeviceItem();
		d1.setDeviceName("公司门口");
		d1.setSecurityProtectionOpen(true);
		
		DeviceItem d2 = new DeviceItem();
		d2.setDeviceName("小区门口");
		d2.setSecurityProtectionOpen(false);
		
		DeviceItem d3 = new DeviceItem();
		d3.setDeviceName("上海");
		d3.setSecurityProtectionOpen(true);
		
		deviceList.add(d1);
		deviceList.add(d2);
		deviceList.add(d3);
		navigation_bar_add_btn.setOnClickListener(new OnClickListener() {//zk
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent();
				intent.setClass(DeviceViewActivity.this, DevicesAddActivity.class);
				startActivity(intent);
			}
		});
		mDeviceList.setAdapter(new DeviceListAdapter(this, deviceList));
	}

}

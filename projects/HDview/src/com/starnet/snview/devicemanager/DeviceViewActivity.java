package com.starnet.snview.devicemanager;

import java.util.ArrayList;

import android.os.Bundle;
import android.widget.ListView;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class DeviceViewActivity extends BaseActivity {
	private static final String TAG = "DeviceViewActivity";
	
	private ListView mDeviceList;
	

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
		
		mDeviceList.setAdapter(new DeviceListAdapter(this, deviceList));
	}

}

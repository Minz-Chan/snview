package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;

public class DeviceViewActivity extends BaseActivity {
	private static final String TAG = "DeviceViewActivity";
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
	private CloudAccountXML caxml;
	
	private ListView mDeviceList;
	private Button navigation_bar_add_btn;//zk
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.device_manager_activity);
		
		initView();
		
		mDeviceList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				//进入该设备的信息界面
				
			}
		});
	}
	
	
	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_device_management));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		
		caxml = new CloudAccountXML();
		mDeviceList = (ListView) findViewById(R.id.device_listview);
		navigation_bar_add_btn = (Button) findViewById(R.id.base_navigationbar_right_btn);//zk
		
		try {
			List<DeviceItem> addDeviceList = caxml.getCollectDeviceListFromXML(filePath);
			mDeviceList.setAdapter(new DeviceListAdapter(this, addDeviceList));
			int size = addDeviceList.size();
			System.out.println(size);
		} catch (Exception e) {
			e.printStackTrace();
		}
		navigation_bar_add_btn.setOnClickListener(new OnClickListener() {//zk
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent();
				intent.setClass(DeviceViewActivity.this, DeviceCollectActivity.class);
				startActivity(intent);
			}
		});
//		mDeviceList.setAdapter(new DeviceListAdapter(this, deviceList));//陈明珍原始数据
	}

}

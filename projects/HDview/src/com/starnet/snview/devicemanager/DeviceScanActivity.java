package com.starnet.snview.devicemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class DeviceScanActivity extends BaseActivity {
	
	private EditText record_et;
	private EditText server_et;
	private EditText port_et;
	private EditText username_et;
	private EditText password_et;
	private EditText defaultChannel_et;
	private EditText channelnumber_et;
	
	private DeviceItem clickDeviceItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manager_scan_acitivity);
		superChangeViewFromBase();
		
		super.getLeftButton().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				DeviceScanActivity.this.finish();
			}
		});
	}

	private void superChangeViewFromBase() {
		super.setRightButtonBg(R.drawable.device_manager_edit);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText("设备管理");
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		
		record_et = (EditText) findViewById(R.id.et_device_add_record);
		server_et = (EditText) findViewById(R.id.et_device_add_server);
		port_et = (EditText) findViewById(R.id.et_device_add_port);
		username_et = (EditText) findViewById(R.id.et_device_add_username);
		
		password_et = (EditText) findViewById(R.id.et_device_add_password);
		defaultChannel_et = (EditText) findViewById(R.id.et_device_add_defaultChannel);
		channelnumber_et = (EditText) findViewById(R.id.et_device_add_channelnumber);
		
		record_et.setKeyListener(null);
		server_et.setKeyListener(null);
		port_et.setKeyListener(null);
		username_et.setKeyListener(null);
		
		password_et.setKeyListener(null);
		defaultChannel_et.setKeyListener(null);
		channelnumber_et.setKeyListener(null);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		clickDeviceItem = (DeviceItem) bundle.getSerializable("clickDeviceItem");
		
		String deviceName = clickDeviceItem.getDeviceName();
		String loginPass = clickDeviceItem.getLoginPass();
		String loginUser = clickDeviceItem.getLoginUser();
		String channelSum = clickDeviceItem.getChannelSum();
		String defaultChannel = String.valueOf(clickDeviceItem.getDefaultChannel());
		
		String svrIp = clickDeviceItem.getSvrIp();
		String svrPort = clickDeviceItem.getSvrPort();
		
		record_et.setText(deviceName);
		server_et.setText(svrIp);
		port_et.setText(svrPort);
		username_et.setText(loginUser);
		password_et.setText(loginPass);
		defaultChannel_et.setText(defaultChannel);
		channelnumber_et.setText(channelSum);
	}
}
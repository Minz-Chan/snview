package com.starnet.snview.devicemanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;

@SuppressLint("SdCardPath")
public class DeviceInfoActivity extends BaseActivity {

	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";//用于保存收藏设备...
	private DeviceItem saveDeviceItem;
	
//	private EditText choose_et;
	private EditText et_device_add_record;
	private EditText et_device_add_server;
	private EditText et_device_add_port;
	
	private EditText et_device_add_username;
	private EditText et_device_add_password;
	private EditText et_device_add_defaultChannel;
	private EditText et_device_add_channelnumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manage_add_baseactivity);
		
		superExtendsAndHidenView();
		
		findViewsAndInitial();
		
		setClickListeners();
	}

	private void findViewsAndInitial() {
//		et_device_add_record = (EditText) findViewById(R.id.et_device_add_record);
		et_device_add_server = (EditText) findViewById(R.id.et_device_add_server);
		et_device_add_port = (EditText) findViewById(R.id.et_device_add_port);
		et_device_add_record = (EditText) findViewById(R.id.et_device_add_record);
		
		et_device_add_username = (EditText) findViewById(R.id.et_device_add_username);
		et_device_add_password = (EditText) findViewById(R.id.et_device_add_password);
		et_device_add_defaultChannel = (EditText) findViewById(R.id.et_device_add_defaultChannel);
		et_device_add_channelnumber = (EditText) findViewById(R.id.et_device_add_channelnumber);
		
		if(saveDeviceItem != null){
			et_device_add_server.setText(saveDeviceItem.getSvrIp());
			et_device_add_port.setText(saveDeviceItem.getSvrPort());
			et_device_add_record.setText(saveDeviceItem.getDeviceName());
			et_device_add_username.setText(saveDeviceItem.getLoginUser());
			et_device_add_password.setText(saveDeviceItem.getLoginPass());
			et_device_add_defaultChannel.setText(String.valueOf(saveDeviceItem.getDefaultChannel()));
			et_device_add_channelnumber.setText(saveDeviceItem.getChannelSum());
		}		
		
		et_device_add_server.setKeyListener(null);
		et_device_add_port.setKeyListener(null);
		et_device_add_record.setKeyListener(null);
		et_device_add_username.setKeyListener(null);
		
		et_device_add_password.setKeyListener(null);
		et_device_add_defaultChannel.setKeyListener(null);
		et_device_add_channelnumber.setKeyListener(null);
	}

	private void setClickListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DeviceInfoActivity.this.finish();
			}
		});
		
		super.getRightButton().setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				CloudAccountXML caXML = new CloudAccountXML();
				try {
					//保存到文档中...
					String status = caXML.addNewDeviceItemToCollectEquipmentXML(saveDeviceItem, filePath);
					Toast toast = Toast.makeText(DeviceInfoActivity.this, status, Toast.LENGTH_LONG);
					toast.show();
					DeviceInfoActivity.this.finish();
					
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putSerializable("saveDeviceItem", saveDeviceItem);
					intent.putExtras(bundle);
					intent.setClass(DeviceInfoActivity.this, DeviceViewActivity.class);
					startActivity(intent);
					DeviceInfoActivity.this.finish();
				} catch (Exception e) {
					e.printStackTrace();
					Toast toast = Toast.makeText(DeviceInfoActivity.this, "保存失败...", Toast.LENGTH_LONG);
					toast.show();
				}
				//验证合法性....
			}
		});
	}

	private void superExtendsAndHidenView() {
		super.setToolbarVisiable(false);
		super.setTitleViewText("设备管理");
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.hideExtendButton();
		
		Intent intent = getIntent();
		if(intent!= null){
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				saveDeviceItem = (DeviceItem) bundle.getSerializable("clickDeviceItem");
			}
		}
	}
}
package com.starnet.snview.devicemanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;

public class DeviceScanActivity extends BaseActivity {
	
	@SuppressLint("SdCardPath")
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
	
	private EditText record_et;
	private EditText server_et;
	private EditText port_et;
	private EditText username_et;
	private EditText password_et;
	private EditText defaultChannel_et;
	private EditText channelnumber_et;
	
	private DeviceItem clickDeviceItem;
	
	private Button editButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manager_scan_acitivity);
		superChangeViewFromBase();
		setListeners();
	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				DeviceScanActivity.this.finish();
			}
		});
		
		editButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("clickDeviceItem", clickDeviceItem);
				intent.putExtras(bundle);
				intent.setClass(DeviceScanActivity.this, DeviceEditableActivity.class);
				startActivityForResult(intent, 10);
			}
		});
		
	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 10) {
			if (data != null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					DeviceItem cDeviceItem = (DeviceItem) bundle.getSerializable("cDeviceItem");
					boolean result = checkChangeableBetweenTwoItems(cDeviceItem,clickDeviceItem);
					if (!result) {//重新显示新的信息
						String dName = cDeviceItem.getDeviceName();
						String lPass = cDeviceItem.getLoginPass();
						String lUser = cDeviceItem.getLoginUser();
						String chSum = cDeviceItem.getChannelSum();
						
						String dfChl = String.valueOf(cDeviceItem.getDefaultChannel());
						String svrIp = cDeviceItem.getSvrIp();
						String svrPt = cDeviceItem.getSvrPort();
						
						record_et.setText(dName);
						server_et.setText(svrIp);
						port_et.setText(svrPt);
						username_et.setText(lUser);
						password_et.setText(lPass);
						defaultChannel_et.setText(dfChl);
						channelnumber_et.setText(chSum);
						CloudAccountXML caXml = new CloudAccountXML();
						try {
							caXml.removeDeviceItemToCollectEquipmentXML(clickDeviceItem, filePath);//移除原来的设备
							caXml.addNewDeviceItemToCollectEquipmentXML(cDeviceItem, filePath);//添加更改后的设备
						}catch (Exception e) {
							String text = "保存失败";
							Toast toast = Toast.makeText(DeviceScanActivity.this, text, Toast.LENGTH_LONG);
							toast.show();
						}
					}
				}
			}
		}
	}

	private boolean checkChangeableBetweenTwoItems(DeviceItem cDeviceItem,DeviceItem clickDeviceItem2) {
		boolean result =false;
		
		String dName = cDeviceItem.getDeviceName();
		String lPass = cDeviceItem.getLoginPass();
		String lUser = cDeviceItem.getLoginUser();
		String chSum = cDeviceItem.getChannelSum();
		
		String dfChl = String.valueOf(cDeviceItem.getDefaultChannel());
		String svrIp = cDeviceItem.getSvrIp();
		String svrPt = cDeviceItem.getSvrPort();
		
		String dName2 = clickDeviceItem2.getDeviceName();
		String lPass2 = clickDeviceItem2.getLoginPass();
		String lUser2 = clickDeviceItem2.getLoginUser();
		String chSum2 = clickDeviceItem2.getChannelSum();
		
		String dfChl2 = String.valueOf(clickDeviceItem2.getDefaultChannel());
		String svrIp2 = clickDeviceItem2.getSvrIp();
		String svrPt2 = clickDeviceItem2.getSvrPort();
		
		if ((dName.equals(dName2)||(dName == dName2))&&(lPass.equals(lPass2)||(lPass == lPass2))
			&&(lUser.equals(lUser2)||(lUser == lUser2))&&(chSum.equals(chSum2)||(chSum == chSum2))
			&&(dfChl.equals(dfChl2)||(dfChl == dfChl2))&&(svrIp.equals(svrIp2)||(svrIp == svrIp2))
			&&(svrPt.equals(svrPt2)||(svrPt == svrPt2))) {
			result = true;
		}
		return result;
	}

	private void superChangeViewFromBase() {
		super.setRightButtonBg(R.drawable.device_scan_edit_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		String tileName = getString(R.string.common_drawer_device_management);
		super.setTitleViewText(tileName);
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		
		editButton = super.getRightButton();
		
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
		if ((deviceName.contains("(在线)")||deviceName.contains("（在线）"))&&deviceName.length() > 3) {
			deviceName = deviceName.substring(4);
		}
		record_et.setText(deviceName);
		server_et.setText(svrIp);
		port_et.setText(svrPort);
		username_et.setText(loginUser);
		password_et.setText(loginPass);
		defaultChannel_et.setText(defaultChannel);
		channelnumber_et.setText(channelSum);
	}
}
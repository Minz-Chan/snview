package com.starnet.snview.devicemanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.ReadWriteXmlUtils;

public class DeviceScanActivity extends BaseActivity {
	
	static final String TAG = "DeviceScanActivity";
	
	private EditText record_et;
	private EditText server_et;
	private EditText port_et;
	private EditText username_et;
	private EditText password_et;
	private EditText defaultChannel_et;
//	private EditText channelnumber_et;
	private RadioButton YesRadioButton;
	private RadioButton NoRadioButton;
	private DeviceItem clickDeviceItem;
	
	private int position;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manager_scan_acitivity);
		superChangeViewFromBase();
		setListeners();
	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener(){//返回到设备列表查看界面
			@Override
			public void onClick(View v) {
				DeviceScanActivity.this.finish();
			}
		});
		
		super.getRightButton().setOnClickListener(new OnClickListener(){//进入编辑界面。。。
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
					final DeviceItem cDeviceItem = (DeviceItem) bundle.getSerializable("cDeviceItem");
					boolean result = checkChangeableBetweenTwoItems(cDeviceItem,clickDeviceItem);
					if (!result) {//重新显示新的信息
						record_et.setText(cDeviceItem.getDeviceName());
						server_et.setText(cDeviceItem.getSvrIp());
						port_et.setText(cDeviceItem.getSvrPort());
						username_et.setText(cDeviceItem.getLoginUser());
						password_et.setText(cDeviceItem.getLoginPass());
						defaultChannel_et.setText(String.valueOf(cDeviceItem.getDefaultChannel()));
						if (cDeviceItem.isUsable()) {
							YesRadioButton.setChecked(true);
							NoRadioButton.setChecked(false);
						}else {
							YesRadioButton.setChecked(false);
							NoRadioButton.setChecked(true);
						}
//						channelnumber_et.setText(cDeviceItem.getChannelSum());
						try {
							new Thread(){
								@Override
								public void run() {
									try {
										ReadWriteXmlUtils.replaceSpecifyDeviceItem(ChannelListActivity.filePath, position, cDeviceItem);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}.start();
							SharedPreferences spf = getSharedPreferences("user", Context.MODE_PRIVATE);
							Editor editor = spf.edit();
							editor.putString("dName", cDeviceItem.getDeviceName());
							editor.putString("lUser", cDeviceItem.getLoginUser());
							editor.putString("lPass", cDeviceItem.getLoginPass());
							editor.putString("chSum", cDeviceItem.getChannelSum());
							
							editor.putString("dfChl", String.valueOf(cDeviceItem.getDefaultChannel()));
							editor.putString("svrIp", cDeviceItem.getSvrIp());
							editor.putString("svrPt", cDeviceItem.getSvrPort());
							editor.putBoolean("isUsable", cDeviceItem.isUsable());
							editor.commit();
							setResult(21, data);
							DeviceScanActivity.this.finish();
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
			&&(svrPt.equals(svrPt2)||(svrPt == svrPt2))&&(clickDeviceItem2.isUsable()==cDeviceItem.isUsable())) {
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
		
		record_et = (EditText) findViewById(R.id.et_device_add_record);
		server_et = (EditText) findViewById(R.id.et_device_add_server);
		port_et = (EditText) findViewById(R.id.et_device_add_port);
		username_et = (EditText) findViewById(R.id.et_device_add_username);
		
		password_et = (EditText) findViewById(R.id.et_device_add_password);
		defaultChannel_et = (EditText) findViewById(R.id.et_device_add_defaultChannel);
//		channelnumber_et = (EditText) findViewById(R.id.et_device_add_channelnumber);
		NoRadioButton = (RadioButton) findViewById(R.id.isenable_no_radioBtn);
		YesRadioButton = (RadioButton) findViewById(R.id.isenable_yes_radioBtn);
		
		record_et.setKeyListener(null);
		server_et.setKeyListener(null);
		port_et.setKeyListener(null);
		username_et.setKeyListener(null);
		
		password_et.setKeyListener(null);
		defaultChannel_et.setKeyListener(null);
//		channelnumber_et.setKeyListener(null);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		clickDeviceItem = (DeviceItem) bundle.getSerializable("clickDeviceItem");
		position = bundle.getInt("position");
		
		String deviceName = clickDeviceItem.getDeviceName();
		String loginPass = clickDeviceItem.getLoginPass();
		String loginUser = clickDeviceItem.getLoginUser();
//		String channelSum = clickDeviceItem.getChannelSum();
		String defaultChannel = String.valueOf(clickDeviceItem.getDefaultChannel());
		
		String svrIp = clickDeviceItem.getSvrIp();
		String svrPort = clickDeviceItem.getSvrPort();
		
		record_et.setText(deviceName);
		server_et.setText(svrIp);
		port_et.setText(svrPort);
		username_et.setText(loginUser);
		password_et.setText(loginPass);
		defaultChannel_et.setText(defaultChannel);
//		channelnumber_et.setText(channelSum);
		if (clickDeviceItem.isUsable()) {
			YesRadioButton.setChecked(true);
			NoRadioButton.setChecked(false);
		}else {
			YesRadioButton.setChecked(false);
			NoRadioButton.setChecked(true);
		}
	}
}
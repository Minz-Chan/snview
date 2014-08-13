package com.starnet.snview.devicemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;

public class DeviceEditableActivity extends BaseActivity {
	
	

	private EditText record_et;
	private EditText server_et;
	private EditText port_et;
	private EditText username_et;
	private EditText password_et;
	private EditText defaultChannel_et;
	private EditText channelnumber_et;

	private DeviceItem clickDeviceItem;

	private Button saveButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manager_editable_acitivity);
		superChangeViewFromBase();
		setListeners();
	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DeviceEditableActivity.this.finish();
			}
		});

		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 获取信息
				String dName = record_et.getText().toString();
				String svrIp = server_et.getText().toString();
				String svrPt = port_et.getText().toString();
				String lUser = username_et.getText().toString();

				String lPass = password_et.getText().toString();
				String dfChl = defaultChannel_et.getText().toString();
				String chSum = channelnumber_et.getText().toString();

				if ((!dName.equals("") && !svrIp.equals("")
						&& !svrPt.equals("") && !lUser.equals("")
						&& !lPass.equals("") && !dfChl.equals("") 
						&& !chSum.equals(""))) {// 检查信息是否为空
					int defaultChannl = Integer.valueOf(dfChl);
					int channelNumber = Integer.valueOf(chSum);
					if (defaultChannl < channelNumber) {
						clickDeviceItem.setChannelSum(chSum);
						clickDeviceItem.setDefaultChannel(Integer.valueOf(dfChl));
						clickDeviceItem.setDeviceName(dName);
						clickDeviceItem.setSvrIp(svrIp);
						clickDeviceItem.setSvrPort(svrPt);
						clickDeviceItem.setLoginUser(lUser);
						clickDeviceItem.setLoginPass(lPass);
						// 并返回原来的界面
						Intent data = new Intent();
						Bundle bundle = new Bundle();
						bundle.putSerializable("cDeviceItem", clickDeviceItem);
						data.putExtras(bundle);
						setResult(10, data);
						DeviceEditableActivity.this.finish();
					}else {
						String text = getString(R.string.defaultchannel_channelNumber_small);
						Toast toast = Toast.makeText(DeviceEditableActivity.this, text, Toast.LENGTH_SHORT);
						toast.show();
					}
				}
			}
		});

	}

	private void superChangeViewFromBase() {
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		String tileName = getString(R.string.common_drawer_device_management);
		super.setTitleViewText(tileName);
		super.hideExtendButton();
		super.setToolbarVisiable(false);

		saveButton = super.getRightButton();

		record_et = (EditText) findViewById(R.id.et_device_add_record);
		server_et = (EditText) findViewById(R.id.et_device_add_server);
		port_et = (EditText) findViewById(R.id.et_device_add_port);
		username_et = (EditText) findViewById(R.id.et_device_add_username);

		password_et = (EditText) findViewById(R.id.et_device_add_password);
		defaultChannel_et = (EditText) findViewById(R.id.et_device_add_defaultChannel);
		channelnumber_et = (EditText) findViewById(R.id.et_device_add_channelnumber);

		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				clickDeviceItem = (DeviceItem) bundle
						.getSerializable("clickDeviceItem");
			}
		}

		String deviceName = clickDeviceItem.getDeviceName();
		String loginPass = clickDeviceItem.getLoginPass();
		String loginUser = clickDeviceItem.getLoginUser();
		String channelSum = clickDeviceItem.getChannelSum();
		String defaultChannel = String.valueOf(clickDeviceItem
				.getDefaultChannel());

		String svrIp = clickDeviceItem.getSvrIp();
		String svrPort = clickDeviceItem.getSvrPort();
		if ((deviceName.contains("(在线)") || deviceName.contains("（在线）"))
				&& deviceName.length() > 3) {
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
package com.starnet.snview.devicemanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("SdCardPath")
public class DeviceInfoActivity extends BaseActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "DeviceInfoActivity";
	private DeviceItem saveDeviceItem;
	
	private EditText et_device_add_record;
	private EditText et_device_add_server;
	private EditText et_device_add_port;
	
	private EditText et_device_add_username;
	private EditText et_device_add_password;
	private EditText et_device_add_defaultChannel;
	private EditText et_device_add_channelnumber;
	
	private Button identify_save_btn;//验证并保存按钮...
	private EditText select_et;
//	private Button on_off_btn;//单击,控制device_add_choose_et是否可输入
//	private static int clickTime = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manage_add_baseactivity);
		
		superExtendsAndHidenView();
		
		findViewsAndInitial();
		
		setClickListenersForButton();
	}

	private void findViewsAndInitial() {
//		on_off_btn = (Button) findViewById(R.id.device_add_button_state);''
		select_et = (EditText) findViewById(R.id.device_add_choose_et);

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
			
			et_device_add_username.setText(saveDeviceItem.getLoginUser());
			et_device_add_password.setText(saveDeviceItem.getLoginPass());
			et_device_add_defaultChannel.setText(String.valueOf(saveDeviceItem.getDefaultChannel()));
			et_device_add_channelnumber.setText(saveDeviceItem.getChannelSum());
		}		
		String record = saveDeviceItem.getDeviceName();
		int rdLen = record.length();
		if (rdLen >= 4) {
			String word1 = getString(R.string.device_manager_online_en);
			String word2 = getString(R.string.device_manager_online_cn);
			String word3 = getString(R.string.device_manager_offline_cn);
			String word4 = getString(R.string.device_manager_offline_en);
			String recordName = record.substring(0, 4);
			if (recordName.contains(word1)||recordName.contains(word2)
				||recordName.contains(word3)||recordName.contains(word4)) {
				record = record.substring(4);
			}
			et_device_add_record.setText(record);
		}
		
		et_device_add_channelnumber.setKeyListener(null);
		select_et.setKeyListener(null);
	}

	private void setClickListenersForButton() {
		super.getLeftButton().setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				DeviceInfoActivity.this.finish();
			}
		});
		
//		on_off_btn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				clickTime++;
//				if ((clickTime%2 != 0)) {
//					select_et.setEnabled(true);
//					select_et.setInputType(InputType.TYPE_CLASS_TEXT);
//				}else {
//					String content = select_et.getText().toString();
//					select_et.setText(content);
//					select_et.clearFocus();
//					Context context = DeviceInfoActivity.this;
//					InputMethodManager im = (InputMethodManager) getSystemService(context.INPUT_METHOD_SERVICE);
//					im.hideSoftInputFromWindow(select_et.getWindowToken(), 0);
//					select_et.setKeyListener(null);
//				}
//			}
//		});
		
		
		identify_save_btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				boolean allEdit = examineAllEditAndRight();//检查是否全部输入,并且IP地址、网络端口号等是否合法
				if (allEdit) {//已经全部输入，将输入的项保存到文档中，并且验证是否可达...
					try {
						//保存到文档中...
						String status = ReadWriteXmlUtils.addNewDeviceItemToCollectEquipmentXML(saveDeviceItem, ChannelListActivity.filePath);
						Toast toast = Toast.makeText(DeviceInfoActivity.this, status, Toast.LENGTH_LONG);
						toast.show();
						SharedPreferences spf = getSharedPreferences("saveUser", Context.MODE_PRIVATE);
						Editor editor = spf.edit();
						editor.putString("dName", saveDeviceItem.getDeviceName());
						editor.putString("chSum", saveDeviceItem.getChannelSum());
						editor.putString("dChnl", String.valueOf(saveDeviceItem.getDefaultChannel()));
						editor.putString("svrIp", saveDeviceItem.getSvrIp());
						
						editor.putString("lgUsr", saveDeviceItem.getLoginUser());
						editor.putString("lgPas", saveDeviceItem.getLoginPass());
						editor.putString("svrPt", saveDeviceItem.getSvrPort());
						
						editor.commit();
						DeviceInfoActivity.this.finish();
					} catch (Exception e) {
						e.printStackTrace();
						String printSentence = getString(R.string.save_failed);
						Toast toast = Toast.makeText(DeviceInfoActivity.this, printSentence, Toast.LENGTH_LONG);
						toast.show();
						
					}
				}else {
					String printSentence = getString(R.string.exist_input_null_check);
					Toast toast = Toast.makeText(DeviceInfoActivity.this, printSentence, Toast.LENGTH_LONG);
					toast.show();
				}
			}
		});
	}

	protected boolean examineAllEditAndRight() {
		boolean allEdit = false;
		
		String server = et_device_add_server.getText().toString();
		String record = et_device_add_record.getText().toString();
		String ddport = et_device_add_port.getText().toString();
		String channl = et_device_add_defaultChannel.getText().toString();
		
		String number = et_device_add_channelnumber.getText().toString();
		String usname = et_device_add_username.getText().toString();
		String lgPass = et_device_add_password.getText().toString();
		
		if (!server.equals("")&&!record.equals("")&&!ddport.equals("")
			&&!channl.equals("")&&!number.equals("")&&!usname.equals("")) {
			allEdit = true;
			
			int rdLen = record.length();
			if (rdLen >= 4) {
				String word1 = getString(R.string.device_manager_online_en);
				String word2 = getString(R.string.device_manager_online_cn);
				String word3 = getString(R.string.device_manager_offline_cn);
				String word4 = getString(R.string.device_manager_offline_en);
				String recordName = record.substring(0, 4);
				if (recordName.contains(word1)||recordName.contains(word2)
					||recordName.contains(word3)||recordName.contains(word4)) {
					record = record.substring(4);
				}
				saveDeviceItem.setDeviceName(record);
				saveDeviceItem.setSvrIp(server);
				saveDeviceItem.setSvrPort(ddport);
				saveDeviceItem.setLoginUser(usname);
				saveDeviceItem.setLoginPass(lgPass);
				saveDeviceItem.setDefaultChannel(Integer.valueOf(channl));
				saveDeviceItem.setChannelSum(number);
			}
		}
		return allEdit;
	}

	private void superExtendsAndHidenView() {
		super.setToolbarVisiable(false);
		super.setTitleViewText("设备管理");
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.hideExtendButton();
		identify_save_btn = super.getRightButton();
		
		Intent intent = getIntent();
		if(intent!= null){
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				saveDeviceItem = (DeviceItem) bundle.getSerializable("clickDeviceItem");
			}
		}
	}
}
package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;

@SuppressLint("SdCardPath")
public class DeviceCollectActivity extends BaseActivity {

	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";//用于保存收藏设备...
	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";//用于从文档中获取所有的设备

	private TextView titleView;// 标题
	private Button leftButton;// 左边按钮
	private Button rightButton;// 右边按钮
	
	private CloudAccountXML caXML;
	private EditText et_device_add_record;
	private EditText et_device_add_server;
	private EditText et_device_add_port;
	private EditText et_device_add_username;
	private EditText et_device_add_password;
	private EditText et_device_add_channelnumber;
	private EditText et_device_add_defaultchannel;
	
	private EditText et_device_choose;
	
	private Button device_add_button_state;

	private List<DeviceItem> deviceItemList = new ArrayList<DeviceItem>();
	private DeviceItem saveDeviceItem = new DeviceItem();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manage_add_baseactivity);

		superChangeViewFromBase();

		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DeviceCollectActivity.this.finish();
			}
		});

		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//判断用户设置为空的时候				
				String recordName = getEditTextString(et_device_add_record);
				String serverIP = getEditTextString(et_device_add_server);
				String serverPort = getEditTextString(et_device_add_port);
				String userName = getEditTextString(et_device_add_username);
				String password = getEditTextString(et_device_add_password);
				String channelNumber = getEditTextString(et_device_add_channelnumber);
				String defaultChannel = getEditTextString(et_device_add_defaultchannel);
				
				//当所有的内容都不为空的时候，则保存到指定的文档中
				if (!recordName.equals("")&&!serverIP.equals("")&&!serverPort.equals("")&&!userName.equals("")&&!password.equals("")&&!defaultChannel.equals("")&&!channelNumber.equals("")) {
					
					int dChannel = Integer.valueOf(defaultChannel);
					int channelNum = Integer.valueOf(channelNumber);
					saveDeviceItem.setDeviceName(recordName);
					saveDeviceItem.setChannelSum(channelNumber);
					saveDeviceItem.setLoginUser(userName);
					saveDeviceItem.setLoginPass(password);
					saveDeviceItem.setDefaultChannel(dChannel);
					saveDeviceItem.setSvrIp(serverIP);
					saveDeviceItem.setSvrPort(serverPort);
					
					try {//测试saveDeviceItem的数据；？？？？？？？？？？？？
						if(dChannel <= channelNum){
						String saveResult = caXML.addNewDeviceItemToCollectEquipmentXML(saveDeviceItem,filePath);//保存
						Toast toast = Toast.makeText(DeviceCollectActivity.this, saveResult, Toast.LENGTH_SHORT);
						toast.show();
						}else{
							//文档读写异常
							String text = "默认通道的数字应小于通道数量...";
							Toast toast = Toast.makeText(DeviceCollectActivity.this, text, Toast.LENGTH_SHORT);
							toast.show();
						}	
					} catch (Exception e) {
						//文档读写异常
						String text = "保存失败";
						Toast toast = Toast.makeText(DeviceCollectActivity.this, text, Toast.LENGTH_SHORT);
						toast.show();
					}//保存到指定的文档中
				}else{
					String text = "包含有尚未赋值的部分,请检查...";
					Toast toast = Toast.makeText(DeviceCollectActivity.this, text, Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});
		
		device_add_button_state.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(DeviceCollectActivity.this, DeviceChooseActivity.class);
				startActivity(intent);
			}
		});
	}

	private void superChangeViewFromBase() {// 得到从父类继承的控件，并修改
		titleView = super.getTitleView();
		leftButton = super.getLeftButton();
		rightButton = super.getRightButton();

		super.setRightButtonBg(R.drawable.navigation_bar_save_btn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText("设备管理");
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		
		caXML = new CloudAccountXML();		
		et_device_add_record = (EditText) findViewById(R.id.et_device_add_record);
		et_device_add_server = (EditText) findViewById(R.id.et_device_add_server);
		et_device_add_port = (EditText) findViewById(R.id.et_device_add_port);
		et_device_add_username = (EditText) findViewById(R.id.et_device_add_username);
		et_device_add_password = (EditText) findViewById(R.id.et_device_add_password);
		et_device_add_defaultchannel = (EditText) findViewById(R.id.et_device_add_defaultChannel);
		et_device_add_channelnumber = (EditText) findViewById(R.id.et_device_add_channelnumber);
		device_add_button_state = (Button) findViewById(R.id.device_add_button_state);
	}
	

	private String getEditTextString(EditText editText) {
		String content = "";
		Editable editable = editText.getText();
		if (editable!= null) {
			content = editable.toString();
		}
		return content;
	}
}
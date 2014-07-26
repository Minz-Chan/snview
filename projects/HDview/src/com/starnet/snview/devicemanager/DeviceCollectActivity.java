package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.syssetting.CloudAccount;

@SuppressLint("SdCardPath")
public class DeviceCollectActivity extends BaseActivity {

	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";

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
	private Spinner device_add_spinner;
	
	private Button device_add_button_state;

	private List<DeviceItem> deviceItemList;
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
		
		List<String> spinnerData = new ArrayList<String>(); 
		deviceItemList = getData();
		int deviceItemListSize = deviceItemList.size();
		for (int i = 0; i < deviceItemListSize; i++) {
			spinnerData.add(deviceItemList.get(i).getDeviceName());
		}
		
		ArrayAdapter<String>adapter = new ArrayAdapter<String>(DeviceCollectActivity.this,android.R.layout.simple_spinner_item, spinnerData);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		device_add_spinner.setAdapter(adapter);		
		//根据spinnerData选择的数据，加载该DeviceItem的其他数据，显示到其它控件。
		device_add_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {//为spinner添加点击事件
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				String recordName = (String) device_add_spinner.getSelectedItem();
				int deviceItemListSize = deviceItemList.size();
				for (int i = 0; i < deviceItemListSize; i++) {//加载其它控件的显示，根据用户的选择获取DeviceItem
					if (deviceItemList.get(i).getDeviceName().equals(recordName)) {//若是有重复的怎么办？？？
						saveDeviceItem = deviceItemList.get(i);
						break;
					}
				}
				int defaultChannel = saveDeviceItem.getDefaultChannel();
				et_device_add_record.setText(recordName);//保存时，记录名唯一
				String svrIp = saveDeviceItem.getSvrIp();
				if(!svrIp.equals(null)){
					et_device_add_server.setText(svrIp);
				}
				String svrPort = saveDeviceItem.getSvrPort();
				if(!svrPort.equals(null)){
					et_device_add_port.setText(svrPort);
				}
				String loginUser = saveDeviceItem.getLoginUser();
				if(!loginUser.equals(null)){
					et_device_add_username.setText(loginUser);
				}
				String loginPass = saveDeviceItem.getLoginPass();
				if(!loginPass.equals(null)){
					et_device_add_password.setText(loginPass);
				}
				String faultChannel = String.valueOf(defaultChannel);
				if(!faultChannel.equals(null)){
					et_device_add_defaultchannel.setText(faultChannel);
				}
				String channelSum =saveDeviceItem.getChannelSum();
				if(!channelSum.equals(null)){
					et_device_add_channelnumber.setText(channelSum);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
					
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
		
		caXML = new CloudAccountXML();		
		et_device_add_record = (EditText) findViewById(R.id.et_device_add_record);
		et_device_add_server = (EditText) findViewById(R.id.et_device_add_server);
		et_device_add_port = (EditText) findViewById(R.id.et_device_add_port);
		et_device_add_username = (EditText) findViewById(R.id.et_device_add_username);
		et_device_add_password = (EditText) findViewById(R.id.et_device_add_password);
		et_device_add_defaultchannel = (EditText) findViewById(R.id.et_device_add_defaultChannel);
		et_device_add_channelnumber = (EditText) findViewById(R.id.et_device_add_channelnumber);
		device_add_spinner = (Spinner) findViewById(R.id.device_add_spinner);
		device_add_button_state = (Button) findViewById(R.id.device_add_button_state);
	}
	
	private List<DeviceItem> getData() {//从个人用户处获得。。。从文档中读取
		List<DeviceItem> data = new ArrayList<DeviceItem>();
		List<CloudAccount>cloudAccountList =  caXML.readCloudAccountFromXML(CLOUD_ACCOUNT_PATH);
		int size = cloudAccountList.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cloudAccount = cloudAccountList.get(i);
			if (cloudAccount!=null) {
				List <DeviceItem> deviceItemList = cloudAccount.getDeviceList();
				if (deviceItemList!=null) {
					int deviceItemSize = deviceItemList.size();
					for (int j = 0; j < deviceItemSize; j++) {
						DeviceItem deviceItem = deviceItemList.get(j);
						data.add(deviceItem);
					}
				}
			}
		}
		return data;
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
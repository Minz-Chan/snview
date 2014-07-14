package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.List;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;







import com.starnet.snview.syssetting.CloudAccount;

import android.annotation.SuppressLint;
//import com.starnet.snview.component.BaseActivity;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * 
 * @author zhongxu
 * @Date 2014年7月25日
 * @ClassName DevicesAddActivity.java
 * @Description 功能：设备添加
 */
@SuppressLint("SdCardPath")
public class DevicesAddActivity extends Activity {
	//设备添加
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";	
	private CloudAccountXML caXML;
	private EditText et_device_add_record;
	private EditText et_device_add_server;
	private EditText et_device_add_port;
	private EditText et_device_add_username;
	private EditText et_device_add_password;
	private EditText et_device_add_defaultchannel;
	private EditText et_device_add_channelnumber;	
	private Button base_navigationbar_right_btn_save ;//保存按钮
	private Button base_navigationbar_right_btn_return ;//返回按钮	
	private Spinner device_add_spinner ;
	
	private List <DeviceItem> deviceItemList;
	private DeviceItem saveDeviceItem = new DeviceItem();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manager_add_activity);		
		findWadgetsAndIntial();
		base_navigationbar_right_btn_save.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//判断用户设置为空的时候				
				String recordName = getEditTextString(et_device_add_record);
				String serverIP = getEditTextString(et_device_add_server);
				String serverPort = getEditTextString(et_device_add_port);
				String userName = getEditTextString(et_device_add_username);
				String password = getEditTextString(et_device_add_password);
				String defaultChannel = getEditTextString(et_device_add_defaultchannel);
				String channelNumber = getEditTextString(et_device_add_channelnumber);
				
				//当所有的内容都不为空的时候，则保存到指定的文档中
				if (!recordName.equals("")&&!serverIP.equals("")&&!serverPort.equals("")&&!userName.equals("")&&!password.equals("")&&!defaultChannel.equals("")&&!channelNumber.equals("")) {
					
					int dChannel = Integer.valueOf(defaultChannel);
					saveDeviceItem.setDeviceName(recordName);
					saveDeviceItem.setChannelSum(channelNumber);
					saveDeviceItem.setLoginUser(userName);
					saveDeviceItem.setLoginPass(password);
					saveDeviceItem.setDefaultChannel(dChannel);
					saveDeviceItem.setSvrIp(serverIP);
					saveDeviceItem.setSvrPort(serverPort);
//					deviceItem.setDeviceType(5);
					
					try {
						String saveResult = caXML.addNewDeviceItemToCollectEquipmentXML(saveDeviceItem,filePath);//测试saveDeviceItem的数据；？？？？？？？？？？？？
						Toast toast = Toast.makeText(DevicesAddActivity.this, saveResult, Toast.LENGTH_SHORT);
						toast.show();
					} catch (Exception e) {
						//文档读写异常
						String text = "保存失败";
						Toast toast = Toast.makeText(DevicesAddActivity.this, text, Toast.LENGTH_SHORT);
						toast.show();
					}//保存到指定的文档中
				}else{
					String text = "包含有尚未赋值的部分,请检查...";
					Toast toast = Toast.makeText(DevicesAddActivity.this, text, Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});		
		base_navigationbar_right_btn_return.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				DevicesAddActivity.this.finish();	
			}
		});
	}
	private void findWadgetsAndIntial() {	
		caXML = new CloudAccountXML();		
		et_device_add_record = (EditText) findViewById(R.id.et_device_add_record);
		et_device_add_server = (EditText) findViewById(R.id.et_device_add_server);
		et_device_add_port = (EditText) findViewById(R.id.et_device_add_port);
		et_device_add_username = (EditText) findViewById(R.id.et_device_add_username);
		et_device_add_password = (EditText) findViewById(R.id.et_device_add_password);
		et_device_add_defaultchannel = (EditText) findViewById(R.id.et_device_add_defaultchannel);
		et_device_add_channelnumber = (EditText) findViewById(R.id.et_device_add_channelnumber);
		base_navigationbar_right_btn_save = (Button) findViewById(R.id.base_navigationbar_right_btn_save);	
		base_navigationbar_right_btn_return = (Button) findViewById(R.id.base_navigationbar_left_btn_return);
		device_add_spinner = (Spinner) findViewById(R.id.device_add_spinner);
		
		List<String> spinnerData = new ArrayList<String>(); 
		deviceItemList = getData();
		int deviceItemListSize = deviceItemList.size();
		for (int i = 0; i < deviceItemListSize; i++) {
			spinnerData.add(deviceItemList.get(i).getDeviceName());
		}
		
		ArrayAdapter<String>adapter = new ArrayAdapter<String>(DevicesAddActivity.this,android.R.layout.simple_spinner_item, spinnerData);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		device_add_spinner.setAdapter(adapter);		
		//根据spinnerData选择的数据，加载该DeviceItem的其他数据，显示到其它控件。
		device_add_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {//为spinner添加点击事件
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
				String recordName = (String) device_add_spinner.getSelectedItem();
				
//				DeviceItem deviceItem = new DeviceItem();
				int deviceItemListSize = deviceItemList.size();
				for (int i = 0; i < deviceItemListSize; i++) {//加载其它控件的显示，根据用户的选择获取DeviceItem
					if (deviceItemList.get(i).getDeviceName().equals(recordName)) {//若是有重复的怎么办？？？
						saveDeviceItem = deviceItemList.get(i);
						break;
					}
				}
				
				int defaultChannel = saveDeviceItem.getDefaultChannel();
				et_device_add_record.setText(recordName);//保存时，记录名唯一
				et_device_add_server.setText(saveDeviceItem.getSvrIp());
				et_device_add_port.setText(saveDeviceItem.getSvrPort());
				et_device_add_username.setText(saveDeviceItem.getLoginUser());
				et_device_add_password.setText(saveDeviceItem.getLoginPass());
				et_device_add_defaultchannel.setText(String.valueOf(defaultChannel));
				et_device_add_channelnumber.setText(saveDeviceItem.getChannelSum());
				
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
					
			}
		});
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
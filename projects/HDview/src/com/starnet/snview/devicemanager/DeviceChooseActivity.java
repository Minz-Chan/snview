package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.channelmanager.xml.PinyinComparator;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.syssetting.CloudAccount;

@SuppressLint("SdCardPath")
public class DeviceChooseActivity extends BaseActivity {

	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";

//	private TextView titleView;// 标题
	private Button rightButton;// 右边按钮
	
	private CloudAccountXML caXML;

	private ListView deviceListView;
	private List<DeviceItem> deviceItemList = new ArrayList<DeviceItem>();
	private List<DeviceItem> searchDeviceItemList = new ArrayList<DeviceItem>();
	private DeviceChooseAdapter deviceChooseAdapter;
	private DeviceItem clickDeviceItem;
	
	private Button device_add;
	private EditText device_search_et;//模糊搜索框...

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manage_choose_baseactivity);

		superChangeViewFromBase();

		rightButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DeviceChooseActivity.this.finish();
			}
		}); 
		
			
		//根据spinnerData选择的数据，加载该DeviceItem的其他数据，显示到其它控件。
		device_add.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				caXML = new CloudAccountXML();
				int size = deviceItemList.size();
				int number = 0 ;
				for(int i =0 ;i<size;i++){
					final DeviceItem deviceItem = deviceItemList.get(i);
					if(deviceItem.isExpanded()){
						Thread thread = new Thread(){
							@Override
							public void run() {
								super.run();
								try {
									caXML.addNewDeviceItemToCollectEquipmentXML(deviceItem, filePath);
								} catch (Exception e) {
										e.printStackTrace();
								}
							}};
						thread.start();
						number++;
					}
				}
				String text = "您保存了"+number+"个设备。";
				Toast toast = Toast.makeText(DeviceChooseActivity.this, text, Toast.LENGTH_SHORT);
				toast.show();
			}
		});
		
		device_search_et.addTextChangedListener(new TextWatcher(){//进行模糊搜索...

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				
				searchDeviceItemList.clear();
				if( device_search_et.getText() != null){
					String searchContent = device_search_et.getText().toString();
					searchDeviceItemList = getSearchDeviceItemList(searchContent);
					deviceChooseAdapter = new DeviceChooseAdapter(DeviceChooseActivity.this,searchDeviceItemList);
					deviceListView.setAdapter(deviceChooseAdapter);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		deviceListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				DeviceItem deviceItem = deviceItemList.get(position);
//				if(deviceItem.isExpanded()){
//					deviceItem.setExpanded(false);
//				}else{
//					deviceItem.setExpanded(true);
//				}
				clickDeviceItem = deviceItem;
				gotoDeviceInfoActivity();
//				DeviceChooseActivity.this.finish();
				
			}
		});
	}

	protected List<DeviceItem> getSearchDeviceItemList(String searchContent) {
		List<DeviceItem> newDeviceItemList = new ArrayList<DeviceItem>(); 
		int size = deviceItemList.size();
		for(int i =0 ;i<size;i++){
			DeviceItem deviceItem = deviceItemList.get(i);
			String deviceName = deviceItem.getDeviceName();
			if(deviceName.contains(searchContent)){
				newDeviceItemList.add(deviceItem);
			}
		}
		return newDeviceItemList;
	}

	protected void gotoDeviceInfoActivity() {//进入设备信息界面
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putSerializable("clickDeviceItem", clickDeviceItem);
		intent.putExtras(bundle);
		intent.setClass(DeviceChooseActivity.this, DeviceInfoActivity.class);
		startActivity(intent);
		
	}

	private void superChangeViewFromBase() {// 得到从父类继承的控件，并修改
		super.getTitleView();
		rightButton = super.getRightButton();

		super.setToolbarVisiable(false);
		super.setRightButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText("星云平台");
		super.hideExtendButton();
		super.hideLeftButton();
		
		caXML = new CloudAccountXML();
		device_add = (Button) findViewById(R.id.device_choose_add_btn);
		deviceListView = (ListView) findViewById(R.id.lview_device);
		device_search_et = (EditText) findViewById(R.id.device_choose_add_et); 
//		deviceListView = (DeviceListView) findViewById(R.id.lview_device);
		
		getDeviceItemListData();
		deviceChooseAdapter = new DeviceChooseAdapter(DeviceChooseActivity.this,deviceItemList);
		deviceListView.setAdapter(deviceChooseAdapter);
	}
	
	private void getDeviceItemListData() {//从个人用户处获得。。。从文档中读取
		
		List<CloudAccount> cloudAccountList =  caXML.readCloudAccountFromXML(CLOUD_ACCOUNT_PATH);
		int size = cloudAccountList.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cloudAccount = cloudAccountList.get(i);
			if (cloudAccount!=null) {
				List<DeviceItem> deviceList = cloudAccount.getDeviceList();
				int dSize = deviceList.size();
				for(int j =0 ;j<dSize;j++){
					deviceItemList.add(deviceList.get(j));
				}
			}
		}
		
		if(deviceItemList.size()>0){
			Collections.sort(deviceItemList, new PinyinComparator());
		}
	}
}
package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.syssetting.CloudAccount;

@SuppressLint("SdCardPath")
public class DeviceChooseActivity extends BaseActivity {

	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";

	private TextView titleView;// 标题
	private Button rightButton;// 右边按钮
	
	private CloudAccountXML caXML;

	private ListView mListView;
	private List<DeviceItem> deviceItemList;

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

		List<String> spinnerData = new ArrayList<String>(); 
		deviceItemList = getData();
		int deviceItemListSize = deviceItemList.size();
		for (int i = 0; i < deviceItemListSize; i++) {
			spinnerData.add(deviceItemList.get(i).getDeviceName());
		}
			
		//根据spinnerData选择的数据，加载该DeviceItem的其他数据，显示到其它控件。
		
	}

	private void superChangeViewFromBase() {// 得到从父类继承的控件，并修改
		titleView = super.getTitleView();
		rightButton = super.getRightButton();

		super.setRightButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText("星云平台");
		super.hideExtendButton();
		super.hideLeftButton();
		
		caXML = new CloudAccountXML();
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
}
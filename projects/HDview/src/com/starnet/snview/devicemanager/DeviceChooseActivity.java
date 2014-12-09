package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.CloudAccountUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;
import com.starnet.snview.util.SynObject;

@SuppressLint({ "SdCardPath", "HandlerLeak" })
public class DeviceChooseActivity extends BaseActivity {
	
	private final String TAG = "DeviceChooseActivity";
	final String devicefilePath = "/data/data/com.starnet.snview/deviceItem_list.xml";
	
	private final int RESULTCODE = 11;
	
	private boolean is_blur_search = false;

	private Button leftButton;														
	private ListView deviceListView;
	private ArrayList <DVRDevice> dvrDeviceList = new ArrayList<DVRDevice>();
	private CloudAccountUtils caUtils= new CloudAccountUtils();
	private List<DeviceItem> deviceItemList = new ArrayList<DeviceItem>();			//保存全部数据
	private List<DeviceItem> searchDeviceItemList = new ArrayList<DeviceItem>();	//保存模糊搜索数据
	private DeviceChooseAdapter deviceChooseAdapter;
	private DeviceItem clickDeviceItem;
	
	private EditText device_search_et;												// 模糊搜索框
	private SynObject synObject = new SynObject();
	private final int ADD_SUCCESS = 1;
	private final int ADD_FAILED = 2;
	private final int ADDDATESTOXMLDialog = 3;										// 一键添加数据到文档
	private final int EMPTY_MSG = 110;

	private Handler mHandler = new Handler() {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String printSentence;
			if (synObject.getStatus() == SynObject.STATUS_RUN) {
				return;
			}
			synObject.resume();// 解除线程挂起,向下继续执行....
			switch (msg.what) {
			case ADD_SUCCESS:
				dismissDialog(ADDDATESTOXMLDialog);
				printSentence = getString(R.string.device_manager_devicechoose_adding_success);
				Toast.makeText(DeviceChooseActivity.this,printSentence, Toast.LENGTH_SHORT).show();
				DeviceChooseActivity.this.finish();
			case ADD_FAILED:
				dismissDialog(ADDDATESTOXMLDialog);
				break;
			case EMPTY_MSG:
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manage_choose_baseactivity);

		superChangeViewFromBase();

		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DeviceChooseActivity.this.finish();
			}
		});

		device_search_et.addTextChangedListener(new TextWatcher() {// 进行模糊搜索...

					@Override
					public void beforeTextChanged(CharSequence s, int start,int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,int before, int count) {
						searchDeviceItemList.clear();
						if (device_search_et.getText() != null) {
							String searchContent = device_search_et.getText().toString();
							searchDeviceItemList = getSearchDeviceItemList(searchContent);
							deviceChooseAdapter = new DeviceChooseAdapter(DeviceChooseActivity.this,searchDeviceItemList);
							deviceListView.setAdapter(deviceChooseAdapter);
							is_blur_search = true;
						}
					}

					@Override
					public void afterTextChanged(Editable s) {  }
				});

		deviceListView.setOnItemClickListener(new OnItemClickListener() {//单击进入平台信息界面...
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				DeviceItem deviceItem ;
				if (is_blur_search) {
					deviceItem = searchDeviceItemList.get(position);
				}else {
					deviceItem = deviceItemList.get(position);
				}
				
				clickDeviceItem = deviceItem;
				String dName = deviceItem.getDeviceName();
				String length = getString(R.string.device_manager_off_on_line_length);
				int dLen = dName.length();
				int len = Integer.valueOf(length);
				if ((dLen >= len)) {
					String word1 = getString(R.string.device_manager_online_en);
					String word2 = getString(R.string.device_manager_online_cn);
					String word3 = getString(R.string.device_manager_offline_cn);
					String word4 = getString(R.string.device_manager_offline_en);
					
					String subDName = dName.substring(0, len);
					if (subDName.contains(word1)||subDName.contains(word2)
						||subDName.contains(word3)||subDName.contains(word4)) {
						dName = dName.substring(len);
					}
					clickDeviceItem.setDeviceName(dName);
				}
				clickDeviceItem.setIdentify(true);
				//返回到DeviceCollectActivity.java界面
				Intent data = new Intent();
				Bundle extras = new Bundle();
				extras.putSerializable("chooseDeviceItem", clickDeviceItem);
				extras.putInt("auto_flag", 2);
				data.putExtras(extras);
				setResult(RESULTCODE, data);
				DeviceChooseActivity.this.finish();
			}
		});
	}

	@SuppressWarnings("deprecation")
	private void showAddDeviceTips() {
		showDialog(ADDDATESTOXMLDialog);
		new AddDeviceDataThread(mHandler).start();
		synObject.suspend();
		Log.i(TAG, "");
	}

	class AddDeviceDataThread extends Thread {
		private Handler handler;

		public AddDeviceDataThread(Handler handler) {
			this.handler = handler;
		}
		private Message msg = new Message();

		@Override
		public void run() {
			super.run();
			try {
				// 检查重复性，若已经包含则不添加，若不包含，则添加到新的通道列表中；
				List<DeviceItem> oldDeviceList = ReadWriteXmlUtils.getCollectDeviceListFromXML(devicefilePath);
				deviceItemList = recreateDeviceList(oldDeviceList,deviceItemList);//重新构造列表，若原来的设备中包含列表，则不需要添加，否则，添加到deviceItemList列表中；
				//在线(离线)字样的删除...
				ReadWriteXmlUtils.addDeviceItemListToXML(deviceItemList, devicefilePath);
				msg.what = ADD_SUCCESS;// 添加成功
				handler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
				msg.what = ADD_FAILED;// 添加失败
				handler.sendMessage(msg);
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ADDDATESTOXMLDialog:
			ProgressDialog progress = ProgressDialog.show(this, "",getString(R.string.device_manager_devicechoose_adding_and_wait), true, true);
			progress.setOnCancelListener(new OnCancelListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(ADDDATESTOXMLDialog);
					synObject.resume();
				}
			});
			return progress;
		default:
			return null;
		}
	}

	//将新的列表添加到老的列表中；
	public List<DeviceItem> recreateDeviceList(List<DeviceItem> oldDeviceList,List<DeviceItem> deviceItemList2) {
		int size = deviceItemList2.size();
		for (int i = 0; i < size; i++) {
			DeviceItem deviceItem = deviceItemList2.get(i);
			String deviceName = deviceItem.getDeviceName();
			String length = getString(R.string.device_manager_off_on_line_length);
			int len = Integer.valueOf(length);
			int rdLen = deviceName.length();
			if (rdLen >= len) {
				String word1 = getString(R.string.device_manager_online_en);
				String word2 = getString(R.string.device_manager_online_cn);
				String word3 = getString(R.string.device_manager_offline_cn);
				String word4 = getString(R.string.device_manager_offline_en);
				String recordName = deviceName.substring(0, len);
				if (recordName.contains(word1)||recordName.contains(word2)
					||recordName.contains(word3)||recordName.contains(word4)) {
					deviceName = deviceName.substring(len);
				}
				deviceItem.setDeviceName(deviceName);
			}
			boolean contain = judgeContainable(oldDeviceList,deviceItem);//判断列表中是否包含
			if (!contain) {
				oldDeviceList.add(deviceItem);
			}
		}
		return oldDeviceList;
	}

	private boolean judgeContainable(List<DeviceItem> oldDeviceList,DeviceItem deviceItem) {
		boolean contain = false;
		int size = oldDeviceList.size();
		for (int i = 0; i < size; i++) {
			DeviceItem oldDeviceItem = oldDeviceList.get(i);
			String oldDeviceName = oldDeviceItem.getDeviceName();
			String deviceName = deviceItem.getDeviceName();
			if ((oldDeviceName.equals(deviceName) || (oldDeviceName == deviceName))){
				contain = true;
				break;
			}
		}
		return contain;
	}

	protected List<DeviceItem> getSearchDeviceItemList(String searchContent) {
		List<DeviceItem> newDeviceItemList = new ArrayList<DeviceItem>();
		int size = deviceItemList.size();
		for (int i = 0; i < size; i++) {
			DeviceItem deviceItem = deviceItemList.get(i);
			String deviceName = deviceItem.getDeviceName();
			if (deviceName.contains(searchContent)) {
				newDeviceItemList.add(deviceItem);
			}
		}
		return newDeviceItemList;
	}

	protected void gotoDeviceInfoActivity() {// 进入设备信息界面
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putSerializable("clickDeviceItem", clickDeviceItem);
		intent.putExtras(bundle);
		intent.setClass(DeviceChooseActivity.this, DeviceInfoActivity.class);
		startActivity(intent);
	}

	private void superChangeViewFromBase() {// 得到从父类继承的控件，并修改
		super.getTitleView();
		leftButton = super.getLeftButton();
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText("星云平台");
		super.hideExtendButton();
		super.hideRightButton();
		
		deviceListView = (ListView) findViewById(R.id.lview_device);
		device_search_et = (EditText) findViewById(R.id.device_choose_add_et);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		dvrDeviceList = bundle.getParcelableArrayList("dvrDeviceList");
		Context context = DeviceChooseActivity.this;
		CloudAccount cloudAccount = caUtils.getCloudAccountFromDVRDevice(context,dvrDeviceList);
		deviceItemList = cloudAccount.getDeviceList();
		for (int i = 0; i < deviceItemList.size(); i++) {
			deviceItemList.get(i).setIdentify(true);
		}
		deviceChooseAdapter = new DeviceChooseAdapter(DeviceChooseActivity.this,deviceItemList);
		deviceListView.setAdapter(deviceChooseAdapter);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {// 一键添加按钮...
		super.onOptionsItemSelected(item);
		showAddDeviceTips();
		Log.i(TAG, "");
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.device_choose_menu, menu);
		return true;
	}
}
package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.CloudAccountUtils;
import com.starnet.snview.util.SynObject;

@SuppressLint({ "SdCardPath", "HandlerLeak" })
public class DeviceChooseActivity extends BaseActivity {
	
	private final String TAG = "DeviceChooseActivity";
	private final String oldDevicefilePath = "/data/data/com.starnet.snview/deviceItem_list.xml";//用于保存收藏设备...
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list_another.xml";// 一键保存设备的路径...
	// 用于从文档中获取所有的用户，根据用户信息获取设备

	private Button leftButton;// 左边按钮
	private ListView deviceListView;
	private ArrayList <DVRDevice> dvrDeviceList = new ArrayList<DVRDevice>();//保存全部数据
	private CloudAccountUtils caUtils= new CloudAccountUtils();
	private List<DeviceItem> deviceItemList = new ArrayList<DeviceItem>();//保存全部数据
	private List<DeviceItem> searchDeviceItemList = new ArrayList<DeviceItem>();//保存模糊搜索数据
	private DeviceChooseAdapter deviceChooseAdapter;
	private DeviceItem clickDeviceItem;
	
	private EditText device_search_et;// 模糊搜索框...
	private SynObject synObject = new SynObject();
	private final int ADD_SUCCESS = 1;
	private final int ADD_FAILED = 2;
	private final int ADDDATESTOXMLDialog = 3;// 一键添加数据到文档...
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
				printSentence = "添加成功...";
				Toast toast1 = Toast.makeText(DeviceChooseActivity.this,printSentence, Toast.LENGTH_SHORT);
				toast1.show();
				Intent intent = new Intent();
				intent.setClass(DeviceChooseActivity.this, DeviceViewActivity.class);
				startActivity(intent);				
				DeviceChooseActivity.this.finish();
			case ADD_FAILED:
				dismissDialog(ADDDATESTOXMLDialog);
//				printSentence = "添加失败...";
//				Toast toast = Toast.makeText(DeviceChooseActivity.this,printSentence, 1);
//				toast.show();
//				DeviceChooseActivity.this.finish();
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
						}
					}

					@Override
					public void afterTextChanged(Editable s) {  }
				});

		deviceListView.setOnItemClickListener(new OnItemClickListener() {//单击进入平台信息界面...
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				DeviceItem deviceItem = deviceItemList.get(position);
				clickDeviceItem = deviceItem;
				gotoDeviceInfoActivity();
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
				CloudAccountXML caXML = new CloudAccountXML();
				List<DeviceItem> oldDeviceList = caXML.getCollectDeviceListFromXML(oldDevicefilePath);
				deviceItemList = recreateDeviceList(oldDeviceList,deviceItemList);//重新构造列表，若原来的设备中包含列表，则不需要添加，否则，添加到deviceItemList列表中；
				
				caXML.addDeviceItemListToXML(deviceItemList, oldDevicefilePath);
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
			ProgressDialog progress = ProgressDialog.show(this, "",getString(R.string.adding_device_wait), true, true);
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
//			String oldDevicePasd = oldDeviceItem.getLoginPass();
//			String devicePasd = deviceItem.getLoginPass();
//			String oldDeviceUsrName = oldDeviceItem.getLoginUser();
//			String deviceUsrName = deviceItem.getLoginUser();
//			String oldDevicePort = oldDeviceItem.getSvrPort();
			
			if ((oldDeviceName.equals(deviceName) || (oldDeviceName == deviceName))){
//				&&(oldDevicePasd.equals(devicePasd) || (oldDevicePasd == devicePasd))
//				&&(oldDeviceUsrName.equals(deviceUsrName) || (oldDeviceUsrName == deviceUsrName))
//				&&(oldDeviceUsrName.equals(deviceUsrName) || (oldDeviceUsrName == deviceUsrName))) 
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
//		DeviceChooseActivity.this.finish();
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
		CloudAccount cloudAccount = caUtils.getCloudAccountFromDVRDevice(dvrDeviceList);
		deviceItemList = cloudAccount.getDeviceList();
		deviceChooseAdapter = new DeviceChooseAdapter(DeviceChooseActivity.this,deviceItemList);
		deviceListView.setAdapter(deviceChooseAdapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {// 一键添加按钮...
		super.onOptionsItemSelected(item);
		showAddDeviceTips();
//		synObject.suspend();
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
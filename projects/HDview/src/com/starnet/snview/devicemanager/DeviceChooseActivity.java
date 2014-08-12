package com.starnet.snview.devicemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
import com.starnet.snview.channelmanager.xml.PinyinComparator;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.SynObject;

@SuppressLint("SdCardPath")
public class DeviceChooseActivity extends BaseActivity {

	private final String CLOUD_ACCOUNT_PATH = "/data/data/com.starnet.snview/cloudAccount_list.xml";
	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list_another.xml";// 一键保存设备的路径...
	private CloudAccountXML caXML;

	private Button leftButton;// 左边按钮
	private ListView deviceListView;
	private List<DeviceItem> deviceItemList = new ArrayList<DeviceItem>();
	private List<DeviceItem> searchDeviceItemList = new ArrayList<DeviceItem>();
	private DeviceChooseAdapter deviceChooseAdapter;
	private DeviceItem clickDeviceItem;

	private EditText device_search_et;// 模糊搜索框...
	private SynObject synObject = new SynObject();
	private final int ADD_SUCCESS = 1;
	private final int ADD_FAILED = 0;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (synObject.getStatus() == SynObject.STATUS_RUN) {
				return;
			}
			synObject.resume();// 解除线程挂起,向下继续执行....

			switch (msg.what) {
			case ADD_SUCCESS:
				dismissDialog(1);
				DeviceChooseActivity.this.finish();
			case ADD_FAILED:
				dismissDialog(1);
				String printSentence = "添加失败...";
				Toast toast = Toast.makeText(DeviceChooseActivity.this, printSentence, Toast.LENGTH_SHORT);
				toast.show();
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
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						searchDeviceItemList.clear();
						if (device_search_et.getText() != null) {
							String searchContent = device_search_et.getText().toString();
							searchDeviceItemList = getSearchDeviceItemList(searchContent);
							deviceChooseAdapter = new DeviceChooseAdapter(DeviceChooseActivity.this,searchDeviceItemList);
							deviceListView.setAdapter(deviceChooseAdapter);
						}
					}

					@Override
					public void afterTextChanged(Editable s) {
					}
				});

		deviceListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				DeviceItem deviceItem = deviceItemList.get(position);
				clickDeviceItem = deviceItem;
				gotoDeviceInfoActivity();
			}
		});
	}

	private void showAddDeviceTips() {
		showDialog(1);
		AddDeviceThread adThread = new AddDeviceThread(mHandler);
		adThread.start();
		synObject.suspend();
	}

	class AddDeviceThread extends Thread {
		private Handler handler;

		public AddDeviceThread(Handler handler) {
			this.handler = handler;
		}

		private CloudAccountXML caXML;
		
		Message msg = new Message();

		@Override
		public void run() {
			super.run();
			caXML = new CloudAccountXML();
			int size = deviceItemList.size();
			try {
				for (int i = 0; i < size; i++) {
					DeviceItem deviceItem = deviceItemList.get(i);
					caXML.addNewDeviceItemToCollectEquipmentXML(deviceItem,filePath);
				}
				msg.what = 1;// 添加成功
				handler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
				msg.what = 0;// 添加成功
				handler.sendMessage(msg);
			}
			
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			ProgressDialog progress = ProgressDialog.show(this, "",getString(R.string.adding_device_wait), true, true);
			progress.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(1);
					File file = new File(filePath);
					if (file.exists()) {
						file.delete();
					}
					synObject.resume();
				}
			});
			return progress;

		default:
			return null;
		}
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

		caXML = new CloudAccountXML();
		deviceListView = (ListView) findViewById(R.id.lview_device);
		device_search_et = (EditText) findViewById(R.id.device_choose_add_et);

//		getDeviceItemListData();
//		deviceChooseAdapter = new DeviceChooseAdapter(DeviceChooseActivity.this, deviceItemList);
//		deviceListView.setAdapter(deviceChooseAdapter);
	}

	private void getDeviceItemListData() {// 从个人用户处获得。。。从文档中读取
		List<CloudAccount> cloudAccountList = caXML.readCloudAccountFromXML(CLOUD_ACCOUNT_PATH);
		int size = cloudAccountList.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cloudAccount = cloudAccountList.get(i);
			if (cloudAccount != null) {
				List<DeviceItem> deviceList = cloudAccount.getDeviceList();
				int dSize = deviceList.size();
				for (int j = 0; j < dSize; j++) {
					deviceItemList.add(deviceList.get(j));
				}
			}
		}
		if (deviceItemList.size() > 0) {
			Collections.sort(deviceItemList, new PinyinComparator());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {//一键添加按钮...
		super.onOptionsItemSelected(item);
		showAddDeviceTips();
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.device_choose_menu, menu);
		return true;
	}
}
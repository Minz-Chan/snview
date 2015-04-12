package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.util.IPAndPortUtils;

@SuppressLint("HandlerLeak")
public class DeviceEditableActivity extends BaseActivity {

	protected static final String TAG = "DeviceEditableActivity";

	private EditText port_et;
	private EditText record_et;
	private EditText server_et;
	private EditText username_et;
	private EditText password_et;

	private Button identifyBtn;
	private RadioButton noRadioButton;
	private RadioButton yesRadioButton;
	private DeviceItem clickDeviceItem;
	private final int REQUESTCODE = 11;
	private List<PreviewDeviceItem> mPreviewDeviceItems;
	private List<PreviewDeviceItem> deletePDeviceItems = new ArrayList<PreviewDeviceItem>(); // 预览通道

	private ProgressDialog connIdenPrg;
	private final int CONNIDENPRG = 0x0003;
	private EditableDevConnIdentifyTask connIdenTask;
	private final int CONNECTIFYIDENTIFY_WRONG = 0x0012;
	private final int CONNECTIFYIDENTIFY_SUCCESS = 0x0011;
	private final int CONNECTIFYIDENTIFY_TIMEOUT = 0x0013;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case CONNECTIFYIDENTIFY_WRONG:
				dimissPrg();
				String txt = getString(R.string.device_manager_deviceedit_conniden_fail);
				showToasContent(txt);
				break;
			case CONNECTIFYIDENTIFY_SUCCESS:
				dimissPrg();
				String txt2 = getString(R.string.device_manager_deviceedit_conniden_succ);
				showToasContent(txt2);
				break;
			case CONNECTIFYIDENTIFY_TIMEOUT:
				dimissPrg();
				String txt3 = getString(R.string.device_manager_deviceedit_conniden_timeout);
				showToasContent(txt3);
				break;
			}
		}
	};

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

		super.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 获取信息
				String svrPt = port_et.getText().toString();
				String dName = record_et.getText().toString();
				String svrIp = server_et.getText().toString();
				String lPass = password_et.getText().toString();
				String lUser = username_et.getText().toString();
				// String chSum = channelnumber_et.getText().toString();
				// String dfChl = defaultChannel_et.getText().toString();
				String cName = DeviceEditableActivity.this.getString(R.string.device_manager_collect_device);
				if ((!dName.trim().equals("") && !svrIp.trim().equals("") && !svrPt.trim().equals("") && !lUser.trim().equals(""))) {// 检查信息是否为空
					boolean isIp = IPAndPortUtils.isIp(svrIp);
					boolean isPort = IPAndPortUtils.isNetPort(svrPt);
					if (isPort && isIp) {
						clickDeviceItem.setSvrIp(svrIp);
						clickDeviceItem.setSvrPort(svrPt);
						clickDeviceItem.setLoginUser(lUser);
						clickDeviceItem.setLoginPass(lPass);
						clickDeviceItem.setDeviceName(dName);
						// clickDeviceItem.setDefaultChannel(Integer.valueOf(dfChl));
						boolean isBelong = isBelongDeviceItem(clickDeviceItem);
						// 并返回原来的界面
						Intent data = new Intent();
						Bundle bundle = new Bundle();
						if (isBelong) {
							HashMap<String, ArrayList<Integer>> map = getUpdateInfo(
									clickDeviceItem, mPreviewDeviceItems);
							bundle.putBoolean("priviewUpdate", true);
							bundle.putIntegerArrayList("indexes",
									map.get("indexs"));
							bundle.putIntegerArrayList("channelids",
									map.get("channelids"));
							ArrayList<Integer> channelids = map
									.get("channelids");
							ArrayList<Integer> indexs = map.get("indexs");
							for (int i = 0; i < map.get("indexs").size(); i++) {
								PreviewDeviceItem temp = new PreviewDeviceItem();
								temp.setSvrIp(svrIp);
								temp.setSvrPort(svrPt);
								temp.setLoginPass(lPass);
								temp.setLoginUser(lUser);
								temp.setDeviceRecordName(dName);
								temp.setPlatformUsername(cName);
								temp.setChannel(channelids.get(i));
								mPreviewDeviceItems.set(indexs.get(i), temp);
							}

							if (clickDeviceItem.isUsable() && noRadioButton.isChecked()) {
								setNewPreviewDeviceItems();
							}
						}
						clickDeviceItem.setUsable(yesRadioButton.isChecked());
						bundle.putSerializable("cDeviceItem", clickDeviceItem);
						data.putExtras(bundle);
						setResult(REQUESTCODE, data);
						DeviceEditableActivity.this.finish();
					} else if (isPort && !isIp) {
						String text = getString(R.string.device_manager_deviceeditable_ip_wrong);
						Toast.makeText(DeviceEditableActivity.this, text,Toast.LENGTH_SHORT).show();
					} else if (!isPort && isIp) {
//						String text = getString(R.string.device_manager_deviceeditable_port_wrong);
						String text = getString(R.string.device_manager_collect_add_not_ext65535);
						Toast.makeText(DeviceEditableActivity.this, text,Toast.LENGTH_SHORT).show();
					} else {
						String text = getString(R.string.device_manager_deviceeditable_ip_port_wrong);
						Toast.makeText(DeviceEditableActivity.this, text,Toast.LENGTH_SHORT).show();
					}
				} else {
					String text = getString(R.string.device_manager_edit_notnull);
					Toast.makeText(DeviceEditableActivity.this, text,Toast.LENGTH_SHORT).show();
				}
			}
		});

		identifyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				identifyWork();
			}
		});
	}

	/** 验证链接 ***/
	protected void identifyWork() {
		getCurrentItemAndConnIdentify();
	}

	@SuppressWarnings("deprecation")
	private void getCurrentItemAndConnIdentify() {
		String svrPt = port_et.getText().toString();
		String dName = record_et.getText().toString();
		String svrIp = server_et.getText().toString();
		String lPass = password_et.getText().toString();
		String lUser = username_et.getText().toString();
		if ((!dName.trim().equals("") && !svrIp.trim().equals("") && !svrPt.trim().equals("") && !lUser.trim().equals(""))) {// 检查信息是否为空
			DeviceItem deviceItem = new DeviceItem();
			deviceItem.setDeviceName(dName);
			deviceItem.setSvrIp(svrIp);
			deviceItem.setSvrPort(svrPt);
			deviceItem.setLoginPass(lPass);
			deviceItem.setLoginUser(lUser);
			
			int port = Integer.valueOf(svrPt);
			if (port < 0 || (port > 65535)) {
				showToasContent(getString(R.string.device_manager_collect_add_not_ext65535));
			}else {
				if ((lPass != null) && (lPass.length() < 16)) {
					showDialog(CONNIDENPRG);
					connIdenTask = new EditableDevConnIdentifyTask(mHandler, deviceItem);
					connIdenTask.start();
				}else{
					showToasContent(getString(R.string.device_manager_collect_add_reinput_pass));
				}
			}
		}else {
			String content = getString(R.string.device_manager_deviceedit_conn_info_null);
			showToasContent(content);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONNIDENPRG:
			connIdenPrg = ProgressDialog
					.show(this,
							"",
							getString(R.string.device_manager_deviceedit_conning_and_wait),
							true, true);
			connIdenPrg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dimissPrg();
					connIdenTask.setCancel(true);
				}
			});
			return connIdenPrg;
		default:
			return null;
		}
	}

	private void dimissPrg() {
		if (connIdenPrg != null && connIdenPrg.isShowing()) {
			connIdenPrg.dismiss();
		}
	}

	private void showToasContent(String content) {
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
	}

	protected void setNewPreviewDeviceItems() {
		if ((mPreviewDeviceItems != null) & mPreviewDeviceItems.size() > 0) {
			for (PreviewDeviceItem item : mPreviewDeviceItems) {
				if (item.getPlatformUsername().equals(
						clickDeviceItem.getPlatformUsername())
						&& item.getDeviceRecordName().equals(
								clickDeviceItem.getDeviceName())) {
					deletePDeviceItems.add(item);
				}
			}

			for (int i = 0; i < deletePDeviceItems.size(); i++) {
				mPreviewDeviceItems.remove(deletePDeviceItems.get(i));
			}

			if (deletePDeviceItems.size() > 0) {
				GlobalApplication.getInstance().getRealplayActivity()
						.setPreviewDevices(mPreviewDeviceItems);
				GlobalApplication.getInstance().getRealplayActivity()
						.notifyPreviewDevicesContentChanged();
			}
		}
	}

	protected boolean isBelongDeviceItem(DeviceItem clickDeviceItem2) {
		boolean isBelong = false;
		if (mPreviewDeviceItems == null) {
			return false;
		}
		int size = mPreviewDeviceItems.size();
		String clickUsername = clickDeviceItem2.getPlatformUsername();
		for (int i = 0; i < size; i++) {
			PreviewDeviceItem previewDeviceItem = mPreviewDeviceItems.get(i);
			String userName = previewDeviceItem.getPlatformUsername();
			if (clickUsername.equals(userName)) {
				isBelong = true;
				break;
			}
		}
		return isBelong;
	}

	protected HashMap<String, ArrayList<Integer>> getUpdateInfo(
			DeviceItem clickDeviceItem2,
			List<PreviewDeviceItem> mPreviewDeviceItems2) {
		int size = mPreviewDeviceItems2.size();
		String deviceName = clickDeviceItem.getDeviceName();
		ArrayList<Integer> indexs = new ArrayList<Integer>();
		ArrayList<Integer> channelids = new ArrayList<Integer>();
		String clickUsername = clickDeviceItem.getPlatformUsername();
		HashMap<String, ArrayList<Integer>> previewInfo = new HashMap<String, ArrayList<Integer>>();

		for (int i = 0; i < size; i++) {
			PreviewDeviceItem previewDeviceItem = mPreviewDeviceItems2.get(i);
			String userName = previewDeviceItem.getPlatformUsername();
			String pName = previewDeviceItem.getDeviceRecordName();
			if (clickUsername.equals(userName) && deviceName.equals(pName)) {
				indexs.add(i);
				channelids.add(previewDeviceItem.getChannel());
			}
		}
		previewInfo.put("indexs", indexs);
		previewInfo.put("channelids", channelids);
		return previewInfo;
	}

	protected boolean isBelongAndSetPreviewDeviceItem(
			DeviceItem clickDeviceItem2,
			List<PreviewDeviceItem> mPreviewDeviceItems2) {
		boolean isBelong = false;
		if (mPreviewDeviceItems2 == null) {
			return false;
		}

		if ((mPreviewDeviceItems2 != null)
				&& (mPreviewDeviceItems2.size() == 0)) {
			return false;
		}

		String clickUsername = clickDeviceItem2.getPlatformUsername();
		int size = mPreviewDeviceItems2.size();
		for (int i = 0; i < size; i++) {
			PreviewDeviceItem previewDeviceItem = mPreviewDeviceItems2.get(i);
			String userName = previewDeviceItem.getPlatformUsername();
			if (clickUsername.equals(userName)) {
				isBelong = true;
				break;
			}
		}
		return isBelong;
	}

	private void superChangeViewFromBase() {
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.common_drawer_device_management));

		mPreviewDeviceItems = GlobalApplication.getInstance()
				.getRealplayActivity().getPreviewDevices();

		port_et = (EditText) findViewById(R.id.et_device_add_port);
		record_et = (EditText) findViewById(R.id.et_device_add_record);
		server_et = (EditText) findViewById(R.id.et_device_add_server);
		password_et = (EditText) findViewById(R.id.et_device_add_password);
		username_et = (EditText) findViewById(R.id.et_device_add_username);
		// channelnumber_et = (EditText)
		// findViewById(R.id.et_device_add_channelnumber);
		// defaultChannel_et = (EditText)
		// findViewById(R.id.et_device_add_defaultChannel);

		noRadioButton = (RadioButton) findViewById(R.id.isenable_noi_radioBtn);
		yesRadioButton = (RadioButton) findViewById(R.id.isenable_yesi_radioBtn);

		identifyBtn = (Button) findViewById(R.id.conn_identify_btn);
		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				clickDeviceItem = (DeviceItem) bundle
						.getSerializable("clickDeviceItem");
			}
		}

		String svrIp = clickDeviceItem.getSvrIp();
		String svrPort = clickDeviceItem.getSvrPort();
		String loginPass = clickDeviceItem.getLoginPass();
		String loginUser = clickDeviceItem.getLoginUser();
		String deviceName = clickDeviceItem.getDeviceName();
		// String channelSum = clickDeviceItem.getChannelSum();
		String word4 = getString(R.string.device_manager_online_en);
		String word1 = getString(R.string.device_manager_offline_en);
		String wordLen = getString(R.string.device_manager_off_on_line_length);
		String defaultChannel = String.valueOf(clickDeviceItem.getDefaultChannel());
		if (defaultChannel.equals("0")) {
			defaultChannel = "1";
		}

		int len = Integer.valueOf(wordLen);
		if (deviceName.length() > (len - 1)) {
			String dName = deviceName.substring(0, len);
			if ((dName.contains(word1) || dName.contains(word4))) {
				deviceName = deviceName.substring(len);
			}
		}

		server_et.setText(svrIp);
		port_et.setText(svrPort);
		record_et.setText(deviceName);
		username_et.setText(loginUser);
		password_et.setText(loginPass);
		// defaultChannel_et.setText(defaultChannel);
		// channelnumber_et.setText(channelSum);
		// channelnumber_et.setKeyListener(null);
		if (clickDeviceItem.isUsable()) {
			yesRadioButton.setChecked(true);
			noRadioButton.setChecked(false);
		} else {
			yesRadioButton.setChecked(false);
			noRadioButton.setChecked(true);
		}
	}
}
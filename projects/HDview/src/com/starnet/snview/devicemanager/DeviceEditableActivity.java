package com.starnet.snview.devicemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.global.GlobalApplication;
import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.realplay.PreviewDeviceItem;
import com.starnet.snview.realplay.RealplayActivity;
import com.starnet.snview.util.IPAndPortUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("HandlerLeak")
public class DeviceEditableActivity extends BaseActivity {

	protected static final String TAG = "DeviceEditableActivity";

	private int position;
	private EditText port_et;
	private EditText record_et;
	private EditText server_et;
	private EditText username_et;
	private EditText password_et;

	private Button identifyBtn;
	private RadioButton noRadioButton;
	private RadioButton yesRadioButton;
	private DeviceItem clickDeviceItem;
	private DeviceItem originalDeviceItem;//用于保存设备修改前的信息
	private final int REQUESTCODE = 11;
	private List<PreviewDeviceItem> mPreviewDeviceItems;
	private List<PreviewDeviceItem> deletePDeviceItems = new ArrayList<PreviewDeviceItem>(); // 预览通道
	private List<DeviceItem>deviceItems;

	private ProgressDialog connIdenPrg;
	private final int CONNIDENPRG = 0x0003;
	private EditableDevConnIdentifyTask connIdenTask;
	public static final int CONNECTIFYIDENTIFY_WRONG = 0x0012;//
	public static final int CONNECTIFYIDENTIFY_SUCCESS = 0x0011;//
	public static final int CONNECTIFYIDENTIFY_TIMEOUT = 0x0013;//
	public static final int CONNECTIFYIDENTIFY_USERPSWD_ERROR = 0x0014;//
	public static final int CONNECTIFYIDENTIFY_HOST_ERROR = 0x0015;//
	public static final int CONNECTIFYIDENTIFY_LOGIN_FAIL = 0x0017;
	
	public static final int RESULT_CODE_EXPCETION = 0x1234001;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			dimissPrg();
			
			String prompt = null;
			switch (msg.what) {
			case CONNECTIFYIDENTIFY_HOST_ERROR:
				prompt = getString(R.string.device_manager_conn_iden_ip_port_error);
				break;
			case CONNECTIFYIDENTIFY_WRONG:
				prompt = getString(R.string.device_manager_deviceedit_conniden_fail);
				break;
			case CONNECTIFYIDENTIFY_SUCCESS:
				prompt = getString(R.string.device_manager_deviceedit_conniden_succ);
				break;
			case CONNECTIFYIDENTIFY_TIMEOUT:
				prompt = getString(R.string.device_manager_deviceedit_conniden_timeout_ip_port_correct);
				break;
			case CONNECTIFYIDENTIFY_USERPSWD_ERROR:
				prompt = getString(R.string.device_manager_deviceedit_conniden_error);
				break;
			case CONNECTIFYIDENTIFY_LOGIN_FAIL:
				prompt = getErrorMessage(msg.arg1);
				break;
			}
			
			showToasContent(prompt);
		}
	};
	
	private String getErrorMessage(int errorCode) {
		String errorMessage = null;
		switch (errorCode) {
		case 0:  // 兼容旧版，登录服务器失败，原因即用户或密码错误
			errorMessage = getString(R.string.connection_response_user_pwd_error);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_SUCC:				// 登录服务器成功
			errorMessage = getString(R.string.connection_response_login_success);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_USER_PWD_ERROR:		// 用户名或密码错
			errorMessage = getString(R.string.connection_response_user_pwd_error);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_PDA_VERSION_ERROR:	// 版本不一致
			errorMessage = getString(R.string.connection_response_pda_version_error);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_MAX_USER_ERROR:	    // 已达最大用户数
			errorMessage = getString(R.string.connection_response_max_user_error);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_DEVICE_OFFLINE:		// 设备已经离线
			errorMessage = getString(R.string.connection_response_device_offline);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_DEVICE_HAS_EXIST:	// 设备已经存在
			errorMessage = getString(R.string.connection_response_device_has_exist);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_DEVICE_OVERLOAD:		// 设备性能超载(设备忙)
			errorMessage = getString(R.string.connection_response_device_overload);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_INVALID_CHANNLE:		// 设备不支持的通道
			errorMessage = getString(R.string.connection_response_invalid_channel);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_PROTOCOL_ERROR:		// 协议解析出错
			errorMessage = getString(R.string.connection_response_protocol_error);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_NOT_START_ENCODE:	// 未启动编码
			errorMessage = getString(R.string.connection_response_not_start_encode);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_TASK_DISPOSE_ERROR:	// 任务处理过程出错
			errorMessage = getString(R.string.connection_response_task_dispose_error);
			break;
		case Constants.RESPONSE_CODE._RESPONSECODE_NO_PERMISSION:  		// 无权限
			errorMessage = getString(R.string.connection_response_no_permission);
			break;
		default: 
			errorMessage = getString(R.string.connection_response_unknown_error);
			break;
		}
		
		return errorMessage;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manager_editable_acitivity);
		try {
			superChangeViewFromBase();
			setListeners();
		} catch (Exception e) {
			e.printStackTrace();
			setResult(RESULT_CODE_EXPCETION);
		}
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
				
				String cName = DeviceEditableActivity.this.getString(R.string.device_manager_collect_device);
				if ((!dName.trim().equals("") && !svrIp.trim().equals("") && !svrPt.trim().equals("") && !lUser.trim().equals(""))) {// 检查信息是否为空
					boolean isIp = IPAndPortUtils.isIp(svrIp);
					boolean isPort = IPAndPortUtils.isNetPort(svrPt);
					if (!isIp) {
						showToasContent(getString(R.string.device_manager_deviceeditable_ip_wrong));
						return ;
					}
					
					if (!isPort) {
						showToasContent(getString(R.string.device_manager_collect_add_not_ext65535));
						return ;
					}
					
					if (lUser!=null&&(lUser.length() > 32)) {
						showToasContent(getString(R.string.device_manager_collect_username_ext32));
						return ;
					}
					
					if (lPass != null && (lPass.length() >= 16)) {
						showToasContent(getString(R.string.device_manager_collect_add_pswdnot_ext16));
						return;
					}
					
					if (isPort && isIp) {
						
						Intent data = new Intent();
						Bundle bundle = new Bundle();
						
						clickDeviceItem.setSvrIp(svrIp);
						clickDeviceItem.setSvrPort(svrPt);
						clickDeviceItem.setLoginUser(lUser);
						clickDeviceItem.setLoginPass(lPass);
						clickDeviceItem.setDeviceName(dName);
						clickDeviceItem.setUsable(yesRadioButton.isChecked());
						
						boolean isChanged = checkChanged();
						if(!isChanged){//收藏设备未改变
							//如果由禁用改为启用；或者由启用改为禁用，需要通知设备进行了更改；
							if (!(originalDeviceItem.isUsable() == clickDeviceItem.isUsable())) {
								notitifyPreviewItemsUpdate();
							}							
							bundle.putSerializable("cDeviceItem", clickDeviceItem);
							data.putExtras(bundle);
							setResult(REQUESTCODE, data);
							DeviceEditableActivity.this.finish();
						}else{//收藏设备有改变
							boolean isContained = checkContainItemList();
							if (isContained) {//如果包含的话，则弹出对话框，询问是否覆盖
								Toast.makeText(DeviceEditableActivity.this, getString(R.string.device_edit_contain_same), Toast.LENGTH_SHORT).show();
							}else{//如果bu包含的话，则修改对应的预览通道
								boolean isBelong = isBelongDeviceItem(originalDeviceItem);
								if (isBelong) {
									HashMap<String, ArrayList<Integer>> map = getUpdateInfo(clickDeviceItem, mPreviewDeviceItems);
									bundle.putBoolean("priviewUpdate", true);
									bundle.putIntegerArrayList("indexes",map.get("indexs"));
									bundle.putIntegerArrayList("channelids",map.get("channelids"));
									ArrayList<Integer> channelids = map.get("channelids");
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
									//检测是否是密码等其他信息更改了
									isChanged = checkChanged();
									if (isChanged && clickDeviceItem.isUsable()) {
										updatePreviewDeviceItems();
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
							}
						}
						// 并返回原来的界面
					} else if (isPort && !isIp) {
						String text = getString(R.string.device_manager_deviceeditable_ip_wrong);
						Toast.makeText(DeviceEditableActivity.this, text,Toast.LENGTH_SHORT).show();
					} else if (!isPort && isIp) {
						String text = getString(R.string.device_manager_collect_add_not_ext65535);
						Toast.makeText(DeviceEditableActivity.this, text,Toast.LENGTH_SHORT).show();
					} else {
						String text1 = getString(R.string.device_manager_deviceeditable_ip_wrong);
						Toast.makeText(DeviceEditableActivity.this, text1,Toast.LENGTH_SHORT).show();
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
	
	/**通知实时预览界面，预览通道刷新*/
	private void notitifyPreviewItemsUpdate() {
		if ((mPreviewDeviceItems != null) & mPreviewDeviceItems.size() > 0) {
			if (!clickDeviceItem.isUsable()) {// 删除禁用的
				List<PreviewDeviceItem> disableItems = getDisablePreviewItems();
				removeDisableItemsFromPreviewItems(disableItems);
			}else{
				List<PreviewDeviceItem> enableItems = getNeedEnablePreviewItems();
				addEnablePreviewItems(enableItems);
			}

			RealplayActivity activity = GlobalApplication.getInstance().getRealplayActivity();
			if (activity != null) {
				activity.setPreviewDevices(mPreviewDeviceItems);
				activity.notifyPreviewDevicesContentChanged();
			}
		}
	}
	
	/**添加需要添加的设备*/
	private void addEnablePreviewItems(List<PreviewDeviceItem>enableItems){
		if (enableItems != null && enableItems.size() > 0) {
			for (PreviewDeviceItem item : enableItems) {
				boolean hasAdded = checkExistPreviewItem(item);
				if (!hasAdded) {
					mPreviewDeviceItems.add(item);
				}
			}
		}
	}
	
	/**检测是否已经存在于预览通道之中,如果添加返回true，否则返回false*/
	private boolean checkExistPreviewItem(PreviewDeviceItem item){
		boolean result = false;
		
		if(mPreviewDeviceItems==null||(mPreviewDeviceItems.size()==0)){
			return false;
		}
		
		String device = getString(R.string.device_manager_collect_device);
		String record = item.getDeviceRecordName();
		int itemChnl = item.getChannel();
		
		for (PreviewDeviceItem tempItem:mPreviewDeviceItems) {
			if (device.equals(tempItem.getPlatformUsername())&&record.equals(tempItem.getDeviceRecordName())&&(tempItem.getChannel()==itemChnl)) {
				return true;
			}
		}
		
		return result;
	}
	
	/**获取需要重新启用的设备*/
	private List<PreviewDeviceItem> getNeedEnablePreviewItems(){
		List<PreviewDeviceItem> oriItems = ReadWriteXmlUtils.getPreviewItemListInfoFromXML(ChannelListActivity.previewFilePath);
		List<PreviewDeviceItem> enableItems = new ArrayList<PreviewDeviceItem>();
		String device = getString(R.string.device_manager_collect_device);
		String record = clickDeviceItem.getDeviceName();
		if (oriItems != null && oriItems.size() > 0) {
			for (PreviewDeviceItem item : oriItems) {
				if (device.equals(item.getPlatformUsername())&&record.equals(item.getDeviceRecordName())) {
					enableItems.add(item);
				}
			}
		}
		return enableItems;
	}
	
	/**移除需要禁用的设备*/
	private void removeDisableItemsFromPreviewItems(List<PreviewDeviceItem> disableItems){
		if (disableItems != null && disableItems.size() > 0) {
			for (PreviewDeviceItem item:disableItems ) {
				mPreviewDeviceItems.remove(item);
			}
		}
	}
	
	/**获取需要禁用预览通道*/
	private List<PreviewDeviceItem> getDisablePreviewItems(){
		String device = getString(R.string.device_manager_collect_device);
		String record = clickDeviceItem.getDeviceName();
		List<PreviewDeviceItem> disableItems = new ArrayList<PreviewDeviceItem>();
		for (PreviewDeviceItem item : mPreviewDeviceItems) {
			if (device.equals(item.getPlatformUsername())&&record.equals(item.getDeviceRecordName())) {
				disableItems.add(item);
			}
		}
		return disableItems;
	}
	
	/**弹出覆盖对话框**/
	public void jumpCoverDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.create();
		builder.setTitle(R.string.device_edit_contain_cover);
		
		builder.setPositiveButton(R.string.device_edit_contain_cover_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				coverAction();
			}
		});
		
		builder.setNegativeButton(R.string.device_edit_contain_cover_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DeviceEditableActivity.this.finish();
			}
		});
		
		builder.show();
	}
	
	/**完成覆盖操作**/
	private void coverAction(){
		
	}
	
	/**检测修改后的设备是否包含在设备列表中**/
	boolean checkContainItemList(){
		boolean result = false;
		
		if ( deviceItems == null || deviceItems.size()==0 ) {
			return false;
		}
		
		int size = deviceItems.size();
		for (int i = 0; i < size; i++) {
			if (i != position) {
				if (deviceItems.get(i).getDeviceName().equals(clickDeviceItem.getDeviceName())) {
					result = true;
					break;
				}
			}
		}
		
		return result;
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
			
			boolean isIP = IPAndPortUtils.isIp(svrIp);
			if ((svrIp==null)||svrIp.trim().equals("")||!isIP) {
				String text1 = getString(R.string.device_manager_deviceeditable_ip_wrong);
				Toast.makeText(DeviceEditableActivity.this, text1,Toast.LENGTH_SHORT).show();
				return ;
			}
			
			boolean isPort = IPAndPortUtils.isNetPort(svrPt);
			if ((svrPt==null)||svrPt.trim().equals("")||!isPort) {
				showToasContent(getString(R.string.device_manager_collect_add_not_ext65535));
				return;
			}
			
			if (lUser!=null&&(lUser.length()>32)) {
				showToasContent(getString(R.string.device_manager_collect_username_ext32));
				return;
			}
			
			if ((lPass == null)||((lPass != null) && (lPass.length() < 16))) {
				showDialog(CONNIDENPRG);
				connIdenTask = new EditableDevConnIdentifyTask(mHandler,deviceItem);
				connIdenTask.start();
			} else {
				showToasContent(getString(R.string.device_manager_collect_add_pswdnot_ext16));
				return;
			}
			
		}else {
			String content = getString(R.string.device_manager_deviceedit_conn_info_null);
			showToasContent(content);
			return;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case CONNIDENPRG:
			connIdenPrg = ProgressDialog.show(this,"",getString(R.string.device_manager_deviceedit_conning_and_wait),true, true);
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
	
	/**更新预览通道;将预览通道中来源于该设备的预览通道的信息进行更新**/
	protected void updatePreviewDeviceItems() {
		if ((mPreviewDeviceItems != null) & mPreviewDeviceItems.size() > 0) {
			
			for (PreviewDeviceItem item : mPreviewDeviceItems) {
				if (item.getPlatformUsername().equals(originalDeviceItem.getPlatformUsername())&& item.getDeviceRecordName().equals(originalDeviceItem.getDeviceName())) {
					item.setDeviceRecordName(clickDeviceItem.getDeviceName());
					item.setLoginPass(clickDeviceItem.getLoginPass());
					item.setLoginUser(clickDeviceItem.getLoginUser());
					item.setSvrIp(clickDeviceItem.getSvrIp());
					item.setSvrPort(clickDeviceItem.getSvrPort());
				}
			}
			
			RealplayActivity activity = GlobalApplication.getInstance().getRealplayActivity();
			if(activity!=null){
				activity.setPreviewDevices(mPreviewDeviceItems);
				activity.notifyPreviewDevicesContentChanged();
			}
			
		}
	}

	/**设置新的预览通道****/
	protected void setNewPreviewDeviceItems() {
		if ((mPreviewDeviceItems != null) & mPreviewDeviceItems.size() > 0) {
			for (PreviewDeviceItem item : mPreviewDeviceItems) {
				if (item.getPlatformUsername().equals(clickDeviceItem.getPlatformUsername())&& item.getDeviceRecordName().equals(clickDeviceItem.getDeviceName())) {
					deletePDeviceItems.add(item);
				}
			}

			for (int i = 0; i < deletePDeviceItems.size(); i++) {
				mPreviewDeviceItems.remove(deletePDeviceItems.get(i));
			}

			if (deletePDeviceItems.size() > 0) {
				RealplayActivity activity = GlobalApplication.getInstance().getRealplayActivity();
				if(activity!=null){
					activity.setPreviewDevices(mPreviewDeviceItems);
					activity.notifyPreviewDevicesContentChanged();
				}
			}
		}
	}

	/**预览设备中是否包含来源于修改的设备****/
	protected boolean isBelongDeviceItem(DeviceItem clickDeviceItem2) {
		boolean isBelong = false;
		if (mPreviewDeviceItems == null || mPreviewDeviceItems.size()==0) {
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

	protected HashMap<String, ArrayList<Integer>> getUpdateInfo(DeviceItem clickDeviceItem2,List<PreviewDeviceItem> mPreviewDeviceItems2) {
		
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

	protected boolean isBelongAndSetPreviewDeviceItem(DeviceItem clickDeviceItem2,List<PreviewDeviceItem> mPreviewDeviceItems2) {
		boolean isBelong = false;
		if (mPreviewDeviceItems2 == null) {
			return false;
		}

		if ((mPreviewDeviceItems2 != null) && (mPreviewDeviceItems2.size() == 0)) {
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
		
		RealplayActivity mActivity = GlobalApplication.getInstance().getRealplayActivity();
		if(mActivity!=null){
			mPreviewDeviceItems = mActivity.getPreviewDevices();
		}
		
		port_et = (EditText) findViewById(R.id.et_device_add_port);
		record_et = (EditText) findViewById(R.id.et_device_add_record);
		server_et = (EditText) findViewById(R.id.et_device_add_server);
		password_et = (EditText) findViewById(R.id.et_device_add_password);
		username_et = (EditText) findViewById(R.id.et_device_add_username);

		noRadioButton = (RadioButton) findViewById(R.id.isenable_noi_radioBtn);
		yesRadioButton = (RadioButton) findViewById(R.id.isenable_yesi_radioBtn);

		identifyBtn = (Button) findViewById(R.id.conn_identify_btn);
		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				clickDeviceItem = (DeviceItem) bundle.getSerializable("clickDeviceItem");
				position = bundle.getInt("position");
				copyClickItemInfoToOriginalItem();
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
		if (clickDeviceItem.isUsable()) {
			yesRadioButton.setChecked(true);
			noRadioButton.setChecked(false);
		} else {
			yesRadioButton.setChecked(false);
			noRadioButton.setChecked(true);
		}
		
		try {
			deviceItems = ReadWriteXmlUtils.getCollectDeviceListFromXML(ChannelListActivity.filePath);
		} catch (Exception e) {
			deviceItems = null;
		}
	}

	//用于保存设备修改前的信息
	private void copyClickItemInfoToOriginalItem() {
		originalDeviceItem = new DeviceItem();
		
		originalDeviceItem.setPlatformUsername(clickDeviceItem.getPlatformUsername());
		originalDeviceItem.setDeviceName(clickDeviceItem.getDeviceName());
		originalDeviceItem.setSvrIp(clickDeviceItem.getSvrIp());
		originalDeviceItem.setSvrPort(clickDeviceItem.getSvrPort());
		originalDeviceItem.setLoginUser(clickDeviceItem.getLoginUser());
		originalDeviceItem.setLoginPass(clickDeviceItem.getLoginPass());
		originalDeviceItem.setUsable(clickDeviceItem.isUsable());
		originalDeviceItem.setDefaultChannel(clickDeviceItem.getDefaultChannel());
		
	}

//检测当前的设备信息与之前的信息是否进行了修改，如果修改返回为true，否则返回为false
	protected boolean checkChanged() {
		
		if(!originalDeviceItem.getDeviceName().equals(clickDeviceItem.getDeviceName())){
			return true;
		}
		
		if (!originalDeviceItem.getSvrIp().equals(clickDeviceItem.getSvrIp())) {
			return true;
		}
		
		if (!originalDeviceItem.getSvrPort().equals(clickDeviceItem.getSvrPort())) {
			return true;
		}
		
		if (!originalDeviceItem.getLoginUser().equals(clickDeviceItem.getLoginUser())) {
			return true;
		}
		
		if (!originalDeviceItem.getLoginPass().equals(clickDeviceItem.getLoginPass())) {
			return true;
		}
				
		return false;
	}
}
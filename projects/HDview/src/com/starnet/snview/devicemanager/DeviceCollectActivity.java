package com.starnet.snview.devicemanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.xml.ConnectionIdentifyTask;
import com.starnet.snview.channelmanager.xml.DVRDevice;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.IPAndPortUtils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.PinyinComparatorUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;
import com.starnet.snview.util.SynObject;

@SuppressLint("SdCardPath")
public class DeviceCollectActivity extends BaseActivity {

	private static final String TAG = "DeviceCollectActivity";

	private final String filePath = "/data/data/com.starnet.snview/deviceItem_list.xml";// 用于保存收藏设备...
	private final String fileName = "/data/data/com.starnet.snview/star_cloudAccount.xml";// 用于从文档中获取所有的用户
	private final int RESULTCODE = 11;
	private final int REQUESTCODE = 10;// 用于进入DeviceChooseActivity.java的请求码；

	private Button leftButton;
	private Button conn_identify_btn;
	private Button device_add_shdong_btn;
	private Button device_add_choose_btn;// 选择按钮，单击可从网络下载星云平台数据,"选择添加"

	private RadioButton noRadioButton;
	private boolean isConnPass = false;// 验证是否通过
	private boolean isIdentify = false;// 是否进行了验证
	private RadioButton yesRadioButton;
	private DeviceItem validatedDeviceItem;// 验证后的设备
	private ConnectionIdentifyTask conIdenTask;
	private final int CONNECTIFYIDENTIFY_WRONG = 0x0012;
	private final int CONNECTIFYIDENTIFY_SUCCESS = 0x0011;
	private final int CONNECTIFYIDENTIFY_TIMEOUT = 0x0013;

	private int auto_flag = 1;
	private EditText et_device_choose;
	private EditText et_device_add_port;
	private EditText et_device_add_record;
	private EditText et_device_add_server;
	private EditText et_device_add_username;
	private EditText et_device_add_password;
	// private EditText et_device_add_channelnumber;
	private EditText et_device_add_defaultchannel;

	private DeviceItem saveDeviceItem = new DeviceItem();
	private final int CONNIDENTIFYDIALOG = 5;// 从网络下载数据
	private final int LOADNETDATADIALOG = 1;// 从网络下载数据
	private final int LOAD_SUCCESS = 2;
	private final int LOAD_WRONG = 100;
	private SynObject synObject = new SynObject();
	private List<DVRDevice> dvrDeviceList = new ArrayList<DVRDevice>();// 保存全部数据

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressWarnings("deprecation")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOAD_SUCCESS:
				if (synObject.getStatus() == SynObject.STATUS_RUN) {
					return;
				}
				dismissDialog(LOADNETDATADIALOG);
				synObject.resume();// 解除线程挂起,向下继续执行...
				if (dvrDeviceList.size() > 0) {
					Collections
							.sort(dvrDeviceList, new PinyinComparatorUtils());
				}
				dismissDialog(LOADNETDATADIALOG);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("dvrDeviceList",
						(ArrayList<? extends Parcelable>) dvrDeviceList);
				intent.putExtras(bundle);
				intent.setClass(DeviceCollectActivity.this,
						DeviceChooseActivity.class);
				startActivityForResult(intent, REQUESTCODE);
				break;
			case CONNECTIFYIDENTIFY_WRONG:
				isIdentify = true;
				isConnPass = false;
				saveDeviceItem.setIdentify(true);
				dismissDialog(CONNIDENTIFYDIALOG);
				validatedDeviceItem = getIdentifyDeviceItem(msg);
				showToast(getString(R.string.device_manager_conn_iden_wrong));
				break;
			case CONNECTIFYIDENTIFY_SUCCESS:
				isIdentify = true;
				isConnPass = true;
				auto_flag = 2;
				msg.getData();
				saveDeviceItem.setIdentify(true);
				dismissDialog(CONNIDENTIFYDIALOG);
				validatedDeviceItem = getIdentifyDeviceItem(msg);
				saveDeviceItem.setChannelList(validatedDeviceItem.getChannelList());
				saveDeviceItem.setChannelSum(validatedDeviceItem.getChannelSum());
				showToast(getString(R.string.device_manager_conn_iden_sucess));
				break;
			case CONNECTIFYIDENTIFY_TIMEOUT:
				isIdentify = true;
				isConnPass = false;
				saveDeviceItem.setIdentify(true);
				dismissDialog(CONNIDENTIFYDIALOG);
				validatedDeviceItem = getIdentifyDeviceItem(msg);
				showToast(getString(R.string.device_manager_conn_iden_timout));
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_manage_add_baseactivity);
		superChangeViewFromBase();
		setListeners();
	}

	private void showToast(String content) {
		Toast.makeText(DeviceCollectActivity.this, content, Toast.LENGTH_SHORT)
				.show();
	};

	/*** 获取验证后的收藏设备 ***/
	private DeviceItem getIdentifyDeviceItem(Message msg) {
		Bundle data = msg.getData();
		DeviceItem dItem = (DeviceItem) data.getSerializable("identifyDeviceItem");
		return dItem;
				
	}

	/*** 获取需要验证的设备 ***/
	private DeviceItem getDeviceItem() {
		DeviceItem deviceItem = new DeviceItem();
		String platName = getString(R.string.device_manager_collect_device);
		List<String> infoList = getDeviceItemInfoFromEdx();
		int index = checkIfExistNull(infoList);
		if (index == -1) {
			deviceItem.setPlatformUsername(platName);
			deviceItem.setDeviceName(infoList.get(0));
			deviceItem.setLoginPass(infoList.get(4));
			deviceItem.setLoginUser(infoList.get(3));
			deviceItem.setSvrPort(infoList.get(2));
			deviceItem.setSvrIp(infoList.get(1));
			return deviceItem;
		} else if (index == 0) {
			showToast(getString(R.string.device_manager_conn_iden_devicename_notnull));
			return null;
		} else if (index == 1) {
			showToast(getString(R.string.device_manager_conn_iden_svrip_notnull));
			return null;
		} else if (index == 2) {
			showToast(getString(R.string.device_manager_conn_iden_svrport_notnull));
			return null;
		} else {
			showToast(getString(R.string.device_manager_conn_iden_username_notnull));
			return null;
		}
	}

	private List<String> getDeviceItemInfoFromEdx() {
		List<String> infoList = new ArrayList<String>();
		String deviName = et_device_add_record.getText().toString();
		String svrIp = et_device_add_server.getText().toString();
		String svrPort = et_device_add_port.getText().toString();
		String loginUser = et_device_add_username.getText().toString();
		String loginPass = et_device_add_password.getText().toString();
		infoList.add(deviName);
		infoList.add(svrIp);
		infoList.add(svrPort);
		infoList.add(loginUser);
		infoList.add(loginPass);
		return infoList;
	}

	/*** 检查信息列表是否包含空的选项，如果包含，则返回其索引；如果不包含则返回-1 ****/
	private int checkIfExistNull(List<String> infoList) {
		int index = -1;
		for (int i = 0; i < infoList.size() - 1; i++) {
			if (infoList.get(i).trim() == null
					|| infoList.get(i).trim().equals("")) {
				index = i;
				break;
			}
		}
		return index;
	}

	private void setListeners() {

		conn_identify_btn.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				DeviceItem deviceItem = getDeviceItem();
				if (deviceItem != null) {
					showDialog(CONNIDENTIFYDIALOG);
					conIdenTask = new ConnectionIdentifyTask(mHandler,deviceItem);
					conIdenTask.setContext(DeviceCollectActivity.this);
					conIdenTask.start();
				}
			}
		});

		leftButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DeviceCollectActivity.this.finish();
			}
		});
		device_add_shdong_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 判断用户设置为空的时候
				String recdName = getEditTextString(et_device_add_record)
						.trim();
				String serverIP = getEditTextString(et_device_add_server)
						.trim();// IP地址
				String serverPort = getEditTextString(et_device_add_port)
						.trim();// 端口号
				String userName = getEditTextString(et_device_add_username)
						.trim();
				String password = getEditTextString(et_device_add_password)
						.trim();

				// 当所有的内容都不为空的时候，则保存到指定的文档中
				if (!recdName.equals("") && !serverIP.equals("")
						&& !serverPort.equals("") && !userName.equals("")) {
					boolean isIP = IPAndPortUtils.isIp(serverIP);
					boolean isPort = IPAndPortUtils.isNetPort(serverPort);
					if (isPort && isIP) {
						String defaultChannel = getEditTextString(
								et_device_add_defaultchannel).trim();
						int dChannel = 1;
						if (defaultChannel != null
								&& !defaultChannel.equals("")) {
							dChannel = Integer.valueOf(defaultChannel);
						}
						// int channelNum = Integer.valueOf(channelNumber);
						saveDeviceItem.setDeviceName(recdName);
						// saveDeviceItem.setChannelSum(channelNumber);
						saveDeviceItem.setLoginUser(userName);
						saveDeviceItem.setLoginPass(password);
						saveDeviceItem.setDefaultChannel(dChannel);
						saveDeviceItem.setSvrIp(serverIP);
						saveDeviceItem.setSvrPort(serverPort);
						saveDeviceItem.setSecurityProtectionOpen(true);
						String platformUsername = getString(R.string.device_manager_collect_device);
						saveDeviceItem.setPlatformUsername(platformUsername);
						try {
							if (auto_flag == 1) {
								saveDeviceItem.setChannelSum("1");
								List<Channel> channelList = new ArrayList<Channel>();
								Channel channel = new Channel();
								String text = getString(R.string.device_manager_channel);
								channel.setChannelName(text + "1");
								channel.setChannelNo(dChannel);
								channel.setSelected(false);
								channelList.add(channel);
								saveDeviceItem.setChannelList(channelList);
							}
							if (yesRadioButton.isChecked()) {
								if (saveDeviceItem.isIdentify()) {// 用户进行了连接验证
									if (isConnPass) {// 验证通过
										boolean isSame = checkSaveAndIdentifyDeviceIsSame(
												saveDeviceItem,validatedDeviceItem);
										if (isSame) {
											saveIdentifyDeviceItemToXML(true);// 验证通过后保存用户信息
										} else {
											popupDialogAskIdentify();
										}
									} else {
										saveIdentifyDeviceItemToXML(false);// 验证通过后保存用户信息
									}
								} else {// 尚未进行过验证,弹出提示框
									popupDialogAskIdentify();
								}
							} else {
								saveIdentifyDeviceItemToXML(false);// 用户选择不验证，直接保存
							}
						} catch (Exception e) {
							showToast(getString(R.string.device_manager_save_failed));
						}
					} else if (!isPort) {
						showToast(getString(R.string.device_manager_port_wrong));
					} else {
						showToast(getString(R.string.device_manager_collect_ip_wrong));
					}
				} else {
					showToast(getString(R.string.device_manager_collect_null_wrong));
				}
			}
		});
		device_add_choose_btn.setOnClickListener(new OnClickListener() {
			// 从网络获取数据，获取后，进入DeviceChooseActivity界面；单击返回后，则不进入；
			@Override
			public void onClick(View v) {
				Context context = DeviceCollectActivity.this;
				boolean isConn = NetWorkUtils.checkNetConnection(context);
				if (isConn) {
					try {
						List<CloudAccount> cloudAccountList = ReadWriteXmlUtils.getCloudAccountList(fileName);
						int size = cloudAccountList.size();
						if (size > 0) {
							boolean usable = checkAccountUsable(cloudAccountList);
							if (usable) {
								if (dvrDeviceList.size() > 0) {
									dvrDeviceList.clear();
								}
								requestNetDataFromNet();
								synObject.suspend();
							} else {
								showToast(getString(R.string.check_account_enabled));
							}
						} else {
							showToast(getString(R.string.check_account_addable));
						}
					} catch (Exception e) {
						showToast(getString(R.string.check_account_addable));
					}
				} else {
					showToast(getString(R.string.network_not_conn));
				}
			}
		});
	}
	
	/***弹出对话框，询问用户是否进行验证***/
	private void popupDialogAskIdentify(){
		String ok = getString(R.string.device_manager_connection_identify_ok);
		Builder builder = new Builder(DeviceCollectActivity.this);
		builder.setTitle(R.string.device_manager_please_identify);
		builder.setPositiveButton(ok, null);
		builder.show();
	}

	/*** 保存收藏设备到xml文档中 ***/
	private void saveIdentifyDeviceItemToXML(boolean identify) throws Exception {
		
		if (auto_flag != 1) {
			saveDeviceItem.setIdentify(true);
		}else{
			saveDeviceItem.setIdentify(identify);
		}
		List<DeviceItem> collectList = ReadWriteXmlUtils
				.getCollectDeviceListFromXML(filePath);
		boolean isExist = checkDeviceItemListExist(saveDeviceItem, collectList);// 检查列表中是否存在该用户
		if (isExist) {// 弹出对话框,用户选择确定时，则添加覆盖；
			Builder builder = new Builder(DeviceCollectActivity.this);
			builder.setTitle(getString(R.string.device_manager_devicecollect_cover));
			builder.setNegativeButton(
					getString(R.string.device_manager_devicecollect_cancel),
					null);
			builder.setPositiveButton(
					getString(R.string.device_manager_devicecollect_ensure),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								auto_flag = 1;
								String saveResult = ReadWriteXmlUtils.addNewDeviceItemToCollectEquipmentXML(
												saveDeviceItem, filePath);// 保存
								showToast(saveResult);
								Intent intent = new Intent();
								Bundle bundle = new Bundle();
								bundle.putSerializable("saveDeviceItem",saveDeviceItem);
								intent.putExtras(bundle);
								setResult(11, intent);
								DeviceCollectActivity.this.finish();
							} catch (Exception e) {

							}
						}
					});
			builder.show();
		} else {// 如果不存在设备，则直接添加...
			ReadWriteXmlUtils.addNewDeviceItemToCollectEquipmentXML(saveDeviceItem, filePath);// 保存
			String saveResult = getString(R.string.device_manager_save_success);
			showToast(saveResult);
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("saveDeviceItem", saveDeviceItem);
			intent.putExtras(bundle);
			setResult(11, intent);
			DeviceCollectActivity.this.finish();// 添加成功后，关闭页面
		} // 添加成功后，关闭页面
	}

	/*** 判断验证设备的信息与要保存的设备信息是否一致，一致返回true，否则，返回false ***/
	private boolean checkSaveAndIdentifyDeviceIsSame(DeviceItem item,
			DeviceItem jItem) {

		boolean isSame = false;
		if (item.isIdentify()&&jItem == null) {
			return true;
		}
		if (!item.isIdentify()&&jItem == null) {
			return false;
		}
		String iSvIp = item.getSvrIp();
		String iSvPt = item.getSvrPort();
		String iUser = item.getLoginUser();
		String ipswd = item.getLoginPass();

		String jSvIp = jItem.getSvrIp();
		String jSvPt = jItem.getSvrPort();
		String jUser = jItem.getLoginUser();
		String jpswd = jItem.getLoginPass();
		if (ipswd==null||ipswd.equals("")) {
			if ((iSvIp.equals(jSvIp)) && (iSvPt.equals(jSvPt)) && (iUser.equals(jUser))) {
				 isSame = true;
			}else {
				 isSame = false;
			}
		}else {
			if ((iSvIp.equals(jSvIp)) && (iSvPt.equals(jSvPt)) && (iUser.equals(jUser))
					&& (ipswd.equals(jpswd)) ) {
				 isSame = true;
			}else {
				 isSame = false;
			}
		}
		return isSame;
	}

	// 检查列表中，是否存在与savDeviceItem设备同名的的设备
	protected boolean checkDeviceItemListExist(DeviceItem savDeviceItem,
			List<DeviceItem> collectList) {
		boolean isExist = false;
		int size = collectList.size();
		for (int i = 0; i < size; i++) {
			DeviceItem deviceItem = collectList.get(i);
			if (deviceItem.getDeviceName().equals(savDeviceItem.getDeviceName())) {
				isExist = true;
				break;
			}
		}
		return isExist;
	}

	@SuppressWarnings("deprecation")
	private void requestNetDataFromNet() {
		showDialog(LOADNETDATADIALOG);// 显示从网络的加载圈...
		new ObtainDeviceDataFromNetThread(mHandler).start();
	}

	private boolean checkAccountUsable(List<CloudAccount> cloudAccountList) {
		boolean usable = false;
		int size = cloudAccountList.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cloudAccount = cloudAccountList.get(i);
			if (cloudAccount.isEnabled()) {
				usable = true;
				break;
			}
		}
		return usable;
	}

	private void superChangeViewFromBase() {// 得到从父类继承的控件，并修改

		leftButton = super.getLeftButton();
		device_add_shdong_btn = super.getRightButton();

		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.device_manager));
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		conn_identify_btn = (Button) findViewById(R.id.conn_identify_btn);
		et_device_add_port = (EditText) findViewById(R.id.et_device_add_port);
		et_device_choose = (EditText) findViewById(R.id.device_add_choose_et);
		et_device_add_record = (EditText) findViewById(R.id.et_device_add_record);
		et_device_add_server = (EditText) findViewById(R.id.et_device_add_server);
		device_add_choose_btn = (Button) findViewById(R.id.device_add_button_state);
		et_device_add_username = (EditText) findViewById(R.id.et_device_add_username);
		et_device_add_password = (EditText) findViewById(R.id.et_device_add_password);
		noRadioButton = (RadioButton) findViewById(R.id.device_manager_isenable_no_radioBtn);
		yesRadioButton = (RadioButton) findViewById(R.id.device_manager_isenable_yes_radioBtn);
		et_device_add_defaultchannel = (EditText) findViewById(R.id.et_device_add_defaultChannel);
		et_device_choose.setKeyListener(null);

	}

	private String getEditTextString(EditText editText) {
		String content = "";
		Editable editable = editText.getText();
		if (editable != null) {
			content = editable.toString();
		}
		return content;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case LOADNETDATADIALOG:
			ProgressDialog progress = ProgressDialog.show(this, "",
					getString(R.string.loading_devicedata_wait), true, true);
			progress.setOnCancelListener(new OnCancelListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(LOADNETDATADIALOG);
					synObject.resume();
				}
			});
			return progress;
		case CONNIDENTIFYDIALOG:
			ProgressDialog progress2 = ProgressDialog.show(this, "",
					getString(R.string.device_manager_conn_iden), true, true);
			progress2.setOnCancelListener(new OnCancelListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(CONNIDENTIFYDIALOG);
					conIdenTask.setCanceled(true);// 不进行验证
				}
			});
			return progress2;
		default:
			return null;
		}
	}

	class ObtainDeviceDataFromNetThread extends Thread {
		private Handler handler;

		public ObtainDeviceDataFromNetThread(Handler handler) {
			super();
			this.handler = handler;
		}

		@Override
		public void run() {
			super.run();
			Message msg = new Message();
			List<CloudAccount> cloudAccountList = new ArrayList<CloudAccount>();
			try {
				cloudAccountList = ReadWriteXmlUtils.getCloudAccountList(fileName);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			CloudService cloudService = new CloudServiceImpl("");
			int size = cloudAccountList.size();
			try {
				for (int i = 0; i < size; i++) {
					CloudAccount cloudAccount = cloudAccountList.get(i);
					if (cloudAccount.isEnabled()) {
						String dman = cloudAccount.getDomain();
						String port = cloudAccount.getPort();
						String usnm = cloudAccount.getUsername();
						String pasd = cloudAccount.getPassword();
						Document document = cloudService.SendURLPost(dman,
								port, usnm, pasd);
						String status = cloudService.readXmlStatus(document);
						if (status == null) {// 加载成功...
							List<DVRDevice> deviceList = cloudService
									.readXmlDVRDevices(document);
							int deviceListSize = deviceList.size();
							for (int j = 0; j < deviceListSize; j++) {
								dvrDeviceList.add(deviceList.get(j));
							}
						} else {// 加载不成功...

						}
					}
				}
				msg.what = LOAD_SUCCESS;
				handler.sendMessage(msg);
			} catch (IOException e) {
				e.printStackTrace();
				if (dvrDeviceList.size() > 0) {
					msg.what = LOAD_SUCCESS;
					handler.sendMessage(msg);
				} else {
					msg.what = LOAD_WRONG;
					handler.sendMessage(msg);
				}
			} catch (DocumentException e) {
				e.printStackTrace();
				if (dvrDeviceList.size() > 0) {
					msg.what = LOAD_SUCCESS;
					handler.sendMessage(msg);
				} else {
					msg.what = LOAD_WRONG;
					handler.sendMessage(msg);
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ((requestCode == REQUESTCODE) && (resultCode == RESULTCODE)) {
			if (data != null) {
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					DeviceItem chooseDeviceItem = (DeviceItem) bundle
							.getSerializable("chooseDeviceItem");
					saveDeviceItem.setChannelSum(chooseDeviceItem
							.getChannelSum());
					List<Channel> channelList = new ArrayList<Channel>();
					for (int i = 0; i < Integer.valueOf(chooseDeviceItem
							.getChannelSum()); i++) {
						Channel channel = new Channel();
						String text = getString(R.string.device_manager_channel);
						channel.setChannelName(text + "" + (i + 1));
						channel.setChannelNo((i + 1));
						channel.setSelected(false);
						channelList.add(channel);
					}
					isConnPass = true;
					isIdentify = true;
					noRadioButton.setChecked(false);
					yesRadioButton.setChecked(true);
					saveDeviceItem.setIdentify(true);//
					saveDeviceItem.setChannelList(channelList);
					auto_flag = bundle.getInt("auto_flag");
					String lgUsr = chooseDeviceItem.getLoginUser();
					String lgPas = chooseDeviceItem.getLoginPass();
					String svrIp = chooseDeviceItem.getSvrIp();
					String svrPt = chooseDeviceItem.getSvrPort();
					String dName = chooseDeviceItem.getDeviceName();
					int defltChl = chooseDeviceItem.getDefaultChannel();
					String dChnl = String.valueOf(defltChl);
					et_device_choose.setText(dName);
					et_device_add_record.setText(dName);
					et_device_add_server.setText(svrIp);
					et_device_add_port.setText(svrPt);
					et_device_add_username.setText(lgUsr);
					et_device_add_password.setText(lgPas);
					et_device_add_defaultchannel.setText(dChnl);
					et_device_choose.setKeyListener(null);
//					String username = chooseDeviceItem.getPlatformUsername();
//					Log.v(TAG, "DeviceCollectActivity == username:" + username);
					String usernmae = getString(R.string.device_manager_collect_device);
					saveDeviceItem.setPlatformUsername(usernmae);
					saveDeviceItem.setLoginPass(lgPas);
					saveDeviceItem.setLoginUser(lgUsr);
					saveDeviceItem.setSvrIp(svrIp);
					saveDeviceItem.setSvrPort(svrPt);
					saveDeviceItem.setDeviceName(dName);
					saveDeviceItem.setDefaultChannel(defltChl);
					setIdentifyDeviceItem(saveDeviceItem);
				}
			}
		}
	}
	
	private void setIdentifyDeviceItem(DeviceItem deviceItem){
		validatedDeviceItem = new DeviceItem();
		validatedDeviceItem.setChannelList(deviceItem.getChannelList());
		validatedDeviceItem.setChannelSum(deviceItem.getChannelSum());
		validatedDeviceItem.setDefaultChannel(deviceItem.getDefaultChannel());
		validatedDeviceItem.setDeviceName(deviceItem.getDeviceName());
		validatedDeviceItem.setDeviceType(deviceItem.getDeviceType());
		validatedDeviceItem.setExpanded(deviceItem.isExpanded());
		validatedDeviceItem.setIdentify(deviceItem.isIdentify());
		validatedDeviceItem.setLoginPass(deviceItem.getLoginPass());
		validatedDeviceItem.setLoginUser(deviceItem.getLoginUser());
		validatedDeviceItem.setPlatformUsername(deviceItem.getPlatformUsername());
		validatedDeviceItem.setSecurityProtectionOpen(deviceItem.isSecurityProtectionOpen());
		validatedDeviceItem.setSvrIp(deviceItem.getSvrIp());
		validatedDeviceItem.setSvrPort(deviceItem.getSvrPort());
	}
}
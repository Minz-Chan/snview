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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.Channel;
import com.starnet.snview.channelmanager.ChannelListActivity;
import com.starnet.snview.channelmanager.xml.CloudAccountInfoOpt;
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
	// 用于从文档中获取所有的用户
	private final int REQUESTCODE = 10; // 用于进入DeviceChooseActivity.java的请求码；
	public static final int ALL_ADD = 0x0020;

	private Button identifyBtn;
	private Button chooseBtn; // 选择按钮，单击可从网络下载星云平台数据,"选择添加"

	private RadioButton noRadioButton;
	private boolean isConnPass = false; // 验证是否通过
	private boolean isIdentify = false; // 是否进行了验证
	private RadioButton yesRadioButton;
	private DevConnIdenTask conIdenTask;
	private final int CONNECTIFYIDENTIFY_WRONG = 0x0012;
	private final int CONNECTIFYIDENTIFY_SUCCESS = 0x0011;
	private final int CONNECTIFYIDENTIFY_TIMEOUT = 0x0013;

	private int chooseactivity_return_flag = 1;
	private EditText chooseEdt;
	private EditText portEdt;
	private EditText recordEdt;
	private EditText serverEdt;
	private EditText lgUserEdt;
	private EditText lgPswdEdt;

	private final int LOAD_SUCCESS = 2;
	private final int LOAD_WRONG = 100;
	private final int LOADNETDATADIALOG = 1;
	private final int CONNIDENTIFYDIALOG = 5;

	private SynObject synObject = new SynObject();

	private List<DVRDevice> dvrDeviceList = new ArrayList<DVRDevice>();// 保存全部数据

	private List<DeviceItem> collectDeviceItemList;//
	private LoadCollectDeviceItemTask loadDataTask;

	private Context context;
	private DeviceItem saveDeviceItem = new DeviceItem();
	private DeviceItem chooseDeviceItem; // 选择之后的设备
	private DeviceItem identifyDeviceItem; // 验证之后的设备

	private ProgressDialog loadPrg;
	private ProgressDialog idenPrg;

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOAD_SUCCESS:
				if (synObject.getStatus() == SynObject.STATUS_RUN) {
					return;
				}
				synObject.resume();// 解除线程挂起,向下继续执行...
				dismissLoadPRG();
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("dvrDeviceList",(ArrayList<? extends Parcelable>) dvrDeviceList);
				intent.putExtras(bundle);
				intent.setClass(DeviceCollectActivity.this,DeviceChooseActivity.class);
				startActivityForResult(intent, REQUESTCODE);
				break;
			case CONNECTIFYIDENTIFY_WRONG:
				isIdentify = true;
				isConnPass = false;
				dismissIdenPRG();
				saveDeviceItem.setIdentify(isIdentify);
				saveDeviceItem.setConnPass(isConnPass);
				setSaveDeviceItem();
				showToast(getString(R.string.device_manager_conn_iden_wrong));
				break;
			case CONNECTIFYIDENTIFY_SUCCESS:
				isIdentify = true;
				isConnPass = true;
				dismissIdenPRG();
				saveDeviceItem.setIdentify(true);
				saveDeviceItem.setConnPass(true);
				identifyDeviceItem = getIdentifyDeviceItem(msg);
				saveDeviceItem.setChannelList(identifyDeviceItem.getChannelList());
				saveDeviceItem.setChannelSum(identifyDeviceItem.getChannelSum());
				showToast(getString(R.string.device_manager_conn_iden_sucess));
				break;
			case CONNECTIFYIDENTIFY_TIMEOUT:
				isIdentify = true;
				isConnPass = false;
				dismissIdenPRG();
				saveDeviceItem.setIdentify(isIdentify);
				saveDeviceItem.setConnPass(isConnPass);
				setSaveDeviceItem();
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

	protected void setSaveDeviceItem() {
		if (chooseactivity_return_flag == 1) {
			setNoConnPassDeviceItem();
		}
	}

	private void showToast(String content) {
		Toast.makeText(DeviceCollectActivity.this, content, Toast.LENGTH_SHORT).show();
	}

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
		String deviName = recordEdt.getText().toString();
		String svrIp = serverEdt.getText().toString();
		String svrPort = portEdt.getText().toString();
		String loginUser = lgUserEdt.getText().toString();
		String loginPass = lgPswdEdt.getText().toString();
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

		identifyBtn.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {

				if (!NetWorkUtils.checkNetConnection(DeviceCollectActivity.this)) {
					showToast(getString(R.string.device_manager_conn_iden_notopen));
				} else {
					DeviceItem deviceItem = getDeviceItem();
					if (deviceItem != null) {
						showDialog(CONNIDENTIFYDIALOG);
						conIdenTask = new DevConnIdenTask(mHandler,deviceItem);
						conIdenTask.setContext(DeviceCollectActivity.this);
						conIdenTask.start();
					}
				}
			}
		});

		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DeviceCollectActivity.this.finish();
			}
		});
		super.getRightButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveDeviceItem = getDeviceItemInfoFromUi();
				String rName = saveDeviceItem.getDeviceName().trim();
				String svIP = saveDeviceItem.getSvrIp().trim();
				String port = saveDeviceItem.getSvrPort().trim();
				String uName = saveDeviceItem.getLoginUser().trim();
				if (!rName.equals("") && !svIP.equals("") && !port.equals("")
						&& !uName.equals("")) {
					boolean isIP = IPAndPortUtils.isIp(svIP);
					boolean isPort = IPAndPortUtils.isNetPort(port);
					if (isPort && isIP) {
						try {
							if (chooseactivity_return_flag == 1) {// 表示未进行选择
								if (!isConnPass) {
									setNoConnPassDeviceItem();
								}
								saveDeviceItemToXML(saveDeviceItem,
										DeviceViewActivity.SEMI_AUTO_ADD);// 验证通过后保存用户信息
							} else {
								boolean isSame = judgeDevicItemIsSame(saveDeviceItem, chooseDeviceItem);
								if (isSame) {// 进行了选择
									saveDeviceItemToXML(saveDeviceItem,DeviceViewActivity.AUTO_ADD);
								} else {
									if (!isConnPass) {
										setNoConnPassDeviceItem();
									}
									saveDeviceItemToXML(saveDeviceItem,DeviceViewActivity.AUTO_ADD);// 验证通过后保存用户信息
								}
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
		chooseBtn.setOnClickListener(new OnClickListener() {
			// 从网络获取数据，获取后，进入DeviceChooseActivity界面；单击返回后，则不进入；
			@Override
			public void onClick(View v) {
				Context context = DeviceCollectActivity.this;
				boolean isConn = NetWorkUtils.checkNetConnection(context);
				if (isConn) {
					try {
						List<CloudAccount> cloudAccountList = ReadWriteXmlUtils
								.getCloudAccountList(CloudAccountInfoOpt.filePathOfCloudAccount);
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

	protected void setNoConnPassDeviceItem() {
		saveDeviceItem.setChannelSum("1");
		List<Channel> channelList = new ArrayList<Channel>();
		Channel channel = new Channel();
		String text = getString(R.string.device_manager_channel);
		channel.setChannelName(text + "1");
		channel.setChannelNo(1);
		channel.setSelected(false);
		channelList.add(channel);
		saveDeviceItem.setChannelList(channelList);
		saveDeviceItem.setConnPass(false);
	}

	/** 判断两个设备的ip,port,loginUser,loginPassword字段是否一致 */
	protected boolean judgeDevicItemIsSame(DeviceItem item1, DeviceItem item2) {
		boolean result = false;
		if (chooseactivity_return_flag == 1) {// 不是从choose界面中获取的收藏设备
			result = false;
		} else {
			String ip = item1.getSvrIp();
			String port = item1.getSvrPort();
			String lgUser = item1.getLoginUser();
			String lgPswd = item1.getLoginPass();
			String ipe = item2.getSvrIp();
			String porte = item2.getSvrPort();
			String lgUsere = item2.getLoginUser();
			String lgPswde = item2.getLoginPass();
			if (ip.equals(ipe) && port.equals(porte) && lgUser.equals(lgUsere)
					&& lgPswd.equals(lgPswde)) {
				result = true;
			}
		}
		return result;
	}

	private DeviceItem getDeviceItemInfoFromUi() {
		String rName = recordEdt.getText().toString();
		String svIP = serverEdt.getText().toString();// IP地址
		String sPort = portEdt.getText().toString();// 端口号
		String uName = lgUserEdt.getText().toString();
		String pswd = lgPswdEdt.getText().toString();
		saveDeviceItem.setSvrIp(svIP);
		saveDeviceItem.setSvrPort(sPort);
		saveDeviceItem.setLoginPass(pswd);
		saveDeviceItem.setLoginUser(uName);
		saveDeviceItem.setDeviceName(rName);
		saveDeviceItem.setPlatformUsername(getString(R.string.device_manager_collect_device));
		saveDeviceItem.setUsable(yesRadioButton.isChecked());
		saveDeviceItem.setIdentify(isIdentify);
		saveDeviceItem.setConnPass(isConnPass);
		saveDeviceItem.setSecurityProtectionOpen(true);
		return saveDeviceItem;
	}

	/** 保存收藏设备到xml文档中 ***/
	private void saveDeviceItemToXML(final DeviceItem dItem,
			final int resultCode) throws Exception {
		final Intent intent = new Intent();
		final int index = isContain(dItem);
		if (index == -1) {
			ReadWriteXmlUtils.addNewDeviceItemToCollectEquipmentXML(dItem,ChannelListActivity.filePath);
			Bundle bundle = new Bundle();
			intent.putExtra("replace", false);
			bundle.putSerializable("saveDeviceItem", saveDeviceItem);
			intent.putExtras(bundle);
			setResult(resultCode, intent);
			DeviceCollectActivity.this.finish();
		} else {
			// 弹出对话框，询问是否进行替换....
			Builder builder = new Builder(context);
			builder.setTitle(getString(R.string.device_manager_devicecollect_cover));
			builder.setPositiveButton(
					R.string.device_manager_devicecollect_ensure,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								intent.putExtra("replace", true);
								ReadWriteXmlUtils.replaceSpecifyDeviceItem(ChannelListActivity.filePath, index,dItem);
								Bundle bundle = new Bundle();
								bundle.putInt("index", index);
								bundle.putSerializable("saveDeviceItem",saveDeviceItem);
								intent.putExtras(bundle);
								setResult(resultCode, intent);
								DeviceCollectActivity.this.finish();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			builder.setNegativeButton(R.string.device_manager_devicecollect_cancel, null);
			builder.show();
		}
	}

	private int isContain(DeviceItem dItem) {
		int result = -1;
		for (int i = 0; i < collectDeviceItemList.size(); i++) {
			if (dItem.getDeviceName().equals(collectDeviceItemList.get(i).getDeviceName())) {
				result = i;
				break;
			}
		}
		return result;
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
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		super.setTitleViewText(getString(R.string.device_manager));
		super.hideExtendButton();
		super.setToolbarVisiable(false);
		identifyBtn = (Button) findViewById(R.id.conn_identify_btn);
		portEdt = (EditText) findViewById(R.id.et_device_add_port);
		chooseEdt = (EditText) findViewById(R.id.device_add_choose_et);
		recordEdt = (EditText) findViewById(R.id.et_device_add_record);
		serverEdt = (EditText) findViewById(R.id.et_device_add_server);
		chooseBtn = (Button) findViewById(R.id.device_add_button_state);
		lgUserEdt = (EditText) findViewById(R.id.et_device_add_username);
		lgPswdEdt = (EditText) findViewById(R.id.et_device_add_password);
		noRadioButton = (RadioButton) findViewById(R.id.device_manager_isenable_no_radioBtn);
		yesRadioButton = (RadioButton) findViewById(R.id.device_manager_isenable_yes_radioBtn);
		chooseEdt.setKeyListener(null);
		context = DeviceCollectActivity.this;
		loadDataTask = new LoadCollectDeviceItemTask();
		loadDataTask.execute();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case LOADNETDATADIALOG:
			loadPrg = ProgressDialog.show(this, "", getString(R.string.loading_devicedata_wait), true, true);
			loadPrg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissLoadPRG();
					synObject.resume();
				}
			});
			return loadPrg;
		case CONNIDENTIFYDIALOG:
			idenPrg = ProgressDialog.show(this, "", getString(R.string.device_manager_conn_iden), true, true);
			idenPrg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissIdenPRG();
					conIdenTask.setCanceled(true);// 不进行验证
				}
			});
			return idenPrg;
		default:
			return null;
		}
	}
	
	private void dismissIdenPRG(){
		if (idenPrg!=null && idenPrg.isShowing()) {
			idenPrg.dismiss();
		}
	}
	
	private void dismissLoadPRG(){
		if (loadPrg!=null && loadPrg.isShowing()) {
			loadPrg.dismiss();
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
				cloudAccountList = ReadWriteXmlUtils.getCloudAccountList(CloudAccountInfoOpt.filePathOfCloudAccount);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			int size = cloudAccountList.size();
			try {
				for (int i = 0; i < size; i++) {
					CloudAccount cloudAccount = cloudAccountList.get(i);
					if (cloudAccount.isEnabled()) {
						String dman = cloudAccount.getDomain();
						String port = cloudAccount.getPort();
						String usnm = cloudAccount.getUsername();
						String pasd = cloudAccount.getPassword();
						Document document = ReadWriteXmlUtils.SendURLPost(dman,
								port, usnm, pasd, "conn");
						String status = ReadWriteXmlUtils
								.readXmlStatus(document);
						if (status == null) {// 加载成功...
							List<DVRDevice> deviceList = ReadWriteXmlUtils
									.readXmlDVRDevices(document);
							int deviceListSize = deviceList.size();
							for (int j = 0; j < deviceListSize; j++) {
								dvrDeviceList.add(deviceList.get(j));
							}
							if (dvrDeviceList.size() > 0) {
								Collections.sort(dvrDeviceList,
										new PinyinComparatorUtils());
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
		if ((requestCode == REQUESTCODE)) {
			if (resultCode == ALL_ADD) {
				setResult(ALL_ADD);
				DeviceCollectActivity.this.finish();
			} else {
				if (data != null) {
					Bundle bundle = data.getExtras();
					if (bundle != null) {
						setNewUICollectDevice(bundle);
					}
				}
			}
		}
	}

	private void setNewUICollectDevice(Bundle bundle) {
		chooseDeviceItem = (DeviceItem) bundle
				.getSerializable("chooseDeviceItem");
		int chooseSize = Integer.valueOf(chooseDeviceItem.getChannelSum());
		List<Channel> channelList = new ArrayList<Channel>();
		for (int i = 0; i < chooseSize; i++) {
			Channel channel = new Channel();
			String text = getString(R.string.device_manager_channel);
			channel.setChannelName(text + "" + (i + 1));
			channel.setChannelNo((i+1));
			channel.setSelected(false);
			channelList.add(channel);
		}
		isConnPass = true;
		isIdentify = true;
		noRadioButton.setChecked(false);
		yesRadioButton.setChecked(true);
		chooseDeviceItem.setUsable(true);
		chooseDeviceItem.setIdentify(true);//
		saveDeviceItem.setChannelSum(chooseDeviceItem.getChannelSum());
		saveDeviceItem.setChannelList(channelList);
		chooseactivity_return_flag = bundle
				.getInt("chooseactivity_return_flag");
		chooseEdt.setText(chooseDeviceItem.getDeviceName());
		recordEdt.setText(chooseDeviceItem.getDeviceName());
		serverEdt.setText(chooseDeviceItem.getSvrIp());
		portEdt.setText(chooseDeviceItem.getSvrPort());
		lgUserEdt.setText(chooseDeviceItem.getLoginUser());
		lgPswdEdt.setText(chooseDeviceItem.getLoginPass());
//		dfChnlEdt.setText("" + chooseDeviceItem.getDefaultChannel());
		chooseEdt.setKeyListener(null);
		String platformUsername = getString(R.string.device_manager_collect_device);
		chooseDeviceItem.setPlatformUsername(platformUsername);
		// setIdentifyDeviceItem(saveDeviceItem);
	}

	private class LoadCollectDeviceItemTask extends
			AsyncTask<Void, Void, List<DeviceItem>> {
		@Override
		protected List<DeviceItem> doInBackground(Void... params) {
			List<DeviceItem> result = null;
			try {
				result = ReadWriteXmlUtils
						.getCollectDeviceListFromXML(ChannelListActivity.filePath);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<DeviceItem> result) {
			super.onPostExecute(result);
			collectDeviceItemList = result;
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if ((loadDataTask != null)
				&& (loadDataTask.getStatus() == AsyncTask.Status.RUNNING)) {
			loadDataTask.cancel(true); // 如果Task还在运行，则先取消它
			loadDataTask = null;
		}
	}

	@Override
	protected void onRestart() {
		super.onStart();
	}
}

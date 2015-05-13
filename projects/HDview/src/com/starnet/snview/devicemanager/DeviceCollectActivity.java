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
import android.util.Log;
import android.view.KeyEvent;
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
import com.starnet.snview.protocol.message.Constants;
import com.starnet.snview.syssetting.CloudAccount;
import com.starnet.snview.util.IPAndPortUtils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.PinyinComparatorUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;

@SuppressLint("SdCardPath")
public class DeviceCollectActivity extends BaseActivity {
	private final String TAG = "DeviceCollectActivity";
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
	public static final int CONNECTIFYIDENTIFY_WRONG = 0x0012;
	public static final int CONNECTIFYIDENTIFY_SUCCESS = 0x0011;
	public static final int CONNECTIFYIDENTIFY_TIMEOUT = 0x0013;
	public static final int CONNECTIFYIDENTIFY_ERROR_PSUN = 0x0014;
	public static final int CONNECTIFYIDENTIFY_ERROR_IP_PORT = 0x0016;
	public static final int CONNECTIFYIDENTIFY_EXCEPTION = 0x0017;
	public static final int CONNECTIFYIDENTIFY_LOGIN_FAIL = 0x0018;
	
	private int chooseactivity_return_flag = 1;
	// private EditText chooseEdt;
	private EditText portEdt;
	private EditText recordEdt;
	private EditText serverEdt;
	private EditText lgUserEdt;
	private EditText lgPswdEdt;

	private final int LOADNETDATADIALOG = 1;
	private final int CONNIDENTIFYDIALOG = 5;

	private List<DVRDevice> dvrList = new ArrayList<DVRDevice>();// 保存全部数据

	private List<DeviceItem> collectDeviceItemList;
	private LoadCollectDeviceItemTask loadDataTask;

	private Context context;
	private DeviceItem saveDeviceItem = new DeviceItem();
	private DeviceItem chooseDeviceItem; // 选择之后的设备
	private DeviceItem identifyDeviceItem; // 验证之后的设备

	private ProgressDialog loadPrg;
	private ProgressDialog idenPrg;

	private boolean[] tasksFlag;
	private ObtainDeviceTask[] obtainTasks;

	private final int TIMEOUT = 0x0030;
	private final int SRATUSNULL = 0x0032;
	private final int TIMEOUTERROR = 0x0031;
	private final int DOWNLOADEXCEPTION = 0x0033;
	private final int DOWNLOADSUCCESSFUL = 0x0034;

	private synchronized void getMessgeInfoForNull(Message msg,String flagStr) {
		Bundle data = msg.getData();
		int pos = data.getInt("position");
		String name = data.getString("name");
		tasksFlag[pos] = true;
		
		if (name.equals("timeout")||name.equals("timeouterror")) {
			showToast(name+getString(R.string.device_collect_timeout));
		}else {
			showToast(name+getString(R.string.device_collect_failure));
		}
		
		boolean allLoad = checkTasksFlag();
		if (allLoad) {
			dismissLoadPRG();
			if (dvrList != null && dvrList.size() > 0) {
				Collections.sort(dvrList, new PinyinComparatorUtils());
				gotoDeviceChooseActivity();
			}
		}
	}

	private synchronized void getMessgeInfoWithData(Message msg) {
		Bundle data = msg.getData();
		int pos = data.getInt("position");
		tasksFlag[pos] = true;
		ArrayList<DVRDevice> list = data.getParcelableArrayList("dvrDeviceList");
		if (list != null && list.size() > 0) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				dvrList.add(list.get(i));
			}
		}
		boolean allLoad = checkTasksFlag();
		if (allLoad) {
			dismissLoadPRG();
			Collections.sort(dvrList, new PinyinComparatorUtils());
			gotoDeviceChooseActivity();
		}
	};

	private void gotoDeviceChooseActivity() {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("dvrDeviceList",(ArrayList<? extends Parcelable>) dvrList);
		intent.putExtras(bundle);
		intent.setClass(DeviceCollectActivity.this, DeviceChooseActivity.class);
		startActivityForResult(intent, REQUESTCODE);
	}

	private boolean checkTasksFlag() {
		boolean result = true;
		for (int i = 0; i < tasksFlag.length; i++) {
			if (!tasksFlag[i]) {
				result = false;
				break;
			}
		}
		return result;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case TIMEOUT:
				getMessgeInfoForNull(msg,"timeout");
				break;
			case SRATUSNULL:
				getMessgeInfoForNull(msg,"nul");
				break;
			case TIMEOUTERROR:
				getMessgeInfoForNull(msg,"timeouterror");
				break;
			case DOWNLOADEXCEPTION:
				getMessgeInfoForNull(msg,"downloadException");
				break;
			case DOWNLOADSUCCESSFUL:
				getMessgeInfoWithData(msg);
				break;
			case CONNECTIFYIDENTIFY_ERROR_PSUN:
				connectifyWrong();
				showToast(getString(R.string.device_manager_conn_iden_wrong));
				break;
			case CONNECTIFYIDENTIFY_ERROR_IP_PORT:
				connectifyWrong();
				showToast(getString(R.string.device_manager_conn_iden_ip_port_error));
				break;
			case CONNECTIFYIDENTIFY_WRONG:
				connectifyWrong();
				showToast(getString(R.string.device_manager_conn_iden_wrong));
				break;
			case CONNECTIFYIDENTIFY_EXCEPTION:
				dismissIdenPRG();
				dismissLoadPRG();
				connectifyWrong();
				showToast(getString(R.string.device_manager_conn_iden_exception));
				break;
			case CONNECTIFYIDENTIFY_SUCCESS:
				isIdentify = true;
				isConnPass = true;
				saveDeviceItem.setIdentify(true);
				saveDeviceItem.setConnPass(true);
				identifyDeviceItem = getIdentifyDeviceItem(msg);
				saveDeviceItem.setChannelList(identifyDeviceItem.getChannelList());
				saveDeviceItem.setChannelSum(identifyDeviceItem.getChannelSum());
				showToast(getString(R.string.device_manager_conn_iden_sucess));
				dismissIdenPRG();
				dismissLoadPRG();
				break;
			case CONNECTIFYIDENTIFY_TIMEOUT:
				connectifyWrong();
				showToast(getString(R.string.device_manager_conn_iden_timout));
				break;
			case CONNECTIFYIDENTIFY_LOGIN_FAIL:
				dismissIdenPRG();
				showToast(getErrorMessage(msg.arg1));
				break;
			}
		}

		private void connectifyWrong() {
			isIdentify = true;
			isConnPass = false;
			dismissIdenPRG();
			saveDeviceItem.setIdentify(isIdentify);
			saveDeviceItem.setConnPass(isConnPass);
			setSaveDeviceItem();
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
					showToast(getString(R.string.device_collect_network_not_conn));
				} else {
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
					} else if (index == 0) {
						showToast(getString(R.string.device_manager_conn_iden_devicename_notnull));
						return;
					} else if (index == 1) {
						showToast(getString(R.string.device_manager_conn_iden_svrip_notnull));
						return;
					} else if (index == 2) {
						showToast(getString(R.string.device_manager_conn_iden_svrport_notnull));
						return;
					} else {
						showToast(getString(R.string.device_manager_conn_iden_username_notnull));
						return;
					}

					String ip = deviceItem.getSvrIp();
					if (ip == null || ip.trim().equals("") || !IPAndPortUtils.isIp(ip)) {
						showToast(getString(R.string.device_manager_deviceeditable_ip_wrong));
						return;
					}

					String port = deviceItem.getSvrPort();
					if (port == null || port.trim().equals("") || !IPAndPortUtils.isNetPort(port)) {
						showToast(getString(R.string.device_manager_collect_add_not_ext65535));
						return;
					}
					
					String lgUserName = deviceItem.getLoginUser();
					if ((lgUserName != null) && (lgUserName.length() > 32)) {
						showToast(getString(R.string.device_manager_collect_username_ext32));
						return ;
					}

					String lgPass = deviceItem.getLoginPass();
					if ((lgPass != null) && (lgPass.length() < 16)) {
						showDialog(CONNIDENTIFYDIALOG);
						conIdenTask = new DevConnIdenTask(mHandler, deviceItem);
						conIdenTask.setContext(DeviceCollectActivity.this);
						conIdenTask.start();
					} else {
						showToast(getString(R.string.device_manager_collect_add_pswdnot_ext16));
						return;
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
				if (!rName.equals("") && !svIP.equals("") && !port.equals("") && !uName.equals("")) {

					boolean isIP = IPAndPortUtils.isIp(svIP);
					if (!isIP) {
						showToast(getString(R.string.device_manager_collect_ip_wrong));
						return;
					}

					boolean isPort = IPAndPortUtils.isNetPort(port);
					if (!isPort) {
						showToast(getString(R.string.device_manager_collect_add_not_ext65535));
						return;
					}
					
					if ((uName != null) && (uName.length() > 32)) {
						showToast(getString(R.string.device_manager_collect_username_ext32));
						return ;
					}
					
					String lPass = saveDeviceItem.getLoginPass();
					if (lPass != null && (lPass.length() >= 16)) {
						showToast(getString(R.string.device_manager_collect_add_pswdnot_ext16));
						return;
					}

					if (isPort && isIP) {
						try {
							if (chooseactivity_return_flag == 1) {// 表示未进行选择
								if (!isConnPass) {
									setNoConnPassDeviceItem();
								}
								saveDeviceItemToXML(saveDeviceItem,DeviceViewActivity.SEMI_AUTO_ADD);// 验证通过后保存用户信息
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
						showToast(getString(R.string.device_manager_collect_add_not_ext65535));
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
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				Context context = DeviceCollectActivity.this;
				boolean isConn = NetWorkUtils.checkNetConnection(context);
				if (isConn) {
					try {
						List<CloudAccount> actList = ReadWriteXmlUtils
								.getCloudAccountList(CloudAccountInfoOpt.filePathOfCloudAccount);
						int size = actList.size();
						if (size > 0) {
							boolean usable = checkAccountUsable(actList);
							if (usable) {

								if (dvrList != null && dvrList.size() > 0) {
									dvrList.clear();
								}

								List<CloudAccount> ecs = getUsableAccount(actList);
								if (ecs != null && ecs.size() > 0) {
									showDialog(LOADNETDATADIALOG);
									int usableNum = ecs.size();
									tasksFlag = new boolean[usableNum];
									obtainTasks = new ObtainDeviceTask[usableNum];
									for (int i = 0; i < usableNum; i++) {
										tasksFlag[i] = false;
										obtainTasks[i] = new ObtainDeviceTask(
												mHandler, ecs.get(i), i);
										obtainTasks[i].initialThread();
										obtainTasks[i].startWork();
									}
								} else {
									showToast(getString(R.string.device_manager_devicechoose_exam_open));
								}
								// requestNetDataFromNet();
								// synObject.suspend();
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
					showToast(getString(R.string.device_collect_network_not_conn));
				}
			}
		});
	}

	private List<CloudAccount> getUsableAccount(List<CloudAccount> actList) {
		List<CloudAccount> accounts = new ArrayList<CloudAccount>();
		if (actList == null) {
			return null;
		}
		for (int i = 0; i < actList.size(); i++) {
			CloudAccount temp = actList.get(i);
			if ((temp != null) && (temp.isEnabled())) {
				accounts.add(temp);
			}
		}
		return accounts;
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
		saveDeviceItem
				.setPlatformUsername(getString(R.string.device_manager_collect_device));
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
			ReadWriteXmlUtils.addNewDeviceItemToCollectEquipmentXML(dItem,
					ChannelListActivity.filePath);
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
								ReadWriteXmlUtils.replaceSpecifyDeviceItem(
										ChannelListActivity.filePath, index,
										dItem);
								Bundle bundle = new Bundle();
								bundle.putInt("index", index);
								bundle.putSerializable("saveDeviceItem",
										saveDeviceItem);
								intent.putExtras(bundle);
								setResult(resultCode, intent);
								DeviceCollectActivity.this.finish();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			builder.setNegativeButton(
					R.string.device_manager_devicecollect_cancel, null);
			builder.show();
		}
	}

	private int isContain(DeviceItem dItem) {
		int result = -1;
		for (int i = 0; i < collectDeviceItemList.size(); i++) {
			if (dItem.getDeviceName().equals(
					collectDeviceItemList.get(i).getDeviceName())) {
				result = i;
				break;
			}
		}
		return result;
	}

	// @SuppressWarnings("deprecation")
	// private void requestNetDataFromNet() {
	// showDialog(LOADNETDATADIALOG);// 显示从网络的加载圈...
	// new ObtainDeviceDataFromNetThread(mHandler).start();
	// }

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
		// chooseEdt = (EditText) findViewById(R.id.device_add_choose_et);
		recordEdt = (EditText) findViewById(R.id.et_device_add_record);
		serverEdt = (EditText) findViewById(R.id.et_device_add_server);
		chooseBtn = (Button) findViewById(R.id.device_add_button_state);
		lgUserEdt = (EditText) findViewById(R.id.et_device_add_username);
		lgPswdEdt = (EditText) findViewById(R.id.et_device_add_password);
		noRadioButton = (RadioButton) findViewById(R.id.device_manager_isenable_no_radioBtn);
		yesRadioButton = (RadioButton) findViewById(R.id.device_manager_isenable_yes_radioBtn);
		// chooseEdt.setKeyListener(null);
		context = DeviceCollectActivity.this;
		loadDataTask = new LoadCollectDeviceItemTask();
		loadDataTask.execute();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case LOADNETDATADIALOG:
			loadPrg = ProgressDialog.show(this, "",
					getString(R.string.loading_devicedata_wait), true, true);
			loadPrg.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissLoadPRG();
					// synObject.resume();
					for (int i = 0; i < tasksFlag.length; i++) {
						obtainTasks[i].setCancel(true);
					}
				}
			});
			return loadPrg;
		case CONNIDENTIFYDIALOG:
			idenPrg = ProgressDialog.show(this, "",
					getString(R.string.device_manager_conn_iden), true, true);
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

	private void dismissIdenPRG() {
		if (idenPrg != null && idenPrg.isShowing()) {
			idenPrg.dismiss();
		}
	}

	private void dismissLoadPRG() {
		if (loadPrg != null && loadPrg.isShowing()) {
			loadPrg.dismiss();
		}
	}

	private final class ObtainDeviceTask {

		private int position;
		private Handler handler;
		private Thread timeThread;
		private Thread workThread;
		private CloudAccount account;

		private boolean isCancel = false;
		private boolean timeThreadOver = false;
		private boolean workThreadOver = false;

		private boolean sendTimeOut = false;
		private boolean sendTimeOutError = false;
		private boolean sendDownloadFlag = false;
		private boolean sendStatusNullFlag = false;
		private boolean senddownLoadExceFlag = false;

		private final int TIMEOUTCOUNT = 7;// 超时时间设置为7

		public ObtainDeviceTask(Handler handler, CloudAccount account, int pos) {
			this.handler = handler;
			this.account = account;
			this.position = pos;
		}

		public void initialThread() {
			timeThread = new Thread() {
				@Override
				public void run() {
					int timeCount = 0;
					while (!isCancel && !timeThreadOver) {
						try {
							Thread.sleep(1000);
							timeCount++;
							if (timeCount == TIMEOUTCOUNT) {
								timeThreadOver = true;
								if (!isCancel && !sendTimeOut) {
									onTimeOutWork();
								}
							}
						} catch (InterruptedException e) {
							Log.i(TAG, "====Thread InterruptedException===");
							timeThreadOver = true;
							if (!isCancel && !sendTimeOutError) {
								onTimeOutErrorWork();
							}
						}
					}
				}
			};

			workThread = new Thread() {
				@Override
				public void run() {
					if (!isCancel && !workThreadOver) {
						try {
							startDownLoadDeviceData();
						} catch (Exception e) {
							e.printStackTrace();
							downLoadDeviceDataException();
						}
					}
				}
			};
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void startDownLoadDeviceData() throws IOException,
				DocumentException {
			Document doc = ReadWriteXmlUtils.SendURLPost(account.getDomain(),
					account.getPort(), account.getUsername(),
					account.getPassword(), "con");
			String status = ReadWriteXmlUtils.readXmlStatus(doc);
			if (status == null) {// 加载成功...
				if (!sendDownloadFlag && !isCancel) {
					timeThreadOver = true;
					sendTimeOut = true;
					sendTimeOutError = true;
					Message msg = new Message();
					msg.what = DOWNLOADSUCCESSFUL;
					Bundle data = new Bundle();
					data.putInt("position", position);
					data.putParcelableArrayList("dvrDeviceList",
							(ArrayList) ReadWriteXmlUtils
									.readXmlDVRDevices(doc));
					msg.setData(data);
					handler.sendMessage(msg);
				}
			} else {
				onStatusNullWork();
			}
		}

		private void downLoadDeviceDataException() {
			sendTimeOut = true;
			sendDownloadFlag = true;
			sendTimeOutError = true;
			sendStatusNullFlag = true;
			if (!senddownLoadExceFlag && !isCancel) {
				setMessage(DOWNLOADEXCEPTION);
			}
		}

		private void onStatusNullWork() {
			sendTimeOut = true;
			sendDownloadFlag = true;
			sendTimeOutError = true;
			senddownLoadExceFlag = true;
			if (!sendStatusNullFlag && !isCancel) {
				setMessage(SRATUSNULL);
			}
		}

		private void onTimeOutErrorWork() {
			sendDownloadFlag = true;
			sendStatusNullFlag = true;
			senddownLoadExceFlag = true;
			setMessage(TIMEOUTERROR);
		}

		private void onTimeOutWork() {
			sendDownloadFlag = true;
			sendStatusNullFlag = true;
			senddownLoadExceFlag = true;
			setMessage(TIMEOUT);
		}

		private void setMessage(int id) {
			Message msg = new Message();
			msg.what = id;
			Bundle data = new Bundle();
			data.putInt("position", position);
			data.putString("name", account.getUsername());
			msg.setData(data);
			handler.sendMessage(msg);
		}

		public void setCancel(boolean isCancel) {
			this.isCancel = isCancel;
		}

		public void startWork() {
			timeThread.start();
			workThread.start();
		}
	}

	// private final class ObtainDeviceDataFromNetThread extends Thread {
	//
	// private Handler handler;
	//
	// public ObtainDeviceDataFromNetThread(Handler handler) {
	// super();
	// this.handler = handler;
	// }
	//
	// @Override
	// public void run() {
	// super.run();
	// Message msg = new Message();
	// List<CloudAccount> cloudAccountList = new ArrayList<CloudAccount>();
	// try {
	// cloudAccountList = ReadWriteXmlUtils
	// .getCloudAccountList(CloudAccountInfoOpt.filePathOfCloudAccount);
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// }
	// int size = cloudAccountList.size();
	// try {
	// for (int i = 0; i < size; i++) {
	// CloudAccount cloudAccount = cloudAccountList.get(i);
	// if (cloudAccount.isEnabled()) {
	// String dman = cloudAccount.getDomain();
	// String port = cloudAccount.getPort();
	// String usnm = cloudAccount.getUsername();
	// String pasd = cloudAccount.getPassword();
	// Document document = ReadWriteXmlUtils.SendURLPost(dman,
	// port, usnm, pasd, "conn");
	// String status = ReadWriteXmlUtils
	// .readXmlStatus(document);
	// if (status == null) {// 加载成功...
	// List<DVRDevice> deviceList = ReadWriteXmlUtils
	// .readXmlDVRDevices(document);
	// int deviceListSize = deviceList.size();
	// for (int j = 0; j < deviceListSize; j++) {
	// dvrDeviceList.add(deviceList.get(j));
	// }
	// if (dvrDeviceList.size() > 0) {
	// Collections.sort(dvrDeviceList,new PinyinComparatorUtils());
	// }
	// } else {// 加载不成功...
	//
	// }
	// }
	// }
	// msg.what = LOAD_SUCCESS;
	// handler.sendMessage(msg);
	// } catch (IOException e) {
	// e.printStackTrace();
	// if (dvrDeviceList.size() > 0) {
	// msg.what = LOAD_SUCCESS;
	// handler.sendMessage(msg);
	// } else {
	// msg.what = LOAD_WRONG;
	// handler.sendMessage(msg);
	// }
	// } catch (DocumentException e) {
	// e.printStackTrace();
	// if (dvrDeviceList.size() > 0) {
	// msg.what = LOAD_SUCCESS;
	// handler.sendMessage(msg);
	// } else {
	// msg.what = LOAD_WRONG;
	// handler.sendMessage(msg);
	// }
	// }
	// }
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		dismissLoadPRG();
		if ((requestCode == REQUESTCODE)) {
			if (resultCode == ALL_ADD) {
				setResult(ALL_ADD);
				DeviceCollectActivity.this.finish();
			} else {
				dismissLoadPRG();
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
			channel.setChannelNo((i + 1));
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
		// chooseEdt.setText(chooseDeviceItem.getDeviceName());
		recordEdt.setText(chooseDeviceItem.getDeviceName());
		serverEdt.setText(chooseDeviceItem.getSvrIp());
		portEdt.setText(chooseDeviceItem.getSvrPort());
		lgUserEdt.setText(chooseDeviceItem.getLoginUser());
		lgPswdEdt.setText(chooseDeviceItem.getLoginPass());
		// dfChnlEdt.setText("" + chooseDeviceItem.getDefaultChannel());
		// chooseEdt.setKeyListener(null);
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
		stopLoadDtaTask();
	}

	private void stopLoadDtaTask() {
		if ((loadDataTask != null)
				&& (loadDataTask.getStatus() == AsyncTask.Status.RUNNING)) {
			loadDataTask.cancel(true); // 如果Task还在运行，则先取消它
			loadDataTask = null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			stopLoadDtaTask();
			stopDownloadTasks();
			dismissIdenPRG();
			dismissLoadPRG();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void stopDownloadTasks() {
		if (tasksFlag != null) {
			for (int i = 0; i < tasksFlag.length; i++) {
				if (obtainTasks[i] != null) {
					obtainTasks[i].setCancel(true);
				}
			}
		}
	}

}

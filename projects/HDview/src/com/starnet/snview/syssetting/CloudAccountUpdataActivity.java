package com.starnet.snview.syssetting;

import java.io.IOException;
import java.net.SocketTimeoutException;
//import java.util.List;













import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.CloudService;
import com.starnet.snview.devicemanager.CloudServiceImpl;
import com.starnet.snview.util.IPAndPortUtils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.SynObject;

@SuppressLint({ "HandlerLeak", "SdCardPath" })
public class CloudAccountUpdataActivity extends BaseActivity {
	
	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";

	private EditText serverEditText;
	private EditText portEditText;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private RadioButton isenablYseRadioBtn;
	private RadioButton isenablNoRadioBtn;

	private Button saveBtn;//保存按钮
	private CloudAccount saveCloudAccount;//保存账户
	
	private Button identifyBtn;//验证按钮
	private boolean identifier_flag = false;//验证标志，如果验证通过，则令idenfier_flag = true;如果验证不通过，并且未进行验证，则令idenfier_flag = false;
	private boolean identifier_flag_after = false;
	private CloudAccount identifyCloudAccount;//验证后的账户
	
	private CloudAccount clickCloudAccount = new CloudAccount();
	private int clickPostion;
	
	private SynObject synObj = new SynObject();

	private final int DDNS_RESP_SUCC = 0x1100; // 获取设备信息成功
	private final int DDNS_RESP_FAILURE = 0x1101; // 获取设备信息失败
	private final int DDNS_REQ_TIMEOUT = 0x1102; // 设备列表请求超时
	private final int DDNS_SYS_FAILURE = 0x1103; // 非DDNS返回错误
	private CloudService cloudService = new CloudServiceImpl("conn1");

	private String server;
	private String port;
	private String username;
	private String password;
	private Handler responseHandler = new Handler() {
		@SuppressWarnings("deprecation")
		@SuppressLint("SdCardPath")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (synObj.getStatus() == SynObject.STATUS_RUN) {
				return;
			}
			dismissDialog(1);
			// 解除挂起， 程序往下执行
			synObj.resume();
			String printSentence = "";
			String errMsg = "";
			Context context = CloudAccountUpdataActivity.this;
			switch (msg.what) {
			case DDNS_RESP_SUCC:
				//只验证，不保存
				identifier_flag = true;
				identifier_flag_after = true;
				printSentence = getString(R.string.system_setting_cloudaccount_useable);
				Toast toast1 = Toast.makeText(context,printSentence, Toast.LENGTH_LONG);
				toast1.show();
				dismissDialog(1);
				break;
			case DDNS_RESP_FAILURE:
				identifier_flag = false;
				identifier_flag_after = false;
				errMsg = msg.getData().getString("ERR_MSG");
				Toast.makeText(CloudAccountUpdataActivity.this, errMsg,Toast.LENGTH_LONG).show();
				break;
			case DDNS_SYS_FAILURE:
				identifier_flag = false;
				identifier_flag_after = false;
				errMsg = getString(R.string.common_connection_wrong_check_port_domain);
				Toast.makeText(CloudAccountUpdataActivity.this, errMsg,Toast.LENGTH_LONG).show();
				break;
			case DDNS_REQ_TIMEOUT:
				identifier_flag = false;
				identifier_flag_after = false;
				errMsg = getString(R.string.common_request_outtime_check_port_server);
				Toast.makeText(CloudAccountUpdataActivity.this, errMsg,Toast.LENGTH_LONG).show();
				break;
			default:
				identifier_flag = false;
				identifier_flag_after = false;
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_setting_activity_another);
		initView();
		loadViewFromData();
		setListenters();
	}

	private void setListenters() {
		
		identifyBtn.setOnClickListener(new OnClickListener() {
			//验证用户的有效性；首先检查填写信息是否完整，如果不完整，则提示用户不完整信息；否则，进行网络验证，验证之后，保存用户的验证信息，
			//如果验证通过，
			@Override
			public void onClick(View v) {
				Context context = CloudAccountUpdataActivity.this;
				//检测网络是否连接，若网络并未连接则
				NetWorkUtils netWorkUtils = new NetWorkUtils();
				boolean isConn = netWorkUtils.checkNetConnection(context);
				if (isConn) {
					// 验证是否有为空的现象
					server = serverEditText.getText().toString();
					port = portEditText.getText().toString();
					username = usernameEditText.getText().toString();
					password = passwordEditText.getText().toString();

					if (!server.equals("") && !port.equals("")&& !username.equals("") && !password.equals("")) {
						
						//检测是否是网络端口号
						IPAndPortUtils ipAndPort = new IPAndPortUtils();
						boolean isPort = ipAndPort.isNetPort(port);
//						boolean isIP = ipAndPort.isIPAddress(server);
						if (isPort) {
							identifyCloudAccount = new CloudAccount();	
							if (isenablYseRadioBtn.isChecked()) {
								identifyCloudAccount.setEnabled(true);
							} else {
								identifyCloudAccount.setEnabled(false);
							}
							identifyCloudAccount.setDomain(server);
							identifyCloudAccount.setPassword(password);
							identifyCloudAccount.setUsername(username);
							identifyCloudAccount.setPort(port);
							
							requset4DeviceList();
							synObj.suspend();// 挂起等待请求结果
						}else {
							String printSentence = getString(R.string.device_manager_editact_port_wrong);
							Toast toast3 = Toast.makeText(context,printSentence, Toast.LENGTH_LONG);
							toast3.show();
						}						
					} else {
						String printSentence = getString(R.string.system_setting_cloudaccountsetedit_null_content);
						Toast toast3 = Toast.makeText(context,printSentence, Toast.LENGTH_LONG);
						toast3.show();
					}
				}else {
					String printSentence = getString(R.string.network_not_conn);
					Toast toast3 = Toast.makeText(context,printSentence, Toast.LENGTH_LONG);
					toast3.show();
				}
			}	
		});
		
		// 直接返回...
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloudAccountUpdataActivity.this.finish();
			}// 如果信息无改变，则不刷新ListView
		});

		saveBtn.setOnClickListener(new OnClickListener() {// 单击保存时，需要验证用户的有效性...
					@Override
					public void onClick(View v) {
						
						Context context = CloudAccountUpdataActivity.this;
						// 验证是否有为空的现象
						server = serverEditText.getText().toString();
						port = portEditText.getText().toString();
						username = usernameEditText.getText().toString();
						password = passwordEditText.getText().toString();

						if (!server.equals("") && !port.equals("")&& !username.equals("") && !password.equals("")) {
							
							saveCloudAccount = new CloudAccount();
							saveCloudAccount.setDomain(server);
							saveCloudAccount.setPassword(password);
							saveCloudAccount.setUsername(username);
							saveCloudAccount.setPort(port);
							saveCloudAccount.setRotate(false);
							saveCloudAccount.setExpanded(false);
							identifier_flag = isEqualSaveAndIdentifyCloudAccount(saveCloudAccount,identifyCloudAccount);
							
							if (isenablYseRadioBtn.isChecked()&&(identifier_flag)&&(identifier_flag_after)) {
								saveCloudAccount.setEnabled(true);
							} else {
								saveCloudAccount.setEnabled(false);
							}
							CloudAccountXML caXML = new CloudAccountXML();
							try {
								//检测是否已经存在账户
								List<CloudAccount> cloudAcountList = caXML.getCloudAccountList(filePath);
								boolean result = judgeListContainCloudAccount(saveCloudAccount, cloudAcountList);
								if (result) {//如果包含，则不添加
									String printSentence = getString(R.string.device_manager_setting_setedit_contain_no_need);
									Toast toast = Toast.makeText(context,printSentence, Toast.LENGTH_SHORT);
									toast.show();
								} else {//如果不包含，则添加
									
									caXML.replaceSpecifyCloudAccount(filePath, clickCloudAccount, saveCloudAccount);//替换掉以前的星云账号
									String printSentence = getString(R.string.system_setting_cloudaccountupdate_edit_right);
									Toast toast3 = Toast.makeText(context,printSentence, Toast.LENGTH_LONG);
									toast3.show();
//									caXML.removeCloudAccoutFromXML(filePath, clickCloudAccount);//删除以前的星云平台账户
//									caXML.addNewCloudAccoutNodeToRootXML(filePath,saveCloudAccount);//添加当前的星云平台账户
									Intent intent = new Intent();
									Bundle bundle = new Bundle();
									bundle.putSerializable("edit_cloudAccount",saveCloudAccount);
									intent.putExtras(bundle);
									setResult(3, intent);
									CloudAccountUpdataActivity.this.finish();
								}
							} catch (Exception e1) {
								System.out.println(e1.toString());
							}
						} else {
							String printSentence = getString(R.string.system_setting_cloudaccountsetedit_null_content);
							Toast toast3 = Toast.makeText(context,printSentence, Toast.LENGTH_LONG);
							toast3.show();
						}
					}
				});
	}

	private void loadViewFromData() {

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			clickCloudAccount = (CloudAccount) bundle.getSerializable("cloudAccount");
			String server = clickCloudAccount.getDomain();
			String port = clickCloudAccount.getPort();
			String userName = clickCloudAccount.getUsername();
			String password = clickCloudAccount.getPassword();
			serverEditText.setText(server);
			portEditText.setText(port);
			usernameEditText.setText(userName);
			passwordEditText.setText(password);
			boolean isEnabled = clickCloudAccount.isEnabled();
			if (isEnabled) {
				isenablYseRadioBtn.setChecked(true);
				isenablNoRadioBtn.setChecked(false);
			} else {
				isenablYseRadioBtn.setChecked(false);
				isenablNoRadioBtn.setChecked(true);
			}
		}
	}

	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_cloudaccount_setting));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		saveBtn = super.getRightButton();
		serverEditText = (EditText) findViewById(R.id.cloudaccount_setting_server_edittext);
		portEditText = (EditText) findViewById(R.id.cloudaccount_setting_port_edittext);
		usernameEditText = (EditText) findViewById(R.id.cloudaccount_setting_username_edittext);
		passwordEditText = (EditText) findViewById(R.id.cloudaccount_setting_password_edittext);
		isenablYseRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_setting_isenable_yes_radioBtn);
		isenablNoRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_setting_isenable_no_radioBtn);
		identifyBtn = (Button) findViewById(R.id.identify_cloudaccount_right);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		clickPostion = Integer.valueOf(bundle.getString("clickPostion"));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			ProgressDialog progress = ProgressDialog.show(this, "",getString(R.string.system_set_setedit_identify_user_right), true, true);
			progress.setOnCancelListener(new OnCancelListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(1);
					synObj.resume();
				}
			});
			return progress;
		default:
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	private void requset4DeviceList() {
		showDialog(1);
		(new RequestDeviceInfoThread(responseHandler)).start();
	}

	class RequestDeviceInfoThread extends Thread {
		private Handler handler;

		public RequestDeviceInfoThread(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			
			Message msg = new Message();
			try {
				Document doc = cloudService.SendURLPost(server, port, username,password);
				String requestResult = cloudService.readXmlStatus(doc);
				if (requestResult == null) // 请求成功，返回null
				{
					msg.what = DDNS_RESP_SUCC;
				} else { // 请求失败，返回错误原因
					Bundle errMsg = new Bundle();
					msg.what = DDNS_RESP_FAILURE;
					errMsg.putString("ERR_MSG", requestResult);
					msg.setData(errMsg);
				}
			} catch (DocumentException e) {
				msg.what = DDNS_SYS_FAILURE;
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				msg.what = DDNS_REQ_TIMEOUT;
				e.printStackTrace();
			} catch (IOException e) {
				msg.what = DDNS_SYS_FAILURE;
				e.printStackTrace();
			}
			handler.sendMessage(msg);
		}
	}
	protected boolean isEqualSaveAndIdentifyCloudAccount(CloudAccount save_CloudAccount2, CloudAccount identify_CloudAccount2) {
		boolean isEqual = false;
		if((save_CloudAccount2 == null)||(identify_CloudAccount2 == null)){
			return isEqual;
		}else {
			String sDman = save_CloudAccount2.getDomain();
			String sPort = save_CloudAccount2.getPort();
			String sPass = save_CloudAccount2.getPassword();
			String sName = save_CloudAccount2.getUsername();
			
			String iDman = identify_CloudAccount2.getDomain();
			String iPort = identify_CloudAccount2.getPort();
			String iPass = identify_CloudAccount2.getPassword();
			String iName = identify_CloudAccount2.getUsername();
			
			if (sDman.equals(iDman)&&sPort.equals(iPort)&&sPass.equals(iPass)&&sName.equals(iName)) {
				isEqual = true;
			}else {
				isEqual = false;
			}
			return isEqual;
		}
	}
	private boolean judgeListContainCloudAccount(CloudAccount cloudAccount,List<CloudAccount> cloudAccountList2) {
		boolean result = false;
		int size = cloudAccountList2.size();
		for (int i = 0; i < size; i++) {
			if (i != clickPostion) {
				CloudAccount cA = cloudAccountList2.get(i);
				String cADomain = cA.getDomain();
				String cAPort = cA.getPort();
				String cAUsername = cA.getUsername();
				/*String cAPassword = cA.getPassword();*/
				/* boolean isEnabled = cA.isEnabled(); */
				if (cloudAccount.getUsername().equals(cAUsername)&& cloudAccount.getDomain().equals(cADomain)
					&& cloudAccount.getPort().equals(cAPort)) {/*&&(isEnabled ==cloudAccount.isEnabled())*//*mNetWorkSettingList*/
					result = true;
					break;
				}
			}
		}
		return result;
	}
}
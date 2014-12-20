package com.starnet.snview.syssetting;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
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
import android.widget.RadioGroup;
import android.widget.Toast;

import com.baidu.android.pushservice.PushManager;
import com.starnet.snview.R;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.util.MD5Utils;
import com.starnet.snview.util.NetWorkUtils;
import com.starnet.snview.util.ReadWriteXmlUtils;
import com.starnet.snview.util.SynObject;

@SuppressLint({ "SdCardPath", "HandlerLeak" })
public class CloudAccountAddingActivity extends BaseActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "CloudAccountSettingActivity";
	private final String filePath = "/data/data/com.starnet.snview/star_cloudAccount.xml";
	
	//验证标志，如果验证通过，则令idenfier_flag = true;如果验证不通过，并且未进行验证，则令idenfier_flag = false;
	private boolean identifier_flag = false;
	private boolean identifier_flag_after = false;

	private EditText serverEditText;
	private EditText portEditText;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private RadioButton isenablYseRadioBtn;
	private RadioButton isenablNoRadioBtn;
	RadioGroup isenableRadioGroup;

	private CloudAccount cloudAccount;

	private final int DDNS_RESP_SUCC = 0x1100; // 获取设备信息成功
	private final int DDNS_RESP_FAILURE = 0x1101; // 获取设备信息失败
	private final int DDNS_REQ_TIMEOUT = 0x1102; // 设备列表请求超时
	private final int DDNS_SYS_FAILURE = 0x1103; // 非DDNS返回错误
	
	private final int DDNS_RESP_SUCC_COPY = 0x1104; // 获取设备信息成功
	private final int DDNS_RESP_FAILURE_COPY = 0x1105; // 获取设备信息失败
	private final int DDNS_REQ_TIMEOUT_COPY = 0x1106; // 设备列表请求超时
	private final int DDNS_SYS_FAILURE_COPY = 0x1107; // 非DDNS返回错误
	
	String server;
	String port;
	String username;
	String password;

	private Button identify_btn;//验证客户是否网络可达按钮
	private CloudAccount identify_CloudAccount;//验证后的账号；
	
	private Button save_btn;//右上角保存按钮；
	private CloudAccount save_CloudAccount;//单击保存时的账号；

	private SynObject synObj = new SynObject();

	private Handler responseHandler = new Handler() {
		@SuppressWarnings("deprecation")
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

			switch (msg.what) {
			case DDNS_RESP_SUCC:
				identifier_flag = true;
				identifier_flag_after = true;
				//只验证，不保存
				printSentence = getString(R.string.system_setting_cloudaccount_useable);
				Toast toast1 = Toast.makeText(CloudAccountAddingActivity.this,printSentence, Toast.LENGTH_LONG);
				toast1.show();
				dismissDialog(1);
				break;
			case DDNS_RESP_SUCC_COPY://验证后保存
				save_CloudAccount.setEnabled(true);
				ReadWriteXmlUtils.addNewCloudAccoutNodeToRootXML(filePath,save_CloudAccount);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("cloudAccount",save_CloudAccount);
				intent.putExtras(bundle);
				setResult(3, intent);
				CloudAccountAddingActivity.this.finish();
				break;
			case DDNS_RESP_FAILURE:
				identifier_flag = false;
				identifier_flag_after = false;
				errMsg = msg.getData().getString("ERR_MSG");
				Toast.makeText(CloudAccountAddingActivity.this, errMsg, Toast.LENGTH_LONG).show();
				break;
			case DDNS_RESP_FAILURE_COPY:
				identifier_flag = false;
				identifier_flag_after = false;
				errMsg = msg.getData().getString("ERR_MSG");
				Toast.makeText(CloudAccountAddingActivity.this, errMsg, Toast.LENGTH_LONG).show();
				break;
			case DDNS_SYS_FAILURE:
				identifier_flag = false;
				identifier_flag_after = false;
				errMsg = getString(R.string.common_connection_wrong_check_port_domain);
				Toast.makeText(CloudAccountAddingActivity.this, errMsg, Toast.LENGTH_LONG).show();
				break;
			case DDNS_SYS_FAILURE_COPY:
				identifier_flag = false;
				identifier_flag_after = false;
				errMsg = getString(R.string.common_connection_wrong_check_port_domain);
				Toast.makeText(CloudAccountAddingActivity.this, errMsg, Toast.LENGTH_LONG).show();
				break;
			case DDNS_REQ_TIMEOUT:
				identifier_flag = false;
				identifier_flag_after = false;
				errMsg = getString(R.string.common_request_outtime_check_port_server);
				Toast.makeText(CloudAccountAddingActivity.this, errMsg, Toast.LENGTH_LONG).show();
				break;
			case DDNS_REQ_TIMEOUT_COPY:
				identifier_flag = false;
				identifier_flag_after = false;
				errMsg = getString(R.string.common_request_outtime_check_port_server);
				Toast.makeText(CloudAccountAddingActivity.this, errMsg, Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_setting_activity_another);

		initView();
		setListeners();
	}

	private void initView() {
		super.setTitleViewText(getString(R.string.navigation_title_system_setting_cloudaccount_setting));
		super.hideExtendButton();
		super.setRightButtonBg(R.drawable.navigation_bar_savebtn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);

		serverEditText = (EditText) findViewById(R.id.cloudaccount_setting_server_edittext);
		portEditText = (EditText) findViewById(R.id.cloudaccount_setting_port_edittext);
		usernameEditText = (EditText) findViewById(R.id.cloudaccount_setting_username_edittext);
		passwordEditText = (EditText) findViewById(R.id.cloudaccount_setting_password_edittext);
		isenableRadioGroup = (RadioGroup) findViewById(R.id.cloudaccount_setting_isenable_radioGroup);
		isenablYseRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_setting_isenable_yes_radioBtn);
		isenablNoRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_setting_isenable_no_radioBtn);
		
		identify_btn = (Button) findViewById(R.id.identify_cloudaccount_right);
		save_btn = super.getRightButton();
	}

	private void setListeners() {
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloudAccountAddingActivity.this.finish();
			}
		});
		
		identify_btn.setOnClickListener(new OnClickListener() {
			//验证用户的有效性；首先检查填写信息是否完整，如果不完整，则提示用户不完整信息；否则，进行网络验证，验证之后，保存用户的验证信息，
			//如果验证通过，
			@Override
			public void onClick(View v) {
				
				//检测网络是否连接，若网络并未连接则
				boolean isConn = NetWorkUtils.checkNetConnection(CloudAccountAddingActivity.this);
				if (isConn) {
					identify_CloudAccount = new CloudAccount();
					cloudAccount = new CloudAccount();
					if (isenablYseRadioBtn.isChecked()) {
						cloudAccount.setEnabled(true);
						identify_CloudAccount.setEnabled(true);
					} else if (isenablNoRadioBtn.isChecked()) {
						cloudAccount.setEnabled(false);
						identify_CloudAccount.setEnabled(false);
					}
					
					// 验证是否有为空的现象
					server = serverEditText.getText().toString();
					port = portEditText.getText().toString();
					username = usernameEditText.getText().toString();
					password = passwordEditText.getText().toString();

					if (!server.equals("") && !port.equals("")&& !username.equals("") && !password.equals("")) {
						cloudAccount.setDomain(server);
						cloudAccount.setPassword(password);
						cloudAccount.setUsername(username);
						cloudAccount.setPort(port);
						
						identify_CloudAccount.setPassword(password);
						identify_CloudAccount.setDomain(server);
						identify_CloudAccount.setUsername(username);
						identify_CloudAccount.setPort(port);
						
						requset4DeviceList();
						synObj.suspend();// 挂起等待请求结果
					} else {
						String printSentence = getString(R.string.system_setting_cloudaccountsetting_null_content);
						Toast toast3 = Toast.makeText(CloudAccountAddingActivity.this,printSentence, Toast.LENGTH_LONG);
						toast3.show();
					}
				}else {
					String printSentence = getString(R.string.network_not_conn);
					Toast toast3 = Toast.makeText(CloudAccountAddingActivity.this,printSentence, Toast.LENGTH_LONG);
					toast3.show();
				}
			}	
		});
		

		save_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//根据identifier_flag的值，直接添加用户
				// 验证是否有为空的现象
				server = serverEditText.getText().toString();
				port = portEditText.getText().toString();
				username = usernameEditText.getText().toString();
				password = passwordEditText.getText().toString();
				
				if (!server.equals("") && !port.equals("")&& !username.equals("") && !password.equals("")) {
					save_CloudAccount = new CloudAccount();
					save_CloudAccount.setDomain(server);
					save_CloudAccount.setPort(port);
					save_CloudAccount.setUsername(username);
					save_CloudAccount.setPassword(password);
					save_CloudAccount.setExpanded(false);
					save_CloudAccount.setRotate(false);
					identifier_flag = isEqualSaveAndIdentifyCloudAccount(save_CloudAccount,identify_CloudAccount);
					
					if (isenablYseRadioBtn.isChecked()&&(identifier_flag)) {
						save_CloudAccount.setEnabled(true);
					} else {
						save_CloudAccount.setEnabled(false);
					}
					
					try {
						//判断是否已经包含该用户
						List<CloudAccount> cloudAcountList = ReadWriteXmlUtils.getCloudAccountList(filePath);
						boolean result = judgeListContainCloudAccount(save_CloudAccount, cloudAcountList);
						if (result) {//如果包含，则不添加
							String printSentence = getString(R.string.device_manager_setting_setedit_contain_no_need);
							Toast toast = Toast.makeText(CloudAccountAddingActivity.this,printSentence, Toast.LENGTH_SHORT);
							toast.show();
						} else {//如果不包含，则添加
							//保存为是
							if (!identifier_flag&&isenablYseRadioBtn.isChecked()) {
								
								Builder builder = new Builder(CloudAccountAddingActivity.this);
								builder.setTitle(getString(R.string.system_setting_cloudaccount_identify_ok));
								builder.setPositiveButton(getString(R.string.system_setting_cloudaccountview_ok),null);
								builder.show();
							}else if (identifier_flag&&isenablYseRadioBtn.isChecked()) {
								boolean isEqual = isEqualSaveAndIdentifyCloudAccount(save_CloudAccount,identify_CloudAccount);
								if (isEqual&&identifier_flag_after) {
									save_CloudAccount.setEnabled(true);
									ReadWriteXmlUtils.addNewCloudAccoutNodeToRootXML(filePath,save_CloudAccount);
									Intent intent = new Intent();
									Bundle bundle = new Bundle();
									bundle.putSerializable("cloudAccount",save_CloudAccount);
									intent.putExtras(bundle);
									setResult(3, intent);
									//百度云推送的标签设置
									List<String>tags = new ArrayList<String>();
									tags.add(save_CloudAccount.getUsername()+""+MD5Utils.createMD5(save_CloudAccount.getPassword()));
									PushManager.setTags(CloudAccountAddingActivity.this, tags);
									//百度云推送的标签设置
									CloudAccountAddingActivity.this.finish();
								}else if (!isEqual&&identifier_flag_after){
									Builder builder = new Builder(CloudAccountAddingActivity.this);
									builder.setTitle(getString(R.string.system_setting_cloudaccount_identify_ok));
									builder.setPositiveButton(getString(R.string.system_setting_cloudaccountview_ok),null);
									builder.show();
								}else {
									save_CloudAccount.setEnabled(false);
									ReadWriteXmlUtils.addNewCloudAccoutNodeToRootXML(filePath,save_CloudAccount);
									Intent intent = new Intent();
									Bundle bundle = new Bundle();
									bundle.putSerializable("cloudAccount",save_CloudAccount);
									intent.putExtras(bundle);
									setResult(3, intent);
									CloudAccountAddingActivity.this.finish();
								}
							}else {
								save_CloudAccount.setEnabled(false);
								ReadWriteXmlUtils.addNewCloudAccoutNodeToRootXML(filePath,save_CloudAccount);
								Intent intent = new Intent();
								Bundle bundle = new Bundle();
								bundle.putSerializable("cloudAccount",save_CloudAccount);
								intent.putExtras(bundle);
								setResult(3, intent);
								CloudAccountAddingActivity.this.finish();
							}
						}
					} catch (Exception e1) {
						System.out.println(e1.toString());
					}
				} else {
					String printSentence = getString(R.string.system_setting_cloudaccountsetting_null_content);
					Toast toast3 = Toast.makeText(CloudAccountAddingActivity.this,printSentence, Toast.LENGTH_LONG);
					toast3.show();
				}
			}
		});
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

	@SuppressWarnings("deprecation")
	private void requset4DeviceList() {
		showDialog(1);
		(new RequestDeviceInfoThread(responseHandler)).start();
	}
	/*
	private void requset4DeviceList_copys() {
		showDialog(1);
		(new RequestDeviceInfoThread_copy(responseHandler)).start();
	}*/

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			ProgressDialog progress = ProgressDialog.show(this, "",getString(R.string.system_set_setting_identify_user_right), true, true);
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

	class RequestDeviceInfoThread_copy extends Thread {
		private Handler handler;
		public RequestDeviceInfoThread_copy(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			Message msg = new Message();
			try {
				Document doc = ReadWriteXmlUtils.SendURLPost(server, port, username,password,"conn");
				String requestResult = ReadWriteXmlUtils.readXmlStatus(doc);
				if (requestResult == null) // 请求成功，返回null
				{
					msg.what = DDNS_RESP_SUCC_COPY;
				} else { // 请求失败，返回错误原因
					Bundle errMsg = new Bundle();
					msg.what = DDNS_RESP_FAILURE_COPY;
					errMsg.putString("ERR_MSG", requestResult);
					msg.setData(errMsg);
				}
			} catch (DocumentException e) {
				msg.what = DDNS_SYS_FAILURE_COPY;
				e.printStackTrace();
			} catch (SocketTimeoutException e) {
				msg.what = DDNS_REQ_TIMEOUT_COPY;
				e.printStackTrace();
			} catch (IOException e) {
				msg.what = DDNS_SYS_FAILURE_COPY;
				e.printStackTrace();
			}
			handler.sendMessage(msg);
		}
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
				Document doc = ReadWriteXmlUtils.SendURLPost(server, port, username,password,"conn");
				String requestResult = ReadWriteXmlUtils.readXmlStatus(doc);
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

	private boolean judgeListContainCloudAccount(CloudAccount cloudAccount,List<CloudAccount> cloudAccountList2) {
		boolean result = false;
		int size = cloudAccountList2.size();
		for (int i = 0; i < size; i++) {
			CloudAccount cA = cloudAccountList2.get(i);
			String cADomain = cA.getDomain();
			String cAPort = cA.getPort();
			String cAUsername = cA.getUsername();
			/*String cAPassword = cA.getPassword();*/
			/* boolean isEnabled = cA.isEnabled(); */
			if (cloudAccount.getUsername().equals(cAUsername)&& cloudAccount.getDomain().equals(cADomain)
				&& cloudAccount.getPort().equals(cAPort)) {/*&&(isEnabled ==cloudAccount.isEnabled())*//*&& cloudAccount.getPassword().equals(cAPassword)*/
				result = true;
				break;
			}
		}
		return result;
	}
}
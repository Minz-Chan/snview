package com.starnet.snview.syssetting;

import java.io.IOException;
import java.net.SocketTimeoutException;
//import java.util.List;




import org.dom4j.Document;
import org.dom4j.DocumentException;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.starnet.snview.R;
import com.starnet.snview.channelmanager.xml.CloudAccountXML;
import com.starnet.snview.component.BaseActivity;
import com.starnet.snview.devicemanager.CloudService;
import com.starnet.snview.devicemanager.CloudServiceImpl;
import com.starnet.snview.util.SynObject;

@SuppressLint({ "HandlerLeak", "SdCardPath" })
public class CloudAccountSetEditActivity extends BaseActivity {

	private EditText serverEditText;
	private EditText portEditText;
	private EditText usernameEditText;
	private EditText passwordEditText;
	private RadioButton isenablYseRadioBtn;
	private RadioButton isenablNoRadioBtn;

	private CloudAccount clickCloudAccount = new CloudAccount();
	private SynObject synObj = new SynObject();

	private final int DDNS_RESP_SUCC = 0x1100; // 获取设备信息成功
	private final int DDNS_RESP_FAILURE = 0x1101; // 获取设备信息失败
	private final int DDNS_REQ_TIMEOUT = 0x1102; // 设备列表请求超时
	private final int DDNS_SYS_FAILURE = 0x1103; // 非DDNS返回错误
	private CloudService cloudService = new CloudServiceImpl("conn1");

	private String server;
	private String port;
	private String username;
	private String psd;
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
			String errMsg = "";
			switch (msg.what) {
			case DDNS_RESP_SUCC:
				String serverSentence = getString(R.string.edit_infor_right);
				Toast.makeText(CloudAccountSetEditActivity.this,serverSentence, Toast.LENGTH_LONG).show();
				dismissDialog(1);
				// 未改变数据之前进行删除操作...
				CloudAccountXML caXML = new CloudAccountXML();
				String fileName = "/data/data/com.starnet.snview/star_cloudAccount.xml";
				caXML.removeCloudAccoutFromXML(fileName, clickCloudAccount);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				caXML.addNewCloudAccoutNodeToRootXML(fileName,clickCloudAccount);// 添加到文档中。。。
				bundle.putSerializable("ca", clickCloudAccount);
				intent.putExtras(bundle);
				setResult(20, intent);
				CloudAccountSetEditActivity.this.finish();
				break;
			case DDNS_RESP_FAILURE:
				errMsg = msg.getData().getString("ERR_MSG");
				Toast.makeText(CloudAccountSetEditActivity.this, errMsg,Toast.LENGTH_LONG).show();
				break;
			case DDNS_SYS_FAILURE:
				errMsg = getString(R.string.DEVICE_LIST_ErrorReason);
				Toast.makeText(CloudAccountSetEditActivity.this, errMsg,Toast.LENGTH_LONG).show();
				break;
			case DDNS_REQ_TIMEOUT:
				errMsg = getString(R.string.DEVICE_LIST_REQ_TIMEOUT);
				Toast.makeText(CloudAccountSetEditActivity.this, errMsg,Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_setting_cloudaccount_set_activity);
		initView();
		loadViewFromData();
		setListenters();
	}

	private void setListenters() {
		// 直接返回...
		super.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CloudAccountSetEditActivity.this.finish();
			}// 如果信息无改变，则不刷新ListView
		});

		super.getRightButton().setOnClickListener(new OnClickListener() {// 单击保存时，需要验证用户的有效性...
					@Override
					public void onClick(View v) {
						server = serverEditText.getText().toString();
						if (server.equals("")) {
							String serverSentence = getString(R.string.domain_info_not_null);
							Toast.makeText(CloudAccountSetEditActivity.this,serverSentence, Toast.LENGTH_LONG).show();
							return;
						}
						port = portEditText.getText().toString();
						if (port.equals("")) {
							String portSentence = getString(R.string.port_info_not_null);
							Toast.makeText(CloudAccountSetEditActivity.this,portSentence, Toast.LENGTH_LONG).show();
							return;
						}
						username = usernameEditText.getText().toString();
						if (username.equals("")) {
							String usernameSentence = getString(R.string.username_info_not_null);
							Toast.makeText(CloudAccountSetEditActivity.this,usernameSentence, Toast.LENGTH_LONG).show();
							return;
						}
						psd = passwordEditText.getText().toString();
						if (psd.equals("")) {
							String psdSentence = getString(R.string.password_info_not_null);
							Toast.makeText(CloudAccountSetEditActivity.this,psdSentence, Toast.LENGTH_LONG).show();
							return;
						}
						boolean isEnable = false;
						if (isenablYseRadioBtn.isChecked()) {
							isEnable = true;
						}
						clickCloudAccount.setEnabled(isEnable);

						if (!server.equals("") && !port.equals("")
								&& !psd.equals("") && !username.equals("")) {
							clickCloudAccount.setDomain(server);
							clickCloudAccount.setPort(port);
							clickCloudAccount.setPassword(psd);
							clickCloudAccount.setUsername(username);
							requset4DeviceList();
							synObj.suspend();// 挂起等待请求结果
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
		super.setRightButtonBg(R.drawable.navigation_bar_add_btn_selector);
		super.setToolbarVisiable(false);
		super.setLeftButtonBg(R.drawable.navigation_bar_back_btn_selector);
		serverEditText = (EditText) findViewById(R.id.cloudaccount_set_server_edittext);
		portEditText = (EditText) findViewById(R.id.cloudaccount_set_port_edittext);
		usernameEditText = (EditText) findViewById(R.id.cloudaccount_set_username_edittext);
		passwordEditText = (EditText) findViewById(R.id.cloudaccount_set_password_edittext);
		isenablYseRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_set_isenable_yes_radioBtn);
		isenablNoRadioBtn = (RadioButton) findViewById(R.id.cloudaccount_set_isenable_no_radioBtn);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			ProgressDialog progress = ProgressDialog.show(this, "",getString(R.string.identify_user_right), true, true);
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
				Document doc = cloudService.SendURLPost(server, port, username,psd);
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
}